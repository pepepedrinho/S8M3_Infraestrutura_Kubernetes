package main

import (
	"fmt"
	"io"
	"log"
	"net/http"
	"net/url"
	"os"
	"strings"
	"time"
)

const backendURL = "https://backend-571647583940.southamerica-east1.run.app"
const backendHost = "backend-571647583940.southamerica-east1.run.app"

// getIDToken obtém um Google ID Token via metadata server do Cloud Run.
// Esse token é usado SOMENTE para autenticação de serviço (IAM Cloud Run),
// nunca para autenticar o usuário final.
func getIDToken() (string, error) {
	req, err := http.NewRequest(
		"GET",
		"http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/identity?audience="+url.QueryEscape(backendURL),
		nil,
	)
	if err != nil {
		return "", err
	}

	req.Header.Set("Metadata-Flavor", "Google")

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return "", fmt.Errorf("metadata server retornou %d: %s", resp.StatusCode, string(body))
	}

	token, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", err
	}

	return strings.TrimSpace(string(token)), nil
}

func handler(w http.ResponseWriter, r *http.Request) {

	// Login não precisa de JWT de usuário ainda
	if r.URL.Path == "/api/auth/login" {
		proxyRequest(w, r, "")
		return
	}

	// Demais rotas: exige que o Angular tenha mandado um Bearer token
	// (o JWT DA APLICAÇÃO, não um token Google). A validação de assinatura
	// desse JWT é responsabilidade do backend (JwtService), não do proxy.
	authHeader := r.Header.Get("Authorization")

	if authHeader == "" || !strings.HasPrefix(authHeader, "Bearer ") {
		http.Error(w, "Não autenticado", http.StatusUnauthorized)
		return
	}

	proxyRequest(w, r, authHeader)
}

// proxyRequest encaminha a requisição para o backend.
// userAuthHeader (se presente) é o "Authorization: Bearer <jwt-do-app>"
// que veio do navegador. Ele é reencaminhado em um header PRÓPRIO
// (X-App-Authorization), para não colidir com o Authorization usado
// pelo IAM do Cloud Run.
func proxyRequest(w http.ResponseWriter, r *http.Request, userAuthHeader string) {

	token, err := getIDToken()
	if err != nil {
		log.Printf("Erro obtendo ID token: %v", err)
		http.Error(w, "Erro de autenticação", http.StatusBadGateway)
		return
	}

	targetURL := backendURL + r.URL.RequestURI()

	req, err := http.NewRequestWithContext(
		r.Context(),
		r.Method,
		targetURL,
		r.Body,
	)
	if err != nil {
		http.Error(w, "Erro criando requisição", http.StatusBadGateway)
		return
	}

	// Copia todos os headers originais primeiro
	for key, values := range r.Header {
		for _, value := range values {
			req.Header.Add(key, value)
		}
	}

	// Move o JWT do usuário (se houver) para um header próprio
	if userAuthHeader != "" {
		req.Header.Set("X-App-Authorization", userAuthHeader)
	} else {
		req.Header.Del("X-App-Authorization")
	}

	// Authorization agora é EXCLUSIVAMENTE do IAM do Cloud Run
	req.Header.Set("Authorization", "Bearer "+token)
	req.Host = backendHost

	client := &http.Client{
		Timeout: 60 * time.Second,
	}

	resp, err := client.Do(req)
	if err != nil {
		log.Printf("Erro chamando backend: %v", err)
		http.Error(w, "Erro ao comunicar com backend", http.StatusBadGateway)
		return
	}
	defer resp.Body.Close()

	for key, values := range resp.Header {
		w.Header().Add(key, strings.Join(values, ", "))
	}

	w.WriteHeader(resp.StatusCode)
	io.Copy(w, resp.Body)
}

func main() {
	port := os.Getenv("PORT")
	if port == "" {
		port = "8081"
	}

	log.Printf("Auth proxy iniciado na porta %s", port)

	http.HandleFunc("/", handler)

	log.Fatal(http.ListenAndServe(":"+port, nil))
}
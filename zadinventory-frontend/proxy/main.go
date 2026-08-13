package main

import (
	"fmt"
	"io"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
	"strings"
	"time"
)

const backendURL = "https://backend-571647583940.southamerica-east1.run.app"

// Rotas que o Angular precisa chamar antes de existir um JWT de usuario.
// /api/usuarios/criar-inicial nao tem @PreAuthorize no backend: e o endpoint
// que cria o primeiro GERENTE num ambiente zerado. Sem esta excecao o proxy
// devolvia 401 e o bootstrap era impossivel pelo frontend.
var rotasPublicas = map[string]bool{
	"/api/auth/login":             true,
	"/api/usuarios/criar-inicial": true,
}

// getIDToken obtem um Google ID Token via metadata server do Cloud Run.
// Esse token autentica o SERVICO (IAM do Cloud Run), nunca o usuario final.
// Usa apenas a stdlib de proposito: a biblioteca google.golang.org/api/idtoken
// arrasta uma cadeia grande de dependencias e exige Go >= 1.24.
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

func main() {
	alvo, err := url.Parse(backendURL)
	if err != nil {
		log.Fatalf("backendURL invalida: %v", err)
	}

	// ReverseProxy da stdlib cuida sozinho dos headers hop-by-hop
	// (Connection, Transfer-Encoding, ...) e preserva corretamente headers
	// repetidos como Set-Cookie, que a copia manual anterior corrompia.
	proxy := &httputil.ReverseProxy{
		Director: func(req *http.Request) {
			req.URL.Scheme = alvo.Scheme
			req.URL.Host = alvo.Host
			// O Cloud Run roteia pelo Host: tem que ser o do backend.
			req.Host = alvo.Host
		},
		Transport: &http.Transport{
			ResponseHeaderTimeout: 60 * time.Second,
		},
		ErrorHandler: func(w http.ResponseWriter, r *http.Request, err error) {
			log.Printf("Erro chamando backend em %s: %v", r.URL.Path, err)
			http.Error(w, "Erro ao comunicar com backend", http.StatusBadGateway)
		},
	}

	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		publica := rotasPublicas[r.URL.Path]
		userAuth := r.Header.Get("Authorization")

		if !publica && !strings.HasPrefix(userAuth, "Bearer ") {
			http.Error(w, "Não autenticado", http.StatusUnauthorized)
			return
		}

		// O JWT do usuario (HS256) viaja em header proprio; o Authorization fica
		// EXCLUSIVAMENTE para o ID Token do IAM (RS256). Sem essa separacao o
		// backend tentaria validar o token do Google com a chave HMAC da aplicacao.
		// O Del tambem impede que o navegador injete X-App-Authorization por fora.
		if publica || userAuth == "" {
			r.Header.Del("X-App-Authorization")
		} else {
			r.Header.Set("X-App-Authorization", userAuth)
		}

		token, err := getIDToken()
		if err != nil {
			log.Printf("Erro obtendo ID token: %v", err)
			http.Error(w, "Erro de autenticação", http.StatusBadGateway)
			return
		}
		r.Header.Set("Authorization", "Bearer "+token)

		proxy.ServeHTTP(w, r)
	})

	port := os.Getenv("PORT")
	if port == "" {
		port = "8081"
	}

	log.Printf("Auth proxy iniciado na porta %s", port)

	log.Fatal(http.ListenAndServe(":"+port, nil))
}

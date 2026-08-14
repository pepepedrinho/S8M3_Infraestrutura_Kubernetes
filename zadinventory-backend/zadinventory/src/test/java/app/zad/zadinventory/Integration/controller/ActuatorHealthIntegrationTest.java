package app.zad.zadinventory.Integration.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O workflow valida o deploy chamando /actuator/health e exigindo HTTP 200.
 * Este teste garante que o endpoint existe e responde na propria aplicacao,
 * separando um problema de configuracao de um problema de infraestrutura:
 * se aqui passar e o smoke test falhar, a causa esta no Cloud Run, nao no
 * codigo.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("TESTE DE INTEGRAÇÃO - Actuator health")
class ActuatorHealthIntegrationTest {

    @LocalServerPort
    private int porta;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("GET /actuator/health deve responder 200 com status UP")
    void healthDeveResponderUp() {
        ResponseEntity<String> resposta =
                restTemplate.getForEntity(url("/actuator/health"), String.class);

        assertEquals(200, resposta.getStatusCode().value(),
                "o endpoint usado na validacao do deploy precisa responder 200");
        assertTrue(resposta.getBody() != null && resposta.getBody().contains("UP"),
                "o corpo deveria indicar status UP, veio: " + resposta.getBody());
    }

    @Test
    @DisplayName("Os demais endpoints do Actuator continuam fechados")
    void demaisEndpointsFicamFechados() {
        // application.properties expoe apenas health; env vazaria configuracao.
        // O status exato nao importa - a rota inexistente passa pelo
        // GlobalExceptionHandler e vira 500 - o que importa e nao ser 200 e
        // nao devolver o conteudo do endpoint.
        ResponseEntity<String> resposta =
                restTemplate.getForEntity(url("/actuator/env"), String.class);

        assertNotEquals(200, resposta.getStatusCode().value(),
                "somente o health deveria estar exposto");

        String corpo = resposta.getBody() == null ? "" : resposta.getBody();
        assertFalse(corpo.contains("propertySources"),
                "o corpo nao pode expor a configuracao da aplicacao");
    }

    private String url(String caminho) {
        return "http://localhost:" + porta + caminho;
    }
}

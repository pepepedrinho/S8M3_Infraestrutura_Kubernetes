package app.zad.zadinventory.unit.config;

import app.zad.zadinventory.config.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Garante a separação entre o JWT da aplicação (HS256) e o Google ID Token do
 * IAM do Cloud Run (RS256) — a causa do UnsupportedJwtException em produção.
 */
@DisplayName("TESTE DE UNIDADE - JwtService (HS256 x RS256)")
class JwtServiceUnitTest {

    private static final String SEGREDO_BASE64 =
            "emFkaW52ZW50b3J5LWNoYXZlLWRlLXRlc3RlLXNvbWVudGUtcGFyYS11bml0LXRlc3RzLTAxMjM0NTY3ODk=";

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SEGREDO_BASE64);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86_400_000L);

        userDetails = new User("admin@zad.com", "hash", Collections.emptyList());
    }

    /** Imita o Google ID Token que o Auth Proxy usa para o IAM: assinado com RS256. */
    private String tokenRs256() {
        KeyPair par = Keys.keyPairFor(SignatureAlgorithm.RS256);
        return Jwts.builder()
                .setSubject("571647583940-compute@developer.gserviceaccount.com")
                .setIssuer("https://accounts.google.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(par.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    @Test
    @DisplayName("Token HS256 da aplicação é aceito e o subject é lido")
    void tokenDaAplicacaoEhAceito() {
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isJwtDaAplicacao(token));
        assertEquals("admin@zad.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    @DisplayName("Google ID Token (RS256) é identificado como token de outra origem")
    void tokenRs256NaoEhDaAplicacao() {
        assertFalse(jwtService.isJwtDaAplicacao(tokenRs256()));
    }

    @Test
    @DisplayName("Google ID Token (RS256) é rejeitado antes da verificação de assinatura")
    void tokenRs256EhRejeitado() {
        String google = tokenRs256();

        UnsupportedJwtException ex = assertThrows(
                UnsupportedJwtException.class,
                () -> jwtService.extractUsername(google));

        assertTrue(ex.getMessage().contains("RS256"),
                "a mensagem deve identificar o algoritmo recusado: " + ex.getMessage());
    }

    @Test
    @DisplayName("Texto que não é JWT não quebra a checagem de algoritmo")
    void tokenMalformadoNaoEhDaAplicacao() {
        assertFalse(jwtService.isJwtDaAplicacao("nao-e-um-jwt"));
        assertFalse(jwtService.isJwtDaAplicacao(""));
    }
}

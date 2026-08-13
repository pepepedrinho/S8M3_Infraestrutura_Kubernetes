package app.zad.zadinventory.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    /** Usado só para ler o cabeçalho JOSE (não verificado) e decidir rejeições. */
    private static final ObjectMapper JOSE_MAPPER = new ObjectMapper();

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Diz se o token é um JWT desta aplicação (HS256), sem verificar assinatura.
     *
     * <p>Serve para o filtro descartar de forma explícita o Google ID Token do IAM
     * do Cloud Run, que é RS256. Ler o cabeçalho JOSE sem verificar é seguro aqui
     * porque o resultado só é usado para <em>rejeitar</em>, nunca para confiar.
     */
    public boolean isJwtDaAplicacao(String token) {
        try {
            return SignatureAlgorithm.HS256.getValue().equals(extrairAlgoritmo(token));
        } catch (Exception ex) {
            return false;
        }
    }

    private String extrairAlgoritmo(String token) {
        int fimDoHeader = token.indexOf('.');
        if (fimDoHeader <= 0) {
            throw new MalformedJwtException("Token JWT malformado: cabeçalho ausente");
        }
        try {
            JsonNode header = JOSE_MAPPER.readTree(
                    Decoders.BASE64URL.decode(token.substring(0, fimDoHeader)));
            return header.path("alg").asText();
        } catch (MalformedJwtException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MalformedJwtException("Cabeçalho JWT ilegível", ex);
        }
    }

    private Claims extractAllClaims(String token) {
        // Fixa o algoritmo ANTES de verificar a assinatura. Sem isto, um token
        // RS256 (o ID Token do IAM) só era recusado pela exceção interna do jjwt
        // — a origem do UnsupportedJwtException que derrubava a requisição.
        // Também fecha a porta para ataques de confusão de algoritmo.
        String alg = extrairAlgoritmo(token);
        if (!SignatureAlgorithm.HS256.getValue().equals(alg)) {
            throw new UnsupportedJwtException(
                    "Algoritmo '" + alg + "' não é aceito: esta aplicação só valida JWT HS256. "
                            + "Tokens do Google (RS256) não devem chegar ao JwtService.");
        }

        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
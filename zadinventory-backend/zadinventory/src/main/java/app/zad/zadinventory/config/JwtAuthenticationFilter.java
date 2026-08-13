package app.zad.zadinventory.config;

import app.zad.zadinventory.model.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Registrado explicitamente na cadeia do Spring Security (ver SecurityConfig).
 * Nao usar @Component: alem de duplicar o filtro no container de servlets, isso
 * faz o @WebMvcTest carregar o filtro sem o JwtService e quebrar todos os testes.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // IMPORTANTE: o JWT da aplicação (usuário) vem no header X-App-Authorization.
        // O header "Authorization" é reservado exclusivamente para o Google ID Token
        // usado pelo IAM do Cloud Run (frontend -> backend), e NÃO deve ser lido aqui.
        final String authHeader = request.getHeader("X-App-Authorization");
        final String path = request.getRequestURI();

        if (authHeader == null || !authHeader.startsWith("Bearer ") || path.equals("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        // Segunda barreira contra o Google ID Token (RS256): a primeira e o proxy,
        // que so encaminha o JWT do usuario neste header. Se um token RS256 chegar
        // aqui mesmo assim, e descartado sem nunca passar pelo JwtService.
        if (!jwtService.isJwtDaAplicacao(jwt)) {
            logger.warn("Token em X-App-Authorization nao e HS256 (provavel Google ID Token do IAM) - ignorado em " + path);
            filterChain.doFilter(request, response);
            return;
        }

        // Um token invalido/expirado nao pode derrubar a requisicao com erro 500:
        // seguimos sem autenticar e o @PreAuthorize responde 403.
        try {
            String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            logger.warn("Token JWT invalido na requisicao " + path + ": " + ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
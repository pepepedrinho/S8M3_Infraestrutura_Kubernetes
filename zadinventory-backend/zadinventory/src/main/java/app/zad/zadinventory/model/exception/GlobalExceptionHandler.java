package app.zad.zadinventory.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<String> handleRegraNegocioException(RegraNegocioException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Credenciais invalidas no login. Sem este handler o @ExceptionHandler(Exception.class)
     * abaixo capturaria a excecao e devolveria 500 no lugar de 401.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        return new ResponseEntity<>("Credenciais inválidas", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Usuario autenticado, mas sem permissao (@PreAuthorize). Idem: sem este handler
     * o cliente receberia 500 em vez de 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return new ResponseEntity<>("Acesso negado", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex, WebRequest request) {
        return new ResponseEntity<>("Ocorreu um erro interno: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

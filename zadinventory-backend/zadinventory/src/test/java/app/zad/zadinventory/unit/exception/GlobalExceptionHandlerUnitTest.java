package app.zad.zadinventory.unit.exception;

import app.zad.zadinventory.model.exception.GlobalExceptionHandler;
import app.zad.zadinventory.model.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("TESTE DE UNIDADE - GlobalExceptionHandler")
class GlobalExceptionHandlerUnitTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Cenário com RegraNegocioException deve retornar status 400")
    void deveHandleRegraNegocioException() {
        // Arrange
        RegraNegocioException exception = new RegraNegocioException("Erro de negócio");

        // Act
        ResponseEntity<String> response = exceptionHandler.handleRegraNegocioException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Erro de negócio", response.getBody());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Cenário com Exception genérica deve retornar status 500")
    void deveHandleGlobalException() {
        // Arrange
        Exception exception = new Exception("Erro interno");

        // Act
        ResponseEntity<String> response = exceptionHandler.handleGlobalException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Ocorreu um erro interno: Erro interno"));
    }
}
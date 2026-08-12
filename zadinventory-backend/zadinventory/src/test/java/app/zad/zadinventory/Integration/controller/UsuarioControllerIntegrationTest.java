package app.zad.zadinventory.Integration.controller;

import app.zad.zadinventory.controller.UsuarioController;
import app.zad.zadinventory.model.entity.UsuarioEntity;
import app.zad.zadinventory.model.enums.TipoUsuario;
import app.zad.zadinventory.model.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@ExtendWith(SpringExtension.class)
@Import(UsuarioControllerIntegrationTest.TestConfig.class)
@DisplayName("TESTE DE INTEGRAÇÃO - UsuarioController")
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioService usuarioService;

    private UsuarioEntity usuario;

    @BeforeEach
    void setUp() {
        usuario = UsuarioEntity.builder()
                .id(1L)
                .email("teste@email.com")
                .senha("senha123")
                .tipoUsuario(TipoUsuario.FUNCIONARIO)
                .nome("Usuário Teste")
                .build();
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Cenário com dados válidos deve criar usuário com status 200")
    void deveCriarUsuarioComDadosValidos() throws Exception {
        // Arrange
        when(usuarioService.salvar(any(UsuarioEntity.class))).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("teste@email.com"))
                .andExpect(jsonPath("$.tipoUsuario").value("FUNCIONARIO"))
                .andExpect(jsonPath("$.nome").value("Usuário Teste"));

        verify(usuarioService, times(1)).salvar(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Cenário deve listar todos os usuários")
    void deveListarTodosOsUsuarios() throws Exception {
        // Arrange
        List<UsuarioEntity> usuarios = Arrays.asList(usuario);
        when(usuarioService.buscarTodos()).thenReturn(usuarios);

        // Act & Assert
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("teste@email.com"))
                .andExpect(jsonPath("$[0].tipoUsuario").value("FUNCIONARIO"));

        verify(usuarioService, times(1)).buscarTodos();
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Cenário com ID existente deve retornar usuário")
    void deveBuscarUsuarioPorIdExistente() throws Exception {
        // Arrange
        when(usuarioService.buscarPorId(1L)).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("teste@email.com"));

        verify(usuarioService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Cenário com email deve retornar usuário")
    void deveBuscarUsuarioPorEmail() throws Exception {
        // Arrange
        when(usuarioService.buscarPorEmail("teste@email.com")).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/por-email")
                        .param("email", "teste@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("teste@email.com"));

        verify(usuarioService, times(1)).buscarPorEmail("teste@email.com");
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Cenário com tipo deve retornar usuários correspondentes")
    void deveBuscarUsuariosPorTipo() throws Exception {
        // Arrange
        List<UsuarioEntity> usuarios = Arrays.asList(usuario);
        when(usuarioService.buscarPorTipo(TipoUsuario.FUNCIONARIO)).thenReturn(usuarios);

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/por-tipo")
                        .param("tipo", "FUNCIONARIO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].tipoUsuario").value("FUNCIONARIO"));

        verify(usuarioService, times(1)).buscarPorTipo(TipoUsuario.FUNCIONARIO);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Cenário com tipo ordenado deve retornar usuários ordenados")
    void deveBuscarUsuariosPorTipoOrdenado() throws Exception {
        // Arrange
        List<UsuarioEntity> usuarios = Arrays.asList(usuario);
        when(usuarioService.buscarPorTipoOrdenado(TipoUsuario.FUNCIONARIO)).thenReturn(usuarios);

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/por-tipo-ordenado")
                        .param("tipo", "FUNCIONARIO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].tipoUsuario").value("FUNCIONARIO"));

        verify(usuarioService, times(1)).buscarPorTipoOrdenado(TipoUsuario.FUNCIONARIO);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Cenário com ID existente deve atualizar usuário")
    void deveAtualizarUsuarioComIdExistente() throws Exception {
        // Arrange
        UsuarioEntity usuarioAtualizado = UsuarioEntity.builder()
                .id(1L)
                .email("novo@email.com")
                .senha("novaSenha")
                .tipoUsuario(TipoUsuario.GERENTE)
                .nome("Novo Nome")
                .build();

        when(usuarioService.atualizar(eq(1L), any(UsuarioEntity.class))).thenReturn(usuarioAtualizado);

        // Act & Assert
        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioAtualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("novo@email.com"))
                .andExpect(jsonPath("$.tipoUsuario").value("GERENTE"))
                .andExpect(jsonPath("$.nome").value("Novo Nome"));

        verify(usuarioService, times(1)).atualizar(eq(1L), any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Cenário com ID existente deve remover usuário com status 204")
    void deveRemoverUsuarioComIdExistente() throws Exception {
        // Arrange
        doNothing().when(usuarioService).remover(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).remover(1L);
    }

    @TestConfiguration
    static class TestConfig {
        @Mock
        private UsuarioService usuarioService;

        @Bean
        public UsuarioService usuarioService() {
            return mock(UsuarioService.class);
        }
    }
}
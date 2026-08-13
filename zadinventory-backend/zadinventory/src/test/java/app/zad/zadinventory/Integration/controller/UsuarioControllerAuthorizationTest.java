package app.zad.zadinventory.Integration.controller;

import app.zad.zadinventory.controller.UsuarioController;
import app.zad.zadinventory.model.entity.UsuarioEntity;
import app.zad.zadinventory.model.enums.TipoUsuario;
import app.zad.zadinventory.model.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Cobre as regras {@code @PreAuthorize} do UsuarioController, que os demais
 * testes de controller não exercitam (eles usam addFilters = false e não
 * habilitam method security, então as anotações ficam inertes).
 *
 * <p>Os filtros continuam desligados de propósito: {@code @WithMockUser} popula o
 * SecurityContext diretamente e o {@code @PreAuthorize} é aplicado por AOP, sem
 * depender da cadeia de filtros nem de CSRF.
 */
@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UsuarioControllerAuthorizationTest.TestConfig.class)
@DisplayName("TESTE DE AUTORIZAÇÃO - UsuarioController")
class UsuarioControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioService usuarioService;

    private UsuarioEntity usuario;

    @BeforeEach
    void setUp() {
        reset(usuarioService);
        usuario = UsuarioEntity.builder()
                .id(1L)
                .email("teste@email.com")
                .senha("hash")
                .tipoUsuario(TipoUsuario.FUNCIONARIO)
                .nome("Usuário Teste")
                .build();
    }

    // ---------- POST /api/usuarios : só GERENTE ----------

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("GERENTE pode criar usuário")
    void gerentePodeCriarUsuario() throws Exception {
        when(usuarioService.salvar(any(UsuarioEntity.class))).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    @DisplayName("FUNCIONARIO NÃO pode criar usuário")
    void funcionarioNaoPodeCriarUsuario() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).salvar(any());
    }

    // ---------- GET /api/usuarios : só GERENTE ----------

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("GERENTE pode listar usuários")
    void gerentePodeListar() throws Exception {
        when(usuarioService.buscarTodos()).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    @DisplayName("FUNCIONARIO NÃO pode listar usuários")
    void funcionarioNaoPodeListar() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).buscarTodos();
    }

    // ---------- GET /api/usuarios/{id} : GERENTE ou FUNCIONARIO ----------

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    @DisplayName("FUNCIONARIO pode buscar usuário por id")
    void funcionarioPodeBuscarPorId() throws Exception {
        when(usuarioService.buscarPorId(1L)).thenReturn(usuario);

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk());
    }

    // ---------- GET /api/usuarios/por-tipo : só GERENTE ----------

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    @DisplayName("FUNCIONARIO NÃO pode filtrar usuários por tipo")
    void funcionarioNaoPodeBuscarPorTipo() throws Exception {
        mockMvc.perform(get("/api/usuarios/por-tipo").param("tipo", "GERENTE"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).buscarPorTipo(any());
    }

    // ---------- DELETE /api/usuarios/{id} : só GERENTE ----------

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("GERENTE pode remover usuário")
    void gerentePodeRemover() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(usuarioService).remover(1L);
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    @DisplayName("FUNCIONARIO NÃO pode remover usuário")
    void funcionarioNaoPodeRemover() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).remover(anyLong());
    }

    // ---------- Sem autenticação ----------

    @Test
    @WithAnonymousUser
    @DisplayName("Anônimo NÃO acessa endpoint protegido")
    void anonimoNaoAcessaEndpointProtegido() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).buscarTodos();
    }

    // ---------- /criar-inicial é público (bootstrap) ----------

    @Test
    @WithAnonymousUser
    @DisplayName("Anônimo PODE chamar /criar-inicial quando não há usuários")
    void anonimoPodeCriarUsuarioInicial() throws Exception {
        when(usuarioService.buscarTodos()).thenReturn(List.of());
        when(usuarioService.salvar(any(UsuarioEntity.class))).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/criar-inicial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk());
    }

    /** Habilita o @PreAuthorize no slice: o SecurityConfig real não é carregado aqui. */
    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestConfig {
        @Bean
        public UsuarioService usuarioService() {
            return mock(UsuarioService.class);
        }
    }
}

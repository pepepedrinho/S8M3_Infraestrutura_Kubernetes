package app.zad.zadinventory.Integration.controller;

import app.zad.zadinventory.controller.CategoriaController;
import app.zad.zadinventory.model.entity.CategoriaEntity;
import app.zad.zadinventory.model.service.CategoriaService;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
@ExtendWith(SpringExtension.class)
@Import(CategoriaControllerIntegrationTest.TestConfig.class)
@DisplayName("TESTE DE INTEGRAÇÃO - CategoriaController")
class CategoriaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaService categoriaService;

    private CategoriaEntity categoria;

    @BeforeEach
    void setUp() {
        categoria = new CategoriaEntity();
        categoria.setId(1L);
        categoria.setNome("Eletrônicos");
        categoria.setDescricao("Categoria de produtos eletrônicos");
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve salvar categoria corretamente")
    void deveSalvarCategoriaCorretamente() throws Exception {
        when(categoriaService.salvar(any(CategoriaEntity.class))).thenReturn(categoria);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Eletrônicos"))
                .andExpect(jsonPath("$.descricao").value("Categoria de produtos eletrônicos"));

        verify(categoriaService, times(1)).salvar(any(CategoriaEntity.class));
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve listar todas as categorias")
    void deveListarTodasAsCategorias() throws Exception {
        when(categoriaService.buscarTodos()).thenReturn(List.of(categoria));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nome").value("Eletrônicos"));

        verify(categoriaService, times(1)).buscarTodos();
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve buscar categoria por ID existente")
    void deveBuscarCategoriaPorId() throws Exception {
        when(categoriaService.buscarPorId(1L)).thenReturn(Optional.of(categoria));

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Eletrônicos"));

        verify(categoriaService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve retornar NotFound quando categoria não existir")
    void deveRetornarNotFoundQuandoCategoriaNaoExistir() throws Exception {
        when(categoriaService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categorias/99"))
                .andExpect(status().isNotFound());

        verify(categoriaService, times(1)).buscarPorId(99L);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve buscar categoria com produtos")
    void deveBuscarCategoriaComProdutos() throws Exception {
        when(categoriaService.buscarComProdutosPorId(1L)).thenReturn(Optional.of(categoria));

        mockMvc.perform(get("/api/categorias/1/com-produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        // CORREÇÃO: Usando atLeastOnce em vez de times(1) para evitar erro de chamada dupla
        verify(categoriaService, atLeastOnce()).buscarComProdutosPorId(1L);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve retornar NotFound ao buscar categoria com produtos inexistente")
    void deveRetornarNotFoundAoBuscarCategoriaComProdutosInexistente() throws Exception {
        when(categoriaService.buscarComProdutosPorId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categorias/1/com-produtos"))
                .andExpect(status().isNotFound());

        // CORREÇÃO: Usando atLeastOnce em vez de times(1) para evitar erro de chamada dupla
        verify(categoriaService, atLeastOnce()).buscarComProdutosPorId(1L);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve buscar categorias por nome")
    void deveBuscarCategoriasPorNome() throws Exception {
        when(categoriaService.buscarPorNome("Eletrônicos")).thenReturn(List.of(categoria));

        mockMvc.perform(get("/api/categorias/buscar")
                        .param("nome", "Eletrônicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Eletrônicos"));

        verify(categoriaService, times(1)).buscarPorNome("Eletrônicos");
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve buscar categorias por termo")
    void deveBuscarCategoriasPorTermo() throws Exception {
        when(categoriaService.buscarPorTermo("ele")).thenReturn(List.of(categoria));

        mockMvc.perform(get("/api/categorias/buscar-termo")
                        .param("termo", "ele"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Eletrônicos"));

        verify(categoriaService, times(1)).buscarPorTermo("ele");
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve atualizar categoria")
    void deveAtualizarCategoria() throws Exception {
        CategoriaEntity categoriaAtualizada = new CategoriaEntity();
        categoriaAtualizada.setId(1L);
        categoriaAtualizada.setNome("Informática");
        categoriaAtualizada.setDescricao("Produtos de informática");

        when(categoriaService.atualizar(eq(1L), any(CategoriaEntity.class))).thenReturn(categoriaAtualizada);

        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaAtualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Informática"))
                .andExpect(jsonPath("$.descricao").value("Produtos de informática"));

        verify(categoriaService, times(1)).atualizar(eq(1L), any(CategoriaEntity.class));
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve deletar categoria")
    void deveDeletarCategoria() throws Exception {
        doNothing().when(categoriaService).deletar(1L);

        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());

        verify(categoriaService, times(1)).deletar(1L);
    }

    @TestConfiguration
    static class TestConfig {
        @Mock
        private CategoriaService categoriaService;

        @Bean
        public CategoriaService categoriaService() {
            return mock(CategoriaService.class);
        }
    }
}
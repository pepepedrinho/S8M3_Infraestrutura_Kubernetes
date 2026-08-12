package app.zad.zadinventory.Integration.controller;

import app.zad.zadinventory.controller.ProdutoController;
import app.zad.zadinventory.model.entity.ProdutoEntity;
import app.zad.zadinventory.model.service.ProdutoService;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdutoController.class)
@ExtendWith(SpringExtension.class)
@Import(ProdutoControllerIntegrationTest.TestConfig.class)
@DisplayName("TESTE DE INTEGRAÇÃO - ProdutoController")
class ProdutoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProdutoService produtoService;

    private ProdutoEntity produto;
    private List<ProdutoEntity> listaProdutos;

    @BeforeEach
    void setUp() {
        produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setNome("Copo Stanley Tradicional");
        produto.setDescricao("Copo Stanley Tradicional de Teste");
        produto.setPreco(new BigDecimal("90.0"));
        produto.setQuantidade(10);

        ProdutoEntity produto2 = new ProdutoEntity();
        produto2.setId(2L);
        produto2.setNome("Copo Térmico Preto");
        produto2.setDescricao("Copo Térmico Preto de Teste");
        produto2.setPreco(new BigDecimal("120.0"));
        produto2.setQuantidade(5);

        listaProdutos = Arrays.asList(produto, produto2);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve criar produto corretamente")
    void deveCriarProdutoCorretamente() throws Exception {
        when(produtoService.salvar(any(ProdutoEntity.class))).thenReturn(produto);

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Copo Stanley Tradicional"))
                .andExpect(jsonPath("$.preco").value(90.0));

        verify(produtoService, times(1)).salvar(any(ProdutoEntity.class));
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve listar todos os produtos")
    void deveListarTodosOsProdutos() throws Exception {
        when(produtoService.buscarTodos()).thenReturn(listaProdutos);

        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(produtoService, times(1)).buscarTodos();
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve buscar produto por ID")
    void deveBuscarProdutoPorId() throws Exception {
        when(produtoService.buscarPorId(anyLong())).thenReturn(produto);

        mockMvc.perform(get("/api/produtos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Copo Stanley Tradicional"));

        verify(produtoService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve buscar produto por nome")
    void deveBuscarProdutoPorNome() throws Exception {
        when(produtoService.buscarPorNome(anyString())).thenReturn(listaProdutos);

        mockMvc.perform(get("/api/produtos/por-nome")
                        .param("nome", "Copo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Copo Stanley Tradicional"));

        verify(produtoService, times(1)).buscarPorNome("Copo");
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve buscar produtos por categoria")
    void deveBuscarPorCategoria() throws Exception {
        when(produtoService.buscarPorCategoria(anyLong())).thenReturn(listaProdutos);

        mockMvc.perform(get("/api/produtos/por-categoria/{categoriaId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].nome").value("Copo Térmico Preto"));

        verify(produtoService, times(1)).buscarPorCategoria(1L);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve buscar produtos com baixo estoque")
    void deveBuscarComBaixoEstoque() throws Exception {
        when(produtoService.buscarComBaixoEstoque(any())).thenReturn(List.of(produto));

        mockMvc.perform(get("/api/produtos/baixo-estoque")
                        .param("quantidadeMinima", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(produtoService, times(1)).buscarComBaixoEstoque(5);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve atualizar produto")
    void deveAtualizarProduto() throws Exception {
        when(produtoService.atualizar(anyLong(), any(ProdutoEntity.class))).thenReturn(produto);

        mockMvc.perform(put("/api/produtos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Copo Stanley Tradicional"));

        verify(produtoService, times(1)).atualizar(eq(1L), any(ProdutoEntity.class));
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Deve remover produto")
    void deveRemoverProduto() throws Exception {
        doNothing().when(produtoService).remover(anyLong());

        mockMvc.perform(delete("/api/produtos/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(produtoService, times(1)).remover(1L);
    }

    @TestConfiguration
    static class TestConfig {
        @Mock
        private ProdutoService produtoService;

        @Bean
        public ProdutoService produtoService() {
            return mock(ProdutoService.class);
        }
    }
}
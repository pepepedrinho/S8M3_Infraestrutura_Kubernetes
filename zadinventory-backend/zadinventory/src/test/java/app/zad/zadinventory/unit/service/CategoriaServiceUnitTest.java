package app.zad.zadinventory.unit.service;

import app.zad.zadinventory.model.entity.CategoriaEntity;
import app.zad.zadinventory.model.entity.ProdutoEntity;
import app.zad.zadinventory.model.exception.RegraNegocioException;
import app.zad.zadinventory.model.repository.CategoriaRepository;
import app.zad.zadinventory.model.service.CategoriaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TESTE DE UNIDADE - CategoriaService")
class CategoriaServiceUnitTest {

    @Mock
    private CategoriaRepository repository;

    @InjectMocks
    private CategoriaService service;

    @Test
    @DisplayName("Deve salvar categoria com dados válidos")
    void deveSalvarCategoriaComDadosValidos() {
        // Arrange
        CategoriaEntity categoriaInput = new CategoriaEntity();
        categoriaInput.setNome("Eletrônicos");
        categoriaInput.setDescricao("Dispositivos eletrônicos");

        CategoriaEntity categoriaSalva = new CategoriaEntity();
        categoriaSalva.setId(1L);
        categoriaSalva.setNome("Eletrônicos");
        categoriaSalva.setDescricao("Dispositivos eletrônicos");

        when(repository.existsByNomeIgnoreCase("Eletrônicos")).thenReturn(false);
        when(repository.save(any(CategoriaEntity.class))).thenReturn(categoriaSalva);

        // Act
        CategoriaEntity resultado = service.salvar(categoriaInput);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Eletrônicos", resultado.getNome());
        verify(repository).existsByNomeIgnoreCase("Eletrônicos");
        verify(repository).save(any(CategoriaEntity.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome for vazio")
    void deveLancarExcecaoQuandoNomeForVazio() {
        // Arrange
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setNome("");
        categoria.setDescricao("Descrição");

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(categoria));

        assertEquals("O campo nome é obrigatório", ex.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome for nulo")
    void deveLancarExcecaoQuandoNomeForNulo() {
        // Arrange
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setNome(null);
        categoria.setDescricao("Descrição");

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(categoria));

        assertEquals("O campo nome é obrigatório", ex.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome já existir")
    void deveLancarExcecaoQuandoNomeForDuplicado() {
        // Arrange
        when(repository.existsByNomeIgnoreCase("Existente")).thenReturn(true);

        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setNome("Existente");
        categoria.setDescricao("Descrição");

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(categoria));

        assertEquals("Já existe categoria com esse nome: Existente", ex.getMessage());
        verify(repository).existsByNomeIgnoreCase("Existente");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve definir descrição padrão quando descrição for vazia")
    void deveDefinirDescricaoPadraoQuandoDescricaoForVazia() {
        // Arrange
        CategoriaEntity categoriaInput = new CategoriaEntity();
        categoriaInput.setNome("Eletrônicos");
        categoriaInput.setDescricao("");

        CategoriaEntity categoriaSalva = new CategoriaEntity();
        categoriaSalva.setId(1L);
        categoriaSalva.setNome("Eletrônicos");
        categoriaSalva.setDescricao("Descrição padrão");

        when(repository.existsByNomeIgnoreCase("Eletrônicos")).thenReturn(false);
        when(repository.save(any(CategoriaEntity.class))).thenReturn(categoriaSalva);

        // Act
        CategoriaEntity resultado = service.salvar(categoriaInput);

        // Assert
        assertEquals("Descrição padrão", resultado.getDescricao());
        verify(repository).existsByNomeIgnoreCase("Eletrônicos");
        verify(repository).save(any(CategoriaEntity.class));
    }

    @Test
    @DisplayName("Deve atualizar categoria com dados válidos")
    void deveAtualizarCategoriaComDadosValidos() {
        // Arrange
        CategoriaEntity existente = new CategoriaEntity();
        existente.setId(1L);
        existente.setNome("Antigo");
        existente.setDescricao("Descrição antiga");

        CategoriaEntity atualizado = new CategoriaEntity();
        atualizado.setNome("Novo");
        atualizado.setDescricao("Nova descrição");

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByNomeIgnoreCase("Novo")).thenReturn(false);
        when(repository.save(any(CategoriaEntity.class))).thenReturn(existente);

        // Act
        CategoriaEntity resultado = service.atualizar(1L, atualizado);

        // Assert
        assertEquals("Novo", resultado.getNome());
        assertEquals("Nova descrição", resultado.getDescricao());
        verify(repository).findById(1L);
        verify(repository).existsByNomeIgnoreCase("Novo");
        verify(repository).save(existente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar categoria inexistente")
    void deveLancarExcecaoAoAtualizarCategoriaInexistente() {
        // Arrange
        CategoriaEntity atualizado = new CategoriaEntity();
        atualizado.setNome("Novo");
        atualizado.setDescricao("Nova descrição");

        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.atualizar(99L, atualizado));

        assertEquals("Categoria não encontrada com id: 99", ex.getMessage());
        verify(repository).findById(99L);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar categoria por ID existente")
    void deveBuscarCategoriaPorIdExistente() {
        // Arrange
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1L);
        categoria.setNome("Eletrônicos");
        categoria.setDescricao("Descrição");

        when(repository.findById(1L)).thenReturn(Optional.of(categoria));

        // Act
        Optional<CategoriaEntity> resultado = service.buscarPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Eletrônicos", resultado.get().getNome());
        verify(repository).findById(1L);
    }

    @Test
    @DisplayName("Deve buscar categoria com produtos por ID existente")
    void deveBuscarCategoriaComProdutosPorIdExistente() {
        // Arrange
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1L);
        categoria.setNome("Eletrônicos");
        categoria.setDescricao("Descrição");

        when(repository.findComProdutosById(1L)).thenReturn(Optional.of(categoria));

        // Act
        Optional<CategoriaEntity> resultado = service.buscarComProdutosPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Eletrônicos", resultado.get().getNome());
        verify(repository).findComProdutosById(1L);
    }

    @Test
    @DisplayName("Deve buscar todas as categorias")
    void deveBuscarTodasAsCategorias() {
        // Arrange
        CategoriaEntity cat1 = new CategoriaEntity();
        cat1.setId(1L);
        cat1.setNome("Eletrônicos");

        CategoriaEntity cat2 = new CategoriaEntity();
        cat2.setId(2L);
        cat2.setNome("Livros");

        List<CategoriaEntity> categorias = List.of(cat1, cat2);

        when(repository.findAll()).thenReturn(categorias);

        // Act
        List<CategoriaEntity> resultado = service.buscarTodos();

        // Assert
        assertEquals(2, resultado.size());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve buscar categorias por nome")
    void deveBuscarCategoriasPorNome() {
        // Arrange
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1L);
        categoria.setNome("Eletrônicos");

        List<CategoriaEntity> categorias = List.of(categoria);

        when(repository.findByNomeContainingIgnoreCase("elet")).thenReturn(categorias);

        // Act
        List<CategoriaEntity> resultado = service.buscarPorNome("elet");

        // Assert
        assertEquals(1, resultado.size());
        verify(repository).findByNomeContainingIgnoreCase("elet");
    }

    @Test
    @DisplayName("Deve buscar categorias por termo")
    void deveBuscarCategoriasPorTermo() {
        // Arrange
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1L);
        categoria.setNome("Eletrônicos");
        categoria.setDescricao("Dispositivos");

        List<CategoriaEntity> categorias = List.of(categoria);

        when(repository.buscarPorTermo("dispo")).thenReturn(categorias);

        // Act
        List<CategoriaEntity> resultado = service.buscarPorTermo("dispo");

        // Assert
        assertEquals(1, resultado.size());
        verify(repository).buscarPorTermo("dispo");
    }

    @Test
    @DisplayName("Deve deletar categoria sem produtos")
    void deveDeletarCategoriaSemProdutos() {
        // Arrange
        CategoriaEntity existente = new CategoriaEntity();
        existente.setId(1L);
        existente.setNome("Eletrônicos");
        existente.setDescricao("Descrição");
        existente.setProdutos(new ArrayList<>()); // Lista vazia

        when(repository.findComProdutosById(1L)).thenReturn(Optional.of(existente));
        doNothing().when(repository).delete(existente);

        // Act
        service.deletar(1L);

        // Assert
        verify(repository).findComProdutosById(1L);
        verify(repository).delete(existente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar categoria com produtos")
    void deveLancarExcecaoAoDeletarCategoriaComProdutos() {
        // Arrange
        ProdutoEntity produtoMock = mock(ProdutoEntity.class);
        CategoriaEntity existente = new CategoriaEntity();
        existente.setId(1L);
        existente.setNome("Eletrônicos");
        existente.setDescricao("Descrição");
        existente.setProdutos(List.of(produtoMock)); // Tem produtos

        when(repository.findComProdutosById(1L)).thenReturn(Optional.of(existente));

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.deletar(1L));

        assertEquals("Não é possível excluir categoria vinculada a produtos", ex.getMessage());
        verify(repository).findComProdutosById(1L);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao buscar categoria por ID inexistente")
    void deveRetornarOptionalVazioAoBuscarCategoriaPorIdInexistente() {
        // Arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<CategoriaEntity> resultado = service.buscarPorId(99L);

        // Assert
        assertTrue(resultado.isEmpty());
        verify(repository).findById(99L);
    }
}
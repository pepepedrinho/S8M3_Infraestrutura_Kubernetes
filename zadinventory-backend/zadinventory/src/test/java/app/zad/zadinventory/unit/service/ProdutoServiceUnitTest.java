package app.zad.zadinventory.unit.service;

import app.zad.zadinventory.model.entity.CategoriaEntity;
import app.zad.zadinventory.model.entity.ProdutoEntity;
import app.zad.zadinventory.model.entity.UsuarioEntity;
import app.zad.zadinventory.model.entity.TagEntity;
import app.zad.zadinventory.model.enums.TipoUsuario;
import app.zad.zadinventory.model.exception.RegraNegocioException;
import app.zad.zadinventory.model.repository.ProdutoRepository;
import app.zad.zadinventory.model.repository.CategoriaRepository;
import app.zad.zadinventory.model.repository.UsuarioRepository;
import app.zad.zadinventory.model.repository.TagRepository;
import app.zad.zadinventory.model.service.ProdutoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TESTE DE UNIDADE - ProdutoService")
class ProdutoServiceUnitTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private ProdutoService service;

    @Test
    @DisplayName("Deve salvar produto com dados válidos")
    void deveSalvarProdutoComDadosValidos() {
        // Arrange
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setEmail("user@test.com");
        usuario.setTipoUsuario(TipoUsuario.FUNCIONARIO);

        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1L);
        categoria.setNome("Eletrônicos");

        TagEntity tag = new TagEntity();
        tag.setId(1L);
        tag.setNome("Tecnologia");

        ProdutoEntity produtoInput = new ProdutoEntity();
        produtoInput.setNome("Notebook");
        produtoInput.setDescricao("Notebook Dell");
        produtoInput.setQuantidade(10);
        produtoInput.setPreco(BigDecimal.valueOf(3500.0));

        UsuarioEntity usuarioInput = new UsuarioEntity();
        usuarioInput.setId(1L);
        produtoInput.setUsuario(usuarioInput);

        CategoriaEntity categoriaInput = new CategoriaEntity();
        categoriaInput.setId(1L);
        produtoInput.setCategoria(categoriaInput);

        produtoInput.setTags(List.of(tag));

        ProdutoEntity produtoSalvo = new ProdutoEntity();
        produtoSalvo.setId(1L);
        produtoSalvo.setNome("Notebook");
        produtoSalvo.setDescricao("Notebook Dell");
        produtoSalvo.setQuantidade(10);
        produtoSalvo.setPreco(BigDecimal.valueOf(3500.0));
        produtoSalvo.setUsuario(usuario);
        produtoSalvo.setCategoria(categoria);
        produtoSalvo.setTags(List.of(tag));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(produtoRepository.save(any(ProdutoEntity.class))).thenReturn(produtoSalvo);

        // Act
        ProdutoEntity resultado = service.salvar(produtoInput);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Notebook", resultado.getNome());
        assertEquals(usuario, resultado.getUsuario());
        assertEquals(categoria, resultado.getCategoria());
        assertEquals(1, resultado.getTags().size());
        verify(usuarioRepository).findById(1L);
        verify(categoriaRepository).findById(1L);
        verify(tagRepository).findById(1L);
        verify(produtoRepository).save(any(ProdutoEntity.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome for vazio")
    void deveLancarExcecaoQuandoNomeForVazio() {
        // Arrange
        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome("");
        produto.setDescricao("Descrição");
        produto.setQuantidade(10);
        produto.setPreco(BigDecimal.valueOf(100.0));

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);
        produto.setUsuario(usuario);

        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1L);
        produto.setCategoria(categoria);

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(produto));

        assertEquals("Nome do produto é obrigatório!", ex.getMessage());
        verifyNoInteractions(produtoRepository, usuarioRepository, categoriaRepository, tagRepository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade for negativa")
    void deveLancarExcecaoQuandoQuantidadeForNegativa() {
        // Arrange
        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome("Produto");
        produto.setDescricao("Descrição");
        produto.setQuantidade(-1);
        produto.setPreco(BigDecimal.valueOf(100.0));

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);
        produto.setUsuario(usuario);

        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1L);
        produto.setCategoria(categoria);

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(produto));

        assertEquals("Quantidade não pode ser negativa!", ex.getMessage());
        verifyNoInteractions(produtoRepository, usuarioRepository, categoriaRepository, tagRepository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando preço for negativo")
    void deveLancarExcecaoQuandoPrecoForNegativo() {
        // Arrange
        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome("Produto");
        produto.setDescricao("Descrição");
        produto.setQuantidade(10);
        produto.setPreco(BigDecimal.valueOf(-100.0));

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);
        produto.setUsuario(usuario);

        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1L);
        produto.setCategoria(categoria);

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(produto));

        assertEquals("Preço não pode ser negativo!", ex.getMessage());
        verifyNoInteractions(produtoRepository, usuarioRepository, categoriaRepository, tagRepository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário for nulo")
    void deveLancarExcecaoQuandoUsuarioForNulo() {
        // Arrange
        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome("Produto");
        produto.setDescricao("Descrição");
        produto.setQuantidade(10);
        produto.setPreco(BigDecimal.valueOf(100.0));
        produto.setUsuario(null);

        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1L);
        produto.setCategoria(categoria);

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(produto));

        assertEquals("Usuário é obrigatório!", ex.getMessage());
        verifyNoInteractions(produtoRepository, usuarioRepository, categoriaRepository, tagRepository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando categoria for nula")
    void deveLancarExcecaoQuandoCategoriaForNula() {
        // Arrange
        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome("Produto");
        produto.setDescricao("Descrição");
        produto.setQuantidade(10);
        produto.setPreco(BigDecimal.valueOf(100.0));

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);
        produto.setUsuario(usuario);

        produto.setCategoria(null);

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(produto));

        assertEquals("Categoria é obrigatória!", ex.getMessage());
        verifyNoInteractions(produtoRepository, usuarioRepository, categoriaRepository, tagRepository);
    }

    @Test
    @DisplayName("Deve definir quantidade padrão como zero quando quantidade for nula")
    void deveDefinirQuantidadePadraoQuandoQuantidadeForNula() {
        // Arrange
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setEmail("user@test.com");
        usuario.setTipoUsuario(TipoUsuario.FUNCIONARIO);

        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1L);
        categoria.setNome("Eletrônicos");

        ProdutoEntity produtoInput = new ProdutoEntity();
        produtoInput.setNome("Produto");
        produtoInput.setDescricao("Descrição");
        produtoInput.setQuantidade(null);
        produtoInput.setPreco(BigDecimal.valueOf(100.0));

        UsuarioEntity usuarioInput = new UsuarioEntity();
        usuarioInput.setId(1L);
        produtoInput.setUsuario(usuarioInput);

        CategoriaEntity categoriaInput = new CategoriaEntity();
        categoriaInput.setId(1L);
        produtoInput.setCategoria(categoriaInput);

        ProdutoEntity produtoSalvo = new ProdutoEntity();
        produtoSalvo.setId(1L);
        produtoSalvo.setNome("Produto");
        produtoSalvo.setDescricao("Descrição");
        produtoSalvo.setQuantidade(0);
        produtoSalvo.setPreco(BigDecimal.valueOf(100.0));
        produtoSalvo.setUsuario(usuario);
        produtoSalvo.setCategoria(categoria);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(any(ProdutoEntity.class))).thenReturn(produtoSalvo);

        // Act
        ProdutoEntity resultado = service.salvar(produtoInput);

        // Assert
        assertEquals(0, resultado.getQuantidade());
        verify(produtoRepository).save(any(ProdutoEntity.class));
    }

    @Test
    @DisplayName("Deve definir preço padrão como zero quando preço for nulo")
    void deveDefinirPrecoPadraoQuandoPrecoForNulo() {
        // Arrange
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setEmail("user@test.com");
        usuario.setTipoUsuario(TipoUsuario.FUNCIONARIO);

        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1L);
        categoria.setNome("Eletrônicos");

        ProdutoEntity produtoInput = new ProdutoEntity();
        produtoInput.setNome("Produto");
        produtoInput.setDescricao("Descrição");
        produtoInput.setQuantidade(10);
        produtoInput.setPreco(null);

        UsuarioEntity usuarioInput = new UsuarioEntity();
        usuarioInput.setId(1L);
        produtoInput.setUsuario(usuarioInput);

        CategoriaEntity categoriaInput = new CategoriaEntity();
        categoriaInput.setId(1L);
        produtoInput.setCategoria(categoriaInput);

        ProdutoEntity produtoSalvo = new ProdutoEntity();
        produtoSalvo.setId(1L);
        produtoSalvo.setNome("Produto");
        produtoSalvo.setDescricao("Descrição");
        produtoSalvo.setQuantidade(10);
        produtoSalvo.setPreco(BigDecimal.ZERO);
        produtoSalvo.setUsuario(usuario);
        produtoSalvo.setCategoria(categoria);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(any(ProdutoEntity.class))).thenReturn(produtoSalvo);

        // Act
        ProdutoEntity resultado = service.salvar(produtoInput);

        // Assert
        assertEquals(BigDecimal.ZERO, resultado.getPreco());
        verify(produtoRepository).save(any(ProdutoEntity.class));
    }

    @Test
    @DisplayName("Deve buscar produto por ID existente")
    void deveBuscarProdutoPorIdExistente() {
        // Arrange
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setNome("Produto");
        produto.setDescricao("Descrição");
        produto.setQuantidade(10);
        produto.setPreco(BigDecimal.valueOf(100.0));

        when(produtoRepository.findComRelacionamentosById(1L)).thenReturn(Optional.of(produto));

        // Act
        ProdutoEntity resultado = service.buscarPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Produto", resultado.getNome());
        verify(produtoRepository).findComRelacionamentosById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não for encontrado por ID")
    void deveLancarExcecaoQuandoProdutoNaoForEncontradoPorId() {
        // Arrange
        when(produtoRepository.findComRelacionamentosById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.buscarPorId(99L));

        assertEquals("Produto não encontrado!", ex.getMessage());
        verify(produtoRepository).findComRelacionamentosById(99L);
    }

    @Test
    @DisplayName("Deve buscar todos os produtos")
    void deveBuscarTodosOsProdutos() {
        // Arrange
        ProdutoEntity produto1 = new ProdutoEntity();
        produto1.setId(1L);
        produto1.setNome("Produto1");

        ProdutoEntity produto2 = new ProdutoEntity();
        produto2.setId(2L);
        produto2.setNome("Produto2");

        List<ProdutoEntity> produtos = List.of(produto1, produto2);

        when(produtoRepository.findAll()).thenReturn(produtos);

        // Act
        List<ProdutoEntity> resultado = service.buscarTodos();

        // Assert
        assertEquals(2, resultado.size());
        verify(produtoRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar produtos por nome")
    void deveBuscarProdutosPorNome() {
        // Arrange
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setNome("Notebook");

        List<ProdutoEntity> produtos = List.of(produto);

        when(produtoRepository.findByNomeContainingIgnoreCase("note")).thenReturn(produtos);

        // Act
        List<ProdutoEntity> resultado = service.buscarPorNome("note");

        // Assert
        assertEquals(1, resultado.size());
        verify(produtoRepository).findByNomeContainingIgnoreCase("note");
    }

    @Test
    @DisplayName("Deve buscar produtos por categoria")
    void deveBuscarProdutosPorCategoria() {
        // Arrange
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setNome("Notebook");

        List<ProdutoEntity> produtos = List.of(produto);

        when(produtoRepository.findByCategoriaId(1L)).thenReturn(produtos);

        // Act
        List<ProdutoEntity> resultado = service.buscarPorCategoria(1L);

        // Assert
        assertEquals(1, resultado.size());
        verify(produtoRepository).findByCategoriaId(1L);
    }

    @Test
    @DisplayName("Deve buscar produtos com baixo estoque")
    void deveBuscarProdutosComBaixoEstoque() {
        // Arrange
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setNome("Notebook");
        produto.setQuantidade(5);

        List<ProdutoEntity> produtos = List.of(produto);

        when(produtoRepository.buscarProdutosComBaixoEstoque(10)).thenReturn(produtos);

        // Act
        List<ProdutoEntity> resultado = service.buscarComBaixoEstoque(10);

        // Assert
        assertEquals(1, resultado.size());
        verify(produtoRepository).buscarProdutosComBaixoEstoque(10);
    }

    @Test
    @DisplayName("Deve remover produto pelo ID")
    void deveRemoverProdutoPeloId() {
        // Act
        service.remover(1L);

        // Assert
        verify(produtoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve atualizar produto com dados válidos")
    void deveAtualizarProdutoComDadosValidos() {
        // Arrange
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setEmail("user@test.com");

        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1L);
        categoria.setNome("Eletrônicos");

        TagEntity tag = new TagEntity();
        tag.setId(1L);
        tag.setNome("Tecnologia");

        ProdutoEntity existente = new ProdutoEntity();
        existente.setId(1L);
        existente.setNome("Produto Antigo");
        existente.setDescricao("Descrição Antiga");
        existente.setQuantidade(5);
        existente.setPreco(BigDecimal.valueOf(50.0));
        existente.setUsuario(usuario);
        existente.setCategoria(categoria);
        existente.setTags(new ArrayList<>());

        ProdutoEntity atualizado = new ProdutoEntity();
        atualizado.setNome("Produto Novo");
        atualizado.setDescricao("Nova Descrição");
        atualizado.setQuantidade(10);
        atualizado.setPreco(BigDecimal.valueOf(100.0));

        UsuarioEntity novoUsuarioInput = new UsuarioEntity();
        novoUsuarioInput.setId(2L);
        atualizado.setUsuario(novoUsuarioInput);

        CategoriaEntity novaCategoriaInput = new CategoriaEntity();
        novaCategoriaInput.setId(2L);
        atualizado.setCategoria(novaCategoriaInput);

        atualizado.setTags(List.of(tag));

        UsuarioEntity novoUsuario = new UsuarioEntity();
        novoUsuario.setId(2L);
        novoUsuario.setEmail("novo@test.com");

        CategoriaEntity novaCategoria = new CategoriaEntity();
        novaCategoria.setId(2L);
        novaCategoria.setNome("Novos");

        when(produtoRepository.findComRelacionamentosById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(novoUsuario));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(novaCategoria));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(produtoRepository.save(any(ProdutoEntity.class))).thenReturn(existente);

        // Act
        ProdutoEntity resultado = service.atualizar(1L, atualizado);

        // Assert
        assertEquals("Produto Novo", resultado.getNome());
        assertEquals("Nova Descrição", resultado.getDescricao());
        assertEquals(10, resultado.getQuantidade());
        assertEquals(BigDecimal.valueOf(100.0), resultado.getPreco());
        assertEquals(novoUsuario, resultado.getUsuario());
        assertEquals(novaCategoria, resultado.getCategoria());
        assertEquals(1, resultado.getTags().size());
        verify(produtoRepository).findComRelacionamentosById(1L);
        verify(produtoRepository).save(existente);
    }
}
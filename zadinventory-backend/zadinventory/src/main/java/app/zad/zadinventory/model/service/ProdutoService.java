package app.zad.zadinventory.model.service;

import app.zad.zadinventory.model.entity.ProdutoEntity;
import app.zad.zadinventory.model.entity.CategoriaEntity;
import app.zad.zadinventory.model.entity.UsuarioEntity;
import app.zad.zadinventory.model.entity.TagEntity;
import app.zad.zadinventory.model.exception.RegraNegocioException;
import app.zad.zadinventory.model.repository.ProdutoRepository;
import app.zad.zadinventory.model.repository.CategoriaRepository;
import app.zad.zadinventory.model.repository.UsuarioRepository;
import app.zad.zadinventory.model.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TagRepository tagRepository;

    @Transactional
    public ProdutoEntity salvar(ProdutoEntity produto) {
        validarProduto(produto);
        carregarEntidadesRelacionadas(produto);

        if (produto.getQuantidade() == null) {
            produto.setQuantidade(0);
        }

        if (produto.getPreco() == null) {
            produto.setPreco(BigDecimal.ZERO);
        }

        // Carregar tags explicitamente
        if (produto.getTags() != null && !produto.getTags().isEmpty()) {
            List<TagEntity> managedTags = produto.getTags().stream()
                    .map(tag -> tagRepository.findById(tag.getId())
                            .orElseThrow(() -> new RegraNegocioException("Tag não encontrada: " + tag.getId())))
                    .collect(Collectors.toList());
            produto.setTags(managedTags);
        }

        return repository.save(produto);
    }

    public List<ProdutoEntity> buscarTodos() {
        return repository.findAll();
    }

    public ProdutoEntity buscarPorId(Long id) {
        return repository.findComRelacionamentosById(id)
                .orElseThrow(() -> new RegraNegocioException("Produto não encontrado!"));
    }

    public List<ProdutoEntity> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    public List<ProdutoEntity> buscarPorCategoria(Long categoriaId) {
        return repository.findByCategoriaId(categoriaId);
    }

    public List<ProdutoEntity> buscarComBaixoEstoque(Integer quantidadeMinima) {
        return repository.buscarProdutosComBaixoEstoque(quantidadeMinima);
    }

    @Transactional
    public ProdutoEntity atualizar(Long id, ProdutoEntity produtoAtualizado) {
        ProdutoEntity produtoExistente = buscarPorId(id);

        if (produtoAtualizado.getNome() != null) {
            produtoExistente.setNome(produtoAtualizado.getNome());
        }
        if (produtoAtualizado.getDescricao() != null) {
            produtoExistente.setDescricao(produtoAtualizado.getDescricao());
        }
        if (produtoAtualizado.getQuantidade() != null) {
            produtoExistente.setQuantidade(produtoAtualizado.getQuantidade());
        }
        if (produtoAtualizado.getPreco() != null) {
            produtoExistente.setPreco(produtoAtualizado.getPreco());
        }

        if (produtoAtualizado.getCategoria() != null && produtoAtualizado.getCategoria().getId() != null) {
            CategoriaEntity categoria = categoriaRepository.findById(produtoAtualizado.getCategoria().getId())
                    .orElseThrow(() -> new RegraNegocioException("Categoria não encontrada!"));
            produtoExistente.setCategoria(categoria);
        }

        if (produtoAtualizado.getUsuario() != null && produtoAtualizado.getUsuario().getId() != null) {
            UsuarioEntity usuario = usuarioRepository.findById(produtoAtualizado.getUsuario().getId())
                    .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado!"));
            produtoExistente.setUsuario(usuario);
        }

        // Atualizar tags
        if (produtoAtualizado.getTags() != null) {
            List<TagEntity> managedTags = produtoAtualizado.getTags().stream()
                    .map(tag -> tagRepository.findById(tag.getId())
                            .orElseThrow(() -> new RegraNegocioException("Tag não encontrada: " + tag.getId())))
                    .collect(Collectors.toList());
            produtoExistente.setTags(managedTags);
        }

        return repository.save(produtoExistente);
    }

    @Transactional
    public void remover(Long id) {
        repository.deleteById(id);
    }

    private void validarProduto(ProdutoEntity produto) {
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new RegraNegocioException("Nome do produto é obrigatório!");
        }
        if (produto.getQuantidade() != null && produto.getQuantidade() < 0) {
            throw new RegraNegocioException("Quantidade não pode ser negativa!");
        }
        if (produto.getPreco() != null && produto.getPreco().compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("Preço não pode ser negativo!");
        }
        if (produto.getUsuario() == null || produto.getUsuario().getId() == null) {
            throw new RegraNegocioException("Usuário é obrigatório!");
        }
        if (produto.getCategoria() == null || produto.getCategoria().getId() == null) {
            throw new RegraNegocioException("Categoria é obrigatória!");
        }
    }

    private void carregarEntidadesRelacionadas(ProdutoEntity produto) {
        UsuarioEntity usuario = usuarioRepository.findById(produto.getUsuario().getId())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado!"));
        produto.setUsuario(usuario);

        CategoriaEntity categoria = categoriaRepository.findById(produto.getCategoria().getId())
                .orElseThrow(() -> new RegraNegocioException("Categoria não encontrada!"));
        produto.setCategoria(categoria);
    }
}

package app.zad.zadinventory.model.service;

import app.zad.zadinventory.model.entity.CategoriaEntity;
import app.zad.zadinventory.model.exception.RegraNegocioException;
import app.zad.zadinventory.model.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public CategoriaEntity salvar(CategoriaEntity categoria) {
        validarCategoriaBasica(categoria);

        if (categoria.getDescricao() == null || categoria.getDescricao().isBlank()) {
            categoria.setDescricao("Descrição padrão");
        }

        if (repository.existsByNomeIgnoreCase(categoria.getNome())) {
            throw new RegraNegocioException("Já existe categoria com esse nome: " + categoria.getNome());
        }

        return repository.save(categoria);
    }

    public CategoriaEntity atualizar(Long id, CategoriaEntity categoria) {
        CategoriaEntity existente = repository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Categoria não encontrada com id: " + id));

        validarCategoriaBasica(categoria);

        if (!existente.getNome().equalsIgnoreCase(categoria.getNome())) {
            if (repository.existsByNomeIgnoreCase(categoria.getNome())) {
                throw new RegraNegocioException("Já existe outra categoria com o nome: " + categoria.getNome());
            }
            existente.setNome(categoria.getNome());
        }

        existente.setDescricao(categoria.getDescricao());
        return repository.save(existente);
    }

    public Optional<CategoriaEntity> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Optional<CategoriaEntity> buscarComProdutosPorId(Long id) {
        return repository.findComProdutosById(id);
    }

    // Método atualizado para usar findAll() padrão
    public List<CategoriaEntity> buscarTodos() {
        return repository.findAll();
    }

    public List<CategoriaEntity> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    public List<CategoriaEntity> buscarPorTermo(String termo) {
        return repository.buscarPorTermo(termo);
    }

    public void deletar(Long id) {
        CategoriaEntity existente = repository.findComProdutosById(id)
                .orElseThrow(() -> new RegraNegocioException("Categoria não encontrada com id: " + id));

        if (!existente.getProdutos().isEmpty()) {
            throw new RegraNegocioException("Não é possível excluir categoria vinculada a produtos");
        }

        repository.delete(existente);
    }

    private void validarCategoriaBasica(CategoriaEntity categoria) {
        if (categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new RegraNegocioException("O campo nome é obrigatório");
        }
    }
}
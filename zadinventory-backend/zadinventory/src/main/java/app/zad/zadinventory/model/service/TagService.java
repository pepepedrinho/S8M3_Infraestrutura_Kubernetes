package app.zad.zadinventory.model.service;

import app.zad.zadinventory.model.entity.TagEntity;
import app.zad.zadinventory.model.exception.RegraNegocioException;
import app.zad.zadinventory.model.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository repository;

    public TagEntity salvar(TagEntity tag) {
        validarTag(tag);
        return repository.save(tag);
    }

    public List<TagEntity> buscarTodos() {
        return repository.findAll();
    }

    public TagEntity buscarPorId(Long id) {
        return repository.findComProdutosById(id)
                .orElseThrow(() -> new RegraNegocioException("Tag não encontrada!"));
    }

    public List<TagEntity> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    public List<TagEntity> buscarPorTermo(String termo) {
        return repository.buscarPorTermo(termo);
    }

    public void remover(Long id) {
        repository.deleteById(id);
    }

    private void validarTag(TagEntity tag) {
        if (tag.getNome() == null || tag.getNome().isBlank()) {
            throw new RegraNegocioException("Nome da tag é obrigatório!");
        }

        if (repository.findByNomeIgnoreCase(tag.getNome()).isPresent()) {
            throw new RegraNegocioException("Já existe uma tag com este nome!");
        }
    }
}
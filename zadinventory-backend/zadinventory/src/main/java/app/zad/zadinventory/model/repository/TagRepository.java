package app.zad.zadinventory.model.repository;

import app.zad.zadinventory.model.entity.TagEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<TagEntity, Long> {

    @EntityGraph(attributePaths = {"produtos"})
    Optional<TagEntity> findComProdutosById(Long id);

    // Método padrão sobrescrito
    @Override
    @EntityGraph(attributePaths = {"produtos"})
    List<TagEntity> findAll();

    @EntityGraph(attributePaths = {"produtos"})
    List<TagEntity> findByNomeContainingIgnoreCase(String nome);

    @EntityGraph(attributePaths = {"produtos"})
    @Query("SELECT t FROM TagEntity t WHERE t.nome LIKE %:termo%")
    List<TagEntity> buscarPorTermo(@Param("termo") String termo);

    Optional<TagEntity> findByNomeIgnoreCase(String nome);
}
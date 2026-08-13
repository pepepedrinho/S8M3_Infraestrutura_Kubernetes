package app.zad.zadinventory.model.repository;

import app.zad.zadinventory.model.entity.CategoriaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> {

    @EntityGraph(attributePaths = {"produtos"})
    Optional<CategoriaEntity> findComProdutosById(Long id);

    // Método padrão sobrescrito com EntityGraph
    @Override
    @EntityGraph(attributePaths = {"produtos"})
    List<CategoriaEntity> findAll();

    @EntityGraph(attributePaths = {"produtos"})
    List<CategoriaEntity> findByNomeContainingIgnoreCase(String nome);

    @EntityGraph(attributePaths = {"produtos"})
    @Query("SELECT c FROM CategoriaEntity c WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<CategoriaEntity> buscarPorTermo(@Param("termo") String termo);

    Optional<CategoriaEntity> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);
}
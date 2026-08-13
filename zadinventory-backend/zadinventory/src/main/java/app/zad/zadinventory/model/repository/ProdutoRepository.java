package app.zad.zadinventory.model.repository;

import app.zad.zadinventory.model.entity.ProdutoEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<ProdutoEntity, Long> {

    @EntityGraph(attributePaths = {"categoria", "usuario", "tags"})
    Optional<ProdutoEntity> findComRelacionamentosById(Long id);

    // Método padrão sobrescrito
    @Override
    @EntityGraph(attributePaths = {"categoria"})
    List<ProdutoEntity> findAll();

    @EntityGraph(attributePaths = {"categoria"})
    List<ProdutoEntity> findByNomeContainingIgnoreCase(String nome);

    @EntityGraph(attributePaths = {"categoria"})
    List<ProdutoEntity> findByCategoriaId(Long categoriaId);

    @EntityGraph(attributePaths = {"categoria"})
    List<ProdutoEntity> findByQuantidadeGreaterThan(Integer quantidade);

    @EntityGraph(attributePaths = {"categoria"})
    @Query("SELECT p FROM ProdutoEntity p WHERE p.quantidade < :quantidadeMinima")
    List<ProdutoEntity> buscarProdutosComBaixoEstoque(@Param("quantidadeMinima") Integer quantidadeMinima);

    Optional<ProdutoEntity> findByNomeIgnoreCase(String nome);

}
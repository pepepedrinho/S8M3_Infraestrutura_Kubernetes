package app.zad.zadinventory.model.repository;

import app.zad.zadinventory.model.entity.OperacaoEntity;
import app.zad.zadinventory.model.enums.Situacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OperacaoRepository extends JpaRepository<OperacaoEntity, Long> {

    @EntityGraph(attributePaths = {"produto", "usuario"})
    Optional<OperacaoEntity> findComRelacionamentosById(Long id);

    // Método padrão sobrescrito
    @Override
    @EntityGraph(attributePaths = {"produto", "usuario"})
    List<OperacaoEntity> findAll();

    @EntityGraph(attributePaths = {"produto", "usuario"})
    List<OperacaoEntity> findBySituacao(Situacao situacao);

    @EntityGraph(attributePaths = {"produto", "usuario"})
    @Query("SELECT o FROM OperacaoEntity o WHERE o.produto.id = :produtoId")
    List<OperacaoEntity> findByProdutoId(@Param("produtoId") Long produtoId);

    @EntityGraph(attributePaths = {"produto", "usuario"})
    @Query("SELECT o FROM OperacaoEntity o WHERE o.usuario.id = :usuarioId")
    List<OperacaoEntity> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT SUM (o.quantidade) FROM OperacaoEntity o WHERE  o.diaOperacao BETWEEN :inicio AND :fim AND o.situacao = 'REALIZADA'")
    Long somaQuantidadePorPeriodo(@Param("inicio") LocalDate inicio,@Param("fim") LocalDate fim);

    @Query("SELECT SUM (o.valorTotal) FROM OperacaoEntity o WHERE o.diaOperacao BETWEEN :inicio AND :fim AND o.situacao = 'REALIZADA'")
    BigDecimal somaValorTotalPorPeriodo(@Param("inicio") LocalDate inicio,@Param("fim") LocalDate fim);

    @Query("""
           SELECT o.produto.id, o.produto.nome, SUM(o.quantidade), SUM(o.valorTotal)
           FROM OperacaoEntity o
           WHERE o.diaOperacao BETWEEN :inicio AND :fim
             AND o.situacao = 'REALIZADA'
           GROUP BY o.produto.id, o.produto.nome
           """)
    List<Object[]> relatorioVendasPorProduto(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
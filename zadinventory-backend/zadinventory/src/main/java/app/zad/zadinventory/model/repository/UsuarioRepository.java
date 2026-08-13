package app.zad.zadinventory.model.repository;

import app.zad.zadinventory.model.entity.UsuarioEntity;
import app.zad.zadinventory.model.enums.TipoUsuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    @EntityGraph(attributePaths = {"produtos"})
    Optional<UsuarioEntity> findComProdutosById(Long id);

    // Método padrão sobrescrito
    @Override
    @EntityGraph(attributePaths = {"produtos"})
    List<UsuarioEntity> findAll();

    @EntityGraph(attributePaths = {"produtos"})
    List<UsuarioEntity> findByTipoUsuario(TipoUsuario tipo);

    @EntityGraph(attributePaths = {"produtos"})
    @Query("SELECT u FROM UsuarioEntity u WHERE u.tipoUsuario = :tipo ORDER BY u.email ASC")
    List<UsuarioEntity> buscarPorTipoOrdenado(@Param("tipo") TipoUsuario tipo);

    Optional<UsuarioEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<UsuarioEntity> findByEmailAndSenha(String email, String senha);



}
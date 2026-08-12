package app.zad.zadinventory.unit.service;

import app.zad.zadinventory.model.entity.UsuarioEntity;
import app.zad.zadinventory.model.enums.TipoUsuario;
import app.zad.zadinventory.model.exception.RegraNegocioException;
import app.zad.zadinventory.model.repository.UsuarioRepository;
import app.zad.zadinventory.model.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TESTE DE UNIDADE - UsuarioService")
class UsuarioServiceUnitTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UsuarioService service;

    @Test
    @DisplayName("Deve salvar usuário com dados válidos")
    void deveSalvarUsuarioComDadosValidos() {
        // Arrange
        UsuarioEntity usuarioInput = UsuarioEntity.builder()
                .email("USUARIO@TESTE.COM")
                .senha("senha123")
                .tipoUsuario(TipoUsuario.FUNCIONARIO)
                .build();

        UsuarioEntity usuarioSalvo = UsuarioEntity.builder()
                .id(1L)
                .email("usuario@teste.com")
                .senha("senha123")
                .tipoUsuario(TipoUsuario.FUNCIONARIO)
                .build();

        when(repository.existsByEmail("usuario@teste.com")).thenReturn(false);
        when(repository.save(any(UsuarioEntity.class))).thenReturn(usuarioSalvo);

        // Act
        UsuarioEntity resultado = service.salvar(usuarioInput);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("usuario@teste.com", resultado.getEmail());
        verify(repository).existsByEmail("usuario@teste.com");
        verify(repository).save(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando email for vazio")
    void deveLancarExcecaoQuandoEmailForVazio() {
        // Arrange
        UsuarioEntity usuario = UsuarioEntity.builder()
                .email("")
                .senha("senha123")
                .tipoUsuario(TipoUsuario.FUNCIONARIO)
                .build();

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(usuario));

        assertEquals("Email é obrigatório!", ex.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando email for nulo")
    void deveLancarExcecaoQuandoEmailForNulo() {
        // Arrange
        UsuarioEntity usuario = UsuarioEntity.builder()
                .email(null)
                .senha("senha123")
                .tipoUsuario(TipoUsuario.FUNCIONARIO)
                .build();

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(usuario));

        assertEquals("Email é obrigatório!", ex.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando senha for vazia")
    void deveLancarExcecaoQuandoSenhaForVazia() {
        // Arrange
        UsuarioEntity usuario = UsuarioEntity.builder()
                .email("teste@email.com")
                .senha("")
                .tipoUsuario(TipoUsuario.FUNCIONARIO)
                .build();

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(usuario));

        assertEquals("Senha é obrigatória!", ex.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando tipo de usuário for nulo")
    void deveLancarExcecaoQuandoTipoUsuarioForNulo() {
        // Arrange
        UsuarioEntity usuario = UsuarioEntity.builder()
                .email("teste@email.com")
                .senha("senha123")
                .tipoUsuario(null)
                .build();

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(usuario));

        assertEquals("Tipo de usuário é obrigatório!", ex.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando email já existir")
    void deveLancarExcecaoQuandoEmailForDuplicado() {
        // Arrange
        when(repository.existsByEmail("existente@email.com")).thenReturn(true);

        UsuarioEntity usuario = UsuarioEntity.builder()
                .email("EXISTENTE@EMAIL.COM")
                .senha("senha123")
                .tipoUsuario(TipoUsuario.FUNCIONARIO)
                .build();

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(usuario));

        assertEquals("Email já está em uso!", ex.getMessage());
        verify(repository).existsByEmail("existente@email.com");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar usuário por ID existente")
    void deveBuscarUsuarioPorIdExistente() {
        // Arrange
        UsuarioEntity usuario = UsuarioEntity.builder()
                .id(1L)
                .email("teste@email.com")
                .senha("senha123")
                .tipoUsuario(TipoUsuario.FUNCIONARIO)
                .build();

        when(repository.findComProdutosById(1L)).thenReturn(Optional.of(usuario));

        // Act
        UsuarioEntity resultado = service.buscarPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("teste@email.com", resultado.getEmail());
        verify(repository).findComProdutosById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando ID não existir")
    void deveLancarExcecaoQuandoIdNaoExistir() {
        // Arrange
        when(repository.findComProdutosById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.buscarPorId(99L));

        assertEquals("Usuário não encontrado!", ex.getMessage());
        verify(repository).findComProdutosById(99L);
    }

    @Test
    @DisplayName("Deve retornar lista de usuários pelo tipo")
    void deveBuscarUsuariosPorTipo() {
        // Arrange
        List<UsuarioEntity> usuarios = List.of(
                UsuarioEntity.builder().id(1L).email("a@email.com").tipoUsuario(TipoUsuario.FUNCIONARIO).build(),
                UsuarioEntity.builder().id(2L).email("b@email.com").tipoUsuario(TipoUsuario.FUNCIONARIO).build()
        );

        when(repository.findByTipoUsuario(TipoUsuario.FUNCIONARIO)).thenReturn(usuarios);

        // Act
        List<UsuarioEntity> resultado = service.buscarPorTipo(TipoUsuario.FUNCIONARIO);

        // Assert
        assertEquals(2, resultado.size());
        verify(repository).findByTipoUsuario(TipoUsuario.FUNCIONARIO);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não encontrar usuários por tipo")
    void deveRetornarListaVaziaQuandoNaoEncontrarUsuariosPorTipo() {
        // Arrange
        when(repository.findByTipoUsuario(TipoUsuario.GERENTE)).thenReturn(List.of());

        // Act
        List<UsuarioEntity> resultado = service.buscarPorTipo(TipoUsuario.GERENTE);

        // Assert
        assertTrue(resultado.isEmpty());
        verify(repository).findByTipoUsuario(TipoUsuario.GERENTE);
    }

    @Test
    @DisplayName("Deve retornar usuário ao buscar por email")
    void deveBuscarUsuarioPorEmail() {
        // Arrange
        UsuarioEntity usuario = UsuarioEntity.builder()
                .id(1L)
                .email("teste@email.com")
                .senha("senha123")
                .tipoUsuario(TipoUsuario.FUNCIONARIO)
                .build();

        when(repository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuario));

        // Act
        UsuarioEntity resultado = service.buscarPorEmail("TESTE@EMAIL.COM");

        // Assert
        assertNotNull(resultado);
        assertEquals("teste@email.com", resultado.getEmail());
        verify(repository).findByEmail("teste@email.com");
    }

    @Test
    @DisplayName("Deve lançar exceção quando email não existir")
    void deveLancarExcecaoQuandoEmailNaoExistir() {
        // Arrange
        when(repository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.buscarPorEmail("INEXISTENTE@EMAIL.COM"));

        assertEquals("Usuário não encontrado!", ex.getMessage());
        verify(repository).findByEmail("inexistente@email.com");
    }

    @Test
    @DisplayName("Deve remover usuário pelo ID")
    void deveRemoverUsuarioComIdExistente() {
        // Act
        service.remover(1L);

        // Assert
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve atualizar usuário com dados válidos")
    void deveAtualizarUsuarioComDadosValidos() {
        // Arrange
        UsuarioEntity existente = UsuarioEntity.builder()
                .id(1L)
                .email("antigo@email.com")
                .senha("senhaAntiga")
                .tipoUsuario(TipoUsuario.FUNCIONARIO)
                .build();

        UsuarioEntity atualizado = UsuarioEntity.builder()
                .email("NOVO@EMAIL.COM")
                .senha("novaSenha")
                .tipoUsuario(TipoUsuario.GERENTE)
                .build();

        when(repository.findComProdutosById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(UsuarioEntity.class))).thenReturn(existente);

        // Act
        UsuarioEntity resultado = service.atualizar(1L, atualizado);

        // Assert
        assertEquals("novo@email.com", resultado.getEmail());
        assertEquals(TipoUsuario.GERENTE, resultado.getTipoUsuario());
        assertEquals("novaSenha", resultado.getSenha());
        verify(repository).findComProdutosById(1L);
        verify(repository).save(existente);
    }

    @Test
    @DisplayName("Deve manter senha antiga quando nova senha for nula")
    void deveManterSenhaAntigaQuandoNovaSenhaForNula() {
        // Arrange
        UsuarioEntity existente = UsuarioEntity.builder()
                .id(1L)
                .email("antigo@email.com")
                .senha("senhaAntiga")
                .tipoUsuario(TipoUsuario.FUNCIONARIO)
                .build();

        UsuarioEntity atualizado = UsuarioEntity.builder()
                .email("novo@email.com")
                .senha(null)
                .tipoUsuario(TipoUsuario.GERENTE)
                .build();

        when(repository.findComProdutosById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(UsuarioEntity.class))).thenReturn(existente);

        // Act
        UsuarioEntity resultado = service.atualizar(1L, atualizado);

        // Assert
        assertEquals("senhaAntiga", resultado.getSenha());
        verify(repository).findComProdutosById(1L);
        verify(repository).save(existente);
    }

    @Test
    @DisplayName("Deve manter senha antiga quando nova senha for vazia")
    void deveManterSenhaAntigaQuandoNovaSenhaForVazia() {
        // Arrange
        UsuarioEntity existente = UsuarioEntity.builder()
                .id(1L)
                .email("antigo@email.com")
                .senha("senhaAntiga")
                .tipoUsuario(TipoUsuario.FUNCIONARIO)
                .build();

        UsuarioEntity atualizado = UsuarioEntity.builder()
                .email("novo@email.com")
                .senha("")
                .tipoUsuario(TipoUsuario.GERENTE)
                .build();

        when(repository.findComProdutosById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(UsuarioEntity.class))).thenReturn(existente);

        // Act
        UsuarioEntity resultado = service.atualizar(1L, atualizado);

        // Assert
        assertEquals("senhaAntiga", resultado.getSenha());
        verify(repository).findComProdutosById(1L);
        verify(repository).save(existente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar usuário inexistente")
    void deveLancarExcecaoAoAtualizarUsuarioInexistente() {
        // Arrange
        UsuarioEntity atualizado = UsuarioEntity.builder()
                .email("novo@email.com")
                .senha("novaSenha")
                .tipoUsuario(TipoUsuario.GERENTE)
                .build();

        when(repository.findComProdutosById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.atualizar(99L, atualizado));

        assertEquals("Usuário não encontrado!", ex.getMessage());
        verify(repository).findComProdutosById(99L);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar usuários por tipo ordenado")
    void deveBuscarUsuariosPorTipoOrdenado() {
        // Arrange
        List<UsuarioEntity> usuarios = List.of(
                UsuarioEntity.builder().id(1L).email("a@email.com").tipoUsuario(TipoUsuario.FUNCIONARIO).build()
        );

        when(repository.buscarPorTipoOrdenado(TipoUsuario.FUNCIONARIO)).thenReturn(usuarios);

        // Act
        List<UsuarioEntity> resultado = service.buscarPorTipoOrdenado(TipoUsuario.FUNCIONARIO);

        // Assert
        assertEquals(1, resultado.size());
        verify(repository).buscarPorTipoOrdenado(TipoUsuario.FUNCIONARIO);
    }

    @Test
    @DisplayName("Deve buscar todos os usuários")
    void deveBuscarTodosOsUsuarios() {
        // Arrange
        List<UsuarioEntity> usuarios = List.of(
                UsuarioEntity.builder().id(1L).email("a@email.com").tipoUsuario(TipoUsuario.FUNCIONARIO).build(),
                UsuarioEntity.builder().id(2L).email("b@email.com").tipoUsuario(TipoUsuario.GERENTE).build()
        );

        when(repository.findAll()).thenReturn(usuarios);

        // Act
        List<UsuarioEntity> resultado = service.buscarTodos();

        // Assert
        assertEquals(2, resultado.size());
        verify(repository).findAll();
    }
}
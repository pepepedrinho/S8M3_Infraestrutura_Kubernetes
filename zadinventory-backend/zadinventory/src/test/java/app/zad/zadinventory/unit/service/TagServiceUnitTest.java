package app.zad.zadinventory.unit.service;

import app.zad.zadinventory.model.entity.TagEntity;
import app.zad.zadinventory.model.exception.RegraNegocioException;
import app.zad.zadinventory.model.repository.TagRepository;
import app.zad.zadinventory.model.service.TagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TESTE DE UNIDADE - TagService")
class TagServiceUnitTest {

    @Mock
    private TagRepository repository;

    @InjectMocks
    private TagService service;

    @Test
    @DisplayName("Deve salvar tag com dados válidos")
    void deveSalvarTagComDadosValidos() {
        // Arrange
        TagEntity tagInput = TagEntity.builder().nome("Nova Tag").build();
        TagEntity tagSalva = TagEntity.builder().id(1L).nome("Nova Tag").build();

        when(repository.findByNomeIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(TagEntity.class))).thenReturn(tagSalva);

        // Act
        TagEntity resultado = service.salvar(tagInput);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Nova Tag", resultado.getNome());
        verify(repository).findByNomeIgnoreCase("Nova Tag");
        verify(repository).save(tagInput);
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome for vazio")
    void deveLancarExcecaoQuandoNomeForVazio() {
        // Arrange
        TagEntity tag = TagEntity.builder().nome("").build();

        // Act & Assert
        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> service.salvar(tag)
        );

        assertEquals("Nome da tag é obrigatório!", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome for nulo")
    void deveLancarExcecaoQuandoNomeForNulo() {
        // Arrange
        TagEntity tag = TagEntity.builder().nome(null).build();

        // Act & Assert
        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> service.salvar(tag)
        );

        assertEquals("Nome da tag é obrigatório!", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome já existir")
    void deveLancarExcecaoQuandoNomeForDuplicado() {
        // Arrange
        TagEntity existente = TagEntity.builder().id(1L).nome("Duplicada").build();
        when(repository.findByNomeIgnoreCase("Duplicada")).thenReturn(Optional.of(existente));

        TagEntity nova = TagEntity.builder().nome("Duplicada").build();

        // Act & Assert
        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> service.salvar(nova)
        );

        assertEquals("Já existe uma tag com este nome!", exception.getMessage());
        verify(repository).findByNomeIgnoreCase("Duplicada");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar tag por ID existente")
    void deveBuscarTagPorIdExistente() {
        // Arrange
        TagEntity tag = TagEntity.builder().id(1L).nome("Tag Teste").build();
        when(repository.findComProdutosById(1L)).thenReturn(Optional.of(tag));

        // Act
        TagEntity resultado = service.buscarPorId(1L);

        // Assert
        assertEquals("Tag Teste", resultado.getNome());
        verify(repository).findComProdutosById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando ID não existir")
    void deveLancarExcecaoQuandoIdNaoExistir() {
        // Arrange
        when(repository.findComProdutosById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> service.buscarPorId(99L)
        );

        assertEquals("Tag não encontrada!", exception.getMessage());
        verify(repository).findComProdutosById(99L);
    }

    @Test
    @DisplayName("Deve retornar lista de tags pelo nome")
    void deveBuscarTagsPorNome() {
        // Arrange
        List<TagEntity> tags = List.of(
                TagEntity.builder().id(1L).nome("Tag 1").build(),
                TagEntity.builder().id(2L).nome("Tag 2").build()
        );
        when(repository.findByNomeContainingIgnoreCase("Tag")).thenReturn(tags);

        // Act
        List<TagEntity> resultado = service.buscarPorNome("Tag");

        // Assert
        assertEquals(2, resultado.size());
        verify(repository).findByNomeContainingIgnoreCase("Tag");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não encontrar tags por nome")
    void deveRetornarListaVaziaQuandoNaoEncontrarTagsPorNome() {
        // Arrange
        when(repository.findByNomeContainingIgnoreCase("Inexistente")).thenReturn(List.of());

        // Act
        List<TagEntity> resultado = service.buscarPorNome("Inexistente");

        // Assert
        assertTrue(resultado.isEmpty());
        verify(repository).findByNomeContainingIgnoreCase("Inexistente");
    }

    @Test
    @DisplayName("Deve buscar tags por termo")
    void deveBuscarTagsPorTermo() {
        // Arrange
        List<TagEntity> tags = List.of(
                TagEntity.builder().id(1L).nome("Eletrônicos").build()
        );
        when(repository.buscarPorTermo("eletro")).thenReturn(tags);

        // Act
        List<TagEntity> resultado = service.buscarPorTermo("eletro");

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Eletrônicos", resultado.get(0).getNome());
        verify(repository).buscarPorTermo("eletro");
    }

    @Test
    @DisplayName("Deve remover tag pelo ID")
    void deveRemoverTagComIdExistente() {
        // Act
        service.remover(1L);

        // Assert
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve buscar todas as tags")
    void deveBuscarTodasAsTags() {
        // Arrange
        List<TagEntity> tags = List.of(
                TagEntity.builder().id(1L).nome("Tag 1").build(),
                TagEntity.builder().id(2L).nome("Tag 2").build()
        );
        when(repository.findAll()).thenReturn(tags);

        // Act
        List<TagEntity> resultado = service.buscarTodos();

        // Assert
        assertEquals(2, resultado.size());
        verify(repository).findAll();
    }
}
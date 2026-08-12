package app.zad.zadinventory.Integration.controller;

import app.zad.zadinventory.controller.TagController;
import app.zad.zadinventory.model.entity.TagEntity;
import app.zad.zadinventory.model.service.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagController.class) // ← Carrega APENAS o contexto web
@ExtendWith(SpringExtension.class)
@Import(TagControllerIntegrationTest.TestConfig.class)
@DisplayName("TESTE DE INTEGRAÇÃO - TagController")
class TagControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TagService tagService;

    private TagEntity tag;

    @BeforeEach
    void setUp() {
        tag = TagEntity.builder()
                .id(1L)
                .nome("Tag Teste")
                .build();
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Cenário com dados válidos deve criar tag com status 200")
    void deveCriarTagComDadosValidos() throws Exception {
        when(tagService.salvar(any(TagEntity.class))).thenReturn(tag);

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Tag Teste"));

        verify(tagService, times(1)).salvar(any(TagEntity.class));
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Cenário deve listar todas as tags")
    void deveListarTodasAsTags() throws Exception {
        List<TagEntity> tags = Arrays.asList(tag);
        when(tagService.buscarTodos()).thenReturn(tags);

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nome").value("Tag Teste"));

        verify(tagService, times(1)).buscarTodos();
    }

    @TestConfiguration
    static class TestConfig {
        @Mock
        private TagService tagService;

        @Bean
        public TagService tagService() {
            return mock(TagService.class);
        }
    }
}
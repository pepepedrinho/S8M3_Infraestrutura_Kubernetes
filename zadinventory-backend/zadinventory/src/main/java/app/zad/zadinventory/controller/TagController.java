package app.zad.zadinventory.controller;

import app.zad.zadinventory.model.entity.TagEntity;
import app.zad.zadinventory.model.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TagController {

    private final TagService service;

    @PostMapping
    public ResponseEntity<TagEntity> criar(@RequestBody TagEntity tag) {
        return ResponseEntity.ok(service.salvar(tag));
    }

    @GetMapping
    public ResponseEntity<List<TagEntity>> listar() {
        return ResponseEntity.ok(service.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagEntity> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/por-nome")
    public ResponseEntity<List<TagEntity>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(service.buscarPorNome(nome));
    }

    @GetMapping("/buscar-termo")
    public ResponseEntity<List<TagEntity>> buscarPorTermo(@RequestParam String termo) {
        return ResponseEntity.ok(service.buscarPorTermo(termo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
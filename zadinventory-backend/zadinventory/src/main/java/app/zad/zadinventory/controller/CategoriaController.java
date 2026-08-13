package app.zad.zadinventory.controller;

import app.zad.zadinventory.model.entity.CategoriaEntity;
import app.zad.zadinventory.model.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService service;

    @PostMapping
    public ResponseEntity<CategoriaEntity> salvar(
            @Valid @RequestBody CategoriaEntity categoria) {

        CategoriaEntity saved = service.salvar(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<CategoriaEntity>> buscarTodos() {
        return ResponseEntity.ok(service.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaEntity> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/com-produtos")
    public ResponseEntity<CategoriaEntity> buscarComProdutos(@PathVariable Long id) {
        return service.buscarComProdutosPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CategoriaEntity>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(service.buscarPorNome(nome));
    }

    @GetMapping("/buscar-termo")
    public ResponseEntity<List<CategoriaEntity>> buscarPorTermo(@RequestParam String termo) {
        return ResponseEntity.ok(service.buscarPorTermo(termo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaEntity> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaEntity categoria) {

        return ResponseEntity.ok(service.atualizar(id, categoria));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
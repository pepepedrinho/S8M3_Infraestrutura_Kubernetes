package app.zad.zadinventory.controller;

import app.zad.zadinventory.model.entity.ProdutoEntity;
import app.zad.zadinventory.model.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProdutoController {

    private final ProdutoService service;

    @PostMapping
    public ResponseEntity<ProdutoEntity> criar(@RequestBody ProdutoEntity produto) {
        return ResponseEntity.ok(service.salvar(produto));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoEntity>> listar() {
        return ResponseEntity.ok(service.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoEntity> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/por-nome")
    public ResponseEntity<List<ProdutoEntity>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(service.buscarPorNome(nome));
    }

    @GetMapping("/por-categoria/{categoriaId}")
    public ResponseEntity<List<ProdutoEntity>> buscarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(service.buscarPorCategoria(categoriaId));
    }

    @GetMapping("/baixo-estoque")
    public ResponseEntity<List<ProdutoEntity>> buscarComBaixoEstoque(
            @RequestParam Integer quantidadeMinima) {

        return ResponseEntity.ok(service.buscarComBaixoEstoque(quantidadeMinima));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoEntity> atualizar(
            @PathVariable Long id,
            @RequestBody ProdutoEntity produto) {

        return ResponseEntity.ok(service.atualizar(id, produto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
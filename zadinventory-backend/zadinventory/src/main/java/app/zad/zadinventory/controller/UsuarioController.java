package app.zad.zadinventory.controller;

import app.zad.zadinventory.controller.dto.UsuarioResponse;
import app.zad.zadinventory.model.entity.UsuarioEntity;
import app.zad.zadinventory.model.enums.TipoUsuario;
import app.zad.zadinventory.model.exception.RegraNegocioException;
import app.zad.zadinventory.model.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<UsuarioResponse> criar(@RequestBody UsuarioEntity usuario) {
        UsuarioEntity novo = service.salvar(usuario);
        return ResponseEntity.ok(
                new UsuarioResponse(novo.getId(), novo.getNome(), novo.getEmail(), novo.getTipoUsuario().name())
        );
    }

    @PostMapping("/criar-inicial")
    public ResponseEntity<UsuarioEntity> criarUsuarioInicial(@RequestBody UsuarioEntity usuario) {
        // Verifica se já existe algum usuário no sistema
        if (service.buscarTodos().isEmpty()) {
            return ResponseEntity.ok(service.salvar(usuario));
        } else {
            throw new RegraNegocioException("Usuário inicial já foi criado!");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<List<UsuarioEntity>> listar() {
        return ResponseEntity.ok(service.buscarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE') or hasRole('FUNCIONARIO')")
    public ResponseEntity<UsuarioEntity> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/por-email")
    @PreAuthorize("hasRole('GERENTE') or hasRole('FUNCIONARIO')")
    public ResponseEntity<UsuarioEntity> buscarPorEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.buscarPorEmail(email));
    }

    @GetMapping("/por-tipo")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<List<UsuarioEntity>> buscarPorTipo(@RequestParam TipoUsuario tipo) {
        return ResponseEntity.ok(service.buscarPorTipo(tipo));
    }

    @GetMapping("/por-tipo-ordenado")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<List<UsuarioEntity>> buscarPorTipoOrdenado(@RequestParam TipoUsuario tipo) {
        return ResponseEntity.ok(service.buscarPorTipoOrdenado(tipo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE') or (hasRole('FUNCIONARIO') and @usuarioService.buscarPorEmail(authentication.principal.username).id == #id)")
    public ResponseEntity<UsuarioEntity> atualizar(
            @PathVariable Long id,
            @RequestBody UsuarioEntity usuario) {

        return ResponseEntity.ok(service.atualizar(id, usuario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
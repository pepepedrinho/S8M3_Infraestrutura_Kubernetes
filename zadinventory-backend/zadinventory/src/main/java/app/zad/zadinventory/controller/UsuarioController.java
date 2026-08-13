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

    /**
     * Converte a entidade na resposta da API.
     *
     * <p>Devolver {@link UsuarioEntity} cru deixava a senha (hash BCrypt) a uma
     * anotação de distância de vazar no JSON. O DTO não tem o campo, então a
     * proteção não depende mais de {@code @JsonProperty(WRITE_ONLY)}.
     */
    private UsuarioResponse toResponse(UsuarioEntity usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipoUsuario().name()
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<UsuarioResponse> criar(@RequestBody UsuarioEntity usuario) {
        return ResponseEntity.ok(toResponse(service.salvar(usuario)));
    }

    @PostMapping("/criar-inicial")
    public ResponseEntity<UsuarioResponse> criarUsuarioInicial(@RequestBody UsuarioEntity usuario) {
        // Verifica se já existe algum usuário no sistema
        if (service.buscarTodos().isEmpty()) {
            return ResponseEntity.ok(toResponse(service.salvar(usuario)));
        } else {
            throw new RegraNegocioException("Usuário inicial já foi criado!");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(service.buscarTodos().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE') or hasRole('FUNCIONARIO')")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.buscarPorId(id)));
    }

    @GetMapping("/por-email")
    @PreAuthorize("hasRole('GERENTE') or hasRole('FUNCIONARIO')")
    public ResponseEntity<UsuarioResponse> buscarPorEmail(@RequestParam String email) {
        return ResponseEntity.ok(toResponse(service.buscarPorEmail(email)));
    }

    @GetMapping("/por-tipo")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<List<UsuarioResponse>> buscarPorTipo(@RequestParam TipoUsuario tipo) {
        return ResponseEntity.ok(service.buscarPorTipo(tipo).stream().map(this::toResponse).toList());
    }

    @GetMapping("/por-tipo-ordenado")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<List<UsuarioResponse>> buscarPorTipoOrdenado(@RequestParam TipoUsuario tipo) {
        return ResponseEntity.ok(service.buscarPorTipoOrdenado(tipo).stream().map(this::toResponse).toList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE') or (hasRole('FUNCIONARIO') and @usuarioService.buscarPorEmail(authentication.principal.username).id == #id)")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable Long id,
            @RequestBody UsuarioEntity usuario) {

        return ResponseEntity.ok(toResponse(service.atualizar(id, usuario)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
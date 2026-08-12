package app.zad.zadinventory.controller;

import app.zad.zadinventory.controller.dto.OperacoesDto;
import app.zad.zadinventory.controller.dto.OperacoesDTORequest;
import app.zad.zadinventory.controller.dto.RelatorioVendasProdutoDto;
import app.zad.zadinventory.controller.dto.TotalVendasDto;
import app.zad.zadinventory.model.entity.OperacaoEntity;
import app.zad.zadinventory.model.enums.Situacao;
import app.zad.zadinventory.model.repository.OperacaoRepository;
import app.zad.zadinventory.model.service.OperacaoService;
import app.zad.zadinventory.model.service.ProdutoService;
import app.zad.zadinventory.model.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/operacoes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OperacaoController {

    private final OperacaoService service;
    private final OperacaoRepository operacaoRepository;
    private final ProdutoService produtoService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<OperacoesDto> criar(@RequestBody OperacoesDTORequest dto) {
        var salva = service.salvar(dto);

        return ResponseEntity.ok(
                new OperacoesDto(
                        salva.getId(),
                        salva.getSituacao().name(),
                        salva.getDiaOperacao(),
                        salva.getQuantidade(),
                        salva.getValorTotal()
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<OperacaoEntity>> listar() {
        return ResponseEntity.ok(service.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperacaoEntity> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/por-situacao")
    public ResponseEntity<List<OperacaoEntity>> buscarPorSituacao(@RequestParam Situacao situacao) {
        return ResponseEntity.ok(service.buscarPorSituacao(situacao));
    }

    @GetMapping("/por-produto/{produtoId}")
    public ResponseEntity<List<OperacaoEntity>> buscarPorProduto(@PathVariable Long produtoId) {
        return ResponseEntity.ok(service.buscarPorProduto(produtoId));
    }

    @GetMapping("/por-usuario/{usuarioId}")
    public ResponseEntity<List<OperacaoEntity>> buscarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.buscarPorUsuario(usuarioId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperacaoEntity> atualizar(
            @PathVariable Long id,
            @RequestBody OperacoesDTORequest dto) {

        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @PatchMapping("/{id}/situacao")
    public ResponseEntity<OperacaoEntity> atualizarSituacao(
            @PathVariable Long id,
            @RequestParam Situacao novaSituacao) {

        System.out.println("Recebida requisição para atualizar situação:");
        System.out.println("ID: " + id);
        System.out.println("Nova Situação: " + novaSituacao);

        return ResponseEntity.ok(service.atualizarSituacao(id, novaSituacao));
    }

    // ADICIONAR: Endpoint de exclusão
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        System.out.println("Recebida requisição para excluir operação ID: " + id);
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total-vendas")
    public ResponseEntity<TotalVendasDto> totalVendas(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(service.totalVendas(inicio, fim));
    }

    @GetMapping("/total-vendas-produto")
    public ResponseEntity<List<RelatorioVendasProdutoDto>> relatorioVendasPorProduto(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ){

        return ResponseEntity.ok(service.relatorioVendasPorProduto(inicio, fim));
    }
}
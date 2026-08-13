package app.zad.zadinventory.model.service;

import app.zad.zadinventory.controller.dto.OperacoesDTORequest;
import app.zad.zadinventory.controller.dto.RelatorioVendasProdutoDto;
import app.zad.zadinventory.controller.dto.TotalVendasDto;
import app.zad.zadinventory.model.entity.OperacaoEntity;
import app.zad.zadinventory.model.entity.ProdutoEntity;
import app.zad.zadinventory.model.entity.UsuarioEntity;
import app.zad.zadinventory.model.enums.Situacao;
import app.zad.zadinventory.model.exception.RegraNegocioException;
import app.zad.zadinventory.model.repository.OperacaoRepository;
import app.zad.zadinventory.model.repository.ProdutoRepository;
import app.zad.zadinventory.model.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperacaoService {

    private final OperacaoRepository repository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public OperacaoEntity salvar(OperacoesDTORequest dto) {
        ProdutoEntity produto = produtoRepository.findById(dto.produtoId())
                .orElseThrow(() -> new RegraNegocioException("Produto não encontrado com ID: " + dto.produtoId()));

        UsuarioEntity usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado com ID: " + dto.usuarioId()));

        // Atualiza estoque
        if ("REALIZADA".equalsIgnoreCase(dto.situacao())) {
            if (produto.getQuantidade() < dto.quantidade()) {
                throw new RegraNegocioException("Estoque insuficiente para o produto: " + produto.getNome());
            }

            produto.setQuantidade(produto.getQuantidade() - dto.quantidade());
            produtoRepository.save(produto);
        }

        // Calcula valor total
        BigDecimal valorTotal = produto.getPreco()
                .multiply(BigDecimal.valueOf(dto.quantidade()));

        // Cria a operação
        var operacao = OperacaoEntity.builder()
                .produto(produto)
                .usuario(usuario)
                .situacao(Situacao.valueOf(dto.situacao().toUpperCase()))
                .diaOperacao(dto.diaOperacao())
                .quantidade(dto.quantidade())
                .valorTotal(valorTotal)
                .build();

        return repository.save(operacao);
    }

    public List<OperacaoEntity> buscarTodos() {
        return repository.findAll();
    }

    public OperacaoEntity buscarPorId(Long id) {
        return repository.findComRelacionamentosById(id)
                .orElseThrow(() -> new RegraNegocioException("Operação não encontrada!"));
    }

    public List<OperacaoEntity> buscarPorSituacao(Situacao situacao) {
        return repository.findBySituacao(situacao);
    }

    public List<OperacaoEntity> buscarPorProduto(Long produtoId) {
        return repository.findByProdutoId(produtoId);
    }

    public List<OperacaoEntity> buscarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public OperacaoEntity atualizarSituacao(Long id, Situacao novaSituacao) {
        OperacaoEntity operacao = buscarPorId(id);
        operacao.setSituacao(novaSituacao);
        return repository.save(operacao);
    }

    @Transactional
    public OperacaoEntity atualizar(Long id, OperacoesDTORequest dto) {
        OperacaoEntity operacaoExistente = buscarPorId(id);
        ProdutoEntity produtoAtual = operacaoExistente.getProduto();

        // Se for atualizar a situação
        if (dto.situacao() != null) {
            Situacao novaSituacao = Situacao.valueOf(dto.situacao().toUpperCase());

            if (novaSituacao == Situacao.REALIZADA && operacaoExistente.getSituacao() != Situacao.REALIZADA) {
                if (produtoAtual.getQuantidade() < dto.quantidade()) {
                    throw new IllegalArgumentException("Estoque insuficiente para o produto: " + produtoAtual.getNome());
                }
                produtoAtual.setQuantidade(produtoAtual.getQuantidade() - dto.quantidade());
                produtoRepository.save(produtoAtual);
            }

            operacaoExistente.setSituacao(novaSituacao);
        }

        // Atualiza produto
        if (dto.produtoId() != null) {
            ProdutoEntity novoProduto = produtoRepository.findById(dto.produtoId())
                    .orElseThrow(() -> new RegraNegocioException("Produto não encontrado com ID: " + dto.produtoId()));
            operacaoExistente.setProduto(novoProduto);
        }

        // Atualiza usuário
        if (dto.usuarioId() != null) {
            UsuarioEntity usuario = usuarioRepository.findById(dto.usuarioId())
                    .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado com ID: " + dto.usuarioId()));
            operacaoExistente.setUsuario(usuario);
        }

        // Atualiza data da operação
        if (dto.diaOperacao() != null) {
            operacaoExistente.setDiaOperacao(dto.diaOperacao());
        }

        // Atualiza quantidade
        if (dto.quantidade() != null) {
            operacaoExistente.setQuantidade(dto.quantidade());
        }

        // Recalcula valor total
        if (operacaoExistente.getProduto() != null && operacaoExistente.getQuantidade() != null) {
            BigDecimal valorTotal = operacaoExistente.getProduto().getPreco()
                    .multiply(BigDecimal.valueOf(operacaoExistente.getQuantidade()));
            operacaoExistente.setValorTotal(valorTotal);
        }

        return repository.save(operacaoExistente);
    }


    // ADICIONAR: Método de exclusão
    @Transactional
    public void excluir(Long id) {
        OperacaoEntity operacao = buscarPorId(id);
        repository.delete(operacao);
    }

    private void validarOperacao(OperacaoEntity operacao) {
        if (operacao.getProduto() == null || operacao.getProduto().getId() == null) {
            throw new RegraNegocioException("Produto é obrigatório!");
        }
        if (operacao.getUsuario() == null || operacao.getUsuario().getId() == null) {
            throw new RegraNegocioException("Usuário é obrigatório!");
        }
        if (operacao.getSituacao() == null) {
            throw new RegraNegocioException("Situação é obrigatória!");
        }
    }

    public TotalVendasDto totalVendas(LocalDate inicio, LocalDate fim) {
        Long quantidadeTotal = repository.somaQuantidadePorPeriodo(inicio, fim);
        BigDecimal valorTotal = repository.somaValorTotalPorPeriodo(inicio, fim);

        return new TotalVendasDto(quantidadeTotal != null ? quantidadeTotal :0L,
                valorTotal != null ? valorTotal : BigDecimal.ZERO);
    }

    public List<RelatorioVendasProdutoDto> relatorioVendasPorProduto(LocalDate inicio, LocalDate fim) {
        List<Object[]> resultados = repository.relatorioVendasPorProduto(inicio, fim);

        return resultados.stream()
                .map(obj -> new RelatorioVendasProdutoDto(
                        (Long) obj[0],
                        (String) obj[1],
                        (Long) obj[2],
                        (obj[3] != null) ? (java.math.BigDecimal) obj[3] : java.math.BigDecimal.ZERO
                ))
                .collect(Collectors.toList());
    }
}
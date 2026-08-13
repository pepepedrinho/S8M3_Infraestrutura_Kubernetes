package app.zad.zadinventory.unit.service;

import app.zad.zadinventory.controller.dto.OperacoesDTORequest;
import app.zad.zadinventory.controller.dto.RelatorioVendasProdutoDto;
import app.zad.zadinventory.controller.dto.TotalVendasDto;
import app.zad.zadinventory.model.entity.OperacaoEntity;
import app.zad.zadinventory.model.entity.ProdutoEntity;
import app.zad.zadinventory.model.entity.UsuarioEntity;
import app.zad.zadinventory.model.enums.Situacao;
import app.zad.zadinventory.model.enums.TipoUsuario;
import app.zad.zadinventory.model.exception.RegraNegocioException;
import app.zad.zadinventory.model.repository.OperacaoRepository;
import app.zad.zadinventory.model.repository.ProdutoRepository;
import app.zad.zadinventory.model.repository.UsuarioRepository;
import app.zad.zadinventory.model.service.OperacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TESTE DE UNIDADE - OperacaoService")
class OperacaoServiceUnitTest {

    @Mock
    private OperacaoRepository operacaoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private OperacaoService service;

    @Test
    @DisplayName("Deve salvar operação com situação REALIZADA e atualizar estoque")
    void deveSalvarOperacaoRealizadaComSucesso() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setNome("Notebook");
        produto.setQuantidade(10);
        produto.setPreco(BigDecimal.valueOf(2500.0));

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setEmail("user@test.com");
        usuario.setTipoUsuario(TipoUsuario.FUNCIONARIO);

        OperacoesDTORequest dto = new OperacoesDTORequest(
                1L, 1L, "REALIZADA", LocalDate.now(), 2
        );

        OperacaoEntity operacaoSalva = new OperacaoEntity();
        operacaoSalva.setId(1L);
        operacaoSalva.setProduto(produto);
        operacaoSalva.setUsuario(usuario);
        operacaoSalva.setSituacao(Situacao.REALIZADA);
        operacaoSalva.setDiaOperacao(LocalDate.now());
        operacaoSalva.setQuantidade(2);
        operacaoSalva.setValorTotal(BigDecimal.valueOf(5000.0));

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(produtoRepository.save(any(ProdutoEntity.class))).thenReturn(produto);
        when(operacaoRepository.save(any(OperacaoEntity.class))).thenReturn(operacaoSalva);

        OperacaoEntity resultado = service.salvar(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(Situacao.REALIZADA, resultado.getSituacao());
        assertEquals(BigDecimal.valueOf(5000.0), resultado.getValorTotal());
        verify(produtoRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
        verify(produtoRepository).save(produto);
        verify(operacaoRepository).save(any(OperacaoEntity.class));
    }

    @Test
    @DisplayName("Deve salvar operação com situação CANCELADA sem atualizar estoque")
    void deveSalvarOperacaoCanceladaSemAtualizarEstoque() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setNome("Notebook");
        produto.setQuantidade(10);
        produto.setPreco(BigDecimal.valueOf(2500.0));

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setEmail("user@test.com");

        OperacoesDTORequest dto = new OperacoesDTORequest(
                1L, 1L, "CANCELADA", LocalDate.now(), 2
        );

        OperacaoEntity operacaoSalva = new OperacaoEntity();
        operacaoSalva.setId(1L);
        operacaoSalva.setSituacao(Situacao.CANCELADA);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(operacaoRepository.save(any(OperacaoEntity.class))).thenReturn(operacaoSalva);

        OperacaoEntity resultado = service.salvar(dto);

        assertEquals(Situacao.CANCELADA, resultado.getSituacao());
        verify(produtoRepository, never()).save(any(ProdutoEntity.class));
        verify(operacaoRepository).save(any(OperacaoEntity.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não for encontrado ao salvar")
    void deveLancarExcecaoQuandoProdutoNaoEncontrado() {
        OperacoesDTORequest dto = new OperacoesDTORequest(
                99L, 1L, "REALIZADA", LocalDate.now(), 2
        );

        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(dto));

        assertEquals("Produto não encontrado com ID: 99", ex.getMessage());
        verify(produtoRepository).findById(99L);
        verify(usuarioRepository, never()).findById(any());
        verify(operacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado ao salvar")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setQuantidade(10);

        OperacoesDTORequest dto = new OperacoesDTORequest(
                1L, 99L, "REALIZADA", LocalDate.now(), 2
        );

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(dto));

        assertEquals("Usuário não encontrado com ID: 99", ex.getMessage());
        verify(produtoRepository).findById(1L);
        verify(usuarioRepository).findById(99L);
        verify(operacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque for insuficiente")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setNome("Notebook");
        produto.setQuantidade(1);
        produto.setPreco(BigDecimal.valueOf(2500.0));

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);

        OperacoesDTORequest dto = new OperacoesDTORequest(
                1L, 1L, "REALIZADA", LocalDate.now(), 5
        );

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.salvar(dto));

        assertEquals("Estoque insuficiente para o produto: Notebook", ex.getMessage());
        verify(produtoRepository, never()).save(any(ProdutoEntity.class));
        verify(operacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar operação por ID existente")
    void deveBuscarOperacaoPorIdExistente() {
        OperacaoEntity operacao = new OperacaoEntity();
        operacao.setId(1L);
        operacao.setSituacao(Situacao.REALIZADA);

        when(operacaoRepository.findComRelacionamentosById(1L)).thenReturn(Optional.of(operacao));

        OperacaoEntity resultado = service.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(operacaoRepository).findComRelacionamentosById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando operação não for encontrada por ID")
    void deveLancarExcecaoQuandoOperacaoNaoEncontradaPorId() {
        when(operacaoRepository.findComRelacionamentosById(99L)).thenReturn(Optional.empty());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.buscarPorId(99L));

        assertEquals("Operação não encontrada!", ex.getMessage());
        verify(operacaoRepository).findComRelacionamentosById(99L);
    }

    @Test
    @DisplayName("Deve buscar todas as operações")
    void deveBuscarTodasAsOperacoes() {
        OperacaoEntity operacao1 = new OperacaoEntity();
        operacao1.setId(1L);

        OperacaoEntity operacao2 = new OperacaoEntity();
        operacao2.setId(2L);

        List<OperacaoEntity> operacoes = List.of(operacao1, operacao2);

        when(operacaoRepository.findAll()).thenReturn(operacoes);

        List<OperacaoEntity> resultado = service.buscarTodos();

        assertEquals(2, resultado.size());
        verify(operacaoRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar operações por situação")
    void deveBuscarOperacoesPorSituacao() {
        OperacaoEntity operacao = new OperacaoEntity();
        operacao.setId(1L);
        operacao.setSituacao(Situacao.REALIZADA);

        List<OperacaoEntity> operacoes = List.of(operacao);

        when(operacaoRepository.findBySituacao(Situacao.REALIZADA)).thenReturn(operacoes);

        List<OperacaoEntity> resultado = service.buscarPorSituacao(Situacao.REALIZADA);

        assertEquals(1, resultado.size());
        assertEquals(Situacao.REALIZADA, resultado.get(0).getSituacao());
        verify(operacaoRepository).findBySituacao(Situacao.REALIZADA);
    }

    @Test
    @DisplayName("Deve buscar operações por produto")
    void deveBuscarOperacoesPorProduto() {
        OperacaoEntity operacao = new OperacaoEntity();
        operacao.setId(1L);

        List<OperacaoEntity> operacoes = List.of(operacao);

        when(operacaoRepository.findByProdutoId(1L)).thenReturn(operacoes);

        List<OperacaoEntity> resultado = service.buscarPorProduto(1L);

        assertEquals(1, resultado.size());
        verify(operacaoRepository).findByProdutoId(1L);
    }

    @Test
    @DisplayName("Deve buscar operações por usuário")
    void deveBuscarOperacoesPorUsuario() {
        OperacaoEntity operacao = new OperacaoEntity();
        operacao.setId(1L);

        List<OperacaoEntity> operacoes = List.of(operacao);

        when(operacaoRepository.findByUsuarioId(1L)).thenReturn(operacoes);

        List<OperacaoEntity> resultado = service.buscarPorUsuario(1L);

        assertEquals(1, resultado.size());
        verify(operacaoRepository).findByUsuarioId(1L);
    }

    @Test
    @DisplayName("Deve atualizar situação da operação")
    void deveAtualizarSituacaoDaOperacao() {
        OperacaoEntity operacao = new OperacaoEntity();
        operacao.setId(1L);
        operacao.setSituacao(Situacao.SEPARADA);

        when(operacaoRepository.findComRelacionamentosById(1L)).thenReturn(Optional.of(operacao));
        when(operacaoRepository.save(any(OperacaoEntity.class))).thenReturn(operacao);

        OperacaoEntity resultado = service.atualizarSituacao(1L, Situacao.REALIZADA);

        assertEquals(Situacao.REALIZADA, resultado.getSituacao());
        verify(operacaoRepository).findComRelacionamentosById(1L);
        verify(operacaoRepository).save(operacao);
    }

    @Test
    @DisplayName("Deve atualizar operação com dados válidos")
    void deveAtualizarOperacaoComDadosValidos() {
        ProdutoEntity produtoAtual = new ProdutoEntity();
        produtoAtual.setId(1L);
        produtoAtual.setNome("Produto Atual");
        produtoAtual.setQuantidade(10);
        produtoAtual.setPreco(BigDecimal.valueOf(100.0));

        ProdutoEntity novoProduto = new ProdutoEntity();
        novoProduto.setId(2L);
        novoProduto.setNome("Novo Produto");
        novoProduto.setQuantidade(5);
        novoProduto.setPreco(BigDecimal.valueOf(200.0));

        UsuarioEntity usuarioAtual = new UsuarioEntity();
        usuarioAtual.setId(1L);
        usuarioAtual.setEmail("atual@test.com");

        UsuarioEntity novoUsuario = new UsuarioEntity();
        novoUsuario.setId(2L);
        novoUsuario.setEmail("novo@test.com");

        OperacaoEntity operacaoExistente = new OperacaoEntity();
        operacaoExistente.setId(1L);
        operacaoExistente.setProduto(produtoAtual);
        operacaoExistente.setUsuario(usuarioAtual);
        operacaoExistente.setSituacao(Situacao.SEPARADA);
        operacaoExistente.setDiaOperacao(LocalDate.now().minusDays(1));
        operacaoExistente.setQuantidade(1);
        operacaoExistente.setValorTotal(BigDecimal.valueOf(100.0));

        OperacoesDTORequest dto = new OperacoesDTORequest(
                2L, 2L, "REALIZADA", LocalDate.now(), 2
        );

        when(operacaoRepository.findComRelacionamentosById(1L)).thenReturn(Optional.of(operacaoExistente));
        when(produtoRepository.findById(2L)).thenReturn(Optional.of(novoProduto));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(novoUsuario));
        when(operacaoRepository.save(any(OperacaoEntity.class))).thenReturn(operacaoExistente);

        OperacaoEntity resultado = service.atualizar(1L, dto);

        assertEquals(novoProduto, resultado.getProduto());
        assertEquals(novoUsuario, resultado.getUsuario());
        assertEquals(Situacao.REALIZADA, resultado.getSituacao());
        assertEquals(LocalDate.now(), resultado.getDiaOperacao());
        assertEquals(2, resultado.getQuantidade());
        assertEquals(BigDecimal.valueOf(400.0), resultado.getValorTotal());
        verify(operacaoRepository).findComRelacionamentosById(1L);
        verify(operacaoRepository).save(operacaoExistente);
    }

    @Test
    @DisplayName("Deve excluir operação existente")
    void deveExcluirOperacaoExistente() {
        OperacaoEntity operacao = new OperacaoEntity();
        operacao.setId(1L);

        when(operacaoRepository.findComRelacionamentosById(1L)).thenReturn(Optional.of(operacao));
        doNothing().when(operacaoRepository).delete(operacao);

        service.excluir(1L);

        verify(operacaoRepository).findComRelacionamentosById(1L);
        verify(operacaoRepository).delete(operacao);
    }

    @Test
    @DisplayName("Deve calcular total de vendas com sucesso")
    void deveCalcularTotalVendasComSucesso() {
        LocalDate inicio = LocalDate.now().minusDays(30);
        LocalDate fim = LocalDate.now();

        when(operacaoRepository.somaQuantidadePorPeriodo(inicio, fim)).thenReturn(50L);
        when(operacaoRepository.somaValorTotalPorPeriodo(inicio, fim)).thenReturn(BigDecimal.valueOf(5000.0));

        TotalVendasDto resultado = service.totalVendas(inicio, fim);

        assertEquals(50L, resultado.quantidadeTotal());
        assertEquals(BigDecimal.valueOf(5000.0), resultado.valorTotal());
        verify(operacaoRepository).somaQuantidadePorPeriodo(inicio, fim);
        verify(operacaoRepository).somaValorTotalPorPeriodo(inicio, fim);
    }

    @Test
    @DisplayName("Deve calcular total de vendas com valores nulos")
    void deveCalcularTotalVendasComValoresNulos() {
        LocalDate inicio = LocalDate.now().minusDays(30);
        LocalDate fim = LocalDate.now();

        when(operacaoRepository.somaQuantidadePorPeriodo(inicio, fim)).thenReturn(null);
        when(operacaoRepository.somaValorTotalPorPeriodo(inicio, fim)).thenReturn(null);

        TotalVendasDto resultado = service.totalVendas(inicio, fim);

        assertEquals(0L, resultado.quantidadeTotal());
        assertEquals(BigDecimal.ZERO, resultado.valorTotal());
        verify(operacaoRepository).somaQuantidadePorPeriodo(inicio, fim);
        verify(operacaoRepository).somaValorTotalPorPeriodo(inicio, fim);
    }

    @Test
    @DisplayName("Deve gerar relatório de vendas por produto")
    void deveGerarRelatorioVendasPorProduto() {
        LocalDate inicio = LocalDate.now().minusDays(30);
        LocalDate fim = LocalDate.now();

        Object[] resultado1 = new Object[]{1L, "Notebook", 10L, BigDecimal.valueOf(25000.0)};
        Object[] resultado2 = new Object[]{2L, "Mouse", 25L, BigDecimal.valueOf(1250.0)};
        List<Object[]> resultados = List.of(resultado1, resultado2);

        when(operacaoRepository.relatorioVendasPorProduto(inicio, fim)).thenReturn(resultados);

        List<RelatorioVendasProdutoDto> relatorio = service.relatorioVendasPorProduto(inicio, fim);

        assertEquals(2, relatorio.size());

        RelatorioVendasProdutoDto item1 = relatorio.get(0);
        assertEquals(1L, item1.produtoId());
        assertEquals("Notebook", item1.nomeProduto());
        assertEquals(10L, item1.quantidadeVendida());
        assertEquals(BigDecimal.valueOf(25000.0), item1.valorTotal());

        RelatorioVendasProdutoDto item2 = relatorio.get(1);
        assertEquals(2L, item2.produtoId());
        assertEquals("Mouse", item2.nomeProduto());
        assertEquals(25L, item2.quantidadeVendida());
        assertEquals(BigDecimal.valueOf(1250.0), item2.valorTotal());

        verify(operacaoRepository).relatorioVendasPorProduto(inicio, fim);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar operação com estoque insuficiente")
    void deveLancarExcecaoAoAtualizarComEstoqueInsuficiente() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setNome("Produto");
        produto.setQuantidade(1);

        OperacaoEntity operacaoExistente = new OperacaoEntity();
        operacaoExistente.setId(1L);
        operacaoExistente.setProduto(produto);
        operacaoExistente.setSituacao(Situacao.SEPARADA);
        operacaoExistente.setQuantidade(1);

        OperacoesDTORequest dto = new OperacoesDTORequest(
                1L, 1L, "REALIZADA", LocalDate.now(), 5
        );

        when(operacaoRepository.findComRelacionamentosById(1L)).thenReturn(Optional.of(operacaoExistente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.atualizar(1L, dto));

        assertEquals("Estoque insuficiente para o produto: Produto", ex.getMessage());
        verify(operacaoRepository).findComRelacionamentosById(1L);
        verify(produtoRepository, never()).save(any());
        verify(operacaoRepository, never()).save(any());
    }

    // ------------------ ADICIONADOS PARA COBERTURA DE BRANCHES ------------------

    @Test
    @DisplayName("Atualizar: dto com todos campos nulos não altera nada")
    void deveNaoAlterarQuandoDtoComCamposNulos() {
        ProdutoEntity produtoAtual = new ProdutoEntity();
        produtoAtual.setId(1L);
        produtoAtual.setNome("Produto Atual");
        produtoAtual.setQuantidade(10);
        produtoAtual.setPreco(BigDecimal.valueOf(100.0));

        UsuarioEntity usuarioAtual = new UsuarioEntity();
        usuarioAtual.setId(1L);
        usuarioAtual.setEmail("atual@test.com");

        OperacaoEntity operacaoExistente = new OperacaoEntity();
        operacaoExistente.setId(1L);
        operacaoExistente.setProduto(produtoAtual);
        operacaoExistente.setUsuario(usuarioAtual);
        operacaoExistente.setSituacao(Situacao.SEPARADA);
        operacaoExistente.setQuantidade(3);

        OperacoesDTORequest dtoNulo = new OperacoesDTORequest(null, null, null, null, null);

        when(operacaoRepository.findComRelacionamentosById(1L)).thenReturn(Optional.of(operacaoExistente));
        when(operacaoRepository.save(any(OperacaoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OperacaoEntity resultado = service.atualizar(1L, dtoNulo);

        assertEquals(Situacao.SEPARADA, resultado.getSituacao());
        assertEquals(produtoAtual, resultado.getProduto());
        assertEquals(usuarioAtual, resultado.getUsuario());
        assertEquals(3, resultado.getQuantidade());
        verify(produtoRepository, never()).findById(any());
        verify(usuarioRepository, never()).findById(any());
        verify(produtoRepository, never()).save(any());
        verify(operacaoRepository).save(any(OperacaoEntity.class));
    }

    @Test
    @DisplayName("Atualizar: se já REALIZADA não deve debitar estoque novamente")
    void naoDeveDebitarEstoqueSeJaRealizada() {
        ProdutoEntity produtoAtual = new ProdutoEntity();
        produtoAtual.setId(1L);
        produtoAtual.setNome("Produto");
        produtoAtual.setQuantidade(10);
        produtoAtual.setPreco(BigDecimal.valueOf(100.0));

        OperacaoEntity operacaoExistente = new OperacaoEntity();
        operacaoExistente.setId(1L);
        operacaoExistente.setProduto(produtoAtual);
        operacaoExistente.setSituacao(Situacao.REALIZADA);
        operacaoExistente.setQuantidade(1);

        OperacoesDTORequest dto = new OperacoesDTORequest(null, null, "REALIZADA", null, 5);

        when(operacaoRepository.findComRelacionamentosById(1L)).thenReturn(Optional.of(operacaoExistente));
        when(operacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OperacaoEntity resultado = service.atualizar(1L, dto);

        assertEquals(Situacao.REALIZADA, resultado.getSituacao());
        assertEquals(10, resultado.getProduto().getQuantidade());
        verify(produtoRepository, never()).save(any());
        verify(operacaoRepository).save(any());
    }

    @Test
    @DisplayName("Atualizar: produto não encontrado deve lançar RegraNegocioException")
    void deveLancarQuandoProdutoNaoEncontradoNoAtualizar() {
        OperacaoEntity operacaoExistente = new OperacaoEntity();
        operacaoExistente.setId(1L);
        operacaoExistente.setProduto(new ProdutoEntity());
        operacaoExistente.setSituacao(Situacao.SEPARADA);

        OperacoesDTORequest dto = new OperacoesDTORequest(99L, null, null, null, null);

        when(operacaoRepository.findComRelacionamentosById(1L)).thenReturn(Optional.of(operacaoExistente));
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.atualizar(1L, dto));

        assertEquals("Produto não encontrado com ID: 99", ex.getMessage());
        verify(produtoRepository).findById(99L);
        verify(operacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Atualizar: usuario não encontrado deve lançar RegraNegocioException")
    void deveLancarQuandoUsuarioNaoEncontradoNoAtualizar() {
        ProdutoEntity produtoAtual = new ProdutoEntity();
        produtoAtual.setId(1L);

        OperacaoEntity operacaoExistente = new OperacaoEntity();
        operacaoExistente.setId(1L);
        operacaoExistente.setProduto(produtoAtual);
        operacaoExistente.setSituacao(Situacao.SEPARADA);

        OperacoesDTORequest dto = new OperacoesDTORequest(null, 99L, null, null, null);

        when(operacaoRepository.findComRelacionamentosById(1L)).thenReturn(Optional.of(operacaoExistente));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.atualizar(1L, dto));

        assertEquals("Usuário não encontrado com ID: 99", ex.getMessage());
        verify(usuarioRepository).findById(99L);
        verify(operacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Relatório: quando valor total for null, deve mapear para ZERO")
    void relatorioMapeiaValorNuloParaZero() {
        LocalDate inicio = LocalDate.now().minusDays(10);
        LocalDate fim = LocalDate.now();

        Object[] r1 = new Object[]{1L, "Item", 5L, null};
        List<Object[]> resultados = List.of(new Object[][] { r1 });



        when(operacaoRepository.relatorioVendasPorProduto(inicio, fim)).thenReturn(resultados);

        List<RelatorioVendasProdutoDto> rel = service.relatorioVendasPorProduto(inicio, fim);

        assertEquals(1, rel.size());
        assertEquals(BigDecimal.ZERO, rel.get(0).valorTotal());
        verify(operacaoRepository).relatorioVendasPorProduto(inicio, fim);
    }

    @Test
    @DisplayName("Excluir: quando operação não encontrada deve lançar RegraNegocioException")
    void excluirLancaQuandoNaoEncontrado() {
        when(operacaoRepository.findComRelacionamentosById(99L)).thenReturn(Optional.empty());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.excluir(99L));

        assertEquals("Operação não encontrada!", ex.getMessage());
        verify(operacaoRepository).findComRelacionamentosById(99L);
    }

    @Test
    @DisplayName("validarOperacao privado: lançar exceção quando dados obrigatórios ausentes")
    void validarOperacaoPrivadoLancaQuandoFaltamCampos() throws Throwable {
        OperacaoEntity op = new OperacaoEntity();

        Method m = OperacaoService.class.getDeclaredMethod("validarOperacao", OperacaoEntity.class);
        m.setAccessible(true);

        Executable exec = () -> {
            try {
                m.invoke(service, op);
            } catch (InvocationTargetException ite) {
                throw ite.getTargetException();
            }
        };

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, exec);
        assertEquals("Produto é obrigatório!", ex.getMessage());
    }
}

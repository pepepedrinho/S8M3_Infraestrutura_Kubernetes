package app.zad.zadinventory.controller.dto;

import java.math.BigDecimal;

public record RelatorioVendasProdutoDto(
        Long produtoId,
        String nomeProduto,
        Long quantidadeVendida,
        BigDecimal valorTotal
) {}

package app.zad.zadinventory.controller.dto;

import java.math.BigDecimal;

public record TotalVendasDto(
        Long quantidadeTotal,
        BigDecimal valorTotal
) {}

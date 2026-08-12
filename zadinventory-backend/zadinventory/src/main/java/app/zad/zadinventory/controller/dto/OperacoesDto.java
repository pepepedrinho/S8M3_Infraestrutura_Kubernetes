package app.zad.zadinventory.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OperacoesDto (
        Long id,
        String situacao,
        LocalDate diaOperacao,
        Integer quantidade,
        BigDecimal valorTotal
){
}

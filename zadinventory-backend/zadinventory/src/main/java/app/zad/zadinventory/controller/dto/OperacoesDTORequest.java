package app.zad.zadinventory.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record OperacoesDTORequest(
        Long produtoId,
        Long usuarioId,
        String situacao,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate diaOperacao,
        Integer quantidade
) {
}
package br.edu.invest.sga.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferenciaRequest(
        @NotNull(message = "Informe o novo curso.")
        @Positive(message = "O novo curso deve ser maior que zero.") Long novoCursoId,
        boolean simularFalha) {
}

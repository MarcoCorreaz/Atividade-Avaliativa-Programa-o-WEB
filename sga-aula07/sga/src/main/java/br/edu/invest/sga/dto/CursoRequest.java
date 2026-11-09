package br.edu.invest.sga.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CursoRequest(
	@NotBlank(message = "Informe o nome.") String nome,
	@NotNull(message = "Informe a carga horária.")
	@Positive(message = "A carga horária deve ser maior que zero.") Integer cargaHoraria) {
}

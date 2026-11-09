package br.edu.invest.sga.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

public class ErroResponseDTO {

    private final LocalDateTime timestamp;
    private final Integer status;
    private final String erro;
    private final String mensagem;
    private final String path;
    private final List<CampoErroDTO> campos;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String codigo;

    public ErroResponseDTO(LocalDateTime timestamp, Integer status, String erro,
                           String mensagem, String path, List<CampoErroDTO> campos,
                           String codigo) {
        this.timestamp = timestamp;
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.path = path;
        this.campos = campos == null ? List.of() : List.copyOf(campos);
        this.codigo = codigo;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public String getErro() {
        return erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getPath() {
        return path;
    }

    public List<CampoErroDTO> getCampos() {
        return campos;
    }

    public String getCodigo() {
        return codigo;
    }
}
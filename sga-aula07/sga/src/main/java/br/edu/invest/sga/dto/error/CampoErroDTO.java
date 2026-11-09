package br.edu.invest.sga.dto.error;

public class CampoErroDTO {

    private final String campo;
    private final String mensagem;

    public CampoErroDTO(String campo, String mensagem) {
        this.campo = campo;
        this.mensagem = mensagem;
    }

    public String getCampo() {
        return campo;
    }

    public String getMensagem() {
        return mensagem;
    }
}
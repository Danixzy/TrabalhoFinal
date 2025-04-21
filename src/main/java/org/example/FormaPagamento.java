package org.example;

public enum FormaPagamento {
    CARTAO(1, "Cartão de Crédito"),
    BOLETO(2, "Boleto"),
    PIX(3, "PIX");

    private final int codigo;
    private final String descricao;

    FormaPagamento(int codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public static FormaPagamento fromCodigo(int codigo) {
        for (FormaPagamento forma : FormaPagamento.values()) {
            if (forma.getCodigo() == codigo) {
                return forma;
            }
        }
        return null;
    }
}
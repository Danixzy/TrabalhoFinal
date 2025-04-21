package org.example;

public class PagamentoFactory {
    public static PagamentoStrategy criarEstrategia(FormaPagamento formaPagamento) {
        switch (formaPagamento) {
            case CARTAO:
                return new CartaoCreditoStrategy();
            case BOLETO:
                return new BoletoStrategy();
            case PIX:
                return new PixStrategy();
            default:
                throw new IllegalArgumentException("Forma de pagamento não suportada");
        }
    }
}
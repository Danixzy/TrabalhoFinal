package org.example;

import java.util.UUID;

public class CartaoCreditoStrategy implements PagamentoStrategy {
    @Override
    public String processarPagamento(double valor) {
        String codigoTransacao = UUID.randomUUID().toString();
        return "Código da Transação: " + codigoTransacao;
    }
}
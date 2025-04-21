package org.example;

import java.util.UUID;

public class PixStrategy implements PagamentoStrategy {
    @Override
    public String processarPagamento(double valor) {
        String chavePix = UUID.randomUUID().toString();
        return "Chave PIX: " + chavePix;
    }
}
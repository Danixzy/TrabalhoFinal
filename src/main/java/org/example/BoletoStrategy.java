package org.example;

public class BoletoStrategy implements PagamentoStrategy {
    @Override
    public String processarPagamento(double valor) {
        String codigoBarras = String.format("%04d%04d%04d%04d%04d%04d%04d%04d",
                (int)(Math.random() * 10000),
                (int)(Math.random() * 10000),
                (int)(Math.random() * 10000),
                (int)(Math.random() * 10000),
                (int)(Math.random() * 10000),
                (int)(Math.random() * 10000),
                (int)(Math.random() * 10000),
                (int)(Math.random() * 10000));

        return "Código de Barras: " + codigoBarras;
    }
}
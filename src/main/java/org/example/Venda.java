package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Venda {
    private final Usuario usuario;
    private final List<Produto> produtos;
    private final FormaPagamento formaPagamento;

    public Venda(Usuario usuario, List<Produto> produtos, FormaPagamento formaPagamento) {
        this.usuario = usuario;
        this.produtos = new ArrayList<>(produtos);
        this.formaPagamento = formaPagamento;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public List<Produto> getProdutos() {
        return Collections.unmodifiableList(produtos);
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public double calcularTotal() {
        return produtos.stream().mapToDouble(Produto::getPreco).sum();
    }
}
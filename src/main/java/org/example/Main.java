package org.example;

import java.sql.*;
import java.util.*;
import java.text.DecimalFormat;

public class Main {
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static Connection connection;

    public static void main(String[] args) {
        try {

            connection = DriverManager.getConnection("jdbc:sqlite:ecommerce.db");
            criarTabelasSeNaoExistirem();

            Scanner scanner = new Scanner(System.in);


            System.out.print("Digite o Email do usuário: ");
            String email = scanner.nextLine();
            Usuario usuario = buscarUsuario(email);

            if (usuario == null) {
                System.out.println("Usuário não encontrado!");
                return;
            }
            System.out.println("Usuário encontrado: " + usuario.getNome());


            System.out.print("Digite os IDs dos produtos (separados por vírgula): ");
            String idsInput = scanner.nextLine();
            List<Produto> produtos = buscarProdutos(idsInput);

            if (produtos.isEmpty()) {
                System.out.println("Nenhum produto válido encontrado!");
                return;
            }

            System.out.println("Produtos encontrados:");
            for (Produto produto : produtos) {
                System.out.printf("- %s (R$ %s)%n", produto.getNome(), df.format(produto.getPreco()));
            }


            System.out.println("Escolha a forma de pagamento:");
            System.out.println("1 - Cartão de Crédito");
            System.out.println("2 - Boleto");
            System.out.println("3 - PIX");
            System.out.print("Opção: ");
            int opcaoPagamento = scanner.nextInt();
            scanner.nextLine();

            FormaPagamento formaPagamento = FormaPagamento.fromCodigo(opcaoPagamento);
            if (formaPagamento == null) {
                System.out.println("Forma de pagamento inválida!");
                return;
            }


            Venda venda = new Venda(usuario, produtos, formaPagamento);


            System.out.println("\nAguarde, efetuando pagamento...");
            PagamentoStrategy estrategiaPagamento = PagamentoFactory.criarEstrategia(formaPagamento);
            String comprovante = estrategiaPagamento.processarPagamento(venda.calcularTotal());

            System.out.println("Pagamento confirmado com sucesso via " + formaPagamento.getDescricao() +
                    ". " + comprovante);


            System.out.println("\nResumo da venda:");
            System.out.println("Cliente: " + usuario.getNome());
            System.out.println("Produtos:");
            for (Produto produto : produtos) {
                System.out.println("- " + produto.getNome());
            }
            System.out.println("Valor total: R$ " + df.format(venda.calcularTotal()));
            System.out.println("Pagamento: " + formaPagamento.getDescricao());


            registrarVenda(venda);
            System.out.println("\nVenda registrada com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao acessar o banco de dados: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.out.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }

    private static void criarTabelasSeNaoExistirem() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE)");

        stmt.execute("CREATE TABLE IF NOT EXISTS produtos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "preco REAL NOT NULL)");

        stmt.execute("CREATE TABLE IF NOT EXISTS vendas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario_id INTEGER NOT NULL, " +
                "data_venda TEXT NOT NULL, " +
                "total REAL NOT NULL, " +
                "forma_pagamento TEXT NOT NULL, " +
                "FOREIGN KEY(usuario_id) REFERENCES usuarios(id))");

        stmt.execute("CREATE TABLE IF NOT EXISTS itens_venda (" +
                "venda_id INTEGER NOT NULL, " +
                "produto_id INTEGER NOT NULL, " +
                "quantidade INTEGER NOT NULL DEFAULT 1, " +
                "preco_unitario REAL NOT NULL, " +
                "PRIMARY KEY(venda_id, produto_id), " +
                "FOREIGN KEY(venda_id) REFERENCES vendas(id), " +
                "FOREIGN KEY(produto_id) REFERENCES produtos(id))");

        if (stmt.executeQuery("SELECT COUNT(*) FROM usuarios").getInt(1) == 0) {
            stmt.execute("INSERT INTO usuarios (nome, email) VALUES " +
                    "('Ana Silva', 'ana.silva@email.com'), " +
                    "('Carlos Oliveira', 'carlos.oliveira@email.com'), " +
                    "('Mariana Santos', 'mariana.santos@email.com'), " +
                    "('João Pereira', 'joao.pereira@email.com'), " +
                    "('Luiza Costa', 'luiza.costa@email.com')");
        }

        if (stmt.executeQuery("SELECT COUNT(*) FROM produtos").getInt(1) == 0) {
            stmt.execute("INSERT INTO produtos (nome, preco) VALUES " +
                    "('Smartphone Galaxy S23', 4599.90), " +
                    "('Notebook Dell Inspiron', 3899.00), " +
                    "('Fone de Ouvido Bluetooth', 299.90), " +
                    "('Smart TV 55 Polegadas', 3299.00), " +
                    "('Console PlayStation 5', 4499.90), " +
                    "('Câmera DSLR Canon', 2799.00), " +
                    "('Tablet Samsung Galaxy Tab', 1899.90)");
        }
    }

    private static Usuario buscarUsuario(String email) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT id, nome, email FROM usuarios WHERE email = ?");
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("email"));
        }
        return null;
    }

    private static List<Produto> buscarProdutos(String idsInput) throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        String[] ids = idsInput.split(",");

        for (String idStr : ids) {
            try {
                int id = Integer.parseInt(idStr.trim());
                PreparedStatement stmt = connection.prepareStatement("SELECT id, nome, preco FROM produtos WHERE id = ?");
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    produtos.add(new Produto(rs.getInt("id"), rs.getString("nome"), rs.getDouble("preco")));
                }
            } catch (NumberFormatException e) {

            }
        }

        return produtos;
    }

    private static void registrarVenda(Venda venda) throws SQLException {

        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO vendas (usuario_id, data_venda, total, forma_pagamento) VALUES (?, datetime('now'), ?, ?)",
                Statement.RETURN_GENERATED_KEYS);

        stmt.setInt(1, venda.getUsuario().getId());
        stmt.setDouble(2, venda.calcularTotal());
        stmt.setString(3, venda.getFormaPagamento().name());
        stmt.executeUpdate();


        ResultSet rs = stmt.getGeneratedKeys();
        int vendaId = rs.getInt(1);


        for (Produto produto : venda.getProdutos()) {
            stmt = connection.prepareStatement(
                    "INSERT INTO itens_venda (venda_id, produto_id, preco_unitario) VALUES (?, ?, ?)");

            stmt.setInt(1, vendaId);
            stmt.setInt(2, produto.getId());
            stmt.setDouble(3, produto.getPreco());
            stmt.executeUpdate();
        }
    }
}
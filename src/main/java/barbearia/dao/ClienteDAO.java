// Aqui ficam as consultas SQL dos clientes.
package barbearia.dao;

import barbearia.model.Cliente;
import barbearia.util.DAOException;
import barbearia.util.Ordenacao;
import java.sql.*;
import java.util.*;

public class ClienteDAO implements DAO<Cliente> {
    // Define quais campos podem participar da ordenação.
    private static final Map<String, String> COLUNAS = new LinkedHashMap<>();
    static {
        COLUNAS.put("id", "id"); COLUNAS.put("nome", "nome");
        COLUNAS.put("telefone", "telefone"); COLUNAS.put("email", "email");
    }

    // INSERT acrescenta um cliente à tabela.
    @Override
    public void inserir(Cliente c) throws DAOException {
        // Cada ? recebe um valor separado, sem concatenar o texto informado pelo usuário.
        String sql = "INSERT INTO clientes (nome, telefone, email) VALUES (?,?,?)";
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getTelefone());
            ps.setString(3, c.getEmail());
            // executeUpdate executa comandos que alteram dados, como INSERT/UPDATE/DELETE.
            ps.executeUpdate();
        } catch (SQLException e) { throw Conexao.traduzir(e, "inserir cliente"); }
    }

    // UPDATE modifica a linha identificada pelo ID.
    @Override
    public void atualizar(Cliente c) throws DAOException {
        String sql = "UPDATE clientes SET nome=?, telefone=?, email=? WHERE id=?";
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getTelefone());
            ps.setString(3, c.getEmail());
            ps.setInt(4, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw Conexao.traduzir(e, "atualizar cliente"); }
    }

    // A exclusão pode ser recusada pelo MySQL se houver agendamentos vinculados.
    @Override
    public void excluir(int id) throws DAOException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("DELETE FROM clientes WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw Conexao.traduzir(e, "excluir cliente"); }
    }

    // O parâmetro status faz parte do contrato, mas não se aplica a clientes.
    // A consulta aceita texto parcial no nome, telefone ou e-mail.
    @Override
    public List<Cliente> listar(String busca, String status, String c1, boolean asc1, String c2, boolean asc2) throws DAOException {
        String sql = "SELECT id, nome, telefone, email FROM clientes "
                + "WHERE nome LIKE ? OR telefone LIKE ? OR email LIKE ?"
                + Ordenacao.clausula(COLUNAS, c1, asc1, c2, asc2);
        List<Cliente> lista = new ArrayList<>();
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            // Os sinais % permitem encontrar a busca em qualquer parte do campo.
            String like = "%" + busca + "%";
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                // Cada linha do ResultSet vira um objeto Cliente para a interface.
                while (rs.next())
                    lista.add(new Cliente(rs.getInt("id"), rs.getString("nome"),
                            rs.getString("telefone"), rs.getString("email")));
            }
        } catch (SQLException e) { throw Conexao.traduzir(e, "listar clientes"); }
        return lista;
    }
}

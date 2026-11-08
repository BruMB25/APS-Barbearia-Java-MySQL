// Aqui ficam as operações do banco relacionadas aos serviços.
package barbearia.dao;

import barbearia.model.Servico;
import barbearia.util.DAOException;
import barbearia.util.Ordenacao;
import java.sql.*;
import java.util.*;

public class ServicoDAO implements DAO<Servico> {
    // A lista segura relaciona nomes da tela às colunas reais do banco.
    private static final Map<String, String> COLUNAS = new LinkedHashMap<>();
    static {
        COLUNAS.put("id", "id"); COLUNAS.put("nome", "nome");
        COLUNAS.put("preco", "preco"); COLUNAS.put("duracao", "duracao_min");
    }

    // INSERT guarda nome, descrição, preço e duração.
    @Override
    public void inserir(Servico s) throws DAOException {
        String sql = "INSERT INTO servicos (nome, descricao, preco, duracao_min) VALUES (?,?,?,?)";
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getNome());
            ps.setString(2, s.getDescricao());
            // O valor do serviço é enviado como número decimal exato.
            ps.setBigDecimal(3, s.getPreco());
            ps.setInt(4, s.getDuracaoMin());
            ps.executeUpdate();
        } catch (SQLException e) { throw Conexao.traduzir(e, "inserir serviço"); }
    }

    // UPDATE usa o ID para alterar o serviço correto.
    @Override
    public void atualizar(Servico s) throws DAOException {
        String sql = "UPDATE servicos SET nome=?, descricao=?, preco=?, duracao_min=? WHERE id=?";
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getNome());
            ps.setString(2, s.getDescricao());
            ps.setBigDecimal(3, s.getPreco());
            ps.setInt(4, s.getDuracaoMin());
            ps.setInt(5, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw Conexao.traduzir(e, "atualizar serviço"); }
    }

    // Serviços já vinculados a agendamentos ficam protegidos pela chave estrangeira.
    @Override
    public void excluir(int id) throws DAOException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("DELETE FROM servicos WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw Conexao.traduzir(e, "excluir serviço"); }
    }

    // O parâmetro status faz parte do contrato, mas não se aplica a serviços.
    // Busca por nome ou descrição e permite ordenar por dois critérios.
    @Override
    public List<Servico> listar(String busca, String status, String c1, boolean asc1, String c2, boolean asc2) throws DAOException {
        String sql = "SELECT id, nome, descricao, preco, duracao_min FROM servicos "
                + "WHERE nome LIKE ? OR descricao LIKE ?"
                + Ordenacao.clausula(COLUNAS, c1, asc1, c2, asc2);
        List<Servico> lista = new ArrayList<>();
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + busca + "%";
            ps.setString(1, like); ps.setString(2, like);
            // Percorre o resultado e recria os objetos que aparecerão na tabela.
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(new Servico(rs.getInt("id"), rs.getString("nome"), rs.getString("descricao"),
                            rs.getBigDecimal("preco"), rs.getInt("duracao_min")));
            }
        } catch (SQLException e) { throw Conexao.traduzir(e, "listar serviços"); }
        return lista;
    }
}

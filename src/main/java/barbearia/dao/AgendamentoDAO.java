// Trata agendamentos e seus serviços, inclusive salvando tudo na mesma transação.
package barbearia.dao;

import barbearia.model.*;
import barbearia.util.DAOException;
import barbearia.util.Ordenacao;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** DAO de Agendamento: grava também a tabela relacional agendamento_servico (dentro de transação). */
public class AgendamentoDAO implements DAO<Agendamento> {
    // Estes nomes relacionam as opções de ordenação às colunas SQL.
    private static final Map<String, String> COLUNAS = new LinkedHashMap<>();
    static {
        COLUNAS.put("id", "a.id"); COLUNAS.put("cliente", "c.nome"); COLUNAS.put("data", "a.data_hora");
        COLUNAS.put("status", "a.status"); COLUNAS.put("total", "total");
    }

    // Gravar um agendamento envolve a tabela principal e a associativa.
    @Override
    public void inserir(Agendamento a) throws DAOException {
        String sql = "INSERT INTO agendamentos (id_cliente, data_hora, status, observacao) VALUES (?,?,?,?)";
        try (Connection con = Conexao.getConexao()) {
            // Desliga o commit automático para salvar todas as etapas juntas.
            con.setAutoCommit(false);
            try {
                int id;
                // Pede ao banco o ID que o AUTO_INCREMENT acabou de criar.
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, a.getCliente().getId());
                    ps.setTimestamp(2, Timestamp.valueOf(a.getDataHora()));
                    ps.setString(3, a.getStatus());
                    ps.setString(4, a.getObservacao());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); id = rs.getInt(1); }
                }
                // Com o ID em mãos, salva os serviços escolhidos.
                gravarItens(con, id, a.getServicos());
                // Confirma a transação somente quando não houve erro.
                con.commit();
            // Se algo falhar, desfaz as alterações da transação.
            } catch (SQLException e) { con.rollback(); throw e; }
        } catch (SQLException e) { throw Conexao.traduzir(e, "inserir agendamento"); }
    }

    // Atualiza o cabeçalho e depois recria os itens na mesma transação.
    @Override
    public void atualizar(Agendamento a) throws DAOException {
        String sql = "UPDATE agendamentos SET id_cliente=?, data_hora=?, status=?, observacao=? WHERE id=?";
        try (Connection con = Conexao.getConexao()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, a.getCliente().getId());
                    ps.setTimestamp(2, Timestamp.valueOf(a.getDataHora()));
                    ps.setString(3, a.getStatus());
                    ps.setString(4, a.getObservacao());
                    ps.setInt(5, a.getId());
                    ps.executeUpdate();
                }
                // ATENÇÃO: apagar e recriar os itens pode mudar o preço histórico do agendamento.
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM agendamento_servico WHERE id_agendamento=?")) {
                    ps.setInt(1, a.getId());
                    ps.executeUpdate();
                }
                gravarItens(con, a.getId(), a.getServicos());
                con.commit();
            } catch (SQLException e) { con.rollback(); throw e; }
        } catch (SQLException e) { throw Conexao.traduzir(e, "atualizar agendamento"); }
    }

    /** Exclui o agendamento; os itens em agendamento_servico saem por ON DELETE CASCADE. */
    // Excluir o registro principal apaga os itens pelo CASCADE definido no SQL.
    @Override
    public void excluir(int id) throws DAOException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("DELETE FROM agendamentos WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw Conexao.traduzir(e, "excluir agendamento"); }
    }

    // Usa lote (batch) para inserir todos os serviços de uma vez.
    private void gravarItens(Connection con, int idAgendamento, List<Servico> servicos) throws SQLException {
        String sql = "INSERT INTO agendamento_servico (id_agendamento, id_servico, preco_cobrado) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Servico s : servicos) {
                ps.setInt(1, idAgendamento);
                ps.setInt(2, s.getId());
                ps.setBigDecimal(3, s.getPreco());
                // Acumula os serviços antes de executar o lote.
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Verifica colisão de horário usando a duração total (soma dos serviços) de cada agendamento. */
    // Dois intervalos se cruzam quando início A < fim B e fim A > início B.
    @Override
    public boolean existeConflito(LocalDateTime inicio, int duracaoMin, int ignorarId) throws DAOException {
        String sql = "SELECT COUNT(*) FROM agendamentos a "
                + "JOIN (SELECT i.id_agendamento AS ida, SUM(s.duracao_min) AS dur "
                + "      FROM agendamento_servico i JOIN servicos s ON s.id = i.id_servico "
                + "      GROUP BY i.id_agendamento) d ON d.ida = a.id "
                + "WHERE a.status <> 'CANCELADO' AND a.id <> ? "
                // Compara início e fim dos atendimentos, ignorando cancelados e o ID em edição.
                + "AND a.data_hora < ? AND DATE_ADD(a.data_hora, INTERVAL d.dur MINUTE) > ?";
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ignorarId);
            ps.setTimestamp(2, Timestamp.valueOf(inicio.plusMinutes(duracaoMin)));
            ps.setTimestamp(3, Timestamp.valueOf(inicio));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { throw Conexao.traduzir(e, "verificar conflito de horário"); }
    }

    /** Lista agendamentos filtrando por cliente/serviço e status ("TODOS" ignora o status). */
    // Busca agendamentos pelo nome do cliente ou pelo nome dos serviços.
    @Override
    public List<Agendamento> listar(String busca, String status, String c1, boolean asc1,
                                    String c2, boolean asc2) throws DAOException {
        // Quando o filtro for TODOS, não acrescenta condição de status.
        boolean todos = "TODOS".equals(status);
        String sql = "SELECT a.id, a.data_hora, a.status, a.observacao, "
                + "c.id AS cid, c.nome AS cnome, c.telefone, c.email, "
                + "(SELECT COALESCE(SUM(i.preco_cobrado),0) FROM agendamento_servico i WHERE i.id_agendamento = a.id) AS total "
                + "FROM agendamentos a JOIN clientes c ON c.id = a.id_cliente "
                + "WHERE (c.nome LIKE ? OR EXISTS (SELECT 1 FROM agendamento_servico i2 "
                + "      JOIN servicos s2 ON s2.id = i2.id_servico "
                + "      WHERE i2.id_agendamento = a.id AND s2.nome LIKE ?)) "
                + (todos ? "" : "AND a.status = ? ")
                + Ordenacao.clausula(COLUNAS, c1, asc1, c2, asc2);
        // O mapa mantém a ordem da consulta e facilita associar serviços por ID.
        Map<Integer, Agendamento> mapa = new LinkedHashMap<>();
        try (Connection con = Conexao.getConexao()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                String like = "%" + busca + "%";
                ps.setString(1, like); ps.setString(2, like);
                if (!todos) ps.setString(3, status);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Agendamento a = new Agendamento();
                        a.setId(rs.getInt("id"));
                        a.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
                        a.setStatus(rs.getString("status"));
                        a.setObservacao(rs.getString("observacao"));
                        a.setCliente(new Cliente(rs.getInt("cid"), rs.getString("cnome"),
                                rs.getString("telefone"), rs.getString("email")));
                        mapa.put(a.getId(), a);
                    }
                }
            }
            // A segunda consulta preenche a lista de serviços dos agendamentos encontrados.
            carregarServicos(con, mapa);
        } catch (SQLException e) { throw Conexao.traduzir(e, "listar agendamentos"); }
        return new ArrayList<>(mapa.values());
    }

    /** Busca, numa única consulta, os serviços de todos os agendamentos listados. */
    // Carrega serviços de vários agendamentos numa única consulta.
    private void carregarServicos(Connection con, Map<Integer, Agendamento> mapa) throws SQLException {
        if (mapa.isEmpty()) return;
        // Cria um ? para cada ID selecionado, mantendo parâmetros seguros.
        String marcas = String.join(",", Collections.nCopies(mapa.size(), "?"));
        String sql = "SELECT i.id_agendamento, s.id, s.nome, s.descricao, i.preco_cobrado, s.duracao_min "
                + "FROM agendamento_servico i JOIN servicos s ON s.id = i.id_servico "
                + "WHERE i.id_agendamento IN (" + marcas + ") ORDER BY s.nome";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int n = 1;
            // Preenche cada placeholder com um ID de agendamento.
            for (Integer id : mapa.keySet()) ps.setInt(n++, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    mapa.get(rs.getInt(1)).getServicos().add(new Servico(rs.getInt(2), rs.getString(3),
                            rs.getString(4), rs.getBigDecimal(5), rs.getInt(6)));
            }
        }
    }
}

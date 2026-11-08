// Centraliza o acesso ao MySQL e transforma erros do banco em mensagens compreensíveis.
package barbearia.dao;

import barbearia.util.DAOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexões JDBC com o MySQL.
 * Defina as variáveis de ambiente DB_URL, DB_USER e DB_PASS.
 */
public class Conexao {
    // O endereço do banco pode vir de uma variável de ambiente.
    private static final String URL = env("DB_URL",
            "jdbc:mysql://localhost:3306/barbearia?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo");
    // Usa root como valor padrão caso DB_USER não esteja configurada.
    private static final String USUARIO = env("DB_USER", "root");
    // É melhor passar a senha por DB_PASS em vez de colocá-la no código.
    private static final String SENHA = env("DB_PASS", "");

    // Se a variável estiver vazia, mantém o valor padrão.
    private static String env(String k, String padrao) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? padrao : v;
    }

    // O inicializador usa exatamente esta mesma configuracao de conexao.
    static String urlJdbc() { return URL; }
    static String usuario() { return USUARIO; }
    static String senha() { return SENHA; }

    // Cada DAO pede sua própria conexão quando precisa fazer uma operação.
    public static Connection getConexao() throws DAOException {
        try {
            // JDBC abre a comunicação com o servidor MySQL.
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            throw new DAOException("Não foi possível conectar ao MySQL. Verifique se o servidor está ligado, "
                    + "se usuário/senha estão corretos e se o banco pode ser acessado.", e);
        }
    }

    /** Traduz erros SQL comuns em mensagens amigáveis. */
    // Códigos conhecidos do MySQL viram mensagens mais fáceis para quem usa.
    public static DAOException traduzir(SQLException e, String operacao) {
        switch (e.getErrorCode()) {
            // 1451 normalmente indica tentativa de apagar um registro ainda referenciado.
            case 1451: return new DAOException("Não é possível excluir: existem agendamentos vinculados a este registro.", e);
            // 1452 indica referência a cliente ou serviço inexistente.
            case 1452: return new DAOException("Cliente ou serviço informado não existe.", e);
            // 1062 acontece quando um valor UNIQUE está repetido.
            case 1062: return new DAOException("Já existe um registro com esse valor único (telefone ou nome do serviço).", e);
            default:   return new DAOException("Erro ao " + operacao + ": " + e.getMessage(), e);
        }
    }
}

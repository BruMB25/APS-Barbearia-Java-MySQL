// Faz a preparacao inicial do MySQL quando o programa e aberto.
package barbearia.dao;

import barbearia.util.DAOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Cria o banco se estiver faltando e garante que as tabelas existam. */
public final class InicializadorBanco {
    private InicializadorBanco() {}

    // Reaproveita a configuracao centralizada em Conexao.java.
    private static final String URL = Conexao.urlJdbc();
    private static final String USUARIO = Conexao.usuario();
    private static final String SENHA = Conexao.senha();

    public static void preparar() throws DAOException {
        try (Connection conexao = DriverManager.getConnection(URL, USUARIO, SENHA)) {
            // Quando o banco ja existe, so confere se faltou criar alguma tabela.
            criarTabelas(conexao);
        } catch (SQLException e) {
            // O codigo 1049 significa que o banco informado na URL ainda nao existe.
            if (e.getErrorCode() != 1049) {
                throw new DAOException("Nao foi possivel preparar o MySQL: " + e.getMessage(), e);
            }
            // Primeira execucao: cria o banco sem apagar outros bancos ou tabelas.
            criarBanco();
            try (Connection conexao = DriverManager.getConnection(URL, USUARIO, SENHA)) {
                criarTabelas(conexao);
            } catch (SQLException erro) {
                throw new DAOException("O banco foi criado, mas houve um erro ao preparar as tabelas: "
                        + erro.getMessage(), erro);
            }
        }
    }

    private static void criarBanco() throws DAOException {
        // Remove somente o nome do banco da URL para entrar primeiro no servidor.
        int inicioParametros = URL.indexOf('?');
        String endereco = inicioParametros >= 0 ? URL.substring(0, inicioParametros) : URL;
        String parametros = inicioParametros >= 0 ? URL.substring(inicioParametros) : "";
        int ultimaBarra = endereco.lastIndexOf('/');
        if (!endereco.startsWith("jdbc:mysql://") || ultimaBarra < "jdbc:mysql://".length()) {
            throw new DAOException("Verifique a DB_URL. Exemplo: jdbc:mysql://localhost:3306/barbearia");
        }
        String banco = endereco.substring(ultimaBarra + 1);
        // Valida o nome porque ele vira parte de um comando SQL de criacao.
        if (!banco.matches("[A-Za-z][A-Za-z0-9_]*")) {
            throw new DAOException("Nome do banco invalido na DB_URL: " + banco);
        }
        String urlServidor = endereco.substring(0, ultimaBarra + 1) + parametros;
        String comando = "CREATE DATABASE IF NOT EXISTS `" + banco
                + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
        try (Connection conexao = DriverManager.getConnection(urlServidor, USUARIO, SENHA);
             Statement stmt = conexao.createStatement()) {
            stmt.execute(comando);
        } catch (SQLException e) {
            throw new DAOException("Nao foi possivel criar o banco automaticamente. "
                    + "Confira usuario, senha e permissao CREATE: " + e.getMessage(), e);
        }
    }

    private static void criarTabelas(Connection conexao) throws DAOException {
        // O arquivo SQL fica em resources para funcionar tambem fora do IntelliJ.
        try (InputStream arquivo = InicializadorBanco.class.getResourceAsStream("/db/tabelas.sql")) {
            if (arquivo == null) {
                throw new DAOException("Arquivo interno db/tabelas.sql nao encontrado. Reimporte o projeto Maven.");
            }
            try (BufferedReader leitor = new BufferedReader(
                    new InputStreamReader(arquivo, StandardCharsets.UTF_8));
                 Statement stmt = conexao.createStatement()) {
                StringBuilder comando = new StringBuilder();
                String linha;
                while ((linha = leitor.readLine()) != null) {
                    String texto = linha.trim();
                    if (texto.isEmpty() || texto.startsWith("--")) continue;
                    comando.append(linha).append('\n');
                    // Neste arquivo, cada CREATE TABLE termina com ponto e virgula.
                    if (texto.endsWith(";")) {
                        String sql = comando.toString().trim();
                        stmt.execute(sql.substring(0, sql.length() - 1));
                        comando.setLength(0);
                    }
                }
                if (!comando.toString().isBlank()) {
                    throw new DAOException("O script de tabelas possui um comando SQL incompleto.");
                }
            }
        } catch (SQLException | IOException e) {
            throw new DAOException("Erro ao verificar/criar as tabelas: " + e.getMessage(), e);
        }
    }
}

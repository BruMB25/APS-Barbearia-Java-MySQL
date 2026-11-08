// É por aqui que o programa começa: testa o MySQL e só depois abre a janela.
package barbearia;

import barbearia.dao.Conexao;
import barbearia.dao.InicializadorBanco;
import barbearia.view.MainFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // A interface precisa nascer na thread de eventos do Swing (EDT).
        SwingUtilities.invokeLater(() -> {
            // Tenta usar a aparência padrão do Windows; se não der, o programa segue.
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            // Antes de abrir a tela, cria o banco e as tabelas caso ainda nao existam.
            // Se ja estiverem prontos, nao apaga nem recria os registros.
            try {
                InicializadorBanco.preparar();
                // Confirma que a conexao principal da aplicacao esta funcionando.
                try (java.sql.Connection c = Conexao.getConexao()) { /* conexao validada */ }
                // Só mostra a janela quando o banco respondeu corretamente.
                new MainFrame().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Erro de conexão", JOptionPane.ERROR_MESSAGE);
                // Sem conexão, a aplicação informa o erro e interrompe a abertura.
                System.exit(1);
            }
        });
    }
}

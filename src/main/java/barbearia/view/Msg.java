// Evita repetir o mesmo código de alerta e confirmação em todas as telas.
package barbearia.view;

import barbearia.util.ValidacaoException;
import java.awt.Component;
import javax.swing.JOptionPane;

/** Diálogos padronizados e tratamento central de erros da interface. */
public class Msg {
    // Mostra uma mensagem de sucesso ou orientação.
    public static void info(Component p, String m) {
        JOptionPane.showMessageDialog(p, m, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    // Diferencia falha de preenchimento de um problema inesperado.
    public static void erro(Component p, Exception e) {
        boolean validacao = e instanceof ValidacaoException;
        JOptionPane.showMessageDialog(p, e.getMessage() == null ? "Erro inesperado." : e.getMessage(),
                validacao ? "Dados inválidos" : "Erro", validacao ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }
    // Retorna true somente se o usuário clicar em Sim.
    public static boolean confirmar(Component p, String m) {
        return JOptionPane.showConfirmDialog(p, m, "Confirmação", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}

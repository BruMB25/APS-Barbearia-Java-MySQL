// Pequenos atalhos para organizar linhas e caixas de componentes na tela.
package barbearia.view;

import java.awt.*;
import javax.swing.*;

/** Ajudantes de layout: caixas com título e linhas de componentes empilhadas (sem cortar em telas menores). */
public class Ui {
    // Cria uma seção com título e componentes empilhados.
    public static JPanel caixa(String titulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        return p;
    }

    // Agrupa componentes lado a lado com espaçamento.
    public static JPanel linha(Component... componentes) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Percorre os componentes recebidos e os coloca na linha.
        for (Component c : componentes) p.add(c);
        return p;
    }
}

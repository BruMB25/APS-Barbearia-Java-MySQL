// Janela principal que reúne as três abas do sistema.
package barbearia.view;

import javax.swing.*;
import barbearia.controller.AgendamentoController;
import barbearia.controller.ClienteController;
import barbearia.controller.ServicoController;
import barbearia.dao.AgendamentoDAO;
import barbearia.dao.ClienteDAO;
import barbearia.dao.ServicoDAO;

/** Janela principal com uma aba para cada entidade. */
public class MainFrame extends JFrame {
    // O título da janela e seu tamanho são definidos aqui.
    public MainFrame() {
        super("Barbearia - Sistema de Gerenciamento");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 600);
        setLocationRelativeTo(null);

        // É aqui que escolhemos os DAOs concretos. Os controllers recebem só as interfaces.
        ClienteController clienteCtrl = new ClienteController(new ClienteDAO());
        ServicoController servicoCtrl = new ServicoController(new ServicoDAO());
        AgendamentoController agendamentoCtrl = new AgendamentoController(new AgendamentoDAO());

        // Reaproveitamos os mesmos controllers nas telas que precisam deles.
        ClientePanel clientes = new ClientePanel(clienteCtrl);
        ServicoPanel servicos = new ServicoPanel(servicoCtrl);
        AgendamentoPanel agendamentos = new AgendamentoPanel(agendamentoCtrl, clienteCtrl, servicoCtrl);

        // Cria as abas de clientes, serviços e agendamentos.
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Clientes", clientes);
        abas.addTab("Serviços", servicos);
        abas.addTab("Agendamentos", agendamentos);
        // Ao abrir a aba de agendamentos, recarrega clientes/serviços atualizados
        // Ao abrir Agendamentos, atualiza as listas de clientes e serviços.
        abas.addChangeListener(e -> { if (abas.getSelectedComponent() == agendamentos) agendamentos.atualizar(); });
        add(abas);
    }
}

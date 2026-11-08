// Aba usada para marcar horários, escolher vários serviços e consultar agendamentos.
package barbearia.view;

import barbearia.controller.*;
import barbearia.model.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AgendamentoPanel extends JPanel {
    // Os três estados disponíveis também existem no ENUM do MySQL.
    private static final String[] STATUS = {"AGENDADO", "CONCLUIDO", "CANCELADO"};
    private final AgendamentoController ctrl;
    private final ClienteController clienteCtrl;
    private final ServicoController servicoCtrl;

    private final String[] rotulos = {"ID", "Cliente", "Serviços", "Data/Hora", "Total (R$)", "Status", "Observação"};
    private final String[] rotOrd = {"ID", "Cliente", "Data/Hora", "Status", "Total"};
    private final String[] colunas = {"id", "cliente", "data", "status", "total"};
    private final DefaultTableModel modelo = new DefaultTableModel(rotulos, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(modelo);
    // Mantém os objetos na mesma ordem apresentada pela tabela.
    private List<Agendamento> atual = new ArrayList<>();

    private final JTextField txtBusca = new JTextField(14), txtData = new JTextField(12), txtObs = new JTextField(20);
    private final JComboBox<String> cbStatusFiltro = new JComboBox<>(new String[]{"TODOS", "AGENDADO", "CONCLUIDO", "CANCELADO"});
    private final JComboBox<String> cbOrd1 = new JComboBox<>(rotOrd), cbOrd2 = new JComboBox<>(rotOrd);
    private final JComboBox<String> cbDir1 = new JComboBox<>(new String[]{"Crescente", "Decrescente"}),
            cbDir2 = new JComboBox<>(new String[]{"Crescente", "Decrescente"});
    // O usuário escolhe um cliente já cadastrado, não digita o ID na mão.
    private final JComboBox<Cliente> cbCliente = new JComboBox<>();
    private final DefaultListModel<Servico> modeloServicos = new DefaultListModel<>();
    // Permite selecionar mais de um serviço no mesmo agendamento.
    private final JList<Servico> lstServicos = new JList<>(modeloServicos);
    private final JComboBox<String> cbStatus = new JComboBox<>(STATUS);
    private Integer idSel = null;

    // Organiza os filtros, tabela de resultados e formulário de agendamento.
    public AgendamentoPanel(AgendamentoController ctrl, ClienteController clienteCtrl,
                            ServicoController servicoCtrl) {
        this.ctrl = java.util.Objects.requireNonNull(ctrl, "Controller de agendamentos não pode ser nulo");
        this.clienteCtrl = java.util.Objects.requireNonNull(clienteCtrl, "Controller de clientes não pode ser nulo");
        this.servicoCtrl = java.util.Objects.requireNonNull(servicoCtrl, "Controller de serviços não pode ser nulo");
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cbOrd1.setSelectedIndex(2);

        JButton btBuscar = new JButton("Buscar"), btLimparBusca = new JButton("Limpar busca");
        JPanel filtro = Ui.caixa("Busca, filtro e ordenação");
        filtro.add(Ui.linha(new JLabel("Cliente/serviço:"), txtBusca, new JLabel("Status:"), cbStatusFiltro,
                btBuscar, btLimparBusca));
        filtro.add(Ui.linha(new JLabel("Ordenar por:"), cbOrd1, cbDir1, new JLabel("depois por:"), cbOrd2, cbDir2));
        add(filtro, BorderLayout.NORTH);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(220);
        tabela.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) preencherForm(); });
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // Com MULTIPLE_INTERVAL_SELECTION é possível combinar serviços usando Ctrl.
        lstServicos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstServicos.setVisibleRowCount(4);
        JScrollPane spServicos = new JScrollPane(lstServicos);
        spServicos.setPreferredSize(new Dimension(300, 85));

        JButton btIncluir = new JButton("Incluir"), btSalvar = new JButton("Salvar alteração"),
                btExcluir = new JButton("Excluir"), btNovo = new JButton("Limpar formulário");
        JPanel form = Ui.caixa("Dados do agendamento");
        form.add(Ui.linha(new JLabel("Cliente:"), cbCliente, new JLabel("Data/hora (dd/MM/aaaa HH:mm):"), txtData,
                new JLabel("Status:"), cbStatus));
        form.add(Ui.linha(new JLabel("Serviços (Ctrl = vários):"), spServicos, new JLabel("Obs.:"), txtObs));
        form.add(Ui.linha(btIncluir, btSalvar, btExcluir, btNovo));
        add(form, BorderLayout.SOUTH);

        // Reexecuta a consulta sem precisar reiniciar a aplicação.
        btBuscar.addActionListener(e -> listar());
        txtBusca.addActionListener(e -> listar());
        btLimparBusca.addActionListener(e -> { txtBusca.setText(""); cbStatusFiltro.setSelectedIndex(0); listar(); });
        btIncluir.addActionListener(e -> incluir());
        btSalvar.addActionListener(e -> salvar());
        btExcluir.addActionListener(e -> excluir());
        btNovo.addActionListener(e -> limparForm());
        atualizar();
    }

    /** Recarrega clientes/serviços dos seletores e a tabela; chamado ao abrir a aba. */
    // Recarrega os cadastros quando a aba de agendamentos é aberta.
    public void atualizar() {
        try {
            // Limpa a seleção antiga para não duplicar clientes.
            cbCliente.removeAllItems();
            for (Cliente c : clienteCtrl.listar("", "nome", true, "id", true)) cbCliente.addItem(c);
            modeloServicos.clear();
            for (Servico s : servicoCtrl.listar("", "nome", true, "id", true)) modeloServicos.addElement(s);
        } catch (Exception ex) { Msg.erro(this, ex); }
        listar();
    }

    // Atualiza a tabela de agendamentos conforme busca, status e ordenação.
    public void listar() {
        try {
            atual = ctrl.listar(txtBusca.getText(), (String) cbStatusFiltro.getSelectedItem(),
                    colunas[cbOrd1.getSelectedIndex()], cbDir1.getSelectedIndex() == 0,
                    colunas[cbOrd2.getSelectedIndex()], cbDir2.getSelectedIndex() == 0);
            // Remove as linhas antigas antes de desenhar o novo resultado.
            modelo.setRowCount(0);
            for (Agendamento a : atual)
                modelo.addRow(new Object[]{a.getId(), a.getCliente().getNome(), a.getServicosTexto(),
                        a.getDataHora().format(AgendamentoController.FORMATO), a.getTotal().toPlainString(),
                        a.getStatus(), a.getObservacao() == null ? "" : a.getObservacao()});
            idSel = null;
        } catch (Exception ex) { Msg.erro(this, ex); }
    }

    // Ao selecionar uma linha, recupera cliente, serviços, data e status.
    private void preencherForm() {
        int r = tabela.getSelectedRow();
        if (r < 0 || r >= atual.size()) return;
        Agendamento a = atual.get(r);
        idSel = a.getId();
        for (int i = 0; i < cbCliente.getItemCount(); i++)
            if (cbCliente.getItemAt(i).getId() == a.getCliente().getId()) { cbCliente.setSelectedIndex(i); break; }
        // Converte IDs dos serviços gravados em posições da lista atual da tela.
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < modeloServicos.size(); i++)
            for (Servico s : a.getServicos())
                if (s.getId() == modeloServicos.get(i).getId()) indices.add(i);
        lstServicos.setSelectedIndices(indices.stream().mapToInt(Integer::intValue).toArray());
        txtData.setText(a.getDataHora().format(AgendamentoController.FORMATO));
        cbStatus.setSelectedItem(a.getStatus());
        txtObs.setText(a.getObservacao() == null ? "" : a.getObservacao());
    }

    // Limpa os campos e tira a seleção de agendamento.
    private void limparForm() {
        txtData.setText(""); txtObs.setText(""); cbStatus.setSelectedIndex(0);
        lstServicos.clearSelection();
        tabela.clearSelection(); idSel = null;
    }

    // Envia todos os valores ao controller, incluindo serviços selecionados.
    private void incluir() {
        try {
            ctrl.inserir((Cliente) cbCliente.getSelectedItem(), lstServicos.getSelectedValuesList(),
                    txtData.getText(), (String) cbStatus.getSelectedItem(), txtObs.getText());
            Msg.info(this, "Agendamento cadastrado com sucesso!");
            limparForm(); listar();
        } catch (Exception ex) { Msg.erro(this, ex); }
    }

    // Altera o registro escolhido usando o ID armazenado.
    private void salvar() {
        try {
            ctrl.atualizar(idSel, (Cliente) cbCliente.getSelectedItem(), lstServicos.getSelectedValuesList(),
                    txtData.getText(), (String) cbStatus.getSelectedItem(), txtObs.getText());
            Msg.info(this, "Agendamento atualizado com sucesso!");
            limparForm(); listar();
        } catch (Exception ex) { Msg.erro(this, ex); }
    }

    // Confirma a exclusão do agendamento e dos itens ligados a ele.
    private void excluir() {
        try {
            if (idSel == null) { ctrl.excluir(null); return; }
            if (!Msg.confirmar(this, "Deseja realmente excluir este agendamento (e seus serviços)?")) return;
            ctrl.excluir(idSel);
            Msg.info(this, "Agendamento excluído.");
            limparForm(); listar();
        } catch (Exception ex) { Msg.erro(this, ex); }
    }
}

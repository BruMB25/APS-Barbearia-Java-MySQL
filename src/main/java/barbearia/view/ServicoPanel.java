// Monta a aba de serviços, incluindo busca, tabela e formulário.
package barbearia.view;

import barbearia.controller.ServicoController;
import barbearia.model.Servico;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ServicoPanel extends JPanel {
    // Todas as ações passam pelo controller para validar entradas.
    private final ServicoController ctrl;
    private final String[] rotulos = {"ID", "Nome", "Descrição", "Preço (R$)", "Duração (min)"};
    private final String[] rotOrd = {"ID", "Nome", "Preço", "Duração"};
    private final String[] colunas = {"id", "nome", "preco", "duracao"};
    // O modelo controla o conteúdo mostrado na JTable.
    private final DefaultTableModel modelo = new DefaultTableModel(rotulos, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(modelo);
    private final JTextField txtBusca = new JTextField(14), txtNome = new JTextField(14),
            txtDesc = new JTextField(18), txtPreco = new JTextField(6), txtDur = new JTextField(4);
    private final JComboBox<String> cbOrd1 = new JComboBox<>(rotOrd), cbOrd2 = new JComboBox<>(rotOrd);
    private final JComboBox<String> cbDir1 = new JComboBox<>(new String[]{"Crescente", "Decrescente"}),
            cbDir2 = new JComboBox<>(new String[]{"Crescente", "Decrescente"});
    // Sem um ID selecionado, não é possível salvar alteração ou excluir.
    private Integer idSel = null;

    // Organiza os filtros em cima, tabela ao centro e formulário embaixo.
    public ServicoPanel(ServicoController ctrl) {
        this.ctrl = java.util.Objects.requireNonNull(ctrl, "Controller não pode ser nulo");
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cbOrd1.setSelectedIndex(1);

        JButton btBuscar = new JButton("Buscar"), btLimparBusca = new JButton("Limpar busca");
        JPanel filtro = Ui.caixa("Busca e ordenação");
        filtro.add(Ui.linha(new JLabel("Buscar:"), txtBusca, btBuscar, btLimparBusca));
        filtro.add(Ui.linha(new JLabel("Ordenar por:"), cbOrd1, cbDir1, new JLabel("depois por:"), cbOrd2, cbDir2));
        add(filtro, BorderLayout.NORTH);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Selecionar uma linha carrega os dados do serviço no formulário.
        tabela.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) preencherForm(); });
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JButton btIncluir = new JButton("Incluir"), btSalvar = new JButton("Salvar alteração"),
                btExcluir = new JButton("Excluir"), btNovo = new JButton("Limpar formulário");
        JPanel form = Ui.caixa("Dados do serviço");
        form.add(Ui.linha(new JLabel("Nome:"), txtNome, new JLabel("Descrição:"), txtDesc,
                new JLabel("Preço (R$):"), txtPreco, new JLabel("Duração (min):"), txtDur));
        form.add(Ui.linha(btIncluir, btSalvar, btExcluir, btNovo));
        add(form, BorderLayout.SOUTH);

        btBuscar.addActionListener(e -> listar());
        txtBusca.addActionListener(e -> listar());
        btLimparBusca.addActionListener(e -> { txtBusca.setText(""); listar(); });
        // Cada botão é ligado ao método que realiza sua operação.
        btIncluir.addActionListener(e -> incluir());
        btSalvar.addActionListener(e -> salvar());
        btExcluir.addActionListener(e -> excluir());
        btNovo.addActionListener(e -> limparForm());
        listar();
    }

    // Busca os serviços já filtrados e ordenados.
    public void listar() {
        try {
            List<Servico> lista = ctrl.listar(txtBusca.getText(), colunas[cbOrd1.getSelectedIndex()],
                    cbDir1.getSelectedIndex() == 0, colunas[cbOrd2.getSelectedIndex()], cbDir2.getSelectedIndex() == 0);
            modelo.setRowCount(0);
            for (Servico s : lista)
                modelo.addRow(new Object[]{s.getId(), s.getNome(), s.getDescricao() == null ? "" : s.getDescricao(),
                        s.getPreco().toPlainString(), s.getDuracaoMin()});
            idSel = null;
        } catch (Exception ex) { Msg.erro(this, ex); }
    }

    // Preenche os campos com os dados da linha selecionada.
    private void preencherForm() {
        int r = tabela.getSelectedRow();
        if (r < 0) return;
        idSel = (Integer) modelo.getValueAt(r, 0);
        txtNome.setText((String) modelo.getValueAt(r, 1));
        txtDesc.setText((String) modelo.getValueAt(r, 2));
        txtPreco.setText(String.valueOf(modelo.getValueAt(r, 3)));
        txtDur.setText(String.valueOf(modelo.getValueAt(r, 4)));
    }

    // Deixa a tela pronta para começar outro cadastro.
    private void limparForm() {
        txtNome.setText(""); txtDesc.setText(""); txtPreco.setText(""); txtDur.setText("");
        tabela.clearSelection(); idSel = null;
    }

    // Cadastra e recarrega a tabela para mostrar o novo serviço.
    private void incluir() {
        try {
            ctrl.inserir(txtNome.getText(), txtDesc.getText(), txtPreco.getText(), txtDur.getText());
            Msg.info(this, "Serviço cadastrado com sucesso!");
            limparForm(); listar();
        } catch (Exception ex) { Msg.erro(this, ex); }
    }

    // Aplica a alteração ao ID que estiver selecionado.
    private void salvar() {
        try {
            ctrl.atualizar(idSel, txtNome.getText(), txtDesc.getText(), txtPreco.getText(), txtDur.getText());
            Msg.info(this, "Serviço atualizado com sucesso!");
            limparForm(); listar();
        } catch (Exception ex) { Msg.erro(this, ex); }
    }

    // Só exclui depois de o usuário confirmar.
    private void excluir() {
        try {
            if (idSel == null) { ctrl.excluir(null); return; }
            if (!Msg.confirmar(this, "Deseja realmente excluir este serviço?")) return;
            ctrl.excluir(idSel);
            Msg.info(this, "Serviço excluído.");
            limparForm(); listar();
        } catch (Exception ex) { Msg.erro(this, ex); }
    }
}

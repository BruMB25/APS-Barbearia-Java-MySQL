// Monta a aba de clientes e liga os botões aos comandos do controller.
package barbearia.view;

import barbearia.controller.ClienteController;
import barbearia.model.Cliente;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ClientePanel extends JPanel {
    // A tela não fala direto com o MySQL; passa pelo controller.
    private final ClienteController ctrl;
    private final String[] rotulos = {"ID", "Nome", "Telefone", "E-mail"};
    private final String[] colunas = {"id", "nome", "telefone", "email"};
    // A tabela só exibe dados; as células ficam bloqueadas para edição direta.
    private final DefaultTableModel modelo = new DefaultTableModel(rotulos, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(modelo);
    private final JTextField txtBusca = new JTextField(14), txtNome = new JTextField(18),
            txtTel = new JTextField(12), txtEmail = new JTextField(18);
    private final JComboBox<String> cbOrd1 = new JComboBox<>(rotulos), cbOrd2 = new JComboBox<>(rotulos);
    private final JComboBox<String> cbDir1 = new JComboBox<>(new String[]{"Crescente", "Decrescente"}),
            cbDir2 = new JComboBox<>(new String[]{"Crescente", "Decrescente"});
    // Guarda o ID do cliente selecionado para editar ou excluir.
    private Integer idSel = null;

    // Aqui são criados os campos, botões e a organização da aba.
    public ClientePanel(ClienteController ctrl) {
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
        // Quando o usuário escolhe uma linha, preenche os campos do formulário.
        tabela.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) preencherForm(); });
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JButton btIncluir = new JButton("Incluir"), btSalvar = new JButton("Salvar alteração"),
                btExcluir = new JButton("Excluir"), btNovo = new JButton("Limpar formulário");
        JPanel form = Ui.caixa("Dados do cliente");
        form.add(Ui.linha(new JLabel("Nome:"), txtNome, new JLabel("Telefone (DDD+número):"), txtTel,
                new JLabel("E-mail:"), txtEmail));
        form.add(Ui.linha(btIncluir, btSalvar, btExcluir, btNovo));
        add(form, BorderLayout.SOUTH);

        // O botão dispara uma nova consulta quando recebe um clique.
        btBuscar.addActionListener(e -> listar());
        txtBusca.addActionListener(e -> listar());
        btLimparBusca.addActionListener(e -> { txtBusca.setText(""); listar(); });
        btIncluir.addActionListener(e -> incluir());
        btSalvar.addActionListener(e -> salvar());
        btExcluir.addActionListener(e -> excluir());
        btNovo.addActionListener(e -> limparForm());
        listar();
    }

    // Atualiza as linhas da tabela com os resultados do controller.
    public void listar() {
        try {
            List<Cliente> lista = ctrl.listar(txtBusca.getText(), colunas[cbOrd1.getSelectedIndex()],
                    cbDir1.getSelectedIndex() == 0, colunas[cbOrd2.getSelectedIndex()], cbDir2.getSelectedIndex() == 0);
            // Limpa os resultados anteriores antes de mostrar a nova consulta.
            modelo.setRowCount(0);
            for (Cliente c : lista)
                modelo.addRow(new Object[]{c.getId(), c.getNome(), c.getTelefone(), c.getEmail() == null ? "" : c.getEmail()});
            idSel = null;
        } catch (Exception ex) { Msg.erro(this, ex); }
    }

    // Copia os valores da linha selecionada para os campos editáveis.
    private void preencherForm() {
        int r = tabela.getSelectedRow();
        if (r < 0) return;
        idSel = (Integer) modelo.getValueAt(r, 0);
        txtNome.setText((String) modelo.getValueAt(r, 1));
        txtTel.setText((String) modelo.getValueAt(r, 2));
        txtEmail.setText((String) modelo.getValueAt(r, 3));
    }

    // Zera os campos e desfaz a seleção para permitir outro cadastro.
    private void limparForm() {
        txtNome.setText(""); txtTel.setText(""); txtEmail.setText("");
        tabela.clearSelection(); idSel = null;
    }

    // Lê o formulário, valida pelo controller e atualiza a lista.
    private void incluir() {
        try {
            ctrl.inserir(txtNome.getText(), txtTel.getText(), txtEmail.getText());
            Msg.info(this, "Cliente cadastrado com sucesso!");
            limparForm(); listar();
        } catch (Exception ex) { Msg.erro(this, ex); }
    }

    // Usa idSel para alterar o cadastro previamente selecionado.
    private void salvar() {
        try {
            ctrl.atualizar(idSel, txtNome.getText(), txtTel.getText(), txtEmail.getText());
            Msg.info(this, "Cliente atualizado com sucesso!");
            limparForm(); listar();
        } catch (Exception ex) { Msg.erro(this, ex); }
    }

    // Mostra uma confirmação antes de remover o cliente.
    private void excluir() {
        try {
            if (idSel == null) { ctrl.excluir(null); return; }
            if (!Msg.confirmar(this, "Deseja realmente excluir este cliente?")) return;
            ctrl.excluir(idSel);
            Msg.info(this, "Cliente excluído.");
            limparForm(); listar();
        } catch (Exception ex) { Msg.erro(this, ex); }
    }
}

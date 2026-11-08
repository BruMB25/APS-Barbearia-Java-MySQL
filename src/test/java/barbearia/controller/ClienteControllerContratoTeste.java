package barbearia.controller;

import barbearia.dao.DAO;
import barbearia.model.Cliente;
import barbearia.util.DAOException;
import barbearia.util.ValidacaoException;
import java.util.List;

/** Teste simples de contrato: funciona sem instalar ou abrir o MySQL. */
public class ClienteControllerContratoTeste {
    public static void main(String[] args) throws Exception {
        // A implementação em memória substitui o DAO MySQL neste teste.
        DAODeTeste dao = new DAODeTeste();
        ClienteController controller = new ClienteController(dao);

        controller.inserir("Ana Paula", "(19) 99999-9999", "ana@email.com");
        if (dao.ultimoCliente == null || !"19999999999".equals(dao.ultimoCliente.getTelefone())) {
            throw new AssertionError("Cadastro ou normalização de telefone falhou");
        }

        boolean validacaoFuncionou = false;
        try {
            controller.inserir("Ana Paula", "123", "ana@email.com");
        } catch (ValidacaoException esperado) {
            validacaoFuncionou = true;
        }
        if (!validacaoFuncionou) {
            throw new AssertionError("Telefone inválido deveria ser recusado pelo Controller");
        }

        if (dao.quantidadeInsercoes != 1) {
            throw new AssertionError("Dados inválidos não deveriam chegar ao DAO");
        }

        System.out.println("OK: controller validado com uma implementação de DAO sem MySQL.");
    }

    // Implementação alternativa do contrato, usada só para testar as regras do Controller.
    private static class DAODeTeste implements DAO<Cliente> {
        private Cliente ultimoCliente;
        private int quantidadeInsercoes;

        @Override
        public void inserir(Cliente cliente) {
            ultimoCliente = cliente;
            quantidadeInsercoes++;
        }

        @Override
        public void atualizar(Cliente cliente) {
            ultimoCliente = cliente;
        }

        @Override
        public void excluir(int id) {
            ultimoCliente = null;
        }

        @Override
        public List<Cliente> listar(String busca, String status, String c1, boolean a1,
                                   String c2, boolean a2) throws DAOException {
            return ultimoCliente == null ? List.of() : List.of(ultimoCliente);
        }
    }
}

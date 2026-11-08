package barbearia.controller;

import barbearia.dao.DAO;
import barbearia.model.Agendamento;
import barbearia.model.Cliente;
import barbearia.model.Servico;
import barbearia.util.DAOException;
import barbearia.util.ValidacaoException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Testa as três telas lógicas usando apenas a interface DAO, sem MySQL. */
public class ControladoresDAOUnicoTeste {
    private static class MemoriaDAO<T> implements DAO<T> {
        T ultimo;
        int insercoes;
        int exclusoes;
        String ultimoStatus;

        @Override public void inserir(T objeto) { ultimo = objeto; insercoes++; }
        @Override public void atualizar(T objeto) { ultimo = objeto; }
        @Override public void excluir(int id) { ultimo = null; exclusoes++; }
        @Override public List<T> listar(String busca, String status, String c1, boolean asc1,
                                         String c2, boolean asc2) {
            ultimoStatus = status;
            return ultimo == null ? List.of() : List.of(ultimo);
        }
    }

    private static class AgendamentoEmMemoria extends MemoriaDAO<Agendamento> {
        boolean conflito;
        int consultasDeConflito;
        @Override public boolean existeConflito(LocalDateTime inicio, int duracaoMin, int ignorarId) {
            consultasDeConflito++;
            return conflito;
        }
    }

    private static void verificar(boolean expressao, String mensagem) {
        if (!expressao) throw new AssertionError(mensagem);
    }

    public static void main(String[] args) throws Exception {
        MemoriaDAO<Cliente> clientesDAO = new MemoriaDAO<>();
        ClienteController clienteCtrl = new ClienteController(clientesDAO);
        clienteCtrl.inserir("Ana Paula", "(19) 99999-9999", "ana@email.com");
        verificar("19999999999".equals(clientesDAO.ultimo.getTelefone()), "Telefone não foi normalizado");
        verificar(clienteCtrl.listar("Ana", "nome", true, "id", true).size() == 1, "Cliente não foi listado");
        verificar("TODOS".equals(clientesDAO.ultimoStatus), "Status de clientes não foi passado");
        boolean invalido = false;
        try { clienteCtrl.inserir("Ana Paula", "123", "ana@email.com"); }
        catch (ValidacaoException esperado) { invalido = true; }
        verificar(invalido && clientesDAO.insercoes == 1, "Telefone inválido passou pela validação");
        boolean operacaoNaoAplicavel = false;
        try { clientesDAO.existeConflito(LocalDateTime.now(), 30, 0); }
        catch (DAOException esperado) { operacaoNaoAplicavel = true; }
        verificar(operacaoNaoAplicavel, "DAO comum não deveria aceitar conflito de horário");

        MemoriaDAO<Servico> servicosDAO = new MemoriaDAO<>();
        ServicoController servicoCtrl = new ServicoController(servicosDAO);
        servicoCtrl.inserir("Corte", "Corte masculino", "45,50", "30");
        verificar(servicosDAO.ultimo.getPreco().compareTo(new BigDecimal("45.50")) == 0, "Preço errado");
        verificar(servicoCtrl.listar("Corte", "nome", true, "id", true).size() == 1, "Serviço não listado");
        invalido = false;
        try { servicoCtrl.inserir("Corte", "", "-1", "30"); }
        catch (ValidacaoException esperado) { invalido = true; }
        verificar(invalido && servicosDAO.insercoes == 1, "Preço negativo passou pela validação");

        AgendamentoEmMemoria agendamentosDAO = new AgendamentoEmMemoria();
        AgendamentoController agCtrl = new AgendamentoController(agendamentosDAO);
        String data = LocalDateTime.now().plusDays(3).format(AgendamentoController.FORMATO);
        agCtrl.inserir(clientesDAO.ultimo, List.of(servicosDAO.ultimo), data, "AGENDADO", "Teste");
        verificar(agendamentosDAO.insercoes == 1, "Agendamento não inserido");
        verificar(agendamentosDAO.consultasDeConflito == 1, "Conflito não foi consultado");
        verificar(agCtrl.listar("Ana", "AGENDADO", "data", true, "id", true).size() == 1, "Agendamento não listado");
        verificar("AGENDADO".equals(agendamentosDAO.ultimoStatus), "Filtro de status perdido");
        agendamentosDAO.conflito = true;
        invalido = false;
        try { agCtrl.inserir(clientesDAO.ultimo, List.of(servicosDAO.ultimo), data, "AGENDADO", "Outro"); }
        catch (ValidacaoException esperado) { invalido = true; }
        verificar(invalido && agendamentosDAO.insercoes == 1, "Conflito de horário não foi barrado");
        int antes = agendamentosDAO.consultasDeConflito;
        agCtrl.inserir(clientesDAO.ultimo, List.of(servicosDAO.ultimo), data, "CANCELADO", "Sem bloqueio");
        verificar(agendamentosDAO.consultasDeConflito == antes, "Cancelado não deveria bloquear horário");

        System.out.println("OK: os 3 Controllers usam DAO<T>, com CRUD, filtros e validações preservados.");
    }
}

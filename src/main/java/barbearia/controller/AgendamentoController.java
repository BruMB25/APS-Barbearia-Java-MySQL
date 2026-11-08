// Valida o agendamento e evita que dois horários se sobreponham.
package barbearia.controller;

import barbearia.dao.DAO;
import barbearia.model.*;
import barbearia.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public class AgendamentoController {
    public static final DateTimeFormatter FORMATO =
            // Usa formato dia/mês/ano e checagem estrita da data.
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);
    // O Controller conversa com o contrato, e não depende dos detalhes do SQL.
    private final DAO<Agendamento> dao;

    // Recebe a implementação por fora: o Controller não sabe qual DAO concreto é usado.
    public AgendamentoController(DAO<Agendamento> dao) {
        this.dao = java.util.Objects.requireNonNull(dao, "DAO não pode ser nulo");
    }

    // Monta o objeto e centraliza as regras antes de salvar.
    private Agendamento montar(int id, Cliente c, List<Servico> servicos, String data, String status, String obs,
                               boolean novo) throws ValidacaoException, DAOException {
        // Sem cliente não existe agendamento válido.
        if (c == null) throw new ValidacaoException("Selecione um cliente (cadastre um na aba Clientes, se necessário).");
        // É necessário marcar ao menos um serviço.
        if (servicos == null || servicos.isEmpty())
            throw new ValidacaoException("Selecione pelo menos um serviço (use Ctrl para escolher vários).");
        LocalDateTime dh;
        try {
            // Converte a data digitada em um valor que o Java consegue comparar.
            dh = LocalDateTime.parse(data == null ? "" : data.trim(), FORMATO);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("Data/hora inválida. Use o formato dd/MM/aaaa HH:mm (ex.: 15/10/2026 14:30).");
        }
        obs = obs == null ? "" : obs.trim();
        if (obs.length() > 200) throw new ValidacaoException("A observação deve ter no máximo 200 caracteres.");
        // A verificação de data passada vale para novos agendamentos AGENDADOS.
        if (novo && "AGENDADO".equals(status) && dh.isBefore(LocalDateTime.now()))
            throw new ValidacaoException("Não é possível agendar em data/hora passada.");
        Agendamento a = new Agendamento();
        // Cria um objeto independente com os valores recebidos da tela.
        a.setId(id); a.setCliente(c); a.setServicos(new ArrayList<>(servicos)); a.setDataHora(dh);
        a.setStatus(status); a.setObservacao(obs.isEmpty() ? null : obs);
        // A duração total é a soma dos serviços escolhidos
        // Cancelados não ocupam horário. O próprio registro editado fica fora da comparação.
        if (!"CANCELADO".equals(status) && dao.existeConflito(dh, a.getDuracaoTotal(), id))
            throw new ValidacaoException("Conflito de horário: já existe outro agendamento nesse período.");
        return a;
    }

    // Novo registro: gera o objeto e envia para gravação.
    public void inserir(Cliente c, List<Servico> servicos, String data, String status, String obs)
            throws ValidacaoException, DAOException {
        dao.inserir(montar(0, c, servicos, data, status, obs, true));
    }

    // Edição: aproveita o mesmo metodo de validação.
    public void atualizar(Integer id, Cliente c, List<Servico> servicos, String data, String status, String obs)
            throws ValidacaoException, DAOException {
        if (id == null) throw new ValidacaoException("Selecione um agendamento na tabela para alterar.");
        dao.atualizar(montar(id, c, servicos, data, status, obs, false));
    }

    // Só deixa excluir quando existe um item selecionado.
    public void excluir(Integer id) throws ValidacaoException, DAOException {
        if (id == null) throw new ValidacaoException("Selecione um agendamento na tabela para excluir.");
        dao.excluir(id);
    }

    // Aplica os filtros e solicita os dados completos ao DAO.
    public List<Agendamento> listar(String busca, String status, String c1, boolean a1, String c2, boolean a2)
            throws DAOException {
        return dao.listar(busca == null ? "" : busca.trim(), status, c1, a1, c2, a2);
    }
}

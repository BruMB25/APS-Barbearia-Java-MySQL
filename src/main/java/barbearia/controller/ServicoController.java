// Confere preço, duração e nome antes de enviar o serviço ao banco.
package barbearia.controller;

import barbearia.dao.DAO;
import barbearia.model.Servico;
import barbearia.util.*;
import java.math.BigDecimal;
import java.util.List;

public class ServicoController {
    // O Controller conversa com o contrato, e não depende dos detalhes do SQL.
    private final DAO<Servico> dao;

    // Recebe a implementação por fora: o Controller não sabe qual DAO concreto é usado.
    public ServicoController(DAO<Servico> dao) {
        this.dao = java.util.Objects.requireNonNull(dao, "DAO não pode ser nulo");
    }

    // O mesmo método prepara tanto um novo serviço quanto uma alteração.
    private Servico montar(int id, String nome, String desc, String preco, String duracao) throws ValidacaoException {
        nome = nome == null ? "" : nome.trim();
        desc = desc == null ? "" : desc.trim();
        if (nome.length() < 2) throw new ValidacaoException("O nome do serviço deve ter pelo menos 2 caracteres.");
        if (nome.length() > 80) throw new ValidacaoException("O nome do serviço deve ter no máximo 80 caracteres.");
        if (desc.length() > 200) throw new ValidacaoException("A descrição deve ter no máximo 200 caracteres.");
        BigDecimal p;
        try {
            // Aceita vírgula decimal e converte o valor para BigDecimal.
            p = new BigDecimal(preco.trim().replace(",", "."));
        } catch (Exception e) {
            throw new ValidacaoException("Preço inválido. Use apenas números (ex.: 45,90).");
        }
        // Preço zero ou negativo não é permitido.
        if (p.signum() <= 0) throw new ValidacaoException("O preço deve ser maior que zero.");
        if (p.compareTo(new BigDecimal("99999.99")) > 0) throw new ValidacaoException("Preço muito alto.");
        int d;
        try {
            // A duração precisa ser um número inteiro de minutos.
            d = Integer.parseInt(duracao.trim());
        } catch (Exception e) {
            throw new ValidacaoException("Duração inválida. Informe os minutos como número inteiro.");
        }
        // Define uma faixa razoável de duração para o cadastro.
        if (d < 5 || d > 480) throw new ValidacaoException("A duração deve estar entre 5 e 480 minutos.");
        // Arredonda o preço para duas casas antes de montar o objeto.
        return new Servico(id, nome, desc.isEmpty() ? null : desc, p.setScale(2, java.math.RoundingMode.HALF_UP), d);
    }

    // Chama o DAO apenas se todos os dados passaram pela validação.
    public void inserir(String nome, String desc, String preco, String dur) throws ValidacaoException, DAOException {
        dao.inserir(montar(0, nome, desc, preco, dur));
    }

    // Para alterar, o ID selecionado precisa existir na tela.
    public void atualizar(Integer id, String nome, String desc, String preco, String dur)
            throws ValidacaoException, DAOException {
        if (id == null) throw new ValidacaoException("Selecione um serviço na tabela para alterar.");
        dao.atualizar(montar(id, nome, desc, preco, dur));
    }

    // A chave escolhida identifica qual serviço será excluído.
    public void excluir(Integer id) throws ValidacaoException, DAOException {
        if (id == null) throw new ValidacaoException("Selecione um serviço na tabela para excluir.");
        dao.excluir(id);
    }

    // O filtro e a ordenação passam pelo controller até o DAO.
    public List<Servico> listar(String busca, String c1, boolean a1, String c2, boolean a2) throws DAOException {
        return dao.listar(busca == null ? "" : busca.trim(), "TODOS", c1, a1, c2, a2);
    }
}

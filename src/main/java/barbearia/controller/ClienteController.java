// Antes de salvar um cliente, confere se os campos fazem sentido.
package barbearia.controller;

import barbearia.dao.DAO;
import barbearia.model.Cliente;
import barbearia.util.*;
import java.util.List;

/** Controller: valida as entradas e delega a persistência ao DAO. */
public class ClienteController {
    // O Controller conversa com o contrato, e não depende dos detalhes do SQL.
    private final DAO<Cliente> dao;

    // Recebe a implementação por fora: o Controller não sabe qual DAO concreto é usado.
    public ClienteController(DAO<Cliente> dao) {
        this.dao = java.util.Objects.requireNonNull(dao, "DAO não pode ser nulo");
    }

    // Concentra validações para cadastro e edição seguirem as mesmas regras.
    private Cliente montar(int id, String nome, String tel, String email) throws ValidacaoException {
        nome = nome == null ? "" : nome.trim();
        // Remove parênteses, espaços e traços do telefone antes de validar.
        String digitos = tel == null ? "" : tel.replaceAll("\\D", "");
        email = email == null ? "" : email.trim();
        // Não basta estar preenchido: existe um tamanho mínimo e máximo.
        if (nome.length() < 3) throw new ValidacaoException("O nome deve ter pelo menos 3 caracteres.");
        if (nome.length() > 100) throw new ValidacaoException("O nome deve ter no máximo 100 caracteres.");
        if (digitos.length() < 10 || digitos.length() > 11)
            throw new ValidacaoException("Telefone inválido. Informe DDD + número (10 ou 11 dígitos).");
        // E-mail é opcional, mas precisa ter formato aceitável se informado.
        if (!email.isEmpty() && !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"))
            throw new ValidacaoException("E-mail inválido.");
        return new Cliente(id, nome, digitos, email.isEmpty() ? null : email);
    }

    // Depois de validar, o controller entrega o objeto ao DAO.
    public void inserir(String nome, String tel, String email) throws ValidacaoException, DAOException {
        dao.inserir(montar(0, nome, tel, email));
    }

    // Editar exige que o usuário tenha escolhido um registro.
    public void atualizar(Integer id, String nome, String tel, String email) throws ValidacaoException, DAOException {
        if (id == null) throw new ValidacaoException("Selecione um cliente na tabela para alterar.");
        dao.atualizar(montar(id, nome, tel, email));
    }

    // Exclusão sem seleção é barrada antes de acessar o banco.
    public void excluir(Integer id) throws ValidacaoException, DAOException {
        if (id == null) throw new ValidacaoException("Selecione um cliente na tabela para excluir.");
        dao.excluir(id);
    }

    // Repassa a busca e as duas ordenações para a consulta SQL.
    public List<Cliente> listar(String busca, String c1, boolean a1, String c2, boolean a2) throws DAOException {
        return dao.listar(busca == null ? "" : busca.trim(), "TODOS", c1, a1, c2, a2);
    }
}

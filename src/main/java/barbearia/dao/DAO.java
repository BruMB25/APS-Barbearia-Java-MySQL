package barbearia.dao;

import barbearia.util.DAOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato usado pelos três Controllers.
 * O T vira Cliente, Servico ou Agendamento conforme o módulo.
 */
public interface DAO<T> {
    // Todo cadastro oferece as operações básicas do CRUD.
    void inserir(T objeto) throws DAOException;
    void atualizar(T objeto) throws DAOException;
    void excluir(int id) throws DAOException;

    /**
     * Lista e filtra os registros. Nos clientes e serviços, o parâmetro status
     * não é utilizado; em agendamentos, ele permite filtrar a situação.
     */
    List<T> listar(String busca, String status, String coluna1, boolean crescente1,
                   String coluna2, boolean crescente2) throws DAOException;

    /**
     * Regra específica de agendamentos. Só o AgendamentoDAO implementa.
     * A implementação padrão evita fingir que outras entidades têm horários.
     */
    default boolean existeConflito(LocalDateTime inicio, int duracaoMin, int ignorarId)
            throws DAOException {
        throw new DAOException("Verificação de conflito só existe no DAO de agendamentos.");
    }
}

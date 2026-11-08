// Exceção usada quando alguma operação no banco falha.
package barbearia.util;

/** Erro de acesso ao banco de dados (mensagem já amigável para o usuário). */
public class DAOException extends Exception {
    // Permite mostrar apenas uma mensagem, ou a mensagem junto da causa original.
    public DAOException(String msg) { super(msg); }
    public DAOException(String msg, Throwable causa) { super(msg, causa); }
}

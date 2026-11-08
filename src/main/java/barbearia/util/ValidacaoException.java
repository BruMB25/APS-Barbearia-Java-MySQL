// Exceção usada para avisar que o preenchimento de algum campo está errado.
package barbearia.util;

/** Erro de validação das entradas do usuário. */
public class ValidacaoException extends Exception {
    // É capturada na interface para avisar o usuário sem encerrar o programa.
    public ValidacaoException(String msg) { super(msg); }
}

// Monta a ordenação de forma controlada, sem aceitar nomes de colunas livres do usuário.
package barbearia.util;

import java.util.Map;

/** Monta a cláusula ORDER BY com até 2 critérios, usando lista segura de colunas (evita SQL Injection). */
public class Ordenacao {
    // Recebe duas escolhas da interface e devolve um ORDER BY válido.
    public static String clausula(Map<String, String> permitidas, String c1, boolean asc1,
                                  String c2, boolean asc2) {
        // Busca a coluna em um mapa permitido; assim, texto arbitrário não entra no SQL.
        String col1 = permitidas.getOrDefault(c1, permitidas.values().iterator().next());
        String col2 = permitidas.getOrDefault(c2, col1);
        // ASC ordena crescente e DESC decrescente; o segundo critério desempata.
        return " ORDER BY " + col1 + (asc1 ? " ASC" : " DESC")
                + ", " + col2 + (asc2 ? " ASC" : " DESC");
    }
}

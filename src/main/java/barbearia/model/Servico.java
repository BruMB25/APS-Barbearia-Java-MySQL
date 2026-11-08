// Representa um serviço da barbearia, com preço e tempo de execução.
package barbearia.model;

import java.math.BigDecimal;

/** Entidade Servico (tabela servicos). */
public class Servico {
    private int id;
    private String nome;
    private String descricao;
    // BigDecimal é usado para dinheiro, evitando imprecisões comuns com double.
    private BigDecimal preco;
    private int duracaoMin;

    public Servico() {}
    // Recebe os dados do serviço, inclusive duração em minutos.
    public Servico(int id, String nome, String descricao, BigDecimal preco, int duracaoMin) {
        this.id = id; this.nome = nome; this.descricao = descricao;
        this.preco = preco; this.duracaoMin = duracaoMin;
    }
    // Getters e setters expõem os dados sem tornar os atributos públicos.
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public int getDuracaoMin() { return duracaoMin; }
    public void setDuracaoMin(int duracaoMin) { this.duracaoMin = duracaoMin; }
    // Mostra nome e valor no seletor de serviços da tela de agendamento.
    @Override public String toString() { return nome + " (R$ " + preco + ")"; }
}

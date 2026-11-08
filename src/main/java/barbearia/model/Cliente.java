// Guarda os dados de um cliente enquanto o programa está rodando.
package barbearia.model;

/** Entidade Cliente (tabela clientes). */
public class Cliente {
    // O ID é a chave do registro no MySQL. Os demais campos guardam o cadastro.
    private int id;
    private String nome;
    private String telefone;
    private String email;

    // Construtor vazio: permite criar o objeto e preencher os dados depois.
    public Cliente() {}
    // Este construtor já recebe todas as informações de uma vez.
    public Cliente(int id, String nome, String telefone, String email) {
        this.id = id; this.nome = nome; this.telefone = telefone; this.email = email;
    }
    // Getters leem os campos; setters permitem atualizar o objeto em memória.
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    // O JComboBox usa este texto para mostrar o nome do cliente.
    @Override public String toString() { return nome; }
}

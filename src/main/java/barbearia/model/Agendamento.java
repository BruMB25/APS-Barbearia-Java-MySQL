// Junta um cliente, os serviços escolhidos e o horário marcado.
package barbearia.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Entidade Agendamento: 1 cliente e N serviços (via tabela agendamento_servico). */
public class Agendamento {
    private int id;
    // Um agendamento pertence a um único cliente.
    private Cliente cliente;
    // Um agendamento pode ter vários serviços; no banco isso vira a tabela associativa.
    private List<Servico> servicos = new ArrayList<>();
    // LocalDateTime guarda data e hora sem formatação de texto.
    private LocalDateTime dataHora;
    private String status;
    private String observacao;

    public Agendamento() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public List<Servico> getServicos() { return servicos; }
    public void setServicos(List<Servico> servicos) { this.servicos = servicos; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    /** Soma dos preços cobrados dos serviços. */
    // O valor exibido é calculado a partir dos preços carregados no objeto.
    public BigDecimal getTotal() {
        return servicos.stream().map(Servico::getPreco).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    /** Soma das durações (minutos). */
    // Soma os minutos para saber quanto tempo o atendimento vai ocupar.
    public int getDuracaoTotal() { return servicos.stream().mapToInt(Servico::getDuracaoMin).sum(); }
    // Junta os nomes com vírgulas para caberem na coluna da tabela.
    public String getServicosTexto() {
        return servicos.stream().map(Servico::getNome).collect(Collectors.joining(", "));
    }
}

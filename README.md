# APS — Sistema de Barbearia

Projeto acadêmico em Java Swing, JDBC e MySQL para gerenciamento de clientes, serviços e agendamentos.

## Integrantes

| Nome | RA |
| --- | --- |
| BRUNO MILANI BARRETO | R819957 |
| GUILHERME HENRIQUE JERÔNIMO DE LIMA | R677044 |
| MATHEUS HENRIQUE GUILLEN DA SILVA ARAÚJO | R820424 |
| MATHEUS VICTOR DOS SANTOS | R835CJ5 |
| PEDRO HENRIQUE GOMES DE LIMA | R8501E9 |
| YASMIN GOUVÊA DA SILVA | R857573 |

## Funcionalidades

- Cadastro, edição, exclusão, busca e ordenação de clientes e serviços.
- Agendamentos com vários serviços, status e observações.
- Validação de telefone, valores e conflitos de horário.
- Transações para gravar agendamentos e serviços associados.
- Criação inicial do banco e das tabelas, caso não existam.

## Requisitos

- JDK 17 ou superior.
- Maven instalado, ou uma IDE com suporte a Maven, como IntelliJ IDEA.
- MySQL em execução; o projeto utiliza MySQL Connector/J 8.4.0.
- Ambiente gráfico para abrir a interface Swing.

## Como executar

1. Abra a pasta que contém `pom.xml` como projeto Maven.
2. Inicie o MySQL.
3. Configure as variáveis de ambiente no terminal ou na configuração de execução da IDE:

| Variável | Finalidade | Padrão |
| --- | --- | --- |
| `DB_URL` | Endereço JDBC do banco | MySQL local, porta 3306, banco `barbearia` |
| `DB_USER` | Usuário MySQL | `root` |
| `DB_PASS` | Senha do usuário MySQL | Vazio; configure sua senha local |

Exemplo no PowerShell, na mesma janela em que executará o Maven:

```powershell
$env:DB_USER = "seu_usuario_mysql"
$env:DB_PASS = "sua_senha_local"
mvn compile exec:java
```

A URL padrão completa está em `src/main/java/barbearia/dao/Conexao.java` e é destinada à demonstração local. Para outro servidor, configure `DB_URL` com as opções de conexão adequadas.

Na IDE, configure as mesmas variáveis e execute `barbearia.Main`. O projeto não carrega arquivos `.env` automaticamente.

O inicializador precisa de permissão para criar o banco e as tabelas na primeira execução. Como alternativa, um administrador pode executar `sql/schema.sql` antes. O inicializador não apaga registros existentes nem migra tabelas antigas.

## Dados de demonstração

`sql/dados_exemplo.sql` é opcional e deve ser executado apenas uma vez em um banco novo, sem cadastros. Contém nomes e contatos apresentados no arquivo como exemplos. As datas são fixas; crie novos agendamentos com datas futuras para testar os cadastros atuais.

## Organização do código

```text
src/main/java/barbearia/
  Main.java       Entrada da aplicação
  model/          Entidades Cliente, Servico e Agendamento
  dao/            Acesso JDBC, interface DAO e inicialização do banco
  controller/     Validações e coordenação das operações
  view/           Telas Swing
  util/           Exceções e ordenação
src/main/resources/db/tabelas.sql   Tabelas usadas pelo inicializador
src/test/java/                     Testes de contrato sem MySQL
sql/                               Scripts para execução manual
```

O banco relaciona clientes a agendamentos e utiliza `agendamento_servico` para a relação de vários serviços por agendamento. O preço cobrado é preservado no vínculo do agendamento.

## Testes sem MySQL

Os testes existentes são programas Java com `main`, sem JUnit. Execute explicitamente:

```powershell
mvn test-compile
java -cp "target/classes;target/test-classes" barbearia.controller.ClienteControllerContratoTeste
java -cp "target/classes;target/test-classes" barbearia.controller.ControladoresDAOUnicoTeste
```

Em Linux/macOS, substitua `;` por `:` no classpath. `mvn test` sozinho não executa esses programas.

## Credenciais e arquivos locais

A senha padrão embutida na versão original foi removida. Informe a senha por `DB_PASS` e não envie credenciais ao GitHub. Arquivos compilados, configurações pessoais da IDE e arquivos `.env` estão excluídos pelo `.gitignore`.

## Uso acadêmico

Este repositório permite consultar os códigos pelo navegador e baixar o projeto. Não há licença de redistribuição definida nesta preparação.

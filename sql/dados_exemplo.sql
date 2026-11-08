-- OPCIONAL: dados para a primeira demonstracao.
-- Execute apenas UMA VEZ, em banco novo e sem cadastros.
-- O projeto nao depende destes exemplos para funcionar.
USE barbearia;

-- Dados de exemplo
-- Exemplos para facilitar os testes da apresentação (não representam produção).
INSERT INTO clientes (nome, telefone, email) VALUES
 ('Carlos Silva','19991110001','carlos@email.com'),
 ('João Pereira','19991110002','joao@email.com'),
 ('Marcos Souza','19991110003',NULL);
INSERT INTO servicos (nome, descricao, preco, duracao_min) VALUES
 ('Corte Masculino','Corte na tesoura ou máquina',40.00,30),
 ('Barba','Barba com toalha quente',30.00,20),
 ('Sobrancelha','Design de sobrancelha',15.00,10);
INSERT INTO agendamentos (id_cliente, data_hora, status, observacao) VALUES
 (1,'2026-10-10 09:00:00','AGENDADO',NULL),
 (2,'2026-10-10 10:00:00','AGENDADO','Prefere máquina 2'),
 (3,'2026-10-09 15:00:00','CONCLUIDO',NULL);
INSERT INTO agendamento_servico (id_agendamento, id_servico, preco_cobrado) VALUES
 (1,1,40.00),(1,2,30.00),(1,3,15.00),   -- Agendamento 1: Corte + Barba + Sobrancelha
 (2,1,40.00),                           -- Agendamento 2: Corte
 (3,2,30.00);                           -- Agendamento 3: Barba

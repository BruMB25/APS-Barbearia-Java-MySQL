-- Execute no Workbench apenas se quiser configurar o banco manualmente.
-- O Java ja realiza este processo ao abrir Main.java.
-- Este script NAO exclui tabelas ou registros existentes.

CREATE DATABASE IF NOT EXISTS barbearia CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE barbearia;

-- Entidade: Cliente
CREATE TABLE IF NOT EXISTS clientes (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(100) NOT NULL,
    telefone VARCHAR(15)  NOT NULL UNIQUE,
    email    VARCHAR(100)
) ENGINE=InnoDB;

-- Entidade: Serviço
CREATE TABLE IF NOT EXISTS servicos (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(80)  NOT NULL UNIQUE,
    descricao   VARCHAR(200),
    preco       DECIMAL(8,2) NOT NULL CHECK (preco > 0),
    duracao_min INT          NOT NULL CHECK (duracao_min > 0)
) ENGINE=InnoDB;

-- Entidade: Agendamento (1 cliente : N agendamentos)
CREATE TABLE IF NOT EXISTS agendamentos (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT      NOT NULL,
    data_hora  DATETIME NOT NULL,
    status     ENUM('AGENDADO','CONCLUIDO','CANCELADO') NOT NULL DEFAULT 'AGENDADO',
    observacao VARCHAR(200),
    CONSTRAINT fk_ag_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Tabela relacional: AGENDAMENTO N : N SERVIÇO
-- PK composta (id_agendamento, id_servico): o mesmo serviço não repete no mesmo agendamento.
-- preco_cobrado guarda o preço no momento do agendamento (histórico não muda se o preço mudar).
-- A chave composta evita adicionar o mesmo serviço duas vezes no mesmo agendamento.
CREATE TABLE IF NOT EXISTS agendamento_servico (
    id_agendamento INT          NOT NULL,
    id_servico     INT          NOT NULL,
    preco_cobrado  DECIMAL(8,2) NOT NULL,
    PRIMARY KEY (id_agendamento, id_servico),
    CONSTRAINT fk_as_agendamento FOREIGN KEY (id_agendamento) REFERENCES agendamentos(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_as_servico FOREIGN KEY (id_servico) REFERENCES servicos(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE DATABASE IF NOT EXISTS pcbuilder
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pcbuilder;

CREATE TABLE IF NOT EXISTS usuario (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    login VARCHAR(80) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL DEFAULT 'USUARIO',
    CHECK (perfil IN ('ADMIN', 'USUARIO'))
);

CREATE TABLE IF NOT EXISTS processador (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    socket VARCHAR(30) NOT NULL,
    consumo INT NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    fabricante VARCHAR(80) NOT NULL,
    nucleos INT NOT NULL,
    threads INT NOT NULL,
    desempenho DECIMAL(10, 2) NOT NULL,
    CHECK (consumo >= 0 AND preco >= 0 AND nucleos > 0 AND threads > 0 AND desempenho > 0)
);

CREATE TABLE IF NOT EXISTS placa_mae (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    fabricante VARCHAR(80) NOT NULL,
    socket VARCHAR(30) NOT NULL,
    tipo_memoria VARCHAR(20) NOT NULL,
    consumo INT NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    CHECK (consumo >= 0 AND preco >= 0)
);

CREATE TABLE IF NOT EXISTS placa_video (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    fabricante VARCHAR(80) NOT NULL,
    memoria INT NOT NULL,
    consumo INT NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    desempenho INT NOT NULL,
    CHECK (memoria > 0 AND consumo >= 0 AND preco >= 0 AND desempenho > 0)
);

CREATE TABLE IF NOT EXISTS memoria (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    capacidade INT NOT NULL,
    frequencia INT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    CHECK (capacidade > 0 AND frequencia > 0 AND preco >= 0)
);

CREATE TABLE IF NOT EXISTS ssd (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    capacidade INT NOT NULL,
    leitura INT NOT NULL,
    escrita INT NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    CHECK (capacidade > 0 AND leitura > 0 AND escrita > 0 AND preco >= 0)
);

CREATE TABLE IF NOT EXISTS fonte (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    potencia INT NOT NULL,
    certificacao VARCHAR(60) NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    CHECK (potencia > 0 AND preco >= 0)
);

CREATE TABLE IF NOT EXISTS jogo (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL UNIQUE,
    exigencia_cpu INT NOT NULL,
    exigencia_gpu INT NOT NULL,
    genero VARCHAR(60) NULL,
    descricao VARCHAR(500) NULL,
    imagem_url VARCHAR(1000) NULL,
    CHECK (exigencia_cpu > 0 AND exigencia_gpu > 0)
);

CREATE TABLE IF NOT EXISTS build (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    usuario_id INT NOT NULL,
    processador_id INT NOT NULL,
    placa_mae_id INT NOT NULL,
    placa_video_id INT NOT NULL,
    memoria_id INT NOT NULL,
    ssd_id INT NOT NULL,
    fonte_id INT NOT NULL,
    favorita BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_build_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_build_processador FOREIGN KEY (processador_id) REFERENCES processador(id),
    CONSTRAINT fk_build_placa_mae FOREIGN KEY (placa_mae_id) REFERENCES placa_mae(id),
    CONSTRAINT fk_build_placa_video FOREIGN KEY (placa_video_id) REFERENCES placa_video(id),
    CONSTRAINT fk_build_memoria FOREIGN KEY (memoria_id) REFERENCES memoria(id),
    CONSTRAINT fk_build_ssd FOREIGN KEY (ssd_id) REFERENCES ssd(id),
    CONSTRAINT fk_build_fonte FOREIGN KEY (fonte_id) REFERENCES fonte(id)
);

CREATE TABLE IF NOT EXISTS benchmark_fps (
    id INT PRIMARY KEY AUTO_INCREMENT,
    jogo_id INT NOT NULL,
    processador_id INT NOT NULL,
    placa_video_id INT NOT NULL,
    resolucao VARCHAR(20) NOT NULL,
    qualidade VARCHAR(20) NOT NULL,
    fps_medio INT NOT NULL,
    fps_1_low INT NULL,
    fonte VARCHAR(255) NOT NULL,
    observacoes VARCHAR(255) NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_benchmark_jogo FOREIGN KEY (jogo_id) REFERENCES jogo(id),
    CONSTRAINT fk_benchmark_processador FOREIGN KEY (processador_id) REFERENCES processador(id),
    CONSTRAINT fk_benchmark_placa_video FOREIGN KEY (placa_video_id) REFERENCES placa_video(id),
    INDEX idx_benchmark_recomendacao (jogo_id, resolucao, fps_medio),
    CHECK (fps_medio > 0),
    CHECK (fps_1_low IS NULL OR (fps_1_low > 0 AND fps_1_low <= fps_medio))
);

CREATE TABLE IF NOT EXISTS componente_importado (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tipo VARCHAR(30) NOT NULL,
    componente_id INT NOT NULL,
    origem VARCHAR(40) NOT NULL,
    id_externo VARCHAR(120) NOT NULL,
    nome_original VARCHAR(255) NOT NULL,
    importado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_componente_importado UNIQUE (tipo, origem, id_externo)
);

INSERT IGNORE INTO jogo (nome, exigencia_cpu, exigencia_gpu)
VALUES
    ('Counter-Strike 2', 70, 65),
    ('Cyberpunk 2077', 85, 95),
    ('Fortnite', 60, 60);

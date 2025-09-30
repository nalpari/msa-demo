-- R2DBC H2 Database Schema
-- R2DBC는 자동 DDL 생성이 없으므로 수동 작성 필요

DROP TABLE IF EXISTS tests;

CREATE TABLE tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
-- BP Master 기본 테이블 생성 스크립트
-- PostgreSQL 기준
-- 실행: psql -h localhost -U whaleerp -d whaleerpdb -f create_table.sql

-- 테이블이 이미 존재하지 않을 때만 생성
CREATE TABLE IF NOT EXISTS bp_master (
    bp_id BIGSERIAL PRIMARY KEY,
    bp_code VARCHAR(50) UNIQUE NOT NULL,
    bp_name VARCHAR(200) NOT NULL,
    bp_type VARCHAR(20) NOT NULL,
    business_number VARCHAR(20),
    ceo_name VARCHAR(100),
    business_condition VARCHAR(100),
    business_item VARCHAR(200),
    phone VARCHAR(20),
    fax VARCHAR(20),
    email VARCHAR(100),
    homepage VARCHAR(200),
    address VARCHAR(200),
    address_detail VARCHAR(100),
    postal_code VARCHAR(10),
    tax_type VARCHAR(20),
    payment_terms VARCHAR(100),
    credit_limit DECIMAL(15,2),
    currency VARCHAR(3) DEFAULT 'KRW',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50)
);

-- 인덱스 생성 (이미 존재하지 않을 때만)
CREATE INDEX IF NOT EXISTS idx_bp_master_code ON bp_master(bp_code);
CREATE INDEX IF NOT EXISTS idx_bp_master_type ON bp_master(bp_type);
CREATE INDEX IF NOT EXISTS idx_bp_master_status ON bp_master(status);
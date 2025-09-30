-- 실제 데이터베이스 테이블 확인용 SQL
-- psql -h localhost -U whaleerp -d whaleerpdb -f check_tables.sql

-- 모든 테이블 목록 조회
SELECT
    schemaname as schema,
    tablename as table_name,
    tableowner as owner
FROM pg_tables
WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
ORDER BY schemaname, tablename;

-- bp 관련 테이블만 조회
SELECT
    table_name,
    table_type
FROM information_schema.tables
WHERE table_schema = 'public'
AND (table_name LIKE 'bp_%' OR table_name LIKE '%business%' OR table_name LIKE '%partner%')
ORDER BY table_name;

-- 특정 테이블의 컬럼 정보 조회 (예: bp_master가 있다면)
SELECT
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
AND table_name IN ('bp_master', 'bp_contact', 'bp_bank_account', 'business_partner')
ORDER BY table_name, ordinal_position;
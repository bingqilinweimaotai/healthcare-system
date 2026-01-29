-- AI 智能问诊平台表结构（MyBatis 手动建表）

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    phone VARCHAR(32),
    nickname VARCHAR(64),
    avatar VARCHAR(255),
    role VARCHAR(24) NOT NULL DEFAULT 'PATIENT',
    status VARCHAR(24) NOT NULL DEFAULT 'NORMAL',
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS doctor_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    real_name VARCHAR(64) NOT NULL,
    hospital VARCHAR(128),
    department VARCHAR(64),
    title VARCHAR(32),
    license_no VARCHAR(64),
    audit_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS ai_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ONGOING',
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS ai_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    sender VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS consult_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT,
    status VARCHAR(24) NOT NULL DEFAULT 'WAITING_CLAIM',
    created_at DATETIME,
    updated_at DATETIME,
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS consult_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    sender_type VARCHAR(24) NOT NULL,
    sender_id BIGINT,
    content TEXT NOT NULL,
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS drug (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    spec VARCHAR(64),
    unit VARCHAR(32),
    usage_instruction VARCHAR(256),
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS prescription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    diagnosis VARCHAR(256),
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS prescription_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    dosage VARCHAR(128),
    frequency VARCHAR(64)
);

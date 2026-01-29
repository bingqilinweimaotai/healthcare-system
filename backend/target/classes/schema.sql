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

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE COMMENT '权限码，如 admin:user:list',
    name VARCHAR(128) NOT NULL COMMENT '权限名称',
    description VARCHAR(256) COMMENT '权限描述',
    created_at DATETIME DEFAULT NOW()
);

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(24) NOT NULL COMMENT '角色：PATIENT, DOCTOR, ADMIN',
    permission_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT NOW(),
    UNIQUE KEY uk_role_permission (role, permission_id),
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
);

-- 初始化权限数据
INSERT IGNORE INTO sys_permission (code, name, description) VALUES
-- 管理员权限
('admin:dashboard', '数据概览', '查看系统数据概览'),
('admin:user:list', '用户列表', '查看用户列表'),
('admin:user:update', '用户管理', '修改用户状态'),
('admin:doctor:list', '医生列表', '查看医生列表'),
('admin:doctor:update', '医生审核', '审核医生资质'),
('admin:drug:list', '药品列表', '查看药品列表'),
('admin:drug:update', '药品管理', '新增/修改/删除药品'),
-- 医生权限
('doctor:dashboard', '工作台', '医生工作台'),
('doctor:consult:list', '会话列表', '查看咨询会话列表'),
('doctor:consult:handle', '处理会话', '认领和处理咨询会话'),
('doctor:prescription:create', '开具处方', '为患者开具处方'),
('doctor:prescription:list', '处方记录', '查看处方记录'),
-- 患者权限
('patient:dashboard', '患者首页', '患者首页'),
('patient:consult:create', '发起咨询', '发起AI或人工咨询'),
('patient:consult:list', '咨询记录', '查看咨询记录'),
('patient:history:view', '问诊记录', '查看问诊和处方记录');

-- 初始化角色权限关联（ADMIN 拥有所有权限）
INSERT IGNORE INTO sys_role_permission (role, permission_id)
SELECT 'ADMIN', id FROM sys_permission;

-- DOCTOR 角色权限
INSERT IGNORE INTO sys_role_permission (role, permission_id)
SELECT 'DOCTOR', id FROM sys_permission WHERE code LIKE 'doctor:%';

-- PATIENT 角色权限
INSERT IGNORE INTO sys_role_permission (role, permission_id)
SELECT 'PATIENT', id FROM sys_permission WHERE code LIKE 'patient:%';

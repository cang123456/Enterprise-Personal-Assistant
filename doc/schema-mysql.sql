-- =====================================================================
-- 企业 MySQL 版本建表脚本（生产环境用）
-- 字符集：utf8mb4，排序：utf8mb4_unicode_ci
-- =====================================================================
CREATE DATABASE IF NOT EXISTS assistant_demo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE assistant_demo;

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(64)  NOT NULL COMMENT '登录名/工号',
    nickname    VARCHAR(64)           DEFAULT NULL COMMENT '昵称',
    email       VARCHAR(128)          DEFAULT NULL COMMENT '邮箱',
    status      VARCHAR(16)  NOT NULL DEFAULT 'normal' COMMENT '状态：normal=正常 disabled=禁用',
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业用户表';

-- 测试数据（与 H2 内存库保持一致）
INSERT INTO sys_user (username, nickname, email, status, create_time) VALUES
('zhangsan', '张三', 'zhangsan@corp.com', 'normal',   '2026-08-01 09:10:00'),
('lisi',     '李四', 'lisi@corp.com',     'normal',   '2026-08-05 14:20:00'),
('wangwu',   '王五', 'wangwu@corp.com',   'disabled', '2026-08-10 10:00:00'),
('zhaoliu',  '赵六', 'zhaoliu@corp.com',  'normal',   '2026-09-01 11:30:00'),
('sunqi',    '孙七', 'sunqi@corp.com',    'disabled', '2026-09-15 16:45:00'),
('zhouba',   '周八', 'zhouba@corp.com',   'normal',   '2026-10-01 08:00:00');

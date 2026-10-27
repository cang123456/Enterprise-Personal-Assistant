-- sys_user：企业用户表（H2 内存库，开发演示用）
-- 对应企业 MySQL 版本见 doc/schema-mysql.sql
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL,
    nickname    VARCHAR(64),
    email       VARCHAR(128),
    status      VARCHAR(16)  NOT NULL DEFAULT 'normal',   -- normal / disabled
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sys_user_username ON sys_user(username);
CREATE INDEX idx_sys_user_status   ON sys_user(status);

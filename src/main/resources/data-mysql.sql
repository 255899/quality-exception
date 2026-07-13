-- MySQL 初始化脚本（dev profile 使用 H2，不需要此文件；prod profile 才使用）
-- 在 MySQL 中执行：CREATE DATABASE qualitydb DEFAULT CHARSET utf8mb4;

-- 表结构由 JPA 自动创建（ddl-auto=update），无需手动建表
-- 此处仅提供示例数据（也可由 DataInitializer 在第一次启动时自动写入）

INSERT INTO t_department (name, type, parent_id) VALUES
('加工一课', 'SECTION', NULL),
('加工二课', 'SECTION', NULL),
('加工三课', 'SECTION', NULL);

-- 假设加工一课 ID=1，加工二课 ID=2，加工三课 ID=3（实际 ID 以 JPA 自增为准）
-- INSERT INTO t_department (name, type, parent_id) VALUES
-- ('一组', 'GROUP', 1),
-- ('二组', 'GROUP', 1),
-- ('甲组', 'GROUP', 2),
-- ('乙组', 'GROUP', 2),
-- ('单组', 'GROUP', 3);

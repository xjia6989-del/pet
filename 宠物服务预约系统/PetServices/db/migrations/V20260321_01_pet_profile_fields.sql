-- 宠物档案字段补充脚本
-- 适用数据库：MySQL 5.7+/8.0+
-- 执行前请先备份数据库

ALTER TABLE `pet`
    ADD COLUMN `age` INT NULL COMMENT '年龄' AFTER `breed`,
    ADD COLUMN `vaccineRecord` VARCHAR(2000) NULL COMMENT '疫苗记录' AFTER `age`,
    ADD COLUMN `medicalHistory` VARCHAR(2000) NULL COMMENT '病史' AFTER `vaccineRecord`;

-- 可选：给 userId 加索引，提升“我的宠物”查询性能
CREATE INDEX `idx_pet_userId` ON `pet` (`userId`);

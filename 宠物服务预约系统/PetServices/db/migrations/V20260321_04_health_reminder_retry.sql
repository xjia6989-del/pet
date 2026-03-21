-- 健康提醒推送增强：失败重试 / 失败原因 / 手动重发支持
-- 依赖：V20260321_03_health_reminder_message.sql

ALTER TABLE `health_reminder_message`
  ADD COLUMN `retryCount` INT NOT NULL DEFAULT 0 COMMENT '重试次数' AFTER `status`,
  ADD COLUMN `failReason` VARCHAR(512) NULL COMMENT '失败原因' AFTER `retryCount`,
  ADD COLUMN `lastAttemptAt` DATETIME NULL COMMENT '最后尝试发送时间' AFTER `failReason`;

-- 健康提醒完整推送链路：消息落库表
-- 请先执行 V20260321_02_user_health_subscribe.sql

CREATE TABLE IF NOT EXISTS `health_reminder_message` (
  `messageId` INT NOT NULL AUTO_INCREMENT,
  `userId` INT NOT NULL,
  `petId` INT NOT NULL,
  `petName` VARCHAR(128) NULL,
  `reminderType` VARCHAR(32) NOT NULL,
  `reminderText` VARCHAR(512) NOT NULL,
  `dueDate` VARCHAR(20) NOT NULL,
  `status` INT NOT NULL DEFAULT 0 COMMENT '0待发送 1已发送',
  `sentAt` DATETIME NULL,
  `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`messageId`),
  KEY `idx_hrm_user` (`userId`),
  KEY `idx_hrm_pet` (`petId`),
  UNIQUE KEY `uk_hrm_unique_msg` (`userId`,`petId`,`reminderType`,`dueDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 个人中心：联系客服留言表

CREATE TABLE IF NOT EXISTS `contact_message` (
  `messageId` INT NOT NULL AUTO_INCREMENT,
  `userId` INT NOT NULL,
  `username` VARCHAR(100) NULL,
  `name` VARCHAR(100) NULL,
  `phone` VARCHAR(30) NULL,
  `content` VARCHAR(1000) NOT NULL,
  `status` INT NOT NULL DEFAULT 0 COMMENT '0未处理 1已处理',
  `reply` VARCHAR(1000) NULL,
  `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `replyTime` DATETIME NULL,
  PRIMARY KEY (`messageId`),
  KEY `idx_contact_user` (`userId`),
  KEY `idx_contact_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

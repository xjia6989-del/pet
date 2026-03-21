-- 用户健康提醒订阅开关落地
-- 适用 MySQL 5.7+/8.0+

ALTER TABLE `user`
    ADD COLUMN `healthReminderSubscribed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '健康提醒订阅开关：0未订阅，1已订阅';

ALTER TABLE `booking`
  ADD COLUMN IF NOT EXISTS `vetId` INT NULL COMMENT '分配的兽医ID' AFTER `serve`;

CREATE INDEX IF NOT EXISTS `idx_booking_vet_id` ON `booking` (`vetId`);

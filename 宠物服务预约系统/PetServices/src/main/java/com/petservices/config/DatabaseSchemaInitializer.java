package com.petservices.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        ensureBookingVetIdColumn();
        ensureBookingSlotConfigTable();
    }

    private void ensureBookingVetIdColumn() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'booking' AND COLUMN_NAME = 'vetId'",
                    Integer.class
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute("ALTER TABLE `booking` ADD COLUMN `vetId` INT NULL COMMENT '分配的兽医ID' AFTER `serve`");
                log.info("Database schema initialized: added booking.vetId column");
            }
            Integer indexCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM information_schema.STATISTICS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'booking' AND INDEX_NAME = 'idx_booking_vet_id'",
                    Integer.class
            );
            if (indexCount == null || indexCount == 0) {
                jdbcTemplate.execute("CREATE INDEX `idx_booking_vet_id` ON `booking` (`vetId`)");
                log.info("Database schema initialized: added idx_booking_vet_id index");
            }
        } catch (Exception e) {
            log.warn("Database schema initialization skipped or failed for booking.vetId", e);
        }
    }

    private void ensureBookingSlotConfigTable() {
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `booking_slot_config` (" +
                    "`id` INT NOT NULL AUTO_INCREMENT," +
                    "`serveId` INT NOT NULL," +
                    "`configDate` DATE NOT NULL," +
                    "`timeSlot` VARCHAR(16) NOT NULL," +
                    "`enabled` TINYINT NOT NULL DEFAULT 1," +
                    "`capacity` INT NOT NULL DEFAULT 1," +
                    "PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            Integer uniqueCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM information_schema.STATISTICS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'booking_slot_config' AND INDEX_NAME = 'uk_booking_slot_config_serve_date_slot'",
                    Integer.class
            );
            if (uniqueCount == null || uniqueCount == 0) {
                jdbcTemplate.execute("CREATE UNIQUE INDEX `uk_booking_slot_config_serve_date_slot` ON `booking_slot_config` (`serveId`, `configDate`, `timeSlot`)");
                log.info("Database schema initialized: added booking_slot_config unique index");
            }
        } catch (Exception e) {
            log.warn("Database schema initialization skipped or failed for booking_slot_config", e);
        }
    }
}

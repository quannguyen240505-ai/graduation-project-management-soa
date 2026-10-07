CREATE DATABASE IF NOT EXISTS graduation_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE graduation_management;

CREATE TABLE IF NOT EXISTS detai (
    id BIGINT NOT NULL AUTO_INCREMENT,
    topic_code VARCHAR(50) NOT NULL,
    topic_name VARCHAR(255) NOT NULL,
    description TEXT,
    lecturer_id BIGINT NOT NULL,
    max_students INT NOT NULL,
    current_students INT NOT NULL DEFAULT 0,
    field VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_detai_topic_code
        UNIQUE (topic_code),

    CONSTRAINT chk_detai_max_students
        CHECK (max_students > 0),

    CONSTRAINT chk_detai_current_students
        CHECK (
            current_students >= 0
            AND current_students <= max_students
        )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- Bảng này thuộc Topic Service. Không FK đến bảng dangky của Registration.
CREATE TABLE IF NOT EXISTS topic_registration_slots (
    registration_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    approved BIT(1) NOT NULL,
    capacity_version BIGINT NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (registration_id),
    INDEX idx_topic_slots_topic_id (topic_id),
    CONSTRAINT fk_topic_slots_topic
        FOREIGN KEY (topic_id) REFERENCES detai(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- Chạy một lần trước khi khởi động lại Topic Service (ddl-auto=validate).
USE graduation_management;

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

-- Registration Service hiện dùng ddl-auto=update và sẽ thêm
-- dangky.capacity_version BIGINT NOT NULL DEFAULT 0 khi chạy lại.
-- Script không xóa/reset dữ liệu hiện có.

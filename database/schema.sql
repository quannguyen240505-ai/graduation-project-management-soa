-- ============================================================
-- Graduation Project Management SOA
-- Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS graduation_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE graduation_management;


-- ============================================================
-- 1. USERS
-- Owner: Auth Service
-- ============================================================

CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,

    `class_name` VARCHAR(50)
        COLLATE utf8mb4_unicode_ci DEFAULT NULL,

    `created_at` DATETIME(6) NOT NULL,

    `department` VARCHAR(100)
        COLLATE utf8mb4_unicode_ci DEFAULT NULL,

    `email` VARCHAR(100)
        COLLATE utf8mb4_unicode_ci NOT NULL,

    `full_name` VARCHAR(100)
        COLLATE utf8mb4_unicode_ci NOT NULL,

    `password` VARCHAR(255)
        COLLATE utf8mb4_unicode_ci NOT NULL,

    `phone` VARCHAR(20)
        COLLATE utf8mb4_unicode_ci DEFAULT NULL,

    `role` ENUM(
        'ADMIN',
        'LECTURER',
        'STUDENT'
    ) COLLATE utf8mb4_unicode_ci NOT NULL,

    `status` ENUM(
        'ACTIVE',
        'INACTIVE',
        'LOCKED'
    ) COLLATE utf8mb4_unicode_ci NOT NULL,

    `updated_at` DATETIME(6) NOT NULL,

    `user_code` VARCHAR(30)
        COLLATE utf8mb4_unicode_ci NOT NULL,

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_users_user_code` (`user_code`),

    UNIQUE KEY `uk_users_email` (`email`)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 2. DETAI
-- Owner: Topic Service
-- ============================================================

CREATE TABLE IF NOT EXISTS `detai` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,

    `topic_code` VARCHAR(50)
        COLLATE utf8mb4_unicode_ci NOT NULL,

    `topic_name` VARCHAR(255)
        COLLATE utf8mb4_unicode_ci NOT NULL,

    `description` TEXT
        COLLATE utf8mb4_unicode_ci,

    `lecturer_id` BIGINT NOT NULL,

    `max_students` INT NOT NULL,

    `current_students` INT NOT NULL DEFAULT 0,

    `field` VARCHAR(100)
        COLLATE utf8mb4_unicode_ci DEFAULT NULL,

    `status` VARCHAR(30)
        COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OPEN',

    `created_at` DATETIME(6) NOT NULL,

    `updated_at` DATETIME(6) NOT NULL,

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_detai_topic_code` (`topic_code`),

    CONSTRAINT `chk_detai_max_students`
        CHECK (`max_students` > 0),

    CONSTRAINT `chk_detai_current_students`
        CHECK (
            `current_students` >= 0
            AND `current_students` <= `max_students`
        )
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 3. TOPIC REGISTRATION SLOTS
-- Owner: Topic Service
--
-- Lưu trạng thái số chỗ đã được tính cho từng registration.
-- registration_id chỉ là ID nhận từ Registration Service.
-- Không tạo FK sang bảng dangky vì hai bảng thuộc hai service khác nhau.
-- ============================================================

CREATE TABLE IF NOT EXISTS `topic_registration_slots` (
    `registration_id` BIGINT NOT NULL,

    `topic_id` BIGINT NOT NULL,

    `approved` BIT(1) NOT NULL,

    `capacity_version` BIGINT NOT NULL,

    `updated_at` DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`registration_id`),

    INDEX `idx_topic_slots_topic_id` (`topic_id`),

    CONSTRAINT `fk_topic_slots_topic`
        FOREIGN KEY (`topic_id`)
        REFERENCES `detai` (`id`)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 4. DANGKY
-- Owner: Registration Service
-- ============================================================

CREATE TABLE IF NOT EXISTS `dangky` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,

    `created_at` DATETIME(6) NOT NULL,

    `note` VARCHAR(255)
        COLLATE utf8mb4_unicode_ci DEFAULT NULL,

    `registration_date` DATETIME(6) NOT NULL,

    `status` VARCHAR(30)
        COLLATE utf8mb4_unicode_ci NOT NULL,

    `student_id` BIGINT NOT NULL,

    `topic_id` BIGINT NOT NULL,

    `updated_at` DATETIME(6) NOT NULL,

    PRIMARY KEY (`id`)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 5. NOTIFICATIONS
-- Owner: Notification Service
-- ============================================================

CREATE TABLE IF NOT EXISTS `notifications` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,

    `content` VARCHAR(1000)
        COLLATE utf8mb4_unicode_ci NOT NULL,

    `created_at` DATETIME(6) NOT NULL,

    `is_read` BIT(1) NOT NULL,

    `title` VARCHAR(255)
        COLLATE utf8mb4_unicode_ci NOT NULL,

    `user_id` BIGINT NOT NULL,

    PRIMARY KEY (`id`)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;
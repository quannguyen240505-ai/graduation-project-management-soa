# Graduation Project Management System - SOA

Hệ thống quản lý đồ án tốt nghiệp được xây dựng bằng **Spring Boot** và tổ chức theo kiến trúc **SOA (Service-Oriented Architecture)**.

Framework nghiên cứu chính là **Spring Boot**. Một số công nghệ hỗ trợ được tích hợp thêm để xây dựng hệ thống phân tán gồm Spring Cloud, Eureka, OpenFeign, RabbitMQ, Resilience4j, MySQL và Angular.

---

## 1. Công nghệ sử dụng

### Framework chính
- Spring Boot 4.1.1
- Java 25

### Công nghệ hỗ trợ
- Spring Cloud Gateway
- Netflix Eureka
- OpenFeign
- Spring Security
- Spring Data JPA
- Spring AMQP
- RabbitMQ
- Resilience4j
- MySQL
- Angular

---

## 2. Kiến trúc hệ thống

Hệ thống được chia thành các service độc lập:

| Thành phần | Port | Chức năng |
|---|---:|---|
| Service Registry | 8761 | Đăng ký và phát hiện service bằng Eureka |
| API Gateway | 8080 | Điểm vào chung, routing, JWT và phân quyền |
| Auth Service | 8081 | Đăng ký, đăng nhập, JWT, quản lý người dùng |
| Topic Service | 8082 | Quản lý đề tài |
| Registration Service | 8083 | Quản lý đăng ký đề tài |
| Notification Service | 8084 | Quản lý thông báo |
| Angular Frontend | 4200 | Giao diện người dùng |

---

## 3. Giao tiếp giữa các service

### Giao tiếp đồng bộ

Registration Service gọi Auth Service và Topic Service thông qua **OpenFeign**.

```text
Registration Service
        |
        +----> Auth Service
        |
        +----> Topic Service

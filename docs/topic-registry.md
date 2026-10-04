# Topic Service + Service Registry

Phụ trách: Quốc
Branch: feature/topic-registry

Service Registry — Implemented using Netflix Eureka.

## 1. Môi trường

- JDK 25.
- MySQL chạy tại localhost:3306.
- Database: graduation_management.
- Chạy phần SQL tạo bảng detai trong database/schema.sql.
- Topic Service dùng ddl-auto: validate, nên cần tạo bảng trước khi chạy.

## 2. Cổng chạy

| Thành phần | Cổng |
|---|---:|
| Service Registry | 8761 |
| Auth Service | 8081 |
| Topic Service | 8082 |

## 3. Biến môi trường

Auth Service:
- DB_USERNAME
- DB_PASSWORD
- JWT_SECRET

Topic Service:
- DB_PASSWORD
- JWT_SECRET

Topic Service hiện cấu hình tài khoản MySQL là root.
Auth và Topic phải dùng cùng JWT_SECRET, tối thiểu 32 byte UTF-8.
Thiết lập các giá trị trong cấu hình chạy trên máy, không ghi secret
hoặc mật khẩu MySQL vào mã nguồn.

## 4. Thứ tự khởi động

1. MySQL.
2. ServiceRegistryApplication.
3. AuthServiceApplication.
4. TopicServiceApplication.

Mở http://localhost:8761.
Kiểm tra AUTH-SERVICE và TOPIC-SERVICE có trạng thái UP.

Topic Service tìm Auth Service theo tên AUTH-SERVICE qua Eureka
để kiểm tra giảng viên, không truy cập trực tiếp bảng users.

## 5. API Topic

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | /api/topics | Danh sách đề tài |
| GET | /api/topics/{id} | Chi tiết đề tài |
| POST | /api/topics | Tạo đề tài |
| PUT | /api/topics/{id} | Cập nhật đề tài |
| DELETE | /api/topics/{id} | Xóa đề tài |

Tất cả API Topic yêu cầu JWT.
ADMIN và LECTURER được tạo, cập nhật, xóa.
STUDENT được xem danh sách và chi tiết.

## 6. Demo bằng Postman

Import postman/TopicRegistry.postman_collection.json.

Tài khoản demo đã chuẩn bị trên máy:
- userCode: GVTEST01
- password: Demo@12345
- role: LECTURER
- status: ACTIVE
- id: 2

Tài khoản này thuộc dữ liệu trên máy demo, không tự xuất hiện
khi clone repository. Máy khác cần chuẩn bị tài khoản giảng viên,
sửa thông tin đăng nhập và lecturerId trong Body của POST/PUT
theo ID thực tế.

Gửi request theo thứ tự:
1. Đăng nhập giảng viên → 200, tự lưu biến token.
2. Tạo đề tài → 201, tự lưu biến topicId.
3. Lấy danh sách → 200.
4. Xem chi tiết → 200.
5. Cập nhật đề tài → 200.
6. Xóa đề tài → 204.
7. Xem lại đề tài vừa xóa → 404.

Nếu DT001 đã tồn tại, POST trả 409.
Đổi topicCode hoặc xóa dữ liệu demo cũ trước khi tạo lại.

## 7. Các trường hợp đã kiểm tra

- Không có JWT: 401.
- STUDENT xem danh sách: 200.
- STUDENT tạo đề tài: 403.
- Trùng topicCode: 409.
- maxStudents bằng 0: 400.
- Gán người có role STUDENT làm giảng viên: 400.
- Giảng viên không tồn tại: 400.
- Đề tài không tồn tại: 404.

## 8. Phạm vi tích hợp

Đã kiểm tra Topic Service và Auth Service đăng ký với Eureka,
và Topic gọi Auth để kiểm tra giảng viên.

Gateway, Registration và Notification sẽ được tích hợp cùng nhóm.
Cần thống nhất API cập nhật số sinh viên đăng ký với người phụ trách
Registration Service.
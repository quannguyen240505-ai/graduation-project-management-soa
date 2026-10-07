# Đồng bộ current_students giữa Topic và Registration

Bản sửa dựa trên commit `d32d0eed7a14586cf5d277493abe51bf9a66efd1` mà nhóm đã merge vào main. Nhánh làm việc của Người 2: `feature/topic-capacity`.

## Quy ước nghiệp vụ

`detai.current_students` đếm số đăng ký **APPROVED**, không đếm PENDING. Nhiều sinh viên có thể chờ duyệt; khi duyệt, Topic kiểm tra lại giới hạn trong transaction có khóa hàng.

| Thao tác | Số chỗ |
|---|---|
| Tạo đăng ký PENDING | Chưa tăng |
| PENDING/REJECTED → APPROVED | Tăng 1 nếu còn chỗ và đề tài OPEN |
| Gửi lại APPROVED | Giữ nguyên |
| APPROVED → REJECTED/PENDING | Giảm 1 |
| Gửi lại REJECTED/PENDING | Giữ nguyên |
| Xóa đăng ký APPROVED | Giảm 1 trước khi xóa đăng ký |
| Xóa đăng ký chưa được tính chỗ | Giữ nguyên |

## Phân công và dữ liệu

- Người 2 bổ sung API và logic quản lý chỗ của Topic.
- Registration cần gọi API này khi đổi trạng thái/xóa. Gói sửa đã có phần tích hợp để nhóm review cùng nhau.
- Gateway bổ sung rule cho thao tác duyệt/xóa và chặn API counter trực tiếp qua cổng 8080.
- Auth, Registry, Notification, parent POM và số cổng giữ cấu hình hiện tại của nhóm.
- Topic chỉ cập nhật dữ liệu do Topic quản lý: `detai` và bảng lịch sử chỗ mới `topic_registration_slots`.
- `registration_id` trong lịch sử chỗ là ID trao đổi qua REST, không phải FK đến `dangky`. FK của bảng lịch sử chỉ trỏ tới `detai`, cùng chủ sở hữu Topic.
- Registration chỉ ghi `dangky`, thêm cột `capacity_version` để đánh số lần đổi trạng thái.

## Hợp đồng REST giữa hai service

Registration dùng Feign với tên `topic-service`, được Eureka phân giải địa chỉ. Nó chuyển tiếp Bearer JWT của người đang duyệt/xóa.

```http
PUT /api/topics/{topicId}/registrations/{registrationId}/capacity
Authorization: Bearer <JWT LECTURER/ADMIN>
Content-Type: application/json
```

```json
{"approved": true, "version": 1}
```

Response thành công: 200, body là TopicResponse hiện tại.

- `approved=true`: đăng ký cần được tính một chỗ.
- `approved=false`: đăng ký không cần được tính chỗ.
- `version`: cùng yêu cầu gửi lại dùng cùng số; lần đổi trạng thái tiếp theo dùng số lớn hơn.
- ADMIN được thao tác; LECTURER phải có `userId` trong JWT trùng `lecturer_id` của đề tài.
- Cùng ID đăng ký/version/trạng thái: thành công và giữ số lượng.
- Version cũ hoặc cùng version nhưng khác approved: 409, không sửa số lượng.
- ID đăng ký đã gắn với một Topic khác: 409.
- Đề tài đầy/đóng khi cần cấp thêm chỗ: 409.
- Các thao tác cấp/trả chỗ, sửa maxStudents và xóa Topic cùng khóa hàng `detai` bằng `PESSIMISTIC_WRITE`.

Gateway chặn đường dẫn counter này. Postman test qua **Registration**, không gửi lệnh tăng/giảm trực tiếp tới Topic. `PUT /api/registrations/{id}/status` và `DELETE /api/registrations/{id}` qua Gateway yêu cầu LECTURER/ADMIN.

## Database và khởi động

1. Dừng Topic, Registration và Gateway đang chạy.
2. Trong Workbench, chạy `database/topic_capacity.sql` để tạo bảng lịch sử chỗ. Script không xóa dữ liệu.
3. Topic dùng `ddl-auto=validate`, nên phải tạo bảng trước khi chạy lại.
4. Registration hiện dùng `ddl-auto=update`, nên khi chạy lại sẽ thêm `dangky.capacity_version BIGINT NOT NULL DEFAULT 0`.
5. Database mới có thể chạy `database/schema.sql`; file này đã gồm bảng lịch sử.
6. Chạy lại Registry → Auth → Topic → Registration → Gateway. Notification chạy như quy trình hiện có khi test phần thông báo.

Cổng: Registry 8761, Gateway 8080, Auth 8081, Topic 8082, Registration 8083, Notification 8084. JWT_SECRET tiếp tục dùng giá trị chung của nhóm ở biến môi trường.

## Test Postman

1. Chạy `01 - Auth` của bộ test nhóm để có `lecturerToken`, `lecturerId`, `studentToken`, `studentId`.
2. Chọn Environment **Graduation SOA Local**, `baseUrl=http://localhost:8080`.
3. Import `postman/TopicCapacity.postman_collection.json`.
4. Mở collection **Graduation SOA Capacity Test**, chạy 01–20 theo thứ tự.

Collection tạo Topic mới có mã CAP_<timestamp>, maxStudents=1. Các ID riêng được lưu vào `capacityTopicId` và `capacityRegistrationId`, không ghi đè các ID của collection nhóm.

Kỳ vọng: PENDING=0 → APPROVED=1 → duyệt lại vẫn 1 → REJECTED=0 → từ chối lại vẫn 0 → duyệt lại=1 → xóa đăng ký=0. Student duyệt trả 403; gọi counter qua Gateway trả 403. Xóa lại đăng ký đã xóa trả 404 và số lượng vẫn 0.

Collection không chứa mật khẩu hoặc token thật. Topic demo vẫn tồn tại sau khi test; bản ghi đăng ký demo được xóa tại request 15.

Test giới hạn với hai sinh viên: tạo Topic maxStudents=1, tạo hai đăng ký PENDING bằng hai tài khoản STUDENT khác nhau trước khi duyệt. Duyệt đăng ký thứ nhất → 200/currentStudents=1; duyệt đăng ký thứ hai → 409, trạng thái vẫn PENDING. Việc chỉ kiểm tra GET rồi tăng không khóa sẽ không đủ để bảo đảm giới hạn.

## Đăng ký APPROVED đã có từ bản cũ

Code gửi để review chưa từng tăng counter khi duyệt. Những đăng ký APPROVED cũ không tự được tính lại chỉ bằng việc tạo bảng mới.

Nếu counter trước đó vẫn là 0 như bản cũ, lấy danh sách đăng ký bằng REST và gửi lại `{"status":"APPROVED"}` cho từng đăng ký đã duyệt, dùng JWT của giảng viên phụ trách hoặc ADMIN. Lần đầu sẽ tạo lịch sử và tính chỗ; gửi lại cùng đăng ký không đếm thêm.

Nếu counter đã được chỉnh tay hoặc một bản code khác đã tính chỗ trước khi có bảng lịch sử, cần đối chiếu trước khi làm bước trên để tránh đếm lại dữ liệu cũ. Bản sửa không tự reset counter hoặc xóa dữ liệu để giải quyết chênh lệch.

Khi reset demo theo tài liệu nhóm, phải xử lý `topic_registration_slots` cùng dữ liệu nghiệp vụ trước khi xóa detai hoặc tái sử dụng ID dangky. Không reset AUTO_INCREMENT của dangky riêng trong khi giữ lịch sử chỗ cũ.

## Khi một service hoặc database lỗi

Lịch sử chỗ và counter được commit trong cùng transaction của Topic. Registration giữ khóa hàng đăng ký khi đồng bộ, rồi lưu trạng thái/version sau khi Topic trả thành công. 400/401/403/404/409 từ Topic được giữ nguyên; lỗi kết nối/5xx trả 503 và chưa ghi trạng thái mới tại Registration.

HTTP giữa hai service không tạo transaction chung. Nếu Topic đã commit nhưng response bị mất, hoặc database Registration lỗi sau đó, hai bên có thể tạm lệch. Gửi lại **chính yêu cầu vừa lỗi**, dùng cùng ID và trạng thái, để hoàn tất. Idempotency xử lý việc Topic đã tính/trả chỗ trước đó.

Không đổi sang một thao tác khác ngay khi kết quả trước đó còn chưa xác định: version bảo vệ có thể trả 409 để tránh sửa ngược một yêu cầu đã thực hiện. Cần hoàn tất yêu cầu cũ hoặc đối soát hai bên qua API. Bản sửa chưa có outbox hay tác vụ tự động đối soát khi tiến trình bị tắt đột ngột.

## Kiểm tra và giới hạn xác minh

Đã thực sự chạy: 18 kiểm tra bằng Java đối với **chính TopicCapacityPolicy của ứng dụng**, gồm cấp/trả chỗ, gửi lặp, full/closed, version cũ, cùng version khác trạng thái, và chống âm. Đã parse cú pháp toàn bộ 34 file Java trong snapshot sửa.

Chưa chạy tại môi trường chuẩn bị gói: Maven/JUnit, Spring context, MySQL row locking, Eureka/Feign và Postman thật. Môi trường chuẩn bị chỉ có Java 17, không có Maven/JDK 25; cú pháp parse không thay thế build/type checking.

Trên máy có JDK 25, trong repo đầy đủ của nhóm:

```bash
bash verification/check_capacity_policy.sh
mvn -pl topic-service,registration-service,api-gateway -am -DskipTests package
mvn -pl topic-service,registration-service -am "-Dtest=TopicCapacityServiceTest,RegistrationCapacityTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Hai test class JUnit gồm 25 test, dùng mock cho repository/Feign và không cần MySQL. Chúng kiểm tra orchestration/permissions/status/version và việc không lưu trạng thái khi Topic từ chối. Không dùng chúng để kết luận locking hoặc transaction giữa hai service đã được test thực tế.

Tài liệu chính thức dùng khi triển khai: [Spring Data JPA Locking](https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html), [Spring Cloud OpenFeign](https://docs.spring.io/spring-cloud-openfeign/reference/spring-cloud-openfeign.html), [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/index.html).

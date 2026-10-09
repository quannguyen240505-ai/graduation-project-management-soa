# Áp dụng bản sửa Topic Capacity trên máy Dell

Gói này bổ sung cập nhật `current_students` khi duyệt/bỏ duyệt/xóa đăng ký. Nó được chuẩn bị từ ZIP của nhóm tại commit d32d0ee; code cần được build và test tích hợp trên máy bạn trước khi push.

## 1. Giải nén và chép file

1. Tải `topic-capacity-fix.zip` vào Downloads.
2. Nhấp phải file ZIP → **Extract All / Giải nén tất cả**.
3. Chọn thư mục đích `C:\Users\Dell\Downloads\topic-capacity-fix`.
4. Mở thư mục vừa giải nén. Phải thấy **changes**, **HUONG_DAN_AP_DUNG.md**, **MANIFEST.txt** ở ngay cấp này.
5. Trong Eclipse/STS, dừng Topic, Registration, Gateway đang chạy. Registry và Auth có thể tiếp tục chạy.
6. Mở Git Bash, dán:

```bash
cd ~/graduation-project-management-soa
git branch --show-current
git status --short
```

Nhánh cần là `feature/topic-capacity`; trạng thái trước khi chép file cần sạch như ảnh đã gửi. Bạn đã có nhánh này, không tạo lại.

Chép toàn bộ file thay đổi vào project:

```bash
cp -r /c/Users/Dell/Downloads/topic-capacity-fix/changes/. .
git status --short
```

Lệnh cp thành công thường không có thông báo. Git status sẽ liệt kê các file sửa/thêm. Gói changes chứa các file cần bổ sung/thay, không phải một project độc lập để import mới. Các module khác vẫn nằm trong thư mục project hiện có.

## 2. Eclipse/STS nhận lại code

1. Quay lại Eclipse/STS, chọn project → **F5 / Refresh**.
2. Nhấp phải project Maven → **Maven → Update Project…** (Alt+F5).
3. Chọn các project của repo rồi bấm OK để nạp dependency test mới của Topic.

Nếu Registration chưa có trong danh sách project: **File → Import → Maven → Existing Maven Projects**, chọn root `C:\Users\Dell\graduation-project-management-soa`, rồi import module còn thiếu.

## 3. Chạy SQL

1. Mở MySQL Workbench, kết nối MySQL local.
2. **File → Open SQL Script**, mở:

```text
C:\Users\Dell\graduation-project-management-soa\database\topic_capacity.sql
```

3. Bấm nút tia sét để chạy script. Kỳ vọng xuất hiện bảng **topic_registration_slots** trong database **graduation_management**.
4. Không phải xóa database hoặc chạy lại toàn bộ schema cho database đang có.

Registration sẽ tự thêm cột **capacity_version** trong bảng dangky khi khởi động lại vì module hiện dùng `ddl-auto=update`.

## 4. Build và unit test

Trong Git Bash tại root repo:

```bash
java -version
mvn -version
mvn -pl topic-service,registration-service,api-gateway -am -DskipTests package
mvn -pl topic-service,registration-service -am "-Dtest=TopicCapacityServiceTest,RegistrationCapacityTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Maven cần chạy bằng JDK 25 theo parent POM. Kỳ vọng cuối mỗi lệnh Maven là **BUILD SUCCESS**. Nếu có lỗi, giữ nguyên nhánh và gửi nguyên phần lỗi Console/terminal để sửa tiếp.

Nếu Git Bash báo không có mvn, dùng Maven trong STS: nhấp phải project Maven gốc → **Run As → Maven build…**, chọn JRE/JDK 25; ở Goals lần lượt nhập:

```text
-pl topic-service,registration-service,api-gateway -am -DskipTests package
```

và:

```text
-pl topic-service,registration-service -am -Dtest=TopicCapacityServiceTest,RegistrationCapacityTest -Dsurefire.failIfNoSpecifiedTests=false test
```

18 kiểm tra độc lập cho logic số chỗ cũng có thể chạy bằng:

```bash
bash verification/check_capacity_policy.sh
```

## 5. Khởi động và test Postman

Chạy Registry 8761 → Auth 8081 → Topic 8082 → Registration 8083 → Gateway 8080. Giữ các biến môi trường DB_PASSWORD/JWT_SECRET như nhóm đang dùng. Notification chạy khi test luồng thông báo hiện có của nhóm.

Trong Postman:

1. Chọn Environment **Graduation SOA Local**; `baseUrl=http://localhost:8080`.
2. Trong collection nhóm, chạy các request **01 - Auth** để login Lecturer và Student, lưu token/ID mới.
3. Bấm **Import**, chọn:

```text
C:\Users\Dell\graduation-project-management-soa\postman\TopicCapacity.postman_collection.json
```

4. Mở collection **Graduation SOA Capacity Test → 06 - Topic Capacity**.
5. Chạy 01–20 theo thứ tự; mỗi request có script kiểm tra kết quả.

Các mốc chính: tạo đăng ký → Topic có 0; duyệt → 1; duyệt lại → vẫn 1; từ chối → 0; duyệt lại → 1; xóa đăng ký → 0. Các request 14/19 kỳ vọng **403**, request 17 kỳ vọng **404**; đó là kết quả đúng.

## 6. GitHub sau khi kiểm tra

Chi tiết về nghiệp vụ, dữ liệu cũ và giới hạn khi service lỗi nằm ở **docs/topic-capacity.md**. Có chỉnh file Registration và Gateway để nối luồng, nên nhóm trưởng và Người 3 cần review những phần này cùng Topic.

Sau khi build/test trên local thành công:

```bash
git diff --stat
git add topic-service registration-service api-gateway/src/main/java/com/graduation/gateway/config/GatewaySecurityConfig.java database/schema.sql database/topic_capacity.sql postman/TopicCapacity.postman_collection.json docs/topic-capacity.md docs/topic-capacity-install.md verification/check_capacity_policy.sh
git diff --cached --stat
git commit -m "fix: synchronize approved registration counts with topic capacity"
git push -u origin feature/topic-capacity
```

Tạo PR từ **feature/topic-capacity** vào **main** theo workflow main đang dùng của repo này. Request review nhóm trưởng; để nhóm trưởng merge theo quy định nhóm.

Gợi ý tiêu đề PR: **Fix Topic capacity when registrations are approved or removed**.

Mô tả PR cần ghi: số lượng trước đây luôn 0 sau duyệt; nay đếm APPROVED, duyệt lặp không tăng thêm, bỏ duyệt/xóa trả chỗ, kiểm tra giới hạn bằng khóa hàng và ID/version. Thêm kết quả build, unit test và Postman **thực tế trên máy bạn**; không ghi rằng các bước đó đã pass nếu chưa chạy.

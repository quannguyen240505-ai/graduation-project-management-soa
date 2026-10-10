# Topic UI — phần Frontend của Người 2

Phát triển trên FE Angular 20.3 của nhóm tại commit
`d8fca180f4bb26c63acb271bf2ab7845f63ec4b1`.
Sử dụng lại AuthService, session, authGuard, authInterceptor và layout hiện có.

## Chức năng

| Trang | Địa chỉ | Chức năng |
| --- | --- | --- |
| Danh sách | `/topics` | Tìm tên/mã/lĩnh vực có hoặc không dấu; lọc lĩnh vực, còn chỗ/đã đủ/đã đóng; phân trang 9 đề tài; làm mới |
| Chi tiết | `/topics/:id` | Mô tả, giảng viên, ngày tạo/cập nhật, số sinh viên đã duyệt và số chỗ còn lại |
| Tạo | `/topics/new` | Form theo TopicRequest, kiểm tra giảng viên trước khi lưu |
| Sửa | `/topics/:id/edit` | Tải dữ liệu cũ, kiểm tra số lượng tối đa không thấp hơn số sinh viên đã duyệt |
| Xóa | Hộp xác nhận | Giữ lại/Escape để hủy; chỉ gửi DELETE sau xác nhận; xử lý 204/409 |

STUDENT xem danh sách và chi tiết. LECTURER/ADMIN có thêm thao tác quản lý.
Guard và việc ẩn nút phục vụ điều hướng giao diện; Gateway/Topic Service vẫn
kiểm tra quyền trên từng request. Không bổ sung quy tắc ownership ngoài contract
backend hiện tại.

## Kết nối API

| Phương thức | Đường dẫn | Sử dụng |
| --- | --- | --- |
| GET | `/api/topics` | Danh sách trả về mảng TopicResponse |
| GET | `/api/topics/{id}` | Chi tiết và dữ liệu ban đầu của form sửa |
| POST | `/api/topics` | Tạo, chờ phản hồi 201 rồi mở trang chi tiết |
| PUT | `/api/topics/{id}` | Sửa, chờ phản hồi 200 rồi mở trang chi tiết |
| DELETE | `/api/topics/{id}` | Xóa, chờ phản hồi 204 rồi cập nhật danh sách |
| GET | `/api/auth/users/{lecturerId}` | Kiểm tra ID, role LECTURER và status ACTIVE |

TopicService của Angular sử dụng URL tương đối. authInterceptor gắn Bearer token
từ session; proxy.conf.json chuyển `/api` sang Gateway 8080.
Không có JWT_SECRET trong FE.

TopicRequest chỉ có `topicCode`, `topicName`, `description`, `lecturerId`,
`maxStudents`, `field`. FE không gửi `currentStudents`, `status`, thời gian hay ID
khi tạo/sửa. Số sinh viên lấy từ phản hồi backend. Để xem kết quả duyệt đăng ký
mới nhất, nhấn **Làm mới** ở danh sách hoặc **Cập nhật số lượng** ở chi tiết.
Không gọi endpoint capacity nội bộ từ FE.

API đang có chưa cung cấp danh sách giảng viên cho dropdown. Giảng viên đăng
nhập được điền sẵn ID của mình ở form tạo. Admin nhập số ID của giảng viên,
là số sau dấu `#` trong trang Tổng quan của tài khoản đó. FE tra cứu role/status
khi lưu; backend kiểm tra lại để bảo vệ thao tác ghi.

## Các file

- `core/models/topic.models.ts`: contract dữ liệu.
- `core/services/topic.service.ts`: gọi API.
- `core/guards/topic-manager.guard.ts`: bảo vệ route tạo/sửa.
- `features/topics/topic-list.component.*`: danh sách, tìm kiếm/lọc, phân trang.
- `features/topics/topic-detail.component.*`: chi tiết, số lượng, quản lý.
- `features/topics/topic-form.component.*`: form tạo/sửa và validation.
- `features/topics/topic-delete-dialog.component.ts`: dialog chuẩn HTML, giữ focus
  trong hộp xác nhận, Escape để hủy khi chưa gửi xóa.
- `features/topics/topic.utils.ts`: nhãn trạng thái, quyền UI, lỗi HTTP, ID hợp lệ.
- `features/topics/topic-page.css`: kiểu giao diện dùng chung trong Topic.
- `app.routes.ts`: thêm bốn route Topic.
- `layout/shell/shell.component.html/.css`: thêm menu Đề tài và menu trên mobile.
- `features/dashboard/dashboard.component.html`: thêm lối vào nhanh Đề tài.

## Chạy trên máy

1. Khởi động MySQL, ServiceRegistryApplication (8761), AuthServiceApplication
   (8081), TopicServiceApplication (8082), ApiGatewayApplication (8080).
2. STS: Refresh project sau khi lấy code; cập nhật Maven nếu phần backend thay đổi.
3. Trong Git Bash:

```bash
cd ~/graduation-project-management-soa/frontend
npm ci
npm run build
npm start
```

Đợi từng lệnh kết thúc thành công rồi chạy lệnh tiếp theo. `npm start` tiếp tục
chạy trong terminal; mở `http://localhost:4200`, đăng nhập bằng tài khoản của nhóm
và vào **Đề tài**. Dừng FE bằng Ctrl+C.

Angular 20.3 hỗ trợ Node `^24.0.0`; Node 24.13.0 đã được kiểm tra từ máy của thành
viên đáp ứng dải phiên bản này. Giữ package.json/package-lock.json của nhóm.

## Kiểm tra đã thực hiện

- `npm ci`: cài đặt theo lockfile hiện tại, Angular 20.3.33 / TypeScript 5.8.3.
- `npm run build`: thành công, gồm kiểm tra strict templates.
- `npm start`: biên dịch thành công.
- Playwright: **20/20 kịch bản** qua trên bundle production. Dùng mock HTTP
  Gateway ở 8080 và proxy kiểm thử ở 4200 đọc target từ proxy.conf.json.
- Kiểm tra ảnh desktop 1440px và mobile 360px; các trang Topic không tràn ngang.
- Không có lỗi JavaScript của Angular trong các kịch bản kiểm tra.

Các kịch bản phủ: guest guard; login/student list; token và URL tương đối; tìm
không dấu/lọc/phân trang; route create/edit bị chặn với Student; detail/404/ID sai;
chuỗi chỉ gồm dấu cách; số lượng không nguyên; giảng viên sai role/inactive/không
tồn tại; mã trùng 409; tạo và ngăn submit lặp; sửa và giới hạn số lượng; chặn xóa
đề tài có sinh viên; cancel/Escape; xóa 204; xóa 409 với dữ liệu cũ; empty state;
503 và thử lại; Admin/cancel form; responsive; hồ sơ/logout cũ; 401/403; HTTP 400
khi lưu; làm mới số sinh viên sau thay đổi backend.

Chưa chạy các service Java/MySQL thật trong môi trường kiểm tra FE. Các bước
bên dưới cần thực hiện trên máy của nhóm trước khi tạo PR; kết quả mock không
chứng minh được security, giao dịch DB hoặc luồng Registration thực tế.

## Kiểm tra với backend thật trước khi PR

| Thao tác | Kết quả cần thấy |
| --- | --- |
| Login Student → Đề tài | Danh sách/chi tiết; không có nút tạo/sửa/xóa |
| Student mở `/topics/new`, `/topics/1/edit` | Trang không có quyền |
| Login Lecturer → Tạo đề tài | ID bản thân được điền; dữ liệu hợp lệ → 201 → chi tiết |
| Tạo lại mã đề tài đã có | 409, form vẫn giữ nội dung |
| Gán tài khoản Student hoặc giảng viên inactive | Lỗi dễ hiểu, chưa gửi POST/PUT |
| Sửa tên/lĩnh vực/số lượng | 200; currentStudents vẫn do backend quản lý |
| Giảm tối đa dưới số đã duyệt | Lỗi validation; nếu số lượng đổi sau khi tải form, xử lý lỗi 400 từ backend |
| Xóa đề tài trống rồi xác nhận | 204; đề tài biến mất, có thông báo thành công |
| Xóa đề tài có sinh viên đã duyệt | Nút bị khóa; nếu số lượng đổi sau khi tải trang, xử lý 409 |
| Mở đề tài không tồn tại | Thông báo 404, có đường quay về danh sách |
| Duyệt đăng ký từ luồng Registration rồi làm mới Topic | currentStudents cập nhật theo API backend |
| Tắt Topic hoặc Auth trong khi thao tác | Thông báo lỗi dịch vụ; thử lại sau khi service UP |
| Logout, token hết hạn, API trả 401/403 | Luồng login/forbidden hiện có tiếp tục hoạt động |

Nhánh phát triển: `feature/frontend-topics`. Sau khi kiểm tra trên máy, đồng bộ
code nhóm mới nhất, xử lý các thay đổi chung ở router/layout nếu có, rồi tạo PR
theo quy ước của nhóm.

```bash
git add frontend
git commit -m "feat(frontend): add topic management UI"
git fetch origin
git merge origin/main
git push -u origin feature/frontend-topics
```

Nếu đã có commit cục bộ, chỉ merge sau khi cây làm việc sạch hoặc đã lưu thay
đổi. Không chọn cả một phía khi hai nhánh cùng thêm route/menu: giữ các feature
của cả nhóm. Không commit node_modules, .angular, dist hay thông tin bí mật.

Tham khảo chính thức: https://angular.dev/reference/versions

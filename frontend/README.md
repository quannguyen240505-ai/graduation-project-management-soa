# Graduation SOA - Auth & API Gateway UI

Frontend Angular chỉ tập trung phần Quân phụ trách: Authentication, JWT, API Gateway, route guard và xử lý 401/403.

## API đang dùng

- `POST /api/auth/login` body `{ "userCode": "...", "password": "..." }`
- Login response cần có `accessToken` và `userId` (đúng theo Postman hiện tại của nhóm).
- `GET /api/auth/users/{userId}` để lấy `id`, `role`, `status`.
- `GET /api/users/me` chỉ dùng ở trang kiểm tra Gateway; UI hiển thị raw JSON nên không phụ thuộc DTO cụ thể.
- `GET /actuator/health` để demo public Gateway health.

## Chạy

Yêu cầu Gateway chạy ở `http://localhost:8080`.

```bash
npm install
npm start
```

Mở `http://localhost:4200`.

`proxy.conf.json` chuyển `/api/**` và `/actuator/**` sang Gateway :8080, vì vậy demo local không cần sửa CORS backend.

## Demo nhanh

1. Login bằng tài khoản đã có trong database.
2. Dashboard hiển thị `userId`, `role`, status và JWT claims.
3. Mở `Tài khoản & JWT` và bấm `Làm mới từ Auth Service`.
4. Mở `Kiểm tra Gateway`:
   - Public Health -> `/actuator/health`
   - Protected User Lookup -> `/api/auth/users/{id}`
   - Current User -> `/api/users/me`
5. Logout xóa session localStorage.

## Lưu ý

- FE chỉ decode JWT để trình diễn, không coi dữ liệu decode phía client là nguồn xác thực quyền.
- Quyền thật vẫn phải do API Gateway / backend kiểm tra.
- Chưa thêm màn hình Register vì các nguồn hiện có không cho biết chắc DTO body của `/api/auth/register`; không nên đoán contract API.
- `localStorage` phù hợp demo lớp học. Production cần cân nhắc cookie HttpOnly/BFF và threat model XSS.

## Ghép vào repo nhóm

Đặt thư mục này thành:

```text
graduation-project-management-soa/frontend/
```

Không thêm `frontend` vào Maven `<modules>` vì Angular chạy độc lập với Maven.

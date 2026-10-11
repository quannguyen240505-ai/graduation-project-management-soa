# Demo hai Topic Service và chuyển sang bản còn hoạt động

## Mục tiêu

Chạy cùng một Topic Service thành hai tiến trình độc lập ở 8082 và 8085.
Gateway tìm chúng theo tên `TOPIC-SERVICE` trên Eureka, phân phối request GET
và thử lại khi kết nối lỗi. Khi một tiến trình ngừng, bản còn lại tiếp tục phục vụ.
Đây là failover, phục hồi khả năng phục vụ. Eureka không tự khởi động JVM đã tắt.
Khi bật lại JVM bằng tay, Gateway sẽ tìm và sử dụng lại instance đó.

Nhánh thử nghiệm: `feature/topic-self-healing`.
Bản nền: `9053006c5d3bb09c6ab9b72be23cd818bd724b91`.

| Thành phần | Cổng | Profile khi demo |
|---|---:|---|
| Service Registry | 8761 | topic-failover-demo |
| Auth Service | 8081 | Cấu hình chạy hiện có |
| API Gateway | 8080 | topic-failover-demo |
| Topic Service 1 | 8082 | topic-failover-demo |
| Topic Service 2 | 8085 | topic-failover-demo |
| Angular, nếu mở | 4200 | Cấu hình hiện có |

Hai bản Topic dùng chung DB `graduation_management`, JWT_SECRET và issuer với
Auth/Gateway. Cả hai có `spring.application.name=topic-service`; instance ID
khác nhau: `topic-service:8082` và `topic-service:8085`.

## Các file bổ sung

- Ba file `application-topic-failover-demo.yml` trong Topic, Registry và Gateway.
- `TopicInstanceHeaderFilter.java`: header nhận diện bản xử lý, chỉ ở profile demo.
- `postman/TopicFailover.postman_collection.json`.
- `verification/topic-failover-check.cjs` (Node, không cần npm install).
- Tài liệu này.

Cấu hình mặc định, security, POM, Angular và SQL giữ nguyên. Không cần import lại
DB. Không tạo module Topic thứ hai: chạy cùng một main class hai lần ở hai cổng.
Profile Gateway giữ đủ năm route hiện có, chỉ thêm Retry cho GET của Topic.
POST/PUT/DELETE không được filter này tự gửi lại.

Profile demo: heartbeat 10 giây, lease 30 giây, Gateway đọc registry mỗi 5 giây;
tắt cache thứ hai của LoadBalancer vì Eureka đã có cache. Registry dùng eviction
5 giây và tắt self-preservation chỉ cho demo cùng máy. Mặc định không đổi.

## 1. Kiểm tra build trong STS

Dùng JDK 25. Nhấp phải project > Maven > Update Project để refresh nếu cần.
Nếu có Maven trong Git Bash, từ thư mục gốc chạy:

```bash
mvn -pl topic-service,service-registry,api-gateway -am package
```

Nếu Git Bash báo không có `mvn`, dùng STS: nhấp phải project gốc > Run As >
Maven build..., Goals `package`, Profiles để trống. Xem Console khi build lỗi;
không hạ Java/Spring hoặc đổi cấu hình Angular để chạy demo này.

## 2. Tạo bốn cấu hình demo trong STS

Mở **Run > Run Configurations... > Java Application**. Chọn cấu hình đã chạy
thành công của Registry rồi **Duplicate** để tạo `Registry-demo`.
Làm tương tự với Gateway và Topic. Bản sao giữ Project, Main class, Environment
và JDK 25 từ cấu hình cũ. Tạo hai bản sao Topic, đặt tên theo cổng.

Điền tab **Arguments > Program arguments**:

| Tên cấu hình | Nội dung Program arguments |
|---|---|
| Registry-demo | `--spring.profiles.active=topic-failover-demo` |
| Gateway-demo | `--spring.profiles.active=topic-failover-demo` |
| Topic-8082-demo | `--spring.profiles.active=topic-failover-demo --server.port=8082` |
| Topic-8085-demo | `--spring.profiles.active=topic-failover-demo --server.port=8085` |

Main class:

- Topic: `com.graduation.topic.TopicServiceApplication`.
- Registry: `com.graduation.registry.ServiceRegistryApplication`.
- Gateway: `com.graduation.gateway.ApiGatewayApplication`.

Topic-8085-demo phải có cùng Environment với Topic-8082-demo.
`JWT_SECRET` là giá trị đang dùng của Auth/Gateway/Topic, **không phải accessToken**.
`DB_PASSWORD` là mật khẩu MySQL. `DB_USERNAME` mặc định `root`, `DB_URL` mặc định
`jdbc:mysql://localhost:3306/graduation_management`. Nếu nhóm dùng DB khác, đặt
cùng DB_URL/DB_USERNAME/DB_PASSWORD ở cả hai bản. Không lưu secret vào source.

Bấm **Apply** cho từng cấu hình. Dừng các bản thường đang chiếm 8761, 8080 và
8082 trước khi chạy bản demo. Giữ Auth chạy 8081.

Chạy theo thứ tự: Registry-demo > Auth > Topic-8082-demo > Topic-8085-demo >
Gateway-demo. Console bản demo phải ghi profile `topic-failover-demo`.

Mở `http://localhost:8761`: TOPIC-SERVICE phải có hai instance UP, hai ID khác nhau.
Hai URL `http://localhost:8082/actuator/health` và
`http://localhost:8085/actuator/health` phải trả `{"status":"UP"}`.
Chờ thêm khoảng 10 giây sau khi đăng ký đủ để Gateway cập nhật.

## 3. Import Postman và đăng nhập

Import `postman/TopicFailover.postman_collection.json`.
Trong Variables của collection điền `userCode`, `password` của tài khoản test
hiện có. URL đã có các cổng mặc định. Chạy **00 - Đăng nhập qua Gateway**.
Script lưu `accessToken` ở collection variable; request Topic dùng Bearer token.
Dùng tài khoản STUDENT, LECTURER hoặc ADMIN có quyền đọc danh sách đề tài.

Chạy từng folder riêng: cần dừng/bật service giữa các folder, không chạy cả
collection một lượt. Nếu xuất collection lên Git, xóa token/password khỏi bản xuất.

## 4. Hai bản cùng chạy

Chạy folder **01 - Cả hai bản UP** bằng Collection Runner.
Bốn GET qua Gateway phải trả 200, quan sát được cả hai response header:

```text
X-Topic-Instance: topic-service:8082
X-Topic-Instance: topic-service:8085
```

Mỗi response có một ID, thứ tự ID đầu tiên có thể khác nhau.
Dữ liệu hai bản phải giống nhau. Nếu chỉ thấy một ID, kiểm tra Eureka/profile
rồi chờ Gateway cập nhật. Folder này lưu snapshot để đối chiếu các bước sau.

## 5. Dừng 8082 và kiểm tra chuyển sang 8085

Trong Console/Debug của STS, chọn đúng **Topic-8082-demo**, bấm vuông đỏ
**Terminate**. Giữ Topic-8085-demo, Registry, Gateway và Auth chạy.
Chạy ngay folder **02 - Chỉ còn bản 8085**.

Các GET qua `http://localhost:8080/api/topics` phải 200, chỉ có header 8085,
cùng dữ liệu như trước. Request không token vẫn phải 401.

Dừng trong STS có thể hủy đăng ký Eureka bình thường. Để mô phỏng sập đột ngột,
có thể dùng PowerShell dừng **chỉ PID đang nghe cổng 8082**:

```powershell
$topicProcessId = (Get-NetTCPConnection -LocalPort 8082 -State Listen |
    Select-Object -First 1).OwningProcess
if ($topicProcessId) { Stop-Process -Id $topicProcessId -Force }
```

Không dừng toàn bộ java.exe. Khi Eureka còn thông tin cũ, Retry GET giúp chọn
lại instance khi kết nối lỗi. Nếu request lỗi trong lúc chuyển, ghi lại status
và thời gian, kiểm tra nguyên nhân trước khi đánh dấu PASS.

Eureka có thể chưa mất 8082 ngay. Quan sát lại trong khoảng một phút hoặc lâu
hơn; thời gian phụ thuộc heartbeat cuối cùng, xử lý lease và chu kỳ cập nhật.
Lease 30 giây không có nghĩa Dashboard chắc chắn xóa đúng sau 30 giây.
Các request GET vẫn cần đạt ngay sau khi dừng nhờ Retry; không đợi Dashboard
xóa instance rồi mới kiểm tra chuyển sang bản còn chạy.

## 6. Bật lại 8082

Chạy lại **Topic-8082-demo**. Chờ Eureka có hai UP và Gateway cập nhật.
Chạy folder **03 - Bật lại bản 8082**. Header phải xuất hiện cả hai ID trở lại,
dữ liệu vẫn như snapshot ban đầu. Đây là bật JVM lại bằng tay.

## 7. Dừng cả hai Topic

Dừng đúng hai cấu hình Topic, giữ Gateway/Registry/Auth.
Chạy folder **04 - Cả hai bản ngừng**: Gateway trả 5xx trong giới hạn thời gian,
không trả 200 với dữ liệu giả. Có thể 500/502/503/504 khi registry còn metadata;
khi Gateway biết không còn instance, thường trả 503.

Kết thúc demo, bật lại Topic trước khi dùng FE.

## 8. Node helper (tùy chọn)

Tại thư mục gốc, dùng accessToken từ đăng nhập. Nhập token kín trong Git Bash:

```bash
read -r -s -p "Dán accessToken rồi Enter: " FAILOVER_TOKEN
export FAILOVER_TOKEN
```

Lệnh tương ứng từng trạng thái:

```bash
node verification/topic-failover-check.cjs both
node verification/topic-failover-check.cjs secondary
node verification/topic-failover-check.cjs restored
node verification/topic-failover-check.cjs unavailable
```

Chạy từng lệnh sau khi dừng/bật đúng service, không dán cả bốn lệnh cùng lúc.
Helper chỉ GET, kiểm tra health, status, header và dữ liệu trong mỗi lượt;
Postman đối chiếu dữ liệu cả trước/sau failover. Helper không in token,
không tự dừng tiến trình. Sau test:

```bash
unset FAILOVER_TOKEN
```

## 9. Bằng chứng demo và bàn giao

Giữ ảnh/video: Eureka hai UP; Gateway trả cả hai ID; dừng 8082 vẫn 200 với 8085;
bật lại 8082 có cả hai ID; dừng cả hai trả 5xx hữu hạn.
Hai tiến trình cùng máy minh họa lỗi một tiến trình. Máy, DB, Auth, Gateway và
Registry là những phụ thuộc chung, demo không xử lý lỗi toàn bộ máy.

Chạy cấu hình STS cũ không có profile demo để dùng mặc định. Filter header demo
sẽ không được đăng ký. Sau khi test trên MySQL của nhóm, commit/push lên nhánh
`feature/topic-self-healing` và gửi nhóm trưởng review trước khi merge.

## Tài liệu chính thức

- [Eureka](https://docs.spring.io/spring-cloud-netflix/reference/spring-cloud-netflix.html).
- [LoadBalancer và cache](https://docs.spring.io/spring-cloud-commons/reference/spring-cloud-commons/loadbalancer.html).
- [Gateway Retry](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux/gatewayfilter-factories/retry-factory.html).
- [Timeout theo route](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux/http-timeouts-configuration.html).

## Kiểm tra trước khi bàn giao gói

Build thành công Topic, Registry và Gateway bằng JDK 25 với POM hiện có.
10 test TopicCapacityServiceTest hiện có đều đạt; môi trường kiểm tra truyền
Mockito javaagent vào lệnh Maven, không sửa POM hoặc source test của nhóm.

Đã chạy các tiến trình Spring thật, dùng H2 chung ở chế độ MySQL và JWT HS256
tạm để kiểm tra luồng đọc. Kết quả:

| Trạng thái | Kết quả |
|---|---|
| Hai Topic UP | 12/12 GET 200, quan sát cả hai ID, cùng dữ liệu |
| Kill 8082 khi Eureka vẫn còn cả hai đăng ký | 12/12 GET 200 từ 8085, dữ liệu giữ nguyên |
| Bật lại 8082 bằng tay | 12/12 GET 200, quan sát cả hai ID trở lại |
| Dừng cả hai Topic | 3/3 GET trả 500 trong dưới một giây ở lần kiểm tra này |
| Topic chạy bằng profile mặc định | GET 200, không có header demo |
| Không token / endpoint capacity qua Gateway | 401 / 403 như security hiện có |

Installer đã được kiểm tra: đúng nhánh thêm bảy file; chạy lại/CRLF không trùng;
sai nhánh, baseline thay đổi hoặc file đích khác nội dung đều dừng trước khi ghi.
32 file nền không đổi. Collection đạt schema Postman v2.1; 24 script hợp lệ.

Đây chưa phải xác nhận MySQL và đăng nhập bằng tài khoản Auth thực tế của nhóm.
Thực hiện các bước Postman trên máy nhóm và giữ bằng chứng trước khi gửi PR.
Thời gian ở bảng là kết quả của môi trường kiểm tra, không phải cam kết thời gian
cho mọi máy hoặc mọi loại lỗi.

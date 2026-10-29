
 hệ thống quản lý nhân sự HRM. Hệ thống gồm API Gateway và các service nghiệp vụ chạy độc lập. Mã nguồn backend được quản lý trong repository `managementEmployee` và được chia theo nhánh, mỗi nhánh chứa một service.

## Các service

| Thành phần | Nhánh chứa mã nguồn | Cổng mặc định | Chức năng |
| --- | --- | ---: | --- |
| API Gateway | [`api-gateway`](https://github.com/Nguyendat0819/managementEmployee/tree/api-gateway) | `8088` | Cổng HTTP cho frontend, định tuyến request đến các service và xác thực JWT HS256 dùng chung. |
| Employee Service | [`employee-service`](https://github.com/Nguyendat0819/managementEmployee/tree/employee-service) | `8080` | Đăng nhập, tài khoản, hồ sơ nhân viên, hợp đồng, phòng ban, chức vụ, vai trò, quyền và menu. |
| Attendance Service | [`attendance-service`](https://github.com/Nguyendat0819/managementEmployee/tree/attendance-service) | `8081` | Chấm công vào/ra, lịch sử chấm công, ca làm và phân công ca. |
| Payroll Service | [`payroll-service`](https://github.com/Nguyendat0819/managementEmployee/tree/payroll-service) | `8082` | Cấu trúc lương, kỳ lương, phiếu lương, quy trình duyệt/chi trả và KPI công việc. |
| Leave Service | [`leave-service`](https://github.com/Nguyendat0819/managementEmployee/tree/leave-service) | `8083` | Đơn nghỉ phép, phê duyệt, số dư phép, loại nghỉ và ngày lễ. |
| Notification Service | [`notification-service`](https://github.com/Nguyendat0819/managementEmployee/tree/notification-service) | `8084` | Hộp thư thông báo trong ứng dụng, số lượng chưa đọc và trạng thái đã đọc. |

Các service nghiệp vụ dùng PostgreSQL và xác thực request bằng JWT. API Gateway không kết nối PostgreSQL. Frontend gọi Gateway tại `http://localhost:8088`; Gateway giữ các context path của service, ví dụ `/employee-service/**` và `/attendance-service/**`.

## Kiến trúc request

```text
Management Employee FE
        |
        v
API Gateway :8088
   |       |        |        |        |
   v       v        v        v        v
Employee Attendance Payroll  Leave  Notification
 :8080     :8081    :8082   :8083     :8084
```

## Công nghệ

- Java 17 và Spring Boot
- Spring MVC, Spring Security, JWT và Spring Data JPA
- PostgreSQL
- MapStruct và Lombok
- SpringDoc OpenAPI, Actuator và Micrometer

## Chạy local

### Yêu cầu

- Java 17
- PostgreSQL với schema/migration tương ứng cho các service
- Maven cho API Gateway; các service còn lại có Maven Wrapper (`./mvnw`)

### Cấu hình

Đặt các biến môi trường trong terminal hoặc cấu hình Run của IDE. Các service cần dùng cùng `APP_JWT_SECRET` để Gateway và backend xác thực cùng một token. Không dùng secret phát triển cho môi trường dùng chung hoặc production.

Ví dụ cấu hình cho PostgreSQL local:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/employee_service
export DB_USERNAME=postgres
export DB_PASSWORD='change-me'
export APP_JWT_SECRET='replace-with-a-long-random-secret'
```

Mỗi service có thể nhận cấu hình riêng như `SERVER_PORT`, `SERVER_CONTEXT_PATH`, `JPA_DDL_AUTO` và `INTERNAL_BASE_URL`. Kiểm tra `src/main/resources/application.yaml`, `src/main/resources/application-dev.yaml` và `.env.example` trong nhánh tương ứng. File `.env` không được tự nạp khi chạy Maven; hãy export biến môi trường hoặc khai báo trong IDE. Áp dụng các script trong `docs/migrations/` của từng service trước khi chạy nếu database chưa có schema cần thiết.

### Khởi động

Checkout từng nhánh service vào một thư mục riêng. Trong mỗi thư mục, chạy:

```bash
# API Gateway (nhánh api-gateway)
mvn spring-boot:run

# Các service nghiệp vụ
./mvnw spring-boot:run
```

Mỗi service cần chạy trong terminal riêng và phải được cấu hình cổng/context path theo bảng ở trên. Employee Service mặc định dùng `/employee-service`; Attendance, Payroll, Leave và Notification dùng các context path tương ứng. Sau khi khởi động, frontend gọi các API qua Gateway, ví dụ:

```text
http://localhost:8088/employee-service/api/auth/login
http://localhost:8088/attendance-service/api/attendance/check-in
http://localhost:8088/payroll-service/api/payroll-runs
http://localhost:8088/leave-service/api/leave-requests/me
http://localhost:8088/notification-service/api/notifications/me
```

Các service SpringDoc thường cung cấp Swagger UI tại `/swagger-ui.html` và OpenAPI JSON tại `/v3/api-docs`, có thêm context path của service. Ví dụ Employee Service: `http://localhost:8080/employee-service/swagger-ui.html`.

## Tìm mã nguồn theo nhánh

Các nhánh backend nằm trong cùng repository Git:

```text
git@github.com:Nguyendat0819/managementEmployee.git
```

| Mã nguồn | Repository | Nhánh |
| --- | --- | --- |
| API Gateway | `managementEmployee` | `api-gateway` |
| Employee Service | `managementEmployee` | `employee-service` |
| Attendance Service | `managementEmployee` | `attendance-service` |
| Payroll Service | `managementEmployee` | `payroll-service` |
| Leave Service | `managementEmployee` | `leave-service` |
| Notification Service | `managementEmployee` | `notification-service` |
| Frontend | `managementEmployeeFe` | `develop` |
| Thư viện UI dùng chung | `mfe-platform-libs-no-keycloak` | `develop` |

Clone một service cụ thể bằng:

```bash
git clone --branch employee-service git@github.com:Nguyendat0819/managementEmployee.git
```

Thay `employee-service` bằng tên nhánh cần lấy. Trong repository backend cũng có nhánh `develop` và `dat` chứa các phiên bản khác của Employee Service; nhánh dành riêng cho service hiện là `employee-service`.

Các nhánh được liệt kê là nhánh mã nguồn; thay đổi còn chưa commit trong working tree chỉ có trên máy đang làm việc và chưa xuất hiện trên remote.

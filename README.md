# EventHub - Campus Event Registration API

> **Project Code:** SBF_A_01  
> **Course/Assignment:** Spring Boot Foundation Assignment
> **Link GitHub:** https://github.com/phamhongle412004-bit/EventHub

---

## 1. Project Overview & Architecture (Tổng quan & Kiến trúc)

- **Mô tả:** Hệ thống Backend REST API quản lý sự kiện, người tham gia và đăng ký tham gia sự kiện cho trung tâm đào tạo.
- **Kiến trúc phân lớp (Layered Architecture):**
    - `Controller`: Tiếp nhận HTTP Request, validate DTO đầu vào, gọi Service và trả về DTO Response + HTTP Status phù hợp.
    - `Service`: Xử lý toàn bộ logic nghiệp vụ (Business Rules), quản lý Transaction (`@Transactional`), mã hóa mật khẩu, kiểm tra quyền.
    - `Repository`: Kế thừa `JpaRepository` của Spring Data JPA để tương tác với Cơ sở dữ liệu.
    - `Entity` & `DTO`: Tách biệt hoàn toàn JPA Entity (dưới Database) và DTO (API Contract giao tiếp ra bên ngoài).
    - `Security / JWT`: Xử lý xác thực qua `OncePerRequestFilter`, `AuthenticationManager`, và `JwtTokenProvider`.

### Diagram / Component Wiring (Sơ đồ luồng component)
`Client (Postman/Curl)` $\rightarrow$ `Security Filter (JWT)` $\rightarrow$ `Controller` $\rightarrow$ `Service` $\rightarrow$ `Repository` $\rightarrow$ `Database`

---

## 2. Technical Stack & Exact Versions (Phiên bản kỹ thuật)

- **Java Version:** 17 (hoặc 21 tùy máy bạn)
- **Spring Boot Version:** 3.2.x (hoặc phiên bản chính xác trong pom.xml của bạn)
- **Database:** PostgreSQL / MySQL / H2 (Chế độ file mode cho runtime, In-memory cho testing)
- **Build Tool:** Maven 3.9+
- **Security:** Spring Security, JJWT (hoặc Nimbus JOSE)

---

## 3. Prerequisites & Environment Configuration (Môi trường & Biến môi trường)

### Điều kiện tiên quyết:
- JDK 17+
- Maven 3.8+ (hoặc dùng Maven Wrapper `mvnw`)
- Docker (Tùy chọn cho việc đóng gói Container)

### Cấu hình biến môi trường (Environment Variables):
Ứng dụng không hard-code cấu hình nhạy cảm mà đọc qua các biến môi trường (mặc định cấu hình trong `application.yml`):

| Biến môi trường | Mô tả | Giá trị mặc định |
| --- | --- | --- |
| `SERVER_PORT` | Cổng chạy ứng dụng | `8080` |
| `SPRING_DATASOURCE_URL` | Đường dẫn kết nối DB | `jdbc:postgresql://localhost:5432/eventhub` |
| `SPRING_DATASOURCE_USERNAME` | Username DB | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Password DB | `123456` |
| `JWT_SECRET` | Khóa ký JWT (Base64) | `dGhpc2lzYXNlY3JldGtleWZvcmV2ZW50aHVic2VjdXJpdHlqd3R0b2tlbg==` |
| `JWT_EXPIRATION_MS` | Thời gian hết hạn Token (ms) | `86400000` (24h) |

---

## 4. Build, Test & Run Instructions (Hướng dẫn chạy lệnh)

### 4.1. Chạy tất cả bài Test
```powershell
.\mvnw.cmd test
```

### 4.2. Build file JAR thực thi
```powershell
.\mvnw.cmd clean package
```
(File .jar sẽ được tạo tại target/eventhub-0.0.1-SNAPSHOT.jar)

### 4.3. Chạy file JAR trực tiếp
```powershell
java -jar target/eventhub-0.0.1-SNAPSHOT.jar
```

### 4.4. Build&Chạy bằng Docker
```powershell
# Build Docker Image (dùng Dockerfile non-root)
docker build -t eventhub-api:1.0 .

# Chạy Docker Container
docker run -d -p 8080:8080 --name eventhub-app eventhub-api:1.0
```

## 5. Account Bootstrap & Authorization Matrix (Xác thực & Phân quyền)
Tài khoản Admin khởi tạo ban đầu (Development Seed):
- Email: admin@campus.edu.vn
- Password: AdminSecret@123
- Role: EVENT_ADMIN

Bảng phân quyền (Authorization Matrix):
| Endpoint | Method | Quyền truy cập (Authority) | Mô tả |
| --- | --- | --- | --- |

## 6. Business Rules & Transactions Summary (Tóm tắt quy tắc nghiệp vụ)
### 1. Đăng ký sự kiện (Registration):
- Sự kiện phải tồn tại, trạng thái phải là OPEN, thời gian bắt đầu ở tương lai.

- availableSeats > 0.

- Participant chưa có đăng ký ACTIVE cho sự kiện này.

- Transaction: Việc tạo dòng Registration và giảm availableSeats nằm trong cùng 1 Transaction (Rollback toàn bộ nếu 1 trong 2 thất bại).

### 2. Hủy đăng ký (Cancellation):
- Chỉ hủy lượt đăng ký ở trạng thái ACTIVE.

- Cập nhật trạng thái thành CANCELLED, gán cancelledAt và hoàn lại đúng 1 chỗ (availableSeats + 1).

- Đảm bảo tính Idempotent: Hủy nhiều lần không được cộng bù quá 1 chỗ.

## 7. API Error Contract (Chuẩn định dạng Lỗi)
Mọi lỗi trả về Client đều tuân theo cấu trúc JSON đồng nhất:
- 400 Bad Request: Lỗi Validation / Tham số không hợp lệ.
- 401 Unauthorized: Chưa đăng nhập / Token hết hạn hoặc không hợp lệ.
- 403 Forbidden: Không có quyền truy cập tài nguyên.
- 404 Not Found: Không tìm thấy Tài nguyên (Event, Participant, Registration).
- 409 Conflict: Đăng ký trùng lặp / Email đã tồn tại.
- 500 Internal Server Error: Lỗi hệ thống không xác định (ẩn chi tiết nhạy cảm khỏi Client).

## 8. Actuator Endpoints (Giám sát hệ thống)
- GET /actuator/health: Kiểm tra sức khỏe ứng dụng (Trả về {"status": "UP"}).
- GET /actuator/info: Hiển thị thông tin Build & Version dự án.
- GET /actuator/metrics: Xem danh sách các chỉ số bộ nhớ, CPU, JVM, HTTP Requests.

## 9. AI Assistance Disclosure (Khai báo và Sử dụng Trợ lý AI)

Trong quá trình thực hiện đồ án **EventHub (SBF_A_01)**, công cụ AI (Gemini / ChatGPT) đã được sử dụng như một cộng sự hỗ trợ học tập và phát triển theo các phạm vi cụ thể sau:

### 9.1. Các phần có sự hỗ trợ của AI:
1. **Giải thích Đề bài & Thiết kế Tài liệu:**
  - Dịch thuật và làm rõ các yêu cầu kỹ thuật trong tài liệu bài tập (Assignment Specifications).
  - Tối ưu hóa cấu trúc file `README.md`, chuẩn hóa bảng phân quyền (Authorization Matrix) và định dạng Error Contract.

2. **Hỗ trợ Debug & Sửa lỗi Môi trường:**
  - Phân tích nguyên nhân và đưa ra giải pháp khắc phục các lỗi phổ biến trong quá trình phát triển (ví dụ: lỗi trùng cổng `Port 8080 already in use`, lỗi parse chuỗi JSON `Unexpected end of JSON input`).
  - Hướng dẫn thao tác lệnh Terminal / PowerShell cho việc chạy file `.jar` và kiểm tra tiến trình ngầm (PID).

3. **Cấu hình Đóng gói & Hạ tầng (Packaging & Infrastructure):**
  - Hướng dẫn cú pháp cấu hình `Dockerfile` theo mô hình Multi-stage build (sử dụng Maven Builder + JDK Alpine).
  - Thiết lập cấu hình bảo mật Container chạy dưới quyền **Non-root user** (`appuser`) theo chuẩn yêu cầu của Task 7.

4. **Tham khảo Mẫu Thiết kế Code (Design Patterns):**
  - Tham khảo cấu trúc xử lý ngoại lệ tập trung bằng `@RestControllerAdvice` và `@ExceptionHandler`.
  - Tham khảo cấu hình mã hóa mật khẩu chuẩn BCrypt `PasswordEncoder` trong Spring Security.

### 9.2. Cam kết về Quyền sở hữu & Độ chính xác của Mã nguồn:
- **Kiểm thử độc lập:** Tất cả mã nguồn, cấu hình Docker, script chạy test và các bài Unit/Integration Test đều đã được thực thi, kiểm tra thủ công và vượt qua (PASS) trên môi trường phát triển local.
- **Thấu hiểu logic:** Học viên hoàn toàn hiểu rõ bản chất luồng đi của dữ liệu (Request flow), cơ chế quản lý Transaction, thuật toán mã hóa BCrypt, cơ chế verify chữ ký JWT cũng như lý do thiết lập từng Bean trong Spring Context.
- **Không sao chép thụ động:** AI chỉ đóng vai trò tư vấn, giải thích và gợi ý giải pháp; toàn bộ mã nguồn đóng gói nộp bài đều do học viên chủ động viết, tái cấu trúc (Refactor) và chịu trách nhiệm hoàn toàn về tính đúng đắn.
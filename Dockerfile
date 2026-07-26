# Stage 1: Build file JAR từ nguồn
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml và tải dependencies trước để tận dụng Docker Cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy mã nguồn và build dự án (bỏ qua test để build nhanh)
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Chạy ứng dụng với môi trường nhỏ gọn & an toàn
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Yêu cầu bảo mật: Tạo Non-root user để chạy app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy file .jar đã build từ Stage 1
COPY --from=builder /app/target/*.jar app.jar

# Phân quyền file jar cho non-root user
RUN chown appuser:appgroup app.jar

# Chuyển sang dùng non-root user
USER appuser

# Mở port 8080 (port mặc định của Spring Boot)
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
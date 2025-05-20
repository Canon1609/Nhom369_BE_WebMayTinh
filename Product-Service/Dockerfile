# Sử dụng image Java chính thức
FROM openjdk:17-jdk-slim

# Tạo thư mục để chứa ứng dụng
WORKDIR /app

# Copy file JAR vào container
COPY target/Product-Service-*.jar app.jar

# Expose cổng ứng dụng
EXPOSE 8082

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
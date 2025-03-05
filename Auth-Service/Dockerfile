# Sử dụng image Java 17 làm base image
FROM openjdk:17-jdk-slim


# Cài đặt Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean

    
# Đặt thư mục làm việc bên trong container
WORKDIR /app

# Copy file pom.xml và tải dependency trước để tận dụng Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn của dự án vào container
COPY src ./src

# Build ứng dụng Spring Boot thành file JAR
RUN mvn clean package -DskipTests

# Copy file JAR đã build vào container
# Giả sử file JAR được tạo ra trong thư mục target với tên user-service-0.0.1-SNAPSHOT.jar
RUN cp target/Authentication-Service-0.0.1-SNAPSHOT.jar app.jar

# Mở port 8080 cho ứng dụng (port mặc định của Spring Boot)
EXPOSE 8080

# Lệnh chạy ứng dụng khi container khởi động
ENTRYPOINT ["java", "-jar", "app.jar"]
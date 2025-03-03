# Auth Service - Quản lý xác thực người dùng

## Tổng quan
- **Mục đích**: Cung cấp chức năng đăng nhập, đăng ký và làm mới token cho hệ thống bán máy tính online.
- **Phiên bản**: v1.0.0
- **Ngôn ngữ lập trình**: Java
- **Framework**: Springboot
- **Dependencies chính**: MariaDB , JWT, bcrypt
- **Trạng thái**: Ổn định

## Chức năng chính
- Đăng ký tài khoản người dùng mới (Signup).
- Xác thực thông tin đăng nhập và cấp token (Login).
- Làm mới access token bằng refresh token (Refresh Token).

## Kiến trúc
- **Vị trí trong hệ thống**: Giao tiếp với User Service để lưu trữ thông tin người dùng và cung cấp token cho các service khác (Order Service, Product Service).
- **API**: RESTful
- **Database**: MariaDB (lưu thông tin người dùng và refresh token).

## Cài đặt
### Yêu cầu trước
- Java 17 (hoặc phiên bản tương thích với Spring Boot)
- MariaDB v10.4.32+
- npm hoặc yarn

### Hướng dẫn cài đặt
1. Clone repository:
   ```bash
   git clone https://github.com/Canon1609/Nhom369_BE_WebMayTinh.git
   cd Authentication-service
2. setup database
# application.properties
server.port=8080
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.datasource.url=jdbc:mariadb://localhost:3306/UserDB?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.properties.hibernate.show_sql=true
spring.jackson.serialization.FAIL_ON_EMPTY_BEANS=false
spring.jackson.default-property-inclusion=non_null
spring.jackson.deserialization.fail-on-unknown-properties=false
# Cấu hình JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
3. Build và chạy service
mvn clean install
java -jar target/auth-service-1.0.0.jar
### API Endpoint
1. Login
# POST /auth/login
- request
  {
  "username": "user1",
  "password": "pass123"
  }
- response
  {
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
  }
2. Signup
# POST /auth/signup
- request 
{ 
"username" : 'user123',
"password" : 'password',
"email" : 'abc@email.com'
} 
- response
{
   "message" : "đăng kí thành công",
   "username" : "user123"
}
3. Refresh
# POST /auth/refresh
- request
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
- response
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}


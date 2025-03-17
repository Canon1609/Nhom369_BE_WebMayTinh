Product Service API

Hướng dẫn sử dụng

1. Cài đặt

Clone repository:

git clone https://github.com/<username>/Nhom369_BE_WebMayTinh.git
cd Nhom369_BE_WebMayTinh/Product-Service

Cấu hình database trong application.properties:
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.datasource.url=jdbc:mariadb://localhost:3306/product_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=yourpassword

Chạy ứng dụng:

mvn spring-boot:run

2. API lấy danh sách sản phẩm

URL: GET /api/products

Response:
[
  {
    "id": 1,
    "name": "Laptop Dell XPS 15",
    "price": 35000000,
    "image": "dell_xps_15.jpg",
    "description": "Laptop Dell hiệu năng cao",
    "quantity": 10,
    "factory": "Dell"
  },
  {
    "id": 2,
    "name": "CPU Intel Core i9-12900K",
    "price": 12000000,
    "image": "intel_i9.jpg",
    "description": "CPU mạnh mẽ cho gaming",
    "quantity": 15,
    "factory": "Intel"
  }
]

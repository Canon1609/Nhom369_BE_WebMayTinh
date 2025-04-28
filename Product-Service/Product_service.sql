CREATE DATABASE product_db

USE product_db

INSERT INTO categories (name, description, image) VALUES
('Laptop', 'Các loại laptop văn phòng, gaming', 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/laptop.jpg'),
('Linh kiện PC', 'CPU, RAM, SSD, VGA', 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/cpui9.jpg'),
('Màn hình', 'Màn hình LCD, LED, gaming', 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/manhinh.jpg'),
('Chuột & Bàn phím', 'Chuột gaming, bàn phím cơ, phụ kiện nhập liệu', 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/banphim.jpg'),
('Thiết bị lưu trữ', 'Ổ cứng HDD, SSD, USB, thẻ nhớ', 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/ssd.jpg'),
('Phụ kiện khác', 'Tai nghe, webcam, đế tản nhiệt, balo laptop', 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/tainghe.jpg'),
('Điện thoại', 'Điện thoại thông minh các hãng Apple, Samsung, Xiaomi...', 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/iphone.jpg');


INSERT INTO products (name, price, image, description, quantity, factory, category_id) VALUES
('Laptop Dell XPS 15', 35000000, 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/laptop.jpg', 'Laptop Dell hiệu năng cao', 10, 'Dell', 1),
('CPU Intel Core i9-12900K', 12000000, 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/cpui9.jpg', 'CPU mạnh mẽ cho gaming', 15, 'Intel', 2),
('RAM Corsair 16GB DDR4', 1800000, 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/ram.jpg', 'Bộ nhớ RAM cho máy tính', 20, 'Corsair', 2),
('Màn hình ASUS ROG 27 inch', 8500000, 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/manhinh.jpg', 'Màn hình gaming 165Hz', 8, 'ASUS', 3),
('Chuột Logitech G502', 1500000, 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/chuot.jpg', 'Chuột gaming hiệu năng cao', 25, 'Logitech', 4),
('Bàn phím cơ AKKO 3068', 1800000, 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/banphim.jpg', 'Bàn phím cơ gọn nhẹ, phù hợp văn phòng & gaming', 18, 'AKKO', 4),
('SSD Samsung 970 EVO Plus 1TB', 2800000, 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/ssd.jpg', 'SSD tốc độ cao cho máy tính cá nhân', 12, 'Samsung', 5),
('USB Kingston 64GB', 250000, 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/usb.jpg', 'USB 3.1 tốc độ cao, dung lượng 64GB', 40, 'Kingston', 5),
('Tai nghe Razer Kraken X', 1250000, 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/tainghe.jpg', 'Tai nghe gaming nhẹ, âm thanh vòm 7.1', 10, 'Razer', 6),
('Webcam Logitech C920', 2200000, 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/webcam.jpg', 'Webcam Full HD 1080p cho học và làm việc online', 7, 'Logitech', 6),
('iPhone 14 Pro Max 256GB', 32000000, 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/iphone.jpg', 'Điện thoại cao cấp của Apple, hiệu năng mạnh mẽ', 5, 'Apple', 7),
('Samsung Galaxy S23 Ultra', 29000000, 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/dtsamsung.jpg', 'Flagship của Samsung với camera 200MP', 6, 'Samsung', 7),
('Xiaomi Redmi Note 13', 7500000, 'https://up-load-file-tranquocanh.s3.ap-southeast-2.amazonaws.com/xiaomi.jpg', 'Smartphone giá rẻ cấu hình ổn, pin trâu', 20, 'Xiaomi', 7);


SELECT * FROM categories;
SELECT * FROM products;



CALL API
------------------
POST http://localhost:8082/api/categories (Thêm category) 
Content-Type: application/json
{
"name": "Linh kiện",
"description": "Linh kiện máy tính"
}
tương tự như product
--------------
GET (Lấy danh sách products)
http://localhost:8082/api/products
response
{
"id": 1,
"name": "Laptop Dell XPS 15",
"price": 3.5E7,
"image": "dell_xps_15.jpg",
"description": "Laptop Dell hiệu năng cao",
"quantity": 10,
"factory": "Dell"
}
(lấy product theo id)
http://localhost:8082/api/products/1
----------------------------------------
POST (them sản phẩm)
http://localhost:8082/api/products
form-data
key: "image"(file)
value :"new-product.jpg",
content-type: multipart/form-data

key: "product" (text)
value: 
 {
"name": "Sản phẩm mới",
"price": 99.99,
"description": "Mô tả sản phẩm mới",
"quantity": 100,
"factory": "Nhà máy A",
"category": {
"id": 1 // ID của category đã tồn tại
}
}
content-type: application/json
-------------------------------
DELETE (xóa sản phẩm)
http://localhost:8082/api/products/1
---------------------------------
PUT (cập nhật sản phẩm)
http://localhost:8082/api/products/1
{
"name": "Laptop Dell XPS 15 update",
"price": 3.5E7,
"image": "dell_xps_15.jpg",
"description": "Laptop Dell hiệu năng cao",
"quantity": 10,
"factory": "Dell"
}
-----------------------------------------------new API------------------------
1. API Lọc và Sản Phẩm theo khoảng giá
   GET: 	http://localhost:8082/api/products/price-range?minPrice=1&maxPrice=10000

2. tìm kiếm sản phẩm theo từ khóa
   GET: 	http://localhost:8082/api/products/search?keyword=laptop

3. API Quản Lý Kho (nếu cần theo dõi số lượng tồn kho):
   Lấy thông tin tồn kho của một sản phẩm.

GET 	http://localhost:8082/api/products/9/inventory

Cập nhật thông tin tồn kho của một sản phẩm
PUT	http://localhost:8082/api/products/9/inventory
body
{
"quantity": 3636
}
=========================================Categories==========================
1. Thêm danh mục sản phẩm
POST http://localhost:8082/api/categories
key: "category", type: application/json
{
"name": "Linh kiện",
"description": "Linh kiện máy tính"
}
key: "image" (file)
value: "category.jpg",type: multipart/form-data
2. Lấy danh sách danh mục sản phẩm
GET http://localhost:8082/api/categories
- Get by id
GET http://localhost:8082/api/categories/1
3. xóa danh mục sản phẩm
DELETE http://localhost:8082/api/categories/1
4. cập nhật danh mục sản phẩm
PUT http://localhost:8082/api/categories/1
   key: "category", type: application/json
   {
   "name": "Linh kiện",
   "description": "Linh kiện máy tính"
   }
   key: "image" (file) (có thể không có) nếu không muốn cập nhật image
   value: "category.jpg",type: multipart/form-data

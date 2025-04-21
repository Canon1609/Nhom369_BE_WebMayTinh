CALL API
------------------
Content-Type: application/json
{
"name": "Linh kiện",
"description": "Linh kiện máy tính"
}
--------------
GET (Lấy danh sách products)
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
----------------------------------------
POST (them sản phẩm)
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
-------------------------------
DELETE (xóa sản phẩm)
---------------------------------
PUT (cập nhật sản phẩm)
{
"name": "Laptop Dell XPS 15 update",
"price": 3.5E7,
"image": "dell_xps_15.jpg",
"description": "Laptop Dell hiệu năng cao",
"quantity": 10,
"factory": "Dell"
}


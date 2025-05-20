package fit.iuh.controllers;

import dto.ChangePassworDto;
import fit.iuh.models.Address;
import fit.iuh.models.ProductFavorite;
import fit.iuh.models.User;
import fit.iuh.security.JwtUtil;
import fit.iuh.services.AddressService;
import fit.iuh.services.ProductFavotiteService;
import fit.iuh.services.S3Service;
import fit.iuh.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private AddressService  addressService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private S3Service s3Service;

    @Autowired
    private RestTemplate restTemplate;

    private final String PRODUCT_SERVICE_URL = "http://localhost:8082/api/products";
    @Autowired
    private ProductFavotiteService productFavotiteService;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getListUser(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Kiểm tra xem header có đúng định dạng không
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("message", "Thiếu hoặc sai định dạng Authorization header");
                return ResponseEntity.status(401).body(response);
            }
            // Lấy token từ header
            String token = authHeader.substring(7); // Bỏ qua "Bearer "
            // Trích xuất username từ token
            String username = jwtUtil.extractUsername(token);
            String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse(null);
            if (username == null) {
                response.put("message", "Token không hợp lệ hoặc hết hạn");
                return ResponseEntity.status(401).body(response);
            }
            if(role == null) {
                response.put("message", "Không tìm thấy role hoặc token hết hạn");
            }
            if(!role.equals("ADMIN")) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền lấy danh sách người dùng. Chỉ admin mới có quyền này.");
                return ResponseEntity.status(400).body(response);
            }
            // Tìm người dùng từ database
            List<User> listUsers = userService.getAllUsers();

            // Trả về thông tin người dùng
            Map<String, Object> userInfo = new HashMap<>();
            listUsers.forEach(user -> {
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("role", user.getRole());
                userInfo.put("email", user.getEmail());
//                userInfo.put("name", user.getName());
                userInfo.put("avt", user.getAvt());
                response.put("data", listUsers);
            });
            response.put("message", "lấy danh sách người dùng thành công");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint : /account
     * Xử lý yêu cầu cấp lại accessToken từ người dùng
     * Nhận thông tin accesstoken và refreshtoken từ người dùng
     * Gọi UserService để tìm kiếm người dùng thông qua refreshToken
     * Xác thưc refesh Token và cấp lại access token
     * @Param Map<accessToken , refreshToken >
     * @return ResponseEntity với thông báo cấp lại accessToken  thành công hoặc lỗi
     */
    @GetMapping("/account")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Kiểm tra xem header có đúng định dạng không
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("message", "Thiếu hoặc sai định dạng Authorization header");
                return ResponseEntity.status(401).body(response);
            }
            // Lấy token từ header
            String token = authHeader.substring(7); // Bỏ qua "Bearer "
            // Trích xuất username từ token
            String username = jwtUtil.extractUsername(token);
            if (username == null) {
                response.put("message", "Token không hợp lệ hoặc hết hạn");
                return ResponseEntity.status(401).body(response);
            }
//            Long userId = jwtUtil.extractId(token);
//            if (userId == null) {
//                response.put("message", "Token không hợp lệ hoặc hết hạn");
//                return ResponseEntity.status(401).body(response);
//            }
            // Tìm người dùng từ database
            User user = userService.finByUserName(username);
//            User user = userService.getUserById(userId);
            if (user == null) {
                response.put("message", "Người dùng không tồn tại");
                return ResponseEntity.status(404).body(response);
            }

            // Lấy thông tin các địa chỉ của người dùng
            List<Address> addresses = addressService.getAddressesByUserId(user.getId());

            // Trả về thông tin người dùng
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("id", user.getId());
            userInfo.put("avatar", user.getAvt());
            userInfo.put("phone", user.getPhone());
            userInfo.put("role", user.getRole());
            response.put("message" , "lấy thông tin người dùng thành công");
            // Tạo danh sách địa chỉ
            List<Map<String, String>> addressList = addresses.stream().map(address -> {
                Map<String, String> addressInfo = new HashMap<>();
                addressInfo.put("id", String.valueOf(address.getId()));
                addressInfo.put("street", address.getStreet());
                addressInfo.put("city", address.getCity());
                return addressInfo;
            }).toList();

            // Thêm địa chỉ vào thông tin người dùng
            userInfo.put("addresses", addressList);
            response.put("user", userInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authHeader , @RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Kiểm tra xem header có đúng định dạng không
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("message", "Thiếu hoặc sai định dạng Authorization header");
                return ResponseEntity.status(401).body(response);
            }
            // Lấy token từ header
            String token = authHeader.substring(7); // Bỏ qua "Bearer "
            // Trích xuất username từ token
            String username = jwtUtil.extractUsername(token);
            String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse(null);
            if (username == null) {
                response.put("message", "Token không hợp lệ hoặc hết hạn");
                return ResponseEntity.status(401).body(response);
            }
            if(role == null) {
                response.put("message" , "Role rỗng hoặc token hết hạn");
                return ResponseEntity.status(401).body(response);
            }
            if(!role.equals("ADMIN")) {
                response.put("message","Bạn không có quyền thêm người dùng mới , chỉ có admin mới có quyền thêm");
                return ResponseEntity.status(401).body(response);
            }
            String email = user.getEmail();

            User newUser = userService.addUser(user);
            if(newUser == null) {
                response.put("message","Thêm người dùng thất bại");
                return ResponseEntity.status(401).body(response);
            }
            response.put("message","thêm người dùng thành công");
            response.put("data", newUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<Object> updateUser(
            @PathVariable("id") Long id,
            @RequestBody User user
    ) {
        User existingUserOptional = userService.getUserById(id);
        if(existingUserOptional != null) {
//            user.setId(id);
            try {
                System.out.println("update user: " + user);
                existingUserOptional.setUsername(user.getUsername());
//                existingUserOptional.setEmail(user.getEmail());
                existingUserOptional.setPhone(user.getPhone());
                existingUserOptional.setRole(user.getRole());
                User saveUser;
                try{

                    saveUser = userService.savaUser(existingUserOptional);
                } catch (Exception e) {
                    return new ResponseEntity<>("Error saving user to database: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }

                return new ResponseEntity<>(saveUser, HttpStatus.OK);

            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/updateAvatar/{id}")
    public ResponseEntity<Object> updateAvatar(
            @PathVariable("id") Long id,
            @RequestPart(value = "image", required = false) MultipartFile image
    ){
        User existingUserOptional = userService.getUserById(id);
        if(existingUserOptional != null) {
            try {
                if (image != null && !image.isEmpty()) {
                    try {
                        String imageUrl = s3Service.uploadFile(image);
                        existingUserOptional.setAvt(imageUrl);
                    } catch (Exception e) {
                        return new ResponseEntity<>("Error uploading image to S3: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
                User saveUser;
                try{

                    saveUser = userService.savaUser(existingUserOptional);
                } catch (Exception e) {
                    return new ResponseEntity<>("Error saving user to database: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }

                return new ResponseEntity<>(saveUser, HttpStatus.OK);

            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint : /delete/{id}
     * Xử lý yêu cầu xóa người dùng trong hệ thống của admin
     * Nhận thông tin id của người dùng
     * Gọi UserService để tìm kiếm người dùng thông qua id
     * Nếu tồn tại người dùng thì gọi hàm xóa ngược lại trả về lỗi 404 cho client
     * @Param Long id
     * @return ResponseEntity với thông báo xóa user thành công hoặc lỗi
     */

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestHeader("Authorization") String authHeader, @PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        // Chuyển đổi id thành Long
        Long userId;
        // Lấy role từ SecurityContext
        String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse(null);
        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                response.put("status", "error");
                response.put("message", "Không có thông tin xác thực. Vui lòng đăng nhập lại.");
                return ResponseEntity.status(401).body(response);
            }
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("message", "Thiếu hoặc sai định dạng Authorization header");
                return ResponseEntity.status(401).body(response);
            }
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            userId = Long.parseLong(id);
            if (username == null) {
                response.put("message" , "Token không hợp lệ hoặc hết hạn");
            }
            if(!role.equals("ADMIN")) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền xóa người dùng. Chỉ admin mới có quyền này.");
                return ResponseEntity.status(400).body(response);
            }

            boolean isDelete =  userService.deleteUser(userId);
            if(isDelete) {
                response.put("status", "thành công");
                response.put("message" , "Xóa người dùng thành công");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi hệ thống" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getAddressByUser")
    public ResponseEntity<Map<String, Object>> getAddressByUser(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("message", "Thiếu hoặc sai định dạng Authorization header");
            }
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            if (username == null) {
                response.put("message" , "Token không hợp lệ hoặc hết hạn");
            }

            User user = userService.finByUserName(username);

            if (user == null) {
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            // Lấy các địa chỉ của người dùng từ cơ sở dữ liệu
            List<Address> addresses = addressService.getAddressesByUserId(user.getId());

//            response.put("user", user);
            response.put("addresses", addresses);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // API POST để thêm một địa chỉ cho người dùng
    @PostMapping("/addAddress")
    public ResponseEntity<Map<String, Object>> addAddress(@RequestHeader("Authorization") String authHeader,
                                                          @RequestBody Address addressRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("message", "Thiếu hoặc sai định dạng Authorization header");
            }
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            if (username == null) {
                response.put("message" , "Token không hợp lệ hoặc hết hạn");
            }

            User user = userService.finByUserName(username);

            if (user == null) {
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            // Gán user vào địa chỉ mới
            addressRequest.setUser(user);

            // Lưu địa chỉ vào cơ sở dữ liệu
            Address savedAddress = addressService.saveAddress(addressRequest);

            response.put("message", "Address added successfully");
            response.put("address", savedAddress);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    // API DELETE để xóa một địa chỉ
    @DeleteMapping("/deleteAddress/{addressId}")
    public ResponseEntity<Map<String, Object>> deleteAddress(@RequestHeader("Authorization") String authHeader,
                                                             @PathVariable Long addressId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("message", "Thiếu hoặc sai định dạng Authorization header");
            }
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            if (username == null) {
                response.put("message" , "Token không hợp lệ hoặc hết hạn");
            }

            User user = userService.finByUserName(username);

            if (user == null) {
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            // Kiểm tra xem địa chỉ có tồn tại và thuộc về người dùng không
            Address address = addressService.getAddressById(addressId);

            if (address == null) {
                response.put("message", "Address not found");
                return ResponseEntity.status(404).body(response);
            }

            if (address.getUser().getId() != user.getId()) {
                response.put("message", "Unauthorized: You do not have permission to delete this address");
                return ResponseEntity.status(403).body(response);
            }

            // Xóa địa chỉ
            addressService.deleteAddress(addressId);

            response.put("message", "Address deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    // API PUT để cập nhật địa chỉ
    @PutMapping("/updateAddress/{addressId}")
    public ResponseEntity<Map<String, Object>> updateAddress(@RequestHeader("Authorization") String authHeader,
                                                             @PathVariable Long addressId,
                                                             @RequestBody Address addressRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("message", "Thiếu hoặc sai định dạng Authorization header");
            }
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            if (username == null) {
                response.put("message" , "Token không hợp lệ hoặc hết hạn");
            }

            User user = userService.finByUserName(username);
            if (user == null) {
                response.put("message", "Người dùng không tồn tại");
                return ResponseEntity.status(404).body(response);
            }

            // Lấy địa chỉ hiện tại từ cơ sở dữ liệu
            Address address = addressService.getAddressById(addressId);
            if (address == null) {
                response.put("message", "Địa chỉ không tồn tại");
                return ResponseEntity.status(404).body(response);
            }

            // Kiểm tra xem địa chỉ có thuộc về người dùng này không
            if (address.getUser().getId() != user.getId()) {
                response.put("message", "Bạn không có quyền sửa địa chỉ này");
                return ResponseEntity.status(403).body(response);
            }

            // Cập nhật thông tin địa chỉ
            address.setStreet(addressRequest.getStreet());
            address.setCity(addressRequest.getCity());

            // Lưu cập nhật vào cơ sở dữ liệu
            addressService.saveAddress(address);

            response.put("message", "Cập nhật địa chỉ thành công");
            response.put("address", address);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    // API POST để thêm sản phẩm yêu thích cho người dùng
    @PostMapping("/addFavoriteProduct/{productId}")
    public ResponseEntity<Map<String, Object>> addFavoriteProduct(@RequestHeader("Authorization") String authHeader,
                                                                  @PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("message", "Thiếu hoặc sai định dạng Authorization header");
            }
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            if (username == null) {
                response.put("message" , "Token không hợp lệ hoặc hết hạn");
            }

            User user = userService.finByUserName(username);
            if (user == null) {
                response.put("message", "Người dùng không tồn tại");
                return ResponseEntity.status(404).body(response);
            }

            // Gọi dịch vụ sản phẩm để lấy thông tin sản phẩm
//            String productUrl = PRODUCT_SERVICE_URL + favoriteProductRequest.getProductId();
//            ProductFavorite product = restTemplate.getForObject(productUrl, ProductFavorite.class);
            ProductFavorite product = restTemplate.getForObject(PRODUCT_SERVICE_URL + "/" + productId, ProductFavorite.class);
            System.out.println("Product: " + product);

            if (product == null) {
                response.put("message", "Sản phẩm không tồn tại");
                return ResponseEntity.status(404).body(response);
            }

            // Tạo một sản phẩm yêu thích mới
            ProductFavorite favoriteProduct = new ProductFavorite();
            favoriteProduct.setUser(user);
            favoriteProduct.setProductId(productId);
            favoriteProduct.setName(product.getName());
            favoriteProduct.setPrice(product.getPrice());

            System.out.println("Product Name: " + product.getName());



            // Lưu sản phẩm yêu thích vào cơ sở dữ liệu
            ProductFavorite savedFavoriteProduct = productFavotiteService.saveFavoriteProduct(favoriteProduct);

            response.put("message", "Sản phẩm yêu thích đã được thêm thành công");
            response.put("favoriteProduct", savedFavoriteProduct);
            return ResponseEntity.status(201).body(response);

        } catch (Exception e) {
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getUserById/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById( @PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {

            // Lấy thông tin người dùng theo ID
            User foundUser = userService.getUserById(userId);
            if (foundUser == null) {
                response.put("message", "Người dùng không tồn tại");
                return ResponseEntity.status(404).body(response);
            }

            response.put("message", "Lấy thông tin người dùng thành công");
            response.put("user", foundUser);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/changePassword")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestHeader("Authorization") String authHeader,
                                                              @RequestBody ChangePassworDto request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("message", "Thiếu hoặc sai định dạng Authorization header");
                return ResponseEntity.status(401).body(response);
            }
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            if (username == null) {
                response.put("message" , "Token không hợp lệ hoặc hết hạn");
            }

            User user = userService.finByUserName(username);
            if (user == null) {
                response.put("message", "Người dùng không tồn tại");
                return ResponseEntity.status(404).body(response);
            }
            String oldPassword = request.getOldPassword();
            String newPassword = request.getNewPassword();
            String confirmPassword = request.getConfirmPassword();
            // Kiểm tra mật khẩu mới và xác nhận mật khẩu có giống nhau không
            if (!newPassword.equals(confirmPassword)) {
                response.put("message", "Mật khẩu mới và xác nhận mật khẩu không giống nhau");
                response.put("status", "error");
                return ResponseEntity.status(400).body(response);
            }
//             Kiểm tra mật khẩu cũ có đúng không
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                response.put("message", "Mật khẩu cũ không đúng");
                response.put("status", "error");
                return ResponseEntity.status(400).body(response);
            }

            // Cập nhật mật khẩu mới
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.savaUser(user);

            response.put("message", "Đổi mật khẩu thành công");
            response.put("status", "success");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

}

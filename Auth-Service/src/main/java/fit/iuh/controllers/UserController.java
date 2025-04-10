package fit.iuh.controllers;

import fit.iuh.models.User;
import fit.iuh.security.JwtUtil;
import fit.iuh.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
            if(!role.equals("ROLE_ADMIN")) {
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
                userInfo.put("name", user.getName());
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
            // Tìm người dùng từ database
            User user = userService.finByUserName(username);
            if (user == null) {
                response.put("message", "Người dùng không tồn tại");
                return ResponseEntity.status(404).body(response);
            }
            // Trả về thông tin người dùng
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("id", user.getId());
            userInfo.put("avatar", user.getAvt());
            response.put("message" , "lấy thông tin người dùng thành công");
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
            if(!role.equals("ROLE_ADMIN")) {
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
    @PatchMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUserInfo(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Object> updates) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> userInfo = new HashMap<>();
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
            System.out.println(user);
            if (user == null) {
                response.put("message" , "Không tìm thấy người dùng để cập nhật");
            }
            if(updates.get("id") != null) {
                response.put("status", "error");
                response.put("message", "Không được phép cập nhật ID của người dùng");
                return ResponseEntity.status(400).body(response);
            }
            if(updates.get("email") != null) {
                String newEmail = (String) updates.get("email");
                user.setEmail(newEmail);
            }
            if(updates.get("avt") != null) {
                String newAvt = (String) updates.get("avt");
                user.setAvt(newAvt);
            }
            if(updates.get("username") != null) {
                String newUsername = (String) updates.get("username");
                User isExistUser = userService.finByUserName(newUsername);
                if(isExistUser != null) {
                    response.put("message" , "tên người dùng đã tồn tại");
                    return ResponseEntity.status(400).body(response);
                }
            }
            if(updates.get("address") != null) {
                String newAddress = (String) updates.get("address");
                user.setAddress(newAddress);
            }
            if(updates.get("phone") != null) {
                String newPhone = (String) updates.get("phone");
                user.setPhone(newPhone);
            }
            if (updates.get("password") != null) {
                String newPassword = (String) updates.get("password");
                user.setPassword(newPassword);
            }
            if(updates.get("name") != null) {
                String newName = (String) updates.get("name");
                user.setName(newName);
            }
            userService.updateUser(user);
            // tạo user DTO trả về cho client
            userInfo.put("name" , user.getName());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("address", user.getAddress());
            userInfo.put("phone", user.getPhone());
            userInfo.put("avt", user.getAvt());


            response.put("status", "success");
            response.put("message", "Cập nhật thông tin thành công");
            response.put("data", userInfo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Lỗi hệ thống" + e.getMessage());
            return ResponseEntity.status(500).body(response);
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
            if(!role.equals("ROLE_ADMIN")) {
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

}

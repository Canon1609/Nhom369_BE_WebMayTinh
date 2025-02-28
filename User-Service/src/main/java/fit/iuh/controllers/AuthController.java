package fit.iuh.controllers;

import fit.iuh.models.User;
import fit.iuh.security.JwtUtil;
import fit.iuh.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    /**
     * Endpoint : POST /auth/signup
     * Xử lý yêu cầu đăng ký người dùng mới
     * Nhận thông tin user từ request body (username , password , address , phone , ... )
     * Gọi UserService để lưu user vào db
     * @Param user Đối tượng User chứa thông tin từ client
     * @return ResponseEntity với thông báo thành công hoặc lỗi
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User user){
        try{
            // kiểm tra validation
            if(user.getUsername() == null || user.getPassword()== null){
                return ResponseEntity.status(400).body("Tài khoản và mật khẩu không được trống");
            }
            // Gọi UserService để đăng kí user
            userService.signup(user);
            return ResponseEntity.ok("Đăng kí tài khoản thành công");
        }catch (IllegalArgumentException e){
            // xử lý nếu user đã tồn tại
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
//    @PostMapping("/login")
//    public ResponseEntity<Map<String, String>> login(@RequestBody User user) {
//        try {
//            if (user.getUsername() == null || user.getPassword() == null) {
//                return ResponseEntity.status(400).body(null);
//            }
//            User existingUser = userService.finByUserName(user.getUsername());
//            if (existingUser == null) {
//                return ResponseEntity.status(401).body(null);
//            }
//            if (userService.checkPassword(user.getPassword(), existingUser.getPassword())) {
//                String accessToken = jwtUtil.generateAccessToken(user.getUsername());
//                String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
//                userService.updateRefreshToken(user.getUsername(), refreshToken);
//                Map<String, String> tokens = new HashMap<>();
//                tokens.put("accessToken", accessToken);
//                tokens.put("refreshToken", refreshToken);
//                return ResponseEntity.ok(tokens);
//            } else {
//                return ResponseEntity.status(401).body(null);
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(null);
//        }
//    }
    @GetMapping("/")
    public ResponseEntity<String> Home(){
        return ResponseEntity.ok().body("Hello");
    }
}

package fit.iuh.controllers;

import fit.iuh.models.User;
import fit.iuh.security.JwtUtil;
import fit.iuh.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
    @ResponseBody
    public ResponseEntity<Map<String ,String>> signup(@RequestBody User user){
        Map<String, String> response = new HashMap<>();
        try{
            // kiểm tra validation
            if(user.getUsername() == null || user.getPassword()== null){
                response.put("message", "Tài khoản và mật khẩu không được trống");
                return ResponseEntity.status(400).body(response);
            }
            // Gọi UserService để đăng kí user
            userService.signup(user);
            response.put("message" , "Đăng kí tài khoản thành công");
            response.put("username" , user.getUsername());
            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException e){
            // xử lý nếu user đã tồn tại
            response.put("message" , e.getMessage());
            return ResponseEntity.status(400).body( response);
        } catch (Exception e) {
            response.put("message" , e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
     /**
     * Endpoint : POST /login
     * Xử lý yêu cầu đăng nhập của người dùng
     * Nhận thông tin user từ request body (username , password)
     * Gọi UserService để tìm kiếm người dùng trong db và trả về kết quả
     * @Param user Đối tượng User chứa thông tin từ client
     * @return ResponseEntity với thông báo đăng nhập thành công hoặc lỗi
     */
   @PostMapping("/login")
   public ResponseEntity<Map<String, String>> login(@RequestBody User user) {
       Map<String, String> response = new HashMap<>();
       try {
        // kiểm tra username hoặc password có bị null không 
           if (user.getUsername() == null || user.getPassword() == null) {
               response.put("message" , "Tài khoản và mật khẩu không được bỏ trống");
               return ResponseEntity.status(400).body(response);
           }
           // kiểm tra user có trong db không
           User existingUser = userService.finByUserName(user.getUsername());
           if (existingUser == null) {
               response.put("message" , "Tài khoản hoặc mật khẩu không chính xác");
               return ResponseEntity.status(401).body(response);
           }
           if (userService.checkPassword(user.getPassword(), existingUser.getPassword())) {
               String accessToken = jwtUtil.generateAccessToken(user.getUsername());
               String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
               userService.updateRefreshToken(user.getUsername(), refreshToken);
               Map<String, String> tokens = new HashMap<>();
               tokens.put("accessToken", accessToken);
               tokens.put("refreshToken", refreshToken);
               return ResponseEntity.ok(tokens);
           } else {
               response.put("message","");
               return ResponseEntity.status(401).body(null);
           }
       } catch (Exception e) {
           response.put("message" , e.getMessage());
           return ResponseEntity.status(500).body(response);
       }
   }
    /**
     * Endpoint : /refresh
     * Xử lý yêu cầu cấp lại accessToken từ người dùng
     * Nhận thông tin accesstoken và refreshtoken từ người dùng
     * Gọi UserService để tìm kiếm người dùng thông qua refreshToken
     * Xác thưc refesh Token và cấp lại access token
     * @Param Map<accessToken , refreshToken >
     * @return ResponseEntity với thông báo cấp lại accessToken  thành công hoặc lỗi
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
       Map<String, String> response = new HashMap<>();
        try {
            // lấy refreshToken t request
            String refreshToken = request.get("refreshToken");
            // kiểm tra token có null không
            if (refreshToken == null) {
                response.put("message","yêu cầu không hợp lệ");
                return ResponseEntity.status(400).body(response);
            }
            // tìm kiếm user theo refreshtoken
            User user = userService.findByRefreshToken(refreshToken);
            if (user == null) {
                response.put("message" , "không tìm thấy người dùng");
                return ResponseEntity.status(403).body(response);
            }
            // xác thực refreshToken và tạo accesstoken mới
            if (jwtUtil.validateToken(refreshToken, user.getUsername())) {
                String newAccessToken = jwtUtil.generateAccessToken(user.getUsername());
                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", newAccessToken);
                tokens.put("refreshToken", refreshToken);
                return ResponseEntity.ok(tokens);
            } else {
                response.put("message","refresh token không hợp lệ");
                return ResponseEntity.status(403).body(response);
            }
        } catch (Exception e) {
            response.put("message" , e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
  
}

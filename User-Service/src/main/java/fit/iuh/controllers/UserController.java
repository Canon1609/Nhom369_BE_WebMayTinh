package fit.iuh.controllers;


import fit.iuh.DTO.UserDTO;
import fit.iuh.models.Role;
import fit.iuh.models.User;
import fit.iuh.services.RoleService;
import fit.iuh.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;
    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String , Object>> index(){
        Map<String , Object> res = new HashMap<>();
        try{
            List<User> listUser = userService.getAllUsers();

            res.put("message" , "Lấy danh sách người dùng thành công ");
            res.put("data" , listUser);
        } catch (Exception e) {
            res.put("message" , e.getMessage());
        }
        return ResponseEntity.ok(res);
    };
    /*
        Hàm lấy thông tin của người dùng dựa vào token

     */

    @GetMapping("/me")
    public ResponseEntity<Map<String , Object>> myProfile(@RequestAttribute("token") String token){
        Map<String , Object> res = new HashMap<>();
        try {
            User user = userService.getUserInfo(token);
            UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),user.getRoles(),user.getPhone());
            res.put("message" , "Lấy thông tin người dùng thành công");
            res.put("data" , userDTO);

        }catch (Exception e) {
            res.put("message" , e.getMessage());
        }
        return ResponseEntity.ok(res);

    }
    /*
     * Hàm thêm mới user 
     */
    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String , Object>> createUser(@RequestBody User user){
        Map<String , Object> res = new HashMap<>();
        try {
            if(user.getEmail() == null || user.getPassword() == null || user.getFullName() == null || user.getPhone() == null){
                res.put("message" , "không được bỏ trống các trường dữ liệu");
                ResponseEntity.status(400).body(res);
            }
            // kiểm tra tồn tại
            boolean isExist = userService.isExistingEmail(user.getEmail());
            if(isExist){
               res.put("message" , "Email đã tồn tại");
               return ResponseEntity.status(400).body(res);
            }
            // mã hóa mật khẩu 
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if(user.getRoles() == null || user.getRoles().isEmpty()){
                Set<Role> listRoles = new HashSet<>();
                 Role defaultRole =  roleService.findbyName("user")
                     .orElseThrow(() -> new RuntimeException("Role mặc định không tồn tại"));
                listRoles.add(defaultRole);
            }
           boolean isAddUser = userService.addUser(user);
            if(isAddUser){
                UserDTO resUser = new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),user.getRoles(),user.getPhone());
                res.put("message" , "Tạo người dùng thành công");
                res.put("data" , resUser);
                return ResponseEntity.status(200).body(res);
            }
        } catch (Exception e) {
            res.put("message" , e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
        return ResponseEntity.ok(res);
    }
}

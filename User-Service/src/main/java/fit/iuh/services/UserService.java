package fit.iuh.services;

import fit.iuh.models.User;
import fit.iuh.repositories.UserRepository;
import fit.iuh.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    };

    public void updateRefreshToken(String username, String refreshToken) {
        User user =  userRepository.findByUsername(username);
        if (user != null) {
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        }
    }
    public boolean isAdmin(String token) {
        String userName = jwtUtils.extractUsername(token);
        User user = userRepository.findByUsername(userName);
        String role = user.getRoles().toString();
        System.out.println(role);
        role.equals("admin");
        return user != null && "admin".equals(user.getRoles());
    }
    public User getUserById(int id) {
        User user = userRepository.getById(id);
        return user;
    };


    public boolean addUser(User user)  {
            try{
                userRepository.save(user);
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }
    public void updateUser(int id) {
        User user = userRepository.getById(id);
        if(user != null) {
            userRepository.save(user);
        }
    }
    public boolean deleteUser(int id) {
        User user = userRepository.getById(id);
        if(user != null) {
            userRepository.delete(user);
            return true;
        }
        return false;
    }

    public boolean isExistingEmail(String email) {
        User isExistUser = userRepository.getUsersByEmail(email);
        if(isExistUser != null) {
            return true;
        }
        return false;
    }


    // Hàm lấy thông tin cá nhân
    public User getUserInfo(String token) {
        String username = jwtUtils.extractUsername(token);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tim thấy người dùng");
        }
        return user;
    }
    public User finbyUsername(String username) {
        return userRepository.findByUsername(username);
    }
}

package fit.iuh.services;


import fit.iuh.models.User;
import fit.iuh.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     *Phương thức signup : Đăng kí người dùng mới
     * Nhận một đối tượng User từ controller (chưa thông tin đăng kí như username , password , address , phone , v.v)
     * Mã hóa mật khẩu trước khi lưu
     * Lưu vào db thông qua repository
     * @Param user đối tượng chứa thông tin người dùng
     * @return đối tượng user đã được lưu
     */
    public User signup(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Người dùng đã tồn tại ");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }


    /** Phương thức findByUsename : tìm người dùng theo username
     * Được sử dụng trong quá trình login để kiểm tra xem username có tồn tại không
     * gọi phương thức findByUserName từ UserRepository
     * @Param username Username cần tìm
     * @return Đối tượng User nếu tìm thấy hoặc null nếu không tìm thấy
     *
     */
    public User finByUserName (String userName) {
        return userRepository.findByUsername(userName);
    }
    /**
     * Phương thức checkPassword : So sánh mk người dùng nhập với mk đã hash trong db có trùng khớp với nhau không
     * Sử dụng trong quá trình login để xác thực thông tin đăng nhập
     * @Param rawPassword mật khẩu thô do người dùng nhập
     * @Param encodePassword mật khẩu đã mã hóa lấy từ db
     * @return true nếu mật khẩu khớp , false nếu không khớp
     */
    public boolean checkPassword(String rawPassword , String encodedPassword){
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Phương thức updateRefreshToken : cập nhật Refresh Token cho người dùng
     * Được gọi sau khi  tạo Reresh token
     * Tìm user theo username và cập nhật trường refreshToken trong db'
     * @Param username Username của người dùng
     * @Param refreshToken Refresh Token mới cần lưu
     */
    public void updateRefreshToken(String userName , String refreshToken){
        User user = userRepository.findByUsername(userName);
        if(user != null){
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        }
        else{
            throw new IllegalArgumentException("User not found");
        }
    }
    /**
     * Phương thức findByRefreshToken: Tìm người dùng theo Refresh Token
     * - Được sử dụng trong quá trình refresh token để tìm user tương ứng
     * - Duyệt qua danh sách user để tìm user có refreshToken khớp
     * @param refreshToken Refresh Token cần tìm
     * @return Đối tượng User nếu tìm thấy, hoặc null nếu không tìm thấy
     */
    public User findByRefreshToken(String refreshToken) {
        // Duyệt qua tất cả user trong database để tìm user có refreshToken khớp
        return userRepository.findAll().stream()
                .filter(user -> refreshToken.equals(user.getRefreshToken()))
                .findFirst()
                .orElse(null);
    }
}

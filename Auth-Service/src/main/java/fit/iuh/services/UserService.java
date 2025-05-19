package fit.iuh.services;


import fit.iuh.models.Address;
import fit.iuh.models.User;
import fit.iuh.repositories.AddressRepository;
import fit.iuh.repositories.UserRepository;
import fit.iuh.security.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private JavaMailSender mailSender;
    @Value("${app.base-url}")
    private String baseUrl;
    /**
     *Phương thức signup : Đăng kí người dùng mới
     * Nhận một đối tượng User từ controller (chưa thông tin đăng kí như username , password , address , phone , v.v)
     * Mã hóa mật khẩu trước khi lưu
     * Tạo refresh token
     * Set role mặc định là USER
     * Lưu vào db thông qua repository
     * @Param user đối tượng chứa thông tin người dùng
     * @return đối tượng user đã được lưu
     */
    public User signup(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Người dùng đã tồn tại ");
        }
        if(userRepository.findByEmail(user.getEmail()) != null){
            throw new IllegalArgumentException("Email đã tồn tại ");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setVerificationCode(UUID.randomUUID().toString()); // tạo mã xác thực
        user.setRefreshToken(jwtUtil.generateRefreshToken(user.getUsername()));
        User savedUser =  userRepository.save(user);
        sendVerificationEmail(savedUser.getEmail(), savedUser.getVerificationCode());
        return savedUser;
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
    public User updateUser (User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User savaUser(User user){
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean deleteUser (Long id){
        User user = userRepository.findById(id).orElse(null);
        userRepository.delete(user);
        return true;
    }

    public User addUser (User user){
        String email = user.getEmail();
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Người dùng đã tồn tại ");
        }
        User existEmail  = userRepository.findByEmail(email);
        if(existEmail != null){
            throw new IllegalArgumentException("Email đã tồn tại ");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Gửi email xác thực tới người dùng.
     * @param email Địa chỉ email của người dùng
     * @param verificationCode Mã xác thực
     * @throws RuntimeException Nếu gửi email thất bại
     */
    public void sendVerificationEmail(String email, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            // Thiết lập thông tin email
            helper.setFrom("phuochuynguyen1002@gmail.com"); // Phải khớp với spring.mail.username
            helper.setTo(email);
            helper.setSubject("Xác thực tài khoản của bạn");
            // Nội dung email
            String verificationLink = baseUrl + "/verify?code=" + verificationCode;
            String emailContent = "Vui lòng nhấp vào liên kết bên dưới để xác thực tài khoản:\n" + verificationLink;
            helper.setText(emailContent, false); // Plain text
            // Gửi email
            mailSender.send(message);
        } catch (MessagingException e) {

            throw new RuntimeException("Failed to configure email: " + e.getMessage(), e);
        } catch (MailAuthenticationException e) {

            throw new RuntimeException("SMTP authentication failed: " + e.getMessage(), e);
        } catch (Exception e) {

            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        }
    }

    /**
     * Xác thực người dùng dựa trên mã xác thực.
     * @param verificationCode Mã xác thực từ liên kết
     * @return true nếu xác thực thành công, false nếu mã không hợp lệ hoặc tài khoản đã xác thực
     * @throws IllegalArgumentException Nếu mã xác thực không tồn tại
     */
    public boolean verifyUser(String verificationCode) {
        User user = userRepository.findByVerificationCode(verificationCode);;
        if (user.isEnable()) {
            return false;
        }
        user.setEnable(true);
        user.setVerificationCode(null); // Xóa mã xác thực
        userRepository.save(user);
        return true;
    }



}

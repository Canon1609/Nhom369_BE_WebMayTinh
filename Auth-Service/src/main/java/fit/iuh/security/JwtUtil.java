package fit.iuh.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

@Component
public class JwtUtil {

    private final SecretKey secret;
    public JwtUtil(@Value("${jwt.secret}") String secretString) {
        this.secret = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }
    private long accessTokenExp = 3600000 ; // 1 giờ access token
    private long refreshTokenExp = 604800000; // 7 ngày rf token ;

    // tạo Access Token
    public String generateAccessToken(String userName , Set<String> roles) {
        return Jwts.builder()
                .setSubject(userName)
                .claim("roles" , roles )
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExp))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();

    }
    // tạo Refresh Token
    public String generateRefreshToken(String userName) {
        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExp))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();

    }

    // Trích xuất userName từ token
    public String  extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret) // sử dụng SecretKey
                .build()
                .parseClaimsJwt(token)
                .getBody()
                .getSubject();
    }

    //Trích xuất danh sách role từ token
    public Set<String> extractRoles(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody()
                .get("roles", Set.class);
    }
    // kiểm tra token có hợp lệ không
    public boolean validateToken(String token , String userName) {
       try{
           String tokenUserName = extractUsername(token);
           return (tokenUserName.equals(userName) && isTokenExpired(token));
       } catch (Exception e) {
           return false;
       }
    }
    // kiểm tra token hết hạn
    private boolean isTokenExpired(String token) {
        Date exp = Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJwt(token)
                .getBody()
                .getExpiration();
        return exp.before(new Date());
    }

}

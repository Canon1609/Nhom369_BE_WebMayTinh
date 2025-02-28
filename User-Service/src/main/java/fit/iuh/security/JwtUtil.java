package fit.iuh.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    private SecretKey secret = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private  long accessTokenExp = 3600000 ; // 1 giờ access token
    private long refreshTokenExp = 604800000; // 7 ngày rf token ;

    // tạo Access Token
    public String generateAccessToken(String userName) {
        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExp))
                .signWith(secret, SignatureAlgorithm.HS512)
                .compact();

    }

    // tạo Refresh Token
    public String generateRefreshToken(String userName) {
        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExp))
                .signWith(secret, SignatureAlgorithm.HS512)
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

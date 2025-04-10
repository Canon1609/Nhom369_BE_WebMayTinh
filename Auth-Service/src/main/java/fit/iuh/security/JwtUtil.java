package fit.iuh.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    private SecretKey secret;
    private  long accessTokenExp = 86400000 ; // 1 ngày access token
    private long refreshTokenExp = 604800000; // 7 ngày rf token ;
    @PostConstruct
    public void init() {
        try {
            secret = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SecretKey: " + e.getMessage());
        }
    }
    // tạo Access Token
    public String generateAccessToken(String userName , String role) {
        return Jwts.builder()
                .setSubject(userName)
                .claim("role", role) // Thêm role vào claims
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
//                .parseClaimsJwt(token)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // kiểm tra token có hợp lệ không
    public boolean validateToken(String token , String userName) {
       try{
           String tokenUserName = extractUsername(token);
//           return (tokenUserName.equals(userName) && isTokenExpired(token));
           return (tokenUserName.equals(userName) && !isTokenExpired(token));
       } catch (Exception e) {
           return false;
       }
    }
    // kiểm tra token hết hạn
    private boolean isTokenExpired(String token) {
        Date exp = Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return exp.before(new Date());
    }
    // trích xuất role từ token
    // Trích xuất role từ token
    public String extractRole(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (ExpiredJwtException e) {
            // Không ném ngoại lệ, trả về null để JwtRequestFilter xử lý
            return null;
        } catch (SignatureException e) {
            // Không ném ngoại lệ, trả về null để JwtRequestFilter xử lý
            return null;
        } catch (Exception e) {
            // Không ném ngoại lệ, trả về null để JwtRequestFilter xử lý
            return null;
        }
    }

}

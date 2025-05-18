package fit.iuh.security;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class JwtUtils {
    private final SecretKey secret;
    public JwtUtils(@Value("${jwt.secret}") String secretString) {
        this.secret = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    // Trích xuất userName từ token
    public String  extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret) // sử dụng SecretKey
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    // Lấy roles từ token
    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
        List<String> rolesList = claims.get("roles", List.class); // Hoặc ArrayList.class
        return rolesList != null ? new HashSet<>(rolesList) : Collections.emptySet();
    }


    // kiểm tra token có hợp lệ không
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false; // Token hết hạn, ký sai, hoặc không hợp lệ
        }
    }
    // kiểm tra token hết hạn
    public boolean isTokenExpired(String token) {
        Date exp = Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJwt(token)
                .getBody()
                .getExpiration();
        return exp.before(new Date());
    }



}

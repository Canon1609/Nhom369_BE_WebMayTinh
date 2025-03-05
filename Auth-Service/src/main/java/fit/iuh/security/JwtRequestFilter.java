package fit.iuh.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    // lấy Header từ request
        final String authorizationHeader = request.getHeader("Authorization");

        String userName = null;
        String jwt = null;
        // kiểm tra header có tồn tại và bắt đầu bằng "Bearer" không
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                userName = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            } catch (Exception e) {
                System.out.println("Invalid JWT Token");
            }
        }
        // Nếu username được trích xuất thành công và chưa có authentication trong context
        if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Kiểm tra token hợp lệ
            if (jwtUtil.validateToken(jwt, userName)) {
                // Tạo đối tượng Authentication và gắn vào SecurityContext
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userName, null, null);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // Tiếp tục chuỗi filter
        filterChain.doFilter(request, response);    }
}

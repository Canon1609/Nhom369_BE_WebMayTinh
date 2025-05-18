package fit.iuh.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtil;

    public JwtFilter(JwtUtils jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        // Kiểm tra header Authorization
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }
        String token = header.substring(7);
        // Validate token trước, xử lý ngoại lệ
        try {
            if (!jwtUtil.validateToken(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            // Trích xuất username và roles sau khi token hợp lệ
            String username = jwtUtil.extractUsername(token);
            Set<String> roles = jwtUtil.extractRoles(token);

            // Tạo authorities với prefix "ROLE_"
            var authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            // Set Authentication vào SecurityContext
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Set token vào request attribute cho controller
            request.setAttribute("token", token);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token validation failed: " + e.getMessage());
            return;
        }

        // Tiếp tục filter chain
        filterChain.doFilter(request, response);
    }
}
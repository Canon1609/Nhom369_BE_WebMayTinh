package vn.edu.iuh.fit.cart_orderService.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.cart_orderService.models.*;
import vn.edu.iuh.fit.cart_orderService.repositories.CartDetailRepository;
import vn.edu.iuh.fit.cart_orderService.repositories.CartRepository;

import java.util.*;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;

    @Autowired
    private RestTemplate restTemplate;

//    private final String PRODUCT_SERVICE_URL = "http://localhost:8082/api/products";
//    private final String authServiceUrl = "http://localhost:8080/users/account";
    @Value("${product.service.url:http://product-service:8082/api/products}")
    private String productServiceUrl; // Đổi tên biến để phù hợp với quy ước Spring Boot

    @Value("${user-service.url:http://auth-service:8080/users/account}")
    private String authServiceUrl; // Đổi tên biến để phù hợp với quy ước Spring Boot

    public CartService(CartRepository cartRepository,  CartDetailRepository cartDetailRepository) {
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
    }

    // Phương thức lấy thông tin người dùng từ token
    public User getUserFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Token is missing or invalid format");
        }

        String jwtToken = token.substring(7);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    authServiceUrl, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("user")) {
                    Map<String, Object> userInfo = (Map<String, Object>) responseBody.get("user");

                    User user = new User();
                    user.setUsername((String) userInfo.get("username"));
                    user.setEmail((String) userInfo.get("email"));

                    Integer userId = (Integer) userInfo.get("id");
                    user.setId(userId != null ? Long.valueOf(userId) : null);

                    user.setAvt((String) userInfo.get("avatar"));
                    return user;
                }
            } else {
                throw new RuntimeException("Failed to get user information from AuthService");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error calling AuthService: " + e.getMessage());
        }
        return null;
    }



    public Cart handleAddProductToCart(String token, Long productId, int quantity) {
        User userDto = getUserFromToken(token);

        if (userDto != null) {
            Cart cart = cartRepository.findByUserId(userDto.getId());
            if (cart == null) {
                System.out.println("Creating new cart for user ID: " + userDto.getId());
                Cart newCart = new Cart();
                newCart.setUserId(userDto.getId());
                newCart.setSum(0);
                cart = cartRepository.save(newCart);
            }

            // Gọi API ProductService để lấy thông tin sản phẩm
            Product product = restTemplate.getForObject(productServiceUrl + "/" + productId, Product.class);
            System.out.println("Product: " + product);

            if (product != null) {
                CartDetail cartDetail = cartDetailRepository.findByCartAndProduct(cart, productId);
                System.out.println("CartDetail: " + cartDetail);

                if (cartDetail == null) {
                    cartDetail = new CartDetail();
                    cartDetail.setCart(cart);
                    cartDetail.setProductId(product.getId());
                    cartDetail.setProductName(product.getName());
                    cartDetail.setQuantity(quantity);
                    cartDetail.setPrice(product.getPrice());

                    // Lưu CartDetail vào cơ sở dữ liệu
                    cartDetailRepository.save(cartDetail);
                    cart.setSum(cart.getSum() + 1);

                    // Kiểm tra và khởi tạo cartDetails nếu nó là null
                    if (cart.getCartDetails() == null) {
                        cart.setCartDetails(new ArrayList<>()); // Khởi tạo danh sách nếu chưa có
                    }

                    cart.getCartDetails().add(cartDetail);  // Thêm vào danh sách chi tiết giỏ hàng
                    cartRepository.save(cart);
                } else {
                    // Nếu đã có sản phẩm trong giỏ hàng, chỉ cần cập nhật số lượng
                    cartDetail.setQuantity(cartDetail.getQuantity() + quantity);
                    cartDetailRepository.save(cartDetail);
                }
            } else {
                // Nếu không tìm thấy sản phẩm trong ProductService
                System.out.println("Product not found");
            }
            System.out.println("Cart: " + cart);
            return cart;  // Trả về giỏ hàng đã được cập nhật
        }
        return null;  // Nếu không có user hợp lệ, trả về null
    }



    public Cart handleGetCart(String token) {
        User userDto = getUserFromToken(token);
        if (userDto == null) {
            // Nếu không tìm thấy thông tin người dùng từ token, trả về null hoặc có thể ném exception
            throw new RuntimeException("Invalid token or user not found");
        }

        Cart cart = cartRepository.findByUserId(userDto.getId());
        if (cart != null) {
            return cart;
        }
        return null;  // Trả về null nếu không tìm thấy giỏ hàng
    }


public Cart handleRemoveProductFromCart(String token,Long cartDetailId) {
    User userDto = getUserFromToken(token);
    if (userDto == null) {
        // Nếu không tìm thấy thông tin người dùng từ token, trả về null hoặc có thể ném exception
        throw new RuntimeException("Invalid token or user not found");
    }

    Optional<CartDetail> cartDetail = cartDetailRepository.findById(cartDetailId);
    if (cartDetail.isPresent()) {
        CartDetail detail = cartDetail.get();
        Cart cart = detail.getCart();

        // Xóa sản phẩm khỏi giỏ hàng
        cart.getCartDetails().remove(detail);
        cartDetailRepository.deleteById(cartDetailId);

        // Cập nhật giỏ hàng
        cart.setSum(cart.getCartDetails().size());  // Cập nhật lại tổng số sản phẩm trong giỏ hàng

        // Xóa giỏ hàng nếu không còn sản phẩm
        if (cart.getCartDetails().isEmpty()) {
            cartRepository.delete(cart);  // Xóa giỏ hàng nếu không còn sản phẩm
            return null;  // Trả về null nếu giỏ hàng đã bị xóa
        }

        cartRepository.save(cart);  // Lưu giỏ hàng sau khi cập nhật
        return cart;
    }
    return null;  // Nếu không tìm thấy CartDetail
}

}

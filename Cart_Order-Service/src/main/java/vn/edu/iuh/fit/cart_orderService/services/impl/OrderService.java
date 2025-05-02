package vn.edu.iuh.fit.cart_orderService.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.cart_orderService.models.*;
import vn.edu.iuh.fit.cart_orderService.repositories.*;
import vn.edu.iuh.fit.cart_orderService.services.IService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final PaymentMethodRepository paymentMethodRepository;
//    private final ShippingAddressRepository shippingAddressRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String PRODUCT_SERVICE_URL = "http://localhost:8082/api/products";
    private final String authServiceUrl = "http://localhost:8080/users/account";

    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, CartRepository cartRepository, CartDetailRepository cartDetailRepository, PaymentMethodRepository paymentMethodRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.paymentMethodRepository = paymentMethodRepository;
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

//    public Order handlePlaceOrder(String token,  Long paymentMethodId) {
//        User userDto = getUserFromToken(token);
//        if (userDto == null) {
//            // Nếu không tìm thấy thông tin người dùng từ token, trả về null hoặc có thể ném exception
//            throw new RuntimeException("Invalid token or user not found");
//        }
//
//        Cart cart = cartRepository.findByUserId(userDto.getId());
//        if (cart == null) {
//            throw new RuntimeException("Cart not found for the user");
//        }
//
//        List<CartDetail> cartDetails = cartDetailRepository.findByCart(cart);
//        if (cartDetails.isEmpty()) {
//            throw new RuntimeException("No products in the cart");
//        }
//
//        // Tìm PaymentMethod và kiểm tra nếu không tìm thấy
//        Optional<PaymentMethod> optionalPaymentMethod = paymentMethodRepository.findById(paymentMethodId);
//        if (!optionalPaymentMethod.isPresent()) {
//            throw new RuntimeException("Payment method not found");
//        }
//        PaymentMethod paymentMethod = optionalPaymentMethod.get();
//
//        // Tạo Order
//        Order order = new Order();
//        order.setCreateAt(LocalDate.now());
//        order.setUserId(userDto.getId());
//        order.setStatus("PENDING");
//        order.setPaymentMethod(paymentMethod);
//
//        double sum = 0;
//        for (CartDetail cartDetail : cartDetails) {
//            sum += cartDetail.getPrice() * cartDetail.getQuantity();
//        }
//        order.setTotalPrice(sum);
//
//        // Lưu Order vào cơ sở dữ liệu
//        order = orderRepository.save(order);
//
//        // Tạo OrderDetail từ CartDetail
//        List<OrderDetail> orderDetails = new ArrayList<>();
//        for (CartDetail cartDetail : cartDetails) {
//            OrderDetail orderDetail = new OrderDetail();
//            orderDetail.setOrder(order);
//            orderDetail.setProductId(cartDetail.getProductId());  // Đảm bảo có thông tin sản phẩm
//            orderDetail.setQuantity(cartDetail.getQuantity());
//            orderDetail.setPrice(cartDetail.getPrice());
//            orderDetails.add(orderDetail);
//            orderDetailRepository.save(orderDetail);
//        }
//        order.setOrderDetails(orderDetails);
//
//        // Xóa các CartDetail
//        for (CartDetail cartDetail : cartDetails) {
//            cart.getCartDetails().remove(cartDetail); // Xóa khỏi list để tránh tham chiếu
//            cartDetailRepository.delete(cartDetail); // Xóa tay
//        }
//
//        // Xóa Cart
//        cartRepository.delete(cart); // Sau khi xóa các chi tiết giỏ hàng, xóa giỏ hàng
//        return order;
//    }


    public Order placeOrder(String token,  Long paymentMethodId, String shippingAddress , List<Product> orderProductRequest) {
        User userDto = getUserFromToken(token);
        if (userDto == null) {
            // Nếu không tìm thấy thông tin người dùng từ token, trả về null hoặc có thể ném exception
            throw new RuntimeException("Invalid token or user not found");
//            return false;
        }

        if (orderProductRequest == null || orderProductRequest.isEmpty())
            throw new RuntimeException("Order product request is empty");
        Order order = new Order();
        order.setUserId(userDto.getId());
        order.setCreateAt(LocalDate.now());
        List<OrderDetail> orderDetails = new ArrayList<>();
        Double totalPrice = 0.0;
        // Gọi API ProductService để lấy thông tin sản phẩm
        for (Product item : orderProductRequest) {
            // Gọi Product Service để lấy thông tin sản phẩm theo ID
            Product product = restTemplate.getForObject(PRODUCT_SERVICE_URL + "/" + item.getId(), Product.class);

            if (product != null) {
                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setProductId(product.getId());
                detail.setQuantity(item.getQuantity());
                detail.setPrice(product.getPrice());
                detail.setProductName(product.getName());
                totalPrice += product.getPrice() * item.getQuantity();
                orderDetails.add(detail);
            }
        }



        // Tìm PaymentMethod và kiểm tra nếu không tìm thấy
        Optional<PaymentMethod> optionalPaymentMethod = paymentMethodRepository.findById(paymentMethodId);
        if (!optionalPaymentMethod.isPresent()) {
            // Nếu không tìm thấy phương thức thanh toán, ném exception
            throw new RuntimeException("Payment method not found");
        }
        order.setPaymentMethod(optionalPaymentMethod.get());
        order.setOrderDetails(orderDetails);
        order.setStatus("PENDING");
        order.setTotalPrice(totalPrice);
        order.setShippingAddress(shippingAddress);

        orderRepository.save(order);

        return order;
    }



    public List<Order> handleGetOrderByUser(String token) {
        User userDto = getUserFromToken(token);
        if (userDto == null) {
            throw new RuntimeException("Invalid token or user not found");
        }

        List<Order> order = orderRepository.findByUserId(userDto.getId());
        if (order != null) {
            return order;
        }
        return null;
    }


    public Order handleUpdateStatus(String token, Long orderId, String status) {
        // Lấy thông tin người dùng từ token
        User userDto = getUserFromToken(token);
        if (userDto == null) {
            // Nếu không tìm thấy thông tin người dùng từ token, ném exception
            throw new RuntimeException("Invalid token or user not found");
        }

        // Tìm đơn hàng theo ID
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (!optionalOrder.isPresent()) {
            // Nếu không tìm thấy đơn hàng, ném exception
            throw new RuntimeException("Order not found");
        }

        Order order = optionalOrder.get();

        // Kiểm tra nếu người dùng không phải là chủ của đơn hàng (nếu cần)
        if (!order.getUserId().equals(userDto.getId())) {
            throw new RuntimeException("User is not authorized to update this order");
        }

        // Cập nhật trạng thái của đơn hàng
        order.setStatus(status);


        // Lưu đơn hàng đã được cập nhật
        return orderRepository.save(order);
    }


}

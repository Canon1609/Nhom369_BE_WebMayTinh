package vn.edu.iuh.fit.cart_orderService.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.cart_orderService.models.*;
import vn.edu.iuh.fit.cart_orderService.repositories.*;
import vn.edu.iuh.fit.cart_orderService.services.IService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
    private final String authServiceUrl = "http://localhost:8080/users";

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
                    authServiceUrl + "/account", HttpMethod.GET, entity, Map.class);

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



    public Order placeOrder(String token,  Long paymentMethodId, String shippingAddress, String note , List<Product> orderProductRequest) {
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
        order.setCreateAt(LocalDateTime.now());
        List<OrderDetail> orderDetails = new ArrayList<>();
        Double totalPrice = 0.0;
        // Gọi API ProductService để lấy thông tin sản phẩm
        for (Product item : orderProductRequest) {
            Product product;
            try {
                product = restTemplate.getForObject(PRODUCT_SERVICE_URL + "/" + item.getId(), Product.class);
            } catch (Exception e) {
                throw new RuntimeException("Product with ID " + item.getId() + " not found");
            }


            if (product.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Product quantity is not enough");
            }

            // Tạo OrderDetail
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProductId(product.getId());
            detail.setQuantity(item.getQuantity());
            detail.setPrice(product.getPriceAfterDiscount());
            detail.setProductName(product.getName());
            totalPrice += product.getPriceAfterDiscount() * item.getQuantity();
            orderDetails.add(detail);

            // 👇 Cập nhật số lượng sản phẩm sau khi đặt hàng
            Long updatedQuantity = product.getQuantity() - item.getQuantity();

            // Gọi API update quantity
            String updateQuantityUrl = PRODUCT_SERVICE_URL + "/update-quantity/" + product.getId();

            Map<String, Integer> quantityUpdate = new HashMap<>();
            quantityUpdate.put("quantity", Math.toIntExact(updatedQuantity));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Integer>> requestEntity = new HttpEntity<>(quantityUpdate, headers);

            restTemplate.postForObject(updateQuantityUrl, requestEntity, Void.class);
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
        order.setNote(note);
        order.setShippingAddress(shippingAddress);

        orderRepository.save(order);

        return order;
    }


    public Map<String, Object> checkProductQuantity(List<Product> orderProductRequest) {
        Map<String, Object> response = new HashMap<>();
        for (Product item : orderProductRequest) {
            Product product;
            try {
                product = restTemplate.getForObject(PRODUCT_SERVICE_URL + "/" + item.getId(), Product.class);
                if (product == null) {
                    response.put("message", "Product with ID " + item.getId() + " not found");
                    response.put("isSuccess", false);
                    return response;
                }
            } catch (HttpClientErrorException.NotFound e) {
                response.put("message", "Product with ID " + item.getId() + " not found");
                response.put("isSuccess", false);
                return response;
            }

            if (product.getQuantity() < item.getQuantity()) {
                response.put("message", product.getName() + " Không đủ số lượng");
                response.put("isSuccess", false);
                return response;
            }
        }

        response.put("message", "Product quantity is enough");
        response.put("isSuccess", true);
        return response;
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


    public Order handleUpdateStatus(String token, Long orderId, String status, String message) {
        // Lấy thông tin người dùng từ token
        User userDto = getUserFromToken(token);
        if (userDto == null) {
            throw new RuntimeException("Invalid token or user not found");
        }
        // Tìm đơn hàng theo ID
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (!optionalOrder.isPresent()) {
            throw new RuntimeException("Order not found");
        }
        Order order = optionalOrder.get();
        if (!order.getUserId().equals(userDto.getId())) {
            throw new RuntimeException("User is not authorized to update this order");
        }
        // Cập nhật trạng thái của đơn hàng
        System.out.println("Status: " + status);
        System.out.println("Message: " + message);
        order.setStatus(status);
        order.setReasonCancel(message);
        // Lưu đơn hàng đã được cập nhật
        return orderRepository.save(order);
    }

    public List<Map<String, Object>> getAllOrder() {
      List<Order> orders = orderRepository.findAll();
      List<Map<String, Object>> orderList = new ArrayList<>();

      for (Order order : orders) {
          Map<String, Object> orderMap = new HashMap<>();
          orderMap.put("id", order.getId());
          orderMap.put("date", order.getCreateAt());
          orderMap.put("status", order.getStatus());
          orderMap.put("total", order.getTotalPrice());
          orderMap.put("address", order.getShippingAddress());
          orderMap.put("note", order.getNote());
          orderMap.put("reasonCancel", order.getReasonCancel());

          // Thêm thông tin chi tiết đơn hàng
          List<OrderDetail> orderDetails = orderDetailRepository.findByOrder_Id(order.getId());
          List<Map<String, Object>> detailsList = new ArrayList<>();
          for (OrderDetail detail : orderDetails) {
              Map<String, Object> detailMap = new HashMap<>();
              detailMap.put("id", detail.getProductId());
              detailMap.put("name", detail.getProductName());
              detailMap.put("quantity", detail.getQuantity());
              detailMap.put("price", detail.getPrice());
              detailsList.add(detailMap);
          }
          orderMap.put("items", detailsList);

//           Thêm thông tin phương thức thanh toán
          PaymentMethod paymentMethod = paymentMethodRepository.findById(order.getPaymentMethod().getId()).orElse(null);
          if (paymentMethod != null) {
              orderMap.put("paymentMethod", paymentMethod.getName());
          }

          // Thêm thông tin người dùng
          ResponseEntity<Map> response = restTemplate.exchange(
                  authServiceUrl + "/getUserById/" + order.getUserId(),
                  HttpMethod.GET,
                  null,
                  Map.class
          );

          Map<String, Object> body = response.getBody();
          Map<String, Object> userMap = (Map<String, Object>) body.get("user");

          String username = (String) userMap.get("username");
          String email = (String) userMap.get("email");
          String phone = (String) userMap.get("phone");

          orderMap.put("customer", username);
          orderMap.put("email", email);
          orderMap.put("phone", phone);

          orderList.add(orderMap);

      }
        return orderList;
    }

    public List<Map<String, Object>> getProductSold() {
        List<Order> orders = orderRepository.findAll();
        List<Map<String, Object>> productList = new ArrayList<>();
        ResponseEntity<List<Product>> response = restTemplate.exchange(
                "http://localhost:8082/api/products",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Product>>() {}
        );

        List<Product> products = response.getBody();

        for( Product product : products) {
            int sum = 0;
            List<OrderDetail> orderDetails = orderDetailRepository.findByProductId(product.getId());
            long totalSold = orderDetails.stream()
                    .mapToLong(OrderDetail::getQuantity)
                    .sum();
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("id", product.getId());
            productMap.put("name", product.getName());
            productMap.put("totalSold", totalSold);
            productList.add(productMap);
        }

        return productList;
    }


}

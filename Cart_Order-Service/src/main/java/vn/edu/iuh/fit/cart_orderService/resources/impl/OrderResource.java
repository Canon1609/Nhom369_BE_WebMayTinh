package vn.edu.iuh.fit.cart_orderService.resources.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.cart_orderService.dto.CreateOderRequest;
import vn.edu.iuh.fit.cart_orderService.dto.UpdateOderRequest;
import vn.edu.iuh.fit.cart_orderService.models.Cart;
import vn.edu.iuh.fit.cart_orderService.models.Order;
import vn.edu.iuh.fit.cart_orderService.models.Product;
import vn.edu.iuh.fit.cart_orderService.models.Response;
import vn.edu.iuh.fit.cart_orderService.resources.IManagement;
import vn.edu.iuh.fit.cart_orderService.services.impl.OrderService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
@Slf4j
public class OrderResource {

    private final OrderService orderService;

    public OrderResource(OrderService orderService) {
        this.orderService = orderService;
    }


    @PostMapping("/placeOrder")
    public ResponseEntity<?> placeOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody CreateOderRequest request
    ) {
        log.info("Call place order");
      try{
          Long paymentMethodId = request.getPaymentMethodId();
          List<Product> products = request.getProducts();
          String shippingAddress = request.getShippingAddress();
          String note = request.getNote();
          Order order = orderService.placeOrder(token, paymentMethodId, shippingAddress , note , products);
            return ResponseEntity.ok(new Response(
                    HttpStatus.OK.value(),
                    "Place order success",
                    order
            ));
      } catch (Exception e) {
          log.error("Place order fail");
          log.error("Error: " + e);
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                  HttpStatus.UNAUTHORIZED.value(),
                  "Place order fail: " + e.getMessage(),
                  null
          ));

      }
    }

    @PostMapping("/checkProductQuantity")
    public ResponseEntity<?> checkProductQuantity(@RequestBody CreateOderRequest request) {
        log.info("Call check product quantity");
        try {
            List<Product> products = request.getProducts();
            Map<String, Object> response = orderService.checkProductQuantity(products);
            String message = (String) response.get("message");
            boolean isAvailable = (boolean) response.get("isSuccess");
            return ResponseEntity.ok(new Response(
                    HttpStatus.OK.value(),
                    message,
                    isAvailable
            ));
        } catch (Exception e) {
            log.error("Check product quantity failed");
            log.error("Error: " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e.getMessage(),
                    null
            ));
        }
    }


    @GetMapping("/getOrders")
    public ResponseEntity<Response> getOrders(@RequestHeader("Authorization") String token) {
        log.info("Call get order");
        try {
            List<Order> order = orderService.handleGetOrderByUser(token);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                        HttpStatus.NOT_FOUND.value(),
                        "User does not have order",
                        null
                ));
            } else {
                return ResponseEntity.ok(new Response(
                        HttpStatus.OK.value(),
                        "Get order success",
                        order
                ));
            }
        } catch (Exception e) {
            log.error("Get order fail");
            log.error("Error: " + e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Get order fail: " + e.getMessage(),
                    null
            ));
        }
    }

    @PostMapping("/updateStatus/{orderId}")
    public ResponseEntity<Response> updateStatus(@PathVariable Long orderId, @RequestBody UpdateOderRequest request, @RequestHeader("Authorization") String token) {
        log.info("Call order update status");
        try {
            String status = request.getStatus();
            String message = request.getMessage();
            System.out.println("Message: " + message);
            Order output = orderService.handleUpdateStatus(token, orderId, status, message);
            log.info("Update order status success");

            return ResponseEntity.ok(new Response(
                    200,
                    "Update order status success",
                    output
            ));
        } catch (RuntimeException e) {
            log.error("Update order status fail");
            log.error("Error: " + e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                    400,
                    "Update order status failed: " + e.getMessage(),
                    null
            ));
        } catch (Exception e) {
            log.error("Update order status fail");
            log.error("Error: " + e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(
                    500,
                    "Internal server error",
                    null
            ));
        }
    }

    @GetMapping("/getOrderAllOrder")
    public ResponseEntity<Map<String, Object>> getAllOrder(){
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> orders = orderService.getAllOrder();
        if (orders.isEmpty()) {
            response.put("message", "No orders found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response.put("message", "Get all orders success");
        response.put("status", HttpStatus.OK.value());
        response.put("orders", orders);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getProductSold")
    public ResponseEntity<Map<String, Object>> getProductSold(){
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> products = orderService.getProductSold();
        if (products.isEmpty()) {
            response.put("message", "No products found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response.put("message", "Get all products sold success");
        response.put("status", HttpStatus.OK.value());
        response.put("products", products);
        return ResponseEntity.ok(response);
    }


}

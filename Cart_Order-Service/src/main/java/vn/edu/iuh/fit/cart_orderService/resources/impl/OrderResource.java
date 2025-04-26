package vn.edu.iuh.fit.cart_orderService.resources.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.cart_orderService.dto.CreateOderRequest;
import vn.edu.iuh.fit.cart_orderService.dto.UpdateOderRequest;
import vn.edu.iuh.fit.cart_orderService.models.Cart;
import vn.edu.iuh.fit.cart_orderService.models.Order;
import vn.edu.iuh.fit.cart_orderService.models.Response;
import vn.edu.iuh.fit.cart_orderService.resources.IManagement;
import vn.edu.iuh.fit.cart_orderService.services.impl.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@Slf4j
public class OrderResource {

    private final OrderService orderService;

    public OrderResource(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/placeOrder")
    public ResponseEntity<Response> insert(@RequestBody CreateOderRequest request, @RequestHeader("Authorization") String token) {
        log.info("Call order insert");
        try {
//            Long UserId = request.getUserId();
//            Long AddressId = request.getAddressId();
            Long PaymentId = request.getPaymentMethodId();
            Order ouput = orderService.handlePlaceOrder(token, PaymentId);
            log.info("Insert order success");
            return ResponseEntity.ok(new Response(
                    200,
                    "Insert order success",
                    ouput
            ));
        } catch (Exception e) {
            log.error("Insert order fail");
            log.error("Error: " + e);
            return ResponseEntity.ok(new Response(
                    200,
                    "Insert order fail",
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
            Order output = orderService.handleUpdateStatus(token, orderId, status);
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


}

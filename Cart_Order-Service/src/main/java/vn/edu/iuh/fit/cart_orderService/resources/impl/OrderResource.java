package vn.edu.iuh.fit.cart_orderService.resources.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.cart_orderService.dto.CreateOderRequest;
import vn.edu.iuh.fit.cart_orderService.dto.UpdateOderRequest;
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
    public ResponseEntity<Response> insert(@RequestBody CreateOderRequest request) {
        log.info("Call order insert");
        try {
            Long UserId = request.getUserId();
            Long AddressId = request.getAddressId();
            Long PaymentId = request.getPaymentMethodId();
            Order ouput = orderService.handlePlaceOrder(UserId, AddressId, PaymentId);
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

    @PutMapping("/updateStatus/{orderId}")
    public ResponseEntity<Response> updateStatus(@PathVariable Long orderId, @RequestBody UpdateOderRequest request) {
        log.info("Call order update status");
        try {
            String status = request.getStatus();
            Order ouput = orderService.handleUpdateStatus(orderId, status);
            log.info("Update order status success");
            return ResponseEntity.ok(new Response(
                    200,
                    "Update order status success",
                    ouput
            ));
        } catch (Exception e) {
            log.error("Update order status fail");
            log.error("Error: " + e);
            return ResponseEntity.ok(new Response(
                    200,
                    "Update order status fail",
                    null
            ));
        }
    }

}

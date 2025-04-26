//package vn.edu.iuh.fit.cart_orderService.resources.impl;
//
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import vn.edu.iuh.fit.cart_orderService.dto.ShippingAddressDto;
//import vn.edu.iuh.fit.cart_orderService.models.Response;
//import vn.edu.iuh.fit.cart_orderService.models.ShippingAddress;
//import vn.edu.iuh.fit.cart_orderService.services.impl.ShippingAddressService;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/shippingAddress")
//@Slf4j
//public class ShippingAddressResource {
//    private final ShippingAddressService shippingAddressService;
//
//    public ShippingAddressResource(ShippingAddressService shippingAddressService) {
//        this.shippingAddressService = shippingAddressService;
//    }
//
//    @PostMapping()
//    public ResponseEntity<Response> addShippingAddress(@RequestBody ShippingAddressDto request) {
//        log.info("Call shipping address insert");
//        try {
//            Long UserId = request.getUserId();
//            String Address = request.getAddress();
//            String Phone = request.getPhone();
//            String Name = request.getName();
//            ShippingAddress ouput = shippingAddressService.handleAddShippingAddress(UserId, Address, Phone, Name);
//            log.info("Insert shipping address success");
//            return ResponseEntity.ok(new Response(
//                    200,
//                    "Insert shipping address success",
//                    ouput
//            ));
//        } catch (Exception e) {
//            log.error("Insert shipping address fail");
//            log.error("Error: " + e);
//            return ResponseEntity.ok(new Response(
//                    200,
//                    "Insert shipping address fail",
//                    null
//            ));
//        }
//    }
//
//    @PutMapping()
//    public ResponseEntity<Response> updateShippingAddress(@RequestBody ShippingAddressDto request) {
//        log.info("Call shipping address update");
//        try {
//            Long shippingAddressId = request.getShippingAddressId();
//            String address = request.getAddress();
//            String phone = request.getPhone();
//            String name = request.getName();
//
//            ShippingAddress ouput = shippingAddressService.handleUpdateShippingAddress(shippingAddressId ,address, phone, name);
//            log.info("Update shipping address success");
//            return ResponseEntity.ok(new Response(
//                    200,
//                    "Update shipping address success",
//                    ouput
//            ));
//        } catch (Exception e) {
//            log.error("Update shipping address fail");
//            log.error("Error: " + e);
//            return ResponseEntity.ok(new Response(
//                    200,
//                    "Update shipping address fail",
//                    null
//            ));
//        }
//    }
//
//    @DeleteMapping("/{shippingAddressId}")
//    public ResponseEntity<Response> deleteShippingAddress(@PathVariable Long shippingAddressId) {
//        log.info("Call shipping address delete");
//        try {
//            Long id = shippingAddressId;
//            shippingAddressService.handleDeleteShippingAddress(id);
//            log.info("Delete shipping address success");
//            return ResponseEntity.ok(new Response(
//                    200,
//                    "Delete shipping address success",
//                    null
//            ));
//        } catch (Exception e) {
//            log.error("Delete shipping address fail");
//            log.error("Error: " + e);
//            return ResponseEntity.ok(new Response(
//                    200,
//                    "Delete shipping address fail",
//                    null
//            ));
//        }
//    }
//
//    @GetMapping("/{userId}")
//    public ResponseEntity<Response> getShippingAddressByUserId(@PathVariable Long userId) {
//        log.info("Call get shipping address by user id");
//        try {
//            List<ShippingAddress> ouput = shippingAddressService.handleGetShippingAddressByUserId(userId);
//            log.info("Get shipping address by user id success");
//            return ResponseEntity.ok(new Response(
//                    200,
//                    "Get shipping address by user id success",
//                    ouput
//            ));
//        } catch (Exception e) {
//            log.error("Get shipping address by user id fail");
//            log.error("Error: " + e);
//            return ResponseEntity.ok(new Response(
//                    200,
//                    "Get shipping address by user id fail",
//                    null
//            ));
//        }
//    }
//
//}

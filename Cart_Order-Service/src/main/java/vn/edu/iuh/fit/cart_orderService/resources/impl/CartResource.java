package vn.edu.iuh.fit.cart_orderService.resources.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.cart_orderService.dto.AddToCartRequest;
import vn.edu.iuh.fit.cart_orderService.models.Cart;
import vn.edu.iuh.fit.cart_orderService.models.Response;
import vn.edu.iuh.fit.cart_orderService.services.impl.CartService;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cart")
@Slf4j
public class CartResource {

    private final CartService cartService;

    public CartResource(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/addProductToCart")
    public ResponseEntity<Response> addProductToCart(@RequestBody AddToCartRequest request,
                                                     @RequestHeader("Authorization") String token) {
        log.info("Call add product to cart");
        try {
            Long productId = request.getProductId();
            Integer quantity = request.getQuantity();

            Cart cart = cartService.handleAddProductToCart(token, productId, quantity);

            System.out.println("Cart resource: " + cart);

            return ResponseEntity.ok(new Response(
                    HttpStatus.OK.value(),
                    "Add product to cart success",
                    cart
            ));
        } catch (Exception e) {
            log.error("Insert cartDetail fail");
            log.error("Error: " + e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Add product to cart fail: " + e.getMessage(),
                    null
            ));
        }
    }

    @GetMapping("/getCart")
    public ResponseEntity<Response> getCart(@RequestHeader("Authorization") String token) {
        log.info("Call get cart");
        try {
            // Xử lý lấy giỏ hàng
            Cart cart = cartService.handleGetCart(token);
            if (cart == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                        HttpStatus.NOT_FOUND.value(),
                        "User does not have cart",
                        null
                ));
            } else {
                return ResponseEntity.ok(new Response(
                        HttpStatus.OK.value(),
                        "Get cart success",
                        cart
                ));
            }
        } catch (Exception e) {
            log.error("Get cart fail");
            log.error("Error: " + e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Get cart fail: " + e.getMessage(),
                    null
            ));
        }
    }

    @PostMapping("/removeProductFromCart/{cartDetailId}")
    public ResponseEntity<Response> removeProductFromCart(@PathVariable Long cartDetailId, @RequestHeader("Authorization") String token) {
        log.info("Call remove product from cart");

        try {
            // Gọi service để xử lý việc xóa sản phẩm khỏi giỏ hàng
            Cart updatedCart = cartService.handleRemoveProductFromCart(token, cartDetailId);

            // Kiểm tra xem có sản phẩm nào còn lại trong giỏ hàng sau khi xóa hay không
            if (updatedCart == null ) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                        HttpStatus.BAD_REQUEST.value(),
                        "CartDetail not found",
                        null
                ));
            }

            // Trả về giỏ hàng đã được cập nhật
            return ResponseEntity.ok(new Response(
                    HttpStatus.OK.value(),
                    "Remove product from cart success",
                    updatedCart
            ));
        } catch (Exception e) {
            log.error("Remove product from cart failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Remove product from cart failed",
                    null
            ));
        }
    }



}

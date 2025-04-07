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
    public ResponseEntity<Response> addProductToCart(@RequestBody AddToCartRequest request) {
        log.info("Call cartDetail insert");
        try {
            Long userId = request.getUserId();
            Long productId = request.getProductId();
            Integer quantity = request.getQuantity();
            Optional<Cart> cart = cartService.handleAddProductToCart(userId, productId, quantity);
            return ResponseEntity.ok(new Response(
                    HttpStatus.OK.value(),
                    "Insert cartDetail success",
                    cart
            ));
        } catch (Exception e) {
            log.error("Insert cartDetail fail");
            log.error("Error: " + e);
            return ResponseEntity.ok(new Response(
                    HttpStatus.OK.value(),
                    "Insert cartDetail fail",
                    null
            ));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Response> getCart(@PathVariable Long userId) {
        log.info("Call get cart");
        try {
            Cart cart = cartService.handleGetCart(userId);
            if(cart == null){
                return ResponseEntity.ok(new Response(
                        HttpStatus.OK.value(),
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
            return ResponseEntity.ok(new Response(
                    HttpStatus.OK.value(),
                    "Get cart fail",
                    null
            ));
        }
    }

    @PostMapping("/removeProductFromCart/{cartDetailId}")
    public ResponseEntity<Response> removeProductFromCart(@PathVariable Long cartDetailId) {
        log.info("Call remove product from cart");
        try {
            Cart cart = cartService.handleRemoveProductFromCart(cartDetailId);
            return ResponseEntity.ok(new Response(
                    HttpStatus.OK.value(),
                    "Remove product from cart success",
                    cart
            ));
        } catch (Exception e) {
            log.error("Remove product from cart fail");
            log.error("Error: " + e);
            return ResponseEntity.ok(new Response(
                    HttpStatus.OK.value(),
                    "Remove product from cart fail",
                    null
            ));
        }
    }


}

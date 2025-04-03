package vn.edu.iuh.fit.cart_orderService.services.impl;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.cart_orderService.models.Cart;
import vn.edu.iuh.fit.cart_orderService.models.CartDetail;
import vn.edu.iuh.fit.cart_orderService.models.Product;
import vn.edu.iuh.fit.cart_orderService.models.User;
import vn.edu.iuh.fit.cart_orderService.repositories.CartDetailRepository;
import vn.edu.iuh.fit.cart_orderService.repositories.CartRepository;
import vn.edu.iuh.fit.cart_orderService.repositories.ProductRepository;
import vn.edu.iuh.fit.cart_orderService.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartDetailRepository cartDetailRepository;

    public CartService(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository, CartDetailRepository cartDetailRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartDetailRepository = cartDetailRepository;
    }


    public Optional<Cart> handleAddProductToCart(Long userId, Long productId, int quantity) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()){
            Optional<Cart> cart = Optional.ofNullable(cartRepository.findByUser(user));
            if(cart.isPresent()){
                System.out.println("Cart is present");

            } else {
                System.out.println("Cart is not present");
                Cart newCart = new Cart();
                newCart.setUser(user.get());
                newCart.setSum(0);
                cart = Optional.of(cartRepository.save(newCart));
            }

            Optional<Product> productOptional = productRepository.findById(productId);
            if(productOptional.isPresent()){
                System.out.println("Product is present");
                Product realProduct = productOptional.get();
                CartDetail cartDetail = new CartDetail();
                CartDetail cartCheck = cartDetailRepository.findByCartAndProduct(cart.get(), realProduct);
                    if(cartCheck == null){
                        System.out.println("CartDetail is not present");
                        cartDetail.setCart(cart.get());
                        cartDetail.setProduct(realProduct);
                        cartDetail.setQuantity(quantity);
                        cartDetail.setPrice(realProduct.getPrice());
                        cartDetailRepository.save(cartDetail);
                        cart.get().setSum(cart.get().getSum()+1);
                        if(cart.get().getCartDetails() == null){
                            List<CartDetail> listCartDetail = new ArrayList<>();
                            listCartDetail.add(cartDetail);
                            cart.get().setCartDetails(listCartDetail);
                        }
                        else{
                            cart.get().getCartDetails().add(cartDetail);
                        }
                        cart = Optional.of(cartRepository.save(cart.get()));
                    }else{
                        System.out.println("CartDetail is present");
                        cartCheck.setQuantity(cartCheck.getQuantity()+quantity);
                        cartDetailRepository.save(cartCheck);
                    }
            } else {
                System.out.println("Product is not present");
            }
            return cart;
        }
        return null;

    }

    public Cart handleGetCart(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()){
            Optional<Cart> cart = Optional.ofNullable(cartRepository.findByUser(user));
            if(cart.isPresent()){
                return cart.get();
            }
        }
        return null;
    }

    public Cart handleRemoveProductFromCart(Long cartDetailId) {
            Optional<CartDetail> cartDetail = cartDetailRepository.findById(cartDetailId);
            if(cartDetail.isPresent()){
                System.out.println("CartDetail is present");
//            Cart cart = cartDetail.get().getCart();

                CartDetail detail = cartDetail.get();
                Cart cart = detail.getCart();

                if (cart.getCartDetails() != null) {
                    cart.getCartDetails().remove(detail);
                }

                cartDetailRepository.deleteById(cartDetailId);


                if(cart.getSum() == 1){
                    if (cart.getUser() != null) {
                        cart.getUser().setCart(null);
                        cart.setUser(null);
                    }
                    if (cart.getCartDetails() != null) {
                        cart.getCartDetails().clear();
                    }
                    cartRepository.delete(cart);
                    return null;
                } else {
                    cart.setSum(cart.getSum()-1);
                    cartRepository.save(cart);
                }
                return cart;
            }
            return null;
    }
}

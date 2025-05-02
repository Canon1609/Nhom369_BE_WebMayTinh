package vn.edu.iuh.fit.cart_orderService.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.cart_orderService.models.Cart;
import vn.edu.iuh.fit.cart_orderService.models.CartDetail;
import vn.edu.iuh.fit.cart_orderService.models.Product;

import java.util.List;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long>{
    @Query("select c from CartDetail c where c.cart = ?1 and c.productId = ?2")
    CartDetail findByCartAndProduct(Cart cart, Long productId);

    @Query("select c from CartDetail c where c.cart = ?1")
    List<CartDetail> findByCart(Cart cart);


    @Transactional
    @Modifying
    @Query("delete from CartDetail c where c.cart = ?1")
    int deleteByCart(Cart cart);




}

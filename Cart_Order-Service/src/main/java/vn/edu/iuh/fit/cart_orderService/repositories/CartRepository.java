package vn.edu.iuh.fit.cart_orderService.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.cart_orderService.models.Cart;
import vn.edu.iuh.fit.cart_orderService.models.User;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("select c from Cart c where c.user = ?1")
    Cart findByUser(Optional<User> user);

}

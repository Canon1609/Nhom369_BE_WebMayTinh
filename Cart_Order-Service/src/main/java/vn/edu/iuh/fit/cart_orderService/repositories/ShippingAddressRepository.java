package vn.edu.iuh.fit.cart_orderService.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.cart_orderService.models.ShippingAddress;
import vn.edu.iuh.fit.cart_orderService.models.User;

import java.util.List;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
    @Query("select s from ShippingAddress s where s.user = ?1")
    List<ShippingAddress> findByUser(User user);

}

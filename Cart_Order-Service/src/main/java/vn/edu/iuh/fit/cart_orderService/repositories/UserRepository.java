package vn.edu.iuh.fit.cart_orderService.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.cart_orderService.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}

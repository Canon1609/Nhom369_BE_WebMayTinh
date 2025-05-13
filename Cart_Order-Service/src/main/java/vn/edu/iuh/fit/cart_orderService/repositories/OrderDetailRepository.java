package vn.edu.iuh.fit.cart_orderService.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.cart_orderService.models.OrderDetail;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    @Query("select o from OrderDetail o where o.order.id = ?1")
    List<OrderDetail> findByOrder_Id(long id);

    @Query("select o from OrderDetail o where o.productId = ?1")
    List<OrderDetail> findByProductId(Long productId);

}

package iuh.fit.se.repository;

import iuh.fit.se.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query("SELECT od.productId, od.productName, SUM(od.quantity) " +
            "FROM OrderDetail od " +
            "JOIN od.order o " +
            "WHERE o.status NOT IN ('CANCELLED') " +
            "GROUP BY od.productId, od.productName")
    List<Object[]> findProductSales();
}
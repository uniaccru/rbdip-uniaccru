package com.rbdip.bookstore.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("""
            SELECT i FROM OrderItem i
            JOIN FETCH i.product
            WHERE i.orderId IN :orderIds
            """)
    List<OrderItem> findAllWithProductsByOrderIdIn(@Param("orderIds") List<Long> orderIds);
}

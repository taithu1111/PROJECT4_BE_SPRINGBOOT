package phamiz.ecommerce.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import phamiz.ecommerce.backend.model.Order;

import java.util.List;

@Repository
public interface IOrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId " +
            "AND (o.orderStatus = 'PLACED' OR o.orderStatus = 'CONFIRMED' " +
            "OR o.orderStatus = 'SHIPPED' OR o.orderStatus = 'DELIVERED' OR o.orderStatus = 'PENDING')")
    public List<Order> getUsersOrders(@Param("userId") Long userId);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.orderStatus = 'DELIVERED'")
    Long getTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o")
    Long countOrders();

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    java.util.Optional<Order> findOrderByIdWithItems(@Param("orderId") Long orderId);

    List<Order> findByOrderStatus(String orderStatus);
}

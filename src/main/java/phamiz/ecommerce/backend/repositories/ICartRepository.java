package phamiz.ecommerce.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import phamiz.ecommerce.backend.model.Cart;

@Repository
public interface ICartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT cart FROM Cart cart LEFT JOIN FETCH cart.cartItems ci LEFT JOIN FETCH ci.product p LEFT JOIN FETCH p.images WHERE cart.user.id = :userId")
    public Cart findByUserId(@Param("userId") Long userId);

    @Query("""
                SELECT c FROM Cart c
                LEFT JOIN FETCH c.cartItems
                WHERE c.user.id = :userId
            """)
    Cart findByUserIdWithItems(@Param("userId") Long userId);
}

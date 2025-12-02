package phamiz.ecommerce.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import phamiz.ecommerce.backend.model.Rating;

import java.util.List;

@Repository
public interface IRatingRepository extends JpaRepository<Rating, Long> {
    @Query("SELECT r FROM Rating r WHERE r.product.id = :productId")
    public List<Rating> getAllProductsRating(@Param("productId") Long productId);

    @Query("SELECT r FROM Rating r WHERE " +
            "(:productId IS NULL OR r.product.id = :productId) AND " +
            "(:userId IS NULL OR r.user.id = :userId)")
    org.springframework.data.domain.Page<Rating> findByFilter(@Param("productId") Long productId,
            @Param("userId") Long userId,
            org.springframework.data.domain.Pageable pageable);

    @Modifying
    @Query("DELETE FROM Rating r WHERE r.product.id = :productId")
    public void deleteAllProductsRating(@Param("productId") Long productId);

}

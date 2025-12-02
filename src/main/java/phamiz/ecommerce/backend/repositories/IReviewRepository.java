package phamiz.ecommerce.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import phamiz.ecommerce.backend.model.Review;

import java.util.List;

@Repository
public interface IReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId")
    public List<Review> getAllProductsReview(@Param("productId") Long productId);

    @Query("SELECT r FROM Review r WHERE " +
            "(:productId IS NULL OR r.product.id = :productId) AND " +
            "(:userId IS NULL OR r.user.id = :userId)")
    org.springframework.data.domain.Page<Review> findByFilter(@Param("productId") Long productId,
            @Param("userId") Long userId,
            org.springframework.data.domain.Pageable pageable);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.product.id = :productId")
    public void deleteAllProductsReview(@Param("productId") Long productId);

}

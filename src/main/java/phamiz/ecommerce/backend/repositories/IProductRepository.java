package phamiz.ecommerce.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import phamiz.ecommerce.backend.model.Product;
import jakarta.persistence.LockModeType;

import java.util.List;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {
        @Query("SELECT p FROM Product p " +
                        "WHERE (:category IS NULL OR :category = '' OR p.category.category_name = :category) " +
                        "AND ((:minPrice IS NULL AND :maxPrice IS NULL) OR (p.price BETWEEN :minPrice AND :maxPrice)) "
                        +
                        "ORDER BY " +
                        "CASE WHEN :sort = 'price_low' THEN p.price END ASC, " +
                        "CASE WHEN :sort = 'price_high' THEN p.price END DESC")
        public List<Product> filterProducts(@Param("category") String category,
                        @Param("minPrice") Integer minPrice,
                        @Param("maxPrice") Integer maxPrice,
                        @Param("sort") String sort);

        // New method to get 6 latest products
        List<Product> findTop6ByOrderByCreatedAtDesc();

        // New method to get 6 random products
        @Query(value = "SELECT * FROM product ORDER BY RAND() LIMIT 6", nativeQuery = true)
        List<Product> findRandom6Products();

        // Pessimistic locking for concurrency control
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT p FROM Product p WHERE p.id = :productId")
        Product findByIdWithLock(@Param("productId") Long productId);

        // Delete product colors before deleting product to avoid foreign key constraint
        @Modifying
        @Query(value = "DELETE FROM product_color WHERE product_id = :productId", nativeQuery = true)
        void deleteProductColorsByProductId(@Param("productId") Long productId);
}

package phamiz.ecommerce.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import phamiz.ecommerce.backend.model.ProductImage;

@Repository
public interface IProductImageRepository extends JpaRepository<ProductImage, Long> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ProductImage p WHERE p.product.id = :productId")
    void deleteByProductId(@org.springframework.data.repository.query.Param("productId") Long productId);
}

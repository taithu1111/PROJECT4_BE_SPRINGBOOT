package phamiz.ecommerce.backend.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phamiz.ecommerce.backend.dto.Product.CreateProductRequest;
import phamiz.ecommerce.backend.dto.Product.ProductDTO;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.model.Product;
import phamiz.ecommerce.backend.service.IProductService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final IProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);

    // --- 1. CREATE ---
    @PostMapping("/")
    public ResponseEntity<Product> createProductHandler(@RequestBody CreateProductRequest request) {

        Product product = productService.createProduct(request);
        logger.info("New product created: {}", product.getProduct_name());
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    // --- 2. UPDATE INVENTORY
    @PutMapping("/{productId}/stock")
    public ResponseEntity<ProductDTO> adjustStockHandler(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> request) throws ProductException {

        Integer quantityChange = request.get("quantity");

        if (quantityChange == null || quantityChange == 0) {
            Product product = productService.findProductById(productId);
            return new ResponseEntity<>(productService.toDTO(product), HttpStatus.OK);
        }

        Product updatedProduct;

        if (quantityChange > 0) {
            updatedProduct = productService.increaseStock(productId, quantityChange);
            logger.info("Stock increased by {} for product ID: {}", quantityChange, productId);
        } else {
            updatedProduct = productService.decreaseStock(productId, Math.abs(quantityChange));
            logger.info("Stock decreased by {} for product ID: {}", Math.abs(quantityChange), productId);
        }

        return new ResponseEntity<>(productService.toDTO(updatedProduct), HttpStatus.OK);
    }

    // --- 3. UPDATE THÔNG TIN CƠ BẢN ---
    /**
     * Endpoint cập nhật thông tin cơ bản của sản phẩm.
     * @param productId ID sản phẩm cần cập nhật.
     * @param req Đối tượng Product chứa các trường cần cập nhật.
     * @return Thông tin sản phẩm sau khi cập nhật.
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductDTO> updateProductHandler(
            @PathVariable Long productId,
            @RequestBody Product req) throws ProductException {
        Product updatedProduct = productService.updateProduct(productId, req);
        logger.info("Product ID {} updated.", productId);

        return new ResponseEntity<>(productService.toDTO(updatedProduct), HttpStatus.OK);
    }
// hiện tại chỉ cập nhật quanity


    // --- 4. DELETE ---
    /**
     * Endpoint xóa sản phẩm.
     * @param productId ID sản phẩm cần xóa.
     * @return Thông báo thành công.
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteProductHandler(@PathVariable Long productId) throws ProductException {
        String message = productService.deleteProduct(productId);
        logger.warn("Product ID {} deleted.", productId);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}
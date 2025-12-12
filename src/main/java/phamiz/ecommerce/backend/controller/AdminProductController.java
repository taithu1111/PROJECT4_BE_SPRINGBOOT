package phamiz.ecommerce.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import phamiz.ecommerce.backend.dto.ApiResponse;
import phamiz.ecommerce.backend.dto.Product.CreateProductRequest;
import phamiz.ecommerce.backend.dto.Product.ProductDTO;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.model.Product;
import phamiz.ecommerce.backend.service.IProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminProductController {
    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);
    private final IProductService productService;

    @GetMapping("")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.findAllProduct();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<Product> createProduct(@RequestBody @Valid CreateProductRequest req) {
        logger.info("AdminProductController - createProduct called");
        logger.debug("AdminProductController - Request: {}", req);
        Product product = productService.createProduct(req);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long productId) throws ProductException {
        productService.deleteProduct(productId);
        ApiResponse res = new ApiResponse();
        res.setMessage("Product deleted successfully");
        res.setStatus(true);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@RequestBody @Valid CreateProductRequest req,
            @PathVariable Long productId)
            throws ProductException {
        Product product = productService.updateProduct(productId, req);
        return new ResponseEntity<>(productService.toDTO(product), HttpStatus.OK);
    }

    @PostMapping("/creates")
    public ResponseEntity<ApiResponse> createMultipleProduct(@RequestBody List<@Valid CreateProductRequest> reqs) {
        productService.createProducts(reqs);
        ApiResponse res = new ApiResponse();
        res.setMessage("Products created successfully");
        res.setStatus(true);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }
}

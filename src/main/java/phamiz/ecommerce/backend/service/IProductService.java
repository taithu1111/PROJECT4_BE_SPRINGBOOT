package phamiz.ecommerce.backend.service;

import org.springframework.data.domain.Page;
import phamiz.ecommerce.backend.dto.Product.CreateProductRequest;
import phamiz.ecommerce.backend.dto.Product.ProductDTO;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.model.Product;

import java.util.List;

public interface IProductService {
    ProductDTO toDTO(Product product);

    List<ProductDTO> findAllProduct();

    Product createProduct(CreateProductRequest request);

    String deleteProduct(Long productId) throws ProductException;

    Product updateProduct(Long productId, CreateProductRequest req) throws ProductException;

    Product findProductById(Long id) throws ProductException;

    Page<ProductDTO> getAllProduct(String category, List<String> colors,
            Integer minPrice, Integer maxPrice, String sort,
            Integer pageNumber, Integer pageSize) throws ProductException;

    void createProducts(List<CreateProductRequest> reqs);

    List<ProductDTO> getNewProduct();

    List<ProductDTO> getRandomProduct();
}

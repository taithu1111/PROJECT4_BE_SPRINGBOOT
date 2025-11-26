package phamiz.ecommerce.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import phamiz.ecommerce.backend.dto.Product.ProductDTO;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.model.Product;
import phamiz.ecommerce.backend.model.ProductColor;
import phamiz.ecommerce.backend.repositories.ICategoryRepository;
import phamiz.ecommerce.backend.repositories.IProductRepository;
import phamiz.ecommerce.backend.service.serviceImpl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private IProductRepository productRepository;

    @Mock
    private ICategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    @DisplayName("findAllProduct - Should return list of ProductDTOs")
    void shouldReturnListOfProductDTOs() {
        Product product = new Product();
        product.setId(1L);
        product.setProduct_name("Test Product");
        product.setImages(new ArrayList<>());
        product.setProductColors(new HashSet<>());
        product.setReviews(new ArrayList<>());
        product.setRatings(new ArrayList<>());

        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));

        List<ProductDTO> result = productService.findAllProduct();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getProductName());
    }

    @Test
    @DisplayName("findProductById - Should return product when found")
    void shouldReturnProductWhenFound() throws ProductException {
        Product product = new Product();
        product.setId(1L);
        product.setProduct_name("Test Product");
        product.setImages(new ArrayList<>());
        product.setProductColors(new HashSet<>());
        product.setReviews(new ArrayList<>());
        product.setRatings(new ArrayList<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.findProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getProduct_name());
    }

    @Test
    @DisplayName("findProductById - Should throw ProductException when not found")
    void shouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ProductException exception = assertThrows(ProductException.class, () -> {
            productService.findProductById(999L);
        });

        assertEquals("Product not found with 999", exception.getMessage());
    }

    // ==================== FILTER TESTS ====================

    @Test
    @DisplayName("getAllProduct - Should filter by category")
    void shouldFilterByCategory() throws Exception {
        Product product1 = createProduct(1L, "T-Shirt", 50, "Red");

        List<Product> categoryFilteredProducts = Collections.singletonList(product1);
        when(productRepository.filterProducts("Clothing", null, null, null))
                .thenReturn(categoryFilteredProducts);

        Page<ProductDTO> result = productService.getAllProduct("Clothing", Collections.emptyList(), null, null, null, 0,
                10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("T-Shirt", result.getContent().get(0).getProductName());
        verify(productRepository).filterProducts("Clothing", null, null, null);
    }

    @Test
    @DisplayName("getAllProduct - Should filter by price range")
    void shouldFilterByPriceRange() throws Exception {
        Product product1 = createProduct(1L, "Cheap Product", 50, "Red");

        List<Product> priceFilteredProducts = Collections.singletonList(product1);
        when(productRepository.filterProducts(null, 40, 100, null))
                .thenReturn(priceFilteredProducts);

        Page<ProductDTO> result = productService.getAllProduct(null, Collections.emptyList(), 40, 100, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Cheap Product", result.getContent().get(0).getProductName());
        verify(productRepository).filterProducts(null, 40, 100, null);
    }

    @Test
    @DisplayName("getAllProduct - Should filter by sort option")
    void shouldFilterBySort() throws Exception {
        Product product1 = createProduct(1L, "Product A", 100, "Red");
        Product product2 = createProduct(2L, "Product B", 50, "Blue");

        // Assuming sort="price_low" sorts by price ascending
        List<Product> sortedProducts = Arrays.asList(product2, product1);
        when(productRepository.filterProducts(null, null, null, "price_low"))
                .thenReturn(sortedProducts);

        Page<ProductDTO> result = productService.getAllProduct(null, Collections.emptyList(), null, null, "price_low",
                0, 10);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(productRepository).filterProducts(null, null, null, "price_low");
    }

    @Test
    @DisplayName("getAllProduct - Should filter by color (in-memory)")
    void shouldFilterByColor() throws Exception {
        Product product1 = createProduct(1L, "Red Shirt", 100, "Red");
        Product product2 = createProduct(2L, "Blue Shirt", 100, "Blue");

        List<Product> allProducts = Arrays.asList(product1, product2);
        when(productRepository.filterProducts(any(), any(), any(), any())).thenReturn(allProducts);

        List<String> colorFilter = Collections.singletonList("Red");
        Page<ProductDTO> result = productService.getAllProduct(null, colorFilter, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Red Shirt", result.getContent().get(0).getProductName());
    }

    @Test
    @DisplayName("getAllProduct - Should filter by multiple colors")
    void shouldFilterByMultipleColors() throws Exception {
        Product product1 = createProduct(1L, "Red Shirt", 100, "Red");
        Product product2 = createProduct(2L, "Blue Shirt", 100, "Blue");
        Product product3 = createProduct(3L, "Green Shirt", 100, "Green");

        List<Product> allProducts = Arrays.asList(product1, product2, product3);
        when(productRepository.filterProducts(any(), any(), any(), any())).thenReturn(allProducts);

        List<String> colorFilter = Arrays.asList("Red", "Blue");
        Page<ProductDTO> result = productService.getAllProduct(null, colorFilter, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    @DisplayName("getAllProduct - Should apply combined filters (category + price + color)")
    void shouldApplyCombinedFilters() throws Exception {
        Product product1 = createProduct(1L, "Red Shirt", 50, "Red");
        Product product2 = createProduct(2L, "Blue Shirt", 60, "Blue");

        // Repository filters by category and price
        List<Product> repositoryFiltered = Arrays.asList(product1, product2);
        when(productRepository.filterProducts("Clothing", 40, 100, null))
                .thenReturn(repositoryFiltered);

        // Service filters by color
        List<String> colorFilter = Collections.singletonList("Red");
        Page<ProductDTO> result = productService.getAllProduct("Clothing", colorFilter, 40, 100, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Red Shirt", result.getContent().get(0).getProductName());
    }

    @Test
    @DisplayName("getAllProduct - Should paginate results correctly")
    void shouldPaginateResultsCorrectly() throws Exception {
        // Create 25 products
        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            products.add(createProduct((long) i, "Product " + i, 100, "Red"));
        }

        when(productRepository.filterProducts(any(), any(), any(), any())).thenReturn(products);

        // Get page 0, size 10
        Page<ProductDTO> page1 = productService.getAllProduct(null, Collections.emptyList(), null, null, null, 0, 10);
        assertEquals(10, page1.getContent().size());
        assertEquals(25, page1.getTotalElements());
        assertEquals(3, page1.getTotalPages());

        // Get page 1, size 10
        Page<ProductDTO> page2 = productService.getAllProduct(null, Collections.emptyList(), null, null, null, 1, 10);
        assertEquals(10, page2.getContent().size());

        // Get page 2, size 10 (last page)
        Page<ProductDTO> page3 = productService.getAllProduct(null, Collections.emptyList(), null, null, null, 2, 10);
        assertEquals(5, page3.getContent().size());
    }

    @Test
    @DisplayName("getAllProduct - Should throw exception when no products found")
    void shouldThrowExceptionWhenNoProductsFound() {
        when(productRepository.filterProducts(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        ProductException exception = assertThrows(ProductException.class, () -> {
            productService.getAllProduct("NonExistent", Collections.emptyList(), null, null, null, 0, 10);
        });

        assertEquals("Can not found any product", exception.getMessage());
    }

    // Helper method to create products
    private Product createProduct(Long id, String name, Integer price, String colorName) {
        Product product = new Product();
        product.setId(id);
        product.setProduct_name(name);
        product.setPrice(price);
        product.setImages(new ArrayList<>());
        product.setReviews(new ArrayList<>());
        product.setRatings(new ArrayList<>());

        Set<ProductColor> colors = new HashSet<>();
        ProductColor color = new ProductColor();
        color.setColor_name(colorName);
        colors.add(color);
        product.setProductColors(colors);

        return product;
    }
}

package phamiz.ecommerce.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import phamiz.ecommerce.backend.model.*;
import phamiz.ecommerce.backend.repositories.ICategoryRepository;
import phamiz.ecommerce.backend.repositories.IProductImageRepository;
import phamiz.ecommerce.backend.repositories.IProductRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AdminProductDeletionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private IProductImageRepository productImageRepository;

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Category category;
    private Product testProduct;

    @BeforeEach
    public void setup() {
        // Create category
        Category parentCategory = new Category();
        parentCategory.setCategory_name("Electronics");
        parentCategory.setLevel(1);
        parentCategory = categoryRepository.save(parentCategory);

        category = new Category();
        category.setCategory_name("Smartphones");
        category.setParent_category(parentCategory);
        category.setLevel(2);
        category = categoryRepository.save(category);

        // Create product with colors and images
        testProduct = new Product();
        testProduct.setProduct_name("Test Phone");
        testProduct.setBrand("TestBrand");
        testProduct.setPrice(500);
        testProduct.setQuantity(10);
        testProduct.setCategory(category);
        testProduct.setCreatedAt(LocalDateTime.now());

        // Add colors
        Set<ProductColor> colors = new HashSet<>();
        ProductColor color1 = new ProductColor();
        color1.setColor_name("Red");
        colors.add(color1);
        ProductColor color2 = new ProductColor();
        color2.setColor_name("Blue");
        colors.add(color2);
        testProduct.setProductColors(colors);

        // Save product first
        testProduct = productRepository.save(testProduct);

        // Add images
        ProductImage image1 = new ProductImage();
        image1.setImageUrl("http://example.com/image1.jpg");
        image1.setProduct(testProduct);
        productImageRepository.save(image1);

        ProductImage image2 = new ProductImage();
        image2.setImageUrl("http://example.com/image2.jpg");
        image2.setProduct(testProduct);
        productImageRepository.save(image2);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testDeleteProduct_ShouldDeleteImagesAndColorsFirst() throws Exception {
        Long productId = testProduct.getId();

        // Verify product exists with images and colors
        assertTrue(productRepository.findById(productId).isPresent());
        assertEquals(2, productImageRepository.findAll().stream()
                .filter(img -> img.getProduct().getId().equals(productId))
                .count());

        // Delete the product
        mockMvc.perform(delete("/api/admin/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Product deleted successfully"));

        // Verify product is deleted
        assertFalse(productRepository.findById(productId).isPresent());

        // Verify images are deleted
        assertEquals(0, productImageRepository.findAll().stream()
                .filter(img -> img.getProduct() != null && img.getProduct().getId().equals(productId))
                .count());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testDeleteProduct_ShouldNotCauseConstraintViolation() throws Exception {
        Long productId = testProduct.getId();

        // This should not throw any foreign key constraint violations
        assertDoesNotThrow(() -> {
            mockMvc.perform(delete("/api/admin/products/" + productId))
                    .andExpect(status().isOk());
        });
    }
}

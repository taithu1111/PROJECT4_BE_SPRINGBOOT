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
import phamiz.ecommerce.backend.repositories.*;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AdminDeleteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private IReviewRepository reviewRepository;

    @Autowired
    private IRatingRepository ratingRepository;

    @Autowired
    private ICategoryRepository categoryRepository;

    private User testUser;
    private Product testProduct;
    private Review testReview;
    private Rating testRating;

    @BeforeEach
    public void setup() {
        // Create test user
        testUser = new User();
        testUser.setEmail("user@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password123");
        testUser.setRole("ROLE_USER");
        testUser.setMobile("1234567890");
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // Create test product
        Category category = new Category();
        category.setCategory_name("TestCategory");
        category.setLevel(1);
        category = categoryRepository.save(category);

        testProduct = new Product();
        testProduct.setProduct_name("Test Product");
        testProduct.setBrand("TestBrand");
        testProduct.setPrice(100);
        testProduct.setQuantity(10);
        testProduct.setCategory(category);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct = productRepository.save(testProduct);

        // Create test review
        testReview = new Review();
        testReview.setUser(testUser);
        testReview.setProduct(testProduct);
        testReview.setReview("Review to be deleted");
        testReview.setCreatedAt(LocalDateTime.now());
        testReview = reviewRepository.save(testReview);

        // Create test rating
        testRating = new Rating();
        testRating.setUser(testUser);
        testRating.setProduct(testProduct);
        testRating.setRating(4.0);
        testRating.setCreatedAt(LocalDateTime.now());
        testRating = ratingRepository.save(testRating);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testAdminDeleteReview_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/reviews/" + testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Review deleted successfully"));

        // Verify review was deleted
        assertFalse(reviewRepository.findById(testReview.getId()).isPresent());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testAdminDeleteRating_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/ratings/" + testRating.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Rating deleted successfully"));

        // Verify rating was deleted
        assertFalse(ratingRepository.findById(testRating.getId()).isPresent());
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void testUserCannotDeleteReviewViaAdminApi() throws Exception {
        mockMvc.perform(delete("/api/admin/reviews/" + testReview.getId()))
                .andExpect(status().isForbidden());
    }
}

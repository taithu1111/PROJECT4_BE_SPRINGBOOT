package phamiz.ecommerce.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import phamiz.ecommerce.backend.config.JwtProvider;
import phamiz.ecommerce.backend.model.*;
import phamiz.ecommerce.backend.repositories.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserCRUDIntegrationTest {

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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    private User testUser;
    private Product testProduct;
    private Review testReview;
    private Rating testRating;
    private String validJwtToken;

    @BeforeEach
    public void setup() {
        // Create test user
        testUser = new User();
        testUser.setEmail("testuser@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password123");
        testUser.setRole("ROLE_USER");
        testUser.setMobile("1234567890");
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // Generate real JWT token for integration tests
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUser.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        validJwtToken = jwtProvider.generateToken(authentication);

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
        testReview.setReview("Initial review text");
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
    @WithMockUser(username = "testuser@test.com", roles = { "USER" })
    public void testUpdateUserProfile_Success() throws Exception {
        String requestBody = """
                {
                    "firstName": "Updated",
                    "lastName": "Name",
                    "mobile": "9876543210"
                }
                """;

        mockMvc.perform(put("/api/users/profile")
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));
    }

    @Test
    @WithMockUser(username = "testuser@test.com", roles = { "USER" })
    public void testUpdateReview_Success() throws Exception {
        String requestBody = """
                {
                    "review": "Updated review text with more details"
                }
                """;

        mockMvc.perform(put("/api/reviews/" + testReview.getId())
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review").value("Updated review text with more details"));
    }

    @Test
    @WithMockUser(username = "testuser@test.com", roles = { "USER" })
    public void testDeleteReview_Success() throws Exception {
        mockMvc.perform(delete("/api/reviews/" + testReview.getId())
                .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Review deleted successfully"));

        // Verify review was deleted
        assertFalse(reviewRepository.findById(testReview.getId()).isPresent());
    }

    @Test
    @WithMockUser(username = "testuser@test.com", roles = { "USER" })
    public void testUpdateRating_Success() throws Exception {
        String requestBody = """
                {
                    "rating": 5.0
                }
                """;

        mockMvc.perform(put("/api/ratings/" + testRating.getId())
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5.0));
    }

    @Test
    @WithMockUser(username = "testuser@test.com", roles = { "USER" })
    public void testDeleteRating_Success() throws Exception {
        mockMvc.perform(delete("/api/ratings/" + testRating.getId())
                .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Rating deleted successfully"));

        // Verify rating was deleted
        assertFalse(ratingRepository.findById(testRating.getId()).isPresent());
    }
}

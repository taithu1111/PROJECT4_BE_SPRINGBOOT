package phamiz.ecommerce.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import phamiz.ecommerce.backend.dto.Review.ReviewRequest;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.model.Product;
import phamiz.ecommerce.backend.model.Review;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.service.IReviewService;
import phamiz.ecommerce.backend.service.IUserService;

@WebMvcTest(controllers = ReviewController.class, excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class ReviewControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private IReviewService reviewService;

        @MockBean
        private IUserService userService;

        @MockBean
        private DataSource dataSource;

        @Test
        @DisplayName("POST /api/reviews/create - Should create review successfully")
        void shouldCreateReviewSuccessfully() throws Exception {
                // Setup mock user
                User mockUser = new User();
                mockUser.setId(1L);
                mockUser.setEmail("test@example.com");

                // Setup mock product
                Product mockProduct = new Product();
                mockProduct.setId(100L);
                mockProduct.setProduct_name("Test Product");

                // Setup request
                ReviewRequest request = new ReviewRequest();
                request.setProductId(100L);
                request.setReview("Great product! Highly recommend.");

                // Setup mock review
                Review mockReview = new Review();
                mockReview.setId(1L);
                mockReview.setProduct(mockProduct);
                mockReview.setUser(mockUser);
                mockReview.setReview(request.getReview());
                mockReview.setCreatedAt(LocalDateTime.now());

                // Mock service calls
                when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
                when(reviewService.createReview(any(ReviewRequest.class), eq(mockUser))).thenReturn(mockReview);

                // Perform request
                mockMvc.perform(post("/api/reviews/create")
                                .header("Authorization", "Bearer mock-jwt-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.review").value("Great product! Highly recommend."));
        }

        @Test
        @DisplayName("POST /api/reviews/create - Should handle ProductException when product not found")
        void shouldHandleProductExceptionWhenProductNotFound() throws Exception {
                // Setup mock user
                User mockUser = new User();
                mockUser.setId(1L);
                mockUser.setEmail("test@example.com");

                // Setup request with invalid product ID
                ReviewRequest request = new ReviewRequest();
                request.setProductId(999L);
                request.setReview("This should fail");

                // Mock service calls - product not found
                when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
                when(reviewService.createReview(any(ReviewRequest.class), eq(mockUser)))
                                .thenThrow(new ProductException("Product not found with 999"));

                // Perform request
                mockMvc.perform(post("/api/reviews/create")
                                .header("Authorization", "Bearer mock-jwt-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest()); // ProductException returns BAD_REQUEST
        }

        @Test
        @DisplayName("POST /api/reviews/create - Should create review with long text")
        void shouldCreateReviewWithLongText() throws Exception {
                // Setup mock user
                User mockUser = new User();
                mockUser.setId(2L);
                mockUser.setEmail("customer@example.com");

                // Setup mock product
                Product mockProduct = new Product();
                mockProduct.setId(200L);
                mockProduct.setProduct_name("Premium Product");

                // Setup request with long review text
                ReviewRequest request = new ReviewRequest();
                request.setProductId(200L);
                request.setReview("This is an excellent product that I have been using for several months now. " +
                                "The quality is outstanding and it has exceeded all my expectations. " +
                                "I would definitely recommend this to anyone looking for a reliable solution.");

                // Setup mock review
                Review mockReview = new Review();
                mockReview.setId(2L);
                mockReview.setProduct(mockProduct);
                mockReview.setUser(mockUser);
                mockReview.setReview(request.getReview());
                mockReview.setCreatedAt(LocalDateTime.now());

                // Mock service calls
                when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
                when(reviewService.createReview(any(ReviewRequest.class), eq(mockUser))).thenReturn(mockReview);

                // Perform request
                mockMvc.perform(post("/api/reviews/create")
                                .header("Authorization", "Bearer mock-jwt-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(2));
        }

        @Test
        @DisplayName("POST /api/reviews/create - Should create review with short text")
        void shouldCreateReviewWithShortText() throws Exception {
                // Setup mock user
                User mockUser = new User();
                mockUser.setId(3L);
                mockUser.setEmail("buyer@example.com");

                // Setup mock product
                Product mockProduct = new Product();
                mockProduct.setId(300L);
                mockProduct.setProduct_name("Basic Product");

                // Setup request with short review
                ReviewRequest request = new ReviewRequest();
                request.setProductId(300L);
                request.setReview("Good!");

                // Setup mock review
                Review mockReview = new Review();
                mockReview.setId(3L);
                mockReview.setProduct(mockProduct);
                mockReview.setUser(mockUser);
                mockReview.setReview(request.getReview());
                mockReview.setCreatedAt(LocalDateTime.now());

                // Mock service calls
                when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
                when(reviewService.createReview(any(ReviewRequest.class), eq(mockUser))).thenReturn(mockReview);

                // Perform request
                mockMvc.perform(post("/api/reviews/create")
                                .header("Authorization", "Bearer mock-jwt-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(3));
        }

        // ==================== GET /api/reviews/product/{productId} TESTS
        // ====================

        @Test
        @DisplayName("GET /api/reviews/product/{productId} - Should return list of reviews")
        void shouldReturnListOfReviews() throws Exception {
                // Setup mock product
                Product mockProduct = new Product();
                mockProduct.setId(100L);
                mockProduct.setProduct_name("Test Product");

                // Setup mock reviews
                Review review1 = new Review();
                review1.setId(1L);
                review1.setProduct(mockProduct);
                review1.setReview("Review 1");
                review1.setCreatedAt(LocalDateTime.now());

                Review review2 = new Review();
                review2.setId(2L);
                review2.setProduct(mockProduct);
                review2.setReview("Review 2");
                review2.setCreatedAt(LocalDateTime.now());

                List<Review> reviews = Arrays.asList(review1, review2);

                // Mock service calls
                when(reviewService.getAllReview(100L)).thenReturn(reviews);

                // Perform request
                mockMvc.perform(get("/api/reviews/product/100")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].review").value("Review 1"))
                                .andExpect(jsonPath("$[1].review").value("Review 2"));
        }

        @Test
        @DisplayName("GET /api/reviews/product/{productId} - Should return empty list when no reviews")
        void shouldReturnEmptyListWhenNoReviews() throws Exception {
                // Mock service calls - no reviews
                when(reviewService.getAllReview(999L)).thenReturn(new ArrayList<>());

                // Perform request
                mockMvc.perform(get("/api/reviews/product/999")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(0));
        }
}

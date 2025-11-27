package phamiz.ecommerce.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import phamiz.ecommerce.backend.config.JwtProvider;
import phamiz.ecommerce.backend.dto.Cart.AddItemRequest;
import phamiz.ecommerce.backend.model.Address;
import phamiz.ecommerce.backend.model.Category;
import phamiz.ecommerce.backend.model.Product;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.repositories.ICartRepository;
import phamiz.ecommerce.backend.repositories.ICategoryRepository;
import phamiz.ecommerce.backend.repositories.IOrderRepository;
import phamiz.ecommerce.backend.repositories.IProductRepository;
import phamiz.ecommerce.backend.repositories.UserRepository;
import phamiz.ecommerce.backend.service.ICartService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class ConcurrencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private ICartRepository cartRepository;

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private ICartService cartService;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Product testProduct;
    private List<String> userTokens;
    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        // Clear DB
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create Category
        Category category = new Category();
        category.setCategory_name("Test Category");
        category.setLevel(1);
        category = categoryRepository.save(category);

        // Create Product with limited stock
        testProduct = new Product();
        testProduct.setProduct_name("Limited Stock Product");
        testProduct.setPrice(100);
        testProduct.setQuantity(5); // Only 5 items available
        testProduct.setCategory(category);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setDescription("Test Description");
        testProduct = productRepository.save(testProduct);

        // Create 10 users
        userTokens = new ArrayList<>();
        testUsers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setEmail("user" + i + "@test.com");
            user.setPassword(passwordEncoder.encode("password"));
            user.setFirstName("User" + i);
            user.setLastName("Test");
            user.setRole("ROLE_USER");
            user = userRepository.save(user);
            testUsers.add(user);

            // Create cart for each user
            cartService.createCart(user);

            // Generate token
            Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            String token = "Bearer " + jwtProvider.generateToken(auth);
            userTokens.add(token);
        }
    }

    @Test
    @DisplayName("Concurrency Test: 10 users try to buy 1 item each from a product with only 5 in stock")
    void testConcurrentOrderCreation() throws Exception {
        int numberOfThreads = 10;
        int itemsInStock = 5;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final int userIndex = i;
            executorService.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();

                    // 1. Add to cart
                    AddItemRequest req = new AddItemRequest();
                    req.setProductId(testProduct.getId());
                    req.setQuantity(1);
                    req.setPrice(100);

                    mockMvc.perform(put("/api/cart/add")
                            .header("Authorization", userTokens.get(userIndex))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)));

                    // 2. Create order
                    Address shippingAddress = new Address();
                    shippingAddress.setStreetAddress("123 Test St");
                    shippingAddress.setCity("Test City");
                    shippingAddress.setZipCode("12345");

                    var result = mockMvc.perform(post("/api/orders/")
                            .header("Authorization", userTokens.get(userIndex))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(shippingAddress)))
                            .andReturn();

                    int status = result.getResponse().getStatus();
                    String response = result.getResponse().getContentAsString();

                    System.out.println("User " + userIndex + " - Status: " + status + " - Response: " + response);

                    if (status == 201) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for all threads to complete
        endLatch.await();
        executorService.shutdown();

        // Verify results
        System.out.println("Success: " + successCount.get());
        System.out.println("Failure: " + failureCount.get());

        // Exactly 5 should succeed, 5 should fail
        assertEquals(itemsInStock, successCount.get(), "Exactly 5 orders should succeed");
        assertEquals(numberOfThreads - itemsInStock, failureCount.get(), "Exactly 5 orders should fail");

        // Verify final product quantity is 0 (not negative!)
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(0, updatedProduct.getQuantity(), "Product quantity should be exactly 0, not negative");
    }
}

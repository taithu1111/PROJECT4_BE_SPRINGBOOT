package phamiz.ecommerce.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collections;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import phamiz.ecommerce.backend.config.JwtProvider;
import phamiz.ecommerce.backend.dto.Cart.AddItemRequest;
import phamiz.ecommerce.backend.model.Address;
import phamiz.ecommerce.backend.model.Cart;
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
@Transactional
public class InventoryIntegrationTest {

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

    private User testUser;
    private User adminUser;
    private Product testProduct;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clear DB
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create User
        testUser = new User();
        testUser.setEmail("user@test.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setFirstName("User");
        testUser.setLastName("Test");
        testUser.setRole("ROLE_USER");
        testUser = userRepository.save(testUser);

        Authentication userAuth = new UsernamePasswordAuthenticationToken(testUser.getEmail(), null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        userToken = "Bearer " + jwtProvider.generateToken(userAuth);

        // Create Admin
        adminUser = new User();
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("Test");
        adminUser.setRole("ROLE_ADMIN");
        adminUser = userRepository.save(adminUser);

        Authentication adminAuth = new UsernamePasswordAuthenticationToken(adminUser.getEmail(), null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        adminToken = "Bearer " + jwtProvider.generateToken(adminAuth);

        // Create Category
        Category category = new Category();
        category.setCategory_name("Test Category");
        category.setLevel(1);
        category = categoryRepository.save(category);

        // Create Product
        testProduct = new Product();
        testProduct.setProduct_name("Test Product");
        testProduct.setPrice(100);
        testProduct.setQuantity(10); // Initial Quantity 10
        testProduct.setCategory(category);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setDescription("Test Description");
        testProduct = productRepository.save(testProduct);

        // Create Cart for User
        cartService.createCart(testUser);
    }

    @Test
    @DisplayName("Add to Cart: Try to add more items than available in stock. Expect error.")
    void testAddCartItem_InsufficientStock() throws Exception {
        AddItemRequest req = new AddItemRequest();
        req.setProductId(testProduct.getId());
        req.setQuantity(11); // More than 10
        req.setPrice(100);

        mockMvc.perform(put("/api/cart/add")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest()); // Expect 400
    }

    @Test
    @DisplayName("Create Order: Create an order. Verify product quantity decreases in DB.")
    void testCreateOrder_DeductStock() throws Exception {
        // 1. Add to Cart (5 items)
        AddItemRequest req = new AddItemRequest();
        req.setProductId(testProduct.getId());
        req.setQuantity(5);
        req.setPrice(100);

        mockMvc.perform(put("/api/cart/add")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // 2. Create Order
        Address shippingAddress = new Address();
        shippingAddress.setStreetAddress("123 Test St");
        shippingAddress.setCity("Test City");
        shippingAddress.setZipCode("12345");

        mockMvc.perform(post("/api/orders/")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shippingAddress)))
                .andExpect(status().isCreated());

        // 3. Verify Stock
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assert updatedProduct.getQuantity() == 5; // 10 - 5 = 5
    }

    @Test
    @DisplayName("Cancel Order: Cancel the order. Verify product quantity increases in DB.")
    void testCancelOrder_RestoreStock() throws Exception {
        // 1. Add to Cart (5 items)
        AddItemRequest req = new AddItemRequest();
        req.setProductId(testProduct.getId());
        req.setQuantity(5);
        req.setPrice(100);

        mockMvc.perform(put("/api/cart/add")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // 2. Create Order
        Address shippingAddress = new Address();
        shippingAddress.setStreetAddress("123 Test St");
        shippingAddress.setCity("Test City");
        shippingAddress.setZipCode("12345");

        MvcResult result = mockMvc.perform(post("/api/orders/")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shippingAddress)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        phamiz.ecommerce.backend.model.Order createdOrder = objectMapper.readValue(responseContent,
                phamiz.ecommerce.backend.model.Order.class);
        Long orderId = createdOrder.getId();

        // Verify Stock Deducted
        Product productAfterOrder = productRepository.findById(testProduct.getId()).orElseThrow();
        assert productAfterOrder.getQuantity() == 5;

        // 3. Cancel Order (Admin)
        mockMvc.perform(put("/api/admin/orders/" + orderId + "/cancel")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());

        // 4. Verify Stock Restored
        Product productAfterCancel = productRepository.findById(testProduct.getId()).orElseThrow();
        assert productAfterCancel.getQuantity() == 10; // 5 + 5 = 10
    }
}

package phamiz.ecommerce.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import phamiz.ecommerce.backend.dto.Product.CreateProductRequest;
import phamiz.ecommerce.backend.model.Category;
import phamiz.ecommerce.backend.model.Product;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.repositories.ICategoryRepository;
import phamiz.ecommerce.backend.repositories.IProductRepository;
import phamiz.ecommerce.backend.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AdminDataFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private phamiz.ecommerce.backend.config.JwtProvider jwtProvider;

    private User adminUser;
    private User normalUser;
    private Product testProduct;
    private String adminJwt;

    @BeforeEach
    public void setup() {
        // Create Admin User
        adminUser = new User();
        adminUser.setEmail("admin@test.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPassword("password123");
        adminUser.setRole("ROLE_ADMIN");
        adminUser.setMobile("0000000000");
        adminUser.setActive(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser = userRepository.save(adminUser);

        // Generate JWT for Admin
        org.springframework.security.core.Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                adminUser.getEmail(),
                null,
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
        adminJwt = jwtProvider.generateToken(authentication);

        // Create Normal User
        normalUser = new User();
        normalUser.setEmail("user@test.com");
        normalUser.setFirstName("Normal");
        normalUser.setLastName("User");
        normalUser.setPassword("password123");
        normalUser.setRole("ROLE_USER");
        normalUser.setMobile("1234567890");
        normalUser.setActive(true);
        normalUser.setCreatedAt(LocalDateTime.now());
        normalUser = userRepository.save(normalUser);

        // Setup Categories
        Category parent = new Category();
        parent.setCategory_name("Electronics");
        parent.setLevel(1);
        parent = categoryRepository.save(parent);

        Category child = new Category();
        child.setCategory_name("Laptops");
        child.setLevel(2);
        child.setParent_category(parent);
        categoryRepository.save(child);

        // Create Product
        testProduct = new Product();
        testProduct.setProduct_name("Existing Product");
        testProduct.setBrand("BrandX");
        testProduct.setPrice(1000);
        testProduct.setQuantity(50);
        testProduct.setCategory(child);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = { "ADMIN" })
    public void testAdminGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].email").exists());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = { "ADMIN" })
    public void testAdminToggleUserStatus() throws Exception {
        // Initial state: Normal user is active
        assertTrue(normalUser.isActive());

        // Lock user
        mockMvc.perform(put("/api/admin/users/" + normalUser.getId() + "/status")
                .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Verify in DB
        User updatedUser = userRepository.findById(normalUser.getId()).get();
        assertFalse(updatedUser.isActive());

        // Unlock user
        mockMvc.perform(put("/api/admin/users/" + normalUser.getId() + "/status")
                .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = { "ADMIN" })
    public void testAdminCreateProduct() throws Exception {
        CreateProductRequest req = new CreateProductRequest();
        req.setTitle("New Gaming Laptop");
        req.setDescription("High performance");
        req.setPrice(2000);
        req.setQuantity(10);
        req.setBrand("Alienware");
        req.setFirstLevelCategory("Electronics");
        req.setSecondLevelCategory("Laptops");
        req.setColors(new HashSet<>());
        req.setImages(List.of());

        mockMvc.perform(post("/api/admin/products/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.product_name").value("New Gaming Laptop"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = { "ADMIN" })
    public void testAdminCreateMultipleProducts() throws Exception {
        CreateProductRequest req1 = new CreateProductRequest();
        req1.setTitle("Batch Product 1");
        req1.setDescription("Desc 1");
        req1.setPrice(100);
        req1.setQuantity(5);
        req1.setBrand("BrandA");
        req1.setFirstLevelCategory("Electronics");
        req1.setSecondLevelCategory("Laptops");
        req1.setColors(new HashSet<>());
        req1.setImages(List.of());

        CreateProductRequest req2 = new CreateProductRequest();
        req2.setTitle("Batch Product 2");
        req2.setDescription("Desc 2");
        req2.setPrice(200);
        req2.setQuantity(5);
        req2.setBrand("BrandB");
        req2.setFirstLevelCategory("Electronics");
        req2.setSecondLevelCategory("Laptops");
        req2.setColors(new HashSet<>());
        req2.setImages(List.of());

        List<CreateProductRequest> reqs = List.of(req1, req2);

        mockMvc.perform(post("/api/admin/products/creates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqs)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Products created successfully"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = { "ADMIN" })
    public void testAdminUpdateProduct() throws Exception {
        CreateProductRequest req = new CreateProductRequest();
        req.setTitle("Updated Product Name");
        req.setDescription("Updated desc");
        req.setPrice(1500);
        req.setQuantity(40);
        req.setBrand("BrandX");
        req.setFirstLevelCategory("Electronics");
        req.setSecondLevelCategory("Laptops");

        mockMvc.perform(put("/api/admin/products/" + testProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product_name").value("Updated Product Name"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = { "ADMIN" })
    public void testAdminGetStatistics() throws Exception {
        mockMvc.perform(get("/api/admin/statistics"))
                .andExpect(status().isOk());
    }
}

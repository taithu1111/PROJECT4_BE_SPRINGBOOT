package phamiz.ecommerce.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import phamiz.ecommerce.backend.dto.Auth.SignupRequest;
import phamiz.ecommerce.backend.repositories.UserRepository;

/**
 * Integration test for POST /auth/signup endpoint.
 * Tests the full signup flow with real database interactions.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class SignupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clean up any existing test user
        userRepository.findAll().forEach(user -> {
            if (user.getEmail().startsWith("test")) {
                userRepository.delete(user);
            }
        });
    }

    @Test
    @DisplayName("POST /auth/signup - Should create new user and return 201 with token")
    void testSignupSuccess() throws Exception {
        // Arrange
        SignupRequest request = new SignupRequest();
        request.setEmail("test.user@example.com");
        request.setPassword("StrongP@ssw0rd");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setMobile("0123456789");

        // Act & Assert
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").value("Signup Success!"));
    }

    @Test
    @DisplayName("POST /auth/signup - Should return 409 when email already exists")
    void testSignupDuplicateEmail() throws Exception {
        // Arrange - Create first user
        SignupRequest request1 = new SignupRequest();
        request1.setEmail("test.duplicate@example.com");
        request1.setPassword("StrongP@ssw0rd");
        request1.setFirstName("First");
        request1.setLastName("User");
        request1.setMobile("0123456789");

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Act - Try to create another user with same email
        SignupRequest request2 = new SignupRequest();
        request2.setEmail("test.duplicate@example.com");
        request2.setPassword("AnotherP@ssw0rd");
        request2.setFirstName("Second");
        request2.setLastName("User");
        request2.setMobile("0987654321");

        // Assert - Should get conflict
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /auth/signup - Should return 400 for invalid email")
    void testSignupInvalidEmail() throws Exception {
        // Arrange
        SignupRequest request = new SignupRequest();
        request.setEmail("invalid-email");
        request.setPassword("StrongP@ssw0rd");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setMobile("0123456789");

        // Act & Assert
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/signup - Should return 400 for weak password")
    void testSignupWeakPassword() throws Exception {
        // Arrange
        SignupRequest request = new SignupRequest();
        request.setEmail("test.weak@example.com");
        request.setPassword("weak");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setMobile("0123456789");

        // Act & Assert
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

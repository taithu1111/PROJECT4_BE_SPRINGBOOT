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

import phamiz.ecommerce.backend.dto.Auth.LoginRequest;
import phamiz.ecommerce.backend.dto.Auth.SignupRequest;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.repositories.UserRepository;

/**
 * Integration test for POST /auth/signin endpoint.
 * Tests the full signin/authentication flow with real database interactions.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class SigninIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_EMAIL = "signin.test@example.com";
    private static final String TEST_PASSWORD = "StrongP@ssw0rd";

    @BeforeEach
    void setUp() throws Exception {
        // Clean up any existing test user
        User existingUser = userRepository.findByEmail(TEST_EMAIL);
        if (existingUser != null) {
            userRepository.delete(existingUser);
        }

        // Create a test user via signup API
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail(TEST_EMAIL);
        signupRequest.setPassword(TEST_PASSWORD);
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");
        signupRequest.setMobile("0123456789");

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /auth/signin - Should login successfully and return 201 with token")
    void testSigninSuccess() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        // Act & Assert
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").value("Signin Success!"));
    }

    @Test
    @DisplayName("POST /auth/signin - Should return 401 for invalid email")
    void testSigninInvalidEmail() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword(TEST_PASSWORD);

        // Act & Assert
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid Username"));
    }

    @Test
    @DisplayName("POST /auth/signin - Should return 401 for wrong password")
    void testSigninWrongPassword() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword("WrongPassword123@");

        // Act & Assert
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid Password"));
    }

    @Test
    @DisplayName("POST /auth/signin - Should return 401 for locked account")
    void testSigninLockedAccount() throws Exception {
        // Arrange - Lock the account
        User user = userRepository.findByEmail(TEST_EMAIL);
        user.setActive(false);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        // Act & Assert
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Account has been locked. Please contact support."));
    }

    @Test
    @DisplayName("POST /auth/signin - Should return 400 for invalid email format")
    void testSigninInvalidEmailFormat() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setPassword(TEST_PASSWORD);

        // Act & Assert
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/signin - Should return 400 for missing password")
    void testSigninMissingPassword() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        // Password not set

        // Act & Assert
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

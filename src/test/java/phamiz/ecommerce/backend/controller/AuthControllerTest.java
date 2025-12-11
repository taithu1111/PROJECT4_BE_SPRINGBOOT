package phamiz.ecommerce.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import phamiz.ecommerce.backend.dto.Auth.SignupRequest;
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.repositories.UserRepository;
import phamiz.ecommerce.backend.service.ICartService;
import phamiz.ecommerce.backend.service.serviceImpl.CustomUserServiceImpl;
import phamiz.ecommerce.backend.config.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Tests for {@link AuthController#createUserHandler}.
 *
 * The tests cover:
 * 1️⃣ Successful signup (201 Created, token, Location header, UTC timestamp)
 * 2️⃣ Duplicate‑email case (409 Conflict via {@link UserException})
 */
@org.springframework.boot.test.context.SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ----- Mocked collaborators -------------------------------------------------
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CustomUserServiceImpl customUserService;

    @MockBean
    private ICartService cartService;

    @MockBean
    private phamiz.ecommerce.backend.repositories.IAddressRepository addressRepository;

    // -------------------------------------------------------------------------

    /** Helper to build a valid {@link SignupRequest}. */
    private SignupRequest validRequest() {
        SignupRequest req = new SignupRequest();
        req.setEmail("newuser@example.com");
        req.setPassword("StrongP@ssw0rd");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setMobile("0123456789");
        return req;
    }

    @Test
    @DisplayName("✅ Successful signup")
    void shouldReturn201CreatedWithTokenAndLocationHeader() throws Exception {
        // ---- Arrange -------------------------------------------------------
        SignupRequest req = validRequest();

        // Mock password encoding
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encodedPassword");

        // Mock saving user
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L); // simulate generated ID
            return u;
        });

        // Mock loading UserDetails (authorities contain ROLE_USER)
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(req.getEmail())
                .password("encodedPassword")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        when(customUserService.loadUserByUsername(req.getEmail())).thenReturn(userDetails);

        // Mock JWT generation
        when(jwtProvider.generateToken(any())).thenReturn("dummy-jwt-token");

        // ---- Act -----------------------------------------------------------
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                // ---- Assert ----------------------------------------------------
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "/users/1"))
                .andExpect(jsonPath("$.token").value("dummy-jwt-token"))
                .andExpect(jsonPath("$.message").value("Signup Success!"));

        // ---- Additional verification ----------------------------------------
        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        org.mockito.Mockito.verify(userRepository).save(savedUserCaptor.capture());
        User saved = savedUserCaptor.getValue();
        // Verify UTC timestamp
        LocalDateTime createdAt = saved.getCreatedAt();
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        long diffSec = java.time.Duration.between(createdAt, nowUtc).getSeconds();
        assert Math.abs(diffSec) < 5 : "Timestamp not stored in UTC";
        // Verify password was encoded
        assert saved.getPassword().equals("encodedPassword");
    }

    @Test
    @DisplayName("❌ Duplicate‑email handling")
    void shouldReturn409ConflictWhenEmailAlreadyExists() throws Exception {
        // ---- Arrange -------------------------------------------------------
        SignupRequest req = validRequest();

        // Simulate existing user
        User existing = new User();
        existing.setId(99L);
        existing.setEmail(req.getEmail());
        when(userRepository.findByEmail(req.getEmail())).thenReturn(existing);

        // ---- Act -----------------------------------------------------------
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                // ---- Assert ----------------------------------------------------
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email is already exists with another account"));
    }
}

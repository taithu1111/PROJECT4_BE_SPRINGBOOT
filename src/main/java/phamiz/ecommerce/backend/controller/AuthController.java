package phamiz.ecommerce.backend.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import phamiz.ecommerce.backend.config.JwtProvider;
import phamiz.ecommerce.backend.dto.Auth.AuthResponse;
import phamiz.ecommerce.backend.dto.Auth.LoginRequest;
import phamiz.ecommerce.backend.dto.Auth.SignupRequest;
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.Address;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.repositories.IAddressRepository;
import phamiz.ecommerce.backend.repositories.UserRepository;
import phamiz.ecommerce.backend.service.ICartService;
import phamiz.ecommerce.backend.service.serviceImpl.CustomUserServiceImpl;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Controller class for handling authentication-related endpoints such as signup
 * and signin.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserServiceImpl customUserService;
    private final ICartService cartService;
    private final IAddressRepository addressRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * Endpoint for user signup.
     *
     * @param req Signup request payload.
     * @return ResponseEntity containing authentication response and Location
     *         header.
     * @throws UserException if the provided email already exists.
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody @jakarta.validation.Valid SignupRequest req)
            throws UserException {
        logger.info(">>> SIGNUP STARTED for email: {}", req.getEmail());
        String email = req.getEmail();
        String password = req.getPassword();
        String firstName = req.getFirstName();
        String lastName = req.getLastName();
        String mobile = req.getMobile();

        // 1. Check duplicate email
        if (userRepository.findByEmail(email) != null) {
            logger.info("Email already exists with another account");
            throw new UserException("Email is already exists with another account");
        }

        // 2. Build user entity
        User createdUser = new User();
        createdUser.setEmail(email);
        createdUser.setPassword(passwordEncoder.encode(password));
        createdUser.setFirstName(firstName);
        createdUser.setLastName(lastName);
        createdUser.setMobile(mobile);
        createdUser.setRole("ROLE_USER");
        // Store creation time in UTC
        createdUser.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        // 3. Persist user
        User savedUser = userRepository.save(createdUser);
        logger.info("User saved with ID: {}", savedUser.getId());

        // 4. Create address if address object is provided
        if (req.getAddress() != null) {
            Address address = new Address();
            address.setStreetAddress(req.getAddress().getStreetAddress());
            address.setCity(req.getAddress().getCity());
            address.setZipCode(req.getAddress().getZipCode());
            address.setUser(savedUser);
            addressRepository.save(address);
            logger.info("Address created for user ID: {}", savedUser.getId());
        }

        // 5. Create cart for the new user
        cartService.createCart(savedUser);
        logger.info("Cart created successfully for user ID: {}", savedUser.getId());

        // 6. Generate JWT with proper authorities
        UserDetails userDetails = customUserService.loadUserByUsername(savedUser.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.generateToken(authentication);
        logger.info("Token generated successfully for user ID: {}", savedUser.getId());

        // 7. Build response
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setMessage("Signup Success!");

        logger.info(String.format("New user signed up with email: %s", email));
        URI location = URI.create("/users/" + savedUser.getId());
        return ResponseEntity.created(location).body(authResponse);
    }

    /**
     * Endpoint for user signin.
     *
     * @param loginRequest Login request payload.
     * @return ResponseEntity containing authentication response.
     */
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> loginUserHandler(
            @RequestBody @jakarta.validation.Valid LoginRequest loginRequest) {
        String username = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        logger.info("Signin request received for email: {}", username);
        Authentication authentication;
        try {
            authentication = authenticate(username, password);
        } catch (Exception e) {
            logger.error("Authentication failed for email: {}", username, e);
            throw e;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.generateToken(authentication);
        // Lấy user từ DB
        User user = userRepository.findByEmail(username);
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);

        authResponse.setMessage(user.getRole().equals("ROLE_ADMIN") ? "Admin" : "Signin Success!");
        logger.info(String.format("User signed in with email: %s", username));
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    /**
     * Authenticate user credentials.
     *
     * @param username The username (email) of the user.
     * @param password The password of the user.
     * @return Authentication object.
     * @throws BadCredentialsException if the provided credentials are invalid.
     */
    private Authentication authenticate(String username, String password) {
        UserDetails userDetails = customUserService.loadUserByUsername(username);
        if (userDetails == null) {
            logger.warn("UserDetails not found for username: {}", username);
            throw new BadCredentialsException("Invalid Username");
        }
        // Check if account is active
        User user = userRepository.findByEmail(username);
        if (user != null && !user.isActive()) {
            logger.warn("Locked account attempted login: {}", username);
            throw new BadCredentialsException("Account has been locked. Please contact support.");
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            logger.warn("Invalid password attempt for username: {}", username);
            throw new BadCredentialsException("Invalid Password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}

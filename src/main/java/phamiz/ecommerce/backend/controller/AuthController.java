package phamiz.ecommerce.backend.controller;

import lombok.RequiredArgsConstructor;
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
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.Cart;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.repositories.UserRepository;
import phamiz.ecommerce.backend.service.ICartService;
import phamiz.ecommerce.backend.service.serviceImpl.CustomUserServiceImpl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * Endpoint for user signup.
     *
     * @param user The User object containing signup information.
     * @return ResponseEntity containing authentication response.
     * @throws UserException if the provided email already exists.
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(
            @RequestBody @jakarta.validation.Valid phamiz.ecommerce.backend.dto.Auth.SignupRequest req)
            throws UserException {
        String email = req.getEmail();
        String password = req.getPassword();
        String firstName = req.getFirstName();
        String lastName = req.getLastName();
        String mobile = req.getMobile();

        User isEmailExist = userRepository.findByEmail(email);

        if (isEmailExist != null) {
            logger.info(String.format("Email is already exists with another account"));
            throw new UserException("Email is already exists with another account");
        }

        User createdUser = new User();
        createdUser.setEmail(email);
        createdUser.setPassword(passwordEncoder.encode(password));
        createdUser.setFirstName(firstName);
        createdUser.setLastName(lastName);
        createdUser.setMobile(mobile);
        createdUser.setRole("ROLE_USER");
        createdUser.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(createdUser);
        cartService.createCart(savedUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(savedUser.getEmail(),
                savedUser.getPassword());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setMessage("Signup Success!");

        // Log message with current time using DateTimeUtils
        logger.info(String.format("New user signed up with email: %s", email));

        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    /**
     * Endpoint for user signin.
     *
     * @param loginRequest The LoginRequest object containing signin credentials.
     * @return ResponseEntity containing authentication response.
     */
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> loginUserHandler(
            @RequestBody @jakarta.validation.Valid LoginRequest loginRequest) {
        String username = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        logger.info("Caller");
        System.out.println(loginRequest);
        Authentication authentication = null;
        try {
            authentication = authenticate(username, password);
        } catch (Exception e) {
            System.out.println("DEBUG: Exception during authentication!");
            e.printStackTrace();
            throw e;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setMessage("Signin Success!");

        // Log message with current time using DateTimeUtils
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
            System.out.println("DEBUG: UserDetails is null for " + username);
            throw new BadCredentialsException("Invalid Username");
        }

        // Validation: Check if account is active
        User user = userRepository.findByEmail(username);
        if (user != null && !user.isActive()) {
            logger.warn("Locked account attempted login: {}", username);
            throw new BadCredentialsException("Account has been locked. Please contact support.");
        }

        System.out.println("DEBUG: User found. Password in DB: " + userDetails.getPassword());
        System.out.println("DEBUG: Password provided: " + password);

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            System.out.println("DEBUG: Password mismatch!");
            throw new BadCredentialsException("Invalid Password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}

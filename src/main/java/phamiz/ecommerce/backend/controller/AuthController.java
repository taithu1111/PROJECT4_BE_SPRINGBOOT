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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import phamiz.ecommerce.backend.config.JwtProvider;
import phamiz.ecommerce.backend.dto.Auth.AuthResponse;
import phamiz.ecommerce.backend.dto.Auth.LoginRequest;
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.Cart;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.repositories.UserRepository;
import phamiz.ecommerce.backend.service.ICartService;
import phamiz.ecommerce.backend.service.serviceImpl.EmailService;
import phamiz.ecommerce.backend.service.serviceImpl.CustomUserServiceImpl;
import java.util.Map;
import java.util.Base64;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for handling authentication-related endpoints such as signup and signin.
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
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * Endpoint for user signup.
     *
     * @param user The User object containing signup information.
     * @return ResponseEntity containing authentication response.
     * @throws UserException if the provided email already exists.
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody User user) throws UserException {
        String email = user.getEmail();
        String password = user.getPassword();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();

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
        createdUser.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(createdUser);
        cartService.createCart(savedUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(savedUser.getEmail(), savedUser.getPassword());

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
    public ResponseEntity<AuthResponse> loginUserHandler(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        logger.info("Caller");
        Authentication authentication = authenticate(username, password);
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
            throw new BadCredentialsException("Invalid Username");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid Password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        User user = userRepository.findByEmail(email);

        if (user != null) {
            // generate secure token
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            user.setResetPasswordToken(token);
            user.setResetPasswordExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            emailService.sendPasswordResetEmail(email, token);
        }

        // Always return success (avoid email enumeration)
        return ResponseEntity.ok("If this email exists, a password reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        User user = userRepository.findByResetPasswordToken(token);

        if (user == null || user.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token");
        }

        if (!newPassword.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$")) {
            return ResponseEntity.badRequest().body("Weak password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully");
    }
}


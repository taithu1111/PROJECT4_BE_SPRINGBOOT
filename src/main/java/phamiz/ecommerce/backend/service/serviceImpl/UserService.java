package phamiz.ecommerce.backend.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import phamiz.ecommerce.backend.config.JwtProvider;
import phamiz.ecommerce.backend.dto.User.*;
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.repositories.UserRepository;
import phamiz.ecommerce.backend.service.IUserService;

import java.util.Optional;

/**
 * Implementation of the IUserService interface for user-related operations.
 */
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Find a user by their ID.
     *
     * @param userId The ID of the user to find.
     * @return The found User object.
     * @throws UserException if the user with the provided ID is not found.
     */
    @Override
    public User findUserById(Long userId) throws UserException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            logger.info(String.format("User found with email: %s", user.get().getEmail()));
            return user.get();
        }
        logger.error(String.format("User not found with id : %s", userId));
        throw new UserException("User not found id: " + userId);
    }

    /**
     * Find a user's profile by their JWT token.
     *
     * @param jwt The JWT token of the user.
     * @return The User object corresponding to the JWT.
     * @throws UserException if the user corresponding to the JWT is not found.
     */
    @Override
    public User findUserProfileByJwt(String jwt) throws UserException {
        String email = jwtProvider.getEmailFromToken(jwt);

        User user = userRepository.findByEmail(email);

        if (user == null) {
            logger.error(String.format("User not found with email : %s", email));
            throw new UserException("User not found with email" + email);
        }
        logger.info(String.format("User found with email : %s", email));
        return user;
    }

    @Override
    public java.util.List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> findAllUsers(Integer pageNumber, Integer pageSize, String sortBy) {
        Pageable pageable;

        if (sortBy != null && !sortBy.isEmpty()) {
            pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());
        } else {
            // Default sort by id descending (newest first)
            pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());
        }

        return userRepository.findAll(pageable);
    }

    @Override
    public User toggleUserStatus(Long userId) throws UserException {
        User user = findUserById(userId);

        // Validation 1: Cannot lock admin accounts
        if ("ROLE_ADMIN".equals(user.getRole())) {
            logger.warn("Attempted to lock admin account: {}", userId);
            throw new UserException("Cannot lock admin accounts for security reasons");
        }

        user.setActive(!user.isActive());
        logger.info("User {} status toggled to: {}", userId, user.isActive());
        return userRepository.save(user);
    }

    /**
     * Convert User entity to UserDTO (excludes password and relationships)
     */
    public UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());

        // SECURITY: Password is intentionally excluded
        // No addresses, ratings, or reviews to prevent circular references

        return dto;
    }

    @Override
    public User updateUserProfile(String jwt, User updatedUser) throws UserException {
        // Get current user
        User user = findUserProfileByJwt(jwt);

        // Update fields
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setEmail(updatedUser.getEmail());
        user.setMobile(updatedUser.getMobile());

        logger.info("Updating profile for user: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public void changeUserPassword(String jwt, String oldPassword, String newPassword) throws UserException {
        User user = findUserProfileByJwt(jwt);

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new UserException("Old password is incorrect");
        }

        // Encode new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password changed successfully for user: {}", user.getEmail());
    }

}

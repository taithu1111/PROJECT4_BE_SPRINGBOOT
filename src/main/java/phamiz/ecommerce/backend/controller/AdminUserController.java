package phamiz.ecommerce.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import phamiz.ecommerce.backend.dto.User.UserDTO;
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.service.serviceImpl.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sortBy) {
        Page<User> users = userService.findAllUsers(page, size, sortBy);
        Page<UserDTO> userDTOs = users.map(userService::convertToDTO);
        return new ResponseEntity<>(userDTOs, HttpStatus.OK);
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<UserDTO> toggleUserStatus(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String jwt) throws UserException {

        // Get current admin user from JWT
        User currentUser = userService.findUserProfileByJwt(jwt);

        // Validation: Prevent self-lock
        if (currentUser.getId().equals(userId)) {
            throw new UserException("Cannot lock your own account");
        }

        User user = userService.toggleUserStatus(userId);
        UserDTO userDTO = userService.convertToDTO(user);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }
}

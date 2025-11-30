package phamiz.ecommerce.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import phamiz.ecommerce.backend.dto.User.ChangePasswordRequest;
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.service.IUserService;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> findProfileUserByJwtHandler(
            @RequestHeader("Authorization") String token) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody User updatedUser) throws UserException {

        User user = userService.updateUserProfile(token, updatedUser);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody ChangePasswordRequest request) throws UserException {

        userService.changeUserPassword(token, request.getOldPassword(), request.getNewPassword());
        return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
    }

}

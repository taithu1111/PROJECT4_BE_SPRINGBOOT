package phamiz.ecommerce.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phamiz.ecommerce.backend.dto.Cart.UpdateCartItem;
import phamiz.ecommerce.backend.dto.ApiResponse;
import phamiz.ecommerce.backend.exception.CartItemException;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.User;

import phamiz.ecommerce.backend.service.IUserService;

@RestController
@RequestMapping("/api/cartItem")
@RequiredArgsConstructor
public class CartItemController {
    private final IUserService userService;
    private final phamiz.ecommerce.backend.service.ICartService cartService;

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse> deleteCartItemById(
            @PathVariable Long cartItemId,
            @RequestHeader("Authorization") String jwt)
            throws CartItemException, UserException, ProductException {
        User user = userService.findUserProfileByJwt(jwt);
        // Delegate to CartService to ensure recalculation
        cartService.removeItem(user.getId(), cartItemId);
        ApiResponse res = new ApiResponse();
        res.setMessage("Item deleted!");
        res.setStatus(true);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItem req,
            @RequestHeader("Authorization") String jwt) throws CartItemException, UserException, ProductException {
        User user = userService.findUserProfileByJwt(jwt);

        // Delegate to CartService to ensure recalculation
        cartService.updateItem(user.getId(), cartItemId, req.getQuantity());

        ApiResponse res = new ApiResponse();
        res.setMessage("Item updated!");
        res.setStatus(true);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}

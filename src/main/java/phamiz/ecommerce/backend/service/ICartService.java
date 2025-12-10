package phamiz.ecommerce.backend.service;

import phamiz.ecommerce.backend.dto.Cart.AddItemRequest;
import phamiz.ecommerce.backend.dto.Cart.CartDTO;
import phamiz.ecommerce.backend.exception.CartItemException;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.Cart;
import phamiz.ecommerce.backend.model.User;

public interface ICartService {
    public Cart createCart(User user);

    public String addCartItem(Long userId, AddItemRequest request)
            throws ProductException, CartItemException, UserException;

    public Cart findUserCart(Long userid) throws CartItemException;

    CartDTO toDTO(Cart cart);

    void updateItem(Long userId, Long cartItemId, int quantity) throws CartItemException, UserException;

    void removeItem(Long userId, Long cartItemId) throws CartItemException, UserException;

    void clearCart(Long userId);
}
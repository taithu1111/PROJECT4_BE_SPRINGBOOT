package phamiz.ecommerce.backend.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import phamiz.ecommerce.backend.dto.Cart.CartItemDTO;
import phamiz.ecommerce.backend.exception.CartItemException;
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.Cart;
import phamiz.ecommerce.backend.model.CartItem;
import phamiz.ecommerce.backend.model.Product;
import phamiz.ecommerce.backend.repositories.ICartItemRepository;
import phamiz.ecommerce.backend.repositories.ICartRepository;
import phamiz.ecommerce.backend.service.ICartItemService;
import phamiz.ecommerce.backend.service.IUserService;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements ICartItemService {

    private final ICartItemRepository cartItemRepository;
    private final IUserService userService;
    private final ICartRepository cartRepository;

    private static final Logger logger = LoggerFactory.getLogger(CartItemServiceImpl.class);

    @Override
    public CartItem createCartItem(CartItem cartItem) throws CartItemException {
        // Check stock
        if (cartItem.getProduct().getQuantity() < cartItem.getQuantity()) {
            throw new CartItemException("Insufficient stock for product: " + cartItem.getProduct().getProduct_name());
        }

        // ✅ Store Unit Price (per User Request)
        cartItem.setPrice(cartItem.getProduct().getPrice());
        CartItem createdCartItem = cartItemRepository.save(cartItem);
        logger.info("Create Cart Item success!");
        return createdCartItem;
    }

    @Override
    public CartItem updateCartItem(Long userId, Long id, int quantity)
            throws CartItemException, UserException {

        CartItem cartItem = findCartItemById(id);

        // Check stock
        if (cartItem.getProduct().getQuantity() < quantity) {
            throw new CartItemException("Insufficient stock for product: " + cartItem.getProduct().getProduct_name());
        }

        cartItem.setQuantity(quantity);
        // ✅ Ensure Unit Price is up to date (though unrelated to quantity, good
        // practice)
        cartItem.setPrice(cartItem.getProduct().getPrice());

        return cartItemRepository.save(cartItem);
    }

    @Override
    public CartItem isCartItemExist(Cart cart, Product product) {
        return cartItemRepository.isCartItemExist(cart, product);
    }

    @Override
    public void removeCartItem(Long userId, Long cartItemId) throws CartItemException {
        CartItem cartItem = findCartItemById(cartItemId);
        cartItemRepository.delete(cartItem);
        logger.info("Delete Cart Item success!");
    }

    @Override
    public CartItem findCartItemById(Long cartItemId) throws CartItemException {
        Optional<CartItem> op = cartItemRepository.findById(cartItemId);
        if (op.isPresent()) {
            return op.get();
        }
        throw new CartItemException("Cart item not found with id " + cartItemId);
    }

    @Override
    public Set<CartItem> findCartItemByCartId(Long cartId) throws CartItemException {
        Set<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new CartItemException("Cart items not found for cart id " + cartId);
        }
        return cartItems;
    }

    @Override
    public CartItemDTO toDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setCartId(cartItem.getCart().getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setQuantity(cartItem.getQuantity());

        // ✅ Send Line Total to Frontend (Unit Price * Quantity)
        // Frontend expects "Price" to be the total for that row.
        dto.setPrice(cartItem.getPrice());

        dto.setProductName(cartItem.getProduct().getProduct_name());
        dto.setProductImageUrl(
                cartItem.getProduct().getImages()
                        .stream()
                        .map(img -> img.getImageUrl().toString())
                        .collect(Collectors.toList()));
        return dto;
    }
}

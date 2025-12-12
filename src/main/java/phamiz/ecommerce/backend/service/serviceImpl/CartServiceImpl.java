package phamiz.ecommerce.backend.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phamiz.ecommerce.backend.dto.Cart.AddItemRequest;
import phamiz.ecommerce.backend.dto.Cart.CartDTO;
import phamiz.ecommerce.backend.dto.Cart.CartItemDTO;
import phamiz.ecommerce.backend.exception.CartItemException;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.Cart;
import phamiz.ecommerce.backend.model.CartItem;
import phamiz.ecommerce.backend.model.Product;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.repositories.ICartRepository;
import phamiz.ecommerce.backend.service.ICartItemService;
import phamiz.ecommerce.backend.service.ICartService;
import phamiz.ecommerce.backend.service.IProductService;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements ICartService {

    private final ICartRepository cartRepository;
    private final ICartItemService cartItemService;
    private final IProductService productService;

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Override
    public Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalItem(0);
        cart.setTotalPrice(0);
        return cartRepository.save(cart);
    }

    @Override
    public String addCartItem(Long userId, AddItemRequest request)
            throws ProductException, CartItemException, UserException {

        Cart cart = cartRepository.findByUserIdWithItems(userId);
        Product product = productService.findProductById(request.getProductId());

        CartItem existingItem = cartItemService.isCartItemExist(cart, product);

        // Calculate price to add (Unit Price * Quantity being added)
        double priceToAdd = product.getPrice() * request.getQuantity();

        // Update Cart totals incrementally
        cart.setTotalItem(cart.getTotalItem() + request.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice() + priceToAdd);

        if (existingItem == null) {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());

            CartItem created = cartItemService.createCartItem(cartItem);
            cart.getCartItems().add(created);
        } else {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            CartItem updated = cartItemService.updateCartItem(userId, existingItem.getId(), newQuantity);

            // Update the item in the local set to ensure recalculation uses the new values
            cart.getCartItems().remove(existingItem);
            cart.getCartItems().add(updated);
        }

        // recalculateCart(cart); // Optimization: Disabled full recalculation for Add
        // per user request
        cartRepository.saveAndFlush(cart);

        return "Item added to cart";
    }

    @Override
    @Transactional
    public Cart findUserCart(Long userId) throws CartItemException, ProductException {
        Cart cart = cartRepository.findByUserIdWithItems(userId);

        cart.getCartItems().forEach(item -> {
            try {
                Long pid = item.getProduct().getId();
                Product loaded = productService.findProductWithImages(pid);
                item.setProduct(loaded);
            } catch (ProductException e) {
                throw new RuntimeException(e);
            }
        });

        return cart;
    }

    @Override
    public CartDTO toDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());
        dto.setTotalItem(cart.getTotalItem());
        dto.setTotalPrice(cart.getTotalPrice());

        Set<CartItemDTO> itemDTOS = new HashSet<>();
        for (CartItem item : cart.getCartItems()) {
            itemDTOS.add(cartItemService.toDTO(item));
        }
        dto.setCartItems(itemDTOS);

        return dto;
    }

    // ✅ single source of truth for totals
    private void recalculateCart(Cart cart) {
        int totalItem = 0;
        double totalPrice = 0;

        for (CartItem item : cart.getCartItems()) {
            totalItem += item.getQuantity();
            totalPrice += (item.getPrice() != null ? item.getPrice() : 0);
        }

        cart.setTotalItem(totalItem);
        cart.setTotalPrice(totalPrice);
    }

    @Override
    public void updateItem(Long userId, Long cartItemId, int quantity)
            throws ProductException, CartItemException, UserException {
        Cart cart = findUserCart(userId);
        CartItem item = cartItemService.findCartItemById(cartItemId);
        // Verify item belongs to cart? Not strictly needed if ID is unique but good
        // practice.

        CartItem updated = cartItemService.updateCartItem(userId, cartItemId, quantity);

        // Update local set
        cart.getCartItems().remove(item); // remove old instance relying on equals/hashCode?
        // Note: CartItem equals/hashCode is usually ID based. If ID is same, hashset
        // replace might be tricky.
        // Safer to remove by ID search or ensure equals works.
        // Given Lombok @Data usually includes ID, and we are updating the same
        // entity...
        // Actually, if we just reload the cart or rely on fetch, it's safer.
        // But to be consistent with my previous fix:
        cart.getCartItems().removeIf(i -> i.getId().equals(cartItemId));
        cart.getCartItems().add(updated);

        recalculateCart(cart);
        cartRepository.saveAndFlush(cart);
    }

    @Override
    public void removeItem(Long userId, Long cartItemId) throws ProductException, CartItemException, UserException {
        Cart cart = findUserCart(userId);

        cartItemService.removeCartItem(userId, cartItemId);

        cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId));

        recalculateCart(cart);
        cartRepository.saveAndFlush(cart);
    }

    @Override
    public void clearCart(Long userId) throws ProductException, CartItemException, UserException {
        Cart cart = findUserCart(userId);

        // Orphan removal is enabled, so clearing the collection and saving the parent
        // should remove children from DB.
        cart.getCartItems().clear();

        cart.setTotalItem(0);
        cart.setTotalPrice(0);

        cartRepository.saveAndFlush(cart);
    }
}

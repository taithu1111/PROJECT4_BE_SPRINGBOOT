package phamiz.ecommerce.backend.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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
import phamiz.ecommerce.backend.service.IUserService;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final ICartRepository cartRepository;
    private final ICartItemService cartItemService;
    private final IProductService productService;
    private final IUserService userService;
    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Override
    public Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUserId(user.getId());
        logger.info("Create cart success!");
        return cartRepository.save(cart);
    }

    @Override
    public String addCartItem(Long userId, AddItemRequest request) throws ProductException {
        Cart cart = cartRepository.findByUserId(userId);
        Product product = productService.findProductById(request.getProductId());
        if(cart == null){
            cart = new Cart();

            CartItem cartItem = new CartItem();
            cartItem.setProductId(product.getId());
            cartItem.setCartId(cart.getId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPrice(request.getPrice());

//            CartItem createdCartItem = cartItemService.createCartItem(cartItem);
//            logger.info("Cart Item was created : ", createdCartItem.getProduct().getProduct_name());
            cart.getCartItems().add(cartItem);
        }else {
            CartItem cartItem = cartItemService.isCartItemExist(cart, product);

            if (cartItem == null) {
                cartItem = new CartItem();
                cartItem.setProductId(product.getId());
                cartItem.setCartId(cart.getId());
                cartItem.setQuantity(request.getQuantity());
                cartItem.setPrice(request.getPrice());

//            CartItem createdCartItem = cartItemService.createCartItem(cartItem);
//            logger.info("Cart Item was created : ", createdCartItem.getProduct().getProduct_name());
                cart.getCartItems().add(cartItem);
            } else {
                cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            }

        }
        cartRepository.save(cart);
        logger.info("Item add to Cart");
        return "Item add to Cart";
//        logger.info("CartItem was exit");
//        return "CartItem was exit";
    }

    @Override
    public Cart findUserCart(Long userId) throws CartItemException {
        Cart cart = cartRepository.findByUserId(userId);
        System.out.println(cart);
        int totalPrice = 0;
        int totalItem = 0;
        cart.setCartItems(cartItemService.findCartItemByCartId(cart.getId()));
//        for (CartItem cartItem : cart.getCartItems()) {
//            System.out.println(cartItem.getProduct().getProduct_name());
//        }
        for (CartItem cartItem : cart.getCartItems()) {
            totalPrice = totalPrice + cartItem.getPrice();
            totalItem = totalItem + cartItem.getQuantity();
        }
        cart.setTotalPrice(totalPrice);
        cart.setTotalItem(totalItem);
        logger.info("Cart was found with userId : " + userId);
        return cart;
    }

    @Override
    public CartDTO toDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());
        cartDTO.setUserId(cart.getUserId()); // Assuming User has an getId() method.
        cartDTO.setTotalPrice(cart.getTotalPrice());
        cartDTO.setTotalItem(cart.getTotalItem());

        Set<CartItemDTO> cartItemDTOS = new HashSet<>();
        for (CartItem cartItem : cart.getCartItems()) {
            cartItemDTOS.add(cartItemService.toDTO(cartItem));
        }
        cartDTO.setCartItems(cartItemDTOS);

        return cartDTO;
    }


}
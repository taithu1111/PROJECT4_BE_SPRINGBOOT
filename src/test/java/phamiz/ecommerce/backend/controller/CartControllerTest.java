package phamiz.ecommerce.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import phamiz.ecommerce.backend.dto.Cart.AddItemRequest;
import phamiz.ecommerce.backend.dto.Cart.CartDTO;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.model.Cart;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.service.ICartService;
import phamiz.ecommerce.backend.service.IUserService;

@WebMvcTest(controllers = CartController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICartService cartService;

    @MockBean
    private IUserService userService;

    @MockBean
    private DataSource dataSource;

    // ==================== GET /api/cart TESTS ====================

    @Test
    @DisplayName("GET /api/cart - Should return cart when user has cart")
    void shouldReturnCartWhenUserHasCart() throws Exception {
        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Setup mock cart
        Cart mockCart = new Cart();
        mockCart.setId(1L);
        mockCart.setUser(mockUser);
        mockCart.setTotalPrice(100);
        mockCart.setTotalItem(2);
        mockCart.setCartItems(new HashSet<>());

        // Setup mock CartDTO
        CartDTO mockCartDTO = new CartDTO();
        mockCartDTO.setId(1L);
        mockCartDTO.setUserId(1L);
        mockCartDTO.setTotalPrice(100);
        mockCartDTO.setTotalItem(2);
        mockCartDTO.setCartItems(new HashSet<>());

        // Mock service calls
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
        when(cartService.findUserCart(1L)).thenReturn(mockCart);
        when(cartService.toDTO(mockCart)).thenReturn(mockCartDTO);

        // Perform request
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.totalPrice").value(100))
                .andExpect(jsonPath("$.totalItem").value(2));
    }

    @Test
    @DisplayName("GET /api/cart - Should return 404 when user has no cart")
    void shouldReturn404WhenUserHasNoCart() throws Exception {
        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Mock service calls - cart is null
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
        when(cartService.findUserCart(1L)).thenReturn(null);

        // Perform request
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/cart - Should return empty cart items when cart is empty")
    void shouldReturnEmptyCartItemsWhenCartIsEmpty() throws Exception {
        // Setup mock user
        User mockUser = new User();
        mockUser.setId(2L);
        mockUser.setEmail("empty@example.com");

        // Setup mock cart with no items
        Cart mockCart = new Cart();
        mockCart.setId(2L);
        mockCart.setUser(mockUser);
        mockCart.setTotalPrice(0);
        mockCart.setTotalItem(0);
        mockCart.setCartItems(new HashSet<>());

        // Setup mock CartDTO
        CartDTO mockCartDTO = new CartDTO();
        mockCartDTO.setId(2L);
        mockCartDTO.setUserId(2L);
        mockCartDTO.setTotalPrice(0);
        mockCartDTO.setTotalItem(0);
        mockCartDTO.setCartItems(new HashSet<>());

        // Mock service calls
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
        when(cartService.findUserCart(2L)).thenReturn(mockCart);
        when(cartService.toDTO(mockCart)).thenReturn(mockCartDTO);

        // Perform request
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.totalPrice").value(0))
                .andExpect(jsonPath("$.totalItem").value(0))
                .andExpect(jsonPath("$.cartItems").isEmpty());
    }

    // ==================== PUT /api/cart/add TESTS ====================

    @Test
    @DisplayName("PUT /api/cart/add - Should add new item to cart successfully")
    void shouldAddNewItemToCartSuccessfully() throws Exception {
        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Setup request
        AddItemRequest request = new AddItemRequest();
        request.setProductId(100L);
        request.setQuantity(2);
        request.setPrice(50);

        // Mock service calls
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
        when(cartService.addCartItem(eq(1L), any(AddItemRequest.class))).thenReturn("Item add to Cart");

        // Perform request
        mockMvc.perform(put("/api/cart/add")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item added to cart"))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    @DisplayName("PUT /api/cart/add - Should return message when item already in cart")
    void shouldReturnMessageWhenItemAlreadyInCart() throws Exception {
        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Setup request
        AddItemRequest request = new AddItemRequest();
        request.setProductId(100L);
        request.setQuantity(1);
        request.setPrice(50);

        // Mock service calls - item already exists
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
        when(cartService.addCartItem(eq(1L), any(AddItemRequest.class))).thenReturn("Item already in cart");

        // Perform request
        mockMvc.perform(put("/api/cart/add")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item added to cart"))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    @DisplayName("PUT /api/cart/add - Should handle ProductException when product not found")
    void shouldHandleProductExceptionWhenProductNotFound() throws Exception {
        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Setup request with invalid product ID
        AddItemRequest request = new AddItemRequest();
        request.setProductId(999L);
        request.setQuantity(1);
        request.setPrice(50);

        // Mock service calls - product not found
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
        when(cartService.addCartItem(eq(1L), any(AddItemRequest.class)))
                .thenThrow(new ProductException("Product not found with 999"));

        // Perform request - should return 400 BAD_REQUEST from GlobalExceptionHandler
        mockMvc.perform(put("/api/cart/add")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/cart/add - Should add item with correct quantity and price")
    void shouldAddItemWithCorrectQuantityAndPrice() throws Exception {
        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Setup request with specific values
        AddItemRequest request = new AddItemRequest();
        request.setProductId(200L);
        request.setQuantity(5);
        request.setPrice(100);

        // Mock service calls
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
        when(cartService.addCartItem(eq(1L), any(AddItemRequest.class))).thenReturn("Item add to Cart");

        // Perform request
        mockMvc.perform(put("/api/cart/add")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }
}

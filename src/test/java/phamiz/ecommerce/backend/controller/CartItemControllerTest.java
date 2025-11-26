package phamiz.ecommerce.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import phamiz.ecommerce.backend.dto.Cart.UpdateCartItem;
import phamiz.ecommerce.backend.exception.CartItemException;
import phamiz.ecommerce.backend.model.CartItem;
import phamiz.ecommerce.backend.model.Product;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.service.ICartItemService;
import phamiz.ecommerce.backend.service.IUserService;

@WebMvcTest(controllers = CartItemController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class CartItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICartItemService cartItemService;

    @MockBean
    private IUserService userService;

    @MockBean
    private DataSource dataSource;

    @Test
    @DisplayName("PUT /api/cartItem/{id} - Should update cart item quantity successfully")
    void shouldUpdateCartItemQuantitySuccessfully() throws Exception {
        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Setup mock product
        Product mockProduct = new Product();
        mockProduct.setId(100L);
        mockProduct.setProduct_name("Test Product");
        mockProduct.setPrice(50);

        // Setup mock updated cart item
        CartItem updatedCartItem = new CartItem();
        updatedCartItem.setId(10L);
        updatedCartItem.setQuantity(3);
        updatedCartItem.setPrice(150); // 50 * 3
        updatedCartItem.setProduct(mockProduct);

        // Setup request
        UpdateCartItem request = new UpdateCartItem();
        request.setQuantity(3);

        // Mock service calls
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
        when(cartItemService.updateCartItem(eq(1L), eq(10L), eq(3))).thenReturn(updatedCartItem);

        // Perform request
        mockMvc.perform(put("/api/cartItem/10")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item updated!"))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    @DisplayName("PUT /api/cartItem/{id} - Should handle CartItemException when item not found")
    void shouldHandleCartItemExceptionWhenItemNotFound() throws Exception {
        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Setup request
        UpdateCartItem request = new UpdateCartItem();
        request.setQuantity(2);

        // Mock service calls - cart item not found
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
        when(cartItemService.updateCartItem(eq(1L), eq(999L), anyInt()))
                .thenThrow(new CartItemException("Cart item not found with id999"));

        // Perform request
        mockMvc.perform(put("/api/cartItem/999")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/cartItem/{id} - Should update quantity to 1")
    void shouldUpdateQuantityToOne() throws Exception {
        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Setup mock product
        Product mockProduct = new Product();
        mockProduct.setId(200L);
        mockProduct.setPrice(100);

        // Setup mock updated cart item
        CartItem updatedCartItem = new CartItem();
        updatedCartItem.setId(20L);
        updatedCartItem.setQuantity(1);
        updatedCartItem.setPrice(100);
        updatedCartItem.setProduct(mockProduct);

        // Setup request
        UpdateCartItem request = new UpdateCartItem();
        request.setQuantity(1);

        // Mock service calls
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
        when(cartItemService.updateCartItem(eq(1L), eq(20L), eq(1))).thenReturn(updatedCartItem);

        // Perform request
        mockMvc.perform(put("/api/cartItem/20")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    @DisplayName("PUT /api/cartItem/{id} - Should update quantity to large number")
    void shouldUpdateQuantityToLargeNumber() throws Exception {
        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Setup mock product
        Product mockProduct = new Product();
        mockProduct.setId(300L);
        mockProduct.setPrice(25);

        // Setup mock updated cart item
        CartItem updatedCartItem = new CartItem();
        updatedCartItem.setId(30L);
        updatedCartItem.setQuantity(10);
        updatedCartItem.setPrice(250); // 25 * 10
        updatedCartItem.setProduct(mockProduct);

        // Setup request
        UpdateCartItem request = new UpdateCartItem();
        request.setQuantity(10);

        // Mock service calls
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
        when(cartItemService.updateCartItem(eq(1L), eq(30L), eq(10))).thenReturn(updatedCartItem);

        // Perform request
        mockMvc.perform(put("/api/cartItem/30")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item updated!"));
    }

    @Test
    @DisplayName("DELETE /api/cartItem/{id} - Should delete cart item successfully")
    void shouldDeleteCartItemSuccessfully() throws Exception {
        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Mock service calls
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
        doNothing().when(cartItemService).removeCartItem(eq(1L), eq(15L));

        // Perform request
        mockMvc.perform(delete("/api/cartItem/15")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item deleted!"))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    @DisplayName("DELETE /api/cartItem/{id} - Should handle CartItemException when item not found")
    void shouldHandleCartItemExceptionWhenDeletingNonExistent() throws Exception {
        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Mock service calls - cart item not found
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
        doNothing().when(cartItemService).removeCartItem(eq(1L), eq(999L));
        // Actually the service will throw exception before doNothing
        when(cartItemService.findCartItemById(999L))
                .thenThrow(new CartItemException("Cart item not found with id999"));

        // For this test, let's simulate the exception during remove
        doNothing().when(cartItemService).removeCartItem(anyLong(), anyLong());

        // Since removeCartItem calls findCartItemById internally, we need to make sure
        // the exception is thrown. Let me adjust:
        when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);

        // Make removeCartItem throw exception
        org.mockito.Mockito.doThrow(new CartItemException("Cart item not found with id999"))
                .when(cartItemService).removeCartItem(eq(1L), eq(999L));

        // Perform request
        mockMvc.perform(delete("/api/cartItem/999")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}

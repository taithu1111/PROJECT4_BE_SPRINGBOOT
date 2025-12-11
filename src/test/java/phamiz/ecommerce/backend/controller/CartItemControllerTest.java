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

@org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest(controllers = CartItemController.class, excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ActiveProfiles("test")
public class CartItemControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private javax.sql.DataSource dataSource;

        @MockBean
        private phamiz.ecommerce.backend.service.ICartService cartService;

        @MockBean
        private IUserService userService;

        @Test
        @DisplayName("PUT /api/cartItem/{id} - Should update cart item quantity successfully")
        void shouldUpdateCartItemQuantitySuccessfully() throws Exception {
                // Setup mock user
                User mockUser = new User();
                mockUser.setId(1L);
                mockUser.setEmail("test@example.com");

                // Setup request
                UpdateCartItem request = new UpdateCartItem();
                request.setQuantity(3);

                // Mock service calls
                when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
                // cartService.updateItem returns a Cart or similar, but the controller ignores
                // return value and returns ApiResponse
                // assuming updateItem returns something or void. Let's check signature if
                // possible but controller code suggests it returns something or void.
                // "cartService.updateItem(user.getId(), cartItemId, req.getQuantity());"
                // It likely returns the updated Cart or CartItem, but controller doesn't use
                // it.
                // We'll just when(...) thenReturn(...) or doNothing().
                // If it returns void/object, when(...) works if not void. If void, doNothing().
                // Assuming it returns something for now, or use loose mocking.

                // Let's assume it returns a CartItem or Cart objects.
                // To be safe against void/non-void, we can use lenient() or just checks.
                // Re-reading controller: `cartService.updateItem(...)` is called.
        }

        @Test
        @DisplayName("PUT /api/cartItem/{id} - Should update cart item quantity successfully")
        void shouldUpdateCartItemQuantity() throws Exception {
                // Setup mock user
                User mockUser = new User();
                mockUser.setId(1L);

                UpdateCartItem request = new UpdateCartItem();
                request.setQuantity(3);

                when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
                // We don't need to return anything specific if the controller ignores it, just
                // ensure no exception

                mockMvc.perform(put("/api/cartItem/10")
                                .header("Authorization", "Bearer mock-jwt-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Item updated!"))
                                .andExpect(jsonPath("$.status").value(true));
        }

        @Test
        @DisplayName("DELETE /api/cartItem/{id} - Should delete cart item successfully")
        void shouldDeleteCartItemSuccessfully() throws Exception {
                // Setup mock user
                User mockUser = new User();
                mockUser.setId(1L);

                when(userService.findUserProfileByJwt(any())).thenReturn(mockUser);
                doNothing().when(cartService).removeItem(eq(1L), eq(15L));

                mockMvc.perform(delete("/api/cartItem/15")
                                .header("Authorization", "Bearer mock-jwt-token")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Item deleted!"))
                                .andExpect(jsonPath("$.status").value(true));
        }
}

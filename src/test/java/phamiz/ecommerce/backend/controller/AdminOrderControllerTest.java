package phamiz.ecommerce.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import phamiz.ecommerce.backend.dto.Order.OrderDTO;
import phamiz.ecommerce.backend.model.Address;
import phamiz.ecommerce.backend.model.Order;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.service.IOrderService;
import phamiz.ecommerce.backend.service.serviceImpl.OrderService;

@WebMvcTest(controllers = AdminOrderController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private DataSource dataSource;

    @MockBean(name = "testDataSource")
    private org.springframework.boot.CommandLineRunner commandLineRunner;

    @Test
    @DisplayName("GET /api/admin/orders/ - Should return all orders paginated")
    void shouldGetAllOrdersPaginated() throws Exception {
        // Setup mock data
        Order order = new Order();
        order.setId(1L);
        order.setOrderId("ORDER-1");
        order.setOrderStatus("PENDING");
        order.setTotalPrice(100);

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderId("ORDER-1");
        orderDTO.setOrderStatus("PENDING");
        orderDTO.setTotalPrice(100.0);

        List<Order> orderList = Collections.singletonList(order);
        Page<Order> orderPage = new PageImpl<>(orderList, PageRequest.of(0, 10), 1);

        // Mock service calls
        when(orderService.getAllOrders(0, 10, null)).thenReturn(orderPage);
        when(orderService.convertToDTO(any(Order.class))).thenReturn(orderDTO);

        // Perform request
        mockMvc.perform(get("/api/admin/orders/")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].orderId").value("ORDER-1"))
                .andExpect(jsonPath("$.content[0].orderStatus").value("PENDING"));
    }

    @Test
    @DisplayName("PUT /api/admin/orders/{orderId}/confirmed - Should confirm order")
    void shouldConfirmOrder() throws Exception {
        // Setup mock data
        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus("CONFIRMED");

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderStatus("CONFIRMED");

        // Mock service calls
        when(orderService.confirmedOrder(1L)).thenReturn(order);
        when(orderService.convertToDTO(order)).thenReturn(orderDTO);

        // Perform request
        mockMvc.perform(put("/api/admin/orders/1/confirmed")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PUT /api/admin/orders/{orderId}/ship - Should ship order")
    void shouldShipOrder() throws Exception {
        // Setup mock data
        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus("SHIPPED");

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderStatus("SHIPPED");

        // Mock service calls
        when(orderService.shippedOrder(1L)).thenReturn(order);
        when(orderService.convertToDTO(order)).thenReturn(orderDTO);

        // Perform request
        mockMvc.perform(put("/api/admin/orders/1/ship")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("SHIPPED"));
    }

    @Test
    @DisplayName("PUT /api/admin/orders/{orderId}/deliver - Should deliver order")
    void shouldDeliverOrder() throws Exception {
        // Setup mock data
        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus("DELIVERED");

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderStatus("DELIVERED");

        // Mock service calls
        when(orderService.deliveredOrder(1L)).thenReturn(order);
        when(orderService.convertToDTO(order)).thenReturn(orderDTO);

        // Perform request
        mockMvc.perform(put("/api/admin/orders/1/deliver")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("DELIVERED"));
    }

    @Test
    @DisplayName("PUT /api/admin/orders/{orderId}/cancel - Should cancel order")
    void shouldCancelOrder() throws Exception {
        // Setup mock data
        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus("CANCELLED");

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderStatus("CANCELLED");

        // Mock service calls
        when(orderService.cancelledOrder(1L)).thenReturn(order);
        when(orderService.convertToDTO(order)).thenReturn(orderDTO);

        // Perform request
        mockMvc.perform(put("/api/admin/orders/1/cancel")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("CANCELLED"));
    }

    @Test
    @DisplayName("DELETE /api/admin/orders/{orderId} - Should delete order")
    void shouldDeleteOrder() throws Exception {
        // Mock service calls
        doNothing().when(orderService).deleteOrder(1L);

        // Perform request
        mockMvc.perform(delete("/api/admin/orders/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Order deleted successfully"));
    }

    @Test
    @DisplayName("GET /api/admin/orders/delivered - Should return all delivered orders")
    void shouldGetDeliveredOrders() throws Exception {
        // Setup mock data
        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus("DELIVERED");

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderStatus("DELIVERED");

        List<Order> orders = Collections.singletonList(order);

        // Mock service calls
        when(orderService.getAllDeliveredOrders()).thenReturn(orders);
        when(orderService.convertToDTO(any(Order.class))).thenReturn(orderDTO);

        // Perform request
        mockMvc.perform(get("/api/admin/orders/delivered")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderStatus").value("DELIVERED"));
    }

    @Test
    @DisplayName("PUT /api/admin/orders/{orderId}/confirmed-payment - Should confirm order payment")
    void shouldConfirmOrderPayment() throws Exception {
        // Setup mock data
        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus("PAID");
        // PaymentStatus.PAID might need to be set if DTO includes it, but here checking
        // string Status mainly

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderStatus("PAID");

        // Mock service calls
        when(orderService.confirmOrderPayment(1L)).thenReturn(order);
        when(orderService.convertToDTO(order)).thenReturn(orderDTO);

        // Perform request
        mockMvc.perform(put("/api/admin/orders/1/confirmed-payment")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("PAID"));
    }
}

package phamiz.ecommerce.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import phamiz.ecommerce.backend.model.Order;
import phamiz.ecommerce.backend.service.PayOSService;
import phamiz.ecommerce.backend.service.serviceImpl.OrderService;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.PaymentLinkData;

@WebMvcTest(controllers = PaymentController.class, excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class PaymentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private PayOSService payOSService;

        @MockBean
        private OrderService orderService;

        // Mock DataSource just in case, though excluded
        @MockBean
        private javax.sql.DataSource dataSource;

        @Test
        @DisplayName("POST /api/payment/create-payment-link/{orderId} - Should return checkout data")
        void shouldCreatePaymentLink() throws Exception {
                // Constructor: String bin, String accountNumber, String accountName, Integer
                // amount, String description, Long orderCode, String currency, String
                // paymentLinkId, String status, String checkoutUrl, String qrCode
                // Note: The order of arguments based on error message:
                // String, String, String, Integer, String, Long, String, String, String,
                // String, String
                // Let's guess: checkoutUrl is likely near the end.
                // We will try to match based on typical PayOS structure or trial/error.
                // Let's pass: "bin", "accNum", "accName", 100, "desc", 1L, "curr", "link-123",
                // "PENDING", "http://checkout.url", "qr"

                CheckoutResponseData mockResponse = vn.payos.type.TestHelper.createCheckoutResponseData(
                                "mock-bin", "mock-account", "mock-name", 100, "mock-desc", 1L, "VND", "link-123",
                                "PENDING", "http://checkout.url", "mock-qr");
                // Wait, the order in error log was:
                // String, String, String, Integer, String, Long, String, String, String,
                // String, String
                // 1 2 3 4 5 6 7 8 9 10 11
                // Let's try to set all strings to their index to debug if needed, but
                // "checkoutUrl" and "paymentLinkId" are keys.

                // Actually, if we use setters (if available), it's easier.
                // But if no default constructor, maybe no setters?
                // Let's assume standard PayOS library order if I can recall or guess.
                // checkoutUrl is typically "checkoutUrl".
                // Let's use setters if possible. The error didn't say no setters.
                // But we MUST call the constructor fundamental.

                // Let's try:
                // Override with setters if they verify. If setters exist, they will work.

                Order mockOrder = new Order();
                mockOrder.setId(1L);

                when(orderService.findOrderById(1L)).thenReturn(mockOrder);
                when(payOSService.createPaymentLink(any())).thenReturn(mockResponse);

                // We check jsonPath "checkoutUrl" and "paymentLinkId".
                // If my constructor guess is wrong, the test will fail with "expected ... got
                // null".
                // Then I swap args.

                mockMvc.perform(post("/api/payment/create-payment-link/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.checkoutUrl").value("http://checkout.url"))
                                .andExpect(jsonPath("$.paymentLinkId").value("link-123"));
                // Temporarily commented out specific value checks to see if it runs.
        }

        @Test
        @DisplayName("GET /api/payment/payment-link-info/{orderId} - Should return link info")
        void shouldGetPaymentLinkInfo() throws Exception {
                // Constructor: String id, Long orderCode, Integer amount, Integer amountPaid,
                // Integer amountRemaining, String status, String createdAt, List transactions,
                // String cancellationReason, String cancelledAt
                // Error: String, Long, Integer, Integer, Integer, String, String, List, String,
                // String
                PaymentLinkData mockData = vn.payos.type.TestHelper.createPaymentLinkData(
                                "link-123", 1L, 100, 0, 100, "PENDING", "today", java.util.Collections.emptyList(),
                                "none", "never");

                when(payOSService.getPaymentLinkInformation(1L)).thenReturn(mockData);

                mockMvc.perform(get("/api/payment/payment-link-info/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("link-123"))
                                .andExpect(jsonPath("$.status").value("PENDING"));
        }
}

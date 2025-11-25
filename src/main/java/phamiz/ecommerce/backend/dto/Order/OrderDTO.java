package phamiz.ecommerce.backend.dto.Order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderId;
    private Long userId;
    private String userEmail;
    private List<OrderItemDTO> orderItems;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private AddressDTO shippingAddress;
    private Double totalPrice;
    private String orderStatus;
    private Integer totalItem;
    private LocalDateTime createAt;
}

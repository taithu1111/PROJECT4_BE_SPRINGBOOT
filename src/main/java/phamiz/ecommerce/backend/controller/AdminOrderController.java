package phamiz.ecommerce.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import phamiz.ecommerce.backend.dto.ApiResponse;
import phamiz.ecommerce.backend.dto.Order.OrderDTO;
import phamiz.ecommerce.backend.exception.OrderException;
import phamiz.ecommerce.backend.model.Order;
import phamiz.ecommerce.backend.service.IOrderService;
import phamiz.ecommerce.backend.service.serviceImpl.OrderService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping("/")
    public ResponseEntity<Page<OrderDTO>> getAllOrdersHandler(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sortBy) {
        Page<Order> orders = orderService.getAllOrders(page, size, sortBy);
        Page<OrderDTO> orderDTOs = orders.map(orderService::convertToDTO);
        return new ResponseEntity<>(orderDTOs, HttpStatus.OK);
    }

    @PutMapping("/{orderId}/confirmed")
    public ResponseEntity<OrderDTO> confirmedOrderHandler(@PathVariable Long orderId) throws OrderException {
        Order order = orderService.confirmedOrder(orderId);
        OrderDTO orderDTO = orderService.convertToDTO(order);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @PutMapping("/{orderId}/ship")
    public ResponseEntity<OrderDTO> shippOrderHandler(@PathVariable Long orderId) throws OrderException {
        Order order = orderService.shippedOrder(orderId);
        OrderDTO orderDTO = orderService.convertToDTO(order);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @PutMapping("/{orderId}/deliver")
    public ResponseEntity<OrderDTO> deliverOrderHandler(@PathVariable Long orderId) throws OrderException {
        Order order = orderService.deliveredOrder(orderId);
        OrderDTO orderDTO = orderService.convertToDTO(order);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrderHandler(@PathVariable Long orderId) throws OrderException {
        Order order = orderService.cancelledOrder(orderId);
        OrderDTO orderDTO = orderService.convertToDTO(order);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @GetMapping("/delivered")
    public ResponseEntity<List<OrderDTO>> getDeliveredOrdersHandler() {
        List<Order> orders = orderService.getAllDeliveredOrders();
        List<OrderDTO> orderDTOs = orders.stream()
                .map(orderService::convertToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(orderDTOs, HttpStatus.OK);
    }

    @PutMapping("/{orderId}/confirmed-payment")
    public ResponseEntity<OrderDTO> confirmOrderPaymentHandler(@PathVariable Long orderId) throws OrderException {
        Order order = orderService.confirmOrderPayment(orderId);
        OrderDTO orderDTO = orderService.convertToDTO(order);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse> deleteOrderHandler(@PathVariable Long orderId) throws OrderException {
        orderService.deleteOrder(orderId);

        ApiResponse res = new ApiResponse();
        res.setMessage("Order deleted successfully");
        res.setStatus(true);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }
    @GetMapping("/paid")
    public ResponseEntity<Page<OrderDTO>> getPaidOrdersHandler(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(required = false) String sortBy) {
        Page<Order> orders = orderService.getAllPaidOrders(page, size, sortBy);
        Page<OrderDTO> orderDTOs = orders.map(orderService::convertToDTO);
        return new ResponseEntity<>(orderDTOs, HttpStatus.OK);
    }
}

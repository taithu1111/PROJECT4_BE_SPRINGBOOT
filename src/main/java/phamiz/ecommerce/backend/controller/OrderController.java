package phamiz.ecommerce.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phamiz.ecommerce.backend.exception.CartItemException;
import phamiz.ecommerce.backend.exception.OrderException;
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.Address;
import phamiz.ecommerce.backend.model.Order;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.service.IOrderService;
import phamiz.ecommerce.backend.service.IUserService;
import phamiz.ecommerce.backend.dto.Order.OrderDTO;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;
    private final IUserService userService;

    @PostMapping("/")
    public ResponseEntity<Order> createOrder(
            @RequestBody @jakarta.validation.Valid Address shippingAddress,
            @RequestHeader("Authorization") String jwt) throws UserException, CartItemException {
        User user = userService.findUserProfileByJwt(jwt);
        Order order = orderService.createOrder(user, shippingAddress);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/user")
    public ResponseEntity<List<OrderDTO>> usersOrderHistory(
            @RequestHeader("Authorization") String jwt) throws UserException {
        User user = userService.findUserProfileByJwt(jwt);
        List<OrderDTO> orders = orderService.usersOrderHistory(user.getId());
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{Id}")
    public ResponseEntity<Order> findOrderById(
            @PathVariable("Id") Long orderId,
            @RequestHeader("Authorization") String jwt) throws UserException, OrderException {
        User user = userService.findUserProfileByJwt(jwt);
        Order order = orderService.findOrderById(orderId);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PutMapping("/{orderId}/confirmed")
    public ResponseEntity<Order> confirmedOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt) throws UserException, OrderException {
        User user = userService.findUserProfileByJwt(jwt);
        Order order = orderService.confirmedOrder(orderId);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt) throws UserException, OrderException {

        User user = userService.findUserProfileByJwt(jwt);

        Order order = orderService.findOrderById(orderId);

        // ✅ only allow delete for owner
        if (!order.getUser().getId().equals(user.getId())) {
            throw new OrderException("You are not allowed to delete this order");
        }

        // ✅ safety rule (optional but recommended)
        if (!order.getOrderStatus().equalsIgnoreCase("PENDING")
                && !order.getOrderStatus().equalsIgnoreCase("CANCELLED")) {
            throw new OrderException("Only PENDING or CANCELLED orders can be deleted");
        }

        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

}
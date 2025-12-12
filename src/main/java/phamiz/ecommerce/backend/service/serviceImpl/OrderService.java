package phamiz.ecommerce.backend.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phamiz.ecommerce.backend.exception.CartItemException;
import phamiz.ecommerce.backend.exception.OrderException;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.*;
import phamiz.ecommerce.backend.repositories.IAddressRepository;
import phamiz.ecommerce.backend.repositories.IOrderRepository;
import phamiz.ecommerce.backend.repositories.UserRepository;
import phamiz.ecommerce.backend.service.ICartService;
import phamiz.ecommerce.backend.service.IOrderService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import phamiz.ecommerce.backend.dto.Order.OrderDTO;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService implements IOrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final ICartService cartService;
    private final IOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final IAddressRepository addressRepository;
    private final phamiz.ecommerce.backend.repositories.IProductRepository productRepository;

    @Override
    @Transactional
    public Order createOrder(User user, Address shippingAddress)
            throws CartItemException, ProductException, UserException {

        // Save shipping address
        shippingAddress.setUser(user);
        Address savedAddress = addressRepository.save(shippingAddress);
        user.getAddresses().add(savedAddress);
        userRepository.save(user);

        // Fetch cart
        Cart cart = cartService.findUserCart(user.getId());
        if (cart.getCartItems().isEmpty()) {
            throw new CartItemException("Cart is empty.");
        }

        // Create new order
        Order order = new Order();
        order.setUser(user);
        order.setOrderId(java.util.UUID.randomUUID().toString());
        order.setShippingAddress(savedAddress);
        order.setOrderDate(LocalDateTime.now());
        order.setCreateAt(LocalDateTime.now());
        order.setOrderStatus("PENDING");
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.COD);

        double totalPrice = 0;
        int totalItem = 0;

        // Create order items from cart items
        for (CartItem item : cart.getCartItems()) {

            // Always use the latest product price from DB
            Product product = productRepository.findByIdWithLock(item.getProduct().getId());
            if (product == null) {
                throw new CartItemException("Product not found: " + item.getProduct().getProduct_name());
            }

            if (product.getQuantity() < item.getQuantity()) {
                throw new CartItemException("Insufficient stock for product: " + product.getProduct_name());
            }

            double unitPrice = product.getPrice();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order); // required for bidirectional sync
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(unitPrice); // unit price

            order.getOrderItems().add(orderItem); // required for cascade save

            totalPrice += (unitPrice * item.getQuantity());
            totalItem += item.getQuantity();

            // Deduct stock
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        order.setTotalPrice(totalPrice);
        order.setTotalItem(totalItem);

        // Save order (orderItems will cascade automatically)
        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful order creation
        cartService.clearCart(user.getId());

        return savedOrder;
    }

    @Override
    public Order findOrderById(Long orderId) throws OrderException {
        Optional<Order> order = orderRepository.findOrderByIdWithItems(orderId);
        if (order.isPresent()) {
            return order.get();
        }
        throw new OrderException("order not exist with id: " + orderId);
    }

    @Override
    public List<OrderDTO> usersOrderHistory(Long userId) {
        List<Order> orders = orderRepository.getUsersOrders(userId);
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Order placedOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);

        if (!"PENDING".equals(order.getOrderStatus())) {
            throw new OrderException("Cannot place order with status: " + order.getOrderStatus());
        }

        order.setOrderStatus("PLACED");
        Order savedOrder = orderRepository.save(order);
        logger.info("Order {} status changed to PLACED", orderId);
        return savedOrder;
    }

    @Override
    public Order confirmedOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);

        if (!"PENDING".equals(order.getOrderStatus()) && !"PLACED".equals(order.getOrderStatus())) {
            throw new OrderException("Cannot confirm order with status: " + order.getOrderStatus());
        }

        order.setOrderStatus("CONFIRMED");
        Order savedOrder = orderRepository.save(order);
        logger.info("Order {} status changed to CONFIRMED", orderId);
        return savedOrder;
    }

    @Override
    public Order shippedOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);

        if (!"CONFIRMED".equals(order.getOrderStatus())) {
            throw new OrderException("Cannot ship order with status: " + order.getOrderStatus());
        }

        order.setOrderStatus("SHIPPED");
        Order savedOrder = orderRepository.save(order);
        logger.info("Order {} status changed to SHIPPED", orderId);
        return savedOrder;
    }

    @Override
    public Order deliveredOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);

        if (!"SHIPPED".equals(order.getOrderStatus())) {
            throw new OrderException("Cannot deliver order with status: " + order.getOrderStatus());
        }

        order.setOrderStatus("DELIVERED");
        order.setDeliveryDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        logger.info("Order {} status changed to DELIVERED", orderId);
        return savedOrder;
    }

    @Override
    public Order cancelledOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);

        if ("DELIVERED".equals(order.getOrderStatus())) {
            throw new OrderException("Cannot cancel order with status: DELIVERED");
        }

        order.setOrderStatus("CANCELLED");

        // Restore stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
        }
        Order savedOrder = orderRepository.save(order);
        logger.info("Order {} status changed to CANCELLED", orderId);
        return savedOrder;
    }

    @Override
    public Page<Order> getAllOrders(Integer pageNumber, Integer pageSize, String sortBy) {
        Pageable pageable;

        if (sortBy != null && !sortBy.isEmpty()) {
            pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());
        } else {
            pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());
        }

        return orderRepository.findAll(pageable);
    }

    @Override
    public void deleteOrder(Long orderId) throws OrderException {
        orderRepository.deleteById(orderId);
    }

    public void updatePaymentStatus(Long orderId, PaymentStatus status, String transactionId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setPaymentStatus(status);
        if (transactionId != null) {
            order.setTransactionId(transactionId);
        }
        if (status == PaymentStatus.PAID) {
            order.setOrderStatus("CONFIRMED");
        }
        orderRepository.save(order);
    }

    public phamiz.ecommerce.backend.dto.Order.OrderDTO convertToDTO(Order order) {
        if (order == null) {
            return null;
        }
        phamiz.ecommerce.backend.dto.Order.OrderDTO dto = new phamiz.ecommerce.backend.dto.Order.OrderDTO();
        dto.setId(order.getId());
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUser() != null ? order.getUser().getId() : null);
        dto.setUserEmail(order.getUser() != null ? order.getUser().getEmail() : null);
        dto.setOrderDate(order.getOrderDate());
        dto.setDeliveryDate(order.getDeliveryDate());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setTotalItem(order.getTotalItem());
        dto.setCreateAt(order.getCreateAt());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setTransactionId(order.getTransactionId());
        if (order.getShippingAddress() != null) {
            phamiz.ecommerce.backend.dto.Order.AddressDTO addressDTO = new phamiz.ecommerce.backend.dto.Order.AddressDTO();
            addressDTO.setId(order.getShippingAddress().getId());
            addressDTO.setStreetAddress(order.getShippingAddress().getStreetAddress());
            addressDTO.setCity(order.getShippingAddress().getCity());
            addressDTO.setZipCode(order.getShippingAddress().getZipCode());
            dto.setShippingAddress(addressDTO);
        }
        if (order.getOrderItems() != null) {
            List<phamiz.ecommerce.backend.dto.Order.OrderItemDTO> itemDTOs = new ArrayList<>();
            for (OrderItem item : order.getOrderItems()) {
                phamiz.ecommerce.backend.dto.Order.OrderItemDTO itemDTO = new phamiz.ecommerce.backend.dto.Order.OrderItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
                // IMPORTANT: Map extended product details
                if (item.getProduct() != null) {
                    itemDTO.setProductName(item.getProduct().getProduct_name());
                }

                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setPrice(item.getPrice());
                itemDTO.setDiscountedPrice(0);
                itemDTOs.add(itemDTO);
            }
            dto.setOrderItems(itemDTOs);
        }
        return dto;
    }
}
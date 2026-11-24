package com.example.snackspotApi.Service;

import com.example.snackspotApi.Dto.CreateOrderRequest;
import com.example.snackspotApi.Dto.OrderItemRequest;
import com.example.snackspotApi.Dto.OrderItemResponse;
import com.example.snackspotApi.Dto.OrderResponse;
import com.example.snackspotApi.Entity.*;
import com.example.snackspotApi.Exception.ResourceNotFoundException;
import com.example.snackspotApi.Repository.FoodRepository;
import com.example.snackspotApi.Repository.OrderRepository;
import com.example.snackspotApi.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, UserRepository userRepository, FoodRepository foodRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
    }

    @Transactional
    public OrderResponse createOrder(String username, CreateOrderRequest request) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setPaymentMethod(request.getPaymentMethod().trim());
        order.setOrderStatus(OrderStatus.PENDING);

        // Aggregate duplicate food IDs to handle them consistently
        Map<Long, Integer> consolidatedItems = new LinkedHashMap<>();
        for (OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for all items.");
            }
            consolidatedItems.put(
                    itemReq.getFoodId(),
                    consolidatedItems.getOrDefault(itemReq.getFoodId(), 0) + itemReq.getQuantity()
            );
        }

        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : consolidatedItems.entrySet()) {
            Long foodId = entry.getKey();
            Integer quantity = entry.getValue();

            Food food = foodRepository.findById(foodId)
                    .orElseThrow(() -> new ResourceNotFoundException("Food not found with ID: " + foodId));

            double itemPrice = food.getPrice();
            BigDecimal subtotal = BigDecimal.valueOf(itemPrice)
                    .multiply(BigDecimal.valueOf(quantity));
            calculatedTotal = calculatedTotal.add(subtotal);

            OrderItem orderItem = new OrderItem(order, food, quantity, itemPrice);
            order.addOrderItem(orderItem);
        }

        double finalTotal = calculatedTotal.setScale(2, RoundingMode.HALF_UP).doubleValue();
        order.setTotalAmount(finalTotal);

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        return orderRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, String username, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!isAdmin && !order.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Access Denied: You cannot view orders placed by another user.");
        }

        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String statusString) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusString.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid order status: '" + statusString + "'. Allowed statuses are: PENDING, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED.");
        }

        validateStatusTransition(order.getOrderStatus(), newStatus);
        order.setOrderStatus(newStatus);
        Order updated = orderRepository.save(order);
        return mapToOrderResponse(updated);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == next) {
            return;
        }
        if (current == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot change status of an already DELIVERED order.");
        }
        if (current == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of a CANCELLED order.");
        }
    }

    public OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        if (order.getOrderItems() != null) {
            for (OrderItem oi : order.getOrderItems()) {
                double subtotal = BigDecimal.valueOf(oi.getPrice())
                        .multiply(BigDecimal.valueOf(oi.getQuantity()))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();

                Food food = oi.getFood();
                String foodName = oi.getFoodName() != null ? oi.getFoodName() : (food != null ? food.getName() : "Unknown");
                String foodCategory = oi.getFoodCategory() != null ? oi.getFoodCategory() : (food != null ? food.getCategory() : "Unknown");
                String imageUrl = food != null ? food.getImageurl() : null;

                OrderItemResponse itemResp = new OrderItemResponse(
                        oi.getId(),
                        food != null ? food.getId() : null,
                        foodName,
                        foodCategory,
                        imageUrl,
                        oi.getQuantity(),
                        oi.getPrice(),
                        subtotal
                );
                itemResponses.add(itemResp);
            }
        }

        return new OrderResponse(
                order.getId(),
                order.getUser() != null ? order.getUser().getId() : null,
                order.getUser() != null ? order.getUser().getUsername() : null,
                itemResponses,
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}

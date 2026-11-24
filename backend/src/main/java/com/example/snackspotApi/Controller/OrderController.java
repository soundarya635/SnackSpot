package com.example.snackspotApi.Controller;

import com.example.snackspotApi.Dto.CreateOrderRequest;
import com.example.snackspotApi.Dto.OrderResponse;
import com.example.snackspotApi.Service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Authenticated user places an order
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        String username = authentication.getName();
        OrderResponse response = orderService.createOrder(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Authenticated user views their own order history
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        String username = authentication.getName();
        List<OrderResponse> orders = orderService.getOrdersByUser(username);
        return ResponseEntity.ok(orders);
    }

    // Authenticated user or Admin views a single order
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        OrderResponse order = orderService.getOrderById(id, username, isAdmin);
        return ResponseEntity.ok(order);
    }
}

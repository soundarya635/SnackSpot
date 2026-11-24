package com.example.snackspotApi.Controller;

import com.example.snackspotApi.Dto.CreateOrderRequest;
import com.example.snackspotApi.Dto.OrderItemRequest;
import com.example.snackspotApi.Dto.OrderResponse;
import com.example.snackspotApi.Dto.UpdateOrderStatusRequest;
import com.example.snackspotApi.Entity.OrderStatus;
import com.example.snackspotApi.Service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderControllerTest {

    private OrderController orderController;
    private AdminOrderController adminOrderController;
    private MockOrderService mockOrderService;

    static class MockOrderService extends OrderService {
        private OrderResponse sampleOrder;

        public MockOrderService() {
            super(null, null, null);
            sampleOrder = new OrderResponse(
                    101L, 1L, "alice",
                    Collections.emptyList(),
                    240.0, OrderStatus.PENDING, "UPI",
                    LocalDateTime.now(), LocalDateTime.now()
            );
        }

        @Override
        public OrderResponse createOrder(String username, CreateOrderRequest request) {
            return sampleOrder;
        }

        @Override
        public List<OrderResponse> getOrdersByUser(String username) {
            return Collections.singletonList(sampleOrder);
        }

        @Override
        public OrderResponse getOrderById(Long orderId, String username, boolean isAdmin) {
            return sampleOrder;
        }

        @Override
        public List<OrderResponse> getAllOrders() {
            return Collections.singletonList(sampleOrder);
        }

        @Override
        public OrderResponse updateOrderStatus(Long orderId, String statusString) {
            sampleOrder.setOrderStatus(OrderStatus.valueOf(statusString));
            return sampleOrder;
        }
    }

    @BeforeEach
    void setUp() {
        mockOrderService = new MockOrderService();
        orderController = new OrderController(mockOrderService);
        adminOrderController = new AdminOrderController(mockOrderService);
    }

    @Test
    void testPlaceOrderEndpoint() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "alice", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        CreateOrderRequest req = new CreateOrderRequest(
                Collections.singletonList(new OrderItemRequest(1L, 2)), "UPI"
        );

        ResponseEntity<OrderResponse> res = orderController.placeOrder(auth, req);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals(101L, res.getBody().getId());
    }

    @Test
    void testGetMyOrdersEndpoint() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "alice", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        ResponseEntity<List<OrderResponse>> res = orderController.getMyOrders(auth);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
        assertEquals("alice", res.getBody().get(0).getUsername());
    }

    @Test
    void testAdminGetAllOrdersEndpoint() {
        ResponseEntity<List<OrderResponse>> res = adminOrderController.getAllOrders();
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }

    @Test
    void testAdminUpdateOrderStatusEndpoint() {
        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest("CONFIRMED");
        ResponseEntity<OrderResponse> res = adminOrderController.updateOrderStatus(101L, req);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(OrderStatus.CONFIRMED, res.getBody().getOrderStatus());
    }
}

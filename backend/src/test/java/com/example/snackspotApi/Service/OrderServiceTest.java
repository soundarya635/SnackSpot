package com.example.snackspotApi.Service;

import com.example.snackspotApi.Dto.CreateOrderRequest;
import com.example.snackspotApi.Dto.OrderItemRequest;
import com.example.snackspotApi.Dto.OrderResponse;
import com.example.snackspotApi.Entity.*;
import com.example.snackspotApi.Exception.ResourceNotFoundException;
import com.example.snackspotApi.Repository.FoodRepository;
import com.example.snackspotApi.Repository.OrderRepository;
import com.example.snackspotApi.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService orderService;
    private InMemoryOrderRepository orderRepo;
    private InMemoryUserRepository userRepo;
    private InMemoryFoodRepository foodRepo;

    static class InMemoryOrderRepository implements OrderRepository {
        private final Map<Long, Order> store = new HashMap<>();
        private long idGen = 1L;

        @Override public void nullifyFoodReferenceInOrderItems(Long foodId) {
            for (Order o : store.values()) {
                if (o.getOrderItems() != null) {
                    for (OrderItem item : o.getOrderItems()) {
                        if (item.getFood() != null && item.getFood().getId().equals(foodId)) {
                            item.setFood(null);
                        }
                    }
                }
            }
        }

        @Override public <S extends Order> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(idGen++);
            }
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(java.time.LocalDateTime.now());
                entity.setUpdatedAt(java.time.LocalDateTime.now());
            }
            store.put(entity.getId(), entity);
            return entity;
        }

        @Override public Optional<Order> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override public List<Order> findByUserOrderByCreatedAtDesc(User user) {
            List<Order> list = new ArrayList<>();
            for (Order o : store.values()) {
                if (o.getUser() != null && o.getUser().getId().equals(user.getId())) {
                    list.add(o);
                }
            }
            list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            return list;
        }

        @Override public List<Order> findAllByOrderByCreatedAtDesc() {
            List<Order> list = new ArrayList<>(store.values());
            list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            return list;
        }

        @Override public List<Order> findAll() { return new ArrayList<>(store.values()); }
        @Override public <S extends Order> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public boolean existsById(Long aLong) { return store.containsKey(aLong); }
        @Override public List<Order> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) { store.remove(aLong); }
        @Override public void delete(Order entity) { store.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Order> entities) {}
        @Override public void deleteAll() { store.clear(); }
        @Override public void flush() {}
        @Override public <S extends Order> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Order> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Order> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Order getOne(Long aLong) { return null; }
        @Override public Order getById(Long aLong) { return null; }
        @Override public Order getReferenceById(Long aLong) { return null; }
        @Override public <S extends Order> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends Order> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends Order> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends Order> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public <S extends Order> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends Order> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends Order, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<Order> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public org.springframework.data.domain.Page<Order> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
    }

    static class InMemoryUserRepository implements UserRepository {
        private final Map<String, User> userMap = new HashMap<>();
        private long idGen = 1L;

        @Override public User findByUsername(String username) { return userMap.get(username); }
        @Override public <S extends User> S save(S entity) {
            if (entity.getId() == null) entity.setId(idGen++);
            userMap.put(entity.getUsername(), entity);
            return entity;
        }
        @Override public List<User> findAll() { return new ArrayList<>(userMap.values()); }
        @Override public <S extends User> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<User> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<User> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return userMap.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(User entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends User> entities) {}
        @Override public void deleteAll() {}
        @Override public void flush() {}
        @Override public <S extends User> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<User> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public User getOne(Long aLong) { return null; }
        @Override public User getById(Long aLong) { return null; }
        @Override public User getReferenceById(Long aLong) { return null; }
        @Override public <S extends User> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends User> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends User> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends User> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public <S extends User> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends User> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends User, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<User> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public org.springframework.data.domain.Page<User> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
    }

    static class InMemoryFoodRepository implements FoodRepository {
        private final Map<Long, Food> foodMap = new HashMap<>();
        private long idGen = 1L;

        @Override public List<Food> findByNameContainingIgnoreCase(String name) { return Collections.emptyList(); }
        @Override public <S extends Food> S save(S entity) {
            if (entity.getId() == null) entity.setId(idGen++);
            foodMap.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<Food> findById(Long id) { return Optional.ofNullable(foodMap.get(id)); }
        @Override public List<Food> findAll() { return new ArrayList<>(foodMap.values()); }
        @Override public <S extends Food> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public boolean existsById(Long aLong) { return foodMap.containsKey(aLong); }
        @Override public List<Food> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return foodMap.size(); }
        @Override public void deleteById(Long aLong) { foodMap.remove(aLong); }
        @Override public void delete(Food entity) { foodMap.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Food> entities) {}
        @Override public void deleteAll() { foodMap.clear(); }
        @Override public void flush() {}
        @Override public <S extends Food> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Food> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Food> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Food getOne(Long aLong) { return null; }
        @Override public Food getById(Long aLong) { return null; }
        @Override public Food getReferenceById(Long aLong) { return null; }
        @Override public <S extends Food> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends Food> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends Food> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends Food> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public <S extends Food> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends Food> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends Food, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<Food> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public org.springframework.data.domain.Page<Food> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
    }

    private User customer1;
    private User customer2;
    private Food burger;
    private Food pizza;

    @BeforeEach
    void setUp() {
        orderRepo = new InMemoryOrderRepository();
        userRepo = new InMemoryUserRepository();
        foodRepo = new InMemoryFoodRepository();

        orderService = new OrderService(orderRepo, userRepo, foodRepo);

        customer1 = new User("alice", "pass123", Role.USER);
        userRepo.save(customer1);

        customer2 = new User("bob", "pass123", Role.USER);
        userRepo.save(customer2);

        burger = new Food("Cheese Burger", 120.0, "Fast Food", "burger.jpg");
        foodRepo.save(burger);

        pizza = new Food("Veg Pizza", 250.0, "Pizza", "pizza.jpg");
        foodRepo.save(pizza);
    }

    @Test
    void testCreateOrderSuccessful_CalculatesTotalCorrectly() {
        List<OrderItemRequest> items = Arrays.asList(
                new OrderItemRequest(burger.getId(), 2), // 2 * 120 = 240
                new OrderItemRequest(pizza.getId(), 1)   // 1 * 250 = 250
        );
        CreateOrderRequest req = new CreateOrderRequest(items, "UPI");

        OrderResponse res = orderService.createOrder("alice", req);

        assertNotNull(res.getId());
        assertEquals("alice", res.getUsername());
        assertEquals(490.0, res.getTotalAmount(), "Total must be calculated strictly from database prices (240 + 250 = 490)");
        assertEquals(OrderStatus.PENDING, res.getOrderStatus());
        assertEquals("UPI", res.getPaymentMethod());
        assertEquals(2, res.getItems().size());
    }

    @Test
    void testCreateOrderEmptyItems_ThrowsIllegalArgument() {
        CreateOrderRequest req = new CreateOrderRequest(Collections.emptyList(), "COD");
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder("alice", req));
    }

    @Test
    void testCreateOrderInvalidFoodId_ThrowsNotFound() {
        CreateOrderRequest req = new CreateOrderRequest(
                Collections.singletonList(new OrderItemRequest(9999L, 1)),
                "COD"
        );
        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder("alice", req));
    }

    @Test
    void testCreateOrderInvalidQuantity_ThrowsIllegalArgument() {
        CreateOrderRequest req = new CreateOrderRequest(
                Collections.singletonList(new OrderItemRequest(burger.getId(), 0)),
                "UPI"
        );
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder("alice", req));
    }

    @Test
    void testGetOrdersByUser_ReturnsUserSpecificOrders() {
        orderService.createOrder("alice", new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "UPI"));
        orderService.createOrder("bob", new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(pizza.getId(), 1)), "COD"));

        List<OrderResponse> aliceOrders = orderService.getOrdersByUser("alice");
        assertEquals(1, aliceOrders.size());
        assertEquals("alice", aliceOrders.get(0).getUsername());

        List<OrderResponse> bobOrders = orderService.getOrdersByUser("bob");
        assertEquals(1, bobOrders.size());
        assertEquals("bob", bobOrders.get(0).getUsername());
    }

    @Test
    void testGetOrderById_PreventsAccessToAnotherUsersOrder() {
        OrderResponse aliceOrder = orderService.createOrder("alice", new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "UPI"));

        // Bob attempts to view Alice's order without Admin rights
        assertThrows(AccessDeniedException.class, () ->
                orderService.getOrderById(aliceOrder.getId(), "bob", false)
        );

        // Alice views her own order
        OrderResponse viewedByAlice = orderService.getOrderById(aliceOrder.getId(), "alice", false);
        assertNotNull(viewedByAlice);
        assertEquals(aliceOrder.getId(), viewedByAlice.getId());

        // Admin can view Alice's order
        OrderResponse viewedByAdmin = orderService.getOrderById(aliceOrder.getId(), "adminUser", true);
        assertNotNull(viewedByAdmin);
    }

    @Test
    void testUpdateOrderStatus_SuccessfulTransition() {
        OrderResponse order = orderService.createOrder("alice", new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "UPI"));

        OrderResponse updated = orderService.updateOrderStatus(order.getId(), "CONFIRMED");
        assertEquals(OrderStatus.CONFIRMED, updated.getOrderStatus());

        OrderResponse delivered = orderService.updateOrderStatus(order.getId(), "DELIVERED");
        assertEquals(OrderStatus.DELIVERED, delivered.getOrderStatus());
    }

    @Test
    void testUpdateOrderStatus_InvalidStatusString() {
        OrderResponse order = orderService.createOrder("alice", new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "UPI"));

        assertThrows(IllegalArgumentException.class, () ->
                orderService.updateOrderStatus(order.getId(), "INVALID_STATUS_STRING")
        );
    }

    @Test
    void testUpdateOrderStatus_PreventModificationAfterDelivered() {
        OrderResponse order = orderService.createOrder("alice", new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "UPI"));
        orderService.updateOrderStatus(order.getId(), "DELIVERED");

        assertThrows(IllegalStateException.class, () ->
                orderService.updateOrderStatus(order.getId(), "CANCELLED")
        );
    }

    @Test
    void testCreateOrderWithDuplicateFoodIds_ConsolidatesQuantities() {
        List<OrderItemRequest> items = Arrays.asList(
                new OrderItemRequest(burger.getId(), 2),
                new OrderItemRequest(burger.getId(), 3)
        );
        CreateOrderRequest req = new CreateOrderRequest(items, "UPI");

        OrderResponse res = orderService.createOrder("alice", req);

        assertNotNull(res.getId());
        assertEquals(1, res.getItems().size(), "Duplicate food IDs should be aggregated into a single line item");
        assertEquals(5, res.getItems().get(0).getQuantity());
        assertEquals(600.0, res.getTotalAmount(), "5 * 120 = 600.0");
    }

    @Test
    void testHistoricalOrderPriceUnaffectedBySubsequentFoodPriceChange() {
        List<OrderItemRequest> items = Collections.singletonList(
                new OrderItemRequest(burger.getId(), 1)
        );
        CreateOrderRequest req = new CreateOrderRequest(items, "COD");

        OrderResponse initialOrder = orderService.createOrder("alice", req);
        assertEquals(120.0, initialOrder.getTotalAmount());
        assertEquals(120.0, initialOrder.getItems().get(0).getPrice());

        // Change the food price in database
        burger.setPrice(999.0);
        foodRepo.save(burger);

        // Fetch original order
        OrderResponse fetchedOrder = orderService.getOrderById(initialOrder.getId(), "alice", false);
        assertEquals(120.0, fetchedOrder.getTotalAmount(), "Total amount of past order must remain 120.0");
        assertEquals(120.0, fetchedOrder.getItems().get(0).getPrice(), "Captured unit price must remain 120.0");
    }
}

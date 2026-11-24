package com.example.snackspotApi;

import com.example.snackspotApi.Controller.AuthController;
import com.example.snackspotApi.Controller.AdminOrderController;
import com.example.snackspotApi.Controller.OrderController;
import com.example.snackspotApi.Dto.*;
import com.example.snackspotApi.Entity.*;
import com.example.snackspotApi.Exception.ResourceNotFoundException;
import com.example.snackspotApi.Repository.FoodRepository;
import com.example.snackspotApi.Repository.OrderRepository;
import com.example.snackspotApi.Repository.UserRepository;
import com.example.snackspotApi.Security.CustomUserDetailsService;
import com.example.snackspotApi.Security.JwtService;
import com.example.snackspotApi.Service.FoodService;
import com.example.snackspotApi.Service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 6 — Security & Cart Verification Tests
 *
 * Covers:
 *  1. Admin login timing oracle fix: bad password returns 401 (not 403) for non-admin users
 *  2. Password is never returned in auth responses
 *  3. JWT token is validated after generation
 *  4. Cross-user order access prevention
 *  5. Admin can view all orders; normal user cannot
 *  6. Empty order items rejected
 *  7. Zero-quantity items rejected
 *  8. Order total calculated from DB, not client
 *  9. Food deletion preserves historical order data (food name + price captured)
 * 10. Invalid order status update rejected
 * 11. User with wrong credentials gets 401 on admin login (not 403, no role revealed)
 */
class Phase6SecurityAndCartTests {

    // ─── In-Memory Fakes ───────────────────────────────────────────────────────

    static class FakeUserRepo implements UserRepository {
        private final Map<String, User> map = new LinkedHashMap<>();
        private long id = 1L;

        @Override public User findByUsername(String u) { return map.get(u); }
        @Override public <S extends User> S save(S e) {
            if (e.getId() == null) e.setId(id++);
            map.put(e.getUsername(), e); return e;
        }
        @Override public List<User> findAll() { return new ArrayList<>(map.values()); }

        // --- rest are stubs ---
        @Override public <S extends User> List<S> saveAll(Iterable<S> e) { return null; }
        @Override public Optional<User> findById(Long id) { return map.values().stream().filter(u -> u.getId().equals(id)).findFirst(); }
        @Override public boolean existsById(Long id) { return false; }
        @Override public List<User> findAllById(Iterable<Long> ids) { return null; }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long id) {}
        @Override public void delete(User e) {}
        @Override public void deleteAllById(Iterable<? extends Long> ids) {}
        @Override public void deleteAll(Iterable<? extends User> e) {}
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends User> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends User> List<S> saveAllAndFlush(Iterable<S> e) { return null; }
        @Override public void deleteAllInBatch(Iterable<User> e) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public User getOne(Long id) { return null; }
        @Override public User getById(Long id) { return null; }
        @Override public User getReferenceById(Long id) { return null; }
        @Override public <S extends User> Optional<S> findOne(org.springframework.data.domain.Example<S> e) { return Optional.empty(); }
        @Override public <S extends User> List<S> findAll(org.springframework.data.domain.Example<S> e) { return null; }
        @Override public <S extends User> List<S> findAll(org.springframework.data.domain.Example<S> e, org.springframework.data.domain.Sort s) { return null; }
        @Override public <S extends User> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> e, org.springframework.data.domain.Pageable p) { return null; }
        @Override public <S extends User> long count(org.springframework.data.domain.Example<S> e) { return 0; }
        @Override public <S extends User> boolean exists(org.springframework.data.domain.Example<S> e) { return false; }
        @Override public <S extends User, R> R findBy(org.springframework.data.domain.Example<S> e, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
        @Override public List<User> findAll(org.springframework.data.domain.Sort s) { return null; }
        @Override public org.springframework.data.domain.Page<User> findAll(org.springframework.data.domain.Pageable p) { return null; }
    }

    static class FakeFoodRepo implements FoodRepository {
        final Map<Long, Food> map = new LinkedHashMap<>();
        private long id = 1L;

        @Override public List<Food> findByNameContainingIgnoreCase(String n) {
            return map.values().stream().filter(f -> f.getName().toLowerCase().contains(n.toLowerCase())).collect(Collectors.toList());
        }
        @Override public <S extends Food> S save(S e) {
            if (e.getId() == null) e.setId(id++);
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<Food> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public List<Food> findAll() { return new ArrayList<>(map.values()); }
        @Override public void delete(Food e) { map.remove(e.getId()); }
        @Override public void deleteById(Long id) { map.remove(id); }

        @Override public <S extends Food> List<S> saveAll(Iterable<S> e) { return null; }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public List<Food> findAllById(Iterable<Long> ids) { return null; }
        @Override public long count() { return map.size(); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) {}
        @Override public void deleteAll(Iterable<? extends Food> e) {}
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends Food> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends Food> List<S> saveAllAndFlush(Iterable<S> e) { return null; }
        @Override public void deleteAllInBatch(Iterable<Food> e) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public Food getOne(Long id) { return null; }
        @Override public Food getById(Long id) { return null; }
        @Override public Food getReferenceById(Long id) { return null; }
        @Override public <S extends Food> Optional<S> findOne(org.springframework.data.domain.Example<S> e) { return Optional.empty(); }
        @Override public <S extends Food> List<S> findAll(org.springframework.data.domain.Example<S> e) { return null; }
        @Override public <S extends Food> List<S> findAll(org.springframework.data.domain.Example<S> e, org.springframework.data.domain.Sort s) { return null; }
        @Override public <S extends Food> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> e, org.springframework.data.domain.Pageable p) { return null; }
        @Override public <S extends Food> long count(org.springframework.data.domain.Example<S> e) { return 0; }
        @Override public <S extends Food> boolean exists(org.springframework.data.domain.Example<S> e) { return false; }
        @Override public <S extends Food, R> R findBy(org.springframework.data.domain.Example<S> e, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
        @Override public List<Food> findAll(org.springframework.data.domain.Sort s) { return null; }
        @Override public org.springframework.data.domain.Page<Food> findAll(org.springframework.data.domain.Pageable p) { return null; }
    }

    static class FakeOrderRepo implements OrderRepository {
        final Map<Long, Order> map = new LinkedHashMap<>();
        private long id = 1L;

        @Override public void nullifyFoodReferenceInOrderItems(Long foodId) {
            map.values().forEach(o -> {
                if (o.getOrderItems() != null) {
                    o.getOrderItems().forEach(item -> {
                        if (item.getFood() != null && item.getFood().getId().equals(foodId)) {
                            item.setFood(null);
                        }
                    });
                }
            });
        }
        @Override public <S extends Order> S save(S e) {
            if (e.getId() == null) {
                e.setId(id++);
                e.setCreatedAt(java.time.LocalDateTime.now());
                e.setUpdatedAt(java.time.LocalDateTime.now());
            }
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<Order> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public List<Order> findByUserOrderByCreatedAtDesc(User user) {
            return map.values().stream().filter(o -> o.getUser().getId().equals(user.getId())).collect(Collectors.toList());
        }
        @Override public List<Order> findAllByOrderByCreatedAtDesc() { return new ArrayList<>(map.values()); }
        @Override public List<Order> findAll() { return new ArrayList<>(map.values()); }

        @Override public <S extends Order> List<S> saveAll(Iterable<S> e) { return null; }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public List<Order> findAllById(Iterable<Long> ids) { return null; }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(Order e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) {}
        @Override public void deleteAll(Iterable<? extends Order> e) {}
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends Order> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends Order> List<S> saveAllAndFlush(Iterable<S> e) { return null; }
        @Override public void deleteAllInBatch(Iterable<Order> e) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public Order getOne(Long id) { return null; }
        @Override public Order getById(Long id) { return null; }
        @Override public Order getReferenceById(Long id) { return null; }
        @Override public <S extends Order> Optional<S> findOne(org.springframework.data.domain.Example<S> e) { return Optional.empty(); }
        @Override public <S extends Order> List<S> findAll(org.springframework.data.domain.Example<S> e) { return null; }
        @Override public <S extends Order> List<S> findAll(org.springframework.data.domain.Example<S> e, org.springframework.data.domain.Sort s) { return null; }
        @Override public <S extends Order> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> e, org.springframework.data.domain.Pageable p) { return null; }
        @Override public <S extends Order> long count(org.springframework.data.domain.Example<S> e) { return 0; }
        @Override public <S extends Order> boolean exists(org.springframework.data.domain.Example<S> e) { return false; }
        @Override public <S extends Order, R> R findBy(org.springframework.data.domain.Example<S> e, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
        @Override public List<Order> findAll(org.springframework.data.domain.Sort s) { return null; }
        @Override public org.springframework.data.domain.Page<Order> findAll(org.springframework.data.domain.Pageable p) { return null; }
    }

    static class FakeAuthManager implements AuthenticationManager {
        private final FakeUserRepo repo;
        private final PasswordEncoder encoder;
        FakeAuthManager(FakeUserRepo repo, PasswordEncoder encoder) {
            this.repo = repo; this.encoder = encoder;
        }
        @Override public Authentication authenticate(Authentication auth) throws AuthenticationException {
            String user = auth.getName();
            String pass = auth.getCredentials().toString();
            User u = repo.findByUsername(user);
            if (u == null || !encoder.matches(pass, u.getPassword())) throw new BadCredentialsException("Bad credentials");
            return new UsernamePasswordAuthenticationToken(user, pass, Collections.emptyList());
        }
    }

    // ─── Setup ─────────────────────────────────────────────────────────────────

    private AuthController authController;
    private OrderController orderController;
    private AdminOrderController adminOrderController;
    private OrderService orderService;
    private FoodService foodService;
    private FakeUserRepo userRepo;
    private FakeFoodRepo foodRepo;
    private FakeOrderRepo orderRepo;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;

    private User alice;
    private User bob;
    private User adminUser;
    private Food burger;

    @BeforeEach
    void setUp() {
        userRepo = new FakeUserRepo();
        foodRepo = new FakeFoodRepo();
        orderRepo = new FakeOrderRepo();
        passwordEncoder = new BCryptPasswordEncoder();

        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);

        CustomUserDetailsService uds = new CustomUserDetailsService();
        ReflectionTestUtils.setField(uds, "userRepository", userRepo);

        FakeAuthManager authManager = new FakeAuthManager(userRepo, passwordEncoder);

        authController = new AuthController();
        ReflectionTestUtils.setField(authController, "repo", userRepo);
        ReflectionTestUtils.setField(authController, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(authController, "authenticationManager", authManager);
        ReflectionTestUtils.setField(authController, "userDetailsService", uds);
        ReflectionTestUtils.setField(authController, "jwtService", jwtService);

        orderService = new OrderService(orderRepo, userRepo, foodRepo);
        foodService = new FoodService(foodRepo, orderRepo);
        orderController = new OrderController(orderService);
        adminOrderController = new AdminOrderController(orderService);

        // Seed users
        alice = new User("alice", passwordEncoder.encode("alice123"), Role.USER);
        userRepo.save(alice);
        bob = new User("bob", passwordEncoder.encode("bob123"), Role.USER);
        userRepo.save(bob);
        adminUser = new User("admin", passwordEncoder.encode("adminPass123"), Role.ADMIN);
        userRepo.save(adminUser);

        // Seed food
        burger = new Food("Cheese Burger", 120.0, "Fast Food", "burger.jpg");
        foodRepo.save(burger);
    }

    // ─── 1. Admin login timing oracle fix ──────────────────────────────────────

    /**
     * SECURITY: If a normal user submits WRONG password to /auth/admin/login,
     * they should get 401 (password failed), NOT 403 (revealing the account is not admin).
     * This prevents enumeration of "is this username an admin?" via different status codes.
     */
    @Test
    void adminLogin_WrongPassword_Returns401NotRevealingRole() {
        // alice is a USER. With the fixed order (authenticate first, check role second),
        // wrong password → 401 whether account is ADMIN or USER.
        LoginRequest req = new LoginRequest("alice", "WRONGPASSWORD");
        ResponseEntity<AuthResponse> res = authController.adminLogin(req);
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode(),
                "Wrong password must return 401 regardless of role, never 403");
        assertNull(res.getBody().getToken());
    }

    /**
     * SECURITY: Normal user with correct password → 403 (not admin).
     * But only after password is verified — attacker already knew credentials.
     */
    @Test
    void adminLogin_CorrectPasswordButNotAdmin_Returns403() {
        LoginRequest req = new LoginRequest("alice", "alice123");
        ResponseEntity<AuthResponse> res = authController.adminLogin(req);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        assertNull(res.getBody().getToken());
    }

    // ─── 2. Password never returned in auth responses ───────────────────────────

    @Test
    void register_PasswordNeverReturnedInResponse() {
        RegisterRequest req = new RegisterRequest("newuser", "securepass123");
        ResponseEntity<AuthResponse> res = authController.register(req);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        // AuthResponse must not contain the raw password
        assertNull(res.getBody().getToken(), "No token on register");
        // Verify saved password is hashed, not plain
        User saved = userRepo.findByUsername("newuser");
        assertNotEquals("securepass123", saved.getPassword());
        assertTrue(passwordEncoder.matches("securepass123", saved.getPassword()));
    }

    @Test
    void login_PasswordNotExposedInJwtToken() {
        // After login, the JWT should not embed the plain-text password anywhere
        LoginRequest req = new LoginRequest("alice", "alice123");
        ResponseEntity<AuthResponse> res = authController.login(req);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        String token = res.getBody().getToken();
        assertNotNull(token);
        // JWT is 3 base64 segments; raw password should not appear anywhere in the token string
        assertFalse(token.contains("alice123"),
                "Plain-text password must never appear inside the JWT token");
    }

    // ─── 3. JWT token validated after generation ────────────────────────────────

    @Test
    void login_GeneratedTokenIsValid() {
        LoginRequest req = new LoginRequest("alice", "alice123");
        ResponseEntity<AuthResponse> res = authController.login(req);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        String token = res.getBody().getToken();
        assertTrue(jwtService.validateToken(token), "Generated token must pass validation");
        assertEquals("alice", jwtService.extractUsername(token));
    }

    // ─── 4. Order total strictly from DB, not client ────────────────────────────

    @Test
    void createOrder_TotalAlwaysFromDatabasePrice() {
        CreateOrderRequest req = new CreateOrderRequest(
                Collections.singletonList(new OrderItemRequest(burger.getId(), 3)), // 3 × ₹120
                "COD"
        );
        OrderResponse res = orderService.createOrder("alice", req);
        assertEquals(360.0, res.getTotalAmount(), 0.001,
                "Total must be 3 × 120 = 360 from DB, client cannot inject a different total");
    }

    // ─── 5. Cross-user order isolation ──────────────────────────────────────────

    @Test
    void user_CannotViewAnotherUsersOrder() {
        OrderResponse aliceOrder = orderService.createOrder("alice",
                new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "UPI"));
        // Bob tries to access Alice's order — must be denied
        assertThrows(AccessDeniedException.class, () ->
                orderService.getOrderById(aliceOrder.getId(), "bob", false));
    }

    @Test
    void user_CanViewTheirOwnOrder() {
        OrderResponse aliceOrder = orderService.createOrder("alice",
                new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "UPI"));
        OrderResponse fetched = orderService.getOrderById(aliceOrder.getId(), "alice", false);
        assertEquals(aliceOrder.getId(), fetched.getId());
        assertEquals("alice", fetched.getUsername());
    }

    @Test
    void admin_CanViewAnyUsersOrder() {
        OrderResponse aliceOrder = orderService.createOrder("alice",
                new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "UPI"));
        OrderResponse fetched = orderService.getOrderById(aliceOrder.getId(), "admin", true);
        assertEquals(aliceOrder.getId(), fetched.getId());
    }

    // ─── 6. Order history isolation ─────────────────────────────────────────────

    @Test
    void getOrdersByUser_ReturnsOnlyThatUsersOrders() {
        orderService.createOrder("alice", new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "UPI"));
        orderService.createOrder("bob", new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 2)), "COD"));

        List<OrderResponse> aliceOrders = orderService.getOrdersByUser("alice");
        assertTrue(aliceOrders.stream().allMatch(o -> "alice".equals(o.getUsername())),
                "Alice's order history must contain only her orders");
        assertEquals(1, aliceOrders.size());

        List<OrderResponse> bobOrders = orderService.getOrdersByUser("bob");
        assertTrue(bobOrders.stream().allMatch(o -> "bob".equals(o.getUsername())));
        assertEquals(1, bobOrders.size());
    }

    // ─── 7. Admin can list all orders ───────────────────────────────────────────

    @Test
    void admin_CanViewAllOrders() {
        orderService.createOrder("alice", new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "UPI"));
        orderService.createOrder("bob", new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "COD"));

        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                "admin", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        ResponseEntity<List<OrderResponse>> res = adminOrderController.getAllOrders();
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(2, res.getBody().size(), "Admin must see all orders across all users");
    }

    // ─── 8. Empty and zero-quantity orders rejected ──────────────────────────────

    @Test
    void createOrder_EmptyItems_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder("alice",
                        new CreateOrderRequest(Collections.emptyList(), "UPI")));
    }

    @Test
    void createOrder_NullItems_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder("alice",
                        new CreateOrderRequest(null, "UPI")));
    }

    @Test
    void createOrder_ZeroQuantity_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder("alice",
                        new CreateOrderRequest(
                                Collections.singletonList(new OrderItemRequest(burger.getId(), 0)), "UPI")));
    }

    @Test
    void createOrder_NegativeQuantity_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder("alice",
                        new CreateOrderRequest(
                                Collections.singletonList(new OrderItemRequest(burger.getId(), -3)), "UPI")));
    }

    // ─── 9. Historical order data preserved after food deletion ─────────────────

    @Test
    void deletingFood_PreservesHistoricalOrderNameAndPrice() {
        // Place an order capturing burger at ₹120
        OrderResponse placedOrder = orderService.createOrder("alice",
                new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 2)), "COD"));

        // Delete the food item
        foodService.deleteFood(burger.getId());
        assertFalse(foodRepo.map.containsKey(burger.getId()), "Food must be deleted from DB");

        // The historical order must still show the correct name and price
        OrderResponse historicalOrder = orderService.getOrderById(placedOrder.getId(), "alice", false);
        assertEquals(1, historicalOrder.getItems().size());
        OrderItemResponse item = historicalOrder.getItems().get(0);
        assertEquals("Cheese Burger", item.getFoodName(),
                "Historical food name must be preserved even after deletion");
        assertEquals(120.0, item.getPrice(), 0.001,
                "Historical price must be preserved even after deletion");
        assertEquals(240.0, historicalOrder.getTotalAmount(), 0.001,
                "Historical total must remain unchanged");
    }

    // ─── 10. Invalid status transitions rejected ─────────────────────────────────

    @Test
    void updateOrderStatus_InvalidEnum_ThrowsIllegalArgument() {
        OrderResponse order = orderService.createOrder("alice",
                new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "UPI"));
        assertThrows(IllegalArgumentException.class, () ->
                orderService.updateOrderStatus(order.getId(), "FLYING"));
    }

    @Test
    void updateOrderStatus_CancelledOrderCannotBeUpdated() {
        OrderResponse order = orderService.createOrder("alice",
                new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "UPI"));
        orderService.updateOrderStatus(order.getId(), "CANCELLED");
        assertThrows(IllegalStateException.class, () ->
                orderService.updateOrderStatus(order.getId(), "PENDING"));
    }

    @Test
    void updateOrderStatus_DeliveredOrderCannotBeUpdated() {
        OrderResponse order = orderService.createOrder("alice",
                new CreateOrderRequest(Collections.singletonList(new OrderItemRequest(burger.getId(), 1)), "UPI"));
        orderService.updateOrderStatus(order.getId(), "DELIVERED");
        assertThrows(IllegalStateException.class, () ->
                orderService.updateOrderStatus(order.getId(), "CANCELLED"));
    }

    // ─── 11. Non-existent food ID rejected ──────────────────────────────────────

    @Test
    void createOrder_NonExistentFoodId_ThrowsNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                orderService.createOrder("alice",
                        new CreateOrderRequest(
                                Collections.singletonList(new OrderItemRequest(99999L, 1)), "UPI")));
    }

    // ─── 12. Food search ─────────────────────────────────────────────────────────

    @Test
    void searchFood_ReturnsMatchingFoods() {
        List<Food> results = foodService.searchFoodByName("burger");
        assertEquals(1, results.size());
        assertEquals("Cheese Burger", results.get(0).getName());
    }

    @Test
    void searchFood_EmptyQuery_ReturnsAll() {
        Food pizza = new Food("Veg Pizza", 200.0, "Pizza", "pizza.jpg");
        foodRepo.save(pizza);
        List<Food> results = foodService.searchFoodByName("");
        assertEquals(2, results.size());
    }

    @Test
    void searchFood_NoMatch_ReturnsEmpty() {
        List<Food> results = foodService.searchFoodByName("sushi");
        assertTrue(results.isEmpty());
    }
}

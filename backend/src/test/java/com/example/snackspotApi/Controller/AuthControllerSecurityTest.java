package com.example.snackspotApi.Controller;

import com.example.snackspotApi.Dto.AuthResponse;
import com.example.snackspotApi.Dto.LoginRequest;
import com.example.snackspotApi.Dto.RegisterRequest;
import com.example.snackspotApi.Dto.UserDto;
import com.example.snackspotApi.Entity.Role;
import com.example.snackspotApi.Entity.User;
import com.example.snackspotApi.Repository.UserRepository;
import com.example.snackspotApi.Security.CustomUserDetailsService;
import com.example.snackspotApi.Security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerSecurityTest {

    private AuthController authController;
    private InMemoryUserRepository fakeRepo;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private FakeAuthenticationManager fakeAuthManager;

    // In-memory fake repository
    static class InMemoryUserRepository implements UserRepository {
        private final Map<String, User> userMap = new HashMap<>();
        private long currentId = 1L;

        @Override
        public User findByUsername(String username) {
            return userMap.get(username);
        }

        @Override
        public <S extends User> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(currentId++);
            }
            userMap.put(entity.getUsername(), entity);
            return entity;
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(userMap.values());
        }

        @Override public <S extends User> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<User> findById(Long aLong) { return Optional.ofNullable(userMap.values().stream().filter(u -> u.getId().equals(aLong)).findFirst().orElse(null)); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<User> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return userMap.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(User entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends User> entities) {}
        @Override public void deleteAll() { userMap.clear(); }
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

    static class FakeAuthenticationManager implements AuthenticationManager {
        private final InMemoryUserRepository repo;
        private final PasswordEncoder encoder;

        public FakeAuthenticationManager(InMemoryUserRepository repo, PasswordEncoder encoder) {
            this.repo = repo;
            this.encoder = encoder;
        }

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            String username = authentication.getName();
            String password = authentication.getCredentials().toString();

            User user = repo.findByUsername(username);
            if (user == null || !encoder.matches(password, user.getPassword())) {
                throw new BadCredentialsException("Bad credentials");
            }
            return new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList());
        }
    }

    @BeforeEach
    void setUp() {
        authController = new AuthController();
        fakeRepo = new InMemoryUserRepository();
        passwordEncoder = new BCryptPasswordEncoder();
        fakeAuthManager = new FakeAuthenticationManager(fakeRepo, passwordEncoder);

        CustomUserDetailsService userDetailsService = new CustomUserDetailsService();
        ReflectionTestUtils.setField(userDetailsService, "userRepository", fakeRepo);

        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);

        ReflectionTestUtils.setField(authController, "repo", fakeRepo);
        ReflectionTestUtils.setField(authController, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(authController, "authenticationManager", fakeAuthManager);
        ReflectionTestUtils.setField(authController, "userDetailsService", userDetailsService);
        ReflectionTestUtils.setField(authController, "jwtService", jwtService);
    }

    @Test
    void testRegisterSuccess_HashesPassword() {
        RegisterRequest req = new RegisterRequest("john_doe", "mySecret123");
        ResponseEntity<AuthResponse> response = authController.register(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        User saved = fakeRepo.findByUsername("john_doe");
        assertNotNull(saved);
        assertNotEquals("mySecret123", saved.getPassword());
        assertTrue(passwordEncoder.matches("mySecret123", saved.getPassword()));
        assertEquals(Role.USER, saved.getRole());
    }

    @Test
    void testRegisterDuplicateUsername_FailsWithConflict() {
        fakeRepo.save(new User("existinguser", passwordEncoder.encode("pass123"), Role.USER));

        RegisterRequest req = new RegisterRequest("existinguser", "differentPass123");
        ResponseEntity<AuthResponse> response = authController.register(req);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void testRegisterValidationFailure() {
        RegisterRequest shortUser = new RegisterRequest("ab", "pass12345");
        ResponseEntity<AuthResponse> res1 = authController.register(shortUser);
        assertEquals(HttpStatus.BAD_REQUEST, res1.getStatusCode());

        RegisterRequest shortPass = new RegisterRequest("validuser", "123");
        ResponseEntity<AuthResponse> res2 = authController.register(shortPass);
        assertEquals(HttpStatus.BAD_REQUEST, res2.getStatusCode());
    }

    @Test
    void testLoginSuccess_GeneratesJwt() {
        fakeRepo.save(new User("customer", passwordEncoder.encode("password123"), Role.USER));

        LoginRequest req = new LoginRequest("customer", "password123");
        ResponseEntity<AuthResponse> response = authController.login(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getToken());
        assertEquals("customer", response.getBody().getUsername());
        assertEquals(Role.USER, response.getBody().getRole());

        // Validate returned JWT
        assertEquals("customer", jwtService.extractUsername(response.getBody().getToken()));
        assertTrue(jwtService.validateToken(response.getBody().getToken()));
    }

    @Test
    void testLoginInvalidPassword_FailsWithUnauthorized() {
        fakeRepo.save(new User("customer", passwordEncoder.encode("password123"), Role.USER));

        LoginRequest req = new LoginRequest("customer", "wrongPassword");
        ResponseEntity<AuthResponse> response = authController.login(req);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getToken());
    }

    @Test
    void testAdminLoginSuccess_ForAdminRole() {
        fakeRepo.save(new User("admin", passwordEncoder.encode("adminPass123"), Role.ADMIN));

        LoginRequest req = new LoginRequest("admin", "adminPass123");
        ResponseEntity<AuthResponse> response = authController.adminLogin(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(Role.ADMIN, response.getBody().getRole());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void testAdminLoginDenied_ForNormalUser() {
        fakeRepo.save(new User("normaluser", passwordEncoder.encode("userPass123"), Role.USER));

        LoginRequest req = new LoginRequest("normaluser", "userPass123");
        ResponseEntity<AuthResponse> response = authController.adminLogin(req);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getToken());
    }
}

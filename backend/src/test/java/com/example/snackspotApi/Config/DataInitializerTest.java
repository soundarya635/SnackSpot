package com.example.snackspotApi.Config;

import com.example.snackspotApi.Entity.Role;
import com.example.snackspotApi.Entity.User;
import com.example.snackspotApi.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DataInitializerTest {

    private PasswordEncoder passwordEncoder;
    private InMemoryUserRepository fakeRepo;
    private DataInitializer dataInitializer;

    static class InMemoryUserRepository implements UserRepository {
        private final Map<String, User> userMap = new HashMap<>();
        private long currentId = 1L;

        @Override public User findByUsername(String username) { return userMap.get(username); }
        @Override public <S extends User> S save(S entity) {
            if (entity.getId() == null) entity.setId(currentId++);
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

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        fakeRepo = new InMemoryUserRepository();
        dataInitializer = new DataInitializer(fakeRepo, passwordEncoder);
    }

    @Test
    void testMigratesPlainTextPasswordToBCryptHash() {
        User legacyUser = new User("legacy_customer", "plainTextPass123", Role.USER);
        fakeRepo.save(legacyUser);

        dataInitializer.run();

        User migrated = fakeRepo.findByUsername("legacy_customer");
        assertNotNull(migrated);
        assertNotEquals("plainTextPass123", migrated.getPassword());
        assertTrue(migrated.getPassword().startsWith("$2a$") || migrated.getPassword().startsWith("$2b$"));
        assertTrue(passwordEncoder.matches("plainTextPass123", migrated.getPassword()));
    }

    @Test
    void testDoesNotReHashAlreadyHashedPassword() {
        String existingHash = passwordEncoder.encode("alreadyHashedPass");
        User user = new User("hashed_user", existingHash, Role.USER);
        fakeRepo.save(user);

        dataInitializer.run();

        User retrieved = fakeRepo.findByUsername("hashed_user");
        assertEquals(existingHash, retrieved.getPassword(), "Already hashed password should not be altered");
    }

    @Test
    void testDoesNotCreateAdminWhenEnvVarsNotProvided() {
        ReflectionTestUtils.setField(dataInitializer, "adminUsername", "");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "");

        dataInitializer.run();

        assertNull(fakeRepo.findByUsername("admin"));
        assertEquals(0, fakeRepo.count());
    }

    @Test
    void testCreatesAdminWhenEnvVarsProvided() {
        ReflectionTestUtils.setField(dataInitializer, "adminUsername", "sysadmin");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "secureAdminPassword999");

        dataInitializer.run();

        User admin = fakeRepo.findByUsername("sysadmin");
        assertNotNull(admin);
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(passwordEncoder.matches("secureAdminPassword999", admin.getPassword()));
    }
}

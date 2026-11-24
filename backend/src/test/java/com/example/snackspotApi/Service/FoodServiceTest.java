package com.example.snackspotApi.Service;

import com.example.snackspotApi.Dto.FoodRequest;
import com.example.snackspotApi.Entity.Food;
import com.example.snackspotApi.Exception.ResourceNotFoundException;
import com.example.snackspotApi.Repository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FoodServiceTest {

    private FoodService foodService;
    private InMemoryFoodRepository foodRepo;

    static class InMemoryFoodRepository implements FoodRepository {
        private final Map<Long, Food> store = new HashMap<>();
        private long idGen = 1L;

        @Override public List<Food> findByNameContainingIgnoreCase(String name) {
            List<Food> matched = new ArrayList<>();
            for (Food f : store.values()) {
                if (f.getName() != null && f.getName().toLowerCase().contains(name.toLowerCase())) {
                    matched.add(f);
                }
            }
            return matched;
        }

        @Override public <S extends Food> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(idGen++);
            }
            store.put(entity.getId(), entity);
            return entity;
        }

        @Override public Optional<Food> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override public List<Food> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override public void delete(Food entity) {
            store.remove(entity.getId());
        }

        @Override public void deleteById(Long id) {
            store.remove(id);
        }

        @Override public <S extends Food> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public boolean existsById(Long aLong) { return store.containsKey(aLong); }
        @Override public List<Food> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return store.size(); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Food> entities) {}
        @Override public void deleteAll() { store.clear(); }
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

    @BeforeEach
    void setUp() {
        foodRepo = new InMemoryFoodRepository();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        foodService = new FoodService(foodRepo, orderRepo);
    }

    static class InMemoryOrderRepository implements com.example.snackspotApi.Repository.OrderRepository {
        @Override public void nullifyFoodReferenceInOrderItems(Long foodId) {}
        @Override public List<com.example.snackspotApi.Entity.Order> findByUserOrderByCreatedAtDesc(com.example.snackspotApi.Entity.User user) { return Collections.emptyList(); }
        @Override public List<com.example.snackspotApi.Entity.Order> findAllByOrderByCreatedAtDesc() { return Collections.emptyList(); }
        @Override public void flush() {}
        @Override public <S extends com.example.snackspotApi.Entity.Order> S saveAndFlush(S entity) { return null; }
        @Override public <S extends com.example.snackspotApi.Entity.Order> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<com.example.snackspotApi.Entity.Order> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public com.example.snackspotApi.Entity.Order getOne(Long aLong) { return null; }
        @Override public com.example.snackspotApi.Entity.Order getById(Long aLong) { return null; }
        @Override public com.example.snackspotApi.Entity.Order getReferenceById(Long aLong) { return null; }
        @Override public <S extends com.example.snackspotApi.Entity.Order> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends com.example.snackspotApi.Entity.Order> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends com.example.snackspotApi.Entity.Order> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends com.example.snackspotApi.Entity.Order> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public <S extends com.example.snackspotApi.Entity.Order> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends com.example.snackspotApi.Entity.Order> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends com.example.snackspotApi.Entity.Order, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends com.example.snackspotApi.Entity.Order> S save(S entity) { return entity; }
        @Override public <S extends com.example.snackspotApi.Entity.Order> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<com.example.snackspotApi.Entity.Order> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<com.example.snackspotApi.Entity.Order> findAll() { return Collections.emptyList(); }
        @Override public List<com.example.snackspotApi.Entity.Order> findAllById(Iterable<Long> longs) { return Collections.emptyList(); }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(com.example.snackspotApi.Entity.Order entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends com.example.snackspotApi.Entity.Order> entities) {}
        @Override public void deleteAll() {}
        @Override public List<com.example.snackspotApi.Entity.Order> findAll(org.springframework.data.domain.Sort sort) { return Collections.emptyList(); }
        @Override public org.springframework.data.domain.Page<com.example.snackspotApi.Entity.Order> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
    }

    @Test
    void testCreateFoodSuccessful() {
        FoodRequest req = new FoodRequest("Crispy Samosa", 25.0, "Snacks", "http://img.com/samosa.png");
        Food created = foodService.createFood(req);

        assertNotNull(created.getId());
        assertEquals("Crispy Samosa", created.getName());
        assertEquals(25.0, created.getPrice());
        assertEquals("Snacks", created.getCategory());
    }

    @Test
    void testGetAllFoods() {
        foodService.createFood(new FoodRequest("Burger", 120.0, "Fast Food", ""));
        foodService.createFood(new FoodRequest("Pizza", 250.0, "Fast Food", ""));

        List<Food> foods = foodService.getAllFoods();
        assertEquals(2, foods.size());
    }

    @Test
    void testGetFoodByIdSuccess() {
        Food created = foodService.createFood(new FoodRequest("Sandwich", 80.0, "Breakfast", ""));
        Food found = foodService.getFoodById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Sandwich", found.getName());
    }

    @Test
    void testGetFoodByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> foodService.getFoodById(999L));
    }

    @Test
    void testUpdateFoodModifiesExistingRecord() {
        Food initial = foodService.createFood(new FoodRequest("Old Name", 50.0, "Snacks", ""));
        Long id = initial.getId();

        FoodRequest updateReq = new FoodRequest("Updated Name", 75.0, "Meal", "http://new.jpg");
        Food updated = foodService.updateFood(id, updateReq);

        assertEquals(id, updated.getId(), "Should keep the exact same ID");
        assertEquals("Updated Name", updated.getName());
        assertEquals(75.0, updated.getPrice());
        assertEquals(1, foodRepo.count(), "Should not create a duplicate row");
    }

    @Test
    void testUpdateFoodNotFound() {
        FoodRequest updateReq = new FoodRequest("Updated", 50.0, "Meal", "");
        assertThrows(ResourceNotFoundException.class, () -> foodService.updateFood(999L, updateReq));
    }

    @Test
    void testDeleteFoodSuccess() {
        Food created = foodService.createFood(new FoodRequest("To Delete", 40.0, "Snacks", ""));
        Long id = created.getId();

        foodService.deleteFood(id);
        assertEquals(0, foodRepo.count());
        assertThrows(ResourceNotFoundException.class, () -> foodService.getFoodById(id));
    }

    @Test
    void testDeleteFoodNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> foodService.deleteFood(999L));
    }
}

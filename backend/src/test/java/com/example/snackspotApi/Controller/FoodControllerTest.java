package com.example.snackspotApi.Controller;

import com.example.snackspotApi.Dto.FoodRequest;
import com.example.snackspotApi.Entity.Food;
import com.example.snackspotApi.Exception.GlobalExceptionHandler;
import com.example.snackspotApi.Exception.ResourceNotFoundException;
import com.example.snackspotApi.Service.FoodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FoodControllerTest {

    private FoodController foodController;
    private GlobalExceptionHandler exceptionHandler;
    private MockFoodService mockFoodService;

    static class MockFoodService extends FoodService {
        private Food mockFood;
        private boolean notFound = false;

        public MockFoodService() {
            super(null, null);
            this.mockFood = new Food("Test Food", 100.0, "Fast Food", "url");
            this.mockFood.setId(1L);
        }

        @Override public List<Food> getAllFoods() { return Arrays.asList(mockFood); }
        @Override public Food getFoodById(Long id) {
            if (notFound || !id.equals(1L)) throw new ResourceNotFoundException("Food not found");
            return mockFood;
        }
        @Override public Food createFood(FoodRequest request) {
            Food f = new Food(request.getName(), request.getPrice(), request.getCategory(), request.getImageurl());
            f.setId(10L);
            return f;
        }
        @Override public Food updateFood(Long id, FoodRequest request) {
            if (notFound) throw new ResourceNotFoundException("Food not found");
            Food f = new Food(request.getName(), request.getPrice(), request.getCategory(), request.getImageurl());
            f.setId(id);
            return f;
        }
        @Override public void deleteFood(Long id) {
            if (notFound) throw new ResourceNotFoundException("Food not found");
        }
        @Override public List<Food> searchFoodByName(String name) { return Arrays.asList(mockFood); }
    }

    @BeforeEach
    void setUp() {
        mockFoodService = new MockFoodService();
        foodController = new FoodController(mockFoodService);
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testGetAllFoods() {
        ResponseEntity<List<Food>> res = foodController.getAllFoods();
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }

    @Test
    void testGetFoodById() {
        ResponseEntity<Food> res = foodController.getFoodById(1L);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("Test Food", res.getBody().getName());
    }

    @Test
    void testCreateFood() {
        FoodRequest req = new FoodRequest("New Burger", 150.0, "Fast Food", "");
        ResponseEntity<Food> res = foodController.addFood(req);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertEquals(10L, res.getBody().getId());
    }

    @Test
    void testUpdateFood() {
        FoodRequest req = new FoodRequest("Updated Burger", 160.0, "Fast Food", "");
        ResponseEntity<Food> res = foodController.updateFood(1L, req);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1L, res.getBody().getId());
        assertEquals("Updated Burger", res.getBody().getName());
    }

    @Test
    void testDeleteFood() {
        ResponseEntity<Void> res = foodController.deleteFood(1L);
        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

    @Test
    void testNotFoundExceptionHandling() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Food item not found with id: 99");
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/foods/99");
        var errorRes = exceptionHandler.handleResourceNotFoundException(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, errorRes.getStatusCode());
        assertFalse(errorRes.getBody().isSuccess());
        assertEquals("Food item not found with id: 99", errorRes.getBody().getMessage());
        assertEquals(404, errorRes.getBody().getStatus());
    }

    @Test
    void testValidationFailureHandling() throws Exception {
        FoodRequest invalidReq = new FoodRequest("", -5.0, "", "");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(invalidReq, "foodRequest");
        bindingResult.addError(new FieldError("foodRequest", "name", "Food name must not be blank"));
        bindingResult.addError(new FieldError("foodRequest", "price", "Price must be greater than zero"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/foods");
        var errorRes = exceptionHandler.handleValidationExceptions(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, errorRes.getStatusCode());
        assertFalse(errorRes.getBody().isSuccess());
        assertEquals(400, errorRes.getBody().getStatus());
        assertTrue(errorRes.getBody().getErrors().containsKey("name"));
        assertTrue(errorRes.getBody().getErrors().containsKey("price"));
    }
}

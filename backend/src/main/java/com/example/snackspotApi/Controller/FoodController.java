package com.example.snackspotApi.Controller;

import com.example.snackspotApi.Dto.FoodRequest;
import com.example.snackspotApi.Entity.Food;
import com.example.snackspotApi.Service.FoodService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/foods")
public class FoodController {

    private final FoodService foodService;

    @Autowired
    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    // Public: Retrieve all food items
    @GetMapping
    public ResponseEntity<List<Food>> getAllFoods() {
        return ResponseEntity.ok(foodService.getAllFoods());
    }

    // Public: Retrieve single food item by ID
    @GetMapping("/{id}")
    public ResponseEntity<Food> getFoodById(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodById(id));
    }

    // Public: Search foods by name
    @GetMapping("/search")
    public ResponseEntity<List<Food>> searchFood(@RequestParam(required = false, defaultValue = "") String name) {
        return ResponseEntity.ok(foodService.searchFoodByName(name));
    }

    // Admin-only: Create a new food item
    @PostMapping
    public ResponseEntity<Food> addFood(@Valid @RequestBody FoodRequest foodRequest) {
        Food createdFood = foodService.createFood(foodRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFood);
    }

    // Admin-only: Update existing food item
    @PutMapping("/{id}")
    public ResponseEntity<Food> updateFood(@PathVariable Long id, @Valid @RequestBody FoodRequest foodRequest) {
        Food updatedFood = foodService.updateFood(id, foodRequest);
        return ResponseEntity.ok(updatedFood);
    }

    // Admin-only: Delete food item
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }
}

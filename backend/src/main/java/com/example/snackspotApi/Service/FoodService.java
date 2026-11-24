package com.example.snackspotApi.Service;

import com.example.snackspotApi.Dto.FoodRequest;
import com.example.snackspotApi.Entity.Food;
import com.example.snackspotApi.Exception.ResourceNotFoundException;
import com.example.snackspotApi.Repository.FoodRepository;
import com.example.snackspotApi.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FoodService {

    private final FoodRepository foodRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public FoodService(FoodRepository foodRepository, OrderRepository orderRepository) {
        this.foodRepository = foodRepository;
        this.orderRepository = orderRepository;
    }

    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    public Food getFoodById(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + id));
    }

    public List<Food> searchFoodByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllFoods();
        }
        return foodRepository.findByNameContainingIgnoreCase(name.trim());
    }

    @Transactional
    public Food createFood(FoodRequest request) {
        Food food = new Food();
        food.setName(request.getName().trim());
        food.setPrice(request.getPrice());
        food.setCategory(request.getCategory().trim());
        food.setImageurl(request.getImageurl() != null ? request.getImageurl().trim() : null);

        return foodRepository.save(food);
    }

    @Transactional
    public Food updateFood(Long id, FoodRequest request) {
        Food existingFood = getFoodById(id);

        existingFood.setName(request.getName().trim());
        existingFood.setPrice(request.getPrice());
        existingFood.setCategory(request.getCategory().trim());
        existingFood.setImageurl(request.getImageurl() != null ? request.getImageurl().trim() : null);

        return foodRepository.save(existingFood);
    }

    @Transactional
    public void deleteFood(Long id) {
        Food existingFood = getFoodById(id);
        // Nullify foreign key references in historical order items to preserve past orders
        orderRepository.nullifyFoodReferenceInOrderItems(id);
        foodRepository.delete(existingFood);
    }
}

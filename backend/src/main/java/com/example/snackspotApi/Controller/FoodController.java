package com.example.snackspotApi.Controller;

import com.example.snackspotApi.Entity.Food;
import com.example.snackspotApi.Repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/foods")
public class FoodController {
    @Autowired
    private FoodRepository repo;
    @GetMapping
    public List<Food> getAllFoods(){
        return repo.findAll();
    }
    @PostMapping
    public Food addFood(@RequestBody Food food){
        return repo.save(food);
    }
    @DeleteMapping("/{id}")
    public void deleteFood(@PathVariable Long id){
        repo.deleteById(id);
    }
    @GetMapping("/search")
    public List<Food> searchFood(@RequestParam String name){
        return repo.findByNameContainingIgnoreCase(name);
    }
}

package com.example.snackspotApi.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = true)
    private Food food;

    @Column(nullable = false)
    private String foodName; // Preserves item name even if Food entity is deleted or renamed

    @Column(nullable = true)
    private String foodCategory;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price; // Price captured at the time of ordering

    public OrderItem() {}

    public OrderItem(Order order, Food food, Integer quantity, Double price) {
        this.order = order;
        this.food = food;
        this.foodName = food != null ? food.getName() : "Unknown Item";
        this.foodCategory = food != null ? food.getCategory() : "Unknown";
        this.quantity = quantity;
        this.price = price;
    }

    public OrderItem(Order order, Food food, String foodName, String foodCategory, Integer quantity, Double price) {
        this.order = order;
        this.food = food;
        this.foodName = foodName;
        this.foodCategory = foodCategory;
        this.quantity = quantity;
        this.price = price;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodCategory() {
        return foodCategory;
    }

    public void setFoodCategory(String foodCategory) {
        this.foodCategory = foodCategory;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}

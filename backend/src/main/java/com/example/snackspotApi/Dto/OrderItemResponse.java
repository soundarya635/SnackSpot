package com.example.snackspotApi.Dto;

public class OrderItemResponse {
    private Long id;
    private Long foodId;
    private String foodName;
    private String foodCategory;
    private String imageUrl;
    private Integer quantity;
    private Double price;
    private Double subtotal;

    public OrderItemResponse() {}

    public OrderItemResponse(Long id, Long foodId, String foodName, String foodCategory, String imageUrl, Integer quantity, Double price, Double subtotal) {
        this.id = id;
        this.foodId = foodId;
        this.foodName = foodName;
        this.foodCategory = foodCategory;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}

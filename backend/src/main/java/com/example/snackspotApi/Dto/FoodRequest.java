package com.example.snackspotApi.Dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FoodRequest {

    @NotBlank(message = "Food name must not be blank")
    @Size(min = 2, max = 100, message = "Food name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private Double price;

    @NotBlank(message = "Category must not be blank")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    private String imageurl;

    public FoodRequest() {}

    public FoodRequest(String name, Double price, String category, String imageurl) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.imageurl = imageurl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}

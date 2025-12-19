package backend.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class FoodLogEntry {
    private String id;
    private LocalDate date;
    private LocalTime time;
    private String mealType;
    private Food food;
    private double servings;
    
    public FoodLogEntry(LocalDate date, LocalTime time, String mealType, Food food, double servings) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.time = time;
        this.mealType = mealType;
        this.food = food;
        this.servings = servings;
    }
    
    // Constructor with ID for loading from database
    public FoodLogEntry(String id, LocalDate date, LocalTime time, String mealType, Food food, double servings) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.mealType = mealType;
        this.food = food;
        this.servings = servings;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public LocalTime getTime() {
        return time;
    }
    
    public void setTime(LocalTime time) {
        this.time = time;
    }
    
    public String getMealType() {
        return mealType;
    }
    
    public void setMealType(String mealType) {
        this.mealType = mealType;
    }
    
    public Food getFood() {
        return food;
    }
    
    public void setFood(Food food) {
        this.food = food;
    }
    
    public double getServings() {
        return servings;
    }
    
    public void setServings(double servings) {
        this.servings = servings;
    }
    
    public double getTotalCalories() {
        return food.getCaloriesPerServing() * servings;
    }
}
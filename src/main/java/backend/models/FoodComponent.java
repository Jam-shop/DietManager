package backend.models;

public class FoodComponent {
    private Food food;
    private double servings;
    
    public FoodComponent(Food food, double servings) {
        this.food = food;
        this.servings = servings;
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
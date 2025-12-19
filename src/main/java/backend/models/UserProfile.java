package backend.models;

import java.time.LocalDate;

public class UserProfile {
    private String name;
    private String sex;
    private int age;
    private double height; // in cm
    private double weight; // in kg
    private int activityLevel; // 0-10 scale
    private LocalDate lastUpdated;
    private String calorieCalculationMethod; // "Harris-Benedict" or "Mifflin-St Jeor"
    
    public UserProfile(String name, String sex, int age, double height, double weight, int activityLevel) {
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.activityLevel = activityLevel;
        this.lastUpdated = LocalDate.now();
        this.calorieCalculationMethod = "Harris-Benedict"; // Default method
    }
    
    // Calculate daily calorie needs using selected method
    public double calculateDailyCalorieNeeds() {
        if ("Mifflin-St Jeor".equals(calorieCalculationMethod)) {
            return calculateMifflinStJeor();
        } else {
            return calculateHarrisBenedict(); // Default
        }
    }
    
    // Harris-Benedict Equation
    private double calculateHarrisBenedict() {
        double bmr;
        
        if ("Male".equalsIgnoreCase(sex)) {
            bmr = 66.47 + (13.75 * weight) + (5.003 * height) - (6.755 * age);
        } else {
            bmr = 655.1 + (9.563 * weight) + (1.85 * height) - (4.676 * age);
        }
        
        // Apply activity factor
        double activityFactor = getActivityFactor();
        return bmr * activityFactor;
    }
    
    // Mifflin-St Jeor Equation
    private double calculateMifflinStJeor() {
        double bmr;
        
        if ("Male".equalsIgnoreCase(sex)) {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }
        
        // Apply activity factor
        double activityFactor = getActivityFactor();
        return bmr * activityFactor;
    }
    
    // Convert activity level (0-10) to an activity factor
    private double getActivityFactor() {
        if (activityLevel <= 1) {
            return 1.2; // Sedentary
        } else if (activityLevel <= 3) {
            return 1.375; // Light activity
        } else if (activityLevel <= 5) {
            return 1.55; // Moderate activity
        } else if (activityLevel <= 7) {
            return 1.725; // Active
        } else {
            return 1.9; // Very active
        }
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSex() {
        return sex;
    }
    
    public void setSex(String sex) {
        this.sex = sex;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    public int getActivityLevel() {
        return activityLevel;
    }
    
    public void setActivityLevel(int activityLevel) {
        this.activityLevel = activityLevel;
    }
    
    public LocalDate getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public String getCalorieCalculationMethod() {
        return calorieCalculationMethod;
    }
    
    public void setCalorieCalculationMethod(String calorieCalculationMethod) {
        this.calorieCalculationMethod = calorieCalculationMethod;
    }
}
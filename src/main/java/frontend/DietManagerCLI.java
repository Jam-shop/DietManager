package frontend;

import backend.models.*;
import backend.services.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;

public class DietManagerCLI {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    private FoodDatabaseService foodDbService;
    private FoodLogService logService;
    private UserProfile userProfile;
    private LocalDate currentDate;
    
    public DietManagerCLI() {
        foodDbService = new FoodDatabaseService();
        logService = new FoodLogService(foodDbService);
        currentDate = LocalDate.now();
        
        // Try to load user profile or create a new one
        loadOrCreateUserProfile();
    }
    
    private void loadOrCreateUserProfile() {
        // In a real implementation, this would load from a file
        // For now, we'll just create a default one
        userProfile = new UserProfile("Default User", "Male", 30, 175, 70, 5);
        System.out.println("User profile loaded or created.");
    }
    
    public void start() {
        System.out.println("Welcome to YADA - Yet Another Diet Assistant");
        System.out.println("Current date: " + currentDate.format(dateFormatter));
        
        boolean exit = false;
        while (!exit) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1: // Manage foods
                    manageFoods();
                    break;
                case 2: // Manage daily log
                    manageDailyLog();
                    break;
                case 3: // Manage user profile
                    manageUserProfile();
                    break;
                case 4: // View calories summary
                    viewCaloriesSummary();
                    break;
                case 5: // Change current date
                    changeCurrentDate();
                    break;
                case 6: // Save data
                    saveData();
                    break;
                case 0: // Exit
                    saveData();
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        
        System.out.println("Thank you for using YADA. Goodbye!");
    }
    
    private void displayMainMenu() {
        System.out.println("\n===== MAIN MENU =====");
        System.out.println("1. Manage Foods");
        System.out.println("2. Manage Daily Log");
        System.out.println("3. Manage User Profile");
        System.out.println("4. View Calories Summary");
        System.out.println("5. Change Current Date (Current: " + currentDate.format(dateFormatter) + ")");
        System.out.println("6. Save Data");
        System.out.println("0. Exit");
    }
    
    private void manageFoods() {
        boolean back = false;
        while (!back) {
            System.out.println("\n===== FOOD MANAGEMENT =====");
            System.out.println("1. View All Foods");
            System.out.println("2. Search Foods");
            System.out.println("3. Add Basic Food");
            System.out.println("4. Create Composite Food");
            System.out.println("5. Delete Food");
            System.out.println("0. Back to Main Menu");
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    viewAllFoods();
                    break;
                case 2:
                    searchFoods();
                    break;
                case 3:
                    addBasicFood();
                    break;
                case 4:
                    createCompositeFood();
                    break;
                case 5:
                    deleteFood();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void viewAllFoods() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("food_database.json"); // root directory
            if (!file.exists()) {
                System.out.println("food_database.json not found in root directory.");
                return;
            }

            List<Food> foods = mapper.readValue(file, new TypeReference<List<Food>>() {});

            if (foods.isEmpty()) {
                System.out.println("No foods found in the database.");
                return;
            }

            System.out.println("\n===== ALL FOODS =====");
            System.out.printf("%-5s %-30s %-15s %-50s\n", "ID", "Name", "Calories", "Keywords");
            System.out.println("-".repeat(100));

            for (Food food : foods) {
                String keywords = String.join(", ", food.getKeywords());
                System.out.printf("%-5s %-30s %-15.1f %-50s\n", 
                    food.getId().substring(0, 4), 
                    food.getName(), 
                    food.getCaloriesPerServing(), 
                    keywords);
            }
        } catch (Exception e) {
            System.out.println("Error reading food database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void searchFoods() {
        System.out.println("\n===== SEARCH FOODS =====");
        System.out.println("1. Search by Any Keyword");
        System.out.println("2. Search by All Keywords");
        
        int searchType = getIntInput("Enter your choice: ");
        if (searchType < 1 || searchType > 2) {
            System.out.println("Invalid choice. Returning to menu.");
            return;
        }
        
        String keywordsInput = getStringInput("Enter search keywords (comma separated): ");
        List<String> keywords = Arrays.asList(keywordsInput.split(",\\s*"));
        
        List<Food> results;
        if (searchType == 1) {
            results = foodDbService.searchFoodsByAnyKeyword(keywords);
        } else {
            results = foodDbService.searchFoodsByAllKeywords(keywords);
        }
        
        if (results.isEmpty()) {
            System.out.println("No foods found matching your search criteria.");
            return;
        }
        
        System.out.println("\n===== SEARCH RESULTS =====");
        System.out.printf("%-5s %-30s %-15s %-50s\n", "ID", "Name", "Calories", "Keywords");
        System.out.println("-".repeat(100));
        
        for (Food food : results) {
            String foodKeywords = String.join(", ", food.getKeywords());
            System.out.printf("%-5s %-30s %-15.1f %-50s\n", 
                    food.getId().substring(0, 4), 
                    food.getName(), 
                    food.getCaloriesPerServing(),
                    foodKeywords);
        }
    }
    
    private void addBasicFood() {
        System.out.println("\n===== ADD BASIC FOOD =====");
        String name = getStringInput("Enter food name: ");
        String keywordsInput = getStringInput("Enter keywords (comma separated): ");
        List<String> keywords = Arrays.asList(keywordsInput.split(",\\s*"));
        double calories = getDoubleInput("Enter calories per serving: ");
        
        Food food = foodDbService.addBasicFood(name, keywords, calories);
        System.out.println("Basic food added successfully: " + food.getName());
    }
    
    private void createCompositeFood() {
        System.out.println("\n===== CREATE COMPOSITE FOOD =====");
        String name = getStringInput("Enter composite food name: ");
        String keywordsInput = getStringInput("Enter keywords (comma separated): ");
        List<String> keywords = Arrays.asList(keywordsInput.split(",\\s*"));
        
        List<FoodComponent> components = new ArrayList<>();
        boolean addMoreComponents = true;
        
        while (addMoreComponents) {
            // Show available foods
            viewAllFoods();
            
            String foodId = getStringInput("Enter food ID to add (first 4 characters): ");
            Food selectedFood = null;
            
            // Find the food with the matching ID prefix
            for (Food food : foodDbService.getAllFoods()) {
                if (food.getId().startsWith(foodId)) {
                    selectedFood = food;
                    break;
                }
            }
            
            if (selectedFood == null) {
                System.out.println("Food not found. Please try again.");
                continue;
            }
            
            double servings = getDoubleInput("Enter number of servings: ");
            components.add(new FoodComponent(selectedFood, servings));
            
            String addMore = getStringInput("Add another component? (y/n): ");
            addMoreComponents = addMore.equalsIgnoreCase("y");
        }
        
        Food compositeFood = foodDbService.addCompositeFood(name, keywords, components);
        System.out.println("Composite food added successfully: " + compositeFood.getName());
    }
    
    private void deleteFood() {
        System.out.println("\n===== DELETE FOOD =====");
        viewAllFoods();
        
        String foodId = getStringInput("Enter food ID to delete (first 4 characters): ");
        
        // Find the food with the matching ID prefix
        Food foodToDelete = null;
        for (Food food : foodDbService.getAllFoods()) {
            if (food.getId().startsWith(foodId)) {
                foodToDelete = food;
                break;
            }
        }
        
        if (foodToDelete == null) {
            System.out.println("Food not found. Please try again.");
            return;
        }
        
        String confirm = getStringInput("Are you sure you want to delete " + foodToDelete.getName() + "? (y/n): ");
        if (confirm.equalsIgnoreCase("y")) {
            boolean deleted = foodDbService.deleteFood(foodToDelete.getId());
            if (deleted) {
                System.out.println("Food deleted successfully.");
            } else {
                System.out.println("Failed to delete food.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
    
    private void manageDailyLog() {
        boolean back = false;
        while (!back) {
            System.out.println("\n===== DAILY LOG MANAGEMENT (" + currentDate.format(dateFormatter) + ") =====");
            System.out.println("1. View Today's Log");
            System.out.println("2. Add Food to Log");
            System.out.println("3. Delete Entry from Log");
            System.out.println("4. Undo Last Action");
            System.out.println("0. Back to Main Menu");
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    viewDailyLog();
                    break;
                case 2:
                    addFoodToLog();
                    break;
                case 3:
                    deleteEntryFromLog();
                    break;
                case 4:
                    undoLastAction();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void viewDailyLog() {
        List<FoodLogEntry> entries = logService.getEntriesByDate(currentDate);
        
        if (entries.isEmpty()) {
            System.out.println("No entries found for " + currentDate.format(dateFormatter));
            return;
        }
        
        System.out.println("\n===== FOOD LOG FOR " + currentDate.format(dateFormatter) + " =====");
        System.out.printf("%-5s %-10s %-15s %-30s %-10s %-10s\n", 
                "ID", "Time", "Meal Type", "Food", "Servings", "Calories");
        System.out.println("-".repeat(85));
        
        double totalCalories = 0;
        
        for (FoodLogEntry entry : entries) {
            System.out.printf("%-5s %-10s %-15s %-30s %-10.1f %-10.1f\n",
                    entry.getId().substring(0, 4),
                    entry.getTime().format(timeFormatter),
                    entry.getMealType(),
                    entry.getFood().getName(),
                    entry.getServings(),
                    entry.getTotalCalories());
            
            totalCalories += entry.getTotalCalories();
        }
        
        System.out.println("-".repeat(85));
        System.out.printf("Total Calories: %.1f\n", totalCalories);
        
        // Calculate remaining calories
        double targetCalories = userProfile.calculateDailyCalorieNeeds();
        double remainingCalories = targetCalories - totalCalories;
        
        System.out.printf("Target Calories: %.1f\n", targetCalories);
        System.out.printf("Remaining Calories: %.1f\n", remainingCalories);
    }
    
    private void addFoodToLog() {
        System.out.println("\n===== ADD FOOD TO LOG =====");
        
        // Show available foods or search
        System.out.println("1. View All Foods");
        System.out.println("2. Search Foods");
        int foodChoice = getIntInput("Enter your choice: ");
        
        if (foodChoice == 1) {
            viewAllFoods();
        } else if (foodChoice == 2) {
            searchFoods();
        } else {
            System.out.println("Invalid choice. Returning to menu.");
            return;
        }
        
        String foodId = getStringInput("Enter food ID to add (first 4 characters): ");
        
        // Find the food with the matching ID prefix
        Food selectedFood = null;
        for (Food food : foodDbService.getAllFoods()) {
            if (food.getId().startsWith(foodId)) {
                selectedFood = food;
                break;
            }
        }
        
        if (selectedFood == null) {
            System.out.println("Food not found. Please try again.");
            return;
        }
        
        double servings = getDoubleInput("Enter number of servings: ");
        
        // Get meal type
        System.out.println("Select meal type:");
        System.out.println("1. Breakfast");
        System.out.println("2. Lunch");
        System.out.println("3. Dinner");
        System.out.println("4. Snack");
        
        int mealChoice = getIntInput("Enter your choice: ");
        String mealType;
        
        switch (mealChoice) {
            case 1:
                mealType = "Breakfast";
                break;
            case 2:
                mealType = "Lunch";
                break;
            case 3:
                mealType = "Dinner";
                break;
            case 4:
                mealType = "Snack";
                break;
            default:
                System.out.println("Invalid choice. Using 'Other' as meal type.");
                mealType = "Other";
        }
        
        // Get time
        LocalTime time = LocalTime.now();
        String timeInput = getStringInput("Enter time (HH:mm) or press Enter for current time: ");
        if (!timeInput.isEmpty()) {
            try {
                time = LocalTime.parse(timeInput, timeFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid time format. Using current time.");
            }
        }
        
        // Add entry
        FoodLogEntry entry = logService.addEntry(currentDate, time, mealType, selectedFood, servings);
        System.out.println("Food added to log successfully.");
    }
    
    private void deleteEntryFromLog() {
        List<FoodLogEntry> entries = logService.getEntriesByDate(currentDate);
        
        if (entries.isEmpty()) {
            System.out.println("No entries found for " + currentDate.format(dateFormatter));
            return;
        }
        
        System.out.println("\n===== DELETE ENTRY FROM LOG =====");
        System.out.printf("%-5s %-10s %-15s %-30s %-10s %-10s\n", 
                "ID", "Time", "Meal Type", "Food", "Servings", "Calories");
        System.out.println("-".repeat(85));
        
        for (FoodLogEntry entry : entries) {
            System.out.printf("%-5s %-10s %-15s %-30s %-10.1f %-10.1f\n",
                    entry.getId().substring(0, 4),
                    entry.getTime().format(timeFormatter),
                    entry.getMealType(),
                    entry.getFood().getName(),
                    entry.getServings(),
                    entry.getTotalCalories());
        }
        
        String entryId = getStringInput("Enter entry ID to delete (first 4 characters): ");
        
        // Find the entry with the matching ID prefix
        String fullEntryId = null;
        for (FoodLogEntry entry : entries) {
            if (entry.getId().startsWith(entryId)) {
                fullEntryId = entry.getId();
                break;
            }
        }
        
        if (fullEntryId == null) {
            System.out.println("Entry not found. Please try again.");
            return;
        }
        
        boolean deleted = logService.deleteEntry(fullEntryId);
        if (deleted) {
            System.out.println("Entry deleted successfully.");
        } else {
            System.out.println("Failed to delete entry.");
        }
    }
    
    private void undoLastAction() {
        if (logService.canUndo()) {
            boolean undone = logService.undo();
            if (undone) {
                System.out.println("Last action undone successfully.");
            } else {
                System.out.println("Failed to undo last action.");
            }
        } else {
            System.out.println("No actions to undo.");
        }
    }
    
    private void manageUserProfile() {
        boolean back = false;
        while (!back) {
            System.out.println("\n===== USER PROFILE MANAGEMENT =====");
            System.out.println("1. View Profile");
            System.out.println("2. Update Profile");
            System.out.println("3. Change Calorie Calculation Method");
            System.out.println("0. Back to Main Menu");
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    viewUserProfile();
                    break;
                case 2:
                    updateUserProfile();
                    break;
                case 3:
                    changeCalorieCalculationMethod();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void viewUserProfile() {
        System.out.println("\n===== USER PROFILE =====");
        System.out.println("Name: " + userProfile.getName());
        System.out.println("Sex: " + userProfile.getSex());
        System.out.println("Age: " + userProfile.getAge());
        System.out.println("Height: " + userProfile.getHeight() + " cm");
        System.out.println("Weight: " + userProfile.getWeight() + " kg");
        System.out.println("Activity Level (0-10): " + userProfile.getActivityLevel());
        System.out.println("Calorie Calculation Method: " + userProfile.getCalorieCalculationMethod());
        System.out.println("Last Updated: " + userProfile.getLastUpdated().format(dateFormatter));
        System.out.println("Daily Calorie Needs: " + String.format("%.1f", userProfile.calculateDailyCalorieNeeds()));
    }
    
    private void updateUserProfile() {
        System.out.println("\n===== UPDATE USER PROFILE =====");
        
        String name = getStringInput("Enter name (current: " + userProfile.getName() + "): ");
        if (!name.isEmpty()) {
            userProfile.setName(name);
        }
        
        String sex = getStringInput("Enter sex (Male/Female) (current: " + userProfile.getSex() + "): ");
        if (!sex.isEmpty()) {
            userProfile.setSex(sex);
        }
        
        String ageStr = getStringInput("Enter age (current: " + userProfile.getAge() + "): ");
        if (!ageStr.isEmpty()) {
            try {
                int age = Integer.parseInt(ageStr);
                userProfile.setAge(age);
            } catch (NumberFormatException e) {
                System.out.println("Invalid age format. Keeping current value.");
            }
        }
        
        String heightStr = getStringInput("Enter height in cm (current: " + userProfile.getHeight() + "): ");
        if (!heightStr.isEmpty()) {
            try {
                double height = Double.parseDouble(heightStr);
                userProfile.setHeight(height);
            } catch (NumberFormatException e) {
                System.out.println("Invalid height format. Keeping current value.");
            }
        }
        
        String weightStr = getStringInput("Enter weight in kg (current: " + userProfile.getWeight() + "): ");
        if (!weightStr.isEmpty()) {
            try {
                double weight = Double.parseDouble(weightStr);
                userProfile.setWeight(weight);
            } catch (NumberFormatException e) {
                System.out.println("Invalid weight format. Keeping current value.");
            }
        }
        
        String activityLevelStr = getStringInput("Enter activity level (0-10) (current: " + userProfile.getActivityLevel() + "): ");
        if (!activityLevelStr.isEmpty()) {
            try {
                int activityLevel = Integer.parseInt(activityLevelStr);
                if (activityLevel >= 0 && activityLevel <= 10) {
                    userProfile.setActivityLevel(activityLevel);
                } else {
                    System.out.println("Activity level must be between 0 and 10. Keeping current value.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid activity level format. Keeping current value.");
            }
        }
        
        userProfile.setLastUpdated(LocalDate.now());
        System.out.println("User profile updated successfully.");
    }
    
    private void changeCalorieCalculationMethod() {
        System.out.println("\n===== CHANGE CALORIE CALCULATION METHOD =====");
        System.out.println("Current method: " + userProfile.getCalorieCalculationMethod());
        System.out.println("Available methods:");
        System.out.println("1. Harris-Benedict");
        System.out.println("2. Mifflin-St Jeor");
        
        int choice = getIntInput("Enter your choice: ");
        
        switch (choice) {
            case 1:
                userProfile.setCalorieCalculationMethod("Harris-Benedict");
                System.out.println("Calorie calculation method changed to Harris-Benedict.");
                break;
            case 2:
                userProfile.setCalorieCalculationMethod("Mifflin-St Jeor");
                System.out.println("Calorie calculation method changed to Mifflin-St Jeor.");
                break;
            default:
                System.out.println("Invalid choice. Keeping current method.");
        }
    }
    
    private void viewCaloriesSummary() {
        System.out.println("\n===== CALORIES SUMMARY =====");
        
        // Display today's calories
        double todaysCalories = logService.calculateTotalCaloriesForDate(currentDate);
        double targetCalories = userProfile.calculateDailyCalorieNeeds();
        double remainingCalories = targetCalories - todaysCalories;
        
        System.out.println("Date: " + currentDate.format(dateFormatter));
        System.out.println("Calories Consumed: " + String.format("%.1f", todaysCalories));
        System.out.println("Target Calories: " + String.format("%.1f", targetCalories));
        System.out.println("Remaining Calories: " + String.format("%.1f", remainingCalories));
        
        // Show breakdown by meal type
        System.out.println("\nBreakdown by Meal Type:");
        System.out.printf("%-15s %-10s\n", "Meal Type", "Calories");
        System.out.println("-".repeat(25));
        
        String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack", "Other"};
        for (String mealType : mealTypes) {
            double calories = logService.calculateCaloriesByMealType(currentDate, mealType);
            if (calories > 0) {
                System.out.printf("%-15s %-10.1f\n", mealType, calories);
            }
        }
        
        // Display weekly summary if desired
        System.out.println("\nWould you like to see a weekly summary? (y/n)");
        String choice = scanner.nextLine();
        
        if (choice.equalsIgnoreCase("y")) {
            LocalDate startDate = currentDate.minusDays(6);
            Map<LocalDate, Double> dailyCalories = logService.getDailyCalorieSummary();
            
            System.out.println("\n===== WEEKLY CALORIES SUMMARY =====");
            System.out.printf("%-15s %-15s %-15s %-15s\n", "Date", "Consumed", "Target", "Difference");
            System.out.println("-".repeat(60));
            
            for (int i = 0; i <= 6; i++) {
                LocalDate date = startDate.plusDays(i);
                double consumed = dailyCalories.getOrDefault(date, 0.0);
                double target = targetCalories; // Using current target for simplicity
                double difference = target - consumed;
                
                System.out.printf("%-15s %-15.1f %-15.1f %-15.1f\n", 
                        date.format(dateFormatter), consumed, target, difference);
            }
        }
    }
    
    private void changeCurrentDate() {
        System.out.println("\n===== CHANGE CURRENT DATE =====");
        System.out.println("Current date: " + currentDate.format(dateFormatter));
        
        String dateInput = getStringInput("Enter new date (yyyy-MM-dd) or press Enter to cancel: ");
        if (!dateInput.isEmpty()) {
            try {
                LocalDate newDate = LocalDate.parse(dateInput, dateFormatter);
                currentDate = newDate;
                System.out.println("Current date changed to: " + currentDate.format(dateFormatter));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Current date remains unchanged.");
            }
        } else {
            System.out.println("Date change cancelled.");
        }
    }
    
    private void saveData() {
        foodDbService.saveDatabase();
        logService.saveLog();
        System.out.println("All data saved successfully.");
    }
    
    // Helper methods for input
    private int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
    
    private double getDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
    
    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
    
    public static void main(String[] args) {
        DietManagerCLI cli = new DietManagerCLI();
        cli.start();
    }
}
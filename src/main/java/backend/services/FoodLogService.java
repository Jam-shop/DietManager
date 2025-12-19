package backend.services;

import backend.models.Food;
import backend.models.FoodLogEntry;
import backend.models.UserProfile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class FoodLogService {
    private static final String FOOD_LOG_FILE = "food_log.json";
    private List<FoodLogEntry> logEntries;
    private FoodDatabaseService foodDatabaseService;
    private Stack<Command> undoStack;
    
    public FoodLogService(FoodDatabaseService foodDatabaseService) {
        this.foodDatabaseService = foodDatabaseService;
        this.logEntries = new ArrayList<>();
        this.undoStack = new Stack<>();
        loadLog();
    }
    
    // Load log from file
    private void loadLog() {
        File file = new File(FOOD_LOG_FILE);
        if (!file.exists()) {
            return;
        }
        
        try (FileReader reader = new FileReader(file)) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int bytesRead;
            
            while ((bytesRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, bytesRead);
            }
            
            JSONArray jsonArray = new JSONArray(content.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject entryJson = jsonArray.getJSONObject(i);
                
                String id = entryJson.getString("id");
                LocalDate date = LocalDate.parse(entryJson.getString("date"));
                LocalTime time = LocalTime.parse(entryJson.getString("time"));
                String mealType = entryJson.getString("mealType");
                String foodId = entryJson.getString("foodId");
                double servings = entryJson.getDouble("servings");
                
                Food food = foodDatabaseService.getFoodById(foodId);
                if (food != null) {
                    FoodLogEntry entry = new FoodLogEntry(id, date, time, mealType, food, servings);
                    logEntries.add(entry);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading food log: " + e.getMessage());
        }
    }
    
    // Save log to file
    public void saveLog() {
        try (FileWriter writer = new FileWriter(new File(FOOD_LOG_FILE))) {
            JSONArray jsonArray = new JSONArray();
            
            for (FoodLogEntry entry : logEntries) {
                JSONObject entryJson = new JSONObject();
                entryJson.put("id", entry.getId());
                entryJson.put("date", entry.getDate().toString());
                entryJson.put("time", entry.getTime().toString());
                entryJson.put("mealType", entry.getMealType());
                entryJson.put("foodId", entry.getFood().getId());
                entryJson.put("servings", entry.getServings());
                
                jsonArray.put(entryJson);
            }
            
            writer.write(jsonArray.toString(4)); // Pretty print with 4-space indentation
        } catch (IOException e) {
            System.err.println("Error saving food log: " + e.getMessage());
        }
    }
    
    // Add a new entry to the log
    public FoodLogEntry addEntry(LocalDate date, LocalTime time, String mealType, Food food, double servings) {
        FoodLogEntry entry = new FoodLogEntry(date, time, mealType, food, servings);
        logEntries.add(entry);
        
        // Add to undo stack
        undoStack.push(new Command(CommandType.ADD, entry));
        
        return entry;
    }
    
    // Delete an entry from the log
    public boolean deleteEntry(String id) {
        FoodLogEntry entryToRemove = null;
        for (FoodLogEntry entry : logEntries) {
            if (entry.getId().equals(id)) {
                entryToRemove = entry;
                break;
            }
        }
        
        if (entryToRemove != null) {
            logEntries.remove(entryToRemove);
            
            // Add to undo stack
            undoStack.push(new Command(CommandType.DELETE, entryToRemove));
            
            return true;
        }
        
        return false;
    }
    
    // Update an entry in the log
    public boolean updateEntry(String id, LocalDate date, LocalTime time, String mealType, Food food, double servings) {
        for (int i = 0; i < logEntries.size(); i++) {
            FoodLogEntry entry = logEntries.get(i);
            if (entry.getId().equals(id)) {
                // Save the old entry for undo
                FoodLogEntry oldEntry = new FoodLogEntry(
                        entry.getId(), entry.getDate(), entry.getTime(), 
                        entry.getMealType(), entry.getFood(), entry.getServings()
                );
                
                // Update the entry
                entry.setDate(date);
                entry.setTime(time);
                entry.setMealType(mealType);
                entry.setFood(food);
                entry.setServings(servings);
                
                // Add to undo stack
                undoStack.push(new Command(CommandType.UPDATE, oldEntry, entry));
                
                return true;
            }
        }
        
        return false;
    }
    
    // Get all entries for a specific date
    public List<FoodLogEntry> getEntriesByDate(LocalDate date) {
        return logEntries.stream()
                .filter(entry -> entry.getDate().equals(date))
                .collect(Collectors.toList());
    }
    
    // Get all entries
    public List<FoodLogEntry> getAllEntries() {
        return new ArrayList<>(logEntries);
    }
    
    // Calculate total calories consumed on a specific date
    public double calculateTotalCaloriesForDate(LocalDate date) {
        return getEntriesByDate(date).stream()
                .mapToDouble(FoodLogEntry::getTotalCalories)
                .sum();
    }
    
    // Get daily calorie summary for all logged dates
    public Map<LocalDate, Double> getDailyCalorieSummary() {
        Map<LocalDate, Double> summary = new HashMap<>();
        
        for (FoodLogEntry entry : logEntries) {
            LocalDate date = entry.getDate();
            double calories = entry.getTotalCalories();
            
            summary.put(date, summary.getOrDefault(date, 0.0) + calories);
        }
        
        return summary;
    }
    
    // Undo the last command
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        
        Command command = undoStack.pop();
        
        switch (command.getType()) {
            case ADD:
                logEntries.removeIf(entry -> entry.getId().equals(command.getOldEntry().getId()));
                break;
                
            case DELETE:
                logEntries.add(command.getOldEntry());
                break;
                
            case UPDATE:
                for (int i = 0; i < logEntries.size(); i++) {
                    if (logEntries.get(i).getId().equals(command.getNewEntry().getId())) {
                        logEntries.set(i, command.getOldEntry());
                        break;
                    }
                }
                break;
        }
        
        return true;
    }
    
    // Get entries by meal type for a specific date
    public List<FoodLogEntry> getEntriesByMealType(LocalDate date, String mealType) {
        return getEntriesByDate(date).stream()
                .filter(entry -> entry.getMealType().equalsIgnoreCase(mealType))
                .collect(Collectors.toList());
    }
    
    // Calculate total calories by meal type for a specific date
    public double calculateCaloriesByMealType(LocalDate date, String mealType) {
        return getEntriesByMealType(date, mealType).stream()
                .mapToDouble(FoodLogEntry::getTotalCalories)
                .sum();
    }
    
    // Clear undo stack
    public void clearUndoStack() {
        undoStack.clear();
    }
    
    // Check if undo is available
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    // Command class for undo functionality
    private static class Command {
        private CommandType type;
        private FoodLogEntry oldEntry;
        private FoodLogEntry newEntry;
        
        public Command(CommandType type, FoodLogEntry entry) {
            this.type = type;
            this.oldEntry = entry;
        }
        
        public Command(CommandType type, FoodLogEntry oldEntry, FoodLogEntry newEntry) {
            this.type = type;
            this.oldEntry = oldEntry;
            this.newEntry = newEntry;
        }
        
        public CommandType getType() {
            return type;
        }
        
        public FoodLogEntry getOldEntry() {
            return oldEntry;
        }
        
        public FoodLogEntry getNewEntry() {
            return newEntry;
        }
    }
    
    // Command type enum
    private enum CommandType {
        ADD,
        DELETE,
        UPDATE
    }
}
package backend.services;

import backend.models.Food;
import backend.models.BasicFood;
import backend.models.CompositeFood;
import backend.models.FoodComponent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FoodDatabaseService {
    private static final String FOOD_DB_FILE = "food_database.json";
    private List<Food> foods;

    public FoodDatabaseService() {
        foods = new ArrayList<>();
        loadDatabase();
    }

    private void loadDatabase() {
        File file = new File(FOOD_DB_FILE);

        try (FileReader reader = new FileReader(file)) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int bytesRead;

            while ((bytesRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, bytesRead);
            }

            JSONArray jsonArray = new JSONArray(content.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject foodJson = jsonArray.getJSONObject(i);

                String id = foodJson.getString("id");
                String name = foodJson.getString("name");

                JSONArray keywordsJson = foodJson.getJSONArray("keywords");
                List<String> keywords = new ArrayList<>();
                for (int j = 0; j < keywordsJson.length(); j++) {
                    keywords.add(keywordsJson.getString(j));
                }

                double calories = foodJson.getDouble("caloriesPerServing");
                boolean isComposite = foodJson.getBoolean("isComposite");

                if (isComposite) {
                    JSONArray componentsJson = foodJson.getJSONArray("components");
                    CompositeFood compositeFood = new CompositeFood(name, keywords, new ArrayList<>());
                    compositeFood.setId(id);
                    foods.add(compositeFood);

                    for (int j = 0; j < componentsJson.length(); j++) {
                        JSONObject componentJson = componentsJson.getJSONObject(j);
                        String foodId = componentJson.getString("foodId");
                        double servings = componentJson.getDouble("servings");

                        componentJson.put("_tempId", foodId);
                        componentJson.put("_tempServings", servings);
                    }
                } else {
                    BasicFood basicFood = new BasicFood(name, keywords, calories);
                    basicFood.setId(id);
                    foods.add(basicFood);
                }
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject foodJson = jsonArray.getJSONObject(i);
                if (foodJson.getBoolean("isComposite")) {
                    String id = foodJson.getString("id");
                    Food compositeFood = getFoodById(id);

                    if (compositeFood instanceof CompositeFood) {
                        JSONArray componentsJson = foodJson.getJSONArray("components");
                        List<FoodComponent> components = new ArrayList<>();

                        for (int j = 0; j < componentsJson.length(); j++) {
                            JSONObject componentJson = componentsJson.getJSONObject(j);
                            String foodId = componentJson.getString("_tempId");
                            double servings = componentJson.getDouble("_tempServings");

                            Food componentFood = getFoodById(foodId);
                            if (componentFood != null) {
                                components.add(new FoodComponent(componentFood, servings));
                            }
                        }

                        ((CompositeFood) compositeFood).setComponents(components);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading food database: " + e.getMessage());
            createInitialDatabase();
        }
    }

    public void saveDatabase() {
        try (FileWriter writer = new FileWriter(new File(FOOD_DB_FILE))) {
            JSONArray jsonArray = new JSONArray();

            for (Food food : foods) {
                JSONObject foodJson = new JSONObject();
                foodJson.put("id", food.getId());
                foodJson.put("name", food.getName());

                JSONArray keywordsJson = new JSONArray();
                for (String keyword : food.getKeywords()) {
                    keywordsJson.put(keyword);
                }
                foodJson.put("keywords", keywordsJson);

                foodJson.put("caloriesPerServing", food.getCaloriesPerServing());
                foodJson.put("isComposite", food.isComposite());

                if (food.isComposite() && food instanceof CompositeFood) {
                    JSONArray componentsJson = new JSONArray();
                    for (FoodComponent component : ((CompositeFood) food).getComponents()) {
                        JSONObject componentJson = new JSONObject();
                        componentJson.put("foodId", component.getFood().getId());
                        componentJson.put("servings", component.getServings());
                        componentsJson.put(componentJson);
                    }
                    foodJson.put("components", componentsJson);
                }

                jsonArray.put(foodJson);
            }

            writer.write(jsonArray.toString(4));
        } catch (IOException e) {
            System.err.println("Error saving food database: " + e.getMessage());
        }
    }

    public Food addBasicFood(String name, List<String> keywords, double caloriesPerServing) {
        Food food = new BasicFood(name, keywords, caloriesPerServing);
        foods.add(food);
        return food;
    }

    public Food addCompositeFood(String name, List<String> keywords, List<FoodComponent> components) {
        Food food = new CompositeFood(name, keywords, components);
        foods.add(food);
        return food;
    }

    public List<Food> getAllFoods() {
        return new ArrayList<>(foods);
    }

    public Food getFoodById(String id) {
        for (Food food : foods) {
            if (food.getId().equals(id)) {
                return food;
            }
        }
        return null;
    }

    public Food getFoodByName(String name) {
        for (Food food : foods) {
            if (food.getName().equalsIgnoreCase(name)) {
                return food;
            }
        }
        return null;
    }

    public List<Food> searchFoodsByAllKeywords(List<String> keywords) {
        return foods.stream()
                .filter(food -> food.matchesAllKeywords(keywords))
                .collect(Collectors.toList());
    }

    public List<Food> searchFoodsByAnyKeyword(List<String> keywords) {
        return foods.stream()
                .filter(food -> food.matchesAnyKeyword(keywords))
                .collect(Collectors.toList());
    }

    public boolean deleteFood(String id) {
        return foods.removeIf(food -> food.getId().equals(id));
    }
}

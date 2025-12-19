package backend.models;

import java.util.List;

public class BasicFood extends Food {
    private double caloriesPerServing;

    public BasicFood(String name, List<String> keywords, double caloriesPerServing) {
        super(name, keywords);
        this.caloriesPerServing = caloriesPerServing;
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public double getCaloriesPerServing() {
        return caloriesPerServing;
    }
}

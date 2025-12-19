package backend.models;

import java.util.List;

public class CompositeFood extends Food {
    private List<FoodComponent> components;

    public CompositeFood(String name, List<String> keywords, List<FoodComponent> components) {
        super(name, keywords);
        this.components = components;
    }

    public List<FoodComponent> getComponents() {
        return components;
    }

    public void setComponents(List<FoodComponent> components) {
        this.components = components;
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public double getCaloriesPerServing() {
        return components.stream()
                .mapToDouble(c -> c.getFood().getCaloriesPerServing() * c.getServings())
                .sum();
    }
}

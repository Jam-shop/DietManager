package backend.models;

import java.util.List;
import java.util.UUID;

public abstract class Food {
    protected String id;
    protected String name;
    protected List<String> keywords;

    public Food(String name, List<String> keywords) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.keywords = keywords;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public boolean matchesAllKeywords(List<String> queryKeywords) {
        return keywords.containsAll(queryKeywords);
    }

    public boolean matchesAnyKeyword(List<String> queryKeywords) {
        for (String keyword : queryKeywords) {
            if (keywords.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public abstract boolean isComposite();

    public abstract double getCaloriesPerServing();
}

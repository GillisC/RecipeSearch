package recipesearch;

import se.chalmers.ait.dat215.lab2.Recipe;
import se.chalmers.ait.dat215.lab2.RecipeDatabase;
import se.chalmers.ait.dat215.lab2.SearchFilter;

import java.util.List;

public class RecipeBackendController {
    private String cuisine;
    private String mainIngredient;
    private String difficulty;
    private int maxPrice;
    private int maxTime;

    RecipeDatabase db = RecipeDatabase.getSharedInstance();

    public List<Recipe> getRecipes() {
        return db.search(new SearchFilter(this.difficulty, this.maxTime, this.cuisine, this.maxPrice, this.mainIngredient));
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }
    public void setMainIngredient(String mainIngredient) {
        this.mainIngredient = mainIngredient;
    }
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    public void setMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }
    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }
}

package recipesearch;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import se.chalmers.ait.dat215.lab2.Recipe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RecipeListItem extends AnchorPane {

    private RecipeSearchController parentController;
    private Recipe recipe;

    @FXML private Label recipeListTitleLabel;
    @FXML private ImageView recipeListImageView;
    @FXML private ImageView recipeListItemMainImageView;
    @FXML private ImageView recipeListItemDifficultyImageView;
    @FXML private Label recipeListItemTimeLabel;
    @FXML private Label recipeListPriceLabel;
    @FXML private Label recipeListItemDescriptionLabel;


    public RecipeListItem(Recipe recipe, RecipeSearchController recipeSearchController) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("recipe_listitem.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.recipe = recipe;
        this.parentController = recipeSearchController;

        Image recipeImage = parentController.getSquareImage(recipe.getFXImage());
        this.recipeListImageView.setImage(recipeImage);

        this.recipeListTitleLabel.setText(recipe.getName());

        Image ingredientImage = parentController.getIngredientImage(recipe.getMainIngredient());
        this.recipeListItemMainImageView.setImage(ingredientImage);

        Image difficultyImage = parentController.getDifficultyImage(recipe.getDifficulty());
        this.recipeListItemDifficultyImageView.setImage(difficultyImage);

        this.recipeListItemTimeLabel.setText(String.format("%s minuter", recipe.getTime()));

        this.recipeListPriceLabel.setText(String.format("%s kr", recipe.getPrice()));

        this.recipeListItemDescriptionLabel.setText(recipe.getDescription());

    }

    @FXML
    protected void onClick(Event event) {
        parentController.openRecipeView(recipe);
    }
}


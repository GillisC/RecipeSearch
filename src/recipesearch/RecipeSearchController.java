
package recipesearch;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import se.chalmers.ait.dat215.lab2.Recipe;
import se.chalmers.ait.dat215.lab2.RecipeDatabase;


public class RecipeSearchController implements Initializable {

    private RecipeDatabase db = RecipeDatabase.getSharedInstance();
    private Map<String, RecipeListItem> recipeListItemMap = new HashMap<>();
    private RecipeBackendController recipeController;
    private ToggleGroup toggleGroup;

    @FXML private FlowPane recipeListFlowPane;
    @FXML private ComboBox<String> mainIngredientComboBox;
    @FXML private ComboBox<String> cuisineComboBox;
    @FXML private RadioButton difficultyAllRadioButton;
    @FXML private RadioButton difficultyEasyRadioButton;
    @FXML private RadioButton difficultyMediumRadioButton;
    @FXML private RadioButton difficultyHardRadioButton;
    @FXML private Spinner<Integer> maxPriceSpinner;
    @FXML private Slider maxTimeSlider;
    @FXML private Label maxTimeSliderLabel;

    @FXML private AnchorPane detailedViewAnchorPane;
    @FXML private StackPane searchViewStackPane;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        recipeController = new RecipeBackendController();

        for (Recipe r: recipeController.getRecipes()) {
            RecipeListItem recipeListItem = new RecipeListItem(r, this);
            recipeListItemMap.put(r.getName(), recipeListItem);
        }

        /* Initiate main ingredient menu */
        mainIngredientComboBox.getItems().addAll("Kött", "Fisk", "Kyckling", "Vegetariskt");
        mainIngredientComboBox.getSelectionModel().select("Välj huvudingrediens");

        mainIngredientComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            recipeController.setMainIngredient(newValue);
            updateRecipeList();
        });

        /* Initiate cuisine selection menu */
        cuisineComboBox.getItems().addAll("Sverige", "Grekland", "Indien", "Asien", "Afrika", "Frankrike");
        cuisineComboBox.getSelectionModel().select("Välj kök");

        cuisineComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            recipeController.setCuisine(newValue);
            updateRecipeList();
        });

        /* Initiate difficulty buttons */
        toggleGroup = new ToggleGroup();
        difficultyAllRadioButton.setToggleGroup(toggleGroup);
        difficultyEasyRadioButton.setToggleGroup(toggleGroup);
        difficultyMediumRadioButton.setToggleGroup(toggleGroup);
        difficultyHardRadioButton.setToggleGroup(toggleGroup);
        difficultyAllRadioButton.setSelected(true);

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {

            if (toggleGroup.getSelectedToggle() != null) {
                RadioButton selected = (RadioButton) toggleGroup.getSelectedToggle();
                recipeController.setDifficulty(selected.getText());
                updateRecipeList();
            }
        });

        /* Initiate max price spinner */
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 10000, 50, 10);
        maxPriceSpinner.setValueFactory(valueFactory);

        maxPriceSpinner.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            recipeController.setMaxPrice(newValue);
            updateRecipeList();
        });

        maxPriceSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue){
                //focusgained - do nothing
            }
            else{
                try {
                    int value = Integer.parseInt(maxPriceSpinner.getEditor().getText());
                    recipeController.setMaxPrice(value);
                    updateRecipeList();
                } catch (NumberFormatException e) {
                    maxPriceSpinner.getValueFactory().setValue(10);
                    recipeController.setMaxPrice(10);
                    updateRecipeList();
                }
            }

        });

        maxTimeSlider.setMin(10);
        maxTimeSlider.setMax(150);
        maxTimeSlider.setMinorTickCount(0);
        maxTimeSlider.setMajorTickUnit(10);
        maxTimeSlider.setShowTickMarks(false);
        maxTimeSlider.setSnapToTicks(true);

        /* Checks continuously for updates to the slider */
        maxTimeSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            String output = String.format("%s minutes", newValue.intValue());
            maxTimeSliderLabel.textProperty().setValue(output);

            if(!newValue.equals(oldValue) && !maxTimeSlider.isValueChanging()) {
                recipeController.setMaxTime(newValue.intValue());
                updateRecipeList();
            }
        });

        updateRecipeList();

    }

    private void updateRecipeList() {
        recipeListFlowPane.getChildren().clear();
        for (Recipe r: recipeController.getRecipes()) {
            RecipeListItem listItem;
            listItem = recipeListItemMap.get(r.getName());
            recipeListFlowPane.getChildren().add(listItem);
        }

    }
    @FXML
    public void closeRecipeView() {
        searchViewStackPane.toFront();
    }

    public void openRecipeView(Recipe recipe) {
        //populateRecipeDetailView(recipe);
        detailedViewAnchorPane.toFront();
    }

}
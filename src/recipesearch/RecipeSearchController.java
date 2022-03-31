
package recipesearch;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import se.chalmers.ait.dat215.lab2.Ingredient;
import se.chalmers.ait.dat215.lab2.Recipe;
import se.chalmers.ait.dat215.lab2.RecipeDatabase;


public class RecipeSearchController implements Initializable {

    private RecipeDatabase db = RecipeDatabase.getSharedInstance();
    private Map<String, RecipeListItem> recipeListItemMap = new HashMap<>();
    private RecipeBackendController recipeController;
    private ToggleGroup toggleGroup;

    @FXML private StackPane searchViewStackPane;
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

    @FXML private AnchorPane detailedViewPane;
    @FXML private ImageView detailedImage;
    @FXML private Label detailedTitleLabel;
    @FXML private Label detailedTimeLabel;
    @FXML private Label detailedPriceLabel;
    @FXML private Label detailedPortionLabel;
    @FXML private ImageView detailedMainIngredientImageView;
    @FXML private ImageView detailedDifficultyImageView;
    @FXML private ImageView detailedCuisineImageView;
    @FXML private TextArea detailedDescriptionTextArea;
    @FXML private TextArea detailedCookingDescriptionTextArea;
    @FXML private TextArea detailedIngredientsTextArea;
    @FXML private ImageView detailedViewCloseButton;




    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Platform.runLater(()->mainIngredientComboBox.requestFocus());

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

        populateMainIngredientComboBox();

        /* Initiate cuisine selection menu */
        cuisineComboBox.getItems().addAll("Sverige", "Grekland", "Indien", "Asien", "Afrika", "Frankrike");
        cuisineComboBox.getSelectionModel().select("Välj kök");

        cuisineComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            recipeController.setCuisine(newValue);
            updateRecipeList();
        });

        populateMainCuisineComboBox();

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
                recipeController.setDifficulty(selected.getAccessibleText());
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
        detailedViewPane.setStyle("-fx-background-color: rgba(0,0,0,0.75)");
        populateRecipeDetailView(recipe);
        detailedViewPane.toFront();
    }

    public void populateRecipeDetailView(Recipe recipe) {
        Image image = getSquareImage(recipe.getFXImage());
        detailedImage.setImage(image);

        detailedTitleLabel.setText(recipe.getName());
        detailedMainIngredientImageView.setImage(getIngredientImage(recipe.getMainIngredient()));
        detailedDifficultyImageView.setImage(getDifficultyImage(recipe.getDifficulty()));
        detailedTimeLabel.setText(String.format("%s minuter", recipe.getTime()));
        detailedPriceLabel.setText(String.format("%s kr", recipe.getPrice()));
        detailedCuisineImageView.setImage(getCuisineImage(recipe.getCuisine()));

        detailedDescriptionTextArea.setText(recipe.getDescription());
        detailedCookingDescriptionTextArea.setText(recipe.getInstruction());
        detailedPortionLabel.setText(String.format("%s portioner", recipe.getServings()));

        String s = "";
        for (Ingredient i: recipe.getIngredients()) {
            s = s.concat(i.getAmount() + " " + i.getUnit() + " " + i.getName() + "\n");
        }
        detailedIngredientsTextArea.setText(s);



    }

    private void populateMainIngredientComboBox() {
        Callback<ListView<String>, ListCell<String>> cellFactoryMain = new Callback<ListView<String>, ListCell<String>>() {

            @Override
            public ListCell<String> call(ListView<String> p) {

                return new ListCell<String>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(item);

                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            Image icon = null;
                            String iconPath;
                            try {
                                switch (item) {

                                    case "Kött":
                                        iconPath = "RecipeSearch/resources/icon_main_meat.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Fisk":
                                        iconPath = "RecipeSearch/resources/icon_main_fish.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Kyckling":
                                        iconPath = "RecipeSearch/resources/icon_main_chicken.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Vegetariskt":
                                        iconPath = "RecipeSearch/resources/icon_main_veg.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                }
                            } catch (NullPointerException ex) {
                                //This should never happen in this lab but could load a default image in case of a NullPointer
                            }
                            ImageView iconImageView = new ImageView(icon);
                            iconImageView.setFitHeight(12);
                            iconImageView.setPreserveRatio(true);
                            setGraphic(iconImageView);
                        }
                    }
                };
            }
        };
        mainIngredientComboBox.setButtonCell(cellFactoryMain.call(null));
        mainIngredientComboBox.setCellFactory(cellFactoryMain);
    }

    private void populateMainCuisineComboBox() {
        Callback<ListView<String>, ListCell<String>> cellFactoryCuisine = new Callback<ListView<String>, ListCell<String>>() {

            @Override
            public ListCell<String> call(ListView<String> p) {

                return new ListCell<String>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(item);

                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            Image icon = null;
                            String iconPath;
                            try {
                                switch (item) {

                                    case "Sverige":
                                        iconPath = "RecipeSearch/resources/icon_flag_sweden.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Grekland":
                                        iconPath = "RecipeSearch/resources/icon_flag_greece.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Frankrike":
                                        iconPath = "RecipeSearch/resources/icon_flag_france.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Asien":
                                        iconPath = "RecipeSearch/resources/icon_flag_asia.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Afrika":
                                        iconPath = "RecipeSearch/resources/icon_flag_africa.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Indien":
                                        iconPath = "RecipeSearch/resources/icon_flag_india.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                }
                            } catch (NullPointerException ex) {
                                //This should never happen in this lab but could load a default image in case of a NullPointer
                            }
                            ImageView iconImageView = new ImageView(icon);
                            iconImageView.setFitHeight(12);
                            iconImageView.setPreserveRatio(true);
                            setGraphic(iconImageView);
                        }
                    }
                };
            }
        };
        cuisineComboBox.setButtonCell(cellFactoryCuisine.call(null));
        cuisineComboBox.setCellFactory(cellFactoryCuisine);
    }

    public Image getSquareImage(Image image){

        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;

        if(image.getWidth() > image.getHeight()){
            width = (int) image.getHeight();
            height = (int) image.getHeight();
            x = (int)(image.getWidth() - width)/2;
            y = 0;
        }

        else if(image.getHeight() > image.getWidth()){
            width = (int) image.getWidth();
            height = (int) image.getWidth();
            x = 0;
            y = (int) (image.getHeight() - height)/2;
        }

        else{
            //Width equals Height, return original image
            return image;
        }
        return new WritableImage(image.getPixelReader(), x, y, width, height);
    }

    public Image getCuisineImage(String cuisine) {
        String iconPath;
        switch (cuisine) {
            case "Sverige" -> {
                iconPath = "RecipeSearch/resources/icon_flag_sweden.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
            }
            case "Grekland" -> {
                iconPath = "RecipeSearch/resources/icon_flag_greece.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
            }
            case "Frankrike" -> {
                iconPath = "RecipeSearch/resources/icon_flag_france.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
            }
            case "Asien" -> {
                iconPath = "RecipeSearch/resources/icon_flag_asia.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
            }
            case "Afrika" -> {
                iconPath = "RecipeSearch/resources/icon_flag_africa.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
            }
            case "Indien" -> {
                iconPath = "RecipeSearch/resources/icon_flag_india.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
            }
        }
        return null;
    }

    public Image getIngredientImage(String mainIngredient) {
        String iconPath;
        switch (mainIngredient) {
            case "Kött" -> {
                iconPath = "RecipeSearch/resources/icon_main_beef.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
            }
            case "Kyckling" -> {
                iconPath = "RecipeSearch/resources/icon_main_chicken.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
            }
            case "Fisk" -> {
                iconPath = "RecipeSearch/resources/icon_main_fish.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
            }
            case "Vegetariskt" -> {
                iconPath = "RecipeSearch/resources/icon_main_veg.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
            }
        }
        return null;
    }

    public Image getDifficultyImage(String difficulty) {
        String iconPath;
        switch (difficulty) {
            case "Lätt" -> {
                iconPath = "RecipeSearch/resources/icon_difficulty_easy.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
            }
            case "Mellan" -> {
                iconPath = "RecipeSearch/resources/icon_difficulty_medium.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
            }
            case "Svår" -> {
                iconPath = "RecipeSearch/resources/icon_difficulty_hard.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
            }
        }
        return null;
    }

    @FXML
    public void closeButtonMouseEntered(){
        detailedViewCloseButton.setImage(new Image(getClass().getClassLoader().getResourceAsStream(
                "RecipeSearch/resources/icon_close_hover.png")));
    }

    @FXML
    public void closeButtonMousePressed(){
        detailedViewCloseButton.setImage(new Image(getClass().getClassLoader().getResourceAsStream(
                "RecipeSearch/resources/icon_close_pressed.png")));
        closeRecipeView();
    }

    @FXML
    public void closeButtonMouseExited(){
        detailedViewCloseButton.setImage(new Image(getClass().getClassLoader().getResourceAsStream(
                "RecipeSearch/resources/icon_close.png")));
    }

}
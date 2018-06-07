package infoia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.event.ChangeEvent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class GUI extends Application {
	CookingAgent cookingAgent;
	ListView<String> guiFridge;
	ListView<String> guiBestRecipe;
	ListView<String> guiShoppingList;
	ObservableList<String> guiAllIngredients;
	Text guiBestRecipeName;
	Scene guiScene;
	Stage guiStage;
	BorderPane bPane;
	
	
	@Override
	public void start(Stage stage) throws Exception {
		guiStage = stage;
		bPane = new BorderPane();
		guiScene = new Scene(bPane, 1000, 650);
	    cookingAgent = new CookingAgent(); 
	    guiFridge = new ListView<String>();
	    guiFridge.setPrefSize(250, 520);
	    guiBestRecipe = new ListView<String>();
	    guiBestRecipe.setPrefSize(500, 410);
	    guiBestRecipeName = boldText("");
	    guiShoppingList = new ListView<String>();
	    guiShoppingList.setPrefSize(500, 150);
	    guiAllIngredients = FXCollections.observableArrayList();

	    for (Ingredient i : cookingAgent.ingredients) {
	    	guiAllIngredients.add(i.getName());
	    }
		
	    updateGUIFridge(guiFridge);	
	    
		setupLayout();
		guiStage.show();
	}
	
	private void setupLayout() {
//		bPane.setTop(); 
//	    bPane.setBottom(new TextField("Bottom")); 
	    bPane.setLeft(layoutLeft());
	    bPane.setRight(layoutRight()); 
	    bPane.setCenter(layoutCenter());
		
		// Setup GUI stage and show
		guiStage.setTitle("INFOIA: Cooking Agent");
		guiStage.setScene(guiScene);
		guiScene.setFill(Color.LIGHTGREY);
	}
	
	private Node layoutLeft() {
		Insets padding = new Insets(10);
		VBox leftVBox = new VBox();
	    leftVBox.setPadding(padding);
	    leftVBox.setSpacing(5.0);
	    
	    // Fridge buttons section
	    HBox fridgeHBox = new HBox();
	    fridgeHBox.setSpacing(5.0);
	    Button randomFridgeButton = new Button("Randomize Fridge");
	    randomFridgeButton.setOnAction(value ->  {
	    	cookingAgent.fillFridgeRandomly();
			updateGUIFridge(guiFridge);	
	        });
	    Button clearFridgeButton = new Button("Empty Fridge");
	    clearFridgeButton.setOnAction(value ->  {
	    	cookingAgent.clearFridge();
	    	updateGUIFridge(guiFridge);
	        });
	    fridgeHBox.getChildren().add(randomFridgeButton);	
	    fridgeHBox.getChildren().add(clearFridgeButton);
	    
	    // Fridge add ingredient fields
	    HBox addIngredientHBox = new HBox();
	    addIngredientHBox.setSpacing(5.0);
	    TextField portionField = new TextField();
	    portionField.setPromptText("... g");
	    portionField.setPrefWidth(60);
	    // Source: https://stackoverflow.com/questions/7555564/what-is-the-recommended-way-to-make-a-numeric-textfield-in-javafx
	    portionField.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, 
	            String newValue) {
	            if (!newValue.matches("\\d*")) {
	            	portionField.setText(newValue.replaceAll("[^\\d]", ""));
	            }
	        }
	    });
	    ComboBox<String> ingredientBox = new ComboBox<String>();
	    ingredientBox.setItems(guiAllIngredients);
	    ingredientBox.setEditable(true);
	    ingredientBox.setPromptText("Enter ingredient name...");
	    ingredientBox.getEditor().focusedProperty().addListener(observable -> {
            if (ingredientBox.getSelectionModel().getSelectedIndex() < 0) {
            	ingredientBox.getEditor().setText(null);
            }
        });
	    ingredientBox.addEventHandler(KeyEvent.KEY_PRESSED, t -> ingredientBox.hide());
	    ingredientBox.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue ov, String oldVal, String newVal) {
				handleSearchByKey(oldVal, newVal, guiAllIngredients, ingredientBox);
			}
	    });
	    
	    Button addIngredientButton = new Button("Add");
	    addIngredientButton.setOnAction(value ->  {
	    	cookingAgent.addIngredientToFridge(ingredientBox.getValue(), Integer.parseInt(portionField.getText()));
			updateGUIFridge(guiFridge);
			ingredientBox.valueProperty().setValue("");
			portionField.setText("");
	        });
	    addIngredientHBox.getChildren().add(portionField);
	    addIngredientHBox.getChildren().add(ingredientBox);	
	    addIngredientHBox.getChildren().add(addIngredientButton);
	    
	    leftVBox.getChildren().add(boldText("Fridge"));
	    leftVBox.getChildren().add(fridgeHBox);
	    leftVBox.getChildren().add(addIngredientHBox);
	    leftVBox.getChildren().add(guiFridge);
	    
	    return leftVBox;
	}

	private Node layoutRight() {
		Insets padding = new Insets(10);
	    VBox rightVBox = new VBox();
	    rightVBox.setPadding(padding);
	    rightVBox.setSpacing(5.0);
	    HBox recipeHBox = new HBox();
	    recipeHBox.getChildren().add(boldText("Best recipe: "));
	    recipeHBox.getChildren().add(guiBestRecipeName);
	    rightVBox.getChildren().add(recipeHBox);
	    rightVBox.getChildren().add(guiBestRecipe);
	    rightVBox.getChildren().add(boldText("Shopping list"));
	    rightVBox.getChildren().add(guiShoppingList);
	    
	    return rightVBox;
	}
	
	private Node layoutCenter() {
		Insets padding = new Insets(10);
		VBox centerVBox = new VBox();
	    centerVBox.setPadding(padding);
	    centerVBox.setSpacing(5.0);
	    Button findRecipeButton = new Button("Find Recipe");
	    findRecipeButton.setOnAction(value ->  {
	    	updateGUIBestRecipe(guiBestRecipe, guiShoppingList);
	        });
	    findRecipeButton.setAlignment(Pos.BASELINE_CENTER);
	    centerVBox.getChildren().add(findRecipeButton);
	    
	    return centerVBox;
	}
	
	private Text boldText(String string) {
		Text text = new Text(string);
		text.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		
		return text;
	}
	
	// Source: http://www.drdobbs.com/jvm/simple-searching-in-java/232700121
	private void handleSearchByKey(String oldVal, String newVal, ObservableList<String> totalList, ComboBox<String> box) {
		if (totalList == null || box == null || newVal == null) {
			System.out.println("Warning: parameter is null in handleSearchByKey()");
			return;
		} 	
		// If the number of characters in the text box is less than last time
	    // it must be because the user pressed delete
	    if (oldVal != null && (newVal.length() < oldVal.length())) {
	        // Restore the lists original set of entries 
	        box.setItems( totalList );
	    }
	    // Break out all of the parts of the search text by splitting on white space
	    String[] parts = newVal.toUpperCase().split(" ");
	 
	    // Filter out the entries that don't contain the entered text
	    ObservableList<String> subentries = FXCollections.observableArrayList();
	    for (Object entry : box.getItems()) {
	        boolean match = true;
	        String entryText = (String)entry;
	        for (String part : parts) {
	            // The entry needs to contain all portions of the
	            // search string *but* in any order
	            if (!entryText.toUpperCase().contains(part)) {
	                match = false;
	                break;
	            }
	        }
	 
	        if (match) {
	            subentries.add(entryText);
	        }
	    }
	    box.setItems(subentries); 
	}
	
	private void updateGUIFridge(ListView<String> fridge) {
		ObservableList<String> ingredients = FXCollections.observableArrayList();
		
		if (cookingAgent.fridge == null) {
			System.out.println("Warning: cookingAgent.fridge is null in updateGUIFridge()");
			return;
		}
		
		if (cookingAgent.fridge.size() < 1) {
			ingredients.clear();
		} else {
			for (Portion portion : cookingAgent.fridge) {
				ingredients.add(portion.toString());
			}
		}
		fridge.setItems(ingredients);
	}
	
	private void updateGUIBestRecipe(ListView<String> bestRecipe, ListView<String> shoppingList) {
		ObservableList<String> ingredients = FXCollections.observableArrayList();
		ObservableList<String> missingIngredients = FXCollections.observableArrayList();
		String recipeName = "";
		
		if (!guiFridge.getItems().isEmpty()) {
			Recipe recipe = cookingAgent.getBestRecipe();
			if (recipe != null) {
				recipeName = recipe.name;
				for (Portion p : recipe) {
					if (recipe.getReplacements().containsKey(p)) {
						if(recipe.getReplacements().get(p).getPortion() != null) {
							ingredients.add(p + " REPLACED BY " + recipe.getReplacements().get(p).getPortion());
						} else {
							ingredients.add("REMOVED " + p);
						}
					} else {
						if (recipe.getShoppingList().stream().anyMatch(q -> q.getIngredient() == p.getIngredient())) {
	                        missingIngredients.add(p.toString());
						}
	                    else
	                    	ingredients.add(p.toString());
					}
				}
			}
			
			
		}
		guiBestRecipeName.setText(recipeName);
		bestRecipe.setItems(ingredients);
		shoppingList.setItems(missingIngredients);
	}
	
}
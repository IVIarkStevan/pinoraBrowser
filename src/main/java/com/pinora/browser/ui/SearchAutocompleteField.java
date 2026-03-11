package com.pinora.browser.ui;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.input.KeyCode;
import javafx.geometry.Bounds;
import javafx.stage.Popup;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pinora.browser.util.SearchSuggestionsManager;

import java.util.List;

/**
 * Custom autocomplete search field with suggestions dropdown
 */
public class SearchAutocompleteField extends VBox {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchAutocompleteField.class);
    
    private TextField searchField;
    private Popup suggestionsPopup;
    private ListView<String> suggestionsList;
    private SearchSuggestionsManager suggestionsManager;
    private Runnable onSearchAction;
    private int selectedSuggestionIndex = -1;
    
    public SearchAutocompleteField(SearchSuggestionsManager suggestionsManager) {
        this.suggestionsManager = suggestionsManager;
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        // Create search field
        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(180);
        searchField.setPrefHeight(30);
        searchField.setStyle("-fx-font-size: 12; -fx-padding: 5;");
        
        // Create suggestions popup
        suggestionsPopup = new Popup();
        suggestionsList = new ListView<>();
        suggestionsList.setPrefHeight(200);
        suggestionsList.setMaxHeight(200);
        suggestionsList.setPrefWidth(180);
        suggestionsList.setStyle("-fx-font-size: 11; -fx-padding: 2;");
        
        suggestionsPopup.getContent().add(suggestionsList);
        suggestionsPopup.setAutoHide(true);
        suggestionsPopup.setAutoFix(true);
        
        // Add search field to this VBox
        this.getChildren().add(searchField);
        VBox.setVgrow(searchField, Priority.ALWAYS);
    }
    
    private void setupEventHandlers() {
        // Handle text input for suggestions
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                updateSuggestions(newVal);
            } else {
                suggestionsPopup.hide();
            }
        });
        
        // Handle key events
        searchField.setOnKeyPressed(event -> {
            if (suggestionsPopup.isShowing()) {
                switch (event.getCode()) {
                    case DOWN -> selectNextSuggestion();
                    case UP -> selectPreviousSuggestion();
                    case ENTER -> {
                        if (selectedSuggestionIndex >= 0) {
                            selectCurrentSuggestion();
                        } else {
                            performSearch();
                        }
                        event.consume();
                    }
                    case ESCAPE -> {
                        suggestionsPopup.hide();
                        selectedSuggestionIndex = -1;
                        event.consume();
                    }
                    default -> {}
                }
            } else if (event.getCode() == KeyCode.ENTER) {
                performSearch();
                event.consume();
            }
        });
        
        // Handle suggestion selection via mouse click
        suggestionsList.setOnMouseClicked(event -> {
            int selectedIndex = suggestionsList.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                selectCurrentSuggestion();
            }
        });
        
        // Handle focus lost
        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !suggestionsPopup.isFocused()) {
                // Delay hiding to allow item selection
                javafx.application.Platform.runLater(() -> {
                    if (!suggestionsPopup.isFocused()) {
                        suggestionsPopup.hide();
                    }
                });
            }
        });
    }
    
    private void updateSuggestions(String query) {
        List<String> suggestions = suggestionsManager.getSuggestions(query);
        
        if (suggestions.isEmpty()) {
            suggestionsPopup.hide();
            return;
        }
        
        suggestionsList.getItems().clear();
        suggestionsList.getItems().addAll(suggestions);
        selectedSuggestionIndex = -1;
        
        // Show popup if not already showing
        if (!suggestionsPopup.isShowing()) {
            Window owner = getScene() != null ? getScene().getWindow() : null;
            if (owner != null) {
                Bounds bounds = searchField.localToScene(searchField.getBoundsInLocal());
                if (bounds != null) {
                    suggestionsPopup.show(owner, 
                        owner.getX() + bounds.getCenterX() - 90, // Center horizontally
                        owner.getY() + bounds.getCenterY() + bounds.getHeight() - 5);
                }
            }
        }
    }
    
    private void selectNextSuggestion() {
        if (!suggestionsList.getItems().isEmpty()) {
            selectedSuggestionIndex = Math.min(selectedSuggestionIndex + 1, 
                suggestionsList.getItems().size() - 1);
            suggestionsList.getSelectionModel().select(selectedSuggestionIndex);
            suggestionsList.scrollTo(selectedSuggestionIndex);
        }
    }
    
    private void selectPreviousSuggestion() {
        selectedSuggestionIndex = Math.max(selectedSuggestionIndex - 1, -1);
        if (selectedSuggestionIndex >= 0) {
            suggestionsList.getSelectionModel().select(selectedSuggestionIndex);
            suggestionsList.scrollTo(selectedSuggestionIndex);
        } else {
            suggestionsList.getSelectionModel().clearSelection();
        }
    }
    
    private void selectCurrentSuggestion() {
        if (selectedSuggestionIndex >= 0 && 
            selectedSuggestionIndex < suggestionsList.getItems().size()) {
            String selected = suggestionsList.getItems().get(selectedSuggestionIndex);
            searchField.setText(selected);
            suggestionsPopup.hide();
            performSearch();
        }
    }
    
    private void performSearch() {
        suggestionsPopup.hide();
        selectedSuggestionIndex = -1;
        
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            // Add to search history
            suggestionsManager.addSearchToHistory(query);
            
            // Execute search callback
            if (onSearchAction != null) {
                onSearchAction.run();
            }
        }
    }
    
    /**
     * Set the callback to execute when search is performed
     */
    public void setOnSearch(Runnable action) {
        this.onSearchAction = action;
    }
    
    /**
     * Get the search query text
     */
    public String getText() {
        return searchField.getText();
    }
    
    /**
     * Set the search query text
     */
    public void setText(String text) {
        searchField.setText(text != null ? text : "");
    }
    
    /**
     * Clear the search field
     */
    public void clear() {
        searchField.clear();
        suggestionsPopup.hide();
        selectedSuggestionIndex = -1;
    }
    
    /**
     * Focus the search field
     */
    @Override
    public void requestFocus() {
        searchField.requestFocus();
    }
    
    /**
     * Get the underlying TextField for styling if needed
     */
    public TextField getTextField() {
        return searchField;
    }
}

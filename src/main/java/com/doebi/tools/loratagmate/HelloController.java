

//   protected void onHelloButtonClick() {        welcomeText.setText("Welcome to JavaFX Application!");    }}


package com.doebi.tools.loratagmate;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileReader;
import java.util.Random;



public class HelloController {

    @FXML
    private VBox mainWindow;

    @FXML
    private Label ID_label_file_name;

    @FXML
    private TextField ID_label_file_path;

    @FXML
    private VBox stackPane_drop_zone;

    @FXML
    private FlowPane ID_flow_pane_high_tags;

    @FXML
    private FlowPane ID_flow_pane_low_tags;

    @FXML
    private Button ID_button_save_tag;

    @FXML
    private Button ID_button_save_zip;

    @FXML
    public void initialize() {
        System.out.println("HelloController initialized — UI is loaded and ready.");
        setUpDropZones();
        ID_button_save_tag.setOnAction(event -> {
            saveTag();
        });
    }

    private void setUpDropZones() {
        setupDropZoneForImage();
        setupDropZoneForTags(ID_flow_pane_low_tags);
        setupDropZoneForTags(ID_flow_pane_high_tags);
    }

    private void setupDropZoneForTags(FlowPane flowPane){
        flowPane.setOnDragOver(event -> {
            if (event.getGestureSource() != ID_flow_pane_high_tags &&
                    event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        flowPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                db.getFiles().forEach(file -> System.out.println(" - " + file.getAbsolutePath()));
                // ➕ You can now call a method here to parse the file(s)
                handleDroppedFiles(db.getFiles(), fileacceptancemode.FileAcceptanceMode.TAGS_ONLY);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void setupDropZoneForImage() {
        stackPane_drop_zone.setOnDragOver(event -> {
            if (event.getGestureSource() != stackPane_drop_zone &&
                    event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        stackPane_drop_zone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                System.out.println("📥 Drop detected:");
                db.getFiles().forEach(file -> System.out.println(" - " + file.getAbsolutePath()));
                // ➕ You can now call a method here to parse the file(s)
                handleDroppedFiles(db.getFiles(), fileacceptancemode.FileAcceptanceMode.ALL_FILES);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void handleDroppedFiles(List<File> files, fileacceptancemode.FileAcceptanceMode fileAcceptanceMode) {
        // ⚙️ Replace this with real file handling
        for (File file : files) {
            System.out.println("Handling file: " + file.getName());
            handleFile(file,fileAcceptanceMode);
        }
    }



    private void handleFile(File file, fileacceptancemode.FileAcceptanceMode fileAcceptanceMode) {
        int dotIndex = file.getName().lastIndexOf(".");
        String fileExtension = ((dotIndex > 0) ? file.getName().substring(dotIndex + 1) : "").toLowerCase();
        System.out.println(fileExtension);
        switch (fileExtension) {
            case "txt": break; // make method
            case "taghigh": handleTagFileDrop(file,ID_flow_pane_high_tags); break;// make method
            case "taglow": handleTagFileDrop(file,ID_flow_pane_low_tags); break;// make method
            case "png","jpg" : if (fileAcceptanceMode == fileacceptancemode.FileAcceptanceMode.ALL_FILES)  handleImageFile(file);break;// make method
            default: {
                System.out.println("Invalid file extension: " + fileExtension);
            }
        }
    }

    private void handleTxtFile(File file) {
        System.out.println("Handling TXT tag file: " + file.getName());
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    //addHighTag(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read taghigh file: " + e.getMessage());
        }
    }

    ArrayList<String> getTagFromUI(FlowPane flowPane){
        ArrayList<String> tags = new ArrayList<>();
        for (Node node : flowPane.getChildren()) {
            if (node instanceof ToggleButton toggleButton) {
                tags.add(toggleButton.getText());
            }
        }
        System.out.print("Uitgelzen uit UI"); System.out.println(tags);
        return tags;
    }

    private void handleTagFileDrop(File file,FlowPane flowPane) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {

                if (!line.trim().isEmpty()) {
                    String[] tags = line.split(",");
                    ArrayList<String> existingTags = getTagFromUI(flowPane);
                    for (String tag : tags) {
                        if (!existingTags.contains(tag)) {
                            addTag(tag.trim(),flowPane);
                            existingTags.add(tag);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read tag file: " + e.getMessage());
        }
    }


    private void handleImageFile(File file) {
        System.out.println("Handling image: " + file.getName());
        // Show it in the UI (set image view, update filename label)
        Image image = new Image(file.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(300);       // Optional scaling
        imageView.setPreserveRatio(true); // Keep aspect ratio
        imageView.setSmooth(true);
        imageView.setCache(true);
// Bind width and height to StackPane's dimensions
        imageView.fitWidthProperty().bind(stackPane_drop_zone.widthProperty());
        imageView.fitHeightProperty().bind(stackPane_drop_zone.heightProperty());
        // Optionally clear previous image and add new one to the drop zone
        stackPane_drop_zone.getChildren().clear();
        stackPane_drop_zone.getChildren().add(imageView);
        ID_label_file_name.setText(file.getName());
    }



      private void addTag(String tag,FlowPane flowPane) {
        ToggleButton tagButton = prepareToggleButton(tag,flowPane);
        tagButton.setOnAction(e -> System.out.println("tag clicked: " + tag));

        flowPane.getChildren().add(tagButton);
    }

    public ToggleButton prepareToggleButton(String tag, FlowPane pane) {
        ToggleButton tagButton = new ToggleButton(tag);
        tagButton.getStyleClass().add("tag-button");

        ContextMenu contextMenu = new ContextMenu();

        MenuItem deleteItem = new MenuItem("🗑️ Delete");
        deleteItem.setOnAction(e -> pane.getChildren().remove(tagButton));

        MenuItem moveItem = new MenuItem("Move to other tag zone");
        moveItem.setOnAction(_ -> moveTagToOtherZone(tagButton));

        MenuItem renameItem = new MenuItem("✏️ Rename");
        renameItem.setOnAction(_ -> renameTag(tag, tagButton));

        MenuItem addNewItem = new MenuItem("Add New Tag");
        addNewItem.setOnAction(_ -> addNewTagToThisZone(tagButton));

        contextMenu.getItems().addAll(deleteItem, renameItem,moveItem,addNewItem);
        tagButton.setOnContextMenuRequested(e -> contextMenu.show(tagButton, e.getScreenX(), e.getScreenY()));

        return tagButton;
    }

    private void renameTag(String tag, ToggleButton tagButton) {
       TextInputDialog dialog = new TextInputDialog(tag);
       dialog.setTitle("Rename Tag");
       dialog.setHeaderText(null);
       dialog.setContentText("New tag name:");
       dialog.showAndWait().ifPresent(newName -> {
           if (tagButton.getParent() instanceof FlowPane flowPane ) {
               flowPane.getChildren().remove(tagButton);
               addTag(newName, flowPane);
               tagButton.setText(newName);
           }
       });
    }


    private void addNewTagToThisZone(ToggleButton tagButton) {
        Random random = new Random();
        if (tagButton.getParent() instanceof FlowPane flowPane) {
            addTag("RENAME ME:" + random.nextInt(), flowPane);
        }
    }

    private void moveTagToOtherZone(ToggleButton tagButton) {
        String tagText = tagButton.getText();

        FlowPane source = (FlowPane) tagButton.getParent();
        FlowPane target = (source == ID_flow_pane_low_tags) ? ID_flow_pane_high_tags : ID_flow_pane_low_tags;

        boolean alreadyExists = target.getChildren().stream()
                .filter(node -> node instanceof ToggleButton)
                .map(node -> ((ToggleButton) node).getText())
                .anyMatch(text -> text.equals(tagText));

        // Always remove from source
        source.getChildren().remove(tagButton);

        // Only add if not yet in target (by text)
        if (!alreadyExists) {
            target.getChildren().add(tagButton);
        }
    }







    // Add action methods if needed later
    // public void onSaveTagClick() { ... }
    // public void onSaveZipClick() { ... }

    public void saveTag() {
        System.out.println("HelloController getID_button_save_tagClick");

    }





}


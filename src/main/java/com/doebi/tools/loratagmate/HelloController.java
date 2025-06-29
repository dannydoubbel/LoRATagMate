
// Test commit from second PC 2

package com.doebi.tools.loratagmate;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;



public class HelloController {

    private static final String DEFAULT_TAG_NAME = "RENAME ME";
    private static final String APP_TITLE_BASE = "TagMate by Aeris";
    private static final String NO_IMAGE_LOADED = "No image loaded";
    private static final String NO_PROJECT_SET = "No project set";


    @FXML
    private VBox mainWindow;

    @FXML
    private Label ID_label_app_name;

    @FXML
    private TextField ID_label_file_name;

    @FXML
    private TextField ID_label_file_path;

    @FXML
    private TextField ID_label_project_name;


    @FXML
    private VBox stackPane_drop_zone;

    @FXML
    private VBox stackPane_trash_zone;

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
        setUpSaveButtonListeners();
        ensureAtLeastOneTagIsPresent(ID_flow_pane_high_tags);
        ensureAtLeastOneTagIsPresent(ID_flow_pane_low_tags);
        setupTagContainerListeners();
        setUpProjectNameListener();
        postInitialize();
    }

    public void postInitialize() {
        // This runs AFTER initialize()
        Platform.runLater(() -> {
            Stage stage = (Stage) mainWindow.getScene().getWindow();
            stage.setTitle("TagMate by Aeris — ready");
            System.out.println("postInitialize <UNK> UI is loaded and ready.");
            setAppTitleBase(APP_TITLE_BASE);
            addTrashCan();
            ID_label_file_name.setText(NO_IMAGE_LOADED);
            ID_label_project_name.setText(NO_PROJECT_SET);
        });
    }

    private void addTrashCan() {
        final String RESOURCE_PATH = "/com/doebi/tools/loratagmate";
        Image trash = new Image(Objects.requireNonNull(getClass().getResourceAsStream(RESOURCE_PATH + "/images/trashcan.png")));
        ImageView trashView = new ImageView(trash);

        trashView.setFitWidth(stackPane_trash_zone.getMaxWidth()); // or any size you prefer
        trashView.setPreserveRatio(true);
        // 🟡 Allow drop
        trashView.setOnDragOver(event -> {
            if (event.getGestureSource() instanceof Node) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // 🔴 Handle drop
        trashView.setOnDragDropped(event -> {
            Object gestureSource = event.getGestureSource();
            if (gestureSource instanceof Node node) {
                ((Pane) node.getParent()).getChildren().remove(node);
                System.out.println("Deleted node: " + node);
            }
            if (event.getGestureSource() instanceof ImageView) {
                // Remove image
                stackPane_drop_zone.getChildren().clear();
                ID_label_file_name.setText(NO_IMAGE_LOADED);
                ID_label_project_name.setText(NO_PROJECT_SET);
            }


            event.setDropCompleted(true);
            event.consume();
        });
        stackPane_trash_zone.getChildren().add(trashView);

    }

    private void setAppTitleBase(String title) {
        System.out.println("inside setAppTitleBase");
        try {
            ID_label_app_name.setText(APP_TITLE_BASE);
            Stage stage = (Stage) mainWindow.getScene().getWindow();
            stage.setTitle(title);
        } catch (Exception e) {
            System.out.println("setApp Title Base stage is null");
        }
    }

    private void setUpProjectNameListener() {
        ID_label_project_name.textProperty().addListener((_, _, newVal) -> {
            setAppTitleBase(APP_TITLE_BASE + " -" + newVal);
        });
    }

    private void setUpSaveButtonListeners() {
        ID_button_save_tag.setOnAction(_ -> {
            saveTag();
        });
    }


    private void setupTagContainerListeners() {
        ID_flow_pane_high_tags.getChildren().addListener((ListChangeListener<Node>) change -> {
            ensureAtLeastOneTagIsPresent(ID_flow_pane_high_tags);
        });

        ID_flow_pane_low_tags.getChildren().addListener((ListChangeListener<Node>) change -> {
            ensureAtLeastOneTagIsPresent(ID_flow_pane_low_tags);
        });

        setUpTagContainerListenersEmptyClick(ID_flow_pane_high_tags);
        setUpTagContainerListenersEmptyClick(ID_flow_pane_low_tags);
        setUpTagContainersDropZones(ID_flow_pane_high_tags);
        setUpTagContainersDropZones(ID_flow_pane_low_tags);
    }

    private void setUpTagContainersDropZones(FlowPane flowPane) {
       flowPane.setOnDragOver(event ->

    {
        if (event.getGestureSource() instanceof ToggleButton) {
            event.acceptTransferModes(TransferMode.MOVE);
        }
        event.consume();
    });


    flowPane.setOnDragDropped(event ->
    {
        if (event.getGestureSource() instanceof ToggleButton tagButton) {
            FlowPane sourcePane = (FlowPane) tagButton.getParent();


            // Avoid duplicate by tag text
            boolean alreadyExists = flowPane.getChildren().stream()
                    .filter(node -> node instanceof ToggleButton)
                    .anyMatch(node -> ((ToggleButton) node).getText().equals(tagButton.getText()));

                sourcePane.getChildren().remove(tagButton);
            if (!alreadyExists) {
                flowPane.getChildren().add(tagButton);
            }

            event.setDropCompleted(true);
        }
        event.consume();
    });
}



    private void setUpTagContainerListenersEmptyClick(FlowPane flowPane) {
        flowPane.setOnMouseClicked(event -> { // clicking is adding an new default tag
            if (event.getTarget() == flowPane) {
                System.out.println("Clicked on empty space in a TAG zone");
                // Optionally: add a new placeholder tag here
                addTag( getDefaultTagNameRandomized(), flowPane);
            }
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
            case "txt": handleTxtFile(file); break; // make method
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
                // 1. Deactivate all existing tags
                deactivateAllTagsInZone(ID_flow_pane_high_tags);
                deactivateAllTagsInZone(ID_flow_pane_low_tags);

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        String[] tags = line.split(",");
                        for (String rawTag : tags) {
                            String tag = rawTag.trim();
                            if (tag.isEmpty()) continue;

                            boolean found = reactivateIfFound(tag, ID_flow_pane_high_tags)
                                    || reactivateIfFound(tag, ID_flow_pane_low_tags);

                            if (!found) {
                                if (!tagExistsInZone(tag, ID_flow_pane_low_tags)) {
                                    addTag(tag, ID_flow_pane_low_tags);
                                    System.out.println("We add it " + tag);
                                } else {
                                    // It exists but was deactivated — reactivate it
                                    reactivateIfFound(tag, ID_flow_pane_low_tags);
                                }
                            }
                        }
                    }
                }

                ensureAtLeastOneTagIsPresent(ID_flow_pane_high_tags);
                ensureAtLeastOneTagIsPresent(ID_flow_pane_low_tags);

            } catch (IOException e) {
                System.err.println("Failed to read active tag list: " + e.getMessage());
            }
    }
    private boolean tagExistsInZone(String tag, FlowPane pane) {
        for (Node node : pane.getChildren()) {
            if (node instanceof ToggleButton) {
                String btnText = ((ToggleButton) node).getText().trim();
                if (btnText.equalsIgnoreCase(tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void deactivateAllTagsInZone(FlowPane zone) {
        for (Node node : zone.getChildren()) {
            if (node instanceof ToggleButton) {
                node.getStyleClass().remove("tag-active");
                // Optionally update internal state too if needed
            }
        }
    }

    private boolean reactivateIfFound(String tag, FlowPane zone) {
        for (Node node : zone.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton btn = (ToggleButton) node;
                if (btn.getText().trim().equalsIgnoreCase(tag)) {
                    if (!btn.getStyleClass().contains("tag-active")) {
                        btn.getStyleClass().add("tag-active");
                    }
                    return true;
                }
            }
        }
        return false;
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


        imageView.setId("imageView"); // Optional: Helps in distinguishing the node type

        imageView.setOnDragDetected(event -> {
            Dragboard db = imageView.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("Current Image"); // Just required, not actually used
            db.setContent(content);
            // 👌 Bonus: make drag visually show the tag
            db.setDragView(imageView.snapshot(null, null));
            event.consume();
        });



        // Optionally clear previous image and add new one to the drop zone
        stackPane_drop_zone.getChildren().clear();
        stackPane_drop_zone.getChildren().add(imageView);
        ID_label_file_name.setText(file.getName());
        ID_label_project_name.setText( file.getName().substring(0, file.getName().lastIndexOf('.')));
    }



      private void addTag(String tag,FlowPane flowPane) {
        ToggleButton tagButton = prepareToggleButton(tag,flowPane);
        //tagButton.setOnAction(e -> System.out.println("tag clicked: " + tag));
        tagButton.setOnAction(e -> actionForToggleButton(tagButton));

        flowPane.getChildren().add(tagButton);
    }

    private void actionForToggleButton(ToggleButton toggleButton) {
        boolean nowSelected = toggleButton.isSelected();

        System.out.println("tag clicked: " + toggleButton.getText() + " — selected: " + nowSelected);

        if (nowSelected) {
            if (!toggleButton.getStyleClass().contains("tag-active")) {
                toggleButton.getStyleClass().add("tag-active");
            }
        } else {
            toggleButton.getStyleClass().remove("tag-active");
        }
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

        tagButton.setOnDragDetected(event -> {
            Dragboard db = tagButton.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(tagButton.getText()); // Just required, not actually used
            db.setContent(content);
            // 👌 Bonus: make drag visually show the tag
            db.setDragView(tagButton.snapshot(null, null));
            event.consume();
        });


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
            addTag( getDefaultTagNameRandomized(), flowPane);
        }
    }

    private String getDefaultTagNameRandomized(){
        Random random = new Random();
        return DEFAULT_TAG_NAME + random.nextInt();
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
        System.out.println("Moved a togglebutton to another tag zone");
    }







    // Add action methods if needed later
    // public void onSaveTagClick() { ... }
    // public void onSaveZipClick() { ... }

    public void saveTag() {
        System.out.println("HelloController getID_button_save_tagClick");

    }

    private void ensureAtLeastOneTagIsPresent(FlowPane flowPane) {
        if (flowPane.getChildren().isEmpty()) {
            addTag(getDefaultTagNameRandomized(), flowPane);
        }
    }









}


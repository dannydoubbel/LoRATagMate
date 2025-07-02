
// Test commit from second PC 2

package com.doebi.tools.loratagmate;

import javafx.animation.ScaleTransition;
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
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class HelloController {

    private static final String DEFAULT_TAG_NAME = "RENAME ME";
    private static final String APP_TITLE_BASE = "TagMate by Aeris";
    private static final String NO_IMAGE_LOADED = "No image loaded";
    private static final String NO_PROJECT_SET = "No project set";
    private static final String RESOURCE_PATH = "/com/doebi/tools/loratagmate";

    @FXML
    public TilePane ID_flow_pane_global_tags;

    @FXML
    public ScrollPane mainScroll;


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
    private TilePane ID_flow_pane_high_tags;

    @FXML
    private TilePane ID_flow_pane_low_tags;

    @FXML
    private Button ID_button_save_tag;

    @FXML
    private Button ID_button_save_zip;


    @FXML
    public void initialize() {
        System.out.println("HelloController initialized — UI is loaded and ready.");
        setUpDropZones();
        setUpSaveButtonActions();
        setupTagContainerListeners();
        setUpProjectNameListener();
        ensureAtLeastOneTagIsPresent(ID_flow_pane_high_tags);
        ensureAtLeastOneTagIsPresent(ID_flow_pane_low_tags);
        finalizeUISetup();
    }

    public void finalizeUISetup() {
        // This runs AFTER initialize()
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) mainWindow.getScene().getWindow();
                stage.setTitle("TagMate by Aeris — ready");
            } catch (Exception e) {
                System.out.println("finalise Setup() " + e.getMessage());
            }
            System.out.println("postInitialize UI is loaded and ready.");
            setAppTitleBase(APP_TITLE_BASE);
            addTrashCan();
            ID_label_file_name.setText(NO_IMAGE_LOADED);
            ID_label_project_name.setText(NO_PROJECT_SET);


            mainScroll.setFitToWidth(true);
            mainScroll.setFitToHeight(true);
            mainWindow.setPrefHeight(Region.USE_COMPUTED_SIZE);  // may help layout hints
            ID_flow_pane_global_tags.setId("ID_flow_pane_global_tags");

        });
    }

    private void addTrashCan() {

        Image trash = new Image(Objects.requireNonNull(getClass().getResourceAsStream(RESOURCE_PATH + "/images/trashcan.png")));
        ImageView trashView = new ImageView(trash);


        trashView.fitWidthProperty().bind(stackPane_trash_zone.widthProperty());

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
        //System.out.println("inside setAppTitleBase");
        try {
            ID_label_app_name.setText(APP_TITLE_BASE);
            Stage stage = (Stage) mainWindow.getScene().getWindow();
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpProjectNameListener() {
        ID_label_project_name.textProperty().addListener((_, _, newVal) -> {
            String safeVal = (newVal == null || newVal.trim().isEmpty()) ? "" : " - " + newVal.trim();
            setAppTitleBase(APP_TITLE_BASE + safeVal);
        });
    }

    private void setUpSaveButtonActions() {
        ID_button_save_tag.setOnAction(_ -> saveTag());
    }


    private void setupTagContainerListeners() {
        setupTagZone(ID_flow_pane_low_tags);
        setupTagZone(ID_flow_pane_high_tags);
        setupTagZone(ID_flow_pane_global_tags);
    }

    private void setupTagZone(TilePane zone) {
        zone.getChildren().addListener((ListChangeListener<Node>) _ -> ensureAtLeastOneTagIsPresent(zone));
        setUpTagContainerListenersEmptyClick(zone);
        setUpTagContainersDropZones(zone);
    }


    private void setUpTagContainersDropZones(TilePane targetPane) {
        targetPane.setOnDragOver(event ->
        {
            if (event.getGestureSource() instanceof ToggleButton) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        targetPane.setOnDragDropped(event ->
        {
            if (event.getGestureSource() instanceof ToggleButton tagButton) {
                TilePane sourcePane = (TilePane) tagButton.getParent();

                if (sourcePane == targetPane) {
                    event.setDropCompleted(true);
                    event.consume();
                    return;
                }
                boolean alreadyExistsInTarget = targetPane.getChildren().stream()
                        .filter(node -> node instanceof ToggleButton)
                        .anyMatch(node -> ((ToggleButton) node).getText().equals(tagButton.getText()));

                if (!alreadyExistsInTarget) {
                    targetPane.getChildren().add(tagButton);
                }

                // ✅ Use ID or fx:id to check if it's the global tag zone

                System.out.println("Part 1 :" + "ID_flow_pane_global_tags");
                System.out.println("Part 2 :" + sourcePane.getId());
                System.out.println("Part 3 :" + targetPane.getId());
                if (!"ID_flow_pane_global_tags".equals(sourcePane.getId())) {
                    System.out.println("In here to remove");
                    sourcePane.getChildren().remove(tagButton);
                } else {
                    System.out.println("In here to NOT REMOVE");
                    boolean alreadyExistsInSource = sourcePane.getChildren().stream()
                            .filter(node -> node instanceof ToggleButton)
                            .anyMatch(node -> ((ToggleButton) node).getText().equals(tagButton.getText()));
                    if (!alreadyExistsInSource) {
                        sourcePane.getChildren().add(prepareToggleButton(tagButton.getText(), sourcePane));
                    }

                }

                event.setDropCompleted(true);

            }
            event.consume();
        });
    }


    private void setUpTagContainerListenersEmptyClick(TilePane flowPane) {
        flowPane.setOnMouseClicked(event -> { // clicking is adding an new default tag
            if (event.getTarget() == flowPane) {
                System.out.println("Clicked on empty space in a TAG zone");
                // Optionally: add a new placeholder tag here
                addTag(getDefaultTagNameRandomized(), flowPane);
            }
        });
    }


    private void setUpDropZones() {
        setupDropZoneForImage();
        setupDropZoneForTags(ID_flow_pane_low_tags);
        setupDropZoneForTags(ID_flow_pane_high_tags);
    }

    private void setupDropZoneForTags(TilePane flowPane) {
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
                //System.out.println("📥 Drop detected:");
                //db.getFiles().forEach(file -> System.out.println(" - " + file.getAbsolutePath()));
                // ➕ You can now call a method here to parse the file(s)
                handleDroppedFiles(db.getFiles(), fileacceptancemode.FileAcceptanceMode.ALL_FILES);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void handleDroppedFiles(List<File> files, fileacceptancemode.FileAcceptanceMode fileAcceptanceMode) {
        System.out.println("inside private void handleDroppedFiles ");
        System.out.println("Nr of files in the list : " + files.size());
        System.out.println("counter without the handleFile method");
        AtomicInteger counter = new AtomicInteger(0);
        files.forEach(file ->
        {
            System.out.println(" - " + counter + " " + file.getAbsolutePath());
            counter.incrementAndGet();
        });
        counter.set(0);
        System.out.println("Counting with the handleFile method");

        files.forEach(file -> {
            try {

                System.out.println(" - " + counter + " " + file.getAbsolutePath());
                counter.incrementAndGet();
                handleFile(file, fileAcceptanceMode);
            } catch (Exception e) {
                System.err.println("❗ Exception while handling file: " + file.getName());
                e.printStackTrace();

            }
        });
        System.out.println("END OF METHOD");
    }


    private void handleFile(File file, fileacceptancemode.FileAcceptanceMode fileAcceptanceMode) {
        int dotIndex = file.getName().lastIndexOf(".");
        String fileExtension = ((dotIndex > 0) ? file.getName().substring(dotIndex + 1) : "").toLowerCase();
        //System.out.println(fileExtension);
        switch (fileExtension) {
            case "txt":
                handleTxtFile(file);
                break; // make method
            case "taghigh":
                handleTagFileDrop(file, ID_flow_pane_high_tags);
                break;// make method
            case "taglow":
                handleTagFileDrop(file, ID_flow_pane_low_tags);
                break;// make method
            case "png", "jpg":
                if (fileAcceptanceMode == fileacceptancemode.FileAcceptanceMode.ALL_FILES) handleImageFile(file);
                break;// make method
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

    private boolean tagExistsInZone(String tag, TilePane pane) {
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

    private void deactivateAllTagsInZone(TilePane zone) {
        for (Node node : zone.getChildren()) {
            if (node instanceof ToggleButton) {
                node.getStyleClass().remove("tag-active");
                // Optionally update internal state too if needed
            }
        }
    }

    private boolean reactivateIfFound(String tag, TilePane zone) {
        for (Node node : zone.getChildren()) {
            if (node instanceof ToggleButton btn) {
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


    ArrayList<String> getTagTextsFromZone(TilePane flowPane) {
        List<String> tags = flowPane.getChildren().stream()
                .filter(node -> node instanceof ToggleButton)
                .map(node -> ((ToggleButton) node).getText())
                .toList();

        System.out.println("📦 Tags extracted from UI: " + tags);
        return new ArrayList<>(tags);
    }

    private void handleTagFileDrop(File file, TilePane flowPane) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            System.out.println("Inside handling file: " + file.getName());
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] tags = line.split(",");
                    List<String> existingTags = getTagTextsFromZone(flowPane);
                    for (String tag : tags) {
                        System.out.println("Found in the file :" + tag);
                        if (!existingTags.contains(tag)) {
                            addTag(tag.trim(), flowPane);
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
        stackPane_drop_zone.getChildren().clear();
        stackPane_drop_zone.getChildren().add(imageView);
        setFileRelatedLabels(file);
    }

    private void setFileRelatedLabels(File file) {
        ID_label_file_name.setText(file.getName());
        ID_label_project_name.setText(file.getName().substring(0, file.getName().lastIndexOf('.')));
        ID_label_file_path.setText(file.getPath().substring(0, file.getPath().lastIndexOf(ID_label_file_name.getText())));
    }


    private void addTag(String tag, TilePane flowPane) {
        ToggleButton tagButton = prepareToggleButton(tag, flowPane);
        tagButton.setOnAction(_ -> actionForToggleButton(tagButton));
        playScaleTransitionOn(tagButton);

        flowPane.getChildren().add(tagButton);

//        System.out.println("Before TilePane pref height: " + flowPane.getPrefHeight());
        Platform.runLater(() -> {
            flowPane.setPrefHeight(Region.USE_COMPUTED_SIZE); // try explicitly
            flowPane.requestLayout();
            flowPane.getParent().requestLayout();
            if (flowPane.getParent() instanceof Region parentRegion) {
                parentRegion.setPrefHeight(Region.USE_COMPUTED_SIZE);
                parentRegion.requestLayout();
                // System.out.println("inside the if");
                //System.out.println("TilePane child count: " + flowPane.getChildren().size());
                //System.out.println("TilePane pref height: " + flowPane.prefHeight(-1));
            }

        });
        //      System.out.println("After TilePane pref height: " + flowPane.getPrefHeight());
    }

    private void playScaleTransitionOn(Node node) {
        if (node instanceof ToggleButton || node instanceof Button) {
            ScaleTransition st = new ScaleTransition(Duration.millis(250), node);
            st.setFromX(1.2);
            st.setFromY(1.2);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        }
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

    public ToggleButton prepareToggleButton(String tag, TilePane pane) {
        ToggleButton tagButton = new ToggleButton(tag);
        tagButton.getStyleClass().add("tag-button");

        ContextMenu contextMenu = new ContextMenu();

        MenuItem deleteItem = new MenuItem("🗑️ Delete");
        deleteItem.setOnAction(_ -> pane.getChildren().remove(tagButton));

        MenuItem moveItem = new MenuItem("Move to other tag zone");
        moveItem.setOnAction(_ -> moveTagToOtherZone(tagButton));

        MenuItem renameItem = new MenuItem("✏️ Rename");
        renameItem.setOnAction(_ -> renameTag(tag, tagButton));

        MenuItem addNewItem = new MenuItem("Add New Tag");
        addNewItem.setOnAction(_ -> addNewTagToThisZone(tagButton));

        contextMenu.getItems().addAll(deleteItem, renameItem, moveItem, addNewItem);
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
            if (tagButton.getParent() instanceof TilePane flowPane) {
                System.out.println("Renaming tag");
                flowPane.getChildren().remove(tagButton);
                addTag(newName, flowPane);
                tagButton.setText(newName);
            }
        });
    }


    private void addNewTagToThisZone(ToggleButton tagButton) {

        if (tagButton.getParent() instanceof TilePane flowPane) {
            addTag(getDefaultTagNameRandomized(), flowPane);
        }
    }

    private String getDefaultTagNameRandomized() {
        Random random = new Random();
        return DEFAULT_TAG_NAME + random.nextInt();
    }

    private void moveTagToOtherZone(ToggleButton tagButton) {
        String tagText = tagButton.getText();

        TilePane source = (TilePane) tagButton.getParent();
        TilePane target = (source == ID_flow_pane_low_tags) ? ID_flow_pane_high_tags : ID_flow_pane_low_tags;

        boolean alreadyExists = target.getChildren().stream()
                .filter(node -> node instanceof ToggleButton)
                .map(node -> ((ToggleButton) node).getText())
                .anyMatch(text -> text.equals(tagText));

        // Always remove from source
        //System.out.println("Inside moveToAnotherZone");
        //System.out.println("A = "+ source.getId());
        //System.out.println("B = "+ target.getId());

        source.getChildren().remove(tagButton);

        // Only add if not yet in target (by text)
        if (!alreadyExists) {
            target.getChildren().add(tagButton);
        }
        System.out.println("Moved a toggleButton to another tag zone");
    }


// Add action methods if needed later
// public void onSaveTagClick() { ... }
// public void onSaveZipClick() { ... }

    public void saveTag() {
        System.out.println("HelloController getID_button_save_tagClick");
        //System.out.println(getTagNamesCSV(ID_flow_pane_high_tags));
        //System.out.println(getTagNamesCSV(ID_flow_pane_low_tags));

        /*
        String highCsv = getTagNamesCSV(ID_flow_pane_high_tags).trim();
        String lowCsv = getTagNamesCSV(ID_flow_pane_low_tags).trim();

        String combinedCsv;
        if (!highCsv.isEmpty() && !lowCsv.isEmpty()) {
            combinedCsv = highCsv + ", " + lowCsv;
        } else {
            combinedCsv = highCsv + lowCsv; // one of them is empty
        }
         */
/*
        List<String> filteredTagNames = getTagButtons(ID_flow_pane_high_tags)
                .map(b -> buttonToKeep(b, FilterTagCollecting.ONLY_ACTIVE))
                .filter(Objects::nonNull)
                .map(b -> buttonToKeep(b, FilterTagCollecting.ONLY_WITH_NAME))
                .filter(Objects::nonNull)
                .map(ToggleButton::getText)
                .toList();
*/
        String highCsv = toCsvTTrimmed(( getTagButtons(ID_flow_pane_high_tags)
                .map(b -> buttonToKeep(b, FilterTagCollecting.ONLY_ACTIVE))
                .filter(Objects::nonNull)
                .map(b -> buttonToKeep(b, FilterTagCollecting.ONLY_WITH_NAME))
                .filter(Objects::nonNull)
                .map(ToggleButton::getText)
                .toList()));

        String lowCsv = toCsvTTrimmed(( getTagButtons(ID_flow_pane_low_tags)
                .map(b -> buttonToKeep(b, FilterTagCollecting.ONLY_ACTIVE))
                .filter(Objects::nonNull)
                .map(b -> buttonToKeep(b, FilterTagCollecting.ONLY_WITH_NAME))
                .filter(Objects::nonNull)
                .map(ToggleButton::getText)
                .toList()));

        String combinedCsv;
        if (!highCsv.isEmpty() && !lowCsv.isEmpty()) {
            combinedCsv = highCsv + ", " + lowCsv;
        } else {
            combinedCsv = highCsv + lowCsv; // one of them is empty
        }

        String outputPath = ID_label_file_path.getText().trim();
        String projectName = ID_label_project_name.getText().trim();
        File file = new File(outputPath, projectName + ".txt");

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println(combinedCsv);
            System.out.println("Saved tag file to: " + file.getAbsolutePath());
            System.out.println("Content " + combinedCsv);
        } catch (IOException e) {
            System.err.println("Failed to save tag file: " + e.getMessage());
        }

    }

    private void ensureAtLeastOneTagIsPresent(TilePane flowPane) {
        if (flowPane.getChildren().isEmpty()) {
            addTag(getDefaultTagNameRandomized(), flowPane);
        }
    }

    private List<String> getTagNames(Pane pane) {
        return pane.getChildren().stream()
                .filter(node -> node instanceof ToggleButton)
                .map(node -> ((ToggleButton) node).getText())
                .collect(Collectors.toList());
    }

    private Stream<ToggleButton> getTagButtons(Pane pane) {
        return pane.getChildren().stream()
                .filter(node -> node instanceof ToggleButton)
                .map(node -> (ToggleButton) node); // 💡 cast to ToggleButton
    }



    private String toCsvTTrimmed(List<String> tags) {
        return String.join(", ", tags).trim();
    }

    private String getTagNamesCSV(Pane pane) {
        return toCsvTTrimmed(getTagNames(pane));
    }


    // ********************************************* implement this to do in the save method
    public ToggleButton buttonToKeep(ToggleButton button, FilterTagCollecting filter) {
        boolean isSelected = button.isSelected();
        String name = button.getText().trim();

        return switch (filter) {
            case ONLY_ACTIVE -> isSelected ? button : null;
            case ONLY_INACTIVE -> !isSelected ? button : null;
            case ONLY_WITH_NAME -> (!name.isEmpty() && !name.toUpperCase().startsWith("RENAME ME")) ? button : null;
            case ONLY_WITHOUT_NAME -> (name.isEmpty() || name.toUpperCase().startsWith("RENAME ME")) ? button : null;
            case ALL -> button;
        };
    }


}


// File: HelloApplication.java (JavaFX Entry Point)



package com.doebi.tools.loratagmate;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

public class HelloApplication extends Application {

    private static final String RESOURCE_PATH = "/com/doebi/tools/loratagmate";

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource(RESOURCE_PATH + "/fxml/hello-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 800, 800);

        // Load CSS
        try {
            String cssPath = HelloApplication.class
                    .getResource(RESOURCE_PATH + "/css/style.css")
                    .toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("❌ Failed to load CSS: " + e.getMessage());
        }

        // Load Icon
        try {
            InputStream iconStream = Objects.requireNonNull(
                    getClass().getResourceAsStream(RESOURCE_PATH + "/images/tagmate.png"));
            stage.getIcons().add(new Image(iconStream));
        } catch (Exception e) {
            System.err.println("❌ Failed to load icon: " + e.getMessage());
        }

        // Final setup
        stage.setTitle("");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}





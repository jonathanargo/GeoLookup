package com.jargo.geolookup;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class GeoLookup extends Application {

    public static final Logger LOGGER = Logger.getLogger(WorkbookProcessor.class.getName());
    
    @Override
    public void start(Stage stage) throws Exception {
        
        Handler fileHandler = null;
        try {
            fileHandler = new FileHandler("./geolookup.log");

            LOGGER.addHandler(fileHandler);

            fileHandler.setLevel(Level.ALL);
            LOGGER.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());

            LOGGER.info("Logging initialized");

        } catch (IOException exception) {
            Output.handle("Failed to initialize logging.", Alert.AlertType.ERROR);
        }
        
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/GeoLookup.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        stage.setTitle("GeoLookup");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}

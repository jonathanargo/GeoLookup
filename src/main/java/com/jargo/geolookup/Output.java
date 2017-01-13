
package com.jargo.geolookup;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Output {
    
    public static void handle(String message) {
        handle(message, AlertType.INFORMATION);        
    }
    
    public static void handle(String message, AlertType alertType) {
        String title = "Message";
        if (alertType == AlertType.ERROR) {
            title = "Error";
        } else if (alertType == AlertType.WARNING) {
            title = "Warning";
        }
        handle(message, title, alertType);       
    }
    
    public static void handle(String message, String title) {
        handle(message, title, AlertType.INFORMATION);
    }
    
    public static void handle(String message, String title, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(message);
        alert.showAndWait();
    }

}

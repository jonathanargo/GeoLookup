
package com.jargo.geolookup;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Output {
    
    public static void handle(String message) {
        handle(message, AlertType.INFORMATION);        
    }
    
    public static void handle(String message, AlertType alertType) {
        handle(message, "Message", alertType);       
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

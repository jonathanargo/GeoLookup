
package com.jargo.geolookup;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Output {
    
    public static List<OutputQueueItem> outputQueue = new ArrayList();
    
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
        GeoLookup.LOGGER.info(message);
        
        outputQueue.add(new OutputQueueItem(message, title, alertType));
    }
    
    public static void flush() {
        for (OutputQueueItem queueItem : outputQueue) {
            Alert alert = queueItem.makeAlert();
            alert.showAndWait();
        }
        
        outputQueue.clear();
    }
    
    private static class OutputQueueItem {
        private String _message;
        private String _title;
        private AlertType _alertType;
        
        public OutputQueueItem(String message, String title, AlertType alertType) {
            _message = message;
            _title = title;
            _alertType = alertType;
        }
        
        public Alert makeAlert() {
            Alert alert = new Alert(_alertType);
            alert.setTitle(_title);
            alert.setHeaderText("");
            alert.setContentText(_message);
            
            return alert;
        }
    }
}



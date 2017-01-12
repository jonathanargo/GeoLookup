package com.jargo.geolookup;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 *
 * @author jargo
 */
public class GeoLookupController implements Initializable {

    @FXML
    private Button lookupButton;
    @FXML
    private TextField addressField;    
    @FXML
    private Label outputLabel;
    
     @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    
    @FXML
    private void lookupSingleAddress(ActionEvent event) {
        LookupConnector lookupConnector = new LookupConnector();
        lookupConnector.setAddress(addressField.getText());
        lookupConnector.setKey(this.getKey()); // Get key from file for the time being
        String output = lookupConnector.makeRequest();
    }
    
    protected String getKey()
    {
        String returnVal = "";
        
        try {
            FileReader fReader = new FileReader("key.txt");
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            while ((line = bReader.readLine()) != null) {
                returnVal += line;
            }

        } catch (FileNotFoundException ex) {
            System.out.println("Could not find file.");
            
        } catch (IOException ex) {
            System.out.println("IOException");
            
        }
        return returnVal;
    }
    
}

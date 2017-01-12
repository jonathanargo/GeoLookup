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
import javafx.scene.control.Alert.AlertType;
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
        ApiResponse apiResponse = ApiResponseBuilder.buildApiResponse(lookupConnector);
        
        if (apiResponse.getResults().size() > 1) {
            Output.handle("More than one result was returned for the address.", AlertType.ERROR);            
        }
        else {            
            ApiResponseResult result = apiResponse.getResults().get(0);
            
            String newLine = System.getProperty("line.separator");
            String outputText;
            
            outputText = "Status: " + apiResponse.getStatus() + newLine;
            outputText += "Formatted address: " + result.getFormattedAdddress() + newLine;
            outputText += "Location: "+result.getFormattedDegrees();

            Output.handle(outputText, "Result", AlertType.INFORMATION);
        }
        
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

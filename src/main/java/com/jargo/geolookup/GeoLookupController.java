package com.jargo.geolookup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ini4j.Wini;

/**
 *
 * @author jargo
 */
public class GeoLookupController implements Initializable {

    @FXML
    private Button selectFileButton;
    @FXML
    private Button okButton;
    @FXML
    private Button closeButton;
    @FXML
    private TextField singleAddressField;
    @FXML
    private TextField selectedFilePath;
    @FXML
    private RadioButton singleAddressRadioButton;
    @FXML
    private RadioButton fileImportRadioButton;
    
    private File selectedFile;
    private Wini options;
    
     @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Ensure that our radio buttons are initialized in the correct state.
        toggleInputType();
        
        options = new Wini();
        try {
            options = new Wini(new FileReader(new File("settings.ini")));
        } catch (IOException ex) {
            Output.handle("Failed to read settings.ini file.", AlertType.ERROR);            
        }
        
        String key = options.get("settings", "key");
        System.out.println(key);
        if (!key.isEmpty()) {
            if (key.compareToIgnoreCase("{your key here}") == 0) {
                Output.handle("Please add your API key to settings.ini", AlertType.ERROR);
            }            
        }
        
    }
    
    @FXML
    private void selectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        
        List<String> extensionList = new ArrayList();
        extensionList.add("*.xls");
        extensionList.add("*.xlsx");
        ExtensionFilter exFilter = new ExtensionFilter("XLS or XLSX", extensionList);
        fileChooser.setSelectedExtensionFilter(exFilter);
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            selectedFilePath.setText(selectedFile.getPath());            
        }
    }
    
    @FXML
    private void handleInputTypeChange(ActionEvent event) {
        toggleInputType();        
    }
    
    @FXML
    private void handleOkAction(ActionEvent event) {
        if (singleAddressRadioButton.isSelected()) {
            lookupSingleAddress(event);
        } else if (fileImportRadioButton.isSelected()) {
            lookupFromFile(event);
        }
    }
    
    @FXML
    private void handleCloseAction(ActionEvent event) {
        // TODO       
    }
    
    private void toggleInputType() {
        if (singleAddressRadioButton.isSelected()) {
            selectFileButton.setDisable(true);
            singleAddressField.setDisable(false);

        } else if (fileImportRadioButton.isSelected()) {
            selectFileButton.setDisable(false);
            singleAddressField.setDisable(true);
        }        
    }
    
    private void lookupSingleAddress(ActionEvent event) {
        // Initialize the connector and pass it to the response builder
        LookupConnector lookupConnector = new LookupConnector();
        lookupConnector.setAddress(singleAddressField.getText());
        lookupConnector.setKey(this.getKey()); // Get key from file for the time being
        ApiResponse apiResponse = ApiResponseBuilder.buildApiResponse(lookupConnector);
        
        if (apiResponse.getResults().size() > 1) {
            // Technically this is valid output, but it's not going to be handled for our purposes
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
    
    private void lookupFromFile(ActionEvent event) {
        if (selectedFile == null) {
            Output.handle("You must select a file.", AlertType.ERROR);
        } else if (!selectedFile.canRead()) {
            Output.handle("Unable to read selected file.", AlertType.ERROR);            
        }
        
        try {
            FileInputStream inputStream = new FileInputStream(selectedFile);
            Workbook wb = null;
            if (selectedFile.getPath().endsWith(".xls")) {
                wb = new HSSFWorkbook(inputStream);
            } else if (selectedFile.getPath().endsWith(".xlsx")) {
                wb = new XSSFWorkbook(inputStream);
            }
            
            if (wb != null) {
                List<String> addresses = parseInputFile(wb);                
                makeLookups(addresses);
            } else {
                Output.handle("Failed to read workbook.", AlertType.ERROR);
            }
        } catch (FileNotFoundException ex) {
            Output.handle("Selected input file not found.", AlertType.ERROR);
        } catch (IOException ex) {
            Output.handle("Failed to read input file: "+ex.getMessage(), AlertType.ERROR);
        }
    }
    
    protected List<String> parseInputFile(Workbook wb) {
        List<String> addresses = new ArrayList<>();
        
        Boolean headersSet = false;
        
        int fullAddressKey = -1;
        int streetKey = -1;
        int cityKey = -1; 
        int stateKey = -1;
        int zipKey = -1;
        
        String fullAddressFieldName = options.get("settings", "fullAddressField");
        String streetFieldName = options.get("settings", "streetField");
        String cityFieldName = options.get("settings", "cityField");
        String stateFieldName = options.get("settings", "stateField");
        String zipFieldName = options.get("settings", "zipField");
        
        String fullAddress = null;
        String street = null;
        String city = null;
        String state = null;
        String zip = null;
        
        // Go through our spreadsheet and parse the results.
        try {
            for (Sheet sheet : wb) {
                for (Row row : sheet) {
                    if (!headersSet) {
                        // This is the header row, so lets match the fields to array keys.
                        int i = 0;
                        for (Cell cell : row) {
                            // Does this field name match one of our input fields? If so, keep track of the key
                            String cellValue = cell.getStringCellValue();
                            if (cellValue.compareToIgnoreCase(fullAddressFieldName) == 0) {
                                fullAddressKey = i;
                            } else if (cellValue.compareToIgnoreCase(streetFieldName) == 0) {
                                streetKey = i;
                            } else if (cellValue.compareToIgnoreCase(cityFieldName) == 0) {
                                cityKey = i;
                            } else if (cellValue.compareToIgnoreCase(stateFieldName) == 0) {
                                stateKey = i;
                            } else if (cellValue.compareToIgnoreCase(zipFieldName) == 0) {
                                zipKey = i;
                            }
                            i++;
                        }

                        if (fullAddressKey == -1 && (streetKey == -1 && cityKey == -1 && stateKey == -1 && zipKey == -1)) {
                            Output.handle("Failed to find appropriate fields in input file.", AlertType.ERROR);
                            return null;
                        }
                        headersSet = true;
                    } else {
                        // Assemble the address and add it to the list
                        if (fullAddressKey != -1) {
                            fullAddress = row.getCell(fullAddressKey).getStringCellValue();
                        } else {
                            street = row.getCell(streetKey).getStringCellValue();
                            city = row.getCell(cityKey).getStringCellValue();
                            state = row.getCell(stateKey).getStringCellValue();

                            // Convert the zip code cell to a string before retrieving it
                            row.getCell(zipKey).setCellType(CellType.STRING);
                            zip = row.getCell(zipKey).getStringCellValue();

                            fullAddress = street + " " + city + " " + state + " " + zip;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Output.handle("An unknown error occurred while parsing input file: "+ex.getMessage());
            return null;
        }
        return addresses;
    }
    
    protected void makeLookups(List<String> addresses) {
        for (String address : addresses) {
            LookupConnector lookupConnector = new LookupConnector();
            lookupConnector.setAddress(address);
            lookupConnector.setKey(this.getKey());
            ApiResponse apiResponse = ApiResponseBuilder.buildApiResponse(lookupConnector);
            
            List<String> badAddresses = new ArrayList();
            if (apiResponse.getResults().size() > 1 || apiResponse.getStatus().compareToIgnoreCase("OK") != 0) {
                badAddresses.add(address);
            } else {
                ApiResponseResult result = apiResponse.getResults().get(0);

                String newLine = System.getProperty("line.separator");
                String outputText;

                outputText = "Status: " + apiResponse.getStatus() + newLine;
                outputText += "Formatted address: " + result.getFormattedAdddress() + newLine;
                System.out.println(address+" - "+result.getFormattedDegrees());
            }
        }
    }
    
    protected String getKey() {
        String returnVal = "";
        
        String key = options.get("settings", "key");
        if (key.isEmpty()) {
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
        } else {
            returnVal = key;
        }
        
        return returnVal;
    }
    
}

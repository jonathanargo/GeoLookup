package com.jargo.geolookup;

import com.sun.javafx.tk.FileChooserType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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


public class GeoLookupController implements Initializable {

    @FXML
    private Button selectFileButton;
    @FXML
    private Button okButton;
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
    private List<String> badAddresses;
    
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
        if (!key.isEmpty()) {
            if (key.compareToIgnoreCase("{your key here}") == 0) {
                Output.handle("Please add your API key to settings.ini", AlertType.ERROR);
            }            
        }
        
        badAddresses = new ArrayList();        
    }
    
    @FXML
    private void selectFile(ActionEvent event) {
        selectedFile = selectSpreadsheetFile(FileChooserType.OPEN, "Select Input File");
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
        // Disable the buttons while we parse the file
        okButton.setDisable(true);
        
        // Keep track of the original state of this button
        Boolean selectFileButtonDisabled = selectFileButton.isDisable();
        
        if (singleAddressRadioButton.isSelected()) {
            lookupSingleAddress(event);
        } else if (fileImportRadioButton.isSelected()) {
            lookupFromFile(event);
        }
        
        // Reset to initial value
        selectFileButton.setDisable(selectFileButtonDisabled);
        okButton.setDisable(false);
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
                parseInputFile(wb);
                try {
                    //Write the result
                    File outputFile = selectSpreadsheetFile(FileChooserType.SAVE, "Save Result");
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    wb.write(outputStream);
                    outputStream.close();
                } catch (IOException ex) {
                    Output.handle("Failed to write output file: " + ex.getMessage(), AlertType.ERROR);
                }
                
                if (!badAddresses.isEmpty()) {
                    String newLine = System.getProperty("line.separator");
                    String badAddressWarning = "The following addresses were unable to be converted:" + newLine;
                    for (String badAddress : badAddresses) {
                        badAddressWarning += badAddress + newLine;
                    }

                    Output.handle(badAddressWarning, AlertType.WARNING);
                }
            } else {
                Output.handle("Failed to read workbook.", AlertType.ERROR);
            }            
            inputStream.close();
        } catch (FileNotFoundException ex) {
            Output.handle("Selected input file not found.", AlertType.ERROR);
        } catch (IOException ex) {
            Output.handle("Failed to read input file: "+ex.getMessage(), AlertType.ERROR);
        }
    }
    
    protected void parseInputFile(Workbook wb) {
        List<String> addresses = new ArrayList<>();
        
        Boolean headersSet = false;
        
        // Input keys
        int fullAddressKey = -1;
        int streetKey = -1;
        int cityKey = -1; 
        int stateKey = -1;
        int zipKey = -1;
        
        // Output keys
        int latKey = -1;
        int lngKey = -1;
        int fullLocationKey = -1;        
        
        String fullAddressFieldName = options.get("settings", "fullAddressField");
        String streetFieldName = options.get("settings", "streetField");
        String cityFieldName = options.get("settings", "cityField");
        String stateFieldName = options.get("settings", "stateField");
        String zipFieldName = options.get("settings", "zipField");
        
        String fullAddress;
        String street;
        String city;
        String state;
        String zip;
        
        // Go through our spreadsheet and parse the results.
        try {
            for (Sheet sheet : wb) {
                for (Row row : sheet) {
                    if (!headersSet) {
                        // This is the header row, so lets match the fields to array keys.
                        int i = 0;
                        for (Cell cell : row) {
                            // Does this field name match one of our input fields? If so, keep track of the key
                            // Probably a better way to do this...
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
                            return;
                        }
                        
                        // Get the output field headers
                        String latOutputField = options.get("settings", "latOutputField");
                        String lngOutputField = options.get("settings", "lngOutputField");
                        String fullLocationOutputField = options.get("settings", "fullLocationOutputField");
                        
                        // Add headers as necessary
                        if (!latOutputField.isEmpty()) {
                            latKey = row.getLastCellNum();
                            row.createCell(latKey, CellType.STRING).setCellValue(latOutputField);
                        }                        
                        if (!lngOutputField.isEmpty()) {
                            lngKey = row.getLastCellNum();
                            row.createCell(lngKey, CellType.STRING).setCellValue(lngOutputField);
                        }                        
                        if (!fullLocationOutputField.isEmpty()) {
                            fullLocationKey = row.getLastCellNum();
                            row.createCell(fullLocationKey, CellType.STRING).setCellValue(fullLocationOutputField);
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
                        
                        ApiResponseResult responseResult = lookupAddress(fullAddress);
                        if (responseResult != null) {
                            // This result is going in our output. Add new columns as necessary.
                            // Todo - support overwriting file
                            String lat = String.valueOf(responseResult.getGeometry().getLocation().getLat());
                            String lng = String.valueOf(responseResult.getGeometry().getLocation().getLng());
                            
                            if (latKey != -1) {
                                row.createCell(latKey, CellType.STRING).setCellValue(lat);
                            }                            
                            if (lngKey != -1) {
                                row.createCell(lngKey, CellType.STRING).setCellValue(lng);
                            }
                            if (fullLocationKey != -1) {
                                row.createCell(fullLocationKey, CellType.STRING).setCellValue(lat+","+lng);
                            }
                        } else {
                            // We'll deal with these later
                            badAddresses.add(fullAddress);
                        }
                    }
                }
            }            
        } catch (Exception ex) {
            Output.handle("An unknown error occurred while parsing input file: "+ex.getMessage());
        }
    }
    
    protected ApiResponseResult lookupAddress(String address) {
        ApiResponseResult result = null;
        
        LookupConnector lookupConnector = new LookupConnector();
        lookupConnector.setAddress(address);
        lookupConnector.setKey(this.getKey());
        ApiResponse apiResponse = ApiResponseBuilder.buildApiResponse(lookupConnector);

        if (apiResponse.getResults().size() == 1 || apiResponse.getStatus().compareToIgnoreCase("OK") == 0) {
            result = apiResponse.getResults().get(0);
        }
        
        return result;
    }
    
    protected File selectSpreadsheetFile(FileChooserType type, String title) {        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        
        File result = null;
        if (type == FileChooserType.OPEN) {
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Microsoft Excel Spreadsheet", "*.xls", "*.xlsx"));
            result = fileChooser.showOpenDialog(null);            
        } else if (type == FileChooserType.SAVE) {
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("Microsoft Excel 2007-2013 XML (.xlsx)", "*.xlsx"),
                    new ExtensionFilter("Microsoft Excel 97-2003 (.xls)", "*.xls")
            );            
            result = fileChooser.showSaveDialog(null);
        }
        
        return result;
    }
    
    protected String getKey() {
        String returnVal = "";
        
        String key = options.get("settings", "key");
        if (key.isEmpty()) {
            File keyFile = new File("key.txt");
            if (keyFile.canRead()) {
                try {
                    FileReader fReader = new FileReader("key.txt");
                    BufferedReader bReader = new BufferedReader(fReader);
                    String line;
                    while ((line = bReader.readLine()) != null) {
                        returnVal += line;
                    }
                } catch (FileNotFoundException ex) {
                    Output.handle("Could not find file 'key.txt'", AlertType.ERROR);
                } catch (IOException ex) {
                    Output.handle("An error occurred while reading the key file: " + ex.getMessage(), AlertType.ERROR);
                }
            }            
        } else {
            returnVal = key;
        }
        
        return returnVal;
    }
    
}

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
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
    private Stage pleaseWaitDialog = new Stage();
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
        
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(GeoLookup.class.getResource("/fxml/PleaseWait.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setResizable(false);
            stage.setTitle("Please Wait");
            stage.setScene(new Scene(root));
            this.pleaseWaitDialog = stage;
        } catch (IOException ex) {
            Output.handle("Failed to initialize dialog.", AlertType.ERROR);
        }
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
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {
                // Initialize the connector and pass it to the response builder
                LookupConnector lookupConnector = new LookupConnector();
                lookupConnector.setAddress(singleAddressField.getText());
                lookupConnector.setKey(options.get("settings", "key"));
                ApiResponse apiResponse = ApiResponseBuilder.buildApiResponse(lookupConnector);

                if (apiResponse.getResults().size() > 1) {
                    // Technically this is valid output, but it's not going to be handled for our purposes
                    Output.handle("More than one result was returned for the address.", AlertType.ERROR);
                } else {
                    ApiResponseResult result = apiResponse.getResults().get(0);

                    String newLine = System.getProperty("line.separator");
                    String outputText;

                    outputText = "Status: " + apiResponse.getStatus() + newLine;
                    outputText += "Formatted address: " + result.getFormattedAdddress() + newLine;
                    outputText += "Location: " + result.getFormattedDegrees();

                    Output.handle(outputText, "Result", AlertType.INFORMATION);
                }
                
                return null;
            }
        };
        
        task.setOnRunning(e-> {
            pleaseWaitDialog.show();
        });
        
        task.setOnSucceeded(e-> {
            GeoLookup.LOGGER.info("Single lookup succeeded.");
            pleaseWaitDialog.hide();
            Output.flush();
        });
        
        task.setOnFailed(e-> {
            GeoLookup.LOGGER.info("Single lookup failed.");
            Output.handle("Single lookup failed: "+task.getException().getMessage());
            pleaseWaitDialog.hide();
            Output.flush();
        });

        Thread thread = new Thread(task);
        thread.start();
    }
    
    private void lookupFromFile(ActionEvent event) {
        Task<Workbook> task = new Task<Workbook>() {
            @Override
            public Workbook call() throws InterruptedException {
                Workbook outputWorkbook = null;
                
                // Initialize the connector and pass it to the response builder
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
                        WorkbookProcessor wbProcessor = new WorkbookProcessor(wb, options);
                        outputWorkbook = wbProcessor.processWorkbook();
                        badAddresses = wbProcessor.getBadAddresses();
                        
                    } else {
                        Output.handle("Failed to read workbook.", AlertType.ERROR);
                    }
                    
                    inputStream.close();
                    return outputWorkbook;
                    
                } catch (FileNotFoundException ex) {
                    Output.handle("Selected input file not found.", AlertType.ERROR);
                } catch (IOException ex) {
                    Output.handle("Failed to read input file: " + ex.getMessage(), AlertType.ERROR);
                }

                return null;
            }
        };

        task.setOnRunning(e -> {
            pleaseWaitDialog.show();
        });

        task.setOnSucceeded(e -> {
            GeoLookup.LOGGER.info("File import succeeded.");
            pleaseWaitDialog.hide();
            
            Workbook outputWorkbook = (Workbook) e.getSource().getValue();
            
            try {
                //Write the result
                File outputFile = selectSpreadsheetFile(FileChooserType.SAVE, "Save Result");
                FileOutputStream outputStream = new FileOutputStream(outputFile);
                outputWorkbook.write(outputStream);
                outputStream.close();
                
                Output.handle("File written to '"+outputFile.getAbsolutePath()+"'.", "Success", AlertType.INFORMATION);
            } catch (IOException ex) {
                Output.handle("Failed to write output file: " + ex.getMessage(), AlertType.ERROR);
            }
            
            if (!badAddresses.isEmpty()) {
                String badAddressWarning;
                if (badAddresses.size() > 10) {
                    badAddressWarning = badAddresses.size() + " addresses were unable to be converted. Please see log for details.";
                } else {
                    String newLine = System.getProperty("line.separator");
                    badAddressWarning = "The following addresses were unable to be converted:" + newLine;
                    for (String badAddress : badAddresses) {
                        badAddressWarning += badAddress + newLine;
                    }
                }

                Output.handle(badAddressWarning, AlertType.WARNING);
            }
            Output.flush();
        });

        task.setOnFailed(e -> {
            GeoLookup.LOGGER.info("File import failed.");
            Output.handle("File import failed: " + task.getException().getMessage());
            pleaseWaitDialog.hide();
            Output.flush();
        });

        Thread thread = new Thread(task);
        thread.start();
    }
    
    protected File selectSpreadsheetFile(FileChooserType type, String title) {        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory(new File("."));        
        
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

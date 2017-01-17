package com.jargo.geolookup;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.ini4j.Wini;

public class WorkbookProcessor {
    
    private Workbook wb;
    private List<String> badAddresses;
    private Wini options;
    
    public WorkbookProcessor(Workbook wb, Wini options) {
        this.wb = wb;
        this.options = options;
        this.badAddresses = new ArrayList();
    }
    
    public Workbook processWorkbook() {
        GeoLookup.LOGGER.info("Beginning to parse workbook.");
        
        System.out.println("Starting log processing.");
        badAddresses = new ArrayList();
        
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
                            Output.handle("Failed to find appropriate fields in input file.", Alert.AlertType.ERROR);
                            return wb;
                        }

                        // Get the output field headers
                        String latOutputField = options.get("settings", "latOutputField");
                        String lngOutputField = options.get("settings", "lngOutputField");
                        String fullLocationOutputField = options.get("settings", "fullLocationOutputField");
                        
                        System.out.println("latOutputField: "+latOutputField);
                        System.out.println("lngOutputField: "+lngOutputField);
                        System.out.println("fullLocationOutputField: "+fullLocationOutputField);

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
                        
                        System.out.println("Parsed address: "+fullAddress);

                        ApiResponseResult responseResult = AddressLookupProcessor.lookupAddress(fullAddress, options.get("settings", "key"));
                        if (responseResult != null) {
                            // This result is going in our output. Add new columns as necessary.
                            // Todo - support overwriting file
                            String lat = String.valueOf(responseResult.getGeometry().getLocation().getLat());
                            String lng = String.valueOf(responseResult.getGeometry().getLocation().getLng());

                            if (latKey != -1) {
                                System.out.println("Writing lat '"+lat+"' to cell "+latKey);
                                row.createCell(latKey, CellType.STRING).setCellValue(lat);
                            }
                            if (lngKey != -1) {
                                System.out.println("Writing lng '"+lng+"' to cell "+lngKey);
                                row.createCell(lngKey, CellType.STRING).setCellValue(lng);
                            }
                            if (fullLocationKey != -1) {
                                System.out.println("Writing full location '"+lat + "," + lng+"' to cell "+fullLocationKey);
                                row.createCell(fullLocationKey, CellType.STRING).setCellValue(lat + "," + lng);
                            }
                        } else {
                            // We'll deal with these later
                            badAddresses.add(fullAddress);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Output.handle("An unknown error occurred while parsing input file: " + ex.getMessage());
        }
        
        return wb;
    }
    
    public List getBadAddresses() {
        return badAddresses;
    }
}

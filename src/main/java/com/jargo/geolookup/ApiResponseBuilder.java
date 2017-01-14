package com.jargo.geolookup;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javafx.scene.control.Alert.AlertType;

public class ApiResponseBuilder {
    public static ApiResponse buildApiResponse(LookupConnector lookupConnector) {
        
        ApiResponse apiResponse = null;
        
        try (InputStream in = lookupConnector.makeRequest()) {
            String rawResponse = readRawResponse(in);
            Gson gson = new Gson();
            apiResponse = gson.fromJson(rawResponse, ApiResponse.class);
            apiResponse.setRawResponse(rawResponse);
        } catch (IOException ex) {
            Output.handle("Failed to read response.", AlertType.ERROR);
        }
        
        return apiResponse;
    }
    
    protected static String readRawResponse(InputStream in) {
        String rawResponse = "";
        
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String newLine = System.getProperty("line.separator");

        try {
            String line;
            while ((line = br.readLine()) != null) {
                rawResponse += line + newLine;
            }

        } catch (IOException ex) {
            Output.handle("Failed to read raw response data.", AlertType.ERROR);
            rawResponse = null;
        }
        
        return rawResponse;
    }
}
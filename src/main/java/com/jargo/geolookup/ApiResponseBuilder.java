package com.jargo.geolookup;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ApiResponseBuilder {
    public static ApiResponse buildApiResponse(LookupConnector lookupConnector) {
        
        InputStream in = lookupConnector.makeRequest();
        String rawResponse = readRawResponse(in);        
        
        Gson gson = new Gson();
        ApiResponse apiResponse = gson.fromJson(rawResponse, ApiResponse.class);
        apiResponse.setRawResponse(rawResponse);
        
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
            System.out.println("Failed to read raw response data: " + ex.getMessage());
            rawResponse = null;
        }
        
        return rawResponse;
    }
}
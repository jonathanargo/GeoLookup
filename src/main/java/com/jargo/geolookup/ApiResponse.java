/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jargo.geolookup;

import java.util.List;

/**
 *
 * @author jonat
 */
public class ApiResponse {
    private String status;
    private List<ApiResponseResult> results;
    private String rawResponse;

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
    
    public String getStatus()
    {
        return status;
    }
    
    public List<ApiResponseResult> getResults() {
        return results;
    }
}

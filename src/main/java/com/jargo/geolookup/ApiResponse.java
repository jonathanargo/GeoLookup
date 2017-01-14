
package com.jargo.geolookup;

import java.util.List;

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

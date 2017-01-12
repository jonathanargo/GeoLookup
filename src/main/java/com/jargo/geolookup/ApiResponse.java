/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jargo.geolookup;

import java.util.List;

/**
 *
 * @author jargo
 */
public class ApiResponse {
    
    private String _status;
    private String _rawResponse;
    private List<ApiResponseResult> _results;

    public String getStatus() {
        return _status;
    }

    public void setStatus(String status) {
        this._status = status;
    }

    public List<ApiResponseResult> getResults() {
        return _results;
    }

    public void setResults(List<ApiResponseResult> results) {
        this._results = results;
    }
    
    public void addResult(ApiResponseResult result) {
        _results.add(result);
    }
    
    public void setRawResponse(String rawResponse) {
        this._rawResponse = rawResponse;
    }
    
    public String getRawResponse() {
        return _rawResponse;
    }
    
    @Override
    public String toString() {
        return _status;        
    }
}

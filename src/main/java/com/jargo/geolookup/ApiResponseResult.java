package com.jargo.geolookup;

public class ApiResponseResult {
    private String formatted_address;
    private ApiResponseResultGeometry geometry;
    
    public String getFormattedAdddress() {
        return formatted_address;
    }
    
    public ApiResponseResultGeometry getGeometry() {
        return geometry;
    }
    
    public String getFormattedDegrees() {
        return getGeometry().getLocation().getLat() + "," + getGeometry().getLocation().getLng();
    }
}
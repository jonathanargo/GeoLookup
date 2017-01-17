package com.jargo.geolookup;

public class AddressLookupProcessor {    
    public static ApiResponseResult lookupAddress(String address, String key) {
        
        ApiResponseResult result = null;

        LookupConnector lookupConnector = new LookupConnector();
        lookupConnector.setAddress(address);
        lookupConnector.setKey(key);
        ApiResponse apiResponse = ApiResponseBuilder.buildApiResponse(lookupConnector);

        if (apiResponse.getResults().size() == 1 || apiResponse.getStatus().compareToIgnoreCase("OK") == 0) {
            result = apiResponse.getResults().get(0);
        } else {
            String newLine = System.getProperty("line.separator");
            GeoLookup.LOGGER.warning("Bad address encountered. Response: "+newLine+apiResponse.getRawResponse());
        }
        
        // We need to sleep for a quarter of a second to ensure that we don't exceed the lookup limit
        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            GeoLookup.LOGGER.severe("Failed to complete sleep.");
        }
        

        return result;
    }
    
}

package com.jargo.geolookup;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.http.client.utils.URIBuilder;

/**
 *
 * @author jargo
 */
public class LookupConnector {

    private String _address;
    private String _key;
    private String _urlBase = "https://maps.googleapis.com/maps/api/geocode/json";

    public String getAddress() {
        return _address;
    }

    public void setAddress(String address) {
        this._address = address;
    }

    public String getKey() {
        return _key;
    }

    public void setKey(String key) {
        this._key = key;
    }

    public String makeRequest() {
        try {
            // Build the URI
            URIBuilder uriBuilder = new URIBuilder(_urlBase);
            uriBuilder.addParameter("address", _address);
            uriBuilder.addParameter("key", _key);
            String uri = uriBuilder.toString();

            URL url = new URL(uri);
            URLConnection connection = url.openConnection();
            InputStream in = connection.getInputStream();
            
            
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
            
            System.out.println("Raw Response: "+rawResponse);
            
            Gson gson = new Gson();
//            gson.fromJson(, classOfT)

            
            
//            ApiResponse response = ApiResponseBuilder.buildApiResponse(in);
//            System.out.println(response.getStatus());

        } catch (MalformedURLException ex) {
            System.out.println("Bad URL detected.");

        } catch (IOException ex) {
            System.out.println("Unable to establish connection to API.");
            
        } catch (URISyntaxException ex) {
            System.out.println("Bad URI syntax.");
        }

        return null;
    }

}

package com.jargo.geolookup;

import java.io.IOException;
import java.io.InputStream;
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

    public InputStream makeRequest() {
        try {
            // Build the URI
            URIBuilder uriBuilder = new URIBuilder(_urlBase);
            uriBuilder.addParameter("address", _address);
            uriBuilder.addParameter("key", _key);
            String uri = uriBuilder.toString();

            URL url = new URL(uri);
            URLConnection connection = url.openConnection();
            return connection.getInputStream();

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

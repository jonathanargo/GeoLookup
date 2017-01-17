package com.jargo.geolookup;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import javafx.scene.control.Alert.AlertType;
import org.apache.http.client.utils.URIBuilder;

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
            
            GeoLookup.LOGGER.info("Making call to "+uri);

            URL url = new URL(uri);
            URLConnection connection = url.openConnection();
            return connection.getInputStream();

        } catch (MalformedURLException ex) {
            Output.handle("Bad URL detected.", AlertType.ERROR);
        } catch (IOException ex) {
            Output.handle("Unable to establish connection to API: "+ex.getMessage(), AlertType.ERROR);            
        } catch (URISyntaxException ex) {
            Output.handle("Bad URI syntax.", AlertType.ERROR);
        }

        return null;
    }

}

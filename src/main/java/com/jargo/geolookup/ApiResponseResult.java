/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jargo.geolookup;

/**
 *
 * @author jargo
 */
public class ApiResponseResult {
    private String _type;
    private String _address;
    private String _lat;
    private String _lng;

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        this._type = type;
    }

    public String getAddress() {
        return _address;
    }

    public void setAddress(String address) {
        this._address = address;
    }

    public String getLat() {
        return _lat;
    }

    public void setLat(String lat) {
        this._lat = lat;
    }

    public String getLng() {
        return _lng;
    }

    public void setLng(String lng) {
        this._lng = lng;
    }
}

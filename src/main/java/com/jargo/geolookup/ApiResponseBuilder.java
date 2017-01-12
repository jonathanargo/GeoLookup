/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jargo.geolookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author jargo
 */
public class ApiResponseBuilder {
    public static ApiResponse buildApiResponse(InputStream in) {
        ApiResponse response = new ApiResponse();
        response.setRawResponse(readRawResponse(in));
        
        Document doc = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(response.getRawResponse()));
            doc = dBuilder.parse(is);
            
        } catch (ParserConfigurationException ex) {
            System.out.println("Failed to parse response document.");
        } catch (IOException ex) {
            System.out.println("Failed to read response: "+ex.getMessage());
        } catch (SAXException ex) {
            System.out.println("Unknown error occurred: "+ex.getMessage());
        }

        if (doc != null)
        {
            Element root = doc.getDocumentElement();
            String content = doc.toString();
            response.setStatus(root.getElementsByTagName("status").item(0).getTextContent());
            
            NodeList results = root.getElementsByTagName("result");
            for (int i = 0; i < results.getLength(); i++) {
                System.out.println("Node "+i+": "+results.item(i).getTextContent());
//                response.addResult(buildResponseResult(results.item(i)));                
            }
            
        }
        
        return response;
    }
    
    protected static String readRawResponse(InputStream in) {
        String rawResponse = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String newLine = System.getProperty("line.separator");
        
        try {
            String line;
            while ((line = br.readLine()) != null) {
                rawResponse += line+newLine;            
            }
            
        } catch (IOException ex) {
            System.out.println("Failed to read raw response data: "+ex.getMessage());
            rawResponse = null;            
        }
        
        return rawResponse;
    }
    
    protected static ApiResponseResult buildResponseResult(Node responseNode) {
        ApiResponseResult result = null;
        
        
        
        return result;        
    }
}

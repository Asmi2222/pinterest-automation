package com.pinterest.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVReader {
    
    private static final String TEST_DATA_PATH = "testdata/TestData.csv";
    private static List<Map<String, String>> testData;
    
    // Load CSV data once when class is loaded
    static {
        testData = loadCSV();
    }
    
    /**
     * Load CSV file from resources folder
     */
    private static List<Map<String, String>> loadCSV() {
        List<Map<String, String>> data = new ArrayList<>();
        
        try {
            // Load from classpath (resources folder)
            InputStream inputStream = CSVReader.class.getClassLoader().getResourceAsStream(TEST_DATA_PATH);
            
            if (inputStream == null) {
                throw new RuntimeException("CSV file not found in resources: " + TEST_DATA_PATH);
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            String[] headers = null;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                if (isFirstLine) {
                    headers = line.split(",");
                    isFirstLine = false;
                    System.out.println("CSV Headers loaded: " + String.join(", ", headers));
                    continue;
                }
                
                // Split by comma but keep empty values
                String[] values = line.split(",", -1);
                Map<String, String> row = new HashMap<>();
                
                for (int i = 0; i < headers.length; i++) {
                    String value = (i < values.length) ? values[i].trim() : "";
                    row.put(headers[i].trim(), value);
                }
                
                data.add(row);
            }
            
            br.close();
            System.out.println("CSV file loaded successfully. Total rows: " + data.size());
            
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load test data from CSV: " + e.getMessage());
        }
        
        return data;
    }
    
    /**
     * Get data row by _key (first column)
     */
    public static Map<String, String> getTestData(String key) {
        if (testData == null || testData.isEmpty()) {
            throw new RuntimeException("Test data is not loaded. Check if CSV file exists.");
        }
        
        for (Map<String, String> row : testData) {
            String rowKey = row.get("_key");
            if (rowKey != null && rowKey.equalsIgnoreCase(key)) {
                return row;
            }
        }
        
        // If not found, print available keys for debugging
        System.err.println("Available test data keys:");
        for (Map<String, String> row : testData) {
            System.err.println("  - " + row.get("_key"));
        }
        
        throw new RuntimeException("Test data key not found: " + key);
    }
    
    /**
     * Get specific field from a key
     */
    public static String getData(String key, String fieldName) {
        Map<String, String> data = getTestData(key);
        String value = data.get(fieldName);
        
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Field '" + fieldName + "' not found or empty for key: " + key);
        }
        
        return value;
    }
    
    /**
     * Get email for a key
     */
    public static String getEmail(String key) {
        return getData(key, "email");
    }
    
    /**
     * Get password for a key
     */
    public static String getPassword(String key) {
        return getData(key, "password");
    }
    
    /**
     * Get birthdate for a key
     */
    public static String getBirthdate(String key) {
        return getData(key, "birthdate");
    }
    
    /**
     * Get validQuery for search
     */
    public static String getValidQuery() {
        return getData("search", "validQuery");
    }
    
    /**
     * Get spellingError query for search
     */
    public static String getSpellingErrorQuery() {
        return getData("search", "spellingError");
    }
    
    /**
     * Get specialCharacters query for search
     */
    public static String getSpecialCharactersQuery() {
        return getData("search", "specialCharacters");
    }
    
    /**
     * Get public board name
     */
    public static String getPublicBoardName() {
        return getData("board", "publicBoardName");
    }
    
    /**
     * Get secret board name
     */
    public static String getSecretBoardName() {
        return getData("board", "secretBoardName");
    }
    
    /**
     * Get first name from profileUpdate
     */
    public static String getFirstName() {
        return getData("profileUpdate", "firstName");
    }
    
    /**
     * Get last name from profileUpdate
     */
    public static String getLastName() {
        return getData("profileUpdate", "lastName");
    }
    
    /**
     * Get about from profileUpdate
     */
    public static String getAbout() {
        return getData("profileUpdate", "about");
    }
    
    /**
     * Get username from profileUpdate
     */
    public static String getUsername() {
        return getData("profileUpdate", "username");
    }
    
    /**
     * Get all test data
     */
    public static List<Map<String, String>> getAllTestData() {
        return testData;
    }
    
    /**
     * Check if a key exists
     */
    public static boolean keyExists(String key) {
        try {
            getTestData(key);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
    
    /**
     * Get field value with default if not found
     */
    public static String getDataOrDefault(String key, String fieldName, String defaultValue) {
        try {
            Map<String, String> data = getTestData(key);
            String value = data.get(fieldName);
            return (value == null || value.isEmpty()) ? defaultValue : value;
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }
}
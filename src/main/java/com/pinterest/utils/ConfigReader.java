package com.pinterest.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    
    private static Properties props = new Properties();
    
    static {
        try (InputStream is = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                throw new RuntimeException("config.properties file not found in classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load config.properties: " + e.getMessage());
        }
    }
    
    /**
     * Get property value as String
     */
    public static String get(String key) {
        String value = props.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property '" + key + "' not found in config.properties");
        }
        return value;
    }
    
    /**
     * Get property value as String with default value
     */
    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
    
    /**
     * Get property value as boolean
     */
    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
    
    /**
     * Get property value as int
     */
    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }
}
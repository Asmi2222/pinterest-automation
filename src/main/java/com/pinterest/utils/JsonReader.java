package com.pinterest.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;

public class JsonReader {
    private static JsonObject jsonObject;

    static {
        try {
            String filePath = "src/test/resources/testdata/TestData.json";
            FileReader reader = new FileReader(filePath);
            jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load JSON test data");
        }
    }

    // Single parameter method for nested keys
    public static String get(String key) {
        String[] keys = key.split("\\.");
        JsonObject current = jsonObject;
        
        for (int i = 0; i < keys.length - 1; i++) {
            current = current.getAsJsonObject(keys[i]);
        }
        
        return current.get(keys[keys.length - 1]).getAsString();
    }

    // Two parameter method (if you want to keep backward compatibility)
    public static String get(String parentKey, String childKey) {
        JsonObject parent = jsonObject.getAsJsonObject(parentKey);
        return parent.get(childKey).getAsString();
    }
}
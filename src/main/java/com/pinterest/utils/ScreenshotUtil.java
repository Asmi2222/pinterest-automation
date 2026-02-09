package com.pinterest.utils;

import org.openqa.selenium.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtil {

    public static String capture(WebDriver driver, String name) {
        try {
            if (driver == null) return "";
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            String dir;
            try {
                // If you added getScreenshotDir() in ExtentManager (optional improvement)
                dir = com.pinterest.utils.ExtentManager.getScreenshotDir();
            } catch (Throwable t) {
                // Fallback if method doesn't exist
                dir = System.getProperty("user.dir") + "/test-output/screenshots/";
            }
            new File(dir).mkdirs();

            String safeName = (name == null || name.isBlank()) ? "screenshot" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
            String path = dir + safeName + "_" + timestamp + ".png";

            FileUtils.copyFile(src, new File(path));
            return path;
        } catch (Exception e) {
            return "";
        }
    }

    private ScreenshotUtil() {}
}
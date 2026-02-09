package com.pinterest.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DriverFactory {
    
    public static WebDriver createInstance(String browser, boolean headless) {
        WebDriver driver;
        
        if (browser == null || browser.isEmpty() || browser.equalsIgnoreCase("chrome")) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            
            // Maximize window
            options.addArguments("--start-maximized");
            
            // Block ALL notifications and popups
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--disable-save-password-bubble");
            options.addArguments("--disable-password-generation");
            
            // Preferences to block notifications, geolocation, and password save prompts
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("profile.default_content_setting_values.notifications", 2);
            prefs.put("profile.default_content_setting_values.geolocation", 2);
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            options.setExperimentalOption("prefs", prefs);
            
            // Headless mode
            if (headless) {
                options.addArguments("--headless=new");
            }
            
            // Additional stability options
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--disable-extensions");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation", "enable-logging"});
            options.setExperimentalOption("useAutomationExtension", false);
            
            driver = new ChromeDriver(options);
            
        } else {
            throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
        
        // Set timeouts from config
        int implicitWait = Integer.parseInt(ConfigReader.get("implicit.wait"));
        int pageLoadTimeout = Integer.parseInt(ConfigReader.get("page.load.timeout"));
        
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        
        return driver;
    }
}
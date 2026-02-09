package com.pinterest.base;

import com.pinterest.utils.ConfigReader;
import com.pinterest.utils.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.time.Duration;

public class BaseTest {
    
    protected WebDriver driver;
    protected WebDriverWait wait;
    
    @BeforeClass(alwaysRun = true)  // Changed from @BeforeMethod
    public void setUp() {
        String browser = ConfigReader.get("browser");
        boolean headless = ConfigReader.getBoolean("headless");
        
        driver = DriverFactory.createInstance(browser, headless);
        
        if (driver == null) {
            throw new RuntimeException("WebDriver initialization failed. Check DriverFactory.");
        }
        
        // Initialize WebDriverWait for explicit waits
        int explicitWait = Integer.parseInt(ConfigReader.get("explicit.wait"));
        wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
        
        // Navigate to base URL
        driver.get(getBaseUrl());
    }
    
    @AfterClass(alwaysRun = true)  // Changed from @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    /**
     * Returns the base URL from config.properties
     */
    protected String getBaseUrl() {
        return ConfigReader.get("base.url");
    }
    
    /**
     * Navigate to a specific path relative to base URL
     * Example: navigateToPath("/login") -> https://www.pinterest.com/login
     */
    protected void navigateToPath(String path) {
        String url = getBaseUrl() + path;
        driver.get(url);
    }
    
    /**
     * Initialize page object with PageFactory
     */
    protected <T> T initPage(Class<T> pageClass) {
        return PageFactory.initElements(driver, pageClass);
    }
}
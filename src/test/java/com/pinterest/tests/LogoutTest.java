package com.pinterest.tests;

import com.pinterest.base.BaseTest;
import com.pinterest.pages.LoginPage;
import com.pinterest.pages.LogoutPage;
import com.pinterest.utils.ConfigReader;
import com.pinterest.utils.CSVReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;

public class LogoutTest extends BaseTest {
    
    private static final Logger logger = LogManager.getLogger(LogoutTest.class);
    
    @Test(priority = 1, description = "Verify user can successfully logout from Pinterest")
    public void validLogout() {
        logger.info("=== Starting Logout Test ===");
        
        // Get test data from CSV
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("validUser");
        String password = CSVReader.getPassword("validUser");
        
        logger.info("Email: {}", email);
        logger.debug("Password: {}", password);
        
        // Step 1: Navigate to login page and login
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/login/");
        
        // Wait for login page to load
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.urlContains("/login"));
        
        // Perform login
        loginPage.login(email, password);
        
        // Wait for successful login
        wait.until(ExpectedConditions.urlContains("pinterest.com"));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        
        logger.info("Login successful");
        
        // Step 2: Initialize logout page
        LogoutPage logoutPage = new LogoutPage(driver);
        
        // Verify dropdown icon is displayed
        Assert.assertTrue(logoutPage.isDropdownIconDisplayed(), 
            "Dropdown icon should be displayed after successful login");
        logger.info("Dropdown icon is displayed");
        
        // Click dropdown icon
        logoutPage.clickDropdownIcon();
        logger.info("Clicked dropdown icon");
        
        // Verify logout button is displayed
        Assert.assertTrue(logoutPage.isLogoutButtonDisplayed(), 
            "Logout button should be displayed after clicking dropdown");
        logger.info("Logout button is displayed");
        
        // Click logout button
        logoutPage.clickLogoutButton();
        logger.info("Clicked logout button");
        
        // Wait for redirect to homepage after logout
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlToBe("https://in.pinterest.com/"),
            ExpectedConditions.urlToBe("https://www.pinterest.com/")
        ));
        
        // Assert: Verify logout was successful
        Assert.assertTrue(logoutPage.isLogoutSuccessful(), 
            "Should redirect to Pinterest homepage after logout");
        
        logger.info("Logout test passed successfully");
        logger.info("=== Logout Test Completed ✅ ===");
    }
}
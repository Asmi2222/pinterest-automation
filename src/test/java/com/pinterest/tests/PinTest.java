package com.pinterest.tests;

import com.aventstack.extentreports.ExtentTest;
import com.pinterest.base.BaseTest;
import com.pinterest.pages.LoginPage;
import com.pinterest.pages.PinPage;
import com.pinterest.utils.ConfigReader;
import com.pinterest.utils.CSVReader;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.NoSuchElementException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;

public class PinTest extends BaseTest {
    
    private static final Logger logger = LogManager.getLogger(PinTest.class);
    
    // Configuration: Set viewing duration
    private static final int VIEW_RESULTS_DURATION_SECONDS = 5;
    
    /**
     * Helper method to pause and view results for specified seconds
     * WITHOUT using Thread.sleep()
     */
    private void viewResultsFor(int seconds) {
        if (seconds <= 0) {
            return;
        }
        
        logger.info("Viewing results for {} seconds", seconds);
        
        FluentWait<Boolean> fluentWait = new FluentWait<>(true)
            .withTimeout(Duration.ofSeconds(seconds))
            .pollingEvery(Duration.ofMillis(500))
            .ignoring(NoSuchElementException.class);
        
        try {
            fluentWait.until(result -> false);
        } catch (Exception e) {
            // Expected timeout
        }
        
        logger.info("Viewing complete");
    }
    
    @Test(priority = 1, description = "Verify user can save the first pin from homepage")
    public void saveFirstPin() {
        
        ExtentTest test = getTest();
        test.assignCategory("Pin");
        test.assignAuthor("Asmi");
        
        logger.info("=== Starting Save First Pin Test ===");
        test.info("Starting Save First Pin Test");
        
        // Get base URL and credentials
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("validUser");
        String password = CSVReader.getPassword("validUser");
        
        logger.info("Email: {}", email);
        test.info("Using email: " + email);
        logger.debug("Password retrieved from CSV (not logged)");
        test.info("Password retrieved from CSV (hidden)");
        
        // Initialize wait
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        // Step 1: Navigate to login page and login
        logger.info("Step 1: Navigating to login page");
        test.info("Step 1: Navigating to login page");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/login/");
        test.info("Opened login page: " + baseUrl + "/login/");
        
        // Wait for login page
        wait.until(ExpectedConditions.urlContains("/login"));
        logger.info("Login page loaded");
        test.pass("Login page loaded");
        
        // Perform login
        logger.info("Performing login");
        test.info("Performing login");
        loginPage.login(email, password);
        
        // Wait for successful login
        wait.until(ExpectedConditions.urlContains("pinterest.com"));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        
        // Verify search box is visible (confirms login success)
        boolean isSearchVisible = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[data-test-id='search-box-input'], input[aria-label='Search'], input[placeholder*='Search']")
        )).isDisplayed();
        
        Assert.assertTrue(isSearchVisible, "Login failed - Search box not visible");
        logger.info("Login successful - Search box visible");
        test.pass("Login successful");
        
        // Step 2: Wait for pins to load on homepage
        logger.info("Step 2: Waiting for pins to load");
        test.info("Step 2: Waiting for pins to load");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("div[data-test-id='pin'], div[data-grid-item='true']")
        ));
        logger.info("Pins loaded on homepage");
        test.pass("Pins loaded on homepage");
        
        // View homepage with pins for 3 seconds
        logger.info("Viewing homepage with pins");
        test.info("Viewing homepage with pins for 3 seconds");
        viewResultsFor(3);
        
        // Step 3: Initialize PinPage and save first pin
        logger.info("Step 3: Saving first pin");
        test.info("Step 3: Saving first pin");
        
        PinPage pinPage = new PinPage(driver);
        test.info("PinPage initialized");
        
        pinPage.saveFirstPin();
        logger.info("First pin save operation completed");
        test.pass("First pin save operation completed");
        
        // Step 4: Verify save was successful by checking for "Saved" button
        logger.info("Step 4: Verifying pin was saved");
        test.info("Step 4: Verifying pin was saved");
        
        // View the result after saving
        logger.info("Viewing results after save");
        test.info("Viewing results for {} seconds");
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        // Assert: Verify save was successful by checking for "Saved" button
        boolean saveSuccessful = pinPage.isPinSaveSuccessful();
        logger.info("Pin save verification result: {}", saveSuccessful);
        
        Assert.assertTrue(saveSuccessful, 
            "Pin save failed - 'Saved' button not found. The Save button might not have been clicked or board not selected.");
        
        logger.info("Pin save verification passed - 'Saved' button found");
        test.pass("Pin saved successfully - 'Saved' button found");
        
        logger.info("=== Save First Pin Test Completed ===");
        test.info("=== Save First Pin Test Completed ===");
    }
}
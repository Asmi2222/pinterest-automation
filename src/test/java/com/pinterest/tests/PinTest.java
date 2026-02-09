package com.pinterest.tests;

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
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;

public class PinTest extends BaseTest {
    
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
        
        System.out.println("👁️  Viewing results for " + seconds + " seconds...");
        
        FluentWait<Boolean> fluentWait = new FluentWait<>(true)
            .withTimeout(Duration.ofSeconds(seconds))
            .pollingEvery(Duration.ofMillis(500))
            .ignoring(NoSuchElementException.class);
        
        try {
            fluentWait.until(result -> false);
        } catch (Exception e) {
            // Expected timeout
        }
        
        System.out.println("✅ Viewing complete\n");
    }
    
    @Test(priority = 1, description = "Verify user can save the first pin from homepage")
    public void saveFirstPin() {
        System.out.println("\n=== TEST: Save First Pin ===");
        
        // Get base URL and credentials
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("validUser");
        String password = CSVReader.getPassword("validUser");
        
        System.out.println("📧 Email: " + email);
        System.out.println("🔐 Password: " + password);
        
        // Initialize wait
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        // Step 1: Login
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/login/");
        
        // Wait for login page
        wait.until(ExpectedConditions.urlContains("/login"));
        
        // Perform login
        loginPage.login(email, password);
        
        // Wait for successful login
        wait.until(ExpectedConditions.urlContains("pinterest.com"));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        
        // Verify search box is visible (confirms login success)
        boolean isSearchVisible = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[data-test-id='search-box-input'], input[aria-label='Search'], input[placeholder*='Search']")
        )).isDisplayed();
        
        Assert.assertTrue(isSearchVisible, "Login failed - Search box not visible");
        System.out.println("✅ Login successful\n");
        
        // Step 2: Wait for pins to load on homepage
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("div[data-test-id='pin'], div[data-grid-item='true']")
        ));
        
        // View homepage with pins for 3 seconds
        viewResultsFor(3);
        
        // Step 3: Initialize PinPage and save first pin
        PinPage pinPage = new PinPage(driver);
        pinPage.saveFirstPin();
        
        // View the result after saving for 10 seconds
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        // Assert: Verify save was successful
        Assert.assertTrue(pinPage.isPinSaveSuccessful(), 
            "Pin save might have failed - not on Pinterest page");
        
        System.out.println("✅ Test completed successfully - First pin saved");
        System.out.println("=== TEST COMPLETED ✅ ===\n");
    }
}
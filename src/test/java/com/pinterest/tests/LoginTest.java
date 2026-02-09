package com.pinterest.tests;

import com.pinterest.base.BaseTest;
import com.pinterest.pages.LoginPage;
import com.pinterest.utils.ConfigReader;
import com.pinterest.utils.CSVReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import java.time.Duration;

public class LoginTest extends BaseTest {
    
    // Configuration: Set viewing duration
    private static final int VIEW_RESULTS_DURATION_SECONDS = 5;
    
    /**
     * Setup method to ensure fresh session for each test
     * This runs BEFORE each @Test method
     */
    @BeforeMethod
    public void setupTest() {
        System.out.println("🔄 Starting fresh session...");
        
        // Clear all cookies to ensure fresh session
        driver.manage().deleteAllCookies();
        
        // Clear browser cache and session storage using JavaScript
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
        } catch (Exception e) {
            // Ignore if fails
        }
        
        System.out.println("✅ Fresh session ready\n");
    }
    
    /**
     * Helper method to pause and view results
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
    
    @Test(priority = 1, description = "Verify successful login with valid credentials")
    public void validLogin() {
        System.out.println("\n=== TEST 1: Valid Login ===");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("validUser");
        String password = CSVReader.getPassword("validUser");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/");
        loginPage.clickLoginButton();
        
        viewResultsFor(2);
        
        loginPage.login(email, password);
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        // Assert: Search bar should be visible after successful login
        try {
            WebElement searchBar = driver.findElement(By.xpath("//*[@id='searchBoxContainer']/div/div/div[2]/input"));
            Assert.assertTrue(searchBar.isDisplayed(), "Search bar should be visible after successful login");
            System.out.println("✅ Search bar is visible - login successful");
        } catch (Exception e) {
            Assert.fail("Search bar not found - login may have failed");
        }
        
        System.out.println("✅ Valid login completed");
        System.out.println("=== TEST 1 COMPLETED ✅ ===\n");
    }
    
    @Test(priority = 2, description = "Verify error message with wrong password")
    public void loginWithWrongPassword() {
        System.out.println("\n=== TEST 2: Wrong Password ===");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("wrongPasswordUser");
        String password = CSVReader.getPassword("wrongPasswordUser");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/");
        loginPage.clickLoginButton();
        
        viewResultsFor(2);
        
        loginPage.login(email, password);
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        // Assert: Should show specific password error message
        try {
            WebElement passwordError = driver.findElement(By.xpath("//span[contains(@class, '_GUqAa') and contains(text(), 'The password you entered is incorrect')]"));
            String errorText = passwordError.getText();
            Assert.assertTrue(errorText.contains("The password you entered is incorrect"),
                "Expected specific password error message");
            System.out.println("✅ Password error message: " + errorText);
        } catch (Exception e) {
            Assert.fail("Password error message not found");
        }
        
        System.out.println("✅ Wrong password error displayed correctly");
        System.out.println("=== TEST 2 COMPLETED ✅ ===\n");
    }
    
    @Test(priority = 3, description = "Verify validation with empty email")
    public void loginWithEmptyEmail() {
        System.out.println("\n=== TEST 3: Empty Email ===");
        
        String baseUrl = ConfigReader.get("base.url");
        String password = CSVReader.getPassword("validUser");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/");
        loginPage.clickLoginButton();
        
        viewResultsFor(2);
        
        loginPage.login("", password);
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        // Assert: Should show specific email error message
        try {
            WebElement emailError = driver.findElement(By.id("email-error"));
            String errorText = emailError.getText();
            Assert.assertEquals(errorText, "You missed a spot! Don't forget to add your email.",
                "Expected specific email error message");
            System.out.println("✅ Email error message: " + errorText);
        } catch (Exception e) {
            Assert.fail("Email error message not found");
        }
        
        System.out.println("✅ Empty email validation working");
        System.out.println("=== TEST 3 COMPLETED ✅ ===\n");
    }
    
    @Test(priority = 4, description = "Verify error with empty password")
    public void loginWithEmptyPassword() {
        System.out.println("\n=== TEST 4: Empty Password ===");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("validUser");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/");
        loginPage.clickLoginButton();
        
        viewResultsFor(2);
        
        loginPage.login(email, "");
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        // Assert: Should show password error message in touchableErrorMessage div
        try {
            WebElement passwordError = driver.findElement(By.cssSelector("div[data-test-id='touchableErrorMessage']"));
            String errorText = passwordError.getText();
            Assert.assertTrue(errorText.contains("The password you entered is incorrect"),
                "Expected specific password error message");
            System.out.println("✅ Password error message: " + errorText);
        } catch (Exception e) {
            Assert.fail("Password error message not found");
        }
        
        System.out.println("✅ Empty password validation working");
        System.out.println("=== TEST 4 COMPLETED ✅ ===\n");
    }
    
    @Test(priority = 5, description = "Verify email format validation")
    public void loginWithInvalidEmailFormat() {
        System.out.println("\n=== TEST 5: Invalid Email Format ===");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getData("invalidEmail", "noAtSymbol");
        String password = CSVReader.getPassword("validUser");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/");
        loginPage.clickLoginButton();
        
        viewResultsFor(2);
        
        loginPage.login(email, password);
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        // Assert: Should show specific email format error message
        try {
            WebElement emailError = driver.findElement(By.id("email-error"));
            String errorText = emailError.getText();
            Assert.assertEquals(errorText, "Hmm...that doesn't look like an email address.",
                "Expected specific email format error message");
            System.out.println("✅ Email error message: " + errorText);
        } catch (Exception e) {
            Assert.fail("Email format error message not found");
        }
        
        System.out.println("✅ Invalid email format error displayed");
        System.out.println("=== TEST 5 COMPLETED ✅ ===\n");
    }
}
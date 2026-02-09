package com.pinterest.tests;

import com.aventstack.extentreports.ExtentTest;
import com.pinterest.base.BaseTest;
import com.pinterest.pages.LoginPage;
import com.pinterest.utils.ConfigReader;
import com.pinterest.utils.CSVReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.NoSuchElementException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public class LoginTest extends BaseTest {
    
    private static final Logger logger = LogManager.getLogger(LoginTest.class);
    
    // Configuration: Set viewing duration
    private static final int VIEW_RESULTS_DURATION_SECONDS = 5;
    
    /**
     * Setup method to ensure fresh session for each test
     * This runs BEFORE each @Test method
     */
    @BeforeMethod
    public void setupTest() {
        logger.info("Starting fresh session");
        
        // Clear all cookies to ensure fresh session
        driver.manage().deleteAllCookies();
        
        // Clear browser cache and session storage using JavaScript
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
            logger.debug("Cleared session storage and local storage");
        } catch (Exception e) {
            logger.warn("Could not clear browser storage: {}", e.getMessage());
        }
        
        logger.info("Fresh session ready");
    }
    
    /**
     * Helper method to pause and view results WITHOUT using Thread.sleep()
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
    
    @Test(priority = 1, description = "Verify successful login with valid credentials")
    public void validLogin() {
        ExtentTest test = getTest();
        test.assignCategory("Login");
        test.assignAuthor("Asmi");
        
        logger.info("TEST 1: Valid Login - Started");
        test.info("Test 1: Valid Login - Started");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("validUser");
        String password = CSVReader.getPassword("validUser");
        
        logger.info("Email: {}", email);
        test.info("Using email: " + email);
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/");
        test.info("Opened base URL");
        
        loginPage.clickLoginButton();
        test.info("Clicked login button");
        
        viewResultsFor(2);
        
        loginPage.login(email, password);
        test.info("Entered credentials and submitted");
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        // Assert: Search bar should be visible after successful login
        try {
            WebElement searchBar = driver.findElement(By.xpath("//*[@id='searchBoxContainer']/div/div/div[2]/input"));
            Assert.assertTrue(searchBar.isDisplayed(), "Search bar should be visible after successful login");
            logger.info("Search bar is visible - login successful");
            test.pass("Search bar is visible - login successful");
        } catch (Exception e) {
            logger.error("Search bar not found - login may have failed");
            test.fail("Search bar not found - login may have failed");
            Assert.fail("Search bar not found - login may have failed");
        }
        
        logger.info("Valid login completed successfully");
        test.pass("Valid login completed successfully");
        logger.info("TEST 1: Valid Login - Completed");
        test.info("TEST 1: Valid Login - Completed");
    }
    
    @Test(priority = 2, description = "Verify error message with wrong password")
    public void loginWithWrongPassword() {
        ExtentTest test = getTest();
        test.assignCategory("Login");
        test.assignAuthor("Asmi");
        
        logger.info("TEST 2: Wrong Password - Started");
        test.info("Test 2: Wrong Password - Started");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("wrongPasswordUser");
        String password = CSVReader.getPassword("wrongPasswordUser");
        
        logger.info("Email: {}", email);
        test.info("Using email: " + email);
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/");
        test.info("Opened base URL");
        
        loginPage.clickLoginButton();
        test.info("Clicked login button");
        
        viewResultsFor(2);
        
        loginPage.login(email, password);
        test.info("Entered wrong password and submitted");
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        // Assert: Should show specific password error message
        try {
            WebElement passwordError = driver.findElement(By.xpath("//span[contains(@class, '_GUqAa') and contains(text(), 'The password you entered is incorrect')]"));
            String errorText = passwordError.getText();
            Assert.assertTrue(errorText.contains("The password you entered is incorrect"),
                "Expected specific password error message");
            logger.info("Password error message displayed: {}", errorText);
            test.pass("Password error message displayed: " + errorText);
        } catch (Exception e) {
            logger.error("Password error message not found");
            test.fail("Password error message not found");
            Assert.fail("Password error message not found");
        }
        
        logger.info("Wrong password error displayed correctly");
        test.pass("Wrong password error displayed correctly");
        logger.info("TEST 2: Wrong Password - Completed");
        test.info("TEST 2: Wrong Password - Completed");
    }
    
    @Test(priority = 3, description = "Verify validation with empty email")
    public void loginWithEmptyEmail() {
        ExtentTest test = getTest();
        test.assignCategory("Login");
        test.assignAuthor("Asmi");
        
        logger.info("TEST 3: Empty Email - Started");
        test.info("Test 3: Empty Email - Started");
        
        String baseUrl = ConfigReader.get("base.url");
        String password = CSVReader.getPassword("validUser");
        
        logger.info("Testing with empty email");
        test.info("Testing with empty email");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/");
        test.info("Opened base URL");
        
        loginPage.clickLoginButton();
        test.info("Clicked login button");
        
        viewResultsFor(2);
        
        loginPage.login("", password);
        test.info("Submitted with empty email");
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        // Assert: Should show specific email error message
        try {
            WebElement emailError = driver.findElement(By.id("email-error"));
            String errorText = emailError.getText();
            Assert.assertEquals(errorText, "You missed a spot! Don't forget to add your email.",
                "Expected specific email error message");
            logger.info("Email error message displayed: {}", errorText);
            test.pass("Email error message displayed: " + errorText);
        } catch (Exception e) {
            logger.error("Email error message not found");
            test.fail("Email error message not found");
            Assert.fail("Email error message not found");
        }
        
        logger.info("Empty email validation working");
        test.pass("Empty email validation working");
        logger.info("TEST 3: Empty Email - Completed");
        test.info("TEST 3: Empty Email - Completed");
    }
    
    @Test(priority = 4, description = "Verify error with empty password")
    public void loginWithEmptyPassword() {
        ExtentTest test = getTest();
        test.assignCategory("Login");
        test.assignAuthor("Asmi");
        
        logger.info("TEST 4: Empty Password - Started");
        test.info("Test 4: Empty Password - Started");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("validUser");
        
        logger.info("Email: {}", email);
        test.info("Using email: " + email);
        test.info("Testing with empty password");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/");
        test.info("Opened base URL");
        
        loginPage.clickLoginButton();
        test.info("Clicked login button");
        
        viewResultsFor(2);
        
        loginPage.login(email, "");
        test.info("Submitted with empty password");
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        // Assert: Should show password error message in touchableErrorMessage div
        try {
            WebElement passwordError = driver.findElement(By.cssSelector("div[data-test-id='touchableErrorMessage']"));
            String errorText = passwordError.getText();
            Assert.assertTrue(errorText.contains("The password you entered is incorrect"),
                "Expected specific password error message");
            logger.info("Password error message displayed: {}", errorText);
            test.pass("Password error message displayed: " + errorText);
        } catch (Exception e) {
            logger.error("Password error message not found");
            test.fail("Password error message not found");
            Assert.fail("Password error message not found");
        }
        
        logger.info("Empty password validation working");
        test.pass("Empty password validation working");
        logger.info("TEST 4: Empty Password - Completed");
        test.info("TEST 4: Empty Password - Completed");
    }
    
    @Test(priority = 5, description = "Verify email format validation")
    public void loginWithInvalidEmailFormat() {
        ExtentTest test = getTest();
        test.assignCategory("Login");
        test.assignAuthor("Asmi");
        
        logger.info("TEST 5: Invalid Email Format - Started");
        test.info("Test 5: Invalid Email Format - Started");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getData("invalidEmail", "noAtSymbol");
        String password = CSVReader.getPassword("validUser");
        
        logger.info("Invalid email: {}", email);
        test.info("Testing with invalid email format: " + email);
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/");
        test.info("Opened base URL");
        
        loginPage.clickLoginButton();
        test.info("Clicked login button");
        
        viewResultsFor(2);
        
        loginPage.login(email, password);
        test.info("Submitted with invalid email format");
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        // Assert: Should show specific email format error message
        try {
            WebElement emailError = driver.findElement(By.id("email-error"));
            String errorText = emailError.getText();
            Assert.assertEquals(errorText, "Hmm...that doesn't look like an email address.",
                "Expected specific email format error message");
            logger.info("Email format error message displayed: {}", errorText);
            test.pass("Email format error message displayed: " + errorText);
        } catch (Exception e) {
            logger.error("Email format error message not found");
            test.fail("Email format error message not found");
            Assert.fail("Email format error message not found");
        }
        
        logger.info("Invalid email format error displayed");
        test.pass("Invalid email format error displayed");
        logger.info("TEST 5: Invalid Email Format - Completed");
        test.info("TEST 5: Invalid Email Format - Completed");
    }
}
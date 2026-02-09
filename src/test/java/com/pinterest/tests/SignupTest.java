package com.pinterest.tests;

import com.pinterest.base.BaseTest;
import com.pinterest.pages.SignupPage;
import com.pinterest.utils.ConfigReader;
import com.pinterest.utils.CSVReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import java.time.Duration;

public class SignupTest extends BaseTest {
    
    private static final Logger logger = LogManager.getLogger(SignupTest.class);
    private static final int VIEW_RESULTS_DURATION_SECONDS = 5;
    
    @BeforeMethod
    public void setupTest() {
        logger.info("Starting fresh session");
        
        driver.manage().deleteAllCookies();
        
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
        } catch (Exception e) {
            logger.debug("Failed to clear storage: {}", e.getMessage());
        }
        
        logger.info("Fresh session ready");
    }
    
    private void viewResultsFor(int seconds) {
        if (seconds <= 0) {
            return;
        }
        
        logger.info("Viewing results for {} seconds", seconds);
        
        try {
            new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(d -> false);
        } catch (Exception e) {
            // Expected timeout
        }
        
        logger.debug("Viewing complete");
    }
    
    @Test(priority = 1, description = "Verify successful signup with valid credentials")
    public void validSignup() {
        logger.info("=== TEST 1: Valid Signup ===");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("newSignupUser");
        String password = CSVReader.getPassword("newSignupUser");
        String birthdate = CSVReader.getBirthdate("newSignupUser");
        
        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        signupPage.clickSignupButton();
        
        viewResultsFor(2);
        
        signupPage.signup(email, password, birthdate);
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        try {
            WebElement searchBar = driver.findElement(By.xpath("//*[@id='searchBoxContainer']/div/div/div[2]/input"));
            Assert.assertTrue(searchBar.isDisplayed(), "Search bar should be visible after successful signup");
            logger.info("Search bar is visible - signup successful");
        } catch (Exception e) {
            logger.error("Search bar not found - signup may have failed");
            Assert.fail("Search bar not found - signup may have failed");
        }
        
        logger.info("=== TEST 1 COMPLETED ✅ ===");
    }
    
    @Test(priority = 2, description = "Verify validation with empty email")
    public void signupWithEmptyEmail() {
        logger.info("=== TEST 2: Empty Email ===");
        
        String baseUrl = ConfigReader.get("base.url");
        String password = CSVReader.getPassword("newSignupUser");
        String birthdate = CSVReader.getBirthdate("newSignupUser");
        
        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        signupPage.clickSignupButton();
        
        viewResultsFor(2);
        
        signupPage.signup("", password, birthdate);
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        try {
            WebElement emailError = driver.findElement(By.id("email-error"));
            String errorText = emailError.getText();
            Assert.assertEquals(errorText, "You missed a spot! Don't forget to add your email.",
                "Expected specific email error message");
            logger.info("Email error message: {}", errorText);
        } catch (Exception e) {
            logger.error("Email error message not found");
            Assert.fail("Email error message not found");
        }
        
        logger.info("=== TEST 2 COMPLETED ✅ ===");
    }
    
    @Test(priority = 3, description = "Verify validation with empty password")
    public void signupWithEmptyPassword() {
        logger.info("=== TEST 3: Empty Password ===");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("newSignupUser2");
        String birthdate = CSVReader.getBirthdate("newSignupUser2");
        
        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        signupPage.clickSignupButton();
        
        viewResultsFor(2);
        
        signupPage.signup(email, "", birthdate);
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        try {
            WebElement passwordError = driver.findElement(By.id("password-error"));
            String errorText = passwordError.getText();
            Assert.assertEquals(errorText, "Your password is too short! You need 6+ characters.",
                "Expected specific password error message");
            logger.info("Password error message: {}", errorText);
        } catch (Exception e) {
            logger.error("Password error message not found");
            Assert.fail("Password error message not found");
        }
        
        logger.info("=== TEST 3 COMPLETED ✅ ===");
    }
    
    @Test(priority = 4, description = "Verify validation with all fields empty")
    public void signupWithAllFieldsEmpty() {
        logger.info("=== TEST 4: All Fields Empty ===");
        
        String baseUrl = ConfigReader.get("base.url");
        
        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        signupPage.clickSignupButton();
        
        viewResultsFor(2);
        
        signupPage.signup("", "", "");
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        try {
            WebElement emailError = driver.findElement(By.id("email-error"));
            String emailErrorText = emailError.getText();
            Assert.assertEquals(emailErrorText, "You missed a spot! Don't forget to add your email.",
                "Expected specific email error message");
            logger.info("Email error message: {}", emailErrorText);
        } catch (Exception e) {
            logger.error("Expected error messages not found: {}", e.getMessage());
            Assert.fail("Expected error messages not found: " + e.getMessage());
        }
        
        logger.info("=== TEST 4 COMPLETED ✅ ===");
    }
    
    @Test(priority = 5, description = "Verify email format validation")
    public void signupWithInvalidEmailFormat() {
        logger.info("=== TEST 5: Invalid Email Format ===");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getData("invalidEmail", "noAtSymbol");
        String password = CSVReader.getPassword("newSignupUser");
        String birthdate = CSVReader.getBirthdate("newSignupUser");
        
        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        signupPage.clickSignupButton();
        
        viewResultsFor(2);
        
        signupPage.signup(email, password, birthdate);
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        try {
            WebElement emailError = driver.findElement(By.id("email-error"));
            String errorText = emailError.getText();
            Assert.assertEquals(errorText, "Hmm...that doesn't look like an email address.",
                "Expected specific email format error message");
            logger.info("Email format error message: {}", errorText);
        } catch (Exception e) {
            logger.error("Email format error message not found");
            Assert.fail("Email format error message not found");
        }
        
        logger.info("=== TEST 5 COMPLETED ✅ ===");
    }
    
    @Test(priority = 6, description = "Verify underage user validation")
    public void signupWithUnderage() {
        logger.info("=== TEST 6: Underage User ===");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("underageUser");
        String password = CSVReader.getPassword("underageUser");
        String birthdate = CSVReader.getBirthdate("underageUser");
        
        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        signupPage.clickSignupButton();
        
        viewResultsFor(2);
        
        signupPage.signup(email, password, birthdate);
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        try {
            WebElement birthdateError = driver.findElement(By.id("birthdate-error"));
            String errorText = birthdateError.getText();
            Assert.assertEquals(errorText, "Oops! Please use a valid age to sign up.",
                "Expected specific age validation error message");
            logger.info("Age error message: {}", errorText);
        } catch (Exception e) {
            logger.error("Age validation error message not found");
            Assert.fail("Age validation error message not found");
        }
        
        logger.info("=== TEST 6 COMPLETED ✅ ===");
    }
    
    @Test(priority = 7, description = "Verify future birthdate validation")
    public void signupWithFutureBirthdate() {
        logger.info("=== TEST 7: Future Birthdate ===");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("futureDateUser");
        String password = CSVReader.getPassword("futureDateUser");
        String birthdate = CSVReader.getBirthdate("futureDateUser");
        
        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        signupPage.clickSignupButton();
        
        viewResultsFor(2);
        
        signupPage.signup(email, password, birthdate);
        
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        try {
            WebElement birthdateError = driver.findElement(By.id("birthdate-error"));
            String errorText = birthdateError.getText();
            Assert.assertEquals(errorText, "Oops! Please use a valid age to sign up.",
                "Expected specific birthdate validation error message");
            logger.info("Birthdate error message: {}", errorText);
        } catch (Exception e) {
            logger.error("Birthdate validation error message not found");
            Assert.fail("Birthdate validation error message not found");
        }
        
        logger.info("=== TEST 7 COMPLETED ✅ ===");
    }
}
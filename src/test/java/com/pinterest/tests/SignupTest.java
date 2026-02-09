package com.pinterest.tests;

import com.aventstack.extentreports.ExtentTest;
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
        ExtentTest test = getTest();
        test.assignCategory("Signup").assignAuthor("Asmi");
        logger.info("=== TEST 1: Valid Signup ===");
        test.info("Starting: Valid Signup");

        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("newSignupUser");
        String password = CSVReader.getPassword("newSignupUser");
        String birthdate = CSVReader.getBirthdate("newSignupUser");

        test.info("Base URL: " + baseUrl);
        test.info("Using email: " + email);
        test.info("Birthdate provided (hidden in logs)");

        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        test.info("Opened home page");

        signupPage.clickSignupButton();
        test.info("Clicked Sign up button");

        viewResultsFor(2);

        signupPage.signup(email, password, birthdate);
        test.info("Submitted signup form");

        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);

        try {
            WebElement searchBar = driver.findElement(By.xpath("//*[@id='searchBoxContainer']/div/div/div[2]/input"));
            Assert.assertTrue(searchBar.isDisplayed(), "Search bar should be visible after successful signup");
            logger.info("Search bar is visible - signup successful");
            test.pass("Search bar visible — signup successful");
        } catch (Exception e) {
            logger.error("Search bar not found - signup may have failed");
            test.info("Search bar not found — will fail the test");
            Assert.fail("Search bar not found - signup may have failed");
        }

        logger.info("=== TEST 1 COMPLETED ===");
        test.info("Completed: Valid Signup");
        
    }

    @Test(priority = 2, description = "Verify validation with empty email")
    public void signupWithEmptyEmail() {
        ExtentTest test = getTest();
        test.assignCategory("Signup").assignAuthor("Asmi");
        logger.info("=== TEST 2: Empty Email ===");
        test.info("Starting: Empty Email validation");

        String baseUrl = ConfigReader.get("base.url");
        String password = CSVReader.getPassword("newSignupUser");
        String birthdate = CSVReader.getBirthdate("newSignupUser");

        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        test.info("Opened home page");

        signupPage.clickSignupButton();
        test.info("Clicked Sign up button");

        viewResultsFor(2);

        signupPage.signup("", password, birthdate);
        test.info("Submitted signup form with empty email");

        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);

        try {
            WebElement emailError = driver.findElement(By.id("email-error"));
            String errorText = emailError.getText();
            Assert.assertEquals(errorText, "You missed a spot! Don't forget to add your email.",
                    "Expected specific email error message");
            logger.info("Email error message: {}", errorText);
            test.pass("Validated email empty error message");
        } catch (Exception e) {
            logger.error("Email error message not found");
            test.info("Email error message not found — will fail the test");
            Assert.fail("Email error message not found");
        }

        logger.info("=== TEST 2 COMPLETED ===");
        test.info("Completed: Empty Email validation");
    }

    @Test(priority = 3, description = "Verify validation with empty password")
    public void signupWithEmptyPassword() {
        ExtentTest test = getTest();
        test.assignCategory("Signup").assignAuthor("Asmi");
        logger.info("=== TEST 3: Empty Password ===");
        test.info("Starting: Empty Password validation");

        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("newSignupUser2");
        String birthdate = CSVReader.getBirthdate("newSignupUser2");

        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        test.info("Opened home page");

        signupPage.clickSignupButton();
        test.info("Clicked Sign up button");

        viewResultsFor(2);

        signupPage.signup(email, "", birthdate);
        test.info("Submitted signup form with empty password");

        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);

        try {
            WebElement passwordError = driver.findElement(By.id("password-error"));
            String errorText = passwordError.getText();
            Assert.assertEquals(errorText, "Your password is too short! You need 6+ characters.",
                    "Expected specific password error message");
            logger.info("Password error message: {}", errorText);
            test.pass("Validated empty password error message");
        } catch (Exception e) {
            logger.error("Password error message not found");
            test.info("Password error message not found — will fail the test");
            Assert.fail("Password error message not found");
        }

        logger.info("=== TEST 3 COMPLETED ===");
        test.info("Completed: Empty Password validation");
    }

    @Test(priority = 4, description = "Verify validation with all fields empty")
    public void signupWithAllFieldsEmpty() {
        ExtentTest test = getTest();
        test.assignCategory("Signup").assignAuthor("Asmi");
        logger.info("=== TEST 4: All Fields Empty ===");
        test.info("Starting: All fields empty validation");

        String baseUrl = ConfigReader.get("base.url");

        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        test.info("Opened home page");

        signupPage.clickSignupButton();
        test.info("Clicked Sign up button");

        viewResultsFor(2);

        signupPage.signup("", "", "");
        test.info("Submitted signup form with all fields empty");

        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);

        try {
            WebElement emailError = driver.findElement(By.id("email-error"));
            String emailErrorText = emailError.getText();
            Assert.assertEquals(emailErrorText, "You missed a spot! Don't forget to add your email.",
                    "Expected specific email error message");
            logger.info("Email error message: {}", emailErrorText);
            test.pass("Validated all-fields-empty: email error message");
        } catch (Exception e) {
            logger.error("Expected error messages not found: {}", e.getMessage());
            test.info("Expected error messages not found — will fail the test");
            Assert.fail("Expected error messages not found: " + e.getMessage());
        }

        logger.info("=== TEST 4 COMPLETED ===");
        test.info("Completed: All fields empty validation");
    }

    @Test(priority = 5, description = "Verify email format validation")
    public void signupWithInvalidEmailFormat() {
        ExtentTest test = getTest();
        test.assignCategory("Signup").assignAuthor("Asmi");
        logger.info("=== TEST 5: Invalid Email Format ===");
        test.info("Starting: Invalid email format validation");

        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getData("invalidEmail", "noAtSymbol");
        String password = CSVReader.getPassword("newSignupUser");
        String birthdate = CSVReader.getBirthdate("newSignupUser");

        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        test.info("Opened home page");

        signupPage.clickSignupButton();
        test.info("Clicked Sign up button");

        viewResultsFor(2);

        signupPage.signup(email, password, birthdate);
        test.info("Submitted signup form with invalid email: " + email);

        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);

        try {
            WebElement emailError = driver.findElement(By.id("email-error"));
            String errorText = emailError.getText();
            Assert.assertEquals(errorText, "Hmm...that doesn't look like an email address.",
                    "Expected specific email format error message");
            logger.info("Email format error message: {}", errorText);
            test.pass("Validated invalid email format error message");
        } catch (Exception e) {
            logger.error("Email format error message not found");
            test.info("Email format error message not found — will fail the test");
            Assert.fail("Email format error message not found");
        }

        logger.info("=== TEST 5 COMPLETED ===");
        test.info("Completed: Invalid email format validation");
    }

    @Test(priority = 6, description = "Verify underage user validation")
    public void signupWithUnderage() {
        ExtentTest test = getTest();
        test.assignCategory("Signup").assignAuthor("Asmi");
        logger.info("=== TEST 6: Underage User ===");
        test.info("Starting: Underage user validation");

        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("underageUser");
        String password = CSVReader.getPassword("underageUser");
        String birthdate = CSVReader.getBirthdate("underageUser");

        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        test.info("Opened home page");

        signupPage.clickSignupButton();
        test.info("Clicked Sign up button");

        viewResultsFor(2);

        signupPage.signup(email, password, birthdate);
        test.info("Submitted signup form with underage birthdate");

        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);

        try {
            WebElement birthdateError = driver.findElement(By.id("birthdate-error"));
            String errorText = birthdateError.getText();
            Assert.assertEquals(errorText, "Oops! Please use a valid age to sign up.",
                    "Expected specific age validation error message");
            logger.info("Age error message: {}", errorText);
            test.pass("Validated underage error message");
        } catch (Exception e) {
            logger.error("Age validation error message not found");
            test.info("Age validation error message not found — will fail the test");
            Assert.fail("Age validation error message not found");
        }

        logger.info("=== TEST 6 COMPLETED ===");
        test.info("Completed: Underage user validation");
    }

    @Test(priority = 7, description = "Verify future birthdate validation")
    public void signupWithFutureBirthdate() {
        ExtentTest test = getTest();
        test.assignCategory("Signup").assignAuthor("Asmi");
        logger.info("=== TEST 7: Future Birthdate ===");
        test.info("Starting: Future birthdate validation");

        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("futureDateUser");
        String password = CSVReader.getPassword("futureDateUser");
        String birthdate = CSVReader.getBirthdate("futureDateUser");

        SignupPage signupPage = new SignupPage(driver);
        signupPage.open(baseUrl + "/");
        test.info("Opened home page");

        signupPage.clickSignupButton();
        test.info("Clicked Sign up button");

        viewResultsFor(2);

        signupPage.signup(email, password, birthdate);
        test.info("Submitted signup form with future birthdate");

        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);

        try {
            WebElement birthdateError = driver.findElement(By.id("birthdate-error"));
            String errorText = birthdateError.getText();
            Assert.assertEquals(errorText, "Oops! Please use a valid age to sign up.",
                    "Expected specific birthdate validation error message");
            logger.info("Birthdate error message: {}", errorText);
            test.pass("Validated future birthdate error message");
        } catch (Exception e) {
            logger.error("Birthdate validation error message not found");
            test.info("Birthdate validation error message not found — will fail the test");
            Assert.fail("Birthdate validation error message not found");
        }

        logger.info("=== TEST 7 COMPLETED ===");
        test.info("Completed: Future birthdate validation");
    }
}
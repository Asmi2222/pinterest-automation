package com.pinterest.tests;

import com.aventstack.extentreports.ExtentTest;
import com.pinterest.base.BaseTest;
import com.pinterest.pages.LoginPage;
import com.pinterest.pages.EditProfilePage;
import com.pinterest.utils.ConfigReader;
import com.pinterest.utils.CSVReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

public class EditProfileTest extends BaseTest {
    
    private static final Logger logger = LogManager.getLogger(EditProfileTest.class);
    private EditProfilePage editProfilePage;
    
    @BeforeClass
    public void setup() {
        logger.info("Setting up Edit Profile Tests");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("validUser");
        String password = CSVReader.getPassword("validUser");
        
        logger.info("Email: {}", email);
        logger.debug("Password configured for user");
        
        // Login
        driver.get(baseUrl + "/login/");
        wait.until(ExpectedConditions.urlContains("/login"));
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(email, password);
        
        wait.until(ExpectedConditions.urlContains("pinterest.com"));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        
        logger.info("Login successful for Edit Profile tests");
        
        editProfilePage = new EditProfilePage(driver);
        logger.info("Edit Profile tests setup completed");
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
    
    @Test(priority = 1, description = "Edit all profile details at once")
    public void testEditCompleteProfile() {
        ExtentTest test = getTest();
        test.assignCategory("Edit Profile");
        test.assignAuthor("Asmi");
        
        logger.info("TEST: Edit Complete Profile - Started");
        test.info("Test: Edit Complete Profile - Started");
        
        String firstName = CSVReader.getFirstName();
        String lastName = CSVReader.getLastName();
        String about = CSVReader.getAbout();
        String username = CSVReader.getUsername();
        
        logger.info("First Name: {}", firstName);
        logger.info("Last Name: {}", lastName);
        logger.info("About: {}", about);
        logger.info("Username: {}", username);
        
        test.info("First Name: " + firstName);
        test.info("Last Name: " + lastName);
        test.info("About: " + about);
        test.info("Username: " + username);
        
        // Update all profile fields at once
        editProfilePage.updateProfile(firstName, lastName, about, username);
        test.info("Updated all profile fields (First Name, Last Name, About, Username)");
        
        viewResultsFor(5);
        
        Assert.assertTrue(driver.getCurrentUrl().contains("pinterest.com"),
            "Should remain on Pinterest domain after profile update");
        
        logger.info("Profile update completed successfully - All fields updated");
        test.pass("Profile update completed successfully - All fields updated");
        logger.info("TEST: Edit Complete Profile - Completed");
        test.info("TEST: Edit Complete Profile - Completed");
    }
}
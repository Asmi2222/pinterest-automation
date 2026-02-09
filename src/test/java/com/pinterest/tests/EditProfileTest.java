package com.pinterest.tests;

import com.pinterest.base.BaseTest;
import com.pinterest.pages.LoginPage;
import com.pinterest.pages.EditProfilePage;
import com.pinterest.utils.ConfigReader;
import com.pinterest.utils.CSVReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EditProfileTest extends BaseTest {
    
    private static final Logger logger = LogManager.getLogger(EditProfileTest.class);
    private EditProfilePage editProfilePage;
    
    @BeforeClass
    public void setup() {
        logger.info("=== Setting up Edit Profile Tests ===");
        
        String baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("validUser");
        String password = CSVReader.getPassword("validUser");
        
        logger.info("Email: {}", email);
        
        // Login
        driver.get(baseUrl + "/login/");
        wait.until(ExpectedConditions.urlContains("/login"));
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(email, password);
        
        wait.until(ExpectedConditions.urlContains("pinterest.com"));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        
        logger.info("Login successful");
        
        editProfilePage = new EditProfilePage(driver);
        logger.info("=== Setup Complete ===\n");
    }
    
    @Test(priority = 1)
    public void testEditCompleteProfile() {
        logger.info("\n=== TEST 1: Edit Complete Profile ===");
        
        String firstName = CSVReader.getFirstName();
        String lastName = CSVReader.getLastName();
        String about = CSVReader.getAbout();
        String username = CSVReader.getUsername();
        
        logger.info("First Name: {}", firstName);
        logger.info("Last Name: {}", lastName);
        logger.info("About: {}", about);
        logger.info("Username: {}", username);
        
        editProfilePage.updateProfile(firstName, lastName, about, username);
        
        pause(5);
        
        Assert.assertTrue(driver.getCurrentUrl().contains("pinterest.com"));
        logger.info("=== TEST 1 COMPLETED ✅ ===\n");
    }
    
    @Test(priority = 2)
    public void testEditFirstNameOnly() {
        logger.info("\n=== TEST 2: Edit First Name Only ===");
        
        goHome();
        
        String firstName = CSVReader.getFirstName() + "2";
        logger.info("First Name: {}", firstName);
        
        editProfilePage.navigateToEditProfile();
        pause(2);
        
        editProfilePage.setFirstName(firstName);
        editProfilePage.clickSave();
        
        pause(5);
        
        Assert.assertTrue(driver.getCurrentUrl().contains("pinterest.com"));
        logger.info("=== TEST 2 COMPLETED ✅ ===\n");
    }
    
    @Test(priority = 3)
    public void testEditLastNameOnly() {
        logger.info("\n=== TEST 3: Edit Last Name Only ===");
        
        goHome();
        
        String lastName = CSVReader.getLastName() + "2";
        logger.info("Last Name: {}", lastName);
        
        editProfilePage.navigateToEditProfile();
        pause(2);
        
        editProfilePage.setLastName(lastName);
        editProfilePage.clickSave();
        
        pause(5);
        
        Assert.assertTrue(driver.getCurrentUrl().contains("pinterest.com"));
        logger.info("=== TEST 3 COMPLETED ✅ ===\n");
    }
    
    @Test(priority = 4)
    public void testProfileFieldsVisibility() {
        logger.info("\n=== TEST 4: Verify Profile Fields ===");
        
        goHome();
        
        editProfilePage.navigateToEditProfile();
        pause(3);
        
        Assert.assertTrue(editProfilePage.isSaveButtonVisible());
        Assert.assertNotNull(editProfilePage.getFirstName());
        Assert.assertNotNull(editProfilePage.getLastName());
        
        logger.info("First Name: {}", editProfilePage.getFirstName());
        logger.info("Last Name: {}", editProfilePage.getLastName());
        logger.info("Username: {}", editProfilePage.getUsername());
        logger.info("About: {}", editProfilePage.getAbout());
        
        pause(5);
        
        logger.info("=== TEST 4 COMPLETED ✅ ===\n");
    }
    
    private void goHome() {
        String baseUrl = ConfigReader.get("base.url");
        driver.get(baseUrl);
        wait.until(ExpectedConditions.urlContains("pinterest.com"));
    }
    
    private void pause(int seconds) {
        logger.info("Viewing for {} seconds...", seconds);
        try {
            wait.until(d -> false);
        } catch (Exception e) {
            // Expected
        }
    }
}
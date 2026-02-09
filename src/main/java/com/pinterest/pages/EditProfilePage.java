package com.pinterest.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.FluentWait;
import java.time.Duration;

public class EditProfilePage {
    
    private static final Logger logger = LogManager.getLogger(EditProfilePage.class);
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;
    
    // Multiple avatar locators
    private final By[] avatarLocators = {
        By.cssSelector("img[alt='Your profile']"),
        By.cssSelector("img.iFOUS5"),
        By.cssSelector("img[alt*='profile' i]"),
        By.cssSelector("div[data-test-id='header-profile']"),
        By.xpath("//img[@alt='Your profile']"),
        By.xpath("//header//img[contains(@alt, 'profile')]"),
        By.xpath("//header//img[@class='iFOUS5']"),
        By.xpath("//img[@draggable='true' and contains(@src, 'pinimg.com')]")
    };
    
    // Profile name locators (dynamic - works for any user)
    private final By[] profileNameLocators = {
        By.xpath("//div[@role='menu']//div[@title and contains(@class, 'WuRgKB')]"),
        By.xpath("//div[@role='menu']//div[@title]"),
        By.xpath("//div[contains(@class, 'WuRgKB') and contains(@class, 'aMgNKE') and @title]"),
        By.cssSelector("div[role='menu'] div[title]"),
        By.xpath("//div[@role='menu']//div[contains(@class, 'yH1eVZ')]")
    };
    
    // Edit profile link locators
    private final By[] editProfileLocators = {
        By.xpath("//div[@role='menu']//a[contains(@href, 'settings')]"),
        By.xpath("//a[contains(@href, 'settings')]"),
        By.xpath("//div[@role='menu']//a[contains(text(), 'Settings')]"),
        By.xpath("//a[contains(text(), 'Settings')]")
    };
    
    private final By firstName = By.id("first_name");
    private final By lastName = By.id("last_name");
    private final By about = By.id("about");
    private final By username = By.id("username");
    private final By saveButton = By.xpath("//button[.//div[text()='Save']]");
    
    public EditProfilePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.js = (JavascriptExecutor) driver;
        logger.info("EditProfilePage initialized");
    }
    
    public void goToEditProfile() {
        logger.info("Starting Edit Profile navigation");
        
        // Step 1: Click avatar
        logger.info("STEP 1: Looking for avatar on current page");
        clickAvatar();
        waitFor(1500);
        
        // Step 2: Click profile name (dynamic for any user)
        logger.info("STEP 2: Clicking user profile name");
        clickProfileName();
        waitFor(1500);
        
        // Step 3: Click edit profile/settings link
        logger.info("STEP 3: Clicking edit profile/settings link");
        clickEditProfile();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstName));
        logger.info("Edit Profile page loaded successfully");
    }
    
    public void navigateToEditProfile() {
        goToEditProfile();
    }
    
    public void updateProfile(String first, String last, String bio, String user) {
        goToEditProfile();
        
        if (notEmpty(first)) {
            clearAndType(firstName, first, "First Name");
        }
        if (notEmpty(last)) {
            clearAndType(lastName, last, "Last Name");
        }
        if (notEmpty(bio)) {
            clearAndType(about, bio, "About");
        }
        if (notEmpty(user)) {
            scroll(username);
            clearAndType(username, user, "Username");
        }
        
        clickSave();
    }
    
    public void setFirstName(String value) {
        clearAndType(firstName, value, "First Name");
    }
    
    public void setLastName(String value) {
        clearAndType(lastName, value, "Last Name");
    }
    
    public void setAbout(String value) {
        clearAndType(about, value, "About");
    }
    
    public void setUsername(String value) {
        scroll(username);
        clearAndType(username, value, "Username");
    }
    
    public void clickSave() {
        logger.info("Clicking Save button");
        scroll(saveButton);
        click(saveButton, "Save Button");
        waitFor(2000);
        logger.info("Save completed");
    }
    
    public String getFirstName() {
        return getValue(firstName);
    }
    
    public String getLastName() {
        return getValue(lastName);
    }
    
    public String getAbout() {
        return getValue(about);
    }
    
    public String getUsername() {
        scroll(username);
        return getValue(username);
    }
    
    public boolean isSaveButtonVisible() {
        try {
            scroll(saveButton);
            boolean visible = driver.findElement(saveButton).isDisplayed();
            logger.debug("Save button visibility: {}", visible);
            return visible;
        } catch (Exception e) {
            logger.debug("Save button not found");
            return false;
        }
    }
    
    // ========== HELPERS ==========
    
    private void clickAvatar() {
        logger.info("Attempting to find and click avatar");
        
        // Try each avatar locator in sequence
        for (int i = 0; i < avatarLocators.length; i++) {
            By locator = avatarLocators[i];
            String locatorName = "Avatar locator " + (i + 1);
            
            if (tryClick(locator, locatorName)) {
                logger.info("Successfully clicked avatar using {}", locatorName);
                return;
            }
        }
        
        // Last resort - find by tag in header
        logger.warn("Standard locators failed, searching header for clickable element");
        try {
            WebElement header = driver.findElement(By.tagName("header"));
            WebElement avatarEl = header.findElement(By.xpath(".//img | .//svg"));
            
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", avatarEl);
            waitFor(300);
            
            try {
                avatarEl.click();
            } catch (ElementClickInterceptedException e) {
                logger.warn("Click intercepted, using JavaScript click");
                js.executeScript("arguments[0].click();", avatarEl);
            }
            
            logger.info("Avatar clicked via header search");
            return;
        } catch (Exception e) {
            logger.error("Cannot find avatar element anywhere: {}", e.getMessage());
            throw new RuntimeException("Avatar not found on page", e);
        }
    }
    
    private void clickProfileName() {
        logger.info("Attempting to find and click profile name");
        
        // Try each profile name locator
        for (int i = 0; i < profileNameLocators.length; i++) {
            By locator = profileNameLocators[i];
            String locatorName = "Profile name locator " + (i + 1);
            
            if (tryClick(locator, locatorName)) {
                logger.info("Successfully clicked profile name using {}", locatorName);
                return;
            }
        }
        
        logger.error("Cannot find profile name element");
        throw new RuntimeException("Profile name not found in menu");
    }
    
    private void clickEditProfile() {
        logger.info("Attempting to find and click edit profile link");
        
        // Try each edit profile locator
        for (int i = 0; i < editProfileLocators.length; i++) {
            By locator = editProfileLocators[i];
            String locatorName = "Edit profile locator " + (i + 1);
            
            if (tryClick(locator, locatorName)) {
                logger.info("Successfully clicked edit profile link using {}", locatorName);
                return;
            }
        }
        
        logger.error("Cannot find edit profile link");
        throw new RuntimeException("Edit profile link not found in menu");
    }
    
    private boolean tryClick(By locator, String name) {
        try {
            logger.debug("Trying {}", name);
            
            WebElement el = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
            
            // Log element details for debugging
            try {
                String elementText = el.getText();
                String elementTitle = el.getAttribute("title");
                logger.debug("{} found - Text: '{}', Title: '{}'", name, elementText, elementTitle);
            } catch (Exception e) {
                // Ignore if we can't get text/title
            }
            
            // Scroll into view
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
            waitFor(300);
            
            // Wait for element to be clickable
            wait.until(ExpectedConditions.elementToBeClickable(locator));
            
            // Try regular click first
            try {
                el.click();
                logger.info("Successfully clicked {} using regular click", name);
                return true;
            } catch (ElementClickInterceptedException e) {
                logger.debug("Regular click intercepted, trying JavaScript click");
                js.executeScript("arguments[0].click();", el);
                logger.info("Successfully clicked {} using JavaScript click", name);
                return true;
            }
            
        } catch (Exception e) {
            logger.debug("Failed to click {}: {}", name, e.getMessage());
            return false;
        }
    }
    
    private void click(By locator, String name) {
        logger.info("Clicking {}", name);
        
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        waitFor(300);
        
        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator));
            el.click();
            logger.info("Successfully clicked {}", name);
        } catch (ElementClickInterceptedException e) {
            logger.warn("Click intercepted, using JavaScript click for {}", name);
            js.executeScript("arguments[0].click();", el);
            logger.info("Successfully clicked {} using JavaScript", name);
        }
    }
    
    private void clearAndType(By locator, String text, String name) {
        logger.info("Clearing and typing in {}: {}", name, text);
        
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        waitFor(300);
        
        // Click to focus
        js.executeScript("arguments[0].click();", el);
        waitFor(200);
        
        // Clear the field using multiple methods to ensure it's cleared
        el.clear();
        js.executeScript("arguments[0].value='';", el);
        
        // Select all and delete (fallback)
        el.sendKeys(Keys.CONTROL + "a");
        el.sendKeys(Keys.DELETE);
        
        waitFor(200);
        
        // Type new value
        el.sendKeys(text);
        
        logger.info("Successfully cleared and updated {}", name);
    }
    
    private void scroll(By locator) {
        try {
            WebElement el = driver.findElement(locator);
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
            waitFor(300);
        } catch (Exception e) {
            logger.warn("Scroll failed for element");
        }
    }
    
    private String getValue(By locator) {
        try {
            String value = driver.findElement(locator).getAttribute("value");
            logger.debug("Retrieved value from element: {}", value);
            return value;
        } catch (Exception e) {
            logger.warn("Could not get value from element");
            return null;
        }
    }
    
    private boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
    
    /**
     * Wait for specified milliseconds using FluentWait instead of Thread.sleep
     */
    private void waitFor(int milliseconds) {
        FluentWait<Boolean> fluentWait = new FluentWait<>(true)
            .withTimeout(Duration.ofMillis(milliseconds))
            .pollingEvery(Duration.ofMillis(100))
            .ignoring(NoSuchElementException.class);
        
        try {
            fluentWait.until(result -> false);
        } catch (Exception e) {
            // Expected timeout
        }
    }
}
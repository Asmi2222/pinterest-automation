package com.pinterest.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class EditProfilePage {
    
    private static final Logger logger = LogManager.getLogger(EditProfilePage.class);
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;
    
    
    private final By avatar = By.cssSelector("div[data-test-id='header-profile'], img[alt*='profile' i], svg[aria-label*='profile' i]");
    private final By avatarAlt = By.xpath("//header//img | //header//svg");
    
    private final By profileCard = By.xpath("//div[@role='menu']//a[contains(@href, '/')]");
    private final By editProfileLink = By.xpath("//div[@role='menu']//a[contains(@href, 'settings')]");
    
    private final By firstName = By.id("first_name");
    private final By lastName = By.id("last_name");
    private final By about = By.id("about");
    private final By username = By.id("username");
    private final By saveButton = By.xpath("//button[.//div[text()='Save']]");
    
    public EditProfilePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.js = (JavascriptExecutor) driver;
    }
    
    public void goToEditProfile() {
        logger.info("Starting Edit Profile navigation");
        
        // Step 1: Click avatar
        logger.info("STEP 1: Looking for avatar on current page");
        clickAvatar();
        pause(1500);
        
        // Step 2: Click profile card
        logger.info("STEP 2: Clicking profile card");
        click(profileCard, "Profile Card");
        pause(1500);
        
        // Step 3: Click edit profile
        logger.info("STEP 3: Clicking edit profile link");
        click(editProfileLink, "Edit Profile Link");
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstName));
        logger.info("SUCCESS: Edit Profile page loaded");
    }
    
    public void navigateToEditProfile() {
        goToEditProfile();
    }
    
    public void updateProfile(String first, String last, String bio, String user) {
        goToEditProfile();
        
        if (notEmpty(first)) type(firstName, first, "First Name");
        if (notEmpty(last)) type(lastName, last, "Last Name");
        if (notEmpty(bio)) type(about, bio, "About");
        if (notEmpty(user)) {
            scroll(username);
            type(username, user, "Username");
        }
        
        clickSave();
    }
    
    public void setFirstName(String value) {
        type(firstName, value, "First Name");
    }
    
    public void setLastName(String value) {
        type(lastName, value, "Last Name");
    }
    
    public void setAbout(String value) {
        type(about, value, "About");
    }
    
    public void setUsername(String value) {
        scroll(username);
        type(username, value, "Username");
    }
    
    public void clickSave() {
        logger.info("Clicking Save");
        scroll(saveButton);
        click(saveButton, "Save Button");
        pause(2000);
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
            return driver.findElement(saveButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    // ========== HELPERS ==========
    
    private void clickAvatar() {
        logger.info("Trying to find and click avatar...");
        
        // Try primary locator
        if (tryClick(avatar, "Avatar (primary)")) {
            return;
        }
        
        // Try alternative
        if (tryClick(avatarAlt, "Avatar (alternative)")) {
            return;
        }
        
        // Last resort - find by tag in header
        logger.warn("Standard locators failed, searching header for clickable element");
        try {
            WebElement header = driver.findElement(By.tagName("header"));
            WebElement avatarEl = header.findElement(By.xpath(".//img | .//svg"));
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", avatarEl);
            pause(300);
            js.executeScript("arguments[0].click();", avatarEl);
            logger.info("Avatar clicked via header search");
            return;
        } catch (Exception e) {
            logger.error("FAILED: Cannot find avatar element anywhere");
            throw new RuntimeException("Avatar not found on page");
        }
    }
    
    private boolean tryClick(By locator, String name) {
        try {
            logger.debug("Trying {}", name);
            WebElement el = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(locator));
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
            pause(300);
            js.executeScript("arguments[0].click();", el);
            logger.info("SUCCESS: {} clicked", name);
            return true;
        } catch (Exception e) {
            logger.debug("FAILED: {} - {}", name, e.getMessage());
            return false;
        }
    }
    
    private void click(By locator, String name) {
        logger.info("Clicking {}", name);
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        pause(300);
        js.executeScript("arguments[0].click();", el);
        logger.info("SUCCESS: {} clicked", name);
    }
    
    private void type(By locator, String text, String name) {
        logger.info("Typing in {}: {}", name, text);
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        pause(300);
        js.executeScript("arguments[0].click();", el);
        js.executeScript("arguments[0].value='';", el);
        el.sendKeys(text);
        logger.info("SUCCESS: {} updated", name);
    }
    
    private void scroll(By locator) {
        try {
            WebElement el = driver.findElement(locator);
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
            pause(300);
        } catch (Exception e) {
            logger.warn("Scroll failed");
        }
    }
    
    private String getValue(By locator) {
        try {
            return driver.findElement(locator).getAttribute("value");
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
    
    private void pause(int ms) {
        try {
            new WebDriverWait(driver, Duration.ofMillis(ms)).until(d -> false);
        } catch (Exception e) {
            // Expected
        }
    }
}
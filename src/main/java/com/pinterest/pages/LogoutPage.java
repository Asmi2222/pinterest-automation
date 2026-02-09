package com.pinterest.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.StaleElementReferenceException;
import java.time.Duration;

public class LogoutPage {

    private static final Logger logger = LogManager.getLogger(LogoutPage.class);
    
    private WebDriver driver;
    private WebDriverWait wait;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_TIMEOUT_SECONDS = 15;

    @FindBy(css = ".VHreRh.pZY3za.XjRT60")
    private WebElement dropdownIcon;

    @FindBy(xpath = "//span[contains(@class, 'WuRgKB') and contains(text(), 'Log out')]")
    private WebElement logoutButton;

    @FindBy(css = "div[data-test-id='header-profile'], button[aria-label*='Profile'], div[data-test-id='user-menu']")
    private WebElement profileMenu;

    public LogoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
        PageFactory.initElements(driver, this);
    }

    public void clickDropdownIcon() {
        retryClick(dropdownIcon, "Dropdown Icon");
    }

    public void clickLogoutButton() {
        retryClick(logoutButton, "Logout Button");
    }

    public void performLogout() {
        clickDropdownIcon();
        waitForLogoutButtonVisibility();
        clickLogoutButton();
        waitForLogoutComplete();
    }

    public boolean isDropdownIconDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(dropdownIcon));
            return dropdownIcon.isDisplayed();
        } catch (TimeoutException e) {
            logger.error("Dropdown icon not displayed within timeout");
            return false;
        } catch (Exception e) {
            logger.error("Error checking dropdown icon visibility: {}", e.getMessage());
            return false;
        }
    }

    public boolean isLogoutButtonDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(logoutButton));
            return logoutButton.isDisplayed();
        } catch (TimeoutException e) {
            logger.error("Logout button not displayed within timeout");
            return false;
        } catch (Exception e) {
            logger.error("Error checking logout button visibility: {}", e.getMessage());
            return false;
        }
    }

    public boolean isProfileMenuDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(profileMenu));
            return profileMenu.isDisplayed();
        } catch (TimeoutException e) {
            logger.error("Profile menu not displayed within timeout");
            return false;
        } catch (Exception e) {
            logger.error("Error checking profile menu visibility: {}", e.getMessage());
            return false;
        }
    }

    private void waitForLogoutButtonVisibility() {
        try {
            wait.until(ExpectedConditions.visibilityOf(logoutButton));
            wait.until(ExpectedConditions.elementToBeClickable(logoutButton));
        } catch (TimeoutException e) {
            logger.error("Logout button did not become visible after clicking dropdown");
            throw new RuntimeException("Logout button did not become visible after clicking dropdown", e);
        }
    }

    private void waitForLogoutComplete() {
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlToBe("https://in.pinterest.com/"),
                ExpectedConditions.urlToBe("https://www.pinterest.com/"),
                ExpectedConditions.urlContains("pinterest.com/login")
            ));
        } catch (TimeoutException e) {
            logger.error("Logout did not complete within timeout. Current URL: {}", driver.getCurrentUrl());
        }
    }

    private void retryClick(WebElement element, String elementName) {
        int attempts = 0;
        
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(element));
                element.click();
                logger.info("{} clicked successfully", elementName);
                return;
                
            } catch (StaleElementReferenceException e) {
                attempts++;
                logger.warn("Stale element detected for {}. Retry attempt: {}", elementName, attempts);
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    logger.error("Failed to click {} after {} attempts due to stale element", elementName, MAX_RETRY_ATTEMPTS);
                    throw new RuntimeException("Failed to click " + elementName + " after " + MAX_RETRY_ATTEMPTS + " attempts due to stale element", e);
                }
                
                PageFactory.initElements(driver, this);
                wait.until(ExpectedConditions.elementToBeClickable(element));
                
            } catch (ElementClickInterceptedException e) {
                attempts++;
                logger.warn("Click intercepted for {}. Retry attempt: {}", elementName, attempts);
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    logger.error("Failed to click {} after {} attempts due to click interception", elementName, MAX_RETRY_ATTEMPTS);
                    throw new RuntimeException("Failed to click " + elementName + " after " + MAX_RETRY_ATTEMPTS + " attempts due to click interception", e);
                }
                
                wait.until(ExpectedConditions.elementToBeClickable(element));
                
            } catch (TimeoutException e) {
                logger.error("{} was not clickable within timeout period", elementName);
                throw new RuntimeException(elementName + " was not clickable within timeout period", e);
            }
        }
    }

    public boolean isLogoutSuccessful() {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.equals("https://in.pinterest.com/") || 
               currentUrl.equals("https://www.pinterest.com/") ||
               currentUrl.contains("pinterest.com/login");
    }
}
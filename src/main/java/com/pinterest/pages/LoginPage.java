package com.pinterest.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

public class LoginPage {
    
    private static final Logger logger = LogManager.getLogger(LoginPage.class);
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    private static final int WAIT_TIMEOUT_SECONDS = 15;
    
    // Multiple locators for dynamic elements
    private By[] emailInputLocators = {
        By.cssSelector("input[name='id']"),
        By.cssSelector("input[type='email']"),
        By.cssSelector("input[autocomplete='username']")
    };
    
    private By[] passwordInputLocators = {
        By.cssSelector("input[name='password']"),
        By.cssSelector("input[type='password']"),
        By.cssSelector("input[autocomplete='current-password']")
    };
    
    private By[] submitButtonLocators = {
        By.cssSelector("button[type='submit']"),
        By.xpath("//button[contains(text(), 'Log in')]"),
        By.xpath("//button[contains(text(), 'Continue')]")
    };
    
    private By[] loginButtonLocators = {
        By.xpath("//*[@id='__PWS_ROOT__']/div[1]/header/div[1]/nav/div[2]/div[2]/button"),
        By.xpath("//button[contains(text(), 'Log in')]"),
        By.cssSelector("button[data-test-id='login-button']"),
        By.xpath("//header//button[contains(., 'Log in')]")
    };
    
    private By[] errorMessageLocators = {
        By.cssSelector("div[data-test-id='error-message']"),
        By.cssSelector(".error"),
        By.cssSelector("[role='alert']"),
        By.xpath("//*[contains(@class, 'error')]"),
        By.id("email-error"),
        By.id("password-error")
    };
    
    @FindBy(css = "input[name='id']")
    private WebElement emailInput;
    
    @FindBy(css = "input[name='password']")
    private WebElement passwordInput;
    
    @FindBy(css = "button[type='submit']")
    private WebElement submitButton;
    
    @FindBy(css = "div[data-test-id='error-message'], .error, [role='alert']")
    private WebElement errorMessage;
    
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
        PageFactory.initElements(driver, this);
        logger.info("LoginPage initialized");
    }
    
    /**
     * Open login page
     */
    public void open(String url) {
        logger.info("Opening URL: {}", url);
        driver.get(url);
        waitForPageLoad();
    }
    
    /**
     * Wait for page to load
     */
    private void waitForPageLoad() {
        wait.until(driver -> 
            ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
        logger.debug("Page loaded successfully");
    }
    
    /**
     * Find element with multiple locator strategies
     */
    private WebElement findElementWithRetry(By[] locators, String elementName) {
        logger.debug("Searching for element: {}", elementName);
        
        for (By locator : locators) {
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                logger.debug("{} found using locator: {}", elementName, locator);
                return element;
            } catch (TimeoutException e) {
                logger.debug("Locator failed: {}, trying next", locator);
            }
        }
        
        logger.warn("{} not found with any locator", elementName);
        return null;
    }
    
    /**
     * Click login button (if present on homepage)
     */
    public void clickLoginButton() {
        logger.info("Attempting to click login button");
        
        for (By locator : loginButtonLocators) {
            try {
                WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(locator));
                
                String buttonText = loginBtn.getText().toLowerCase();
                logger.debug("Found button with text: '{}'", buttonText);
                
                if (buttonText.contains("log in") || buttonText.contains("login") || buttonText.isEmpty()) {
                    loginBtn.click();
                    logger.info("Clicked login button successfully");
                    
                    waitForPageLoad();
                    
                    // Wait for modal/form to appear using WebDriverWait
                    wait.until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(emailInputLocators[0]),
                        ExpectedConditions.presenceOfElementLocated(emailInputLocators[1]),
                        ExpectedConditions.presenceOfElementLocated(emailInputLocators[2])
                    ));
                    
                    return;
                } else {
                    logger.debug("Button text doesn't match login, trying next locator");
                }
            } catch (TimeoutException e) {
                logger.debug("Login button locator timeout, trying next");
            }
        }
        
        logger.info("Login button not found (might already be on login page)");
    }
    
    /**
     * Login with credentials
     */
    public void login(String email, String password) {
        logger.info("Attempting login with email: {}", email);
        
        // Fill email
        if (!email.isEmpty()) {
            WebElement emailField = findElementWithRetry(emailInputLocators, "Email field");
            if (emailField != null) {
                emailField.clear();
                emailField.sendKeys(email);
                logger.info("Email entered: {}", email);
            }
        }
        
        // Fill password
        if (!password.isEmpty()) {
            WebElement passwordField = findElementWithRetry(passwordInputLocators, "Password field");
            if (passwordField != null) {
                passwordField.clear();
                passwordField.sendKeys(password);
                logger.debug("Password entered");
            }
        }
        
        // Click submit button
        clickSubmitButton();
    }
    
    /**
     * Click submit/login button
     */
    private void clickSubmitButton() {
        logger.debug("Attempting to click submit button");
        WebElement submitBtn = findElementWithRetry(submitButtonLocators, "Submit button");
        
        if (submitBtn != null) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(submitBtn));
                submitBtn.click();
                logger.info("Clicked submit button");
                
                // Wait for response using WebDriverWait
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("pinterest.com"),
                    ExpectedConditions.presenceOfElementLocated(errorMessageLocators[0])
                ));
                
            } catch (Exception e) {
                logger.error("Failed to click submit button: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Login with Enter key
     */
    public void loginWithEnter(String email, String password) {
        logger.info("Login with Enter key - Email: {}", email);
        
        if (!email.isEmpty()) {
            WebElement emailField = findElementWithRetry(emailInputLocators, "Email field");
            if (emailField != null) {
                emailField.clear();
                emailField.sendKeys(email);
            }
        }
        
        if (!password.isEmpty()) {
            WebElement passwordField = findElementWithRetry(passwordInputLocators, "Password field");
            if (passwordField != null) {
                passwordField.clear();
                passwordField.sendKeys(password);
                passwordField.sendKeys(Keys.RETURN);
                logger.info("Pressed Enter to submit");
            }
        }
    }
    
    /**
     * Login with button click
     */
    public void loginWithButton(String email, String password) {
        login(email, password);
    }
    
    /**
     * Check if error message is displayed
     */
    public boolean isErrorMessageDisplayed() {
        logger.debug("Checking for error messages");
        
        for (By locator : errorMessageLocators) {
            try {
                WebElement errorMsg = driver.findElement(locator);
                if (errorMsg.isDisplayed()) {
                    logger.info("Error message found: {}", errorMsg.getText());
                    return true;
                }
            } catch (Exception e) {
                // Try next locator
            }
        }
        
        logger.debug("No error message displayed");
        return false;
    }
    
    /**
     * Get error message text
     */
    public String getErrorMessage() {
        logger.debug("Retrieving error message text");
        
        for (By locator : errorMessageLocators) {
            try {
                WebElement errorMsg = driver.findElement(locator);
                if (errorMsg.isDisplayed()) {
                    String text = errorMsg.getText();
                    logger.info("Error message text: {}", text);
                    return text;
                }
            } catch (Exception e) {
                // Try next locator
            }
        }
        
        logger.debug("No error message text found");
        return "";
    }
    
    /**
     * Check if still on login page
     */
    public boolean isOnLoginPage() {
        String currentUrl = driver.getCurrentUrl();
        boolean onLoginPage = currentUrl.contains("/login") || isEmailFieldDisplayed();
        
        if (onLoginPage) {
            logger.debug("Still on login page");
        } else {
            logger.info("Left login page - Current URL: {}", currentUrl);
        }
        
        return onLoginPage;
    }
    
    /**
     * Check if email field is displayed
     */
    public boolean isEmailFieldDisplayed() {
        for (By locator : emailInputLocators) {
            try {
                WebElement element = driver.findElement(locator);
                if (element.isDisplayed()) {
                    logger.debug("Email field is displayed");
                    return true;
                }
            } catch (Exception e) {
                // Try next locator
            }
        }
        
        logger.debug("Email field not displayed");
        return false;
    }
    
    /**
     * Check if password field is displayed
     */
    public boolean isPasswordFieldDisplayed() {
        for (By locator : passwordInputLocators) {
            try {
                WebElement element = driver.findElement(locator);
                if (element.isDisplayed()) {
                    logger.debug("Password field is displayed");
                    return true;
                }
            } catch (Exception e) {
                // Try next locator
            }
        }
        
        logger.debug("Password field not displayed");
        return false;
    }
    
    /**
     * Check if page source contains specific text
     */
    public boolean pageContainsText(String text) {
        String pageSource = driver.getPageSource().toLowerCase();
        boolean contains = pageSource.contains(text.toLowerCase());
        logger.debug("Page contains text '{}': {}", text, contains);
        return contains;
    }
}
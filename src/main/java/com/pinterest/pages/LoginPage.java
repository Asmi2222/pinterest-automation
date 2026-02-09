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
import java.time.Duration;

public class LoginPage {
    
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
    
    // FIXED: Use the correct XPath for login button as first priority
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
    }
    
    /**
     * Open login page
     */
    public void open(String url) {
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
        System.out.println("✅ Page loaded");
    }
    
    /**
     * Find element with multiple locator strategies
     */
    private WebElement findElementWithRetry(By[] locators, String elementName) {
        for (By locator : locators) {
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                System.out.println("✅ " + elementName + " found");
                return element;
            } catch (TimeoutException e) {
                // Try next locator
            }
        }
        
        System.out.println("⚠️  " + elementName + " not found");
        return null;
    }
    
    /**
     * Click login button (if present on homepage)
     */
    public void clickLoginButton() {
        for (By locator : loginButtonLocators) {
            try {
                WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(locator));
                
                // Verify it's actually the login button
                String buttonText = loginBtn.getText().toLowerCase();
                System.out.println("🔍 Found button with text: '" + buttonText + "'");
                
                // Only click if it's clearly a login button or if text is empty (icon button)
                if (buttonText.contains("log in") || buttonText.contains("login") || buttonText.isEmpty()) {
                    loginBtn.click();
                    System.out.println("✅ Clicked login button");
                    
                    // Wait for login form to load
                    waitForPageLoad();
                    
                    // Wait a bit for modal/form to appear
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return;
                } else {
                    System.out.println("⚠️  Button text doesn't match login, trying next locator");
                }
            } catch (TimeoutException e) {
                // Try next locator
            }
        }
        
        System.out.println("ℹ️  Login button not found (might already be on login page)");
    }
    
    /**
     * Login with credentials
     */
    public void login(String email, String password) {
        // Fill email
        if (!email.isEmpty()) {
            WebElement emailField = findElementWithRetry(emailInputLocators, "Email field");
            if (emailField != null) {
                emailField.clear();
                emailField.sendKeys(email);
                System.out.println("✅ Email entered: " + email);
            }
        }
        
        // Fill password
        if (!password.isEmpty()) {
            WebElement passwordField = findElementWithRetry(passwordInputLocators, "Password field");
            if (passwordField != null) {
                passwordField.clear();
                passwordField.sendKeys(password);
                System.out.println("✅ Password entered");
            }
        }
        
        // Click submit button
        clickSubmitButton();
    }
    
    /**
     * Click submit/login button
     */
    private void clickSubmitButton() {
        WebElement submitBtn = findElementWithRetry(submitButtonLocators, "Submit button");
        
        if (submitBtn != null) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(submitBtn));
                submitBtn.click();
                System.out.println("✅ Clicked submit button");
                
                // Wait for response
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
            } catch (Exception e) {
                System.err.println("⚠️  Failed to click submit button");
            }
        }
    }
    
    /**
     * Login with Enter key
     */
    public void loginWithEnter(String email, String password) {
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
                System.out.println("✅ Pressed Enter to submit");
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
        for (By locator : errorMessageLocators) {
            try {
                WebElement errorMsg = driver.findElement(locator);
                if (errorMsg.isDisplayed()) {
                    System.out.println("✅ Error message found: " + errorMsg.getText());
                    return true;
                }
            } catch (Exception e) {
                // Try next locator
            }
        }
        
        System.out.println("ℹ️  No error message displayed");
        return false;
    }
    
    /**
     * Get error message text
     */
    public String getErrorMessage() {
        for (By locator : errorMessageLocators) {
            try {
                WebElement errorMsg = driver.findElement(locator);
                if (errorMsg.isDisplayed()) {
                    return errorMsg.getText();
                }
            } catch (Exception e) {
                // Try next locator
            }
        }
        return "";
    }
    
    /**
     * Check if still on login page
     */
    public boolean isOnLoginPage() {
        String currentUrl = driver.getCurrentUrl();
        boolean onLoginPage = currentUrl.contains("/login") || 
                              isEmailFieldDisplayed();
        
        if (onLoginPage) {
            System.out.println("ℹ️  Still on login page");
        } else {
            System.out.println("✅ Left login page - URL: " + currentUrl);
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
                    return true;
                }
            } catch (Exception e) {
                // Try next locator
            }
        }
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
                    return true;
                }
            } catch (Exception e) {
                // Try next locator
            }
        }
        return false;
    }
    
    /**
     * Check if page source contains specific text
     */
    public boolean pageContainsText(String text) {
        String pageSource = driver.getPageSource().toLowerCase();
        return pageSource.contains(text.toLowerCase());
    }
}
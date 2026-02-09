package com.pinterest.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class SignupPage {
    
    private static final Logger logger = LogManager.getLogger(SignupPage.class);
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    private static final int WAIT_TIMEOUT_SECONDS = 15;
    
    private By[] emailInputLocators = {
        By.cssSelector("input[data-test-id='emailInputField']"),
        By.cssSelector("input[name='id']"),
        By.cssSelector("input[type='email']"),
        By.cssSelector("input[autocomplete='email']")
    };
    
    private By[] passwordInputLocators = {
        By.cssSelector("input[data-test-id='passwordInputField']"),
        By.cssSelector("input[name='password']"),
        By.cssSelector("input[type='password']")
    };
    
    private By[] birthdateInputLocators = {
        By.cssSelector("input[id='birthdate']"),
        By.cssSelector("input[type='date']"),
        By.cssSelector("input[name='birthdate']")
    };
    
    private By[] continueButtonLocators = {
        By.cssSelector("button[type='submit']"),
        By.cssSelector("button[aria-label*='Continue']"),
        By.xpath("//button[contains(text(), 'Continue')]")
    };
    
    private By[] signupButtonLocators = {
        By.xpath("//*[@id='__PWS_ROOT__']/div[1]/header/div[1]/nav/div[2]/div[3]/button"),
        By.xpath("//button[contains(text(), 'Sign up')]"),
        By.cssSelector("button[data-test-id='sign-up-button']"),
        By.xpath("//header//button[contains(., 'Sign up')]")
    };
    
    private By[] errorMessageLocators = {
        By.cssSelector("div[data-test-id='error-message']"),
        By.cssSelector(".error"),
        By.cssSelector("[role='alert']"),
        By.xpath("//*[contains(@class, 'error')]")
    };
    
    @FindBy(css = "input[data-test-id='emailInputField'], input[name='id']")
    private WebElement emailInput;
    
    @FindBy(css = "input[data-test-id='passwordInputField'], input[name='password']")
    private WebElement passwordInput;
    
    @FindBy(css = "input[id='birthdate'], input[type='date']")
    private WebElement birthdateInput;
    
    @FindBy(css = "button[type='submit'], button[aria-label*='Continue']")
    private WebElement continueButton;
    
    public SignupPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
        PageFactory.initElements(driver, this);
    }
    
    public void open(String url) {
        driver.get(url);
        waitForPageLoad();
    }
    
    private void waitForPageLoad() {
        wait.until(driver -> 
            ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
        logger.info("Page loaded");
    }
    
    private WebElement findElementWithRetry(By[] locators, String elementName) {
        for (By locator : locators) {
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                logger.info("{} found", elementName);
                return element;
            } catch (TimeoutException e) {
                logger.debug("{} not found with locator: {}", elementName, locator);
            }
        }
        
        logger.warn("{} not found with any locator", elementName);
        return null;
    }
    
    public void clickSignupButton() {
        for (By locator : signupButtonLocators) {
            try {
                WebElement signupBtn = wait.until(ExpectedConditions.elementToBeClickable(locator));
                
                String buttonText = signupBtn.getText().toLowerCase();
                logger.debug("Found button with text: '{}'", buttonText);
                
                if (buttonText.contains("sign up") || buttonText.isEmpty() || buttonText.contains("signup")) {
                    signupBtn.click();
                    logger.info("Clicked signup button");
                    
                    waitForPageLoad();
                    pause(1000);
                    return;
                } else {
                    logger.debug("Button text doesn't match signup, trying next locator");
                }
            } catch (TimeoutException e) {
                logger.debug("Signup button not found with locator: {}", locator);
            }
        }
        
        logger.info("Signup button not found (might already be on signup page)");
    }
    
    public void signup(String email, String password, String birthdate) {
        if (!email.isEmpty()) {
            WebElement emailField = findElementWithRetry(emailInputLocators, "Email field");
            if (emailField != null) {
                emailField.clear();
                emailField.sendKeys(email);
                logger.info("Email entered: {}", email);
            }
        }
        
        if (!password.isEmpty()) {
            WebElement passwordField = findElementWithRetry(passwordInputLocators, "Password field");
            if (passwordField != null) {
                passwordField.clear();
                passwordField.sendKeys(password);
                logger.info("Password entered");
            }
        }
        
        if (!birthdate.isEmpty()) {
            WebElement birthdateField = findElementWithRetry(birthdateInputLocators, "Birthdate field");
            if (birthdateField != null) {
                birthdateField.clear();
                birthdateField.sendKeys(birthdate);
                logger.info("Birthdate entered: {}", birthdate);
            }
        }
        
        clickContinueButton();
    }
    
    private void clickContinueButton() {
        WebElement continueBtn = findElementWithRetry(continueButtonLocators, "Continue button");
        
        if (continueBtn != null) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(continueBtn));
                continueBtn.click();
                logger.info("Clicked continue button");
                pause(2000);
            } catch (Exception e) {
                logger.warn("Failed to click continue button: {}", e.getMessage());
            }
        }
    }
    
    public boolean isErrorMessageDisplayed() {
        for (By locator : errorMessageLocators) {
            try {
                WebElement errorMsg = driver.findElement(locator);
                if (errorMsg.isDisplayed()) {
                    logger.info("Error message found: {}", errorMsg.getText());
                    return true;
                }
            } catch (Exception e) {
                logger.debug("No error message with locator: {}", locator);
            }
        }
        
        logger.info("No error message displayed");
        return false;
    }
    
    public String getErrorMessage() {
        for (By locator : errorMessageLocators) {
            try {
                WebElement errorMsg = driver.findElement(locator);
                if (errorMsg.isDisplayed()) {
                    return errorMsg.getText();
                }
            } catch (Exception e) {
                logger.debug("Error message not found with locator: {}", locator);
            }
        }
        return "";
    }
    
    public boolean isOnSignupPage() {
        String currentUrl = driver.getCurrentUrl();
        boolean onSignupPage = currentUrl.contains("/signup") || 
                              currentUrl.contains("/register") ||
                              isEmailFieldDisplayed();
        
        if (onSignupPage) {
            logger.info("Still on signup page");
        } else {
            logger.info("Left signup page - URL: {}", currentUrl);
        }
        
        return onSignupPage;
    }
    
    public boolean isEmailFieldDisplayed() {
        for (By locator : emailInputLocators) {
            try {
                WebElement element = driver.findElement(locator);
                if (element.isDisplayed()) {
                    return true;
                }
            } catch (Exception e) {
                logger.debug("Email field not found with locator: {}", locator);
            }
        }
        return false;
    }
    
    public boolean isPasswordFieldDisplayed() {
        for (By locator : passwordInputLocators) {
            try {
                WebElement element = driver.findElement(locator);
                if (element.isDisplayed()) {
                    return true;
                }
            } catch (Exception e) {
                logger.debug("Password field not found with locator: {}", locator);
            }
        }
        return false;
    }
    
    public boolean isBirthdateFieldDisplayed() {
        for (By locator : birthdateInputLocators) {
            try {
                WebElement element = driver.findElement(locator);
                if (element.isDisplayed()) {
                    return true;
                }
            } catch (Exception e) {
                logger.debug("Birthdate field not found with locator: {}", locator);
            }
        }
        return false;
    }
    
    public boolean pageContainsText(String text) {
        String pageSource = driver.getPageSource().toLowerCase();
        return pageSource.contains(text.toLowerCase());
    }
    
    private void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Pause interrupted");
        }
    }
}
package com.pinterest.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class PinPage {
    
    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;
    private JavascriptExecutor js;
    
    private static final int WAIT_TIMEOUT_SECONDS = 20;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    // Dynamic locators for pins
    private By pinLocator = By.cssSelector("div[data-test-id='pin'], div[data-grid-item='true']");
    private By saveButtonLocators[] = {
        By.xpath("//div[contains(@class, 'lIkAnG') and text()='Save']"),
        By.xpath("//div[text()='Save' and contains(@class, 'lIkAnG')]"),
        By.xpath("//button[.//div[text()='Save']]"),
        By.cssSelector("button[aria-label*='Save']"),
        By.xpath("//div[contains(text(), 'Save')]")
    };
    
    // Board selection modal locators
    private By boardModalLocator = By.xpath("//div[@role='dialog']//h1");
    private By boardPickerLocator = By.xpath("//div[contains(@class, 'BoardPickerOverlay')]");
    private By firstBoardLocator = By.xpath("//div[@role='dialog']//div[@role='button'][1]");
    private By boardListLocator = By.xpath("//div[@role='dialog']//div[@role='button']");
    
    @FindBy(css = "button[aria-label*='Save']")
    private WebElement saveButton;
    
    public PinPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
        this.actions = new Actions(driver);
        this.js = (JavascriptExecutor) driver;
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Get the first pin on the page with retry logic
     */
    public WebElement getFirstPin() {
        int attempts = 0;
        
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                WebElement firstPin = wait.until(ExpectedConditions.visibilityOfElementLocated(pinLocator));
                System.out.println("✅ First pin found");
                return firstPin;
                
            } catch (StaleElementReferenceException e) {
                attempts++;
                System.out.println("Stale element for first pin. Retry attempt: " + attempts);
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    throw new RuntimeException("Failed to find first pin after " + MAX_RETRY_ATTEMPTS + " attempts", e);
                }
                
            } catch (TimeoutException e) {
                throw new RuntimeException("First pin not found within timeout period", e);
            }
        }
        
        throw new RuntimeException("Failed to get first pin");
    }
    
    /**
     * Get all pins on the current page
     */
    public List<WebElement> getAllPins() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(pinLocator));
            List<WebElement> pins = driver.findElements(pinLocator);
            System.out.println("📌 Found " + pins.size() + " pins on page");
            return pins;
        } catch (TimeoutException e) {
            System.err.println("⚠️  No pins found on page");
            return List.of();
        }
    }
    
    /**
     * Scroll pin into view
     */
    public void scrollPinIntoView(WebElement pin) {
        js.executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", pin);
        
        // Wait for scroll to complete
        wait.until(ExpectedConditions.visibilityOf(pin));
        
        System.out.println("✅ Scrolled pin into view");
    }
    
    /**
     * Hover over a pin to reveal Save button
     */
    public void hoverOverPin(WebElement pin) {
        try {
            wait.until(ExpectedConditions.visibilityOf(pin));
            actions.moveToElement(pin).perform();
            
            // Wait for hover effect to trigger
            wait.until(driver -> {
                try {
                    return isSaveButtonVisible();
                } catch (Exception e) {
                    return false;
                }
            });
            
            System.out.println("✅ Hovered over pin - Save button revealed");
            
        } catch (Exception e) {
            System.err.println("⚠️  Hover might not have triggered save button");
        }
    }
    
    /**
     * Check if Save button is visible using multiple strategies
     */
    private boolean isSaveButtonVisible() {
        for (By locator : saveButtonLocators) {
            try {
                WebElement btn = driver.findElement(locator);
                if (btn.isDisplayed()) {
                    return true;
                }
            } catch (Exception e) {
                // Try next locator
            }
        }
        return false;
    }
    
    /**
     * Find Save button using multiple strategies with retry logic
     */
    public WebElement findSaveButton() {
        int attempts = 0;
        
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                // Try each locator strategy
                for (int i = 0; i < saveButtonLocators.length; i++) {
                    try {
                        WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(saveButtonLocators[i]));
                        System.out.println("✅ Save button found using strategy " + (i + 1));
                        return btn;
                    } catch (TimeoutException e) {
                        // Try next strategy
                    }
                }
                
                throw new RuntimeException("Save button not found with any strategy");
                
            } catch (StaleElementReferenceException e) {
                attempts++;
                System.out.println("Stale element for Save button. Retry attempt: " + attempts);
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    throw new RuntimeException("Failed to find Save button after " + MAX_RETRY_ATTEMPTS + " attempts", e);
                }
            }
        }
        
        throw new RuntimeException("Failed to find Save button");
    }
    
    /**
     * Click Save button with retry logic
     */
    public void clickSaveButton() {
        WebElement saveBtn = findSaveButton();
        int attempts = 0;
        
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(saveBtn));
                saveBtn.click();
                System.out.println("✅ Save button clicked successfully");
                return;
                
            } catch (ElementClickInterceptedException e) {
                attempts++;
                System.out.println("Click intercepted. Retry attempt: " + attempts + " using JavaScript");
                
                try {
                    js.executeScript("arguments[0].click();", saveBtn);
                    System.out.println("✅ Save button clicked using JavaScript");
                    return;
                } catch (Exception jsError) {
                    if (attempts >= MAX_RETRY_ATTEMPTS) {
                        throw new RuntimeException("Failed to click Save button after " + MAX_RETRY_ATTEMPTS + " attempts", e);
                    }
                }
                
            } catch (StaleElementReferenceException e) {
                attempts++;
                System.out.println("Stale element when clicking. Retry attempt: " + attempts);
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    throw new RuntimeException("Failed to click Save button after " + MAX_RETRY_ATTEMPTS + " attempts", e);
                }
                
                // Re-find the button
                saveBtn = findSaveButton();
            }
        }
    }
    
    /**
     * Check if board selection modal appeared
     */
    public boolean isBoardModalDisplayed() {
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(boardModalLocator),
                ExpectedConditions.presenceOfElementLocated(boardPickerLocator)
            ));
            System.out.println("✅ Board selection modal appeared");
            return true;
        } catch (TimeoutException e) {
            System.out.println("ℹ️  No board modal appeared - pin saved directly");
            return false;
        }
    }
    
    /**
     * Select first board from the modal
     */
    public void selectFirstBoard() {
        try {
            // Wait for board list to be available
            wait.until(ExpectedConditions.presenceOfElementLocated(boardListLocator));
            
            // Click first board
            WebElement firstBoard = wait.until(ExpectedConditions.elementToBeClickable(firstBoardLocator));
            
            try {
                firstBoard.click();
            } catch (ElementClickInterceptedException e) {
                js.executeScript("arguments[0].click();", firstBoard);
            }
            
            System.out.println("✅ First board selected");
            
            // Wait for modal to close
            waitForBoardModalToClose();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to select first board", e);
        }
    }
    
    /**
     * Select board by name
     */
    public void selectBoardByName(String boardName) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(boardListLocator));
            
            WebElement board = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@role='dialog']//div[contains(text(), '" + boardName + "')]")
            ));
            
            try {
                board.click();
            } catch (ElementClickInterceptedException e) {
                js.executeScript("arguments[0].click();", board);
            }
            
            System.out.println("✅ Board '" + boardName + "' selected");
            
            waitForBoardModalToClose();
            
        } catch (Exception e) {
            System.err.println("⚠️  Board '" + boardName + "' not found. Selecting first board instead.");
            selectFirstBoard();
        }
    }
    
    /**
     * Wait for board modal to close
     */
    private void waitForBoardModalToClose() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(boardModalLocator));
            System.out.println("✅ Board modal closed - Pin saved successfully");
        } catch (TimeoutException e) {
            System.err.println("⚠️  Board modal did not close as expected");
        }
    }
    
    /**
     * Complete save pin operation
     * 1. Scroll to pin
     * 2. Hover over pin
     * 3. Click Save button
     * 4. Handle board selection if modal appears
     */
    public void saveFirstPin() {
        WebElement firstPin = getFirstPin();
        scrollPinIntoView(firstPin);
        hoverOverPin(firstPin);
        clickSaveButton();
        
        if (isBoardModalDisplayed()) {
            selectFirstBoard();
        }
        
        System.out.println("🎉 Pin saved successfully!");
    }
    
    /**
     * Save first pin to a specific board
     */
    public void saveFirstPinToBoard(String boardName) {
        WebElement firstPin = getFirstPin();
        scrollPinIntoView(firstPin);
        hoverOverPin(firstPin);
        clickSaveButton();
        
        if (isBoardModalDisplayed()) {
            selectBoardByName(boardName);
        }
        
        System.out.println("🎉 Pin saved to board: " + boardName);
    }
    
    /**
     * Save specific pin by index
     */
    public void savePinByIndex(int index) {
        List<WebElement> pins = getAllPins();
        
        if (index >= pins.size()) {
            throw new RuntimeException("Pin index " + index + " out of bounds. Total pins: " + pins.size());
        }
        
        WebElement pin = pins.get(index);
        scrollPinIntoView(pin);
        hoverOverPin(pin);
        clickSaveButton();
        
        if (isBoardModalDisplayed()) {
            selectFirstBoard();
        }
        
        System.out.println("🎉 Pin at index " + index + " saved successfully!");
    }
    
    /**
     * Verify pin save was successful
     */
    public boolean isPinSaveSuccessful() {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.contains("pinterest.com");
    }
}
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.Duration;
import java.util.List;

public class PinPage {
    
    private static final Logger logger = LogManager.getLogger(PinPage.class);
    
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
    
    // Success indicators
    private By savedButtonLocators[] = {
        By.xpath("//div[contains(@class, 'lIkAnG') and text()='Saved']"),
        By.xpath("//div[text()='Saved' and contains(@class, 'lIkAnG')]"),
        By.xpath("//button[.//div[text()='Saved']]"),
        By.cssSelector("button[aria-label*='Saved']"),
        By.xpath("//div[contains(text(), 'Saved')]")
    };
    
    @FindBy(css = "button[aria-label*='Save']")
    private WebElement saveButton;
    
    public PinPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
        this.actions = new Actions(driver);
        this.js = (JavascriptExecutor) driver;
        PageFactory.initElements(driver, this);
        logger.info("PinPage initialized");
    }
    
    /**
     * Get the first pin on the page with retry logic
     */
    public WebElement getFirstPin() {
        int attempts = 0;
        
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                WebElement firstPin = wait.until(ExpectedConditions.visibilityOfElementLocated(pinLocator));
                logger.info("First pin found successfully");
                return firstPin;
                
            } catch (StaleElementReferenceException e) {
                attempts++;
                logger.warn("Stale element for first pin. Retry attempt: {}", attempts);
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    logger.error("Failed to find first pin after {} attempts", MAX_RETRY_ATTEMPTS);
                    throw new RuntimeException("Failed to find first pin after " + MAX_RETRY_ATTEMPTS + " attempts", e);
                }
                
            } catch (TimeoutException e) {
                logger.error("First pin not found within timeout period");
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
            logger.info("Found {} pins on page", pins.size());
            return pins;
        } catch (TimeoutException e) {
            logger.warn("No pins found on page");
            return List.of();
        }
    }
    
    /**
     * Scroll pin into view
     */
    public void scrollPinIntoView(WebElement pin) {
        js.executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", pin);
        
        // Wait for scroll to complete
        try {
            wait.until(ExpectedConditions.visibilityOf(pin));
            // Add small wait for smooth scroll animation
            wait.until(driver -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return true;
            });
        } catch (Exception e) {
            logger.warn("Error during scroll", e);
        }
        
        logger.info("Scrolled pin into view");
    }
    
    /**
     * Hover over a pin to reveal Save button
     */
    public void hoverOverPin(WebElement pin) {
        try {
            wait.until(ExpectedConditions.visibilityOf(pin));
            
            // Move to element
            actions.moveToElement(pin).perform();
            
            // Wait a bit for the hover effect
            wait.until(driver -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return true;
            });
            
            // Check if Save button appeared
            boolean saveButtonVisible = isSaveButtonVisible();
            logger.info("Hovered over pin - Save button visible: {}", saveButtonVisible);
            
        } catch (Exception e) {
            logger.warn("Error during hover", e);
            throw new RuntimeException("Failed to hover over pin", e);
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
     * Check if Saved button is visible (indicating successful save)
     */
    public boolean isSavedButtonVisible() {
        for (By locator : savedButtonLocators) {
            try {
                WebElement btn = driver.findElement(locator);
                if (btn.isDisplayed()) {
                    logger.info("Found 'Saved' button - pin was saved successfully");
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
                        logger.info("Save button found using strategy {}", (i + 1));
                        return btn;
                    } catch (TimeoutException e) {
                        // Try next strategy
                    }
                }
                
                logger.error("Save button not found with any strategy");
                throw new RuntimeException("Save button not found with any strategy");
                
            } catch (StaleElementReferenceException e) {
                attempts++;
                logger.warn("Stale element for Save button. Retry attempt: {}", attempts);
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    logger.error("Failed to find Save button after {} attempts", MAX_RETRY_ATTEMPTS);
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
                logger.info("Save button clicked successfully");
                return;
                
            } catch (ElementClickInterceptedException e) {
                attempts++;
                logger.warn("Click intercepted. Retry attempt: {} using JavaScript", attempts);
                
                try {
                    js.executeScript("arguments[0].click();", saveBtn);
                    logger.info("Save button clicked using JavaScript");
                    return;
                } catch (Exception jsError) {
                    if (attempts >= MAX_RETRY_ATTEMPTS) {
                        logger.error("Failed to click Save button after {} attempts", MAX_RETRY_ATTEMPTS);
                        throw new RuntimeException("Failed to click Save button after " + MAX_RETRY_ATTEMPTS + " attempts", e);
                    }
                }
                
            } catch (StaleElementReferenceException e) {
                attempts++;
                logger.warn("Stale element when clicking. Retry attempt: {}", attempts);
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    logger.error("Failed to click Save button after {} attempts", MAX_RETRY_ATTEMPTS);
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
            logger.info("Board selection modal appeared");
            return true;
        } catch (TimeoutException e) {
            logger.info("No board modal appeared - pin might be saved directly");
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
            
            logger.info("First board selected");
            
            // Wait for modal to close
            waitForBoardModalToClose();
            
        } catch (Exception e) {
            logger.error("Failed to select first board", e);
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
            
            logger.info("Board '{}' selected", boardName);
            
            waitForBoardModalToClose();
            
        } catch (Exception e) {
            logger.warn("Board '{}' not found. Selecting first board instead", boardName);
            selectFirstBoard();
        }
    }
    
    /**
     * Wait for board modal to close
     */
    private void waitForBoardModalToClose() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(boardModalLocator));
            logger.info("Board modal closed");
        } catch (TimeoutException e) {
            logger.warn("Board modal did not close as expected");
        }
    }
    
    /**
     * Wait for save confirmation
     */
    public boolean waitForSaveConfirmation() {
        try {
            // Wait for either "Saved" button or modal to close
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            
            // Check if "Saved" button appears
            for (By locator : savedButtonLocators) {
                try {
                    shortWait.until(ExpectedConditions.presenceOfElementLocated(locator));
                    logger.info("Save confirmation detected - 'Saved' button visible");
                    return true;
                } catch (TimeoutException e) {
                    // Try next locator
                }
            }
            
            logger.warn("Could not confirm save - 'Saved' button not found");
            return false;
            
        } catch (Exception e) {
            logger.error("Error while waiting for save confirmation", e);
            return false;
        }
    }
    
    /**
     * Complete save pin operation
     * 1. Scroll to pin
     * 2. Hover over pin
     * 3. Click Save button
     * 4. Handle board selection if modal appears
     * 5. Verify save was successful
     */
    public void saveFirstPin() {
        logger.info("Starting save first pin operation");
        
        WebElement firstPin = getFirstPin();
        scrollPinIntoView(firstPin);
        hoverOverPin(firstPin);
        clickSaveButton();
        
        if (isBoardModalDisplayed()) {
            selectFirstBoard();
        }
        
        // Wait for save confirmation
        boolean confirmed = waitForSaveConfirmation();
        
        if (confirmed) {
            logger.info("Pin saved successfully - confirmed");
        } else {
            logger.warn("Pin save completed but confirmation not detected");
        }
    }
    
    /**
     * Save first pin to a specific board
     */
    public void saveFirstPinToBoard(String boardName) {
        logger.info("Starting save first pin to board: {}", boardName);
        
        WebElement firstPin = getFirstPin();
        scrollPinIntoView(firstPin);
        hoverOverPin(firstPin);
        clickSaveButton();
        
        if (isBoardModalDisplayed()) {
            selectBoardByName(boardName);
        }
        
        // Wait for save confirmation
        boolean confirmed = waitForSaveConfirmation();
        
        if (confirmed) {
            logger.info("Pin saved to board: {} - confirmed", boardName);
        } else {
            logger.warn("Pin save to board completed but confirmation not detected");
        }
    }
    
    /**
     * Save specific pin by index
     */
    public void savePinByIndex(int index) {
        logger.info("Starting save pin at index: {}", index);
        
        List<WebElement> pins = getAllPins();
        
        if (index >= pins.size()) {
            logger.error("Pin index {} out of bounds. Total pins: {}", index, pins.size());
            throw new RuntimeException("Pin index " + index + " out of bounds. Total pins: " + pins.size());
        }
        
        WebElement pin = pins.get(index);
        scrollPinIntoView(pin);
        hoverOverPin(pin);
        clickSaveButton();
        
        if (isBoardModalDisplayed()) {
            selectFirstBoard();
        }
        
        // Wait for save confirmation
        boolean confirmed = waitForSaveConfirmation();
        
        if (confirmed) {
            logger.info("Pin at index {} saved successfully - confirmed", index);
        } else {
            logger.warn("Pin save at index {} completed but confirmation not detected", index);
        }
    }
    
    /**
     * Verify pin save was successful by checking for "Saved" button
     */
    public boolean isPinSaveSuccessful() {
        boolean savedButtonVisible = isSavedButtonVisible();
        logger.info("Pin save verification: {}", savedButtonVisible ? "Success - Saved button found" : "Failed - Saved button not found");
        return savedButtonVisible;
    }
}
package com.pinterest.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SearchPage {
    
    private static final Logger logger = LogManager.getLogger(SearchPage.class);
    
    private WebDriver driver;
    private WebDriverWait wait;
    private static final int WAIT_TIMEOUT_SECONDS = 20;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    // Dynamic locators
    private By searchBoxLocator = By.cssSelector("input[placeholder*='Search'], input[data-test-id='search-box-input'], input[aria-label*='Search']");
    private By pinsLocator = By.cssSelector("div[data-test-id='pin'], div[data-grid-item='true'], div[role='listitem']");
    private By noResultsLocator = By.xpath("//*[contains(text(), 'No results') or contains(text(), 'no pins found') or contains(text(), 'Try another search')]");
    private By spellingCorrectionLocator = By.xpath("//*[contains(text(), 'Did you mean') or contains(text(), 'Try searching for')]");
    
    public SearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
        PageFactory.initElements(driver, this);
        logger.info("SearchPage initialized");
    }
    
    /**
     * Find and return the search box element with retry logic
     */
    public WebElement getSearchBox() {
        int attempts = 0;
        logger.debug("Attempting to locate search box");
        
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(searchBoxLocator));
                wait.until(ExpectedConditions.elementToBeClickable(searchBox));
                logger.debug("Search box located successfully");
                return searchBox;
                
            } catch (StaleElementReferenceException e) {
                attempts++;
                logger.warn("Stale element for search box. Retry attempt: {}", attempts);
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    logger.error("Failed to find search box after {} attempts", MAX_RETRY_ATTEMPTS);
                    throw new RuntimeException("Failed to find search box after " + MAX_RETRY_ATTEMPTS + " attempts", e);
                }
                
            } catch (TimeoutException e) {
                logger.error("Search box not found within timeout period");
                throw new RuntimeException("Search box not found within timeout period", e);
            }
        }
        
        logger.error("Failed to get search box");
        throw new RuntimeException("Failed to get search box");
    }
    
    /**
     * Perform search with a query
     */
    public void search(String query) {
        logger.info("Searching for: {}", query);
        
        WebElement searchBox = getSearchBox();
        
        // Clear existing text
        searchBox.clear();
        logger.debug("Cleared search box");
        
        // Enter search query
        searchBox.sendKeys(query);
        logger.debug("Entered search query: {}", query);
        
        // Press Enter to search
        searchBox.sendKeys(Keys.RETURN);
        logger.debug("Pressed Enter to submit search");
        
        // Wait for search results page to load
        waitForSearchResultsPage();
        
        logger.info("Search executed successfully for query: {}", query);
    }
    
    /**
     * Wait for search results page to load
     */
    private void waitForSearchResultsPage() {
        try {
            wait.until(ExpectedConditions.urlContains("search"));
            logger.debug("Search results page loaded - URL contains 'search'");
        } catch (TimeoutException e) {
            logger.warn("URL does not contain 'search' after timeout");
        }
    }
    
    /**
     * Get all pins on the current page with retry logic
     */
    public List<WebElement> getPins() {
        int attempts = 0;
        
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                // Wait for at least one pin to be present
                wait.until(ExpectedConditions.presenceOfElementLocated(pinsLocator));
                
                // Get all pins
                List<WebElement> pins = driver.findElements(pinsLocator);
                
                logger.info("Found {} pins on the page", pins.size());
                return pins;
                
            } catch (StaleElementReferenceException e) {
                attempts++;
                logger.warn("Stale element when getting pins. Retry attempt: {}", attempts);
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    logger.error("Failed to get pins after {} attempts. Returning empty list.", MAX_RETRY_ATTEMPTS);
                    return new ArrayList<>();
                }
                
            } catch (TimeoutException e) {
                logger.warn("No pins found on the page - timeout occurred");
                return new ArrayList<>();
            }
        }
        
        logger.warn("Returning empty pin list after retry attempts");
        return new ArrayList<>();
    }
    
    /**
     * Get pins count
     */
    public int getPinsCount() {
        int count = getPins().size();
        logger.debug("Pins count: {}", count);
        return count;
    }
    
    /**
     * Check if pins are displayed
     */
    public boolean arePinsDisplayed() {
        boolean displayed = getPinsCount() > 0;
        logger.debug("Pins displayed: {}", displayed);
        return displayed;
    }
    
    /**
     * Open a pin by index
     */
    public void openPin(int index) {
        logger.info("Attempting to open pin at index: {}", index);
        List<WebElement> pins = getPins();
        
        if (pins.size() > index) {
            try {
                WebElement pin = pins.get(index);
                wait.until(ExpectedConditions.elementToBeClickable(pin));
                pin.click();
                logger.info("Successfully opened pin at index: {}", index);
            } catch (Exception e) {
                logger.error("Failed to open pin at index: {}", index, e);
                throw new RuntimeException("Failed to open pin at index " + index, e);
            }
        } else {
            logger.error("Pin index {} out of bounds. Total pins: {}", index, pins.size());
            throw new RuntimeException("Pin index " + index + " out of bounds. Total pins: " + pins.size());
        }
    }
    
    /**
     * Check if "No results" message is displayed
     */
    public boolean isNoResultsMessageDisplayed() {
        try {
            WebElement noResultsMsg = wait.until(ExpectedConditions.presenceOfElementLocated(noResultsLocator));
            boolean displayed = noResultsMsg.isDisplayed();
            logger.debug("No results message displayed: {}", displayed);
            return displayed;
        } catch (TimeoutException e) {
            logger.debug("No results message not displayed");
            return false;
        }
    }
    
    /**
     * Check if spelling correction/suggestion is displayed
     */
    public boolean isSpellingCorrectionDisplayed() {
        try {
            WebElement correctionMsg = wait.until(ExpectedConditions.presenceOfElementLocated(spellingCorrectionLocator));
            boolean displayed = correctionMsg.isDisplayed();
            logger.debug("Spelling correction displayed: {}", displayed);
            return displayed;
        } catch (TimeoutException e) {
            logger.debug("Spelling correction not displayed");
            return false;
        }
    }
    
    /**
     * Verify search was attempted (URL changed)
     */
    public boolean isSearchAttempted(String baseUrl) {
        String currentUrl = driver.getCurrentUrl();
        boolean attempted = currentUrl.contains("search") || !currentUrl.equals(baseUrl);
        logger.debug("Search attempted: {} (Current URL: {})", attempted, currentUrl);
        return attempted;
    }
    
    /**
     * Check if search results page loaded successfully
     */
    public boolean isSearchResultsPageLoaded() {
        boolean loaded = driver.getCurrentUrl().contains("search");
        logger.debug("Search results page loaded: {}", loaded);
        return loaded;
    }
    
    /**
     * Navigate back to home page
     */
    public void navigateToHome(String baseUrl) {
        logger.info("Navigating to home page: {}", baseUrl);
        driver.get(baseUrl);
        
        // Wait for home page to load
        wait.until(ExpectedConditions.presenceOfElementLocated(searchBoxLocator));
        
        logger.info("Successfully navigated to home page");
    }
    
    /**
     * Clear search box
     */
    public void clearSearch() {
        logger.debug("Clearing search box");
        WebElement searchBox = getSearchBox();
        searchBox.clear();
        logger.debug("Search box cleared");
    }
    
    /**
     * Check if Pinterest handled the search gracefully
     * (either showing results, suggestions, or no results message)
     */
    public boolean isSearchHandledGracefully() {
        boolean handled = arePinsDisplayed() || 
                         isSpellingCorrectionDisplayed() || 
                         isNoResultsMessageDisplayed();
        logger.debug("Search handled gracefully: {}", handled);
        return handled;
    }
}
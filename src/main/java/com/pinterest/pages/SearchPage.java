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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SearchPage {
    
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
    }
    
    /**
     * Find and return the search box element with retry logic
     */
    public WebElement getSearchBox() {
        int attempts = 0;
        
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(searchBoxLocator));
                wait.until(ExpectedConditions.elementToBeClickable(searchBox));
                return searchBox;
                
            } catch (StaleElementReferenceException e) {
                attempts++;
                System.out.println("Stale element for search box. Retry attempt: " + attempts);
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    throw new RuntimeException("Failed to find search box after " + MAX_RETRY_ATTEMPTS + " attempts", e);
                }
                
            } catch (TimeoutException e) {
                throw new RuntimeException("Search box not found within timeout period", e);
            }
        }
        
        throw new RuntimeException("Failed to get search box");
    }
    
    /**
     * Perform search with a query
     */
    public void search(String query) {
        System.out.println("🔍 Searching for: " + query);
        
        WebElement searchBox = getSearchBox();
        
        // Clear existing text
        searchBox.clear();
        
        // Enter search query
        searchBox.sendKeys(query);
        
        // Press Enter to search
        searchBox.sendKeys(Keys.RETURN);
        
        // Wait for search results page to load
        waitForSearchResultsPage();
        
        System.out.println("✅ Search executed successfully");
    }
    
    /**
     * Wait for search results page to load
     */
    private void waitForSearchResultsPage() {
        try {
            wait.until(ExpectedConditions.urlContains("search"));
            System.out.println("✓ Search results page loaded");
        } catch (TimeoutException e) {
            System.err.println("⚠️  Warning: URL does not contain 'search'");
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
                
                System.out.println("📌 Found " + pins.size() + " pins");
                return pins;
                
            } catch (StaleElementReferenceException e) {
                attempts++;
                System.out.println("Stale element when getting pins. Retry attempt: " + attempts);
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    System.err.println("⚠️  Failed to get pins after " + MAX_RETRY_ATTEMPTS + " attempts. Returning empty list.");
                    return new ArrayList<>();
                }
                
            } catch (TimeoutException e) {
                System.out.println("⚠️  No pins found on the page");
                return new ArrayList<>();
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Get pins count
     */
    public int getPinsCount() {
        return getPins().size();
    }
    
    /**
     * Check if pins are displayed
     */
    public boolean arePinsDisplayed() {
        return getPinsCount() > 0;
    }
    
    /**
     * Open a pin by index
     */
    public void openPin(int index) {
        List<WebElement> pins = getPins();
        
        if (pins.size() > index) {
            try {
                WebElement pin = pins.get(index);
                wait.until(ExpectedConditions.elementToBeClickable(pin));
                pin.click();
                System.out.println("✅ Opened pin at index: " + index);
            } catch (Exception e) {
                throw new RuntimeException("Failed to open pin at index " + index, e);
            }
        } else {
            throw new RuntimeException("Pin index " + index + " out of bounds. Total pins: " + pins.size());
        }
    }
    
    /**
     * Check if "No results" message is displayed
     */
    public boolean isNoResultsMessageDisplayed() {
        try {
            WebElement noResultsMsg = wait.until(ExpectedConditions.presenceOfElementLocated(noResultsLocator));
            return noResultsMsg.isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }
    
    /**
     * Check if spelling correction/suggestion is displayed
     */
    public boolean isSpellingCorrectionDisplayed() {
        try {
            WebElement correctionMsg = wait.until(ExpectedConditions.presenceOfElementLocated(spellingCorrectionLocator));
            return correctionMsg.isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }
    
    /**
     * Verify search was attempted (URL changed)
     */
    public boolean isSearchAttempted(String baseUrl) {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.contains("search") || !currentUrl.equals(baseUrl);
    }
    
    /**
     * Check if search results page loaded successfully
     */
    public boolean isSearchResultsPageLoaded() {
        return driver.getCurrentUrl().contains("search");
    }
    
    /**
     * Navigate back to home page
     */
    public void navigateToHome(String baseUrl) {
        driver.get(baseUrl);
        
        // Wait for home page to load
        wait.until(ExpectedConditions.presenceOfElementLocated(searchBoxLocator));
        
        System.out.println("✅ Navigated to home page");
    }
    
    /**
     * Clear search box
     */
    public void clearSearch() {
        WebElement searchBox = getSearchBox();
        searchBox.clear();
    }
    
    /**
     * Check if Pinterest handled the search gracefully
     * (either showing results, suggestions, or no results message)
     */
    public boolean isSearchHandledGracefully() {
        return arePinsDisplayed() || 
               isSpellingCorrectionDisplayed() || 
               isNoResultsMessageDisplayed();
    }
}
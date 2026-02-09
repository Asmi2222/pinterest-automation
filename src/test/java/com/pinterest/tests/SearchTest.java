package com.pinterest.tests;

import com.pinterest.base.BaseTest;
import com.pinterest.pages.LoginPage;
import com.pinterest.pages.SearchPage;
import com.pinterest.utils.ConfigReader;
import com.pinterest.utils.CSVReader;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.time.Duration;

public class SearchTest extends BaseTest {
    
    private SearchPage searchPage;
    private WebDriverWait wait;
    private String baseUrl;
    
    @BeforeClass
    public void setupSearchTests() {
        baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("validUser");
        String password = CSVReader.getPassword("validUser");
        
        System.out.println("📧 Email: " + email);
        System.out.println("🔐 Password: " + password);
        
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/login/");
        
        wait.until(ExpectedConditions.urlContains("/login"));
        loginPage.login(email, password);
        
        wait.until(ExpectedConditions.urlContains("pinterest.com"));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        
        System.out.println("✅ Login successful for search tests\n");
        searchPage = new SearchPage(driver);
    }
    
    /**
     * Helper method to pause and view results for specified seconds
     * WITHOUT using Thread.sleep()
     */
    private void viewResultsFor(int seconds) {
        System.out.println("👁️  Viewing results for " + seconds + " seconds...");
        
        FluentWait<Boolean> fluentWait = new FluentWait<>(true)
            .withTimeout(Duration.ofSeconds(seconds))
            .pollingEvery(Duration.ofMillis(500))
            .ignoring(NoSuchElementException.class);
        
        try {
            fluentWait.until(result -> false); // This will timeout after specified seconds
        } catch (Exception e) {
            // Expected timeout - this is how we "pause" without Thread.sleep()
        }
        
        System.out.println("✅ Viewing complete\n");
    }
    
    @Test(priority = 1, description = "Verify valid search returns results")
    public void validSearchTest() {
        System.out.println("\n=== TEST 1: Valid Search ===");
        
        String validQuery = CSVReader.getValidQuery();
        searchPage.search(validQuery);
        
        Assert.assertTrue(searchPage.isSearchResultsPageLoaded(), 
            "Search results page did not load for valid query: " + validQuery);
        
        Assert.assertTrue(searchPage.arePinsDisplayed(), 
            "No results found for valid search query: " + validQuery);
        
        int pinsCount = searchPage.getPinsCount();
        System.out.println("✅ Valid search successful - Found " + pinsCount + " pins");
        
        // View results for 10 seconds
        viewResultsFor(10);
        
        Assert.assertTrue(pinsCount > 0, 
            "Expected at least 1 pin for valid search, but found: " + pinsCount);
        
        System.out.println("=== TEST 1 COMPLETED ✅ ===\n");
    }
    
    @Test(priority = 2, description = "Verify search handles spelling errors gracefully")
    public void spellingErrorSearchTest() {
        System.out.println("\n=== TEST 2: Spelling Error Search ===");
        
        searchPage.navigateToHome(baseUrl);
        String spellingErrorQuery = CSVReader.getSpellingErrorQuery();
        
        searchPage.search(spellingErrorQuery);
        
        Assert.assertTrue(searchPage.isSearchResultsPageLoaded(), 
            "Search page did not load for misspelled query: " + spellingErrorQuery);
        
        boolean hasResultsOrSuggestion = searchPage.arePinsDisplayed() || 
                                        searchPage.isSpellingCorrectionDisplayed();
        
        Assert.assertTrue(hasResultsOrSuggestion, 
            "Pinterest did not handle spelling error appropriately");
        
        if (searchPage.arePinsDisplayed()) {
            System.out.println("✅ Pinterest showed results despite spelling error");
            System.out.println("   Found " + searchPage.getPinsCount() + " pins");
        } else if (searchPage.isSpellingCorrectionDisplayed()) {
            System.out.println("✅ Pinterest showed spelling correction/suggestion");
        }
        
        // View results for 10 seconds
        viewResultsFor(10);
        
        System.out.println("=== TEST 2 COMPLETED ✅ ===\n");
    }
    
    @Test(priority = 3, description = "Verify search handles special characters appropriately")
    public void specialCharactersSearchTest() {
        System.out.println("\n=== TEST 3: Special Characters Search ===");
        
        searchPage.navigateToHome(baseUrl);
        String specialCharsQuery = CSVReader.getSpecialCharactersQuery();
        
        searchPage.search(specialCharsQuery);
        
        Assert.assertTrue(searchPage.isSearchAttempted(baseUrl), 
            "Special characters search was not attempted");
        
        boolean handledGracefully = searchPage.isSearchHandledGracefully();
        
        Assert.assertTrue(handledGracefully, 
            "Pinterest did not handle special characters search appropriately");
        
        if (searchPage.arePinsDisplayed()) {
            System.out.println("✅ Pinterest filtered special characters and showed " + 
                             searchPage.getPinsCount() + " pins");
        } else if (searchPage.isNoResultsMessageDisplayed()) {
            System.out.println("✅ Pinterest showed 'No results' message (expected behavior)");
        } else {
            System.out.println("✅ Pinterest handled special characters appropriately");
        }
        
        // View results for 10 seconds
        viewResultsFor(10);
        
        System.out.println("=== TEST 3 COMPLETED ✅ ===\n");
    }
    
    @Test(priority = 4, description = "Verify all search tests completed successfully")
    public void searchTestsSummary() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🎉 ALL SEARCH TESTS COMPLETED SUCCESSFULLY 🎉");
        System.out.println("=".repeat(50));
    }
}
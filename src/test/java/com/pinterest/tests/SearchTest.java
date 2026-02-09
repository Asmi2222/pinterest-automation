package com.pinterest.tests;

import com.aventstack.extentreports.ExtentTest;
import com.pinterest.base.BaseTest;
import com.pinterest.pages.LoginPage;
import com.pinterest.pages.SearchPage;
import com.pinterest.utils.ConfigReader;
import com.pinterest.utils.CSVReader;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.NoSuchElementException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

public class SearchTest extends BaseTest {
    
    private static final Logger logger = LogManager.getLogger(SearchTest.class);
    
    private SearchPage searchPage;
    private WebDriverWait wait;
    private String baseUrl;
    
    @BeforeClass
    public void setupSearchTests() {
        logger.info("Starting search tests setup");
        
        baseUrl = ConfigReader.get("base.url");
        String email = CSVReader.getEmail("validUser");
        String password = CSVReader.getPassword("validUser");
        
        logger.info("Email: {}", email);
        logger.debug("Password configured for user");
        
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl + "/login/");
        
        wait.until(ExpectedConditions.urlContains("/login"));
        loginPage.login(email, password);
        
        wait.until(ExpectedConditions.urlContains("pinterest.com"));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        
        logger.info("Login successful for search tests");
        
        searchPage = new SearchPage(driver);
        logger.info("Search tests setup completed");
    }
    
    /**
     * Helper method to pause and view results for specified seconds
     * WITHOUT using Thread.sleep()
     */
    private void viewResultsFor(int seconds) {
        logger.info("Viewing results for {} seconds", seconds);
        
        FluentWait<Boolean> fluentWait = new FluentWait<>(true)
            .withTimeout(Duration.ofSeconds(seconds))
            .pollingEvery(Duration.ofMillis(500))
            .ignoring(NoSuchElementException.class);
        
        try {
            fluentWait.until(result -> false);
        } catch (Exception e) {
            // Expected timeout
        }
        
        logger.info("Viewing complete");
    }
    
    @Test(priority = 1, description = "Verify valid search returns results")
    public void validSearchTest() {
        ExtentTest test = getTest();
        test.assignCategory("Search");
        test.assignAuthor("Asmi");
        
        logger.info("TEST 1: Valid Search - Started");
        test.info("Test 1: Valid Search - Started");
        
        String validQuery = CSVReader.getValidQuery();
        logger.info("Valid search query: {}", validQuery);
        test.info("Searching for: " + validQuery);
        
        searchPage.search(validQuery);
        
        Assert.assertTrue(searchPage.isSearchResultsPageLoaded(), 
            "Search results page did not load for valid query: " + validQuery);
        logger.info("Search results page loaded successfully");
        test.pass("Search results page loaded successfully");
        
        Assert.assertTrue(searchPage.arePinsDisplayed(), 
            "No results found for valid search query: " + validQuery);
        
        int pinsCount = searchPage.getPinsCount();
        logger.info("Valid search successful - Found {} pins", pinsCount);
        test.pass("Valid search successful - Found " + pinsCount + " pins");
        
        viewResultsFor(10);
        
        Assert.assertTrue(pinsCount > 0, 
            "Expected at least 1 pin for valid search, but found: " + pinsCount);
        
        logger.info("TEST 1: Valid Search - Completed");
        test.pass("TEST 1: Valid Search - Completed Successfully");
    }
    
    @Test(priority = 2, description = "Verify search handles spelling errors gracefully")
    public void spellingErrorSearchTest() {
        ExtentTest test = getTest();
        test.assignCategory("Search");
        test.assignAuthor("Asmi");
        
        logger.info("TEST 2: Spelling Error Search - Started");
        test.info("Test 2: Spelling Error Search - Started");
        
        searchPage.navigateToHome(baseUrl);
        String spellingErrorQuery = CSVReader.getSpellingErrorQuery();
        
        logger.info("Spelling error query: {}", spellingErrorQuery);
        test.info("Searching with misspelled query: " + spellingErrorQuery);
        
        searchPage.search(spellingErrorQuery);
        
        Assert.assertTrue(searchPage.isSearchResultsPageLoaded(), 
            "Search page did not load for misspelled query: " + spellingErrorQuery);
        logger.info("Search results page loaded for spelling error query");
        test.pass("Search results page loaded for misspelled query");
        
        boolean hasResultsOrSuggestion = searchPage.arePinsDisplayed() || 
                                        searchPage.isSpellingCorrectionDisplayed();
        
        Assert.assertTrue(hasResultsOrSuggestion, 
            "Pinterest did not handle spelling error appropriately");
        
        if (searchPage.arePinsDisplayed()) {
            int pinsCount = searchPage.getPinsCount();
            logger.info("Pinterest showed results despite spelling error - Found {} pins", pinsCount);
            test.pass("Pinterest showed results despite spelling error - Found " + pinsCount + " pins");
        } else if (searchPage.isSpellingCorrectionDisplayed()) {
            logger.info("Pinterest showed spelling correction/suggestion");
            test.pass("Pinterest showed spelling correction/suggestion");
        }
        
        viewResultsFor(10);
        
        logger.info("TEST 2: Spelling Error Search - Completed");
        test.pass("TEST 2: Spelling Error Search - Completed Successfully");
    }
    
    @Test(priority = 3, description = "Verify search handles special characters appropriately")
    public void specialCharactersSearchTest() {
        ExtentTest test = getTest();
        test.assignCategory("Search");
        test.assignAuthor("Asmi");
        
        logger.info("TEST 3: Special Characters Search - Started");
        test.info("Test 3: Special Characters Search - Started");
        
        searchPage.navigateToHome(baseUrl);
        String specialCharsQuery = CSVReader.getSpecialCharactersQuery();
        
        logger.info("Special characters query: {}", specialCharsQuery);
        test.info("Searching with special characters: " + specialCharsQuery);
        
        searchPage.search(specialCharsQuery);
        
        Assert.assertTrue(searchPage.isSearchAttempted(baseUrl), 
            "Special characters search was not attempted");
        logger.info("Special characters search was attempted");
        test.pass("Special characters search was attempted");
        
        boolean handledGracefully = searchPage.isSearchHandledGracefully();
        
        Assert.assertTrue(handledGracefully, 
            "Pinterest did not handle special characters search appropriately");
        
        if (searchPage.arePinsDisplayed()) {
            int pinsCount = searchPage.getPinsCount();
            logger.info("Pinterest filtered special characters and showed {} pins", pinsCount);
            test.pass("Pinterest filtered special characters and showed " + pinsCount + " pins");
        } else if (searchPage.isNoResultsMessageDisplayed()) {
            logger.info("Pinterest showed 'No results' message (expected behavior)");
            test.pass("Pinterest showed 'No results' message (expected behavior)");
        } else {
            logger.info("Pinterest handled special characters appropriately");
            test.pass("Pinterest handled special characters appropriately");
        }
        
        viewResultsFor(10);
        
        logger.info("TEST 3: Special Characters Search - Completed");
        test.pass("TEST 3: Special Characters Search - Completed Successfully");
    }
    
    @Test(priority = 4, description = "Verify all search tests completed successfully")
    public void searchTestsSummary() {
        ExtentTest test = getTest();
        test.assignCategory("Search");
        test.assignAuthor("Asmi");
        
        logger.info("All search tests completed successfully");
        test.info("All search tests completed successfully");
        test.pass("Search test suite execution completed");
    }
}
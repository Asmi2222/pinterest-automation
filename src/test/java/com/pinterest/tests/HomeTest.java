package com.pinterest.tests;

import com.aventstack.extentreports.ExtentTest;
import com.pinterest.base.BaseTest;
import com.pinterest.pages.HomePage;
import com.pinterest.utils.ConfigReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class HomeTest extends BaseTest {

    private static final Logger logger = LogManager.getLogger(HomeTest.class);

    // Configuration: Set viewing duration
    private static final int VIEW_RESULTS_DURATION_SECONDS = 10;

    /**
     * Helper method to pause and view results for specified seconds
     */
    private void viewResultsFor(int seconds, ExtentTest test) {
        if (seconds <= 0) {
            return;
        }

        logger.info("Viewing results for {} seconds...", seconds);
        if (test != null) test.info("Viewing results for " + seconds + " seconds...");

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
        if (test != null) test.pass("Viewing complete");
    }

    @Test(priority = 1, description = "Verify Pinterest home page loads and scrolling works")
    public void verifyHomePageAndScroll() {
        ExtentTest test = getTest(); // Provided by BaseTest
        test.assignCategory("Home").assignAuthor("Asmi");

        logger.info("=== TEST: Home Page Load and Scroll ===");
        test.info("Starting: Home Page Load and Scroll");

        String baseUrl = ConfigReader.get("base.url");

        // Step 1: Navigate to Pinterest home page
        HomePage homePage = new HomePage(driver, test); // pass test for step logging
        homePage.open(baseUrl);

        // View the home page
        viewResultsFor(5, test);

        // Assert: Verify home page loaded
        Assert.assertTrue(
                homePage.isHomePageLoaded(),
                "Home page failed to load - not on pinterest.com"
        );
        logger.info("Home page loaded successfully");
        test.pass("Home page loaded successfully");

        // Step 2: Scroll Test
        logger.info("--- Starting Scroll Test ---");
        test.info("--- Starting Scroll Test ---");

        // Get initial position
        double initialPosition = homePage.getScrollPosition();

        // First scroll - using PAGE_DOWN keys (3 times)
        logger.info("First Scroll (using PAGE_DOWN)");
        test.info("First Scroll (using PAGE_DOWN)");
        homePage.scrollDownWithKeyboard(3);

        // View after first scroll
        viewResultsFor(3, test);

        // Check if first scroll worked
        double positionAfterFirst = homePage.getScrollPosition();
        Assert.assertTrue(
                homePage.didPageScroll(initialPosition),
                "First scroll failed - page did not scroll"
        );
        logger.info("First scroll successful (from {} to {})", initialPosition, positionAfterFirst);
        test.pass("First scroll successful");

        // Second scroll - using PAGE_DOWN keys (3 more times)
        logger.info("Second Scroll (using PAGE_DOWN)");
        test.info("Second Scroll (using PAGE_DOWN)");
        homePage.scrollDownWithKeyboard(3);

        // View after second scroll
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS, test);

        // Check if second scroll worked
        double finalPosition = homePage.getScrollPosition();
        double totalScrolled = finalPosition - initialPosition;
        logger.info("Total scrolled: {} pixels", totalScrolled);
        test.info("Total scrolled: " + totalScrolled + " pixels");

        // Final assertion - just verify we're not at position 0
        Assert.assertTrue(
                finalPosition > initialPosition,
                "Page did not scroll during the test"
        );

        logger.info("Scroll test completed successfully");
        test.pass("Scroll test completed successfully");
        logger.info("=== TEST COMPLETED ===");
        test.info("=== TEST COMPLETED ===");
    }
}

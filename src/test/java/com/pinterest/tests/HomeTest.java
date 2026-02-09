package com.pinterest.tests;

import com.pinterest.base.BaseTest;
import com.pinterest.pages.HomePage;
import com.pinterest.utils.ConfigReader;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;

public class HomeTest extends BaseTest {
    
    // Configuration: Set viewing duration
    private static final int VIEW_RESULTS_DURATION_SECONDS = 10;
    
    /**
     * Helper method to pause and view results for specified seconds
     */
    private void viewResultsFor(int seconds) {
        if (seconds <= 0) {
            return;
        }
        
        System.out.println("👁️  Viewing results for " + seconds + " seconds...");
        
        FluentWait<Boolean> fluentWait = new FluentWait<>(true)
            .withTimeout(Duration.ofSeconds(seconds))
            .pollingEvery(Duration.ofMillis(500))
            .ignoring(NoSuchElementException.class);
        
        try {
            fluentWait.until(result -> false);
        } catch (Exception e) {
            // Expected timeout
        }
        
        System.out.println("✅ Viewing complete\n");
    }
    
    @Test(priority = 1, description = "Verify Pinterest home page loads and scrolling works")
    public void verifyHomePageAndScroll() {
        System.out.println("\n=== TEST: Home Page Load and Scroll ===");
        
        String baseUrl = ConfigReader.get("base.url");
        
        // Step 1: Navigate to Pinterest home page
        HomePage homePage = new HomePage(driver);
        homePage.open(baseUrl);
        
        // View the home page
        viewResultsFor(5);
        
        // Assert: Verify home page loaded
        Assert.assertTrue(homePage.isHomePageLoaded(), 
            "Home page failed to load - not on pinterest.com");
        
        System.out.println("✅ Home page loaded successfully\n");
        
        // Step 2: Scroll Test
        System.out.println("--- Starting Scroll Test ---\n");
        
        // Get initial position
        double initialPosition = homePage.getScrollPosition();
        
        // First scroll - using PAGE_DOWN keys (3 times)
        System.out.println("🔽 First Scroll (using PAGE_DOWN):");
        homePage.scrollDownWithKeyboard(3);
        
        // View after first scroll
        viewResultsFor(3);
        
        // Check if first scroll worked
        double positionAfterFirst = homePage.getScrollPosition();
        Assert.assertTrue(homePage.didPageScroll(initialPosition), 
            "First scroll failed - page did not scroll");
        
        System.out.println("✅ First scroll successful\n");
        
        // Second scroll - using PAGE_DOWN keys (3 more times)
        System.out.println("🔽 Second Scroll (using PAGE_DOWN):");
        homePage.scrollDownWithKeyboard(3);
        
        // View after second scroll
        viewResultsFor(VIEW_RESULTS_DURATION_SECONDS);
        
        // Check if second scroll worked
        double finalPosition = homePage.getScrollPosition();
        System.out.println("📊 Total scrolled: " + (finalPosition - initialPosition) + " pixels");
        
        // Final assertion - just verify we're not at position 0
        Assert.assertTrue(finalPosition > initialPosition, 
            "Page did not scroll during the test");
        
        System.out.println("✅ Scroll test completed successfully");
        System.out.println("\n=== TEST COMPLETED ✅ ===\n");
    }
}
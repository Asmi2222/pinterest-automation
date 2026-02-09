package com.pinterest.pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class HomePage {
    
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private Actions actions;
    
    private static final int WAIT_TIMEOUT_SECONDS = 15;
    
    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
        this.js = (JavascriptExecutor) driver;
        this.actions = new Actions(driver);
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Navigate to home page
     */
    public void open(String url) {
        driver.get(url);
        waitForPageLoad();
    }
    
    /**
     * Wait for page to load completely
     */
    private void waitForPageLoad() {
        wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));
        System.out.println("✅ Home page loaded");
    }
    
    /**
     * Check if home page loaded successfully
     */
    public boolean isHomePageLoaded() {
        String currentUrl = driver.getCurrentUrl();
        boolean loaded = currentUrl.contains("pinterest.com");
        
        if (loaded) {
            System.out.println("✅ Home page loaded successfully - URL: " + currentUrl);
        } else {
            System.out.println("❌ Home page did not load - URL: " + currentUrl);
        }
        
        return loaded;
    }
    
    /**
     * Get current scroll position
     */
    public double getScrollPosition() {
        try {
            Number scrollPos = (Number) js.executeScript("return window.pageYOffset || document.documentElement.scrollTop;");
            return scrollPos.doubleValue();
        } catch (Exception e) {
            System.out.println("⚠️  Could not get scroll position: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Scroll down using keyboard (PAGE_DOWN) - Most reliable method
     */
    public void scrollDownWithKeyboard(int times) {
        double initialPosition = getScrollPosition();
        System.out.println("📍 Initial scroll position: " + initialPosition);
        
        for (int i = 0; i < times; i++) {
            actions.sendKeys(Keys.PAGE_DOWN).perform();
            
            // Small pause between key presses
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        double newPosition = getScrollPosition();
        double scrolled = newPosition - initialPosition;
        
        System.out.println("📍 New scroll position: " + newPosition);
        System.out.println("✅ Scrolled " + scrolled + " pixels using " + times + " PAGE_DOWN key(s)");
    }
    
    /**
     * Scroll down using Actions - sendKeys with ARROW_DOWN
     */
    public void scrollDownWithArrowKeys(int times) {
        double initialPosition = getScrollPosition();
        System.out.println("📍 Initial scroll position: " + initialPosition);
        
        for (int i = 0; i < times; i++) {
            actions.sendKeys(Keys.ARROW_DOWN).perform();
        }
        
        // Wait for scroll
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        double newPosition = getScrollPosition();
        double scrolled = newPosition - initialPosition;
        
        System.out.println("📍 New scroll position: " + newPosition);
        System.out.println("✅ Scrolled " + scrolled + " pixels using arrow keys");
    }
    
    /**
     * Scroll using executeScript - alternative method
     */
    public void scrollDownWithScript(int pixels) {
        double initialPosition = getScrollPosition();
        System.out.println("📍 Initial scroll position: " + initialPosition);
        
        // Try multiple scroll methods
        try {
            js.executeScript("window.scrollTo(0, " + pixels + ");");
        } catch (Exception e1) {
            try {
                js.executeScript("document.documentElement.scrollTop = " + pixels + ";");
            } catch (Exception e2) {
                js.executeScript("document.body.scrollTop = " + pixels + ";");
            }
        }
        
        // Wait for scroll
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        double newPosition = getScrollPosition();
        double scrolled = newPosition - initialPosition;
        
        System.out.println("📍 New scroll position: " + newPosition);
        System.out.println("✅ Scrolled " + scrolled + " pixels");
    }
    
    /**
     * Verify any scroll happened
     */
    public boolean didPageScroll(double initialPosition) {
        double currentPosition = getScrollPosition();
        double scrolled = currentPosition - initialPosition;
        
        if (scrolled > 0) {
            System.out.println("✅ Page scrolled successfully: " + scrolled + " pixels");
            return true;
        } else {
            System.out.println("⚠️  Page did not scroll");
            return false;
        }
    }
}
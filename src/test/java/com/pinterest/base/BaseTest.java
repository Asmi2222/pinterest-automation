package com.pinterest.base;

import com.pinterest.utils.ConfigReader;
import com.pinterest.utils.ExtentManager;
import com.pinterest.utils.DriverFactory;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;

import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils; // requires commons-io (added in Step 1)

public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    // === Extent fields ===
    protected ExtentReports extent;               // already in your class
    private static final ThreadLocal<ExtentTest> tlTest = new ThreadLocal<>(); // NEW

    protected ExtentTest getTest() { return tlTest.get(); }                    // NEW
    protected void setTest(ExtentTest t) { tlTest.set(t); }                    // NEW
    protected void unloadTest() { tlTest.remove(); }                           // NEW

    @BeforeSuite
    public void setupReport() {
        extent = ExtentManager.getExtentReport(); // keep using your manager
    }

    @BeforeClass(alwaysRun = true)  // your existing lifecycle
    public void setUp() {
        String browser = ConfigReader.get("browser");
        boolean headless = ConfigReader.getBoolean("headless");

        driver = DriverFactory.createInstance(browser, headless);
        if (driver == null) {
            throw new RuntimeException("WebDriver initialization failed. Check DriverFactory.");
        }

        int explicitWait = Integer.parseInt(ConfigReader.get("explicit.wait"));
        wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));

        driver.get(getBaseUrl());
    }

    @AfterClass(alwaysRun = true)   // your existing lifecycle
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (extent != null) {
            extent.flush();         // keep final flush here
        }
    }

    // =========================
    // NEW: Per-test wiring
    // =========================
    @BeforeMethod(alwaysRun = true)
    public void startTest(Method method) {
        // Example name: LogoutTest - validLogout
        String testName = method.getDeclaringClass().getSimpleName() + " - " + method.getName();
        ExtentTest test = extent.createTest(testName)
                                .assignCategory(method.getDeclaringClass().getSimpleName()); // optional category
        setTest(test);
    }

    @AfterMethod(alwaysRun = true)
    public void logStatusAndAttachArtifacts(ITestResult result) {
        ExtentTest test = getTest();
        try {
            if (test != null) {
                switch (result.getStatus()) {
                    case ITestResult.SUCCESS:
                        test.pass("✅ Test Passed");
                        break;
                    case ITestResult.SKIP:
                        test.skip("⏭ Test Skipped: " +
                                (result.getThrowable() != null ? result.getThrowable().getMessage() : ""));
                        break;
                    case ITestResult.FAILURE:
                        String message = (result.getThrowable() != null)
                                ? result.getThrowable().toString()
                                : "Test Failed";

                        String screenshotPath = takeScreenshot(result.getMethod().getMethodName());
                        if (screenshotPath != null && !screenshotPath.isEmpty()) {
                            test.fail("❌ Failure: " + message,
                                    MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
                        } else {
                            test.fail("❌ Failure: " + message + " (screenshot not available)");
                        }
                        break;
                }
            }
        } catch (Exception e) {
            if (test != null) test.warning("Could not attach screenshot due to: " + e.getMessage());
        } finally {
            unloadTest();               // prevent leakage in parallel runs
            if (extent != null) {
                extent.flush();         // flush after each test for immediate writes
            }
        }
    }

    // =========================
    // NEW: Screenshot helper
    // =========================
    protected String takeScreenshot(String name) {
        try {
            if (driver == null) return "";
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            // Prefer the directory from ExtentManager if you added getScreenshotDir(); else fallback
            String screenshotDir;
            try {
                // If you implemented getScreenshotDir() in ExtentManager (Step 2 optional)
                screenshotDir = com.pinterest.utils.ExtentManager.getScreenshotDir();
            } catch (Throwable ignored) {
                // Fallback if the helper method doesn't exist
                screenshotDir = System.getProperty("user.dir") + "/test-output/screenshots/";
            }

            File folder = new File(screenshotDir);
            if (!folder.exists()) folder.mkdirs();

            String path = screenshotDir + name + "_" + timestamp + ".png";
            FileUtils.copyFile(src, new File(path));
            return path;
        } catch (WebDriverException | IOException e) {
            return "";
        }
    }

    // ========= Existing helpers =========
    protected String getBaseUrl() {
        return ConfigReader.get("base.url");
    }

    protected void navigateToPath(String path) {
        driver.get(getBaseUrl() + path);
    }

    protected <T> T initPage(Class<T> pageClass) {
        return PageFactory.initElements(driver, pageClass);
    }
}
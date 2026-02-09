package com.pinterest.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentManager {

    private static ExtentReports extent;
    private static String reportPath; // <-- store path for later use
    private static final String SCREENSHOT_DIR = System.getProperty("user.dir") + "/test-output/screenshots/";

    // Optional: expose the path for logging/CI usage
    public static String getReportPath() {
        return reportPath;
    }

    // Optional: expose screenshot directory (so BaseTest can reuse the same path)
    public static String getScreenshotDir() {
        return SCREENSHOT_DIR;
    }

    public static synchronized ExtentReports getExtentReport() { // <-- synchronized for safety
        if (extent == null) {
            String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            reportPath = System.getProperty("user.dir")
                    + "/test-output/ExtentReport_" + timestamp + ".html";

            // Ensure screenshots dir exists (helpful for saving images)
            File ssDir = new File(SCREENSHOT_DIR);
            if (!ssDir.exists()) {
                ssDir.mkdirs();
            }

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setReportName("Automation Test Report");
            sparkReporter.config().setDocumentTitle("Test Execution Report");
            sparkReporter.config().setTheme(Theme.DARK);

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);

            extent.setSystemInfo("Project", "Pinterest Automation");
            extent.setSystemInfo("Tester", "Asmi Bajracharya");
            extent.setSystemInfo("Environment", "QA");
        }
        return extent;
    }
}
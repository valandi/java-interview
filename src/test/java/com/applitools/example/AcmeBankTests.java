package com.applitools.example;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.EyesRunner;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.TestResultContainer;
import com.applitools.eyes.TestResultsSummary;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.selenium.Configuration;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.eyes.visualgrid.model.DesktopBrowserInfo;
import com.applitools.eyes.visualgrid.services.RunnerOptions;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AcmeBankTests {

    private static final BatchInfo BATCH = new BatchInfo("Selenium Java Basic Quickstart");

    public static void main(String[] args) {
        int threadCount = 3;
        VisualGridRunner runner = new VisualGridRunner(new RunnerOptions().testConcurrency(threadCount));
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            int threadIndex = i;
            Thread t = new Thread(() -> runTest(threadIndex, runner));
            threads.add(t);
            t.start();
        }

        // Now collect and print results
        TestResultsSummary allTestResults = runner.getAllTestResults(false);
        for (TestResultContainer container : allTestResults.getAllResults()) {
            if (container.getTestResults() != null) {
                System.out.println(container.getTestResults());
            } else if (container.getException() != null) {
                System.err.println("Test exception: " + container.getException().getMessage());
            }
        }

        System.exit(0);
    }

    public static void runTest(int index, EyesRunner runner) {
        System.out.println("[Thread " + index + "] Starting test...");

        Eyes eyes = new Eyes(runner);
        WebDriver driver = null;

        try {
            String apiKey = System.getenv("APPLITOOLS_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("APPLITOOLS_API_KEY environment variable is not set");
            }

            Configuration config = eyes.getConfiguration();
            config.setApiKey(apiKey);
            config.setBatch(BATCH);
            config.addBrowsers(new DesktopBrowserInfo(800, 1024, BrowserType.CHROME));
            eyes.setConfiguration(config);

            ChromeOptions options = new ChromeOptions().addArguments("--headless=new");
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            String testName = "Threaded Test #" + index;
            eyes.open(driver, "ACME Bank", testName, new RectangleSize(1200, 600));

            driver.get("https://sandbox.applitools.com/bank");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));

            eyes.check(Target.window().fully().withName("Login page"));

            driver.findElement(By.id("user")).sendKeys("Chris");
            driver.findElement(By.id("pass")).sendKeys("CorrectHorseBatteryStaple");
            driver.findElement(By.id("log-in")).click();

            eyes.check(Target.window().fully().withName("Main page"));

            eyes.closeAsync();

        } catch (Exception e) {
            System.err.println("[Thread " + index + "] Test failed with error:");
            e.printStackTrace();
            eyes.abortAsync();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}

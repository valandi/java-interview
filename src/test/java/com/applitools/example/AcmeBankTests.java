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
import com.applitools.eyes.visualgrid.model.ChromeEmulationInfo;
import com.applitools.eyes.visualgrid.model.DesktopBrowserInfo;
import com.applitools.eyes.visualgrid.model.DeviceName;
import com.applitools.eyes.visualgrid.model.ScreenOrientation;
import com.applitools.eyes.visualgrid.services.RunnerOptions;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;

public class AcmeBankTests {

    private final static BatchInfo BATCH = new BatchInfo("Selenium Java Basic Quickstart");

    public static void main(String [] args) {

        EyesRunner runner = null;
        Eyes eyes = null;
        WebDriver driver = null;

        try {
            // Configure Applitools SDK to run on the Ultrafast Grid
            runner = new VisualGridRunner(new RunnerOptions().testConcurrency(5));
            eyes = new Eyes(runner);
            Configuration config = eyes.getConfiguration();
            config.setApiKey(System.getenv("APPLITOOLS_API_KEY"));
            config.setBatch(BATCH);
            config.addBrowsers(
                new DesktopBrowserInfo(800, 1024, BrowserType.CHROME),
            );
            eyes.setConfiguration(config);
            ChromeOptions options = new ChromeOptions().addArguments("--headless=new");
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));

            // Start Applitools Visual AI Test
            eyes.open(driver,"ACME Bank", "Selenium Java Basic: Quickstart", new RectangleSize(1200, 600));
            driver.get("https://sandbox.applitools.com/bank");

            // Full Page - Visual AI Assertion
            eyes.check(Target.window().fully().withName("Login page"));

            driver.findElement(By.id("username")).sendKeys("Chris");
            driver.findElement(By.id("password")).sendKeys("CorrectHorseBatteryStaple");
            driver.findElement(By.id("log-in")).click();

            // Full Page - Visual AI Assertion
            eyes.check(
                Target.window().fully().withName("Main page")
            );

            eyes.closeAsync();
        }
        catch (Exception e) {
            e.printStackTrace();
            if (eyes != null)
                eyes.abortAsync();
        } finally {
            if (driver != null)
                driver.quit();

            if (runner != null) {
                TestResultsSummary allTestResults = runner.getAllTestResults();
                for (TestResultContainer container : allTestResults.getAllResults()) {
                    if (container.getTestResults() != null && !container.getTestResults().isPassed()) {
                        System.exit(1);
                    }
                }
            }

            System.exit(0); 
        }

            System.exit(0);
    }
}
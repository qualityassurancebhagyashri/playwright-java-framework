package dev.qa.core;

import dev.qa.config.Config;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayInputStream;

/**
 * One WebDriver per test method, held in a ThreadLocal because TestNG runs methods of the
 * same class instance in parallel. On a Grid, each parallel test uses its own Grid session.
 */
public abstract class BaseSeleniumTest {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    protected WebDriver driver() {
        return DRIVER.get();
    }

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        DRIVER.set(DriverFactory.create());
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowser(ITestResult result) {
        WebDriver driver = DRIVER.get();
        try {
            if (driver instanceof RemoteWebDriver remote) {
                String where = Config.gridUrl().isBlank() ? "local" : Config.gridUrl();
                Allure.addAttachment("Browser session", "text/plain",
                        "browser=" + Config.browser() + "\nrun on=" + where
                                + "\nsession id=" + remote.getSessionId());
            }
            if (result.getStatus() == ITestResult.FAILURE && driver instanceof TakesScreenshot shot) {
                byte[] png = shot.getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("Screenshot on failure", "image/png", new ByteArrayInputStream(png), ".png");
            }
        } catch (Exception e) {
            System.err.println("Could not attach failure evidence: " + e.getMessage());
        } finally {
            if (driver != null) driver.quit(); // always release the Grid session
            DRIVER.remove();
        }
    }
}

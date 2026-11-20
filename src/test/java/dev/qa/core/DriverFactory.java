package dev.qa.core;

import dev.qa.config.Config;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * Creates a Selenium WebDriver. With no grid URL it starts the browser on this machine
 * (Selenium Manager downloads the driver). With a grid URL it asks the Selenium Grid hub
 * for a session: the hub picks a node that matches the requested browser.
 */
public final class DriverFactory {
    private DriverFactory() {}

    public static WebDriver create() {
        String browser = Config.browser();
        String grid = Config.gridUrl();
        boolean remote = !grid.isBlank();

        MutableCapabilities options;
        switch (browser) {
            case "chrome" -> options = chromeOptions(remote);
            case "firefox" -> options = firefoxOptions(remote);
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser + " (use chrome or firefox)");
        }

        if (remote) {
            try {
                return new RemoteWebDriver(URI.create(grid).toURL(), options);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Bad grid URL: " + grid, e);
            }
        }
        return "firefox".equals(browser)
                ? new FirefoxDriver((FirefoxOptions) options)
                : new ChromeDriver((ChromeOptions) options);
    }

    // On a Grid node the browser already has a virtual display, so headless is only for local runs.
    private static ChromeOptions chromeOptions(boolean remote) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080");
        if (!remote && Config.headless()) options.addArguments("--headless=new");
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean remote) {
        FirefoxOptions options = new FirefoxOptions();
        if (!remote && Config.headless()) options.addArguments("-headless");
        return options;
    }
}

package dev.qa.pages.selenium;

import dev.qa.config.Config;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Selenium page object: actions and reads only, no assertions. Explicit waits, no implicit waits. */
public class SeleniumLoginPage {
    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN = By.id("login-button");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public SeleniumLoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Step("Open login page")
    public SeleniumLoginPage open() {
        driver.get(Config.baseUrl());
        return this;
    }

    @Step("Log in as {user}")
    public SeleniumInventoryPage loginAs(String user, String pass) {
        submit(user, pass);
        return new SeleniumInventoryPage(driver);
    }

    @Step("Attempt login as {user} (expecting failure)")
    public SeleniumLoginPage attemptLogin(String user, String pass) {
        submit(user, pass);
        return this;
    }

    public String errorText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR)).getText();
    }

    private void submit(String user, String pass) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME)).sendKeys(user);
        driver.findElement(PASSWORD).sendKeys(pass);
        driver.findElement(LOGIN).click();
    }
}

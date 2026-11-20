package dev.qa.pages.selenium;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SeleniumInventoryPage {
    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By ITEMS = By.cssSelector("[data-test='inventory-item']");
    private static final By CART_BADGE = By.cssSelector("[data-test='shopping-cart-badge']");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public SeleniumInventoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String title() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(TITLE)).getText();
    }

    public int itemCount() {
        return wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(ITEMS, 0)).size();
    }

    @Step("Add '{productSlug}' to cart")
    public SeleniumInventoryPage addToCart(String productSlug) {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-test='add-to-cart-" + productSlug + "']"))).click();
        return this;
    }

    public String cartBadgeText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(CART_BADGE)).getText();
    }
}

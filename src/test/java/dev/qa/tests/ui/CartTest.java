package dev.qa.tests.ui;

import dev.qa.config.Config;
import dev.qa.core.BaseUiTest;
import dev.qa.pages.LoginPage;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Epic("Shopping")
@Feature("Cart")
public class CartTest extends BaseUiTest {

    @Test(groups = "regression", description = "Adding two items shows a cart badge of 2")
    @Story("Adding items updates the cart badge")
    @Severity(SeverityLevel.NORMAL)
    public void addingTwoItemsShowsBadgeOfTwo() {
        var inventory = new LoginPage(page()).open()
                .loginAs(Config.username(), Config.password())
                .addToCart("Sauce Labs Backpack")
                .addToCart("Sauce Labs Bike Light");

        assertThat(inventory.cartBadge()).hasText("2");
    }
}

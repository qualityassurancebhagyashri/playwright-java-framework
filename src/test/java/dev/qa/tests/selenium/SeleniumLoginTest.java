package dev.qa.tests.selenium;

import dev.qa.config.Config;
import dev.qa.core.BaseSeleniumTest;
import dev.qa.pages.selenium.SeleniumLoginPage;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium WebDriver tests. They run on a local browser by default, or on a Selenium Grid
 * when a grid URL is given (-DgridUrl=http://localhost:4444). Excluded from the default
 * `mvn test`; select them with -Dgroups=selenium.
 */
@Epic("Authentication")
@Feature("Login (Selenium)")
public class SeleniumLoginTest extends BaseSeleniumTest {

    @Test(groups = "selenium", description = "Valid user sees the product list")
    @Story("Valid user can log in")
    @Severity(SeverityLevel.BLOCKER)
    public void validUserSeesProductList() {
        var inventory = new SeleniumLoginPage(driver()).open()
                .loginAs(Config.username(), Config.password());

        assertThat(inventory.title()).isEqualTo("Products");
        assertThat(inventory.itemCount()).isEqualTo(6);
    }

    @Test(groups = "selenium", description = "Locked user sees a clear error")
    @Story("Locked user is rejected")
    @Severity(SeverityLevel.CRITICAL)
    public void lockedUserSeesClearError() {
        var login = new SeleniumLoginPage(driver()).open()
                .attemptLogin("locked_out_user", Config.password());

        assertThat(login.errorText()).contains("locked out");
    }

    @Test(groups = "selenium", description = "Wrong password shows an error")
    @Story("Wrong password is rejected")
    @Severity(SeverityLevel.CRITICAL)
    public void wrongPasswordShowsError() {
        var login = new SeleniumLoginPage(driver()).open()
                .attemptLogin(Config.username(), "wrong");

        assertThat(login.errorText()).contains("do not match");
    }

    @Test(groups = "selenium", description = "Adding two items shows a cart badge of 2")
    @Story("Adding items updates the cart badge")
    @Severity(SeverityLevel.NORMAL)
    public void addingTwoItemsShowsBadgeOfTwo() {
        var inventory = new SeleniumLoginPage(driver()).open()
                .loginAs(Config.username(), Config.password())
                .addToCart("sauce-labs-backpack")
                .addToCart("sauce-labs-bike-light");

        assertThat(inventory.cartBadgeText()).isEqualTo("2");
    }
}

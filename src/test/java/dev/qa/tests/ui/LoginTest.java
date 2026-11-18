package dev.qa.tests.ui;

import dev.qa.config.Config;
import dev.qa.core.BaseUiTest;
import dev.qa.pages.LoginPage;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Epic("Authentication")
@Feature("Login")
public class LoginTest extends BaseUiTest {

    @Test(groups = "smoke", description = "Valid user sees the product list")
    @Story("Valid user can log in")
    @Severity(SeverityLevel.BLOCKER)
    public void validUserSeesProductList() {
        var inventory = new LoginPage(page()).open()
                .loginAs(Config.username(), Config.password());

        assertThat(inventory.title()).hasText("Products");
        assertThat(inventory.items()).hasCount(6);
    }

    @Test(groups = "smoke", description = "Locked user sees a clear error")
    @Story("Locked user is rejected")
    @Severity(SeverityLevel.CRITICAL)
    public void lockedUserSeesClearError() {
        var login = new LoginPage(page()).open().attemptLogin("locked_out_user", Config.password());

        assertThat(login.errorMessage()).containsText("locked out");
    }

    @Test(groups = "smoke", description = "Wrong password shows an error")
    @Story("Wrong password is rejected")
    @Severity(SeverityLevel.CRITICAL)
    public void wrongPasswordShowsError() {
        var login = new LoginPage(page()).open().attemptLogin(Config.username(), "wrong");

        assertThat(login.errorMessage()).containsText("do not match");
    }
}

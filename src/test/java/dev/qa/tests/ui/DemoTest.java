package dev.qa.tests.ui;

import com.microsoft.playwright.options.AriaRole;
import dev.qa.core.BaseUiTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Epic("Authentication")
@Feature("Login")
public class DemoTest extends BaseUiTest {

    @Test(groups = "regression", description = "Login page shows the username, password and Login button")
    @Story("Login form is visible")
    public void loginPageShowsAllFormFields() {
        page().navigate("/");

        assertThat(page().getByPlaceholder("Username")).isVisible();
        assertThat(page().getByPlaceholder("Password")).isVisible();
        assertThat(page().getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Login"))).isVisible();
    }
}

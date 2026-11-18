package dev.qa.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class LoginPage {
    private final Page page;
    private final Locator username;
    private final Locator password;
    private final Locator loginButton;
    private final Locator error;

    public LoginPage(Page page) {
        this.page = page;
        this.username = page.getByPlaceholder("Username");
        this.password = page.getByPlaceholder("Password");
        this.loginButton = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Login"));
        this.error = page.locator("[data-test='error']");
    }

    @Step("Open login page")
    public LoginPage open() {
        page.navigate("/");
        return this;
    }

    @Step("Log in as {user}")
    public InventoryPage loginAs(String user, String pass) {
        username.fill(user);
        password.fill(pass);
        loginButton.click();
        return new InventoryPage(page);
    }

    @Step("Attempt login as {user} (expecting failure)")
    public LoginPage attemptLogin(String user, String pass) {
        username.fill(user);
        password.fill(pass);
        loginButton.click();
        return this;
    }

    public Locator errorMessage() {
        return error;
    }
}

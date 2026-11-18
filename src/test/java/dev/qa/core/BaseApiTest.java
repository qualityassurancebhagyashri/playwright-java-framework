package dev.qa.core;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import dev.qa.config.Config;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/** Thread-local API client, same reason as BaseUiTest: TestNG shares the class instance across threads. */
public abstract class BaseApiTest {

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<APIRequestContext> API = new ThreadLocal<>();

    protected APIRequestContext api() {
        return API.get();
    }

    @BeforeMethod(alwaysRun = true)
    public void setUpApi() {
        Playwright playwright = Playwright.create();
        PLAYWRIGHT.set(playwright);
        API.set(playwright.request().newContext(
                new APIRequest.NewContextOptions().setBaseURL(Config.apiBaseUrl())));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownApi() {
        if (API.get() != null) API.get().dispose();
        if (PLAYWRIGHT.get() != null) PLAYWRIGHT.get().close();
        API.remove();
        PLAYWRIGHT.remove();
    }
}

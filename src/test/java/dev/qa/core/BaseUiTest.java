package dev.qa.core;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import dev.qa.config.Config;
import io.qameta.allure.Allure;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * One Playwright + browser + context per test method, held in ThreadLocals.
 *
 * Why ThreadLocal: TestNG runs methods of the SAME class instance in parallel, so plain
 * instance fields would be shared between threads and tests would trample each other.
 * Tests call page() and always get the page that belongs to their own thread.
 */
public abstract class BaseUiTest {

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    protected Page page() {
        return PAGE.get();
    }

    @BeforeMethod(alwaysRun = true)
    public void launch() {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(Config.headless()));
        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setBaseURL(Config.baseUrl()));
        context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
        Page page = context.newPage();
        page.setDefaultTimeout(10_000);

        PLAYWRIGHT.set(playwright);
        BROWSER.set(browser);
        CONTEXT.set(context);
        PAGE.set(page);
    }

    @AfterMethod(alwaysRun = true)
    public void close(ITestResult result) {
        Page page = PAGE.get();
        BrowserContext context = CONTEXT.get();
        try {
            if (result.getStatus() == ITestResult.FAILURE && page != null) {
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                byte[] png = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                Allure.addAttachment("Screenshot on failure", "image/png",
                        new ByteArrayInputStream(png), ".png");
                Path trace = Paths.get("target", "traces", safe(result.getMethod().getMethodName()) + ".zip");
                context.tracing().stop(new Tracing.StopOptions().setPath(trace));
                try (FileInputStream in = new FileInputStream(trace.toFile())) {
                    Allure.addAttachment("Playwright trace", "application/zip", in, ".zip");
                }
            } else if (context != null) {
                context.tracing().stop();
            }
        } catch (Exception e) {
            System.err.println("Could not attach failure evidence: " + e.getMessage());
        } finally {
            if (context != null) context.close();
            if (BROWSER.get() != null) BROWSER.get().close();
            if (PLAYWRIGHT.get() != null) PLAYWRIGHT.get().close();
            PAGE.remove();
            CONTEXT.remove();
            BROWSER.remove();
            PLAYWRIGHT.remove();
        }
    }

    private static String safe(String s) {
        return s.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}

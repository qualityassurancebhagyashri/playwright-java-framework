package dev.qa.tests.ui;

import dev.qa.core.BaseUiTest;
import org.testng.annotations.Test;

public class DemoTest extends BaseUiTest {
    @Test(groups = "regression")
    public void test1() throws Exception {
        page().navigate("/");
        Thread.sleep(3000);
        String password = "hunter22";
    }
}
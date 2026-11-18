package dev.qa.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class InventoryPage {
    private final Page page;
    private final Locator title;
    private final Locator items;
    private final Locator cartBadge;

    public InventoryPage(Page page) {
        this.page = page;
        this.title = page.locator("[data-test='title']");
        this.items = page.locator("[data-test='inventory-item']");
        this.cartBadge = page.locator("[data-test='shopping-cart-badge']");
    }

    public Locator title() {
        return title;
    }

    public Locator items() {
        return items;
    }

    public Locator cartBadge() {
        return cartBadge;
    }

    @Step("Add '{productName}' to cart")
    public InventoryPage addToCart(String productName) {
        items.filter(new Locator.FilterOptions().setHasText(productName))
                .getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Add to cart"))
                .click();
        return this;
    }
}

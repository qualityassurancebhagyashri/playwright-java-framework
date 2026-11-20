package dev.qa.config;

/** Central place for run settings. Override with -DbaseUrl=... or env vars. */
public final class Config {
    private Config() {}

    public static String baseUrl() {
        return get("baseUrl", "BASE_URL", "https://www.saucedemo.com");
    }

    public static String apiBaseUrl() {
        return get("apiBaseUrl", "API_BASE_URL", "https://jsonplaceholder.typicode.com");
    }

    /** Selenium Grid hub URL, for example http://localhost:4444. Empty means run the browser locally. */
    public static String gridUrl() {
        return get("gridUrl", "GRID_URL", "");
    }

    /** Browser for the Selenium tests: chrome or firefox. */
    public static String browser() {
        return get("browser", "BROWSER", "chrome").toLowerCase();
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless", "HEADLESS", "true"));
    }

    /** Test credentials come from env vars in CI; the demo site publishes these publicly. */
    public static String username() {
        return get("appUser", "APP_USER", "standard_user");
    }

    public static String password() {
        return get("appPassword", "APP_PASSWORD", "secret_sauce");
    }

    private static String get(String prop, String env, String fallback) {
        String v = System.getProperty(prop);
        if (v == null || v.isBlank() || v.startsWith("${")) v = System.getenv(env);
        return (v == null || v.isBlank()) ? fallback : v;
    }
}

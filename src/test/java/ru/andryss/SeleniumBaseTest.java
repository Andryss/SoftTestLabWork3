package ru.andryss;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SeleniumBaseTest {

    public static final String BASE_URL = "https://fastpic.org";

    protected static List<WebDriver> drivers;

    @SneakyThrows
    protected String absolutePathOf(String resource) {
        return new File(requireNonNull(getClass().getResource(resource)).toURI()).getAbsolutePath();
    }

    @BeforeAll
    void setUpDrivers() {
        if (drivers == null) {
            drivers = new ArrayList<>();
            drivers.add(getChromeDriver());
            drivers.add(getFirefoxDriver());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                drivers.forEach(WebDriver::quit);
                drivers = null;
            }));
        }
    }

    private ChromeDriver getChromeDriver() {
        String optimize = System.getProperty("fastpic.test.optimize.ad", "yes");
        if ("no".equals(optimize)) {
            return new ChromeDriver();
        }
        ChromeOptions options = new ChromeOptions();
        options.addExtensions(new File(absolutePathOf("adblock_chrome.crx")));
        return new ChromeDriver(options);
    }

    private FirefoxDriver getFirefoxDriver() {
        String optimize = System.getProperty("fastpic.test.optimize.ad", "yes");
        if ("no".equals(optimize)) {
            return new FirefoxDriver();
        }
        FirefoxDriver driver = new FirefoxDriver();
        driver.installExtension(new File(absolutePathOf("adblock_firefox.xpi")).toPath());
        return driver;
    }

    private final Duration timeout = Duration.ofSeconds(6);
    private final Duration pollTime = Duration.ofSeconds(2);

    @SneakyThrows
    protected WebElement waitAndFind(WebDriver driver, By by) {
        Thread.sleep(pollTime.toMillis());
        return new WebDriverWait(driver, timeout, pollTime).until(visibilityOfElementLocated(by));
    }

    @SneakyThrows
    protected List<WebElement> waitAndFindMultiple(WebDriver driver, By by) {
        Thread.sleep(pollTime.toMillis());
        return new WebDriverWait(driver, timeout, pollTime).until(visibilityOfAllElementsLocatedBy(by));
    }
}

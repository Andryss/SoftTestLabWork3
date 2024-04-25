package ru.andryss;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
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

public class SeleniumBaseTest {

    public static final String BASE_URL = "https://fastpic.org";

    protected static List<WebDriver> drivers;

    @SneakyThrows
    protected static String absolutePathOf(String resource) {
        return new File(requireNonNull(SeleniumBaseTest.class.getResource(resource)).toURI()).getAbsolutePath();
    }

    @BeforeAll
    static void setUpDrivers() {
        if (drivers == null) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (drivers == null) return;
                drivers.forEach(WebDriver::quit);
                drivers = null;
            }));

            drivers = new ArrayList<>();
            drivers.add(getChromeDriver());
            drivers.add(getFirefoxDriver());
        }
    }

    private static ChromeDriver getChromeDriver() {
        String optimize = System.getProperty("fastpic.test.optimize.ad", "true").toLowerCase();
        if ("false".equals(optimize)) {
            return new ChromeDriver();
        }
        ChromeOptions options = new ChromeOptions();
        options.addExtensions(new File(absolutePathOf("adblock_chrome.crx")));
        return new ChromeDriver(options);
    }

    private static FirefoxDriver getFirefoxDriver() {
        String optimize = System.getProperty("fastpic.test.optimize.ad", "true").toLowerCase();
        if ("false".equals(optimize)) {
            return new FirefoxDriver();
        }
        FirefoxDriver driver = new FirefoxDriver();
        driver.installExtension(new File(absolutePathOf("adblock_firefox.xpi")).toPath());
        return driver;
    }

    private final Duration timeout = Duration.ofSeconds(6);

    protected WebElement find(WebDriver driver, String xpath) {
        return new WebDriverWait(driver, timeout).until(visibilityOfElementLocated(By.xpath(xpath)));
    }

    protected List<WebElement> findMultiple(WebDriver driver, String xpath) {
        return new WebDriverWait(driver, timeout).until(visibilityOfAllElementsLocatedBy(By.xpath(xpath)));
    }

    private final Duration waitTime = Duration.ofSeconds(2);

    @SneakyThrows
    protected WebElement waitAndFind(WebDriver driver, String xpath) {
        Thread.sleep(waitTime.toMillis());
        return find(driver, xpath);
    }

    @SneakyThrows
    protected List<WebElement> waitAndFindMultiple(WebDriver driver, String xpath) {
        Thread.sleep(waitTime.toMillis());
        return findMultiple(driver, xpath);
    }
}

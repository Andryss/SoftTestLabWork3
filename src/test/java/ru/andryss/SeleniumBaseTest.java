package ru.andryss;

import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class SeleniumBaseTest {

    public static final String BASE_URL = "https://fastpic.org";

    protected static List<WebDriver> drivers;

    @BeforeAll
    static void setUpDrivers() {
        if (drivers == null) {
            drivers = new ArrayList<>();
            drivers.add(new ChromeDriver());
            drivers.add(new FirefoxDriver());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                drivers.forEach(WebDriver::quit);
                drivers = null;
            }));
        }
    }

    protected WebElement waitAndFind(WebDriver driver, By by) {
        return new WebDriverWait(driver, Duration.ofSeconds(10)).until(visibilityOfElementLocated(by));
    }

    protected List<WebElement> waitAndFindMultiple(WebDriver driver, By by) {
        return new WebDriverWait(driver, Duration.ofSeconds(10)).until(visibilityOfAllElementsLocatedBy(by));
    }
}

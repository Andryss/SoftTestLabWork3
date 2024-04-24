package ru.andryss;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyUploadsPageTest extends SeleniumBaseTest {

    @Test
    public void navigateToMyUploadsPage_pageHasCorrectContent_success() {
        drivers.forEach(driver -> {
            driver.get(BASE_URL);
            WebElement myUploadsPageButton = waitAndFind(driver, By.xpath("//*[@id='headermenu']//*[@href='/my.php']/div"));

            assertEquals("Мои загрузки", myUploadsPageButton.getText());

            myUploadsPageButton.click();

            assertEquals("Мои загрузки — FastPic", driver.getTitle());

            assertDoesNotThrow(() -> waitAndFind(driver, By.xpath("//a[@href='/donate']")));

            assertDoesNotThrow(() -> waitAndFind(driver, By.xpath("//a[@href='https://new.fastpic.org/']")));

            WebElement loadAreaHeader = waitAndFind(driver, By.xpath("//table//h2"));
            assertEquals("Мои загрузки", loadAreaHeader.getText());
        });
    }
}

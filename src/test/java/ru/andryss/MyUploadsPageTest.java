package ru.andryss;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyUploadsPageTest extends SeleniumBaseTest {

    @Test
    public void navigateToMyUploadsPage_pageHasCorrectContent_success() {
        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);
            WebElement myUploadsPageButton = waitAndFind(driver, "//*[@id='headermenu']//*[@href='/my.php']/div");

            assertEquals("Мои загрузки", myUploadsPageButton.getText());

            myUploadsPageButton.click();

            assertEquals("Мои загрузки — FastPic", driver.getTitle());

            assertDoesNotThrow(() -> find(driver, "//a[@href='/donate']"));

            WebElement loadAreaHeader = find(driver, "//table//h2");
            assertEquals("Мои загрузки", loadAreaHeader.getText());
        });
    }
}

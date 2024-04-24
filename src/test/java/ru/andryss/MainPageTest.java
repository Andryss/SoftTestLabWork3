package ru.andryss;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainPageTest extends SeleniumBaseTest {

    private static final String limits = """
            Поддерживаемые форматы: gif, jpeg, png, webp, bmp
            Максимальное количество для одновременной загрузки: 30
            Максимальный размер файлов для загрузки "по ссылке": 512 KB
            Максимальный размер загружаемого изображения: 25 MB
            Максимальный размер превью: 400 px

            Если у выходного изображения размер будет больше 512 KB или произведение сторон будет больше 1.164 мегапикселя, то вставить изображение можно будет только через превью ссылки.""";

    @Test
    public void navigateToMainPage_pageHasCorrectContent_success() {
        drivers.forEach(driver -> {
            driver.get(BASE_URL);
            WebElement mainPageButton = waitAndFind(driver, By.xpath("//*[@id='headermenu']//*[@href='/']/div"));

            assertEquals("Главная", mainPageButton.getText());

            mainPageButton.click();

            assertEquals("FastPic — Загрузить изображения", driver.getTitle());

            assertDoesNotThrow(() -> waitAndFind(driver, By.xpath("//a[@href='/donate']")));

            WebElement loadAreaHeader = waitAndFind(driver, By.xpath("//div[@id='load-area']/span/h3"));
            assertEquals("Хостинг картинок и изображений", loadAreaHeader.getText());

            WebElement uploadLimitsLink = waitAndFind(driver, By.xpath("//div[@id='load-area']//a[@id='limits']"));
            uploadLimitsLink.click();

            WebElement uploadLimits = waitAndFind(driver, By.xpath("//div[@id='load-area']//p[@id='uploadLimits']"));
            assertEquals(limits, uploadLimits.getText());
        });
    }
}
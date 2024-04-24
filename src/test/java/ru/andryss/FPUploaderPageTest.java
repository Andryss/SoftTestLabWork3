package ru.andryss;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FPUploaderPageTest extends SeleniumBaseTest {

    private static final String[] features = {
            "- Загрузка изображений с вашего компьютера на хостинг",
            "- Загрузка изображений с интернета на хостинг",
            "- Снятие и загрузка скриншотов (весь экран, выделенное окно, выделенная область экрана)",
            "- Хранение истории загруженных изображений"
    };

    @Test
    public void navigateToFPUploaderPage_pageHasCorrectContent_success() {
        drivers.forEach(driver -> {
            driver.get(BASE_URL);
            WebElement fpUploadPageButton = waitAndFind(driver, By.xpath("//*[@id='headermenu']//*[@href='/fpuploader']/div"));

            assertEquals("FP Uploader", fpUploadPageButton.getText());

            fpUploadPageButton.click();

            assertEquals("FP Uploader программа загрузки изображений — FastPic", driver.getTitle());

            assertDoesNotThrow(() -> waitAndFind(driver, By.xpath("//a[@href='/donate']")));

            WebElement fpUploadHeader = waitAndFind(driver, By.xpath("//div[@id='text-box']/h2"));
            assertEquals("FP Uploader", fpUploadHeader.getText());

            WebElement fpUploadContent = waitAndFind(driver, By.xpath("//div[@id='text-box']/p"));
            assertEquals("Рады вам представить программу загрузки изображений на наш сервис. Ссылка на загрузку: FP Uploader.", fpUploadContent.getText());

            assertDoesNotThrow(() -> waitAndFind(driver, By.xpath("//a[@href='http://static.fastpic.ru/fpuploader/FPUploader.exe']")));

            WebElement featuresHeader = waitAndFind(driver, By.xpath("//div[@id='text-box']/div"));
            assertEquals("Основные возможности программы:", featuresHeader.getText());

            for (int i = 0; i < features.length; i++) {
                WebElement featureContent = waitAndFind(driver, By.xpath(String.format("//div[@id='text-box']/ul/li[%d]", i + 1)));
                assertEquals(features[i], featureContent.getText());
            }
        });
    }
}

package ru.andryss;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class UploadTest extends SeleniumBaseTest {

    private final Pattern bbCodePattern = Pattern.compile("\\[URL=.*?]\\[IMG].*?\\[/IMG]\\[/URL]");
    private final Pattern htmlPattern = Pattern.compile("<a href=\".*?\" target=\"_blank\"><img src=\".*?\" border=\"0\"></a>");
    private final Pattern markdownPattern = Pattern.compile("\\[!\\[FastPic.Ru]\\(.*?\\)]\\(.*?\\)");

    @SneakyThrows
    private String absolutePathOf(String resource) {
        return new File(getClass().getResource(resource).toURI()).getAbsolutePath();
    }

    @SneakyThrows
    private File downloadImage(String link, String format) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(link)).GET().build();

        File tmpFile = File.createTempFile("tmp", format);

        httpClient.send(request, HttpResponse.BodyHandlers.ofFile(tmpFile.toPath()));

        return tmpFile;
    }

    @SneakyThrows
    private void assertFileEquals(File expected, File real) {
        assertEquals(expected.length(), real.length());

        int bufSize = 1024 * 8;
        try (
                FileInputStream expStream = new FileInputStream(expected);
                FileInputStream realStream = new FileInputStream(real)
        ) {
            while (expStream.available() > 0) {
                assertArrayEquals(expStream.readNBytes(bufSize), realStream.readNBytes(bufSize));
            }
        }
    }

    @Test
    public void uploadImage_success() {
        String imageName = "tsopa.jpg";
        String format = imageName.substring(imageName.indexOf('.'));

        drivers.forEach(driver -> {
            driver.get(BASE_URL);
            WebElement uploadFile = waitAndFind(driver, By.xpath("//div[@id='uploading_files']//input[@type='file' and @id='file']"));
            uploadFile.sendKeys(absolutePathOf(imageName));

            WebElement uploadButton = waitAndFind(driver, By.xpath("//form[@id='upload']//input[@id='uploadButton']"));
            uploadButton.click();

            WebElement pictureHeader = waitAndFind(driver, By.xpath("//div[@id='pic-1']//h3"));
            assertEquals(imageName, pictureHeader.getText());

            String directLink = waitAndFind(driver, By.xpath("//div[@id='pic-1']//ul[@class='codes-list']//li[1]//input[@type='text']")).getAttribute("value");
            assertFileEquals(new File(absolutePathOf(imageName)), downloadImage(directLink, format));

            String bbCodePreviewLink = waitAndFind(driver, By.xpath("//div[@id='pic-1']//ul[@class='codes-list']//li[2]//input[@type='text']")).getAttribute("value");
            assertTrue(bbCodePattern.matcher(bbCodePreviewLink).matches());

            String bbCodeBigLink = waitAndFind(driver, By.xpath("//div[@id='pic-1']//ul[@class='codes-list']//li[3]//input[@type='text']")).getAttribute("value");
            assertTrue(bbCodePattern.matcher(bbCodeBigLink).matches());

            String htmlPreviewLink = waitAndFind(driver, By.xpath("//div[@id='pic-1']//ul[@class='codes-list']//li[4]//input[@type='text']")).getAttribute("value");
            assertTrue(htmlPattern.matcher(htmlPreviewLink).matches());

            String markdownPreviewLink = waitAndFind(driver, By.xpath("//div[@id='pic-1']//ul[@class='codes-list']//li[5]//input[@type='text']")).getAttribute("value");
            assertTrue(markdownPattern.matcher(markdownPreviewLink).matches());

            String imagePage = waitAndFind(driver, By.xpath("//div[@id='pic-1']//a")).getAttribute("href");
            driver.get(imagePage);

            String imageLink = waitAndFind(driver, By.xpath("//div[@id='picContainer']//a[@id='imglink']//img")).getAttribute("src");
            assertFileEquals(new File(absolutePathOf(imageName)), downloadImage(imageLink, format));
        });
    }

    @Test
    public void uploadImage_scaleImage_success() {
        String imageName = "klimenkov.jpg";
        String format = imageName.substring(imageName.indexOf('.'));
        int size = 30;

        drivers.forEach(driver -> {
            driver.get(BASE_URL);
            WebElement uploadFile = waitAndFind(driver, By.xpath("//div[@id='uploading_files']//input[@type='file' and @id='file']"));
            uploadFile.sendKeys(absolutePathOf(imageName));

            WebElement resizeCheckbox = waitAndFind(driver, By.xpath("//div[@id='settings']//input[@type='checkbox' and @id='check_orig_resize']"));
            resizeCheckbox.click();

            WebElement resizeField = waitAndFind(driver, By.xpath("//div[@id='settings']//input[@type='text' and @id='orig-resize']"));
            resizeField.clear();
            resizeField.sendKeys(String.valueOf(size));

            WebElement uploadButton = waitAndFind(driver, By.xpath("//form[@id='upload']//input[@id='uploadButton']"));
            uploadButton.click();

            String directLink = waitAndFind(driver, By.xpath("//div[@id='pic-1']//ul[@class='codes-list']//li[1]//input[@type='text']")).getAttribute("value");

            File image = downloadImage(directLink, format);
            BufferedImage bufferedImage;
            try {
                bufferedImage = ImageIO.read(image);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            assertEquals(size, bufferedImage.getHeight());
        });
    }

    @Test
    public void uploadMultiple_deletePresented_success() {
        String imageName1 = "gavrilov.jpg";
        String imageName2 = "nikolaev.jpg";

        drivers.forEach(driver -> {
            driver.get(BASE_URL);

            WebElement addFileInputButton = waitAndFind(driver, By.xpath("//div[@id='uploading_files']//a[@id='add_file_inputLink']"));
            addFileInputButton.click();

            WebElement uploadMainFile = waitAndFind(driver, By.xpath("//div[@id='uploading_files']//input[@type='file' and @id='file']"));
            uploadMainFile.sendKeys(absolutePathOf(imageName1));

            WebElement uploadAdditionalFile = waitAndFind(driver, By.xpath("//div[@id='uploading_files']//input[@type='file' and @name='file2']"));
            uploadAdditionalFile.sendKeys(absolutePathOf(imageName2));

            WebElement uploadButton = waitAndFind(driver, By.xpath("//form[@id='upload']//input[@id='uploadButton']"));
            uploadButton.click();

            WebElement picture1Header = waitAndFind(driver, By.xpath("//div[@id='pic-1']//h3"));
            assertEquals(imageName1, picture1Header.getText());

            WebElement picture2Header = waitAndFind(driver, By.xpath("//div[@id='pic-2']//h3"));
            assertEquals(imageName2, picture2Header.getText());

            WebElement uploadsPageButton = waitAndFind(driver, By.xpath("//*[@id='headermenu']//*[@href='/my.php']/div"));
            assertEquals("Мои загрузки", uploadsPageButton.getText());
            uploadsPageButton.click();

            List<WebElement> imagesSelectBoxes = waitAndFindMultiple(driver, By.xpath("//div[@id='mypics']//input"));
            assertEquals(2, imagesSelectBoxes.size());
            imagesSelectBoxes.forEach(WebElement::click);

            WebElement deleteSelectedButton = waitAndFind(driver, By.xpath("//div[@id='mypics']//a[@id='delete_checked']"));
            deleteSelectedButton.click();

            WebElement noImagesBlock = waitAndFind(driver, By.xpath("//div[@id='mypics']/div[2]"));
            assertEquals("Загруженные изображения не найдены", noImagesBlock.getText());

            assertThrows(TimeoutException.class, () -> waitAndFind(driver, By.xpath("//div[@id='mypics']//input")));
        });
    }

    @ParameterizedTest
    @CsvSource({
            "gavrilov.jpg,   0,             gavrilov.jpg",
            "gavrilov.jpg,  90,  gavrilov_rotated_90.jpg",
            "gavrilov.jpg, 180, gavrilov_rotated_180.jpg",
            "gavrilov.jpg, 270, gavrilov_rotated_270.jpg",
    })
    public void uploadImage_rotateImage_success(String src, String rotation, String dest) {
        String imageName = "opd.jpg";
        String format = imageName.substring(imageName.indexOf('.'));

        drivers.forEach(driver -> {
            driver.get(BASE_URL);
            WebElement uploadFile = waitAndFind(driver, By.xpath("//div[@id='uploading_files']//input[@type='file' and @id='file']"));
            uploadFile.sendKeys(absolutePathOf(imageName));

            WebElement rotateCheckbox = waitAndFind(driver, By.xpath("//div[@id='settings']//input[@type='checkbox' and @id='check_orig_rotate']"));
            rotateCheckbox.click();

            WebElement rotateSelect = waitAndFind(driver, By.xpath("//div[@id='settings']//select[@id='orig-rotate']"));
            new Select(rotateSelect).selectByValue("90");


            WebElement uploadButton = waitAndFind(driver, By.xpath("//form[@id='upload']//input[@id='uploadButton']"));
            uploadButton.click();

            String directLink = waitAndFind(driver, By.xpath("//div[@id='pic-1']//ul[@class='codes-list']//li[1]//input[@type='text']")).getAttribute("value");

            File image = downloadImage(directLink, format);

            assertFileEquals(new File(absolutePathOf("opd_rotated.jpg")), image);
        });
    }

    @AfterEach
    public void clearUploadedImages() {
        drivers.forEach(driver -> {
            WebElement uploadsPageButton = waitAndFind(driver, By.xpath("//*[@id='headermenu']//*[@href='/my.php']/div"));
            uploadsPageButton.click();

            List<WebElement> imagesSelectBoxes = waitAndFindMultiple(driver, By.xpath("//div[@id='mypics']//input"));
            imagesSelectBoxes.forEach(WebElement::click);

            WebElement deleteSelectedButton = waitAndFind(driver, By.xpath("//div[@id='mypics']//a[@id='delete_checked']"));
            deleteSelectedButton.click();
        });
    }

}

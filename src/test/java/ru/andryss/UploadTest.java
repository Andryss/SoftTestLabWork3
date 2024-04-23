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

import java.io.File;
import java.io.FileInputStream;
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

    @ParameterizedTest
    @CsvSource({
            "tsopa.gif",
            "tsopa.jpg",
            "tsopa.png",
            "tsopa.webp",
            // "tsopa.bmp", // NOT SUPPORTED!!!
    })
    public void uploadImage_success(String src) {
        String format = src.substring(src.indexOf('.'));

        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);
            WebElement uploadFile = waitAndFind(driver, By.xpath("//div[@id='uploading_files']//input[@type='file' and @id='file']"));
            uploadFile.sendKeys(absolutePathOf(src));

            WebElement clearOptionsLink = waitAndFind(driver, By.xpath("//div[@id='load-area']//a[@id='offeffectsLink']"));
            clearOptionsLink.click();

            WebElement uploadButton = waitAndFind(driver, By.xpath("//form[@id='upload']//input[@id='uploadButton']"));
            uploadButton.click();

            WebElement pictureHeader = waitAndFind(driver, By.xpath("//div[@id='pic-1']//h3"));
            assertEquals(src, pictureHeader.getText());

            String directLink = waitAndFind(driver, By.xpath("//div[@id='pic-1']//ul[@class='codes-list']//li[1]//input[@type='text']")).getAttribute("value");
            assertFileEquals(new File(absolutePathOf(src)), downloadImage(directLink, format));

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
            assertFileEquals(new File(absolutePathOf(src)), downloadImage(imageLink, format));
        });
    }

    @Test
    public void uploadMultiple_deletePresented_success() {
        String[] imageNames = { "gavrilov.jpg", "klimenkov.jpg", "nikolaev.jpg", "tsopa.jpg" };

        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);

            WebElement addFileInputButton = waitAndFind(driver, By.xpath("//div[@id='uploading_files']//a[@id='add_file_inputLink']"));
            for (int i = 1; i < imageNames.length; i++) {
                addFileInputButton.click();
            }

            for (int i = 0; i < imageNames.length; i++) {
                String inputName = (i == 0 ? "file[]" : "file" + (i + 1));
                WebElement uploadFile = waitAndFind(driver, By.xpath(String.format("//div[@id='uploading_files']//input[@type='file' and @name='%s']", inputName)));
                uploadFile.sendKeys(absolutePathOf(imageNames[i]));
            }

            WebElement clearOptionsLink = waitAndFind(driver, By.xpath("//div[@id='load-area']//a[@id='offeffectsLink']"));
            clearOptionsLink.click();

            WebElement uploadButton = waitAndFind(driver, By.xpath("//form[@id='upload']//input[@id='uploadButton']"));
            uploadButton.click();

            for (int i = 0; i < imageNames.length; i++) {
                String divPicId = "pic-" + (i + 1);
                WebElement pictureHeader = waitAndFind(driver, By.xpath(String.format("//div[@id='%s']//h3", divPicId)));
                assertEquals(imageNames[i], pictureHeader.getText());
            }

            WebElement uploadsPageButton = waitAndFind(driver, By.xpath("//*[@id='headermenu']//*[@href='/my.php']/div"));
            assertEquals("Мои загрузки", uploadsPageButton.getText());
            uploadsPageButton.click();

            List<WebElement> imagesSelectBoxes = waitAndFindMultiple(driver, By.xpath("//div[@id='mypics']//input"));
            assertEquals(imageNames.length, imagesSelectBoxes.size());
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
            "klimenkov.jpg, 439,            klimenkov.jpg",
            "klimenkov.jpg, 400, klimenkov_scaled_400.jpg",
            "klimenkov.jpg, 200, klimenkov_scaled_200.jpg",
            "klimenkov.jpg, 120, klimenkov_scaled_120.jpg",
            "klimenkov.jpg,  50,  klimenkov_scaled_50.jpg", // micro klimenkov
            "klimenkov.jpg,  10,  klimenkov_scaled_10.jpg", // nano klimenkov
    })
    public void uploadImage_scaleImage_success(String src, String scale, String dest) {
        String format = src.substring(src.indexOf('.'));

        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);
            WebElement uploadFile = waitAndFind(driver, By.xpath("//div[@id='uploading_files']//input[@type='file' and @id='file']"));
            uploadFile.sendKeys(absolutePathOf(src));

            WebElement clearOptionsLink = waitAndFind(driver, By.xpath("//div[@id='load-area']//a[@id='offeffectsLink']"));
            clearOptionsLink.click();

            WebElement resizeCheckbox = waitAndFind(driver, By.xpath("//div[@id='settings']//input[@type='checkbox' and @id='check_orig_resize']"));
            resizeCheckbox.click();

            WebElement resizeField = waitAndFind(driver, By.xpath("//div[@id='settings']//input[@type='text' and @id='orig-resize']"));
            resizeField.clear();
            resizeField.sendKeys(scale);

            WebElement uploadButton = waitAndFind(driver, By.xpath("//form[@id='upload']//input[@id='uploadButton']"));
            uploadButton.click();

            String directLink = waitAndFind(driver, By.xpath("//div[@id='pic-1']//ul[@class='codes-list']//li[1]//input[@type='text']")).getAttribute("value");

            File image = downloadImage(directLink, format);

            assertFileEquals(new File(absolutePathOf(dest)), image);
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
        String format = src.substring(src.indexOf('.'));

        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);
            WebElement uploadFile = waitAndFind(driver, By.xpath("//div[@id='uploading_files']//input[@type='file' and @id='file']"));
            uploadFile.sendKeys(absolutePathOf(src));

            WebElement clearOptionsLink = waitAndFind(driver, By.xpath("//div[@id='load-area']//a[@id='offeffectsLink']"));
            clearOptionsLink.click();

            WebElement rotateCheckbox = waitAndFind(driver, By.xpath("//div[@id='settings']//input[@type='checkbox' and @id='check_orig_rotate']"));
            rotateCheckbox.click();

            WebElement rotateSelect = waitAndFind(driver, By.xpath("//div[@id='settings']//select[@id='orig-rotate']"));
            new Select(rotateSelect).selectByValue(rotation);

            WebElement uploadButton = waitAndFind(driver, By.xpath("//form[@id='upload']//input[@id='uploadButton']"));
            uploadButton.click();

            String directLink = waitAndFind(driver, By.xpath("//div[@id='pic-1']//ul[@class='codes-list']//li[1]//input[@type='text']")).getAttribute("value");

            File image = downloadImage(directLink, format);

            assertFileEquals(new File(absolutePathOf(dest)), image);
        });
    }

    @ParameterizedTest
    @CsvSource({
            "nikolaev.jpg, 25, nikolaev_jepg_25.jpg",
            "nikolaev.jpg, 50, nikolaev_jepg_50.jpg",
            "nikolaev.jpg, 75, nikolaev_jepg_75.jpg",
    })
    public void uploadImage_optimizeImage_success(String src, String optimization, String dest) {
        String format = src.substring(src.indexOf('.'));

        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);
            WebElement uploadFile = waitAndFind(driver, By.xpath("//div[@id='uploading_files']//input[@type='file' and @id='file']"));
            uploadFile.sendKeys(absolutePathOf(src));

            WebElement clearOptionsLink = waitAndFind(driver, By.xpath("//div[@id='load-area']//a[@id='offeffectsLink']"));
            clearOptionsLink.click();

            WebElement optimizeCheckbox = waitAndFind(driver, By.xpath("//div[@id='settings']//input[@type='checkbox' and @id='optimization']"));
            optimizeCheckbox.click();

            WebElement optimizeField = waitAndFind(driver, By.xpath("//div[@id='settings']//input[@type='text' and @id='jpeg-quality']"));
            optimizeField.clear();
            optimizeField.sendKeys(optimization);

            WebElement uploadButton = waitAndFind(driver, By.xpath("//form[@id='upload']//input[@id='uploadButton']"));
            uploadButton.click();

            String directLink = waitAndFind(driver, By.xpath("//div[@id='pic-1']//ul[@class='codes-list']//li[1]//input[@type='text']")).getAttribute("value");

            File image = downloadImage(directLink, format);

            assertFileEquals(new File(absolutePathOf(dest)), image);
        });
    }

    @AfterEach
    public void clearUploadedImages() {
        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);

            WebElement uploadsPageButton = waitAndFind(driver, By.xpath("//*[@id='headermenu']//*[@href='/my.php']/div"));
            uploadsPageButton.click();

            try {
                List<WebElement> imagesSelectBoxes = waitAndFindMultiple(driver, By.xpath("//div[@id='mypics']//input"));
                imagesSelectBoxes.forEach(WebElement::click);

                WebElement deleteSelectedButton = waitAndFind(driver, By.xpath("//div[@id='mypics']//a[@id='delete_checked']"));
                deleteSelectedButton.click();
            } catch (TimeoutException ignore) { }
        });
    }

}

package ru.andryss;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
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

    private final String linkXpathFormat = "//div[@id='pic-1']//ul[@class='codes-list']//li[%d]//input[@type='text']";

    @ParameterizedTest
    @CsvSource({
            "tsopa.gif",
            "tsopa.jpg",
            "tsopa.png",
            "tsopa.webp",
            "tsopa.bmp", // :(
    })
    public void uploadImage_success(String src) {
        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);
            selectImageToUpload(driver, src);

            clickClearUploadOptions(driver);

            clickUploadButton(driver);

            WebElement pictureHeader = find(driver, "//div[@id='pic-1']//h3");
            assertEquals(src, pictureHeader.getText());

            String directLink = extractUploadedImageDirectLink(driver);
            assertFileEquals(new File(absolutePathOf(src)), downloadImage(directLink));

            String bbCodePreviewLink = find(driver, String.format(linkXpathFormat, 2)).getAttribute("value");
            assertTrue(bbCodePattern.matcher(bbCodePreviewLink).matches());

            String bbCodeBigLink = find(driver, String.format(linkXpathFormat, 3)).getAttribute("value");
            assertTrue(bbCodePattern.matcher(bbCodeBigLink).matches());

            String htmlPreviewLink = find(driver, String.format(linkXpathFormat, 4)).getAttribute("value");
            assertTrue(htmlPattern.matcher(htmlPreviewLink).matches());

            String markdownPreviewLink = find(driver, String.format(linkXpathFormat, 5)).getAttribute("value");
            assertTrue(markdownPattern.matcher(markdownPreviewLink).matches());

            String imagePage = find(driver, "//div[@id='pic-1']//a").getAttribute("href");

            clickMyUploadsPage(driver);

            String imagePageFromUploads = find(driver, "//div[@id='mypics']//div[@class='thumb']//a").getAttribute("href");
            assertEquals(imagePage, imagePageFromUploads);

            driver.get(imagePage);

            String imageLink = find(driver, "//div[@id='picContainer']//a[@id='imglink']//img").getAttribute("src");
            assertFileEquals(new File(absolutePathOf(src)), downloadImage(imageLink));
        });
    }

    @Test
    public void uploadMultiple_deletePresented_success() {
        String[] imageNames = { "gavrilov.jpg", "klimenkov.jpg", "nikolaev.jpg", "tsopa.jpg" };

        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);

            WebElement addFileInputButton = waitAndFind(driver, "//div[@id='uploading_files']//a[@id='add_file_inputLink']");
            for (int i = 1; i < imageNames.length; i++) {
                addFileInputButton.click();
            }

            for (int i = 0; i < imageNames.length; i++) {
                String inputName = (i == 0 ? "file[]" : "file" + (i + 1));
                WebElement uploadFile = waitAndFind(driver, String.format("//div[@id='uploading_files']//input[@type='file' and @name='%s']", inputName));
                uploadFile.sendKeys(absolutePathOf(imageNames[i]));
            }

            clickClearUploadOptions(driver);

            clickUploadButton(driver);

            for (int i = 0; i < imageNames.length; i++) {
                String divPicId = "pic-" + (i + 1);
                WebElement pictureHeader = find(driver, String.format("//div[@id='%s']//h3", divPicId));
                assertEquals(imageNames[i], pictureHeader.getText());
            }

            clickMyUploadsPage(driver);

            List<WebElement> imagesSelectBoxes = waitAndFindMultiple(driver, "//div[@id='mypics']//input");
            assertEquals(imageNames.length, imagesSelectBoxes.size());
            imagesSelectBoxes.forEach(WebElement::click);

            clickDeleteSelectedImages(driver);

            WebElement noImagesBlock = find(driver, "//div[@id='mypics']/div[2]");
            assertEquals("Загруженные изображения не найдены", noImagesBlock.getText());

            assertThrows(TimeoutException.class, () -> find(driver, "//div[@id='mypics']//input"));
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
        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);
            selectImageToUpload(driver, src);

            clickClearUploadOptions(driver);

            WebElement resizeCheckbox = waitAndFind(driver, "//div[@id='settings']//input[@type='checkbox' and @id='check_orig_resize']");
            resizeCheckbox.click();

            WebElement resizeField = waitAndFind(driver, "//div[@id='settings']//input[@type='text' and @id='orig-resize']");
            resizeField.clear();
            resizeField.sendKeys(scale);

            clickUploadButton(driver);

            String directLink = extractUploadedImageDirectLink(driver);

            assertFileEquals(new File(absolutePathOf(dest)), downloadImage(directLink));
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
        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);
            selectImageToUpload(driver, src);

            clickClearUploadOptions(driver);

            WebElement rotateCheckbox = waitAndFind(driver, "//div[@id='settings']//input[@type='checkbox' and @id='check_orig_rotate']");
            rotateCheckbox.click();

            WebElement rotateSelect = waitAndFind(driver, "//div[@id='settings']//select[@id='orig-rotate']");
            new Select(rotateSelect).selectByValue(rotation);

            clickUploadButton(driver);

            String directLink = extractUploadedImageDirectLink(driver);

            assertFileEquals(new File(absolutePathOf(dest)), downloadImage(directLink));
        });
    }

    @ParameterizedTest
    @CsvSource({
            "nikolaev.jpg, 25, nikolaev_jepg_25.jpg",
            "nikolaev.jpg, 50, nikolaev_jepg_50.jpg",
            "nikolaev.jpg, 75, nikolaev_jepg_75.jpg",
    })
    public void uploadImage_optimizeImage_success(String src, String optimization, String dest) {
        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);
            selectImageToUpload(driver, src);

            clickClearUploadOptions(driver);

            WebElement optimizeCheckbox = waitAndFind(driver, "//div[@id='settings']//input[@type='checkbox' and @id='optimization']");
            optimizeCheckbox.click();

            WebElement optimizeField = waitAndFind(driver, "//div[@id='settings']//input[@type='text' and @id='jpeg-quality']");
            optimizeField.clear();
            optimizeField.sendKeys(optimization);

            clickUploadButton(driver);

            String directLink = extractUploadedImageDirectLink(driver);

            assertFileEquals(new File(absolutePathOf(dest)), downloadImage(directLink));
        });
    }

    @AfterEach
    public void clearUploadedImages() {
        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);

            clickMyUploadsPage(driver);

            try {
                List<WebElement> imagesSelectBoxes = waitAndFindMultiple(driver, "//div[@id='mypics']//input");
                imagesSelectBoxes.forEach(WebElement::click);

                clickDeleteSelectedImages(driver);
            } catch (TimeoutException ignore) { }
        });
    }

    @SneakyThrows
    private File downloadImage(String link) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(link)).GET().build();

        File tmpFile = File.createTempFile("tmp", "image");

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

    private void selectImageToUpload(WebDriver driver, String src) {
        WebElement uploadFile = waitAndFind(driver, "//div[@id='uploading_files']//input[@type='file' and @id='file']");
        uploadFile.sendKeys(absolutePathOf(src));
    }

    private void clickUploadButton(WebDriver driver) {
        WebElement uploadButton = waitAndFind(driver, "//form[@id='upload']//input[@id='uploadButton']");
        uploadButton.click();
    }

    private void clickClearUploadOptions(WebDriver driver) {
        WebElement clearOptionsLink = waitAndFind(driver, "//div[@id='load-area']//a[@id='offeffectsLink']");
        clearOptionsLink.click();
    }

    private String extractUploadedImageDirectLink(WebDriver driver) {
        return find(driver, "//div[@id='pic-1']//ul[@class='codes-list']//li[1]//input[@type='text']").getAttribute("value");
    }

    private void clickMyUploadsPage(WebDriver driver) {
        WebElement uploadsPageButton = waitAndFind(driver, "//*[@id='headermenu']//*[@href='/my.php']/div");
        uploadsPageButton.click();
    }

    private void clickDeleteSelectedImages(WebDriver driver) {
        WebElement deleteSelectedButton = waitAndFind(driver, "//div[@id='mypics']//a[@id='delete_checked']");
        deleteSelectedButton.click();
    }
}

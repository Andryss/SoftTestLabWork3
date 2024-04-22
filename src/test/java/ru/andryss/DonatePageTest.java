package ru.andryss;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

public class DonatePageTest extends SeleniumBaseTest {

    private static final String donateText = """
            На данный момент мы находимся в тяжелой финансовой ситуации.

            Как вы знаете, мы живем исключительно за счёт показа рекламы.
            150 миллионов ваших картинок на нашем сервисе — это много много терабайт информации, это большой трафик и значительное потребление электричества.

            К сожалению, ситуация последних нескольких месяцев обострила уже существующие проблемы.
            Одновременно с подорожанием электричества мы столкнулись с уходом рекламодателей, нежеланием работать с российской аудиторией.
            За 13 лет работы были самые разные сложные ситуации, из которых удавалось найти выход.
            Сейчас нам необходимо пережить эти тяжелые для нас всех времена.
            Поэтому если у вас есть возможность — поддержите нас криптовалютой, это позволит сохранить ваши картинки, скриншоты и фотографии.

            Спасибо вам!

            BTC: bc1qfs3593j0vkynksh4a007nny2atz5h285ershjy

            ETH: 0xA79b4006a28596a732d24b7F272bc5FDd478a721

            USDT (TRC-20) / TRX: TMiStxK2cPJU2kbLgtjVgW1zk1xKancxqM

            USDT (ERC-20): 0xA79b4006a28596a732d24b7F272bc5FDd478a721

            BNB: bnb122t5qd98wuy943w6r96cmlg4mmkxkmf64zv39m

            DOGE: DM5JTSRjqfdWt4zcpfqvXeBhcuoz49vEd2

            LTC: LUQuZE7AS5UoKASYdF6tjTf4qHCvwT6Q2S""";

    @Test
    public void navigateToDonatePage_pageHasCorrectContent_success() {
        drivers.forEach(driver -> {
            driver.get(BASE_URL);
            WebElement donatePageButton = waitAndFind(driver, By.xpath("//*[@id='headermenu']//*[@href='/donate']/div"));

            assertEquals("Donate", donatePageButton.getText());

            donatePageButton.click();

            assertEquals("Donate — FastPic", driver.getTitle());

            assertDoesNotThrow(() -> waitAndFind(driver, By.xpath("//a[@href='/donate']")));

            WebElement donateHeader = waitAndFind(driver, By.xpath("//div[@id='text-box']/h2"));
            assertEquals("Друзья!", donateHeader.getText());

            WebElement donateContent = waitAndFind(driver, By.xpath("//div[@id='text-box']/p"));
            assertEquals(donateText, donateContent.getText());
        });
    }
}

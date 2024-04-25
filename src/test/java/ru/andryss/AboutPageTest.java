package ru.andryss;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AboutPageTest extends SeleniumBaseTest {

    private static final String[] about = {
            "Сервис FastPic.ru — узкоспециализированный проект. Он создан для размещения на нём скриншотов, постеров и других изображений, которые могут понадобиться при создании тем и сообщений на форумах.",
            """
            Исходя из поставленных целей, он несет в себе несколько особенностей.
            
            Во-первых, сервис должен быть быстрым, что собственно, и отображено в названии. Чтобы быть быстрым в той степени, в которой хотелось бы, - рождаются несколько ограничений. Вы их можете посмотреть при загрузке изображений, нажав на "посмотреть лимиты". Хотелось бы отметить так называемый hotlink limit. Он заключается в том, что изображения, у которых превышают лимиты на размер файла или размер разрешения, не могут быть размещены без превью изображений. Это сделано для того, чтобы ограничить нагрузку на наш сервис и разгрузить страницы ваших тем от больших изображений.
            
            Во-вторых, изображения должны быть оптимизированы по размеру. Пожалуйста, выставляйте галочку "Оптимизировать в JPEG" как можно чаще.
            
            В-третьих, что касается регистрации юзеров. Регистрации на нашем сервисе не будет, т.к. хостинг создан для форумов, где найти свои изображения в своих постах очень просто.
            
            Также хочется отметить, что просмотр и удаление изображения в "Мои изображения" ограничивается неделей со дня загрузки изображения.""",
    };

    @Test
    public void navigateToAboutPage_pageHasCorrectContent_success() {
        drivers.parallelStream().forEach(driver -> {
            driver.get(BASE_URL);
            WebElement aboutPageButton = waitAndFind(driver, "//*[@id='headermenu']//*[@href='/about']/div");

            assertEquals("О сервисе", aboutPageButton.getText());

            aboutPageButton.click();

            assertEquals("О сервисе — FastPic", driver.getTitle());

            assertDoesNotThrow(() -> find(driver, "//a[@href='/donate']"));

            WebElement aboutHeader = find(driver, "//div[@id='text-box']/h2");
            assertEquals("О сервисе", aboutHeader.getText());

            for (int i = 0; i < about.length; i++) {
                WebElement aboutContent = find(driver, String.format("//div[@id='text-box']/p[%d]", i + 1));
                assertEquals(about[i], aboutContent.getText());
            }
        });
    }
}

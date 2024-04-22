package ru.andryss;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RulePageTest extends SeleniumBaseTest {

    private static final String[] rules = {
            "1. Сервис предназначен для хранения и оперативной публикации графических файлов (скриншотов, постеров) в форумах.",
            "2. Использование данного сервиса означает согласие со всеми изложенными здесь правилами, а также добровольное согласие пользователя принять на себя обязательства по их соблюдению.",
            """
            3. Ответственность.
            3.1. Публикация изображений производится пользователем по собственной инициативе, сервис не несет ответственности за правильность и достоверность сообщенной пользователем информации.
            3.2. Сервис не несет ответственности за соблюдение авторского права на размещаемые изображения, однако предупреждает пользователей о необходимости учитывать и соблюдать его нормы.""",
            "4. Запрещается размещение материалов, нарушающих законодательство Российской Федерации.",
            "5. Администрация сервиса оставляет за собой право без уведомления удалять любые изображения, нарушающие Правила.",
            "6. Пользователям, неоднократно нарушившим данные Правила, будет заблокирован доступ к сервису.",
            "7. Администрация ресурса оставляет за собой право в любой момент в одностороннем порядке внести изменения в настоящие Правила.",
            """
            8. Ограничения по публикации изображений.
            8.1. Публикация изображений без превью разрешается только на форумах.
            8.2. Существуют дополнительные ограничения на размер и разрешения по публикации без превью, которые можно посмотреть при загрузке изображения."""
    };

    @Test
    public void navigateToRulesPage_pageHasCorrectContent_success() {
        drivers.forEach(driver -> {
            driver.get(BASE_URL);
            WebElement rulesPageButton = waitAndFind(driver, By.xpath("//*[@id='headermenu']//*[@href='/rules']/div"));

            assertEquals("Правила", rulesPageButton.getText());

            rulesPageButton.click();

            assertEquals("Правила — FastPic", driver.getTitle());

            assertDoesNotThrow(() -> waitAndFind(driver, By.xpath("//a[@href='/donate']")));

            WebElement rulesHeader = waitAndFind(driver, By.xpath("//div[@id='text-box']/h2"));
            assertEquals("Правила использования сервиса", rulesHeader.getText());

            for (int i = 0; i < rules.length; i++) {
                WebElement rulesContent = waitAndFind(driver, By.xpath(String.format("//div[@id='text-box']/p[%d]", i + 1)));
                assertEquals(rules[i], rulesContent.getText());
            }
        });
    }
}

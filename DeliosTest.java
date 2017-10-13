package selenide;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.*;
import junit.framework.AssertionFailedError;

import org.junit.*;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.*;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.junit.ScreenShooter;

public class DeliosTest {
	
	private String lang = null;
	private static String TEST_URL = "http://******/****";
	
	@Before
	public void setUp(){
		//Настройка драйвера Internet Explorer
		System.setProperty("selenide.browser", "ie");
		System.setProperty("webdriver.ie.driver", "lib/IEDriverServer.exe");
		lang = executeJavaScript("var lang = window.navigator.userLanguage; return lang;");
		//Проверка русской локали для браузера.
		if ( !lang.equals("ru-RU")){
			throw new AssertionFailedError(
					"Установите приоритет языка «Русский (Россия) [ru-RU]» в настойках IE: "
					+ "Свойства браузера --> Общие --> Языки");
		}
			
		//Установка времени ожидания на ответ, по стандарту стоит 4000 (4 секунды).
		Configuration.timeout = 10000;
		
		loginTest();
	}
	
	@Rule
	public ScreenShooter makeScreenshot = ScreenShooter.failedTests();

	
	private void loginTest() {		
		open(TEST_URL);
		$("#username").val("gorun_pv");
		$("#password").val("gorun_pv").pressEnter();
		$(byText("Список доступных бизнес-процессов и ролей системы")).shouldBe(visible);		
	} 
	
	@Test
	public void create_rerquest(){		
		// Выбираем вторую по названию вкладку
		getRoleElement("Заявки на иностранные переводы (Заявитель)", 2).click();;
		
		//Кнопка «Действия» должна быть видима 
		$(".ui-button-text").shouldBe(visible);
		//Кликаем по первой кнопке
		$(".ui-button-text").click();
	
		//Открывшееся меню должно быть видимо
		$(".submenu").shouldBe(visible);
		$(".submenu").find(byText("Создать заявку")).click();
		
		//Модальная форма должна быть видима
		getWicketModalElement("Заявка на перевод").shouldBe(visible);
		
		getWicketModalElement("Заявка на перевод")
			.find(byText("Поле 'Тема' обязательно для ввода.")).shouldBe(disappears);
		getWicketModalElement("Заявка на перевод")
			.find(byText("Поле 'Обозначение нормативного документа "
				+ "(для общих заявок - наим. файла)' "
				+ "обязательно для ввода.")).shouldBe(disappears);
		
		//Ищем на форме кнопку по значению и кликаем по ней
		getWicketModalElement("Заявка на перевод")
			.find(byValue("Сохранить")).click();
		
		//сообщения для требуюмых заполнения элементов
		getWicketModalElement("Заявка на перевод")
			.find(byText("Поле 'Тема' обязательно для ввода.")).shouldBe(visible);
		getWicketModalElement("Заявка на перевод")
			.find(byText("Поле 'Обозначение нормативного документа "
					+ "(для общих заявок - наим. файла)' "
					+ "обязательно для ввода.")).shouldBe(visible);
				
		//Выбираем второе действие на форме «Заявка на перевод» для заполнения 
		actionClick("Заявка на перевод", "img", "Выбрать", 2 );
		
		getWicketModalElement("Выбор значения справочника").shouldBe(visible);	
		
		//Выбираем значение из справочника
		getWicketModalElement("Выбор значения справочника")
			.find(byText("Общая")).click();
		
		getWicketModalElement("Выбор значения справочника")
			.find(byValue("Выбрать")).click();
		
	
		//Выбираем третье действие на форме «Заявка на перевод» для заполнения 
		actionClick("Заявка на перевод", "img", "Выбрать", 3 );
				
		assertTrue(getWicketModalElement("Выбор значения справочника")
						.shouldBe(visible)
						.isDisplayed());
		
		
		//Выбираем значение из справочника
		getWicketModalElement("Выбор значения справочника")
			.find(byText("Устный")).click();
		
		getWicketModalElement("Выбор значения справочника")
			.find(byValue("Выбрать")).click();
		
		
		//Выбираем четвертое действие на форме «Заявка на перевод» для заполнения 
		actionClick("Заявка на перевод", "img", "Выбрать", 4 );
				
		assertTrue(getWicketModalElement("Выбор значения справочника")
						.shouldBe(visible)
						.isDisplayed());
		//Выбираем значение из справочника
		getWicketModalElement("Выбор значения справочника")
			.find(byText("Совещание")).click();
		
		getWicketModalElement("Выбор значения справочника")
			.find(byValue("Выбрать")).click();
		
	
		//Заполнение полt textarea (первое)
		getWicketModalElement("Заявка на перевод")
			.find("textarea").setValue("louboutin.doc");
			
		//Загрузка файла
		File uploadFile =  new File( System.getProperty("user.dir") + "\\files\\Doc1.doc" );
		getWicketModalElement("Заявка на перевод")
			.find(byAttribute( "title", "Выбрать файл с диска" ))
			.uploadFile( uploadFile );
		
		getWicketModalElement("Заявка на перевод")
			.find(byValue( "Сохранить" )).click();	
		
		checkCreatedBO();
		
		deleteFirstElement();
	}

	/**
	 * 
	 */
	private void actionClick(String modalElementName, String elementName, String titleValue, int numberAction) {
		getWicketModalElement(modalElementName)
			.$$(elementName)
			.filter(attribute("title",titleValue)).get(numberAction-1).click();
	}
	
	
	private void checkCreatedBO() {		
		
		// Выбираем с главной формы содержимое первой строки таблицы и проверяем наличие в тексте указанных данных 
		$$("table .objectList tbody tr")
				.filter(or("evenOdd",hasClass("even"),hasClass("odd")))
				.first()
				.shouldHave( text("Общая"), text("Устный"), text("louboutin.doc") );			
	}
	
	
	private void deleteFirstElement() {
		//Выбираем первый элемент
		$$("table .objectList tbody tr")
		.filter(or("evenOdd",hasClass("even"),hasClass("odd")))
		.first().click();	

		//Кнопка «Действия» должна быть видима 
		$(".ui-button-text").shouldBe(visible);
		//Кликаем по первой кнопке
		$(".ui-button-text").click();
	
		//Открывшееся меню должно быть видимо
		$(".submenu").shouldBe(visible);
		$(".submenu").find(byText("Удалить заявку")).click();
		
		getWicketModalElement("Внимание!").shouldBe(visible);
		getWicketModalElement("Внимание!")
			.find(byValue("Да")).click();
		
		$$("table .objectList tbody tr")
		.filter(or("evenOdd",hasClass("even"),hasClass("odd")))
		.first()
		.shouldNotHave(text("louboutin.doc") );
	}
	
	
	@After
	public void after() throws IOException {
		close();		
	}

	/**
	 * @return Selenide Element {@link SelenideElement}
	 */
	private SelenideElement getRoleElement(String text, int orderNumber ) {
		return $$(".homePage span").filterBy(exactText(text)).get(orderNumber - 1);		
	}

	/**
	 * @return Selenide Element {@link SelenideElement}
	 */
	private SelenideElement getWicketModalElement(String name) {
		return $(byAttribute("aria-labelledby", name));
	}
}

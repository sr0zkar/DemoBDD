package steps;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import utils.ConfigManager;
import utils.MockServerManager;
import java.time.Duration;

public class Hooks {
    private static WebDriver driver;
    private static MockServerManager mockServer;
    private static final int MOCK_PORT = 8080;

    public static WebDriver getDriver() {
        return driver;
    }

    public static MockServerManager getMockServer() {
        return mockServer;
    }

    /**
     * Inicia el servidor mock antes de todos los tests.
     */
    @BeforeAll
    public static void startMockServer() {
        mockServer = MockServerManager.getInstance();
        mockServer.start(MOCK_PORT);

        // Actualizar la configuración dinámicamente con la URL del mock
        System.setProperty("base.url", mockServer.getBaseUrl());
    }

    /**
     * Detiene el servidor mock después de todos los tests.
     */
    @AfterAll
    public static void stopMockServer() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }

    @Before("@ui")
    public void setUpBrowser() {
        // Configurar stub de UI
        mockServer.setupUiStub();

        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        if (ConfigManager.getBoolean("headless", true))
            opts.addArguments("--headless=new");
        opts.addArguments("--no-sandbox", "--disable-dev-shm-usage",
                "--window-size=1920,1080");
        driver = new ChromeDriver(opts);
        driver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(ConfigManager.getInt("implicit.wait", 10)));
    }

    /**
     * Limpia los datos del servidor mock antes de cada escenario de API.
     */
    @Before("@api")
    public void resetMockData() {
        if (mockServer != null) {
            mockServer.reset();
        }
    }

    @After("@ui")
    public void tearDown(Scenario scenario) {
        if (driver != null) {
            if (scenario.isFailed()) {
                byte[] ss = ((TakesScreenshot) driver)
                        .getScreenshotAs(OutputType.BYTES);
                scenario.attach(ss, "image/png", "screenshot-falla");
            }
            driver.quit();
            driver = null;
        }
    }
}

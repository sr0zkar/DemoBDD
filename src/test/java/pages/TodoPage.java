package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigManager;
import java.time.Duration;
import java.util.List;

public class TodoPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(id = "todo-title-input") private WebElement titleInput;
    @FindBy(id = "add-todo-btn") private WebElement addButton;
    @FindBy(css = ".todo-item") private List<WebElement> todoItems;
    @FindBy(css = ".error-message") private WebElement errorMessage;

    public TodoPage(WebDriver driver) {
        this.driver = driver;
        int w = ConfigManager.getInt("explicit.wait", 15);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(w));
        PageFactory.initElements(driver, this);
    }

    public void navigateTo() {
        driver.get(ConfigManager.get("base.url", "http://localhost:8080"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("todo-title-input")));
    }

    public void enterTitle(String title) {
        titleInput.clear();
        titleInput.sendKeys(title);
    }

    public void clickAdd() {
        addButton.click();
    }

    public boolean isTodoVisible(String title) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[contains(@class,'todo-item')]//span[text()='"
                            + title + "']")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void completeTodo(String title) {
        WebElement cb = driver.findElement(By.xpath(
                "//div[contains(@class,'todo-item')]//span[text()='" + title
                        + "']/preceding-sibling::input[@type='checkbox']"));
        if (!cb.isSelected()) cb.click();
    }

    public boolean isTodoCompleted(String title) {
        try {
            WebElement item = driver.findElement(By.xpath(
                    "//div[contains(@class,'todo-item') and .//span[text()='"
                            + title + "']]"));
            return item.getAttribute("class").contains("completed");
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void deleteTodo(String title) {
        driver.findElement(By.xpath(
                "//div[contains(@class,'todo-item') and .//span[text()='"
                        + title + "']]//button[contains(@class,'delete-btn')]")).click();
    }

    public String getErrorMessage() {
        wait.until(ExpectedConditions.visibilityOf(errorMessage));
        return errorMessage.getText();
    }
}

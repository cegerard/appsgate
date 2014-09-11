package appsgate.components.eudtest;

import appsgate.components.eudtest.util.EUDTestHelper;
import com.thoughtworks.selenium.SeleneseTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.grid.selenium.GridLauncher;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

/**
 * IHM test class
 * @author Jander Nascimento
 */
@Ignore
public class EUDTest extends EUDTestHelper {

    @Before
    public void setUp() throws Exception {
       super.setUp();
    }

    @After
    public void tearDown(){
        super.tearDown();
    }

    @Test
    public void mainPage() throws Exception {
        driver.navigate().to(URL);
        Thread.sleep(TIMEOUT);
        Boolean condition=isPresent("//div[@id='programs']");
        assertTrue(condition);
    }

    @Test
    public void goPrograms() throws Exception {
        mainPage();
        click("//div[@id='programs']");
        Thread.sleep(TIMEOUT);
        List<WebElement> isAddButtonPresent=driver.findElements(By.xpath(PROGRAMS_ADDPROGRAMS));
        assertTrue(isAddButtonPresent.size()>0);
    }

    @Test
    public void goProgramsAddProgram() throws Exception {

        goPrograms();

        click(PROGRAMS_ADDPROGRAMS);

        Thread.sleep(TIMEOUT);

    }

    @Test
    public void checkAddProgramConfirmDisabled() throws Exception {
        goProgramsAddProgram();
        Boolean confirmDisactivated=isPresent("//button[contains(@class,'disabled')]");
        assertTrue(confirmDisactivated);
    }

    @Test
    public void checkAddProgramConfirmActivated() throws Exception{
        checkAddProgramConfirmDisabled();

        String INPUT="//input[contains(@data-i18n,'modal-add-program.name-placeholder')]";
        Thread.sleep(TIMEOUT);
        WebElement we=driver.findElement(By.xpath(INPUT));
        we.sendKeys("alpha");
        Boolean confirmActivated=isPresent("//button[contains(@data-i18n,'form.valid-button') and not(contains(@class,'disabled'))]");
        assertTrue(confirmActivated);
        Thread.sleep(TIMEOUT);
        we.sendKeys(Keys.chord(Keys.CONTROL,Keys.BACK_SPACE));
        Thread.sleep(TIMEOUT);
        Boolean confirmDisactivated=isPresent("//button[contains(@class,'disabled')]");
        assertTrue(confirmDisactivated);
        Thread.sleep(TIMEOUT);
        Boolean messageIsPresent=isPresent("//p[@data-i18n='modal-add-program.name-already-existing' and not(contains(@class,'hide'))]");
        assertTrue(messageIsPresent);

    }

}

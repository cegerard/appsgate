package appsgate.components.eudtest.util;

import com.thoughtworks.selenium.SeleneseTestBase;
import org.junit.After;
import org.junit.Before;
import org.openqa.grid.selenium.GridLauncher;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public abstract class EUDTestHelper extends SeleneseTestBase {

    protected static final String PROGRAMS_ADDPROGRAMS="//span[@data-i18n='programs-menu.add-button']";
    protected static final String URL="http://localhost:8089";
    protected static final Long TIMEOUT=1000l;
    private OSDetector os;

    protected WebDriver driver;
    protected GridLauncher gl;

    public void setUp() throws Exception {

        String variant=os.isMac()?"chromedrivermac":"chromedriverlinux";

        System.setProperty("webdriver.chrome.driver",String.format("./sources/appsgate-project/test-components/EUDTest/src/test/resources/%s",variant));
        driver=new ChromeDriver();
        gl=new org.openqa.grid.selenium.GridLauncher();
    }

    public void tearDown(){
        driver.close();
    }

    protected void click(String xpath){
        driver.findElement(By.xpath(xpath)).click();
    }

    protected Boolean isPresent(String xpath){
        return driver.findElements(By.xpath(xpath)).size()>0;
    }

}

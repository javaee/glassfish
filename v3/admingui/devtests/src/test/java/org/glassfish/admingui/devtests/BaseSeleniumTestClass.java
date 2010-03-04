package org.glassfish.admingui.devtests;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Selenium;
import junit.framework.TestCase;
import org.junit.*;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 2, 2010
 * Time: 4:47:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class BaseSeleniumTestClass {
    protected static Selenium selenium;

    @BeforeClass
    public static void setUp() throws Exception {
        if (selenium ==  null) {
            String browserString = getBrowserString();
            selenium = new DefaultSelenium("localhost", getDefaultPort(), browserString, "http://localhost:4848");
            selenium.start();
        }
    }

    private static String getBrowserString() {
        String browserString = System.getenv("browser");
        if (browserString == null) {
            browserString = System.getProperty("browser");
        }
        if (browserString == null) {
            browserString = "firefox";
        }

        return "*"+browserString;
    }

//    @AfterClass
//    public static void afterClass() {
//        selenium.stop();
//    }

    private static int getDefaultPort() {
        try {
            Class c = Class.forName("org.openqa.selenium.server.SeleniumServer");
            Method getDefaultPort = c.getMethod("getDefaultPort", new Class[0]);
            Integer portNumber = (Integer)getDefaultPort.invoke(null, new Object[0]);
            return portNumber.intValue();
        } catch (Exception e) {
            return 4444;
        }
    }

    protected void waitForAjaxLoad (String triggerText) {
        for (int second = 0; ; second++) {
            if (second >= 60) {
                Assert.fail("timeout");
            }
            try {
                if (selenium.isTextPresent(triggerText)) {
                    break;
                }
            } catch (Exception e) {
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}

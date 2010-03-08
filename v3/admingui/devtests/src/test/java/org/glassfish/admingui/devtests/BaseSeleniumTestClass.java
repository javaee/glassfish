package org.glassfish.admingui.devtests;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.SecureRandom;

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
        if (selenium == null) {
            String browserString = getBrowserString();
            selenium = new DefaultSelenium("localhost", getDefaultPort(), browserString, "http://localhost:4848");
            selenium.start();
            (new BaseSeleniumTestClass()).openAndWait(
                    "/common/sysnet/registration.jsf", "Product Registration"); // Make sure the server has started and the user logged in
        }
    }

    protected void openAndWait(String url, String triggerText) {
        selenium.open(url);
        // wait for 2 minutes, as that should be enough time to insure that the admin console app has been deployed by the server
        waitForPageLoad(triggerText, 120);
    }

    /**
     * Cause the test to wait for the page to load, timing out after 1 minute.
     *
     * @param triggerText
     * @See #waitForPageLoad(StringtriggerText,inttimeout)
     */
    protected void waitForPageLoad(String triggerText) {
        waitForPageLoad(triggerText, 60);
    }

    /**
     * Cause the test to wait for the page to load.  This will be used, for example, after an initial page load
     * (selenium.open) or after an Ajaxy navigation request has been made.
     *
     * @param triggerText The text that should appear on the page when it has finished loading
     * @param timeout     How long to wait (in seconds)
     */
    protected void waitForPageLoad(String triggerText, int timeout) {
        waitForPageLoad(triggerText, timeout, false);
    }

    protected void waitForPageLoad(String triggerText, boolean textShouldBeMissing) {
        waitForPageLoad(triggerText, 60, textShouldBeMissing);
    }

    protected void waitForPageLoad(String triggerText, int timeout, boolean textShouldBeMissing) {
        for (int second = 0; ; second++) {
            if (second >= timeout) {
                Assert.fail("timeout");
            }
            try {
                if (!textShouldBeMissing) {
                    if (selenium.isTextPresent(triggerText)) {
                        break;
                    }
                } else {
                    if (!selenium.isTextPresent(triggerText)) {
                        break;
                    }
                }
            } catch (Exception e) {
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    protected void waitForElementContentNotEqualTo(String id, String content) {
        selenium.waitForCondition("selenium.browserbot.getCurrentWindow().document.getElementById('" + id + "').innerHTML != '" + content + "'", "2500");
    }

    protected String generateRandomString() {
        SecureRandom random = new SecureRandom();

        return new BigInteger(130, random).toString(16);
    }

    /**
     * This method will scan the all ths links for the link with the given text.  We can't rely on a link's position
     * in the table, as row order may vary (if, for example, a prior test run left data behind).  If the link is not
     * found, null is returned, so the calling code may need to check the return value prior to use.
     *
     * @param baseId
     * @param value
     * @return
     */
    protected String getLinkIdByLinkText(String baseId, String value) {
        String[] links = selenium.getAllLinks();

        for (String link : links) {
            if (link.startsWith(baseId)) {
                String linkText = selenium.getText(link);
                if (value.equals(linkText)) {
                    return link;
                }
            }
        }

        return null;
    }

    protected void selectTableRowByValue(String tableId, String value) {
        selectTableRowByValue(tableId, value, "col0", "col1");
    }
    protected void selectTableRowByValue(String tableId, String value, String selectColId, String valueColId) {
        try {
            int row = 0;
            while (true) { // iterate over any rows
                // Assume one row group for now and hope it doesn't bite us
                String text = selenium.getText(tableId + ":rowGroup1:" + row + ":" + valueColId);
                if (text.equals(value)) {
                    selenium.click(tableId + ":rowGroup1:" + row + ":" + selectColId + ":select");
                    return;
                }
                row++;
            }
        } catch (Exception e) {
            Assert.fail("The specified row was not found: " + value);
        }

    }

    /**
     * Yuck
     * @param millis
     */
    protected void sleep (int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
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

        return "*" + browserString;
    }

    private static int getDefaultPort() {
        try {
            Class c = Class.forName("org.openqa.selenium.server.SeleniumServer");
            Method getDefaultPort = c.getMethod("getDefaultPort", new Class[0]);
            Integer portNumber = (Integer) getDefaultPort.invoke(null, new Object[0]);
            return portNumber.intValue();
        } catch (Exception e) {
            return 4444;
        }
    }
}
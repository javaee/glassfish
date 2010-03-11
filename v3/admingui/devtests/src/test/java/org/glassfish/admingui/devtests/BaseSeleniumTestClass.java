package org.glassfish.admingui.devtests;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class BaseSeleniumTestClass {
    protected static Selenium selenium;
    public static final String CURRENT_WINDOW = "selenium.browserbot.getCurrentWindow()";
    protected static final String MSG_NEW_VALUES_SAVED = "New values successfully saved.";

    @BeforeClass
    public static void setUp() throws Exception {
        if (selenium == null) {
            String browserString = getBrowserString();
            selenium = new DefaultSelenium("localhost", 4444, browserString, "http://localhost:4848");
            selenium.start();
            (new BaseSeleniumTestClass()).openAndWait("/common/index.jsf", "Please Register"); // Make sure the server has started and the user logged in
        }
    }

    protected String generateRandomString() {
        SecureRandom random = new SecureRandom();

        return new BigInteger(130, random).toString(16);
    }

    protected int generateRandomNumber() {
        Random r = new Random();
        return Math.abs(r.nextInt());
    }

    protected int generateRandomNumber(int max) {
        Random r = new Random();
        return Math.abs(r.nextInt(max));
    }

    protected int getTableRowCount(String id) {
        String text = selenium.getText(id);
        int count = Integer.parseInt(text.substring(text.indexOf("(") + 1, text.indexOf(")")));

        return count;
    }

    protected void openAndWait(String url, String triggerText) {
        selenium.open(url);
        // wait for 2 minutes, as that should be enough time to insure that the admin console app has been deployed by the server
        waitForPageLoad(triggerText, 120);
    }

    /**
     * Click the specified element and wait for the specified trigger text on the resulting page, timing out after 1 minute.  
     *
     * @param triggerText
     */
    protected void clickAndWait(String id, String triggerText) {
        selenium.click(id);
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

    protected void waitForButtonEnabled(String buttonId) {
        waitForCondition("document.getElementById('" + buttonId + "').disabled == false", 5000);
    }

    protected void waitForButtonDisabled(String buttonId) {
        String value = selenium.getEval(CURRENT_WINDOW + ".document.getElementById('" + buttonId + "').disabled");
        waitForCondition("document.getElementById('" + buttonId + "').disabled == true", 5000);
    }

    protected void waitForCondition(String js, int timeOutInMillis) {
        selenium.waitForCondition(CURRENT_WINDOW + "." + js, Integer.toString(timeOutInMillis));
    }

    protected void deleteRow(String buttonId, String tableId, String triggerText) {
        deleteRow(buttonId, tableId, triggerText, "col0", "col1");
    }

    protected void deleteRow(String buttonId, String tableId, String triggerText, String selectColId, String valueColId) {
        selenium.chooseOkOnNextConfirmation();
        selectTableRowByValue(tableId, triggerText, selectColId, valueColId);
        selenium.click(buttonId);
        selenium.getConfirmation();
        waitForPageLoad(triggerText, true);
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

    protected int addTableRow(String tableId, String buttonId) {
        return addTableRow(tableId, buttonId, "Additional Properties");
    }

    protected int addTableRow(String tableId, String buttonId, String countLabel) {
        int count = getTableRowCount(tableId);
        clickAndWait(buttonId, countLabel + " (" + (++count)+")");
        return count;
    }

    protected void assertTableRowCount(String tableId, int count) {
        assertEquals(count, getTableRowCount(tableId));
    }

    // Look at all those params. Maybe this isn't such a hot idea.

    /**
     * 
     * @param resourceName
     * @param tableId
     * @param enableButtonId
     * @param enabledId
     * @param backToTableButtonId
     * @param tableTriggerText
     * @param editTriggerText
     */
    protected void testEnableButton(String resourceName,
                                    String tableId,
                                    String enableButtonId,
                                    String enabledId,
                                    String backToTableButtonId,
                                    String tableTriggerText,
                                    String editTriggerText) {
        testEnableDisableButton(resourceName, tableId, enableButtonId, enabledId, backToTableButtonId, tableTriggerText, editTriggerText, "on");
    }

    protected void testDisableButton(String resourceName,
                                    String tableId,
                                    String enableButtonId,
                                    String enabledId,
                                    String backToTableButtonId,
                                    String tableTriggerText,
                                    String editTriggerText) {
        testEnableDisableButton(resourceName, tableId, enableButtonId, enabledId, backToTableButtonId, tableTriggerText, editTriggerText, "off");
    }

    private void testEnableDisableButton(String resourceName,
                                    String tableId,
                                    String enableButtonId,
                                    String enabledId,
                                    String backToTableButtonId,
                                    String tableTriggerText,
                                    String editTriggerText,
                                    String state) {
        selectTableRowByValue(tableId, resourceName);
        waitForButtonEnabled(enableButtonId);
        selenium.click(enableButtonId);
        waitForButtonDisabled(enableButtonId);

        clickAndWait(getLinkIdByLinkText(tableId, resourceName), editTriggerText);
        assertEquals(state, selenium.getValue(enabledId));
        clickAndWait(backToTableButtonId, tableTriggerText);
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
}
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.admingui.devtests;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import org.junit.*;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseSeleniumTestClass {

    public static final String CURRENT_WINDOW = "selenium.browserbot.getCurrentWindow()";
    public static final String TRIGGER_NEW_VALUES_SAVED = "New values successfully saved.";
    public static final String TRIGGER_COMMON_TASKS = "Other Tasks";
    public static final String TRIGGER_REGISTRATION_PAGE = "Receive patch information and bug updates, screencasts and tutorials, support and training offerings, and more";
    public static final String TRIGGER_ERROR_OCCURED = "An error has occurred";
    private static final String AJAX_INDICATOR = "ajaxIndicator";
    public static boolean DEBUG;

    @Rule
    public SpecificTestRule specificTestRule = new SpecificTestRule();

    protected static Selenium selenium;
    protected static WebDriver driver;
    protected static final int TIMEOUT = 90;
    protected static final int BUTTON_TIMEOUT = 750;
    private static String currentTestClass = "";
    private static int currentScreenshotNumber = 1;
    protected static boolean debug = Boolean.parseBoolean(getParameter("debug", "false"));
    private boolean processingLogin = false;
    protected Logger logger = Logger.getLogger(BaseSeleniumTestClass.class.getName());
    
    private static Map<String, String> bundles = new HashMap<String, String>() {{
        put("i18n", "org.glassfish.admingui.core.Strings"); // core
        put("i18nUC", "org.glassfish.updatecenter.admingui.Strings"); // update center
        put("i18n_corba", "org.glassfish.corba.admingui.Strings");
        put("i18n_ejb", "org.glassfish.ejb.admingui.Strings");
        put("i18n_ejbLite", "org.glassfish.ejb-lite.admingui.Strings");
        put("i18n_jts" ,"org.glassfish.jts.admingui.Strings"); // JTS
        put("i18n_web", "org.glassfish.web.admingui.Strings"); // WEB
        put("common", "org.glassfish.common.admingui.Strings");
        put("i18nc", "org.glassfish.common.admingui.Strings"); // common -- apparently we use both in the app :|
        put("i18nce", "org.glassfish.admingui.community-theme.Strings");
        put("i18ncs", "org.glassfish.cluster.admingui.Strings"); // cluster
        put("i18njca", "org.glassfish.jca.admingui.Strings"); // JCA
        put("i18njdbc", "org.glassfish.jdbc.admingui.Strings"); // JDBC
        put("i18njmail", "org.glassfish.full.admingui.Strings");
        put("i18njms", "org.glassfish.jms.admingui.Strings"); // JMS
        put("theme", "org.glassfish.admingui.community-theme.Strings");

        // These conflict with core and should probably be changed in the pages
        //put("i18n", "org.glassfish.common.admingui.Strings");
        //put("i18n", "org.glassfish.web.admingui.Strings");
        //put("i18nc", "org.glassfish.web.admingui.Strings");
    }};
    
    
    @BeforeClass
    public static void setUp() throws Exception {
        String browser = getParameter("browser", "firefox");
        String port = getParameter("admin.port", "4848");
        String baseUrl = "http://localhost:" + port;
        DEBUG = new Boolean(getParameter("debug", "false"));
        currentScreenshotNumber = 1;
        
        if ("firefox".equals(browser)) {
            driver = new FirefoxDriver();
        } else if ("chrome".equals(browser)) {
            driver = new ChromeDriver();
        } else if ("ie".contains(browser)) {
            driver = new InternetExplorerDriver();
        }

        if (selenium == null) {
            selenium = new WebDriverBackedSelenium(driver, baseUrl);
//                    DefaultSelenium("localhost", Integer.parseInt(seleniumPort), "*" + browser, baseUrl);
//            selenium.start();
            selenium.setTimeout("90000");
            (new BaseSeleniumTestClass()).openAndWait("/", TRIGGER_COMMON_TASKS, 480); // Make sure the server has started and the user logged in
        }

        if (!debug) {
            URL rotateLogUrl = new URL(baseUrl + "/management/domain/rotate-log");
            URLConnection conn = rotateLogUrl.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write("");
            wr.flush();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = rd.readLine();
            while (line  != null) {
                line = rd.readLine();
            }
            wr.close();
            rd.close();
        }
    }

    @AfterClass
    public static void captureLog() {
        try {
            selenium.stop();
            selenium = null;

            if (!currentTestClass.isEmpty() && !debug) {
                URL url = new URL("http://localhost:" + getParameter("admin.port", "4848") + "/management/domain/view-log");
    //            URLConnection urlC = url.openConnection();
                InputStream is = url.openStream();
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("target/surefire-reports/" + currentTestClass + "-server.log")));
                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                String line = in.readLine();
                while (line != null) {
                    out.write(line+System.getProperty("line.separator"));
                    line = in.readLine();
                }
                in.close();
                out.close();
            }
        } catch (FileNotFoundException fnfe) {
            //
        } catch (Exception ex) {
            Logger.getLogger(BaseSeleniumTestClass.class.getName()).log(Level.INFO, null, ex);
        }
    }

    @Before
    public void reset() {
        currentTestClass = this.getClass().getName();
        clickAndWait("treeForm:tree:ct", TRIGGER_COMMON_TASKS);
    }

    protected String generateRandomString() {
        SecureRandom random = new SecureRandom();

        // prepend a letter to insure valid JSF ID, which is causing failures in some areas
        return "a" + new BigInteger(130, random).toString(16);
    }

    protected int generateRandomNumber() {
        Random r = new Random();
        return Math.abs(r.nextInt()) + 1;
    }

    protected int generateRandomNumber(int max) {
        Random r = new Random();
        return Math.abs(r.nextInt(max - 1)) + 1;
    }

    protected <T> T selectRandomItem(T... items) {
        Random r = new Random();

        return items[r.nextInt(items.length)];
    }

    protected int getTableRowCount(String id) {
        String text = selenium.getText(id);
        int count = Integer.parseInt(text.substring(text.indexOf("(") + 1, text.indexOf(")")));

        return count;
    }

    protected void openAndWait(String url, String triggerText) {
        openAndWait(url, triggerText, TIMEOUT);
    }

    protected void openAndWait(String url, String triggerText, int timeout) {
        selenium.open(url);
        // wait for 2 minutes, as that should be enough time to insure that the admin console app has been deployed by the server
        waitForPageLoad(triggerText, timeout);
    }

    /**
     * Click the specified element and wait for the specified trigger text on the resulting page, timing out TIMEOUT seconds.
     *
     * @param triggerText
     */
    protected void clickAndWait(String id, String triggerText) {
        clickAndWait(id, triggerText, TIMEOUT);
    }

    protected void clickAndWait(String id, String triggerText, int seconds) {
        log ("Clicking on {0} and waiting for \'{1}\'", id, triggerText);
        insureElementIsVisible(id);
        selenium.click(id);
        waitForPageLoad(triggerText, seconds);
    }
    
    protected void clickAndWait(String id, WaitForLoadCallBack callback) {
        insureElementIsVisible(id);
        selenium.click(id);
        waitForLoad(TIMEOUT, callback);
    }

    protected void clickAndWaitForElement(String clickId, final String elementId) {
        selenium.click(clickId);
        waitForLoad(60, new WaitForLoadCallBack() {
            @Override
            public boolean executeTest() {
                if (selenium.isElementPresent(elementId)) {
                    return true;
                }
                
                return false;
            }
            
        });
    }

    protected void clickAndWaitForButtonEnabled(String id) {
        selenium.click(id);
        waitForButtonEnabled(id);
    }

    // Argh!
    protected void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
//            e.printStackTrace();
        }
    }

    /**
     * Cause the test to wait for the page to load.  This will be used, for example, after an initial page load
     * (selenium.open) or after an Ajaxy navigation request has been made.
     *
     * @param triggerText The text that should appear on the page when it has finished loading
     * @param timeout     How long to wait (in seconds)
     */
    protected void waitForPageLoad(String triggerText, int timeout) {
        waitForLoad(timeout, new PageLoadCallBack(triggerText, false));
    }

    protected void waitForPageLoad(final String triggerText, final boolean textShouldBeMissing) {
        waitForPageLoad(triggerText, TIMEOUT, textShouldBeMissing);
    }

    protected void waitForPageLoad(final String triggerText, final int timeout, final boolean textShouldBeMissing) {
        waitForLoad(timeout, new PageLoadCallBack(triggerText, textShouldBeMissing));        
    }
    
    protected void waitForLoad(int timeoutInSeconds, WaitForLoadCallBack callback) {
        for (int halfSeconds = 0;; halfSeconds++) {
            if (halfSeconds >= (timeoutInSeconds*2)) {
                Assert.fail("The operation timed out waiting for the page to load.");
            }

            try {
                RenderedWebElement ajaxPanel = (RenderedWebElement) driver.findElement(By.id(AJAX_INDICATOR));
                if (!ajaxPanel.isDisplayed()) {
                    if (callback.executeTest()) {
                        break;
                    }
                }
            } catch (NoSuchElementException nse) {
            }

            sleep(500);
        }
    }

    protected void handleLogin() {
        processingLogin = true;
        selenium.type("j_username", "admin");
        selenium.type("j_password", "");
        clickAndWait("loginButton", TRIGGER_COMMON_TASKS);
        processingLogin = false;
    }

    protected void waitForButtonEnabled(String buttonId) {
//        waitForCondition("document.getElementById('" + buttonId + "').disabled == false", BUTTON_TIMEOUT);
        waitForLoad(BUTTON_TIMEOUT, new ButtonDisabledStateCallBack(buttonId, false));
    }

    protected void waitForButtonDisabled(String buttonId) {
        String value = selenium.getEval(CURRENT_WINDOW + ".document.getElementById('" + buttonId + "').disabled");
//        waitForCondition("document.getElementById('" + buttonId + "').disabled == true", BUTTON_TIMEOUT);
        waitForLoad(BUTTON_TIMEOUT, new ButtonDisabledStateCallBack(buttonId, true));
    }

    protected void waitForCondition(String js, int timeOutInMillis) {
        selenium.waitForCondition(CURRENT_WINDOW + "." + js, Integer.toString(timeOutInMillis));
    }

    protected void deleteRow(String buttonId, String tableId, String triggerText) {
        deleteRow(buttonId, tableId, triggerText, "col0", "col1");
    }

    protected void deleteRow(final String buttonId, final String tableId, final String triggerText, final String selectColId, final String valueColId) {
        rowActionWithConfirm(buttonId, tableId, triggerText, selectColId, valueColId);
        waitForLoad(TIMEOUT, new DeleteRowCallBack(tableId, triggerText, valueColId));
//        waitForPageLoad(triggerText, true);
    }

    protected void rowActionWithConfirm(String buttonId, String tableId, String triggerText) {
        rowActionWithConfirm(buttonId, tableId, triggerText, "col0", "col1");
    }

    protected void rowActionWithConfirm(String buttonId, String tableId, String triggerText, String selectColId, String valueColId) {
        // A defensive getConfirmation()
        if (selenium.isConfirmationPresent()) {
            selenium.getConfirmation();
        }
        selenium.chooseOkOnNextConfirmation();
        selectTableRowByValue(tableId, triggerText, selectColId, valueColId);
        sleep(500); // argh!
        waitForButtonEnabled(buttonId);
        selenium.click(buttonId);
        if (selenium.isConfirmationPresent()) {
            selenium.getConfirmation();
        }
        sleep(500); // argh!
        waitForButtonDisabled(buttonId);
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
        WebElement link = driver.findElement(By.linkText(value));
        return (link == null) ?  null : (String)link.getAttribute("id");
        /*
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
        */
    }
    
    protected boolean isTextPresent(String text) {
        return selenium.isTextPresent(resolveTriggerText(text));
    }
    
    protected void selectDropdownOption(String id, String label) {
        try {
            label = resolveTriggerText(label);
            selenium.select(id, "label="+label);
        } catch (SeleniumException se) {
            logger.info("An invalid option was requested.  Here are the valid options:");
            for (String option : selenium.getSelectOptions(id)) {
                logger.log(Level.INFO, "\t{0}", option);
            }
            throw se;
        }
    }

    protected void selectTableRowByValue(String tableId, String value) {
        selectTableRowByValue(tableId, value, "col0", "col1");
    }

    protected void selectTableRowByValue(String tableId, String value, String selectColId, String valueColId) {
        List<String> rows = getTableRowsByValue(tableId, value, valueColId);
        for (String row : rows) {
            // It seems this must be click for the JS to fire in the browser
            final String id = row + ":" + selectColId + ":select";
            selenium.click(id); 
            selenium.check(id); 
        }
    }

    /**
     * @See selectTableRowByValue(String tableId, String value, String selectColId, String valueColId);
     * @param baseId
     * @param value
     * @return
     */
    protected int selectTableRowsByValue(String baseId, String value) {
        return selectTableRowsByValue(baseId, value, "col0", "col1");
    }

    /**
     * For the given table, this method will select each row whose value in the specified column
     * matched the value given, returning the number of rows selected.
     */
    protected int selectTableRowsByValue(String tableId, String value, String selectColId, String valueColId) {
        List<String> rows = getTableRowsByValue("propertyForm:instancesTable", value, "col6");
        if (!rows.isEmpty()) {
            for (String row : rows) {
                selenium.click(row + ":col0:select");
                selenium.check(row + ":col0:select");
            }
        }

        return rows.size();
    }

    protected void deleteAllTableRows(String tableId) {
        String deleteButtonId = tableId + ":topActionsGroup1:button1";
        selectAllTableRows(tableId);
        waitForButtonEnabled(deleteButtonId);
        selenium.chooseOkOnNextConfirmation();
        selenium.click(deleteButtonId);
        if (selenium.isConfirmationPresent()) {
            selenium.getConfirmation();
        }
        this.waitForButtonDisabled(deleteButtonId);
    }
    
    protected void selectAllTableRows(String tableId) {
        int count = getTableRowCount(tableId);
        for (int i = 0 ; i < count; i++) {
            selenium.click(tableId+":rowGroup1:" + i +":col0:select");
            selenium.check(tableId+":rowGroup1:" + i +":col0:select");
        }
    }

    // TODO: write javadocs for this
    protected String getTableRowByValue(String tableId, String value, String valueColId) {
        try {
            int row = 0;
            while (true) { // iterate over any rows
                // Assume one row group for now and hope it doesn't bite us
                String text = selenium.getText(tableId + ":rowGroup1:" + row + ":" + valueColId);
                if (text.equals(value)) {
                    return tableId + ":rowGroup1:" + row + ":";
                }
                row++;
            }
        } catch (Exception e) {
            Assert.fail("The specified row was not found: " + value);
            return "";
        }
    }
    
    protected List<String> getTableRowsByValue(String tableId, String value, String valueColId) {
        List<String> rows = new ArrayList<String>();
        try {
            int row = 0;
            while (true) { // iterate over any rows
                // Assume one row group for now and hope it doesn't bite us
                String text = selenium.getText(tableId + ":rowGroup1:" + row + ":" + valueColId);
                if (text.contains(value)) {
                    rows.add(tableId + ":rowGroup1:" + row);
                }
                row++;
            }
        } catch (Exception e) {
        }

        return rows;
    }

    // TODO: write javadocs for this
    protected int getTableRowCountByValue(String tableId, String value, String valueColId, Boolean isLabel) {
        int tableCount = getTableRowCount(tableId);
        int selectedCount = 0;
        try {
            for (int i = 0; i < tableCount; i++) {
                String text = "";
                if (isLabel) {
                    text = selenium.getText(tableId + ":rowGroup1:" + i + ":" + valueColId);
                } else {
                    text = selenium.getValue(tableId + ":rowGroup1:" + i + ":" + valueColId);
                }
                if (text.equals(value)) {
                    selectedCount++;
                }
            }
        } catch (Exception e) {
            Assert.fail("The specified row was not found: " + value);
            return 0;
        }
        return selectedCount;
    }

    protected int getTableRowCountByValue(String tableId, String value, String valueColId) {
        return getTableRowCountByValue(tableId, value, valueColId, true);
    }
    
    protected List<String> getTableColumnValues(String tableId, String columnId) {
        List<String> values = new ArrayList<String>();
        int tableCount = getTableRowCount(tableId);
        try {
            int row = 0;
            while (true) { // iterate over any rows
                // Assume one row group for now and hope it doesn't bite us
                values.add(selenium.getText(tableId + ":rowGroup1:" + row + ":" + columnId));
                row++;
            }
        } catch (Exception e) {
        }

        return values;
    }
    
    protected boolean tableContainsRow(String tableId, String columnId, String value) {
        return getTableRowCountByValue(tableId, value, columnId) > 0;
    }

    protected int addTableRow(String tableId, String buttonId) {
        return addTableRow(tableId, buttonId, "Additional Properties");
    }

    protected int addTableRow(String tableId, String buttonId, String countLabel) {
        int count = getTableRowCount(tableId);
        clickAndWait(buttonId, new AddTableRowCallBack(tableId, count));
        return ++count;
    }

    protected void assertTableRowCount(String tableId, int count) {
        Assert.assertEquals(count, getTableRowCount(tableId));
    }

    // Look at all those params. Maybe this isn't such a hot idea.
    /**
     * @param resourceName
     * @param tableId
     * @param enableButtonId
     * @param statusID
     * @param backToTableButtonId
     * @param tableTriggerText
     * @param editTriggerText
     */
    protected void testEnableButton(String resourceName,
            String tableId,
            String enableButtonId,
            String statusID,
            String backToTableButtonId,
            String tableTriggerText,
            String editTriggerText,
            String statusMsg) {
        testEnableDisableButton(resourceName, tableId, enableButtonId, statusID, backToTableButtonId, tableTriggerText, editTriggerText, statusMsg);
    }

    protected void testDisableButton(String resourceName,
            String tableId,
            String disableButtonId,
            String statusId,
            String backToTableButtonId,
            String tableTriggerText,
            String editTriggerText,
            String statusMsg) {
        testEnableDisableButton(resourceName, tableId, disableButtonId, statusId, backToTableButtonId, tableTriggerText, editTriggerText, statusMsg);
    }

    private void testEnableDisableButton(String resourceName,
            String tableId,
            String enableButtonId,
            String statusId,
            String backToTableButtonId,
            String tableTriggerText,
            String editTriggerText,
            String state) {
        selectTableRowByValue(tableId, resourceName);
        waitForButtonEnabled(enableButtonId);
        selenium.click(enableButtonId);
        waitForButtonDisabled(enableButtonId);

        clickAndWait(getLinkIdByLinkText(tableId, resourceName), editTriggerText);
        // TODO: this is an ugly, ugly hack and needs to be cleaned up
        if(state.contains("Target")) {
            Assert.assertEquals(state, selenium.getText(statusId));
        } else {
            if ("on".equals(state) || "off".equals(state)) {
                Assert.assertEquals("on".equals(state), selenium.isChecked(statusId));
            } else {
                Assert.assertEquals(state, selenium.getValue(statusId));
            }
        }
        clickAndWait(backToTableButtonId, tableTriggerText);
    }

    protected void testEnableOrDisableTarget(String tableSelectMutlipleId,
            String enableButtonId,
            String generalTabId,
            String targetTabId,
            String statusId,
            String generalTriggerText,
            String targetTriggerText,
            String state) {
        selenium.click(tableSelectMutlipleId);
        waitForButtonEnabled(enableButtonId);
        selenium.click(enableButtonId);
        waitForButtonDisabled(enableButtonId);

        clickAndWait(generalTabId, generalTriggerText);
        Assert.assertEquals(state, selenium.getText(statusId));

        clickAndWait(targetTabId, targetTriggerText);
    }

    protected void testManageTargets(String resourcesLinkId,
            String resourcesTableId,
            String enableButtonId,
            String disableButtonId,
            String enableOrDisableTextFieldId,
            String resGeneralTabId,
            String resTargetTabId,
            String resourcesTriggerText,
            String resEditTriggerText,
            String jndiName,
            String instanceName) {
        final String TRIGGER_EDIT_RESOURCE_TARGETS = "Resource Targets";
        final String enableStatus = "Enabled on 2 of 2 Target(s)";
        final String disableStatus = "Enabled on 0 of 2 Target(s)";
        final String TRIGGER_MANAGE_TARGETS = "Manage Resource Targets";
        final String DEFAULT_SERVER = "server";

        reset();
        clickAndWait(resourcesLinkId, resourcesTriggerText);
        clickAndWait(getLinkIdByLinkText(resourcesTableId, jndiName), resEditTriggerText);
        //Click on the target tab and verify whether the target is in the target table or not.
        clickAndWait(resTargetTabId, TRIGGER_EDIT_RESOURCE_TARGETS);
        Assert.assertTrue(selenium.isTextPresent(instanceName));

        //Disable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                disableButtonId,
                resGeneralTabId,
                resTargetTabId,
                enableOrDisableTextFieldId,
                resEditTriggerText,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                disableStatus);

        //Enable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                enableButtonId,
                resGeneralTabId,
                resTargetTabId,
                enableOrDisableTextFieldId,
                resEditTriggerText,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                enableStatus);

        //Test the manage targets : Remove the server from targets.
        clickAndWait("propertyForm:targetTable:topActionsGroup1:manageTargetButton", TRIGGER_MANAGE_TARGETS);
        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_selected", "label=" + DEFAULT_SERVER);
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        clickAndWait("form:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);

        //Test the issue : 13280
        //If server instance is not one of the target, edit resource was failing. Fixed that and added a test
        clickAndWait(resourcesLinkId, resourcesTriggerText);
        clickAndWait(getLinkIdByLinkText(resourcesTableId, jndiName), resEditTriggerText);
        Assert.assertTrue(isTextPresent(jndiName));
        clickAndWait(resTargetTabId, TRIGGER_EDIT_RESOURCE_TARGETS);

        //Test the manage targets : Remove the instance and add the server.
        clickAndWait("propertyForm:targetTable:topActionsGroup1:manageTargetButton", TRIGGER_MANAGE_TARGETS);
        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_selected", "label=" + instanceName);
        waitForButtonEnabled("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        waitForButtonDisabled("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        selenium.removeAllSelections("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available");

        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", "label=" + DEFAULT_SERVER);
        waitForButtonEnabled("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");
        waitForButtonDisabled("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");
        clickAndWait("form:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        waitForPageLoad(instanceName, false);
        Assert.assertTrue(isTextPresent(DEFAULT_SERVER));

        //Go Back to Resources Page
        clickAndWait(resourcesLinkId, resourcesTriggerText);
    }
    
    protected void logDebugMessage(String message) {
        if (debug) {
            logger.info(message);
        }
    }

    protected static String getParameter(String paramName, String defaultValue) {
        String value = System.getProperty(paramName);

        return value != null ? value : defaultValue;
    }
    
    protected String resolveTriggerText(String original) {
        String triggerText = original;
        int index = original.indexOf(".");
        if (index > -1) {
            String bundleName = original.substring(0, index);
            String key = original.substring(index + 1);
            String bundle = bundles.get(bundleName);
            if (bundle != null) {
                ResourceBundle res = ResourceBundle.getBundle(bundle);
                if (res != null) {
                    // Strip out HTML. Hopefully this will be robust enough
                    triggerText = res.getString(key).replaceAll("<.*?>", "");
                } else {
                    Logger.getLogger(BaseSeleniumTestClass.class.getName()).log(Level.WARNING, null, "An invalid resource bundle was specified: " + original);
                }
            }
        }
        return triggerText;
    }
    
    protected void log(String message, String... args) {
        if (debug) {
            String[] temp = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                temp[i] = resolveTriggerText(args[i]);
            }
            logger.log(Level.INFO, message, temp);
        }
    }
    
    private void insureElementIsVisible (final String id) {
        if (!id.contains("treeForm:tree")) {
            return;
        }
        
        try {
            RenderedWebElement element = (RenderedWebElement) driver.findElement(By.id(id));
            if (element.isDisplayed()) {
                return;
            }
        } catch (StaleElementReferenceException sere) {
            
        }
        
        final String parentId = id.substring(0, id.lastIndexOf(":"));
        final RenderedWebElement parentElement = (RenderedWebElement)driver.findElement(By.id(parentId));
        if (!parentElement.isDisplayed()) {
            insureElementIsVisible(parentId);
            String grandParentId = parentId.substring(0, parentId.lastIndexOf(":"));
            String nodeId = grandParentId.substring(grandParentId.lastIndexOf(":")+1);
            selenium.click(grandParentId + ":" + nodeId+"_turner");
        }
    }
    
    class PageLoadCallBack implements WaitForLoadCallBack {
        boolean textShouldBeMissing;
        String triggerText;

        public PageLoadCallBack(String triggerText, boolean textShouldBeMissing) {
            this.textShouldBeMissing = textShouldBeMissing;
            this.triggerText = resolveTriggerText(triggerText);
        }

        
        @Override
        public boolean executeTest() {
            boolean found = false;
            try {
                if (selenium.isElementPresent("j_username") && !processingLogin) {
                    handleLogin();
                }
                if (!textShouldBeMissing) {
                    if (selenium.isTextPresent(triggerText)) {
                        found = true;
                    }
                } else if (!selenium.isTextPresent(triggerText)) {
                        found = true;
                    
                } else {
                    if (selenium.isTextPresent("RuntimeException")) {
                        throw new RuntimeException("Exception detected on page. Please check the logs for details");
                    }
                }
            } catch (SeleniumException se) {
                String message = se.getMessage();
                if (!"ERROR: Couldn't access document.body.  Is this HTML page fully loaded?".equals(se.getMessage())) {
                    throw new RuntimeException(se);
                }
            }

            return found;
        }
    };
    
    class DeleteRowCallBack implements WaitForLoadCallBack {
        private String tableId;
        private String tableRowValue;
        private String tableColId;

        public DeleteRowCallBack(String tableId, String tableRowValue, String tableColId) {
            this.tableId = tableId;
            this.tableRowValue = tableRowValue;
            this.tableColId = tableColId;
        }

        @Override
        public boolean executeTest() {
            try {
                List<String> rows = getTableRowsByValue(tableId, tableRowValue, tableColId);
                return rows.isEmpty();
            } catch (SeleniumException se) {
                return false;
            }
        }
        
    }
    
    class AddTableRowCallBack implements WaitForLoadCallBack {
        private final String tableId;
        private final int initialCount;

        public AddTableRowCallBack(String tableId, int initialCount) {
            this.tableId = tableId;
            this.initialCount = initialCount;
        }
        
        @Override
        public boolean executeTest() {
            int count = getTableRowCount(tableId);
            return count > initialCount;
        }
        
    };
    
    class ButtonDisabledStateCallBack implements WaitForLoadCallBack {
        private String buttonId;
        private boolean desiredState;

        public ButtonDisabledStateCallBack(String buttonId, boolean desiredState) {
            this.buttonId = buttonId;
            this.desiredState = desiredState;
        }

        @Override
        public boolean executeTest() {
//            String attr = selenium.getEval("this.browserbot.findElement('id=" + buttonId + "').disabled"); // "Classic" Selenium
            try {
                String attr = driver.findElement(By.id(buttonId)).getAttribute("disabled"); // WebDriver-backed Selenium
                return (Boolean.parseBoolean(attr) == desiredState);
            } catch (Exception ex) {
                return true;// ???
            }
        }
        
        
    }
}

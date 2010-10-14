/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.Rule;

public class BaseSeleniumTestClass {

    public static final String CURRENT_WINDOW = "selenium.browserbot.getCurrentWindow()";
    public static final String MSG_NEW_VALUES_SAVED = "New values successfully saved.";
    public static final String TRIGGER_COMMON_TASKS = "Please Register";
    public static final String TRIGGER_REGISTRATION_PAGE = "Receive patch information and bug updates, screencasts and tutorials, support and training offerings, and more";
    public static final String MSG_ERROR_OCCURED = "An error has occurred";

    @Rule
    public SpecificTestRule specificTestRule = new SpecificTestRule();

    protected static Selenium selenium;
    protected static final int TIMEOUT = 90;
    protected static final int BUTTON_TIMEOUT = 30000;
    private static String currentTestClass = "";
    protected static boolean debug;
    private boolean processingLogin = false;

    @BeforeClass
    public static void setUp() throws Exception {
        String browser = getParameter("browser", "firefox");
        String port = getParameter("admin.port", "4848");
        String seleniumPort = getParameter("selenium.port", "4444");
        String baseUrl = "http://localhost:" + port;
        debug = Boolean.parseBoolean(getParameter("debug", "false"));

        if (selenium == null) {
            System.out.println("The GlassFish Admin console is at " + baseUrl + ".  The Selenium server is listening on " + seleniumPort
                    + " and will use " + browser + " as the test browser.");

            selenium = new DefaultSelenium("localhost", Integer.parseInt(seleniumPort), "*" + browser, baseUrl);
            selenium.start();
            selenium.setTimeout("90000");
            (new BaseSeleniumTestClass()).openAndWait("/", TRIGGER_COMMON_TASKS, 480); // Make sure the server has started and the user logged in
        }

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

    @AfterClass
    public static void captureLog() {
        try {
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
        } catch (Exception ex) {
            Logger.getLogger(BaseSeleniumTestClass.class.getName()).log(Level.INFO, null, ex);
        }
    }

    @Before
    public void reset() {
        currentTestClass = this.getClass().getName();
        clickAndWait("treeForm:tree:registration:registration_link", TRIGGER_REGISTRATION_PAGE);
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
        selenium.click(id);
        waitForPageLoad(triggerText, seconds);
    }

    protected void clickAndWaitForElement(String clickId, String elementId) {
        selenium.click(clickId);
        for (int second = 0;; second++) {
            if (second >= 60) {
                Assert.fail("timeout");
            }
            try {
                if (selenium.isElementPresent(elementId)) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            sleep(500);
        }
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
            e.printStackTrace();
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
        waitForPageLoad(triggerText, timeout, false);
    }

    protected void waitForPageLoad(String triggerText, boolean textShouldBeMissing) {
        waitForPageLoad(triggerText, TIMEOUT, textShouldBeMissing);
    }

    protected void waitForPageLoad(String triggerText, int timeout, boolean textShouldBeMissing) {
        for (int second = 0;; second++) {
            if (second >= timeout) {
                Assert.fail("timeout");
            }
            try {
                if (selenium.isElementPresent("j_username") && !processingLogin){
                    handleLogin();
                }
                if (!textShouldBeMissing) {
                    if (selenium.isTextPresent(triggerText)) {
                        break;
                    }
                } else {
                    if (!selenium.isTextPresent(triggerText)) {
                        break;
                    }
                }
            } catch (SeleniumException se) {
                String message = se.getMessage();
                if (!"ERROR: Couldn't access document.body.  Is this HTML page fully loaded?".equals(se.getMessage())) {
                    throw new RuntimeException(se);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            sleep(1500);
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
        waitForCondition("document.getElementById('" + buttonId + "').disabled == false", BUTTON_TIMEOUT);
    }

    protected void waitForButtonDisabled(String buttonId) {
        String value = selenium.getEval(CURRENT_WINDOW + ".document.getElementById('" + buttonId + "').disabled");
        waitForCondition("document.getElementById('" + buttonId + "').disabled == true", BUTTON_TIMEOUT);
    }

    protected void waitForCondition(String js, int timeOutInMillis) {
        selenium.waitForCondition(CURRENT_WINDOW + "." + js, Integer.toString(timeOutInMillis));
    }

    protected void deleteRow(String buttonId, String tableId, String triggerText) {
        deleteRow(buttonId, tableId, triggerText, "col0", "col1");
    }

    protected void deleteRow(String buttonId, String tableId, String triggerText, String selectColId, String valueColId) {
        rowActionWithConfirm(buttonId, tableId, triggerText, selectColId, valueColId);
        waitForPageLoad(triggerText, true);
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
        waitForButtonEnabled(buttonId);
        selenium.click(buttonId);
        if (selenium.isConfirmationPresent()) {
            selenium.getConfirmation();
        }
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
        List<String> rows = getTableRowsByValue(tableId, value, valueColId);
        for (String row : rows) {
            selenium.click(row + ":" + selectColId + ":select");
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
            }
        }

        return rows.size();
    }

    protected void deleteAllTableRows(String selectAllButtonId, String deleteButtonId) {
        selenium.click(selectAllButtonId);
        waitForButtonEnabled(deleteButtonId);
        selenium.chooseOkOnNextConfirmation();
        selenium.click(deleteButtonId);
        if (selenium.isConfirmationPresent()) {
            selenium.getConfirmation();
        }
        this.waitForButtonDisabled(deleteButtonId);
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
    protected int getTableRowCountByValue(String tableId, String value, String valueColId) {
        int tableCount = getTableRowCount(tableId);
        int selectedCount = 0;
        try {
            for (int i = 0; i < tableCount; i++) {
                String text = selenium.getText(tableId + ":rowGroup1:" + i + ":" + valueColId);
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

    protected int addTableRow(String tableId, String buttonId) {
        return addTableRow(tableId, buttonId, "Additional Properties");
    }

    protected int addTableRow(String tableId, String buttonId, String countLabel) {
        int count = getTableRowCount(tableId);
        clickAndWait(buttonId, countLabel + " (" + (++count) + ")");
        return count;
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
        if(state.contains("Target")) {
            Assert.assertEquals(state, selenium.getText(statusId));
        } else {
            Assert.assertEquals(state, selenium.getValue(statusId));
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
        final String TRIGGER_MANAGE_TARGETS = "Manage Targets";
        final String TRIGGGER_VALUES_SAVED = "New values successfully saved.";
        final String DEFAULT_SERVER = "server";

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
        clickAndWait("form:propertyContentPage:topButtons:saveButton", TRIGGGER_VALUES_SAVED);

        //Test the issue : 13280
        //If server instance is not one of the target, edit resource was failing. Fixed that and added a test
        clickAndWait(resourcesLinkId, resourcesTriggerText);
        clickAndWait(getLinkIdByLinkText(resourcesTableId, jndiName), resEditTriggerText);
        Assert.assertTrue(selenium.isTextPresent(jndiName));
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
        clickAndWait("form:propertyContentPage:topButtons:saveButton", TRIGGGER_VALUES_SAVED);
        waitForPageLoad(instanceName, false);
        Assert.assertTrue(selenium.isTextPresent(DEFAULT_SERVER));

        //Go Back to Resources Page
        clickAndWait(resourcesLinkId, resourcesTriggerText);
    }

    private static String getParameter(String paramName, String defaultValue) {
        String value = System.getProperty(paramName);

        return value != null ? value : defaultValue;
    }
}

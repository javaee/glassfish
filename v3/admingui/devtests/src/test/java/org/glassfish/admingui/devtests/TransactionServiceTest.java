package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 12, 2010
 * Time: 1:36:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionServiceTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_TRANSACTION_SERVICE = "Modify general transaction service settings.";

    @Test
    public void testTransactionService() {
        final String timeout = Integer.toString(generateRandomNumber(60));
        final String retry = Integer.toString(generateRandomNumber(600));
        final String keypoint = Integer.toString(generateRandomNumber(65535));

        clickAndWait("treeForm:tree:configuration:transactionService:transactionService_link", TRIGGER_TRANSACTION_SERVICE);
        selenium.check("propertyForm:propertySheet:propertSectionTextField:onRestartProp:enabled");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:timeoutProp:Timeout", timeout);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:retryProp:Retry", retry);
        selenium.select("propertyForm:propertySheet:propertSectionTextField:heuristicProp:HeuristicDecision", "label=Commit");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:keyPointProp:Keypoint", keypoint);
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
        clickAndWait("treeForm:tree:ct", "Please Register");

        clickAndWait("treeForm:tree:configuration:transactionService:transactionService_link", TRIGGER_TRANSACTION_SERVICE);
        assertEquals("on", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:onRestartProp:enabled"));
        assertEquals(timeout, selenium.getValue("propertyForm:propertySheet:propertSectionTextField:timeoutProp:Timeout"));
        assertEquals(retry, selenium.getValue("propertyForm:propertySheet:propertSectionTextField:retryProp:Retry"));
        assertEquals("commit", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:heuristicProp:HeuristicDecision"));
        assertEquals(keypoint, selenium.getValue("propertyForm:propertySheet:propertSectionTextField:keyPointProp:Keypoint"));
        assertTableRowCount("propertyForm:basicTable", count);
    }
}

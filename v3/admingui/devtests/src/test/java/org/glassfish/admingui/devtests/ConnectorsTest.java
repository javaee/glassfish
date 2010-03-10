package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ConnectorsTest extends BaseSeleniumTestClass {
    @Test
    public void testConnectorResources() {
        String testPool = generateRandomString();
        String testConnector = generateRandomString();

        openAndWait("/jca/connectorConnectionPools.jsf", "Connector Connection Pools");

        // Create new connection connection pool
        selenium.click("propertyForm:poolTable:topActionsGroup1:newButton");
        waitForPageLoad("New Connector Connection Pool (Step 1 of 2)");

        selenium.type("propertyForm:propertySheet:generalPropertySheet:jndiProp:name", testPool);
        selenium.select("propertyForm:propertySheet:generalPropertySheet:resAdapterProp:db", "label=jmsra");
        waitForCondition("document.getElementById('propertyForm:propertySheet:generalPropertySheet:connectionDefProp:db').value != ''", 1000);

        selenium.select("propertyForm:propertySheet:generalPropertySheet:connectionDefProp:db", "label=javax.jms.QueueConnectionFactory");
        waitForButtonEnabled("propertyForm:title:topButtons:nextButton");

        selenium.click("propertyForm:title:topButtons:nextButton");
        waitForPageLoad("New Connector Connection Pool (Step 2 of 2)");

        selenium.select("propertyForm:propertySheet:poolPropertySheet:transprop:trans", "label=NoTransaction");
        selenium.click("propertyForm:propertyContentPage:topButtons:finishButton");

        // Verify pool creation
        waitForPageLoad("Click New to create a new connector connection pool.");
        assertTrue(selenium.isTextPresent(testPool));

        // Create new connector resource which uses this new pool
        selenium.click("treeForm:tree:resources:Connectors:connectorResources:connectorResources_link");
        waitForPageLoad("A connector resource is a program object");

        selenium.click("propertyForm:resourcesTable:topActionsGroup1:newButton");
        waitForPageLoad("New Connector Resource");

        selenium.type("propertyForm:propertySheet:propertSectionTextField:jndiTextProp:jnditext", testConnector);
        selenium.select("propertyForm:propertySheet:propertSectionTextField:poolNameProp:PoolName", "label=" + testPool);

        selenium.click("propertyForm:propertyContentPage:topButtons:newButton");
        waitForPageLoad("A connector resource is a program object that provides");

        // Disable resource
        assertTrue(selenium.isTextPresent(testConnector));
        selectTableRowByValue("propertyForm:resourcesTable", testConnector);
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:button3");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:button3");

        // Enable resource
        selectTableRowByValue("propertyForm:resourcesTable", testConnector);
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:button2");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:button2");

        // Delete connector resource
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", testConnector);

        // Delete connector connection pool
        selenium.click("treeForm:tree:resources:Connectors:connectorConnectionPools:connectorConnectionPools_link");
        waitForPageLoad("Click New to create a new connector connection pool.");

        deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", testPool);
    }
}
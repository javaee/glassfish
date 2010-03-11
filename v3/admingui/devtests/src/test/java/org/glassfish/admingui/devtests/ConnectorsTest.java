package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ConnectorsTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_CONNECTOR_CONNECTION_POOLS = "Click New to create a new connector connection pool.";
    private static final String TRIGGER_NEW_CONNECTOR_CONNECTION_POOL_STEP_1 = "New Connector Connection Pool (Step 1 of 2)";
    private static final String TRIGGER_NEW_CONNECTOR_CONNECTION_POOL_STEP_2 = "New Connector Connection Pool (Step 2 of 2)";
    private static final String TRIGGER_CONNECTOR_RESOURCE = "A connector resource is a program object";
    private static final String TRIGGER_NEW_CONNECTOR_RESOURCE = "New Connector Resource";
    private static final String TRIGGER_EDIT_CONNECTOR_RESOURCE = "Edit Connector Resource";

    @Test
    public void testConnectorResources() {
        String testPool = generateRandomString();
        String testConnector = generateRandomString();

        clickAndWait("treeForm:tree:resources:Connectors:connectorConnectionPools:connectorConnectionPools_link", TRIGGER_CONNECTOR_CONNECTION_POOLS);

        // Create new connection connection pool
        clickAndWait("propertyForm:poolTable:topActionsGroup1:newButton", TRIGGER_NEW_CONNECTOR_CONNECTION_POOL_STEP_1);

        selenium.type("propertyForm:propertySheet:generalPropertySheet:jndiProp:name", testPool);
        selenium.select("propertyForm:propertySheet:generalPropertySheet:resAdapterProp:db", "label=jmsra");
        waitForCondition("document.getElementById('propertyForm:propertySheet:generalPropertySheet:connectionDefProp:db').value != ''", 1000);

        selenium.select("propertyForm:propertySheet:generalPropertySheet:connectionDefProp:db", "label=javax.jms.QueueConnectionFactory");
        waitForButtonEnabled("propertyForm:title:topButtons:nextButton");

        clickAndWait("propertyForm:title:topButtons:nextButton", TRIGGER_NEW_CONNECTOR_CONNECTION_POOL_STEP_2);

        selenium.select("propertyForm:propertySheet:poolPropertySheet:transprop:trans", "label=NoTransaction");
        clickAndWait("propertyForm:propertyContentPage:topButtons:finishButton", TRIGGER_CONNECTOR_CONNECTION_POOLS);
        assertTrue(selenium.isTextPresent(testPool));

        // Create new connector resource which uses this new pool
        clickAndWait("treeForm:tree:resources:Connectors:connectorResources:connectorResources_link", TRIGGER_CONNECTOR_RESOURCE);

        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_CONNECTOR_RESOURCE);

        selenium.type("propertyForm:propertySheet:propertSectionTextField:jndiTextProp:jnditext", testConnector);
        selenium.select("propertyForm:propertySheet:propertSectionTextField:poolNameProp:PoolName", "label=" + testPool);

        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_CONNECTOR_RESOURCE);

        // Disable resource
        testDisableButton(testConnector,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CONNECTOR_RESOURCE,
                TRIGGER_EDIT_CONNECTOR_RESOURCE);

        // Enable resource
        testEnableButton(testConnector,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CONNECTOR_RESOURCE,
                TRIGGER_EDIT_CONNECTOR_RESOURCE);

        // Delete connector resource
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", testConnector);

        // Delete connector connection pool
        clickAndWait("treeForm:tree:resources:Connectors:connectorConnectionPools:connectorConnectionPools_link", TRIGGER_CONNECTOR_CONNECTION_POOLS);

        deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", testPool);
    }
}
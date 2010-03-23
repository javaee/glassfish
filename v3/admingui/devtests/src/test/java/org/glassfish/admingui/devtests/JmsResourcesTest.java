package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class JmsResourcesTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_JMS_CONNECTION_FACTORIES = "Java Message Service (JMS) connection factories";
    private static final String TRIGGER_NEW_JMS_CONN_FACT = "New JMS Connection Factory";
    private static final String TRIGGER_EDIT_JMS_CONN_FACT = "Edit JMS Connection Factory";
    private static final String TRIGGER_JMS_DESTINATION_RESOURCES = "Click New to create a new destination resource. Click the name of a destination resource to modify its properties.";
    private static final String TRIGGER_NEW_JMS_DEST_RES = "New JMS Destination Resource";
    private static final String TRIGGER_EDIT_JMS_DEST_RES = "Edit JMS Destination Resource";

    @Test
    public void testAddingConnectionFactories() throws Exception {
        final String poolName = generateRandomString();
        final String description = "Test Pool - " + poolName;

        clickAndWait("treeForm:tree:resources:jmsResources:jmsConnectionFactories:jmsConnectionFactories_link", TRIGGER_JMS_CONNECTION_FACTORIES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_JMS_CONN_FACT);

        selenium.type("propertyForm:propertySheet:generalPropertySheet:jndiProp:jndiProp", poolName);
        selenium.select("propertyForm:propertySheet:generalPropertySheet:resType:resType", "label=javax.jms.TopicConnectionFactory");
        selenium.type("propertyForm:propertySheet:generalPropertySheet:descProp:descProp", description);
        selenium.select("propertyForm:propertySheet:poolPropertySheet:transprop:trans", "label=LocalTransaction");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_JMS_CONNECTION_FACTORIES);
        assertTrue(selenium.isTextPresent(poolName));

        // This can't currently use testDisableButton/testEnableButton because the table is different from the others
        // The table should be fixed to be like the others (in terms of IDs) so the standard test API can be used here.
        selectTableRowByValue("propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");

        selectTableRowByValue("propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:deleteConnButton", "propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
    }

    @Test
    public void testAddingDestinationResources() throws Exception {
        final String resourceName = generateRandomString();
        final String description = "Test Destination - " + resourceName;

        clickAndWait("treeForm:tree:resources:jmsResources:jmsDestinationResources:jmsDestinationResources_link", TRIGGER_JMS_DESTINATION_RESOURCES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_JMS_DEST_RES);
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:jndiProp:jndi", resourceName);
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:nameProp:name", "somePhysicalDestination");
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:descProp:desc", description);
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_JMS_DESTINATION_RESOURCES);
        assertTrue(selenium.isTextPresent(resourceName) && selenium.isTextPresent(description));

        // This can't currently use testDisableButton/testEnableButton because the table is different from the others
        // The table should be fixed to be like the others (in terms of IDs) so the standard test API can be used here.
        selectTableRowByValue("propertyForm:resourcesTable", resourceName, "colSelect", "colName");
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");

        selectTableRowByValue("propertyForm:resourcesTable", resourceName, "colSelect", "colName");
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:deleteDestButton", "propertyForm:resourcesTable", resourceName, "colSelect", "colName");
    }
}
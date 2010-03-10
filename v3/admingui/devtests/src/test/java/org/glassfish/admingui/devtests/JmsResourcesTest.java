package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class JmsResourcesTest extends BaseSeleniumTestClass {
    @Test
    public void testAddingConnectionFactories() throws Exception {
        final String poolName = generateRandomString();
        final String description = "Test Pool - " + poolName;

        clickAndWait("treeForm:tree:resources:jmsResources:jmsConnectionFactories:jmsConnectionFactories_link", "JMS Connection Factories");
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton","Pool Name:");

        selenium.type("propertyForm:propertySheet:generalPropertySheet:jndiProp:jndiProp", poolName);
        selenium.select("propertyForm:propertySheet:generalPropertySheet:resType:resType", "label=javax.jms.TopicConnectionFactory");
        selenium.type("propertyForm:propertySheet:generalPropertySheet:descProp:descProp", description);
        selenium.select("propertyForm:propertySheet:poolPropertySheet:transprop:trans", "label=LocalTransaction");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", "JMS Connection Factories");
        assertTrue(selenium.isTextPresent(poolName));

        selectTableRowByValue("propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");

        selectTableRowByValue("propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:deleteConnButton", "propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
    }

    @Test
    public void testAddingDestinationResources() throws Exception {
        final String resourceName = generateRandomString();
        final String description = "Test Destination - " + resourceName;

//        openAndWait("/jms/jmsDestinations.jsf", "JMS Destination Resources");
        clickAndWait("treeForm:tree:resources:jmsResources:jmsDestinationResources:jmsDestinationResources_link", "JMS Destination Resources");
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", "JNDI Name:");
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:jndiProp:jndi", resourceName);
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:nameProp:name", "somePhysicalDestination");
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:descProp:desc", description);
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", "JMS Destination Resources");
        assertTrue(selenium.isTextPresent(resourceName) && selenium.isTextPresent(description));

        // TODO : write a better test for this. disabling for now
        selectTableRowByValue("propertyForm:resourcesTable", resourceName, "colSelect", "colName");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");

        selectTableRowByValue("propertyForm:resourcesTable", resourceName, "colSelect", "colName");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:deleteDestButton", "propertyForm:resourcesTable", resourceName, "colSelect", "colName");
    }
}
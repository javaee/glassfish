package org.glassfish.admingui.devtests;

import org.junit.Test;
import static org.junit.Assert.*;


public class JmsResourcesTest extends BaseSeleniumTestClass {
    @Test
    public void testAddingConnectionFactories() throws Exception {
        final String poolName = generateRandomString();
        final String description = "Test Pool - " + poolName;

        openAndWait("/jms/jmsConnections.jsf", "JMS Connection Factories");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:newButton");
        waitForPageLoad("Pool Name:");
        selenium.type("propertyForm:propertySheet:generalPropertySheet:jndiProp:jndiProp", poolName);
        selenium.select("propertyForm:propertySheet:generalPropertySheet:resType:resType", "label=javax.jms.TopicConnectionFactory");
        selenium.type("propertyForm:propertySheet:generalPropertySheet:descProp:descProp", description);
        selenium.select("propertyForm:propertySheet:poolPropertySheet:transprop:trans", "label=LocalTransaction");
        selenium.click("propertyForm:propertyContentPage:topButtons:newButton");
        waitForPageLoad("JMS Connection Factories");
        assertTrue(selenium.isTextPresent(poolName));

        selectTableRowByValue("propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
		waitForPageLoad("false");

		selectTableRowByValue("propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
		waitForPageLoad("true");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:deleteConnButton","propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
    }

    @Test
    public void testAddingDestinationResources() throws Exception {
        final String resourceName = generateRandomString();
        final String description = "Test Destination - " + resourceName;

        openAndWait("/jms/jmsDestinations.jsf", "JMS Destination Resources");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:newButton");
        waitForPageLoad("JNDI Name:");
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:jndiProp:jndi", resourceName);
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:nameProp:name", "somePhysicalDestination");
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:descProp:desc", description);
        selenium.click("propertyForm:propertyContentPage:topButtons:newButton");
        waitForPageLoad("JMS Destination Resources");
        assertTrue(selenium.isTextPresent(resourceName) && selenium.isTextPresent(description));

        // TODO : write a better test for this. disabling for now
//        selectTableRowByValue("propertyForm:resourcesTable", resourceName, "colSelect", "colName");
//		selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
//		waitForPageLoad("false");
//
//		selectTableRowByValue("propertyForm:resourcesTable", resourceName, "colSelect", "colName");
//		selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
//		waitForPageLoad("true");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:deleteDestButton","propertyForm:resourcesTable", resourceName, "colSelect", "colName");
    }
}
package org.glassfish.admingui.devtests;

import org.junit.Test;
import static org.junit.Assert.*;


public class JmsResourcesTest extends BaseSeleniumTestClass {
    @Test
    public void testAddingConnectionFactories() throws Exception {
        selenium.open("/jms/jmsConnections.jsf");
        waitForPageLoad("JMS Connection Factories");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:newButton");
        waitForPageLoad("Pool Name:");
        selenium.type("propertyForm:propertySheet:generalPropertySheet:jndiProp:jndiProp", "TestPool");
        selenium.select("propertyForm:propertySheet:generalPropertySheet:resType:resType", "label=javax.jms.TopicConnectionFactory");
        selenium.type("propertyForm:propertySheet:generalPropertySheet:descProp:descProp", "Test Pool");
        selenium.select("propertyForm:propertySheet:poolPropertySheet:transprop:trans", "label=LocalTransaction");
        selenium.click("propertyForm:propertyContentPage:topButtons:newButton");
        waitForPageLoad("JMS Connection Factories");
        assertTrue(selenium.isTextPresent("TestPool"));

        selenium.click("propertyForm:resourcesTable:rowGroup1:0:colSelect:select");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
		waitForPageLoad("false");

		selenium.click("propertyForm:resourcesTable:rowGroup1:0:colSelect:select");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
		waitForPageLoad("true");

        selenium.chooseOkOnNextConfirmation();
        selenium.click("propertyForm:resourcesTable:rowGroup1:0:colSelect:select");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:deleteConnButton");
        selenium.getConfirmation();
        waitForPageLoad("Connection Factories (0)");

        assertFalse(selenium.isTextPresent("TestPool"));
    }

    public void testAddingDestinationResources() throws Exception {
        selenium.open("/jms/jmsDestinations.jsf");
        waitForPageLoad("JMS Destination Resources");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:newButton");
        waitForPageLoad("JNDI Name:");
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:jndiProp:jndi", "TestDestination");
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:nameProp:name", "somePhysicalDestination");
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:descProp:desc", "Test destination");
        selenium.click("propertyForm:propertyContentPage:topButtons:newButton");
        waitForPageLoad("JMS Destination Resources");
        assertTrue(selenium.isTextPresent("TestDestination"));

        selenium.click("propertyForm:resourcesTable:rowGroup1:0:colSelect:select");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
		waitForPageLoad("false");

		selenium.click("propertyForm:resourcesTable:rowGroup1:0:colSelect:select");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
		waitForPageLoad("true");

        selenium.chooseOkOnNextConfirmation();
		selenium.click("propertyForm:resourcesTable:rowGroup1:0:colSelect:select");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:deleteDestButton");
        selenium.getConfirmation();
        waitForPageLoad("Destination Resources (0)");
        assertFalse(selenium.isTextPresent("TestDestination"));
    }
}
package org.glassfish.admingui.devtests;

import org.junit.Test;
import static org.junit.Assert.*;


public class JmsResourcesTest extends BaseSeleniumTestClass {
    @Test
    public void testAddingConnectionFactories() throws Exception {
        selenium.open("/jms/jmsConnections.jsf");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:newButton");
        waitForAjaxLoad("Pool Name:");
        selenium.type("propertyForm:propertySheet:generalPropertySheet:jndiProp:jndiProp", "TestPool");
        selenium.select("propertyForm:propertySheet:generalPropertySheet:resType:resType", "label=javax.jms.TopicConnectionFactory");
        selenium.type("propertyForm:propertySheet:generalPropertySheet:descProp:descProp", "Test Pool");
        selenium.select("propertyForm:propertySheet:poolPropertySheet:transprop:trans", "label=LocalTransaction");
        selenium.click("propertyForm:propertyContentPage:topButtons:newButton");
        waitForAjaxLoad("JMS Connection Factories");
        assertTrue(selenium.isTextPresent("TestPool"));

        selenium.click("propertyForm:resourcesTable:rowGroup1:0:colSelect:select");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
		waitForAjaxLoad("false");

		selenium.click("propertyForm:resourcesTable:rowGroup1:0:colSelect:select");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
		waitForAjaxLoad("true");

        selenium.chooseOkOnNextConfirmation();
        selenium.click("propertyForm:resourcesTable:rowGroup1:0:colSelect:select");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:deleteConnButton");
        selenium.getConfirmation();
        waitForAjaxLoad("Connection Factories (0)");

        assertFalse(selenium.isTextPresent("TestPool"));
    }

    public void testAddingDestinationResources() throws Exception {
        selenium.open("/jms/jmsDestinations.jsf");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:newButton");
        waitForAjaxLoad("JNDI Name:");
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:jndiProp:jndi", "TestDestination");
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:nameProp:name", "somePhysicalDestination");
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:descProp:desc", "Test destination");
        selenium.click("propertyForm:propertyContentPage:topButtons:newButton");
        waitForAjaxLoad("JMS Destination Resources");
        assertTrue(selenium.isTextPresent("TestDestination"));

        selenium.click("propertyForm:resourcesTable:rowGroup1:0:colSelect:select");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
		waitForAjaxLoad("false");

		selenium.click("propertyForm:resourcesTable:rowGroup1:0:colSelect:select");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
		waitForAjaxLoad("true");

        selenium.chooseOkOnNextConfirmation();
		selenium.click("propertyForm:resourcesTable:rowGroup1:0:colSelect:select");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:deleteDestButton");
        selenium.getConfirmation();
        waitForAjaxLoad("Destination Resources (0)");
        assertFalse(selenium.isTextPresent("TestDestination"));
    }

    public static void main (String... args) throws Exception {
        JmsResourcesTest jrt = new JmsResourcesTest();
        ResourceAdapterConfigsTest ract = new ResourceAdapterConfigsTest();
        JmsResourcesTest.setUp();
        jrt.testAddingConnectionFactories();
        jrt.testAddingDestinationResources();
        ract.testResourceAdapterConfigs();
    }
}
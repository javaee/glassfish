package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.*;

public class ResourceAdapterConfigsTest extends BaseSeleniumTestClass {
    @Test
	public void testResourceAdapterConfigs() throws Exception {
		openAndWait("/jca/resourceAdapterConfigs.jsf", "Resource Adapter Configs");
        selenium.click("propertyForm:poolTable:topActionsGroup1:newButton");
        waitForPageLoad("Resource Adapter Name:");
        selenium.select("propertyForm:propertySheet:propertSectionTextField:threadPoolsIdProp:threadpoolsid", "label=http-thread-pool");
        selenium.type("propertyForm:basicTable:rowGroup1:1:col3:col1St", "password");
        selenium.type("propertyForm:basicTable:rowGroup1:2:col3:col1St", "username");
        selenium.click("propertyForm:propertyContentPage:topButtons:newButton");
        waitForPageLoad("A resource adapter config provides the configuration information for a resource adapter.");
        assertTrue(selenium.isTextPresent("jmsra"));
        selenium.click("propertyForm:poolTable:rowGroup1:0:col1:link");
        waitForPageLoad("Resource Adapter Name:");
        assertTrue("password".equals(selenium.getValue("propertyForm:basicTable:rowGroup1:0:col3:col1St")));
        assertTrue("username".equals(selenium.getValue("propertyForm:basicTable:rowGroup1:1:col3:col1St")));
        selenium.click("propertyForm:propertyContentPage:topButtons:saveButton");
        waitForPageLoad("New values successfully saved.");
        selenium.click("propertyForm:propertyContentPage:topButtons:cancelButton");
        waitForPageLoad("Configs (1)");
        assertTrue(selenium.isTextPresent("Resource Adapter Configs"));
        selenium.chooseOkOnNextConfirmation();
        selenium.click("propertyForm:poolTable:rowGroup1:0:col0:select");
        selenium.click("propertyForm:poolTable:topActionsGroup1:button1");
        selenium.getConfirmation();
        waitForPageLoad("Configs (0)");
        assertFalse(selenium.isTextPresent("jmsra"));
	}
}

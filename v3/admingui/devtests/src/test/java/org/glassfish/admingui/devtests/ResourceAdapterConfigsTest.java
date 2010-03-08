package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.*;

public class ResourceAdapterConfigsTest extends BaseSeleniumTestClass {
    @Test
	public void testResourceAdapterConfigs() throws Exception {
		openAndWait("/jca/resourceAdapterConfigs.jsf", "Resource Adapter Configs");

        try {
            // If this exists, delete it so the test below won't explode.  If it doesn't, just move along silently
            deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", "jmsra");
        } catch (AssertionError e) {

        }

        selenium.click("propertyForm:poolTable:topActionsGroup1:newButton");

        waitForPageLoad("Resource Adapter Name:");
        selenium.select("propertyForm:propertySheet:propertSectionTextField:threadPoolsIdProp:threadpoolsid", "label=http-thread-pool");
        selenium.click("propertyForm:propertyContentPage:topButtons:newButton");

        waitForPageLoad("A resource adapter config provides the configuration information for a resource adapter.");
        assertTrue(selenium.isTextPresent("jmsra"));
        selenium.click(getLinkIdByLinkText("propertyForm:poolTable", "jmsra"));

        waitForPageLoad("Resource Adapter Name:");
        selenium.click("propertyForm:propertyContentPage:topButtons:saveButton");

        waitForPageLoad("New values successfully saved.");
        selenium.click("propertyForm:propertyContentPage:topButtons:cancelButton");

        waitForPageLoad("A resource adapter config provides");
        
        deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", "jmsra");
	}
}

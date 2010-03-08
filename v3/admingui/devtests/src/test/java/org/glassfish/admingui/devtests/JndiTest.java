package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JndiTest extends BaseSeleniumTestClass {
    @Test
    public void testCustomResource() {
        final String resourceName = generateRandomString();
        
		openAndWait("/full/customResources.jsf", "Custom resources are nonstandard resources");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:newButton");
		waitForPageLoad("New Custom Resource");

        selenium.type("form1:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
		selenium.select("form1:propertySheet:propertSectionTextField:cp:Classname", "label=java.lang.Double");
		selenium.click("form1:basicTable:topActionsGroup1:addSharedTableButton");
		waitForPageLoad("Additional Properties (1)");

		selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property");
		selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
		selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
		selenium.click("form1:propertyContentPage:topButtons:newButton");
		waitForPageLoad("Custom resources are nonstandard resources");

		assertTrue(selenium.isTextPresent(resourceName));
		selectTableRowByValue("propertyForm:resourcesTable", resourceName);        
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:button3");
        sleep(500);
 		selenium.click(getLinkIdByLinkText("propertyForm:resourcesTable", resourceName));
        waitForPageLoad("Edit Custom Resource");
		assertEquals("off", selenium.getValue("form1:propertySheet:propertSectionTextField:statusProp:enabled"));
		selenium.click("form1:propertyContentPage:topButtons:cancelButton");
        waitForPageLoad("Custom resources are nonstandard resources");

		selectTableRowByValue("propertyForm:resourcesTable", resourceName);
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:button2");
        sleep(500);
		selenium.click(getLinkIdByLinkText("propertyForm:resourcesTable", resourceName));
        waitForPageLoad("Edit Custom Resource");
		assertEquals("on", selenium.getValue("form1:propertySheet:propertSectionTextField:statusProp:enabled"));
		selenium.click("form1:propertyContentPage:topButtons:cancelButton");

        waitForPageLoad("Custom resources are nonstandard resources");
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
    }
    
    @Test
    public void testExternalResource() {
        final String resourceName = generateRandomString();
        final String description = resourceName + " - description";

		openAndWait("/full/externalResources.jsf","Manage external JNDI resources when");
        
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:newButton");
        waitForPageLoad("New External Resource");
        
        selenium.type("form1:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
		selenium.select("form1:propertySheet:propertSectionTextField:cp:Classname", "label=java.lang.Double");
		selenium.type("form1:propertySheet:propertSectionTextField:jndiLookupProp:jndiLookup", resourceName);
        selenium.type("form1:propertySheet:propertSectionTextField:descProp:desc", description);
		selenium.click("form1:basicTable:topActionsGroup1:addSharedTableButton");
        waitForPageLoad("Additional Properties (1)");
        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property");
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
		selenium.click("form1:propertyContentPage:topButtons:newButton");
		waitForPageLoad("Manage external JNDI resources when");
        
		selectTableRowByValue("propertyForm:resourcesTable", resourceName);
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:button3");
        sleep(500);

		selenium.click(getLinkIdByLinkText("propertyForm:resourcesTable", resourceName));
        waitForPageLoad("Edit External Resource");
		assertEquals("off", selenium.getValue("form1:propertySheet:propertSectionTextField:statusProp:enabled"));
		selenium.click("form1:propertyContentPage:topButtons:cancelButton");
        waitForPageLoad("Manage external JNDI resources when");

		selectTableRowByValue("propertyForm:resourcesTable", resourceName);
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:button2");
        sleep(500);
		selenium.click("propertyForm:resourcesTable:rowGroup1:0:col1:link");
        waitForPageLoad("Edit External Resource");
		assertEquals("on", selenium.getValue("form1:propertySheet:propertSectionTextField:statusProp:enabled"));
		selenium.click("form1:propertyContentPage:topButtons:cancelButton");
        waitForPageLoad("Manage external JNDI resources when");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
    }
}

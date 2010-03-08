package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class JavaMailTest extends BaseSeleniumTestClass {
    @Test
    public void createMailResource() {
        final String resourceName = generateRandomString();
        final String description = resourceName + " description";
		openAndWait("/full/mailResources.jsf", "A JavaMail session resource represents");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:newButton");
        waitForPageLoad("New JavaMail Session");
        
		selenium.type("propertyForm:propertySheet:propertSectionTextField:nameNew:name", resourceName);
		selenium.type("propertyForm:propertySheet:propertSectionTextField:hostProp:host", "localhost");
		selenium.type("propertyForm:propertySheet:propertSectionTextField:userProp:user", "user");
		selenium.type("propertyForm:propertySheet:propertSectionTextField:fromProp:from", "return@test.com");
		selenium.type("propertyForm:propertySheet:propertSectionTextField:descProp:desc", description);
        selenium.click("propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
		waitForPageLoad("Additional Properties (1)");
		selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "property");
		selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
		selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", "description");
		selenium.click("propertyForm:propertyContentPage:topButtons:newButton");
		waitForPageLoad("which provides a platform-independent");
		assertTrue(selenium.isTextPresent(resourceName));

/*
		selectTableRowByValue("propertyForm:resourcesTable", resourceName);
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:button3");
		waitForPageLoad("false");
		selectTableRowByValue("propertyForm:resourcesTable", resourceName);
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:button2");
		waitForPageLoad("true");
*/

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
    }
}

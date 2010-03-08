package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 8, 2010
 * Time: 3:19:00 PM
 * To change this template use File | Settings | File Templates.
 */
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
		selenium.click("propertyForm:sun_title3458:topButtons:newButton");
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

        selenium.chooseOkOnNextConfirmation();
		selectTableRowByValue("propertyForm:resourcesTable", resourceName);
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:button1");
		selenium.getConfirmation();
		waitForPageLoad(resourceName, true);    
    }
}

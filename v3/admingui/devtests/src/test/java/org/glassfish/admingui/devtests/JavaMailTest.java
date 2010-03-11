package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class JavaMailTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_JAVA_MAIL = "A JavaMail session resource represents a mail session in the JavaMail API, which provides a platform-independent and protocol-independent framework to build mail and messaging applications.";
    private static final String TRIGGER_NEW_JAVAMAIL_SESSION = "New JavaMail Session";
    private static final String TRIGGER_EDIT_JAVAMAIL_SESSION = "Edit JavaMail Session";

    @Test
    public void createMailResource() {
        final String resourceName = generateRandomString();
        final String description = resourceName + " description";
        
        clickAndWait("treeForm:tree:resources:mailResources:mailResources_link", TRIGGER_JAVA_MAIL);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_JAVAMAIL_SESSION);

        selenium.type("propertyForm:propertySheet:propertSectionTextField:nameNew:name", resourceName);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:hostProp:host", "localhost");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:userProp:user", "user");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:fromProp:from", "return@test.com");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:descProp:desc", description);
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_JAVA_MAIL);

        assertTrue(selenium.isTextPresent(resourceName));

        testDisableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_JAVA_MAIL,
                TRIGGER_EDIT_JAVAMAIL_SESSION);
        testEnableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_JAVA_MAIL,
                TRIGGER_EDIT_JAVAMAIL_SESSION);

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
    }
}

package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JndiTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_CUSTOM_RESOURCES = "Custom resources are nonstandard resources";
    private static final String TRIGGER_NEW_CUSTOM_RESOURCE = "New Custom Resource";
    private static final String TRIGGER_EDIT_CUSTOM_RESOURCE = "Edit Custom Resource";
    private static final String TRIGGER_EDIT_EXTERNAL_RESOURCE = "Edit External Resource";
    private static final String TRIGGER_EXTERNAL_RESOURCES = "Manage external JNDI resources when";
    private static final String TRIGGER_NEW_EXTERNAL_RESOURCE = "New External Resource";

    @Test
    public void testCustomResources() {
        final String resourceName = generateRandomString();

        clickAndWait("treeForm:tree:resources:jndi:customResources:customResources_link", TRIGGER_CUSTOM_RESOURCES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_CUSTOM_RESOURCE);

        selenium.type("form1:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        selenium.select("form1:propertySheet:propertSectionTextField:cp:Classname", "label=java.lang.Double");
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:newButton", TRIGGER_CUSTOM_RESOURCES);

        assertTrue(selenium.isTextPresent(resourceName));

        testDisableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "form1:propertySheet:propertSectionTextField:statusProp:enabled",
                "form1:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CUSTOM_RESOURCES,
                TRIGGER_EDIT_CUSTOM_RESOURCE);
        testEnableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "form1:propertySheet:propertSectionTextField:statusProp:enabled",
                "form1:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CUSTOM_RESOURCES,
                TRIGGER_EDIT_CUSTOM_RESOURCE);

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
    }

    @Test
    public void testExternalResources() {
        final String resourceName = generateRandomString();
        final String description = resourceName + " - description";

        clickAndWait("treeForm:tree:resources:jndi:externalResources:externalResources_link", TRIGGER_EXTERNAL_RESOURCES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_EXTERNAL_RESOURCE);

        selenium.type("form1:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        selenium.select("form1:propertySheet:propertSectionTextField:cp:Classname", "label=java.lang.Double");
        selenium.type("form1:propertySheet:propertSectionTextField:jndiLookupProp:jndiLookup", resourceName);
        selenium.type("form1:propertySheet:propertSectionTextField:descProp:desc", description);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:newButton", TRIGGER_EXTERNAL_RESOURCES);

        testDisableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "form1:propertySheet:propertSectionTextField:statusProp:enabled",
                "form1:propertyContentPage:topButtons:cancelButton",
                TRIGGER_EXTERNAL_RESOURCES,
                TRIGGER_EDIT_EXTERNAL_RESOURCE);
        testEnableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "form1:propertySheet:propertSectionTextField:statusProp:enabled",
                "form1:propertyContentPage:topButtons:cancelButton",
                TRIGGER_EXTERNAL_RESOURCES,
                TRIGGER_EDIT_EXTERNAL_RESOURCE);

//        selectTableRowByValue("propertyForm:resourcesTable", resourceName);
//        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:button3");
//        selenium.click("propertyForm:resourcesTable:topActionsGroup1:button3");
//        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:button3");
//
//        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", resourceName), TRIGGER_EDIT_EXTERNAL_RESOURCE);
//
//        assertEquals("off", selenium.getValue("form1:propertySheet:propertSectionTextField:statusProp:enabled"));
//        clickAndWait("form1:propertyContentPage:topButtons:cancelButton", TRIGGER_EXTERNAL_RESOURCES);
//
//        selectTableRowByValue("propertyForm:resourcesTable", resourceName);
//        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:button2");
//        selenium.click("propertyForm:resourcesTable:topActionsGroup1:button2");
//        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:button2");
//
//        clickAndWait("propertyForm:resourcesTable:rowGroup1:0:col1:link", TRIGGER_EDIT_EXTERNAL_RESOURCE);
//        assertEquals("on", selenium.getValue("form1:propertySheet:propertSectionTextField:statusProp:enabled"));
//        clickAndWait("form1:propertyContentPage:topButtons:cancelButton", TRIGGER_EXTERNAL_RESOURCES);

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
    }
}

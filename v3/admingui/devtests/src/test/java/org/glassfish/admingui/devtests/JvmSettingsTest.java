package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JvmSettingsTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_JVM_GENERAL_SETTINGS = "JVM General Settings";
    private static final String TRIGGER_JVM_PATH_SETTINGS = "JVM Path Settings";
    private static final String TRIGGER_JVM_OPTIONS = "Manage JVM options for the server.";

    @Test
    public void testJvmGeneralSettings() {
        clickAndWait("treeForm:tree:configuration:jvmSettings:jvmSettings_link", TRIGGER_JVM_GENERAL_SETTINGS);
        selenium.click("propertyForm:propertySheet:propertSectionTextField:debugEnabledProp:debug");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
        waitForPageLoad("Restart Required", 1000);
    }

    @Test
    public void testJvmSettings() {
        clickAndWait("treeForm:tree:configuration:jvmSettings:jvmSettings_link", TRIGGER_JVM_GENERAL_SETTINGS);
        clickAndWait("propertyForm:javaConfigTab:jvmOptions", TRIGGER_JVM_OPTIONS);

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton", "Options");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "-Dfoo=" + generateRandomString());
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
        clickAndWait("propertyForm:javaConfigTab:pathSettings", TRIGGER_JVM_PATH_SETTINGS);
        clickAndWait("propertyForm:javaConfigTab:jvmOptions", TRIGGER_JVM_OPTIONS);

        assertTableRowCount("propertyForm:basicTable", count);
    }
}

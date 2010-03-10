package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JvmSettingsTest extends BaseSeleniumTestClass {
    @Test
    public void testJvmGeneralSettings() {
        openAndWait("/common/javaConfig/serverInstJvmGeneral.jsf?configName=server-config", "JVM General Settings");

        selenium.click("propertyForm:propertySheet:propertSectionTextField:debugEnabledProp:debug");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", "New values successfully saved.");
        assertTrue(selenium.isTextPresent("Restart Required"));
    }

    @Test
    public void testJvmSettings() {
        final String property = "-Dfoo=" + generateRandomString();
        clickAndWait("treeForm:tree:configuration:jvmSettings:jvmSettings_link", "JVM General Settings");

        clickAndWait("propertyForm:javaConfigTab:jvmOptions", "Options (");
        int count = getTableRowCount("propertyForm:basicTable:_titleBar");

        clickAndWait("propertyForm:basicTable:topActionsGroup1:addSharedTableButton", "Options (" + (count + 1) + ")");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", property);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", "New values successfully saved.");
        clickAndWait("propertyForm:javaConfigTab:pathSettings", "JVM Path Settings");
        clickAndWait("propertyForm:javaConfigTab:jvmOptions", "Options (");

        // Too fragile?
        //assertEquals(property, selenium.getValue("propertyForm:basicTable:rowGroup1:" + count + ":col3:col1St"));
        assertEquals(getTableRowCount("propertyForm:basicTable:_titleBar"), count+1);
    }
}

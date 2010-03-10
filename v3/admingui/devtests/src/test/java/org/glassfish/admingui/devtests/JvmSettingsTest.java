package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JvmSettingsTest extends BaseSeleniumTestClass {
    @Test
    public void testJvmGeneralSettings() {
		openAndWait("/common/javaConfig/serverInstJvmGeneral.jsf?configName=server-config", "JVM General Settings");
		selenium.click("propertyForm:propertySheet:propertSectionTextField:debugEnabledProp:debug");
		selenium.click("propertyForm:propertyContentPage:topButtons:saveButton");
		waitForPageLoad("New values successfully saved.");
		assertTrue(selenium.isTextPresent("Restart Required"));
    }

    @Test
    public void testJvmSettings() {
        final String property = "-Dfoo="+generateRandomString();
        selenium.click("treeForm:tree:configuration:jvmSettings:jvmSettings_link");
        waitForPageLoad("JVM General Settings");
        selenium.click("propertyForm:javaConfigTab:jvmOptions");
        waitForPageLoad("Options (");
        int count = getTableRowCount("propertyForm:basicTable:_titleBar");
        selenium.click("propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        waitForPageLoad("Options (" + (count+1) + ")");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", property);
        selenium.click("propertyForm:propertyContentPage:topButtons:saveButton");
        waitForPageLoad("New values successfully saved.");
        selenium.click("propertyForm:javaConfigTab:pathSettings");
        waitForPageLoad("JVM Path Settings");
        selenium.click("propertyForm:javaConfigTab:jvmOptions");
        waitForPageLoad("Options (");
        // Too fragile?
        assertEquals(property, selenium.getValue("propertyForm:basicTable:rowGroup1:"+count+":col3:col1St"));
    }
}

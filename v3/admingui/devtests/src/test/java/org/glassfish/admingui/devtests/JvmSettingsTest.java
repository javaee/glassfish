package org.glassfish.admingui.devtests;

import org.junit.Test;

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
}

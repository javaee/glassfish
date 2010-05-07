package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Apr 19, 2010
 * Time: 11:34:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class MonitoringTest extends BaseSeleniumTestClass {
    @Test
    public void testMonitoring() {
		selenium.click("treeForm:tree:configuration:monitor:monitor_link");
//		verifyTrue(selenium.isTextPresent("Enable monitoring for a component or service by selecting either LOW or HIGH. To use the Administration Console for monitoring, Monitoring Service and Monitoring MBeans must both be enabled."));
//		selenium.click("form1:propertySheet:propertSectionTextField:monServiceProp:sun_checkbox130");
//		selenium.click("form1:propertySheet:propertSectionTextField:monMbeansProp:sun_checkbox131");
//		selenium.select("form1:basicTable:rowGroup1:0:col3:level", "label=HIGH");
//		selenium.click("form1:title:topButtons:saveButton");
//		verifyTrue(selenium.isTextPresent("New values successfully saved."));
//		assertEquals("off", selenium.getValue("form1:propertySheet:propertSectionTextField:monServiceProp:sun_checkbox130"));
//		assertEquals("off", selenium.getValue("form1:propertySheet:propertSectionTextField:monMbeansProp:sun_checkbox131"));
//		verifyTrue(selenium.isElementPresent("form1:basicTable:rowGroup1:0:col3:level"));
    }
}

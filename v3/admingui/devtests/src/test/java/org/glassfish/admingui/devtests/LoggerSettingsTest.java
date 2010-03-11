package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LoggerSettingsTest extends BaseSeleniumTestClass {
    @Test
    public void testLoggerSettings() {
        final String rotationLimit = Integer.toString(generateRandomNumber());
        final String rotationTimeLimit = Integer.toString(generateRandomNumber());
        final String flushFrequency = Integer.toString(generateRandomNumber());

        clickAndWait("treeForm:tree:configuration:loggerSetting:loggerSetting_link", "Enterprise Server logging messages");
        selenium.click("form1:general:sheetSection:writeSystemLogEnabledProp:writeSystemLogEnabled");
        String enabled = selenium.getValue("form1:general:sheetSection:writeSystemLogEnabledProp:writeSystemLogEnabled");
        selenium.type("form1:general:sheetSection:FileRotationLimitProp:FileRotationLimit", rotationLimit);
        selenium.type("form1:general:sheetSection:FileRotationTimeLimitProp:FileRotationTimeLimit", rotationTimeLimit);
        selenium.type("form1:general:sheetSection:FlushFrequencyProp:FlushFrequency", flushFrequency);
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        clickAndWait("treeForm:tree:ct", "GlassFish News");

        clickAndWait("treeForm:tree:configuration:loggerSetting:loggerSetting_link", "Enterprise Server logging messages");
        assertEquals(enabled, selenium.getValue("form1:general:sheetSection:writeSystemLogEnabledProp:writeSystemLogEnabled"));
        assertEquals(rotationLimit, selenium.getValue("form1:general:sheetSection:FileRotationLimitProp:FileRotationLimit"));
        assertEquals(rotationTimeLimit, selenium.getValue("form1:general:sheetSection:FileRotationTimeLimitProp:FileRotationTimeLimit"));
        assertEquals(flushFrequency, selenium.getValue("form1:general:sheetSection:FlushFrequencyProp:FlushFrequency"));
    }

    @Test
    public void testLogLevels() {
        clickAndWait("treeForm:tree:configuration:loggerSetting:loggerSetting_link", "Enterprise Server logging messages");
        clickAndWait("form1:loggingTabs:loggerLevels", "Module Log Levels");
        String newLevel = "WARNING";
        if ("WARNING".equals(selenium.getValue("form1:basicTable:rowGroup1:0:col3:level"))) {
            newLevel = "INFO";
        }
        selenium.select("form1:basicTable:topActionsGroup1:change_list", "label=" + newLevel);
        selenium.click("form1:basicTable:_tableActionsTop:_selectMultipleButton");
        waitForButtonEnabled("form1:basicTable:topActionsGroup1:button1");

        selenium.click("form1:basicTable:topActionsGroup1:button1");
        waitForButtonDisabled("form1:basicTable:topActionsGroup1:button1");

        clickAndWait("form1:title:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        clickAndWait("form1:loggingTabs:loggerGeneral", "Enterprise Server logging messages");
        clickAndWait("form1:loggingTabs:loggerLevels", "Module Log Levels");
        assertEquals(newLevel, selenium.getValue("form1:basicTable:rowGroup1:0:col3:level"));
    }
}
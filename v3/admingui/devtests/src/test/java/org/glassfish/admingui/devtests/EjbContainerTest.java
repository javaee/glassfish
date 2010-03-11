package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EjbContainerTest extends BaseSeleniumTestClass {
    @Test
    public void testEjbSettings() {
        final String minSize = Integer.toString(generateRandomNumber(64));
        final String maxSize = Integer.toString(generateRandomNumber(64));
        final String poolResize = Integer.toString(generateRandomNumber(64));
        final String timeout = Integer.toString(generateRandomNumber(600));

        clickAndWait("treeForm:tree:configuration:ejbContainer:ejbContainer_link","Enterprise Java Beans (EJB)");
        
        selenium.click("form1:propertySheet:generalPropertySection:commitOptionProp:optC");
        selenium.type("form1:propertySheet:poolSettingSection:MinSizeProp:MinSize", minSize);
        selenium.type("form1:propertySheet:poolSettingSection:MaxSizeProp:MaxSize", maxSize);
        selenium.type("form1:propertySheet:poolSettingSection:PoolResizeProp:PoolResize", poolResize);
        selenium.type("form1:propertySheet:poolSettingSection:TimeoutProp:Timeout", timeout);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
        clickAndWait("form1:ejbContainerTabs:mdbSettingsTab", "MDB Default Pool Settings");
        clickAndWait("form1:ejbContainerTabs:ejbSettingsTab", "Enterprise Java Beans (EJB)");
        assertEquals("on", selenium.getValue("form1:propertySheet:generalPropertySection:commitOptionProp:optC"));
        assertEquals(minSize, selenium.getValue("form1:propertySheet:poolSettingSection:MinSizeProp:MinSize"));
        assertEquals(maxSize, selenium.getValue("form1:propertySheet:poolSettingSection:MaxSizeProp:MaxSize"));
        assertEquals(poolResize, selenium.getValue("form1:propertySheet:poolSettingSection:PoolResizeProp:PoolResize"));
        assertEquals(timeout, selenium.getValue("form1:propertySheet:poolSettingSection:TimeoutProp:Timeout"));
        assertTableRowCount("form1:basicTable", count);
    }
}

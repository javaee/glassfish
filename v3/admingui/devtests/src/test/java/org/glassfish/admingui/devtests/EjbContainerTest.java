package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EjbContainerTest extends BaseSeleniumTestClass {
    private static final String TAB_EJB_SETTINGS = "Enterprise Java Beans (EJB)";
    private static final String TAB_MDB_SETTINGS = "MDB Default Pool Settings";
    private static final String TAB_EJB_TIMER_SERVICE = "The EJB timer service enables you";

    @Test
    public void testEjbSettings() {
        final String minSize = Integer.toString(generateRandomNumber(64));
        final String maxSize = Integer.toString(generateRandomNumber(64));
        final String poolResize = Integer.toString(generateRandomNumber(64));
        final String timeout = Integer.toString(generateRandomNumber(600));

        clickAndWait("treeForm:tree:configuration:ejbContainer:ejbContainer_link", TAB_EJB_SETTINGS);
        
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
        clickAndWait("form1:ejbContainerTabs:mdbSettingsTab", TAB_MDB_SETTINGS);
        clickAndWait("form1:ejbContainerTabs:ejbSettingsTab", TAB_EJB_SETTINGS);
        assertEquals("on", selenium.getValue("form1:propertySheet:generalPropertySection:commitOptionProp:optC"));
        assertEquals(minSize, selenium.getValue("form1:propertySheet:poolSettingSection:MinSizeProp:MinSize"));
        assertEquals(maxSize, selenium.getValue("form1:propertySheet:poolSettingSection:MaxSizeProp:MaxSize"));
        assertEquals(poolResize, selenium.getValue("form1:propertySheet:poolSettingSection:PoolResizeProp:PoolResize"));
        assertEquals(timeout, selenium.getValue("form1:propertySheet:poolSettingSection:TimeoutProp:Timeout"));
        assertTableRowCount("form1:basicTable", count);
    }

    @Test
    public void testMdbSettings() {
        final String minSize = Integer.toString(generateRandomNumber(64));
        final String maxSize = Integer.toString(generateRandomNumber(64));
        final String poolResize = Integer.toString(generateRandomNumber(64));
        final String timeout = Integer.toString(generateRandomNumber(600));

        clickAndWait("treeForm:tree:configuration:ejbContainer:ejbContainer_link", TAB_EJB_SETTINGS);
        clickAndWait("form1:ejbContainerTabs:mdbSettingsTab", TAB_MDB_SETTINGS);

        selenium.type("form1:propertySheet:propertySectionTextField:MinSizeProp:MinSize", minSize);
        selenium.type("form1:propertySheet:propertySectionTextField:MaxSizeProp:MaxSize", maxSize);
        selenium.type("form1:propertySheet:propertySectionTextField:PoolResizeProp:PoolResize", poolResize);
        selenium.type("form1:propertySheet:propertySectionTextField:TimeoutProp:Timeout", timeout);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        clickAndWait("treeForm:tree:configuration:ejbContainer:ejbContainer_link", TAB_EJB_SETTINGS);
        clickAndWait("form1:ejbContainerTabs:mdbSettingsTab", TAB_MDB_SETTINGS);

        assertEquals(minSize, selenium.getValue("form1:propertySheet:propertySectionTextField:MinSizeProp:MinSize"));
        assertEquals(maxSize, selenium.getValue("form1:propertySheet:propertySectionTextField:MaxSizeProp:MaxSize"));
        assertEquals(poolResize, selenium.getValue("form1:propertySheet:propertySectionTextField:PoolResizeProp:PoolResize"));
        assertEquals(timeout, selenium.getValue("form1:propertySheet:propertySectionTextField:TimeoutProp:Timeout"));
        assertTableRowCount("form1:basicTable", count);
    }

    @Test
    public void testEjbTimerService() {
        final String minDelivery = Integer.toString(generateRandomNumber(5000));
        final String maxRedelivery = Integer.toString(generateRandomNumber(10));
        final String redeliveryInterval = Integer.toString(generateRandomNumber(20000));
        final String timerDatasource = "jndi/" + generateRandomString();

        clickAndWait("treeForm:tree:configuration:ejbContainer:ejbContainer_link", TAB_EJB_SETTINGS);
        clickAndWait("form1:ejbContainerTabs:ejbTimerTab", TAB_EJB_TIMER_SERVICE);

        selenium.type("form1:propertySheet:propertySectionTextField:MinDeliveryProp:MinDelivery", minDelivery);
        selenium.type("form1:propertySheet:propertySectionTextField:MaxRedeliveryProp:MaxRedelivery", maxRedelivery);
        selenium.type("form1:propertySheet:propertySectionTextField:RedeliveryIntrProp:RedeliveryIntr", redeliveryInterval);
        selenium.type("form1:propertySheet:propertySectionTextField:TimerDatasourceProp:TimerDatasource", timerDatasource);
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        clickAndWait("form1:ejbContainerTabs:mdbSettingsTab", TAB_MDB_SETTINGS);
        clickAndWait("form1:ejbContainerTabs:ejbTimerTab", TAB_EJB_TIMER_SERVICE);

        assertEquals(minDelivery, selenium.getValue("form1:propertySheet:propertySectionTextField:MinDeliveryProp:MinDelivery"));
        assertEquals(maxRedelivery, selenium.getValue("form1:propertySheet:propertySectionTextField:MaxRedeliveryProp:MaxRedelivery"));
        assertEquals(redeliveryInterval, selenium.getValue("form1:propertySheet:propertySectionTextField:RedeliveryIntrProp:RedeliveryIntr"));
        assertEquals(timerDatasource, selenium.getValue("form1:propertySheet:propertySectionTextField:TimerDatasourceProp:TimerDatasource"));

        // Clean up after ourselves, just because... :)
        selenium.type("form1:propertySheet:propertySectionTextField:MinDeliveryProp:MinDelivery", "1000");
        selenium.type("form1:propertySheet:propertySectionTextField:MaxRedeliveryProp:MaxRedelivery", "1");
        selenium.type("form1:propertySheet:propertySectionTextField:RedeliveryIntrProp:RedeliveryIntr", "5000");
        selenium.type("form1:propertySheet:propertySectionTextField:TimerDatasourceProp:TimerDatasource", "");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
    }
}

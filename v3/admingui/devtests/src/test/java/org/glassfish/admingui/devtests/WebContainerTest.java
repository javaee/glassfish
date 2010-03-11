package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 10, 2010
 * Time: 3:48:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebContainerTest extends BaseSeleniumTestClass {
    private static final String TAB_GENERAL_PROPERTIES = "General Properties";
    private static final String TAB_SESSION_PROPERTIES = "Maximum number of seconds";
    private static final String TAB_MANAGER_PROPERTIES = "Number of seconds until";
    private static final String TAB_STORE_PROPERTIES = "Absolute or relative pathname";

    @Test
    public void testGeneralTab() {
        final String property = "property"+generateRandomString();
        final String value = generateRandomString();
        final String description = "Description for " + property;
        
		clickAndWait("treeForm:tree:configuration:webContainer:webContainer_link", TAB_GENERAL_PROPERTIES);
        
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", property);
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", value);
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", description);
		clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

		clickAndWait("form1:webContainerTabs:sessionTab", TAB_SESSION_PROPERTIES);
		clickAndWait("form1:webContainerTabs:general", TAB_GENERAL_PROPERTIES);

        assertTableRowCount("form1:basicTable", count);
    }

    @Test
    public void testSessionProperties() {
        clickAndWait("treeForm:tree:configuration:webContainer:webContainer_link", TAB_GENERAL_PROPERTIES);
        clickAndWait("form1:webContainerTabs:sessionTab", TAB_SESSION_PROPERTIES);

        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");

        selenium.type("form1:sessionPropSheet:sessionPropSheetSection:SessionTimeoutProp:SessionTimeout", "300");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        clickAndWait("form1:webContainerTabs:general", TAB_GENERAL_PROPERTIES);

        clickAndWait("form1:webContainerTabs:sessionTab", TAB_SESSION_PROPERTIES);

        assertTableRowCount("form1:basicTable", count);
        assertEquals("300", selenium.getValue("form1:sessionPropSheet:sessionPropSheetSection:SessionTimeoutProp:SessionTimeout"));

        selenium.type("form1:sessionPropSheet:sessionPropSheetSection:SessionTimeoutProp:SessionTimeout", "300");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
    }

    @Test
    public void testManagerProperties() {
        final String reapInterval = Integer.toString(generateRandomNumber(100));
        final String maxSessions = Integer.toString(generateRandomNumber(1000));
        final String sessFileName = generateRandomString();

        clickAndWait("treeForm:tree:configuration:webContainer:webContainer_link", TAB_GENERAL_PROPERTIES);

        clickAndWait("form1:webContainerTabs:managerTab", TAB_MANAGER_PROPERTIES);

        selenium.type("form1:managerPropSheet:managerPropSheetSection:ReapIntervalProp:ReapInterval", reapInterval);
        selenium.type("form1:managerPropSheet:managerPropSheetSection:MaxSessionsProp:MaxSessions", maxSessions);
        selenium.type("form1:managerPropSheet:managerPropSheetSection:SessFileNameProp:SessFileName", sessFileName);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");
        
        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        clickAndWait("form1:webContainerTabs:sessionTab", TAB_SESSION_PROPERTIES);
        clickAndWait("form1:webContainerTabs:managerTab", TAB_MANAGER_PROPERTIES);

        assertEquals(reapInterval, selenium.getValue("form1:managerPropSheet:managerPropSheetSection:ReapIntervalProp:ReapInterval"));
        assertEquals(maxSessions, selenium.getValue("form1:managerPropSheet:managerPropSheetSection:MaxSessionsProp:MaxSessions"));
        assertEquals(sessFileName, selenium.getValue("form1:managerPropSheet:managerPropSheetSection:SessFileNameProp:SessFileName"));
        assertTableRowCount("form1:basicTable", count);
    }

    @Test
    public void testStoreProperties() {
        final String directory = generateRandomString();

        clickAndWait("treeForm:tree:configuration:webContainer:webContainer_link", TAB_GENERAL_PROPERTIES);

        clickAndWait("form1:webContainerTabs:storeTab", TAB_STORE_PROPERTIES);

        selenium.type("form1:storePropSheet:storePropSheetSection:DirectoryProp:Directory", directory);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        clickAndWait("form1:webContainerTabs:managerTab", TAB_MANAGER_PROPERTIES);
        clickAndWait("form1:webContainerTabs:storeTab", TAB_STORE_PROPERTIES);
        assertEquals(directory, selenium.getValue("form1:storePropSheet:storePropSheetSection:DirectoryProp:Directory"));
        assertTableRowCount("form1:basicTable", count);

        selenium.type("form1:storePropSheet:storePropSheetSection:DirectoryProp:Directory", "");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
    }
}
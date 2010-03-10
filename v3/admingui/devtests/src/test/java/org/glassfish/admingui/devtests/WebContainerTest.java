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
    @Test
    public void testGeneralTab() {
        final String property = "property"+generateRandomString();
        final String value = generateRandomString();
        final String description = "Description for " + property;
        
		clickAndWait("treeForm:tree:configuration:webContainer:webContainer_link", "General Properties");
        
        int count = getTableRowCount("form1:basicTable");
		clickAndWait("form1:basicTable:topActionsGroup1:addSharedTableButton", "Additional Properties (" + (count+1)+")");

        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", property);
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", value);
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", description);
		clickAndWait("form1:propertyContentPage:topButtons:saveButton", "New values successfully saved.");

		clickAndWait("form1:webContainerTabs:sessionTab", "Session Properties");
		clickAndWait("form1:webContainerTabs:general", "General Properties");

        assertTrue(selenium.isTextPresent("Additional Properties (" + (count+1)+")"));
    }

}

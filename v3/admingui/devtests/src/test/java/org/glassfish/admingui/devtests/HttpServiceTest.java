package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpServiceTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_HTTP_SERVICE = "Configure rotation of the HTTP access log files. Access logs for virtual servers are in the install_dir/domains/domain_name/logs/access directory.";

    @Test
    public void testHttpService() {
        final String interval = Integer.toString(generateRandomNumber(2880));
        final String maxFiles = Integer.toString(generateRandomNumber(50));
        final String bufferSize = Integer.toString(generateRandomNumber(65536));
        final String logWriteInterval = Integer.toString(generateRandomNumber(600));

        clickAndWait("treeForm:tree:configuration:httpService:httpService_link", TRIGGER_HTTP_SERVICE);
        selenium.check("form1:propertySheet:http:acLog:ssoEnabled");
        selenium.check("form1:propertySheet:accessLog:acLog:accessLoggingEnabled");
        selenium.type("form1:propertySheet:accessLog:intervalProp:Interval", interval);
        selenium.type("form1:propertySheet:accessLog:MaxHistoryFiles:MaxHistoryFiles", maxFiles);
        selenium.type("form1:propertySheet:accessLog:accessLogBufferSize:accessLogBufferSize", bufferSize);
        selenium.type("form1:propertySheet:accessLog:accessLogWriteInterval:accessLogWriteInterval", logWriteInterval);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
        
        clickAndWait("treeForm:tree:ct", "Please Register");
        clickAndWait("treeForm:tree:configuration:httpService:httpService_link", TRIGGER_HTTP_SERVICE);
        assertEquals("on", selenium.getValue("form1:propertySheet:http:acLog:ssoEnabled"));
        assertTrue(selenium.isTextPresent(TRIGGER_HTTP_SERVICE));
        assertEquals(interval, selenium.getValue("form1:propertySheet:accessLog:intervalProp:Interval"));
        assertEquals(maxFiles, selenium.getValue("form1:propertySheet:accessLog:MaxHistoryFiles:MaxHistoryFiles"));
        assertEquals(bufferSize, selenium.getValue("form1:propertySheet:accessLog:accessLogBufferSize:accessLogBufferSize"));
        assertEquals(logWriteInterval, selenium.getValue("form1:propertySheet:accessLog:accessLogWriteInterval:accessLogWriteInterval"));
        assertTableRowCount("form1:basicTable", count);
    }
}

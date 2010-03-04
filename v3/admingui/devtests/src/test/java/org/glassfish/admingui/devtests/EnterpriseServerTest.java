package org.glassfish.admingui.devtests;

import com.thoughtworks.selenium.SeleneseTestCase;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class EnterpriseServerTest extends BaseSeleniumTestClass {
    @Test
    public void testUntitled() throws Exception {
        selenium.open("/common/appServer/serverInstGeneralPe.jsf?instanceName=server");
        selenium.click("treeForm:tree:applicationServer:applicationServer_link");

        waitForAjaxLoad("JVM Report");

        assertTrue(selenium.isTextPresent("JVM Report"));
        assertTrue(selenium.isTextPresent("glassfishv3/glassfish/domains/domain1/config"));
    }
}


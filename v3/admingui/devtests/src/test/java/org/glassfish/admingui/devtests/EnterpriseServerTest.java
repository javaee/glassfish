package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class EnterpriseServerTest extends BaseSeleniumTestClass {
    @Test
    public void testUntitled() throws Exception {
//        openAndWait("/common/appServer/serverInstGeneralPe.jsf?instanceName=server", "General Information");
        clickAndWait("treeForm:tree:applicationServer:applicationServer_link", "JVM Report");

        assertTrue(selenium.isTextPresent("JVM Report"));
        assertTrue(selenium.isTextPresent("glassfishv3/glassfish/domains/domain1/config"));
    }
}
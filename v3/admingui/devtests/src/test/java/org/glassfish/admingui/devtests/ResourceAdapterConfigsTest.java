package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ResourceAdapterConfigsTest extends BaseSeleniumTestClass {
    @Test
    public void testResourceAdapterConfigs() throws Exception {
        openAndWait("/jca/resourceAdapterConfigs.jsf", "Resource Adapter Configs");

        try {
            // If this exists, delete it so the test below won't explode.  If it doesn't, just move along silently
            deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", "jmsra");
        } catch (AssertionError e) {

        }

        clickAndWait("propertyForm:poolTable:topActionsGroup1:newButton", "Resource Adapter Name:");
        selenium.select("propertyForm:propertySheet:propertSectionTextField:threadPoolsIdProp:threadpoolsid", "label=http-thread-pool");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", "A resource adapter config provides the configuration information for a resource adapter.");

        assertTrue(selenium.isTextPresent("jmsra"));
        clickAndWait(getLinkIdByLinkText("propertyForm:poolTable", "jmsra"), "Resource Adapter Name:");

        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", "New values successfully saved.");
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", "A resource adapter config provides");

        deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", "jmsra");
    }
}

package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ResourceAdapterConfigsTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_RESOURCE_ADAPTER_CONFIGS = "A resource adapter config provides the configuration information for a resource adapter.";
    private static final String TRIGGER_NEW_RESOURCE_ADAPTER = "New Resource Adapter Config";
    private static final String TRIGGER_EDIT_RESOURCE_ADAPTER_CONFIG = "Edit Resource Adapter Config";

    @Test
    public void testResourceAdapterConfigs() throws Exception {
        clickAndWait("treeForm:tree:resources:resourceAdapterConfigs:resourceAdapterConfigs_link", TRIGGER_RESOURCE_ADAPTER_CONFIGS);

        try {
            // If this exists, delete it so the test below won't explode.  If it doesn't, just move along silently
            deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", "jmsra");
        } catch (AssertionError e) {

        }

        clickAndWait("propertyForm:poolTable:topActionsGroup1:newButton", TRIGGER_NEW_RESOURCE_ADAPTER);
        selenium.select("propertyForm:propertySheet:propertSectionTextField:threadPoolsIdProp:threadpoolsid", "label=http-thread-pool");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_RESOURCE_ADAPTER_CONFIGS);

        assertTrue(selenium.isTextPresent("jmsra"));
        clickAndWait(getLinkIdByLinkText("propertyForm:poolTable", "jmsra"), TRIGGER_EDIT_RESOURCE_ADAPTER_CONFIG);

        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_RESOURCE_ADAPTER_CONFIGS);

        deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", "jmsra");
    }
}

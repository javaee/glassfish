package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 12, 2010
 * Time: 2:38:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectorServiceTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_CONNECTOR_SERVICE = "The attributes specified apply to all resource adapters deployed in this Enterprise Server.";

    @Test
    public void testConnectorService() {
        clickAndWait("treeForm:tree:configuration:connectorService:connectorService_link", TRIGGER_CONNECTOR_SERVICE);

        String policy = "derived";
        if (selenium.getValue("propertyForm:propertySheet:propertSectionTextField:ClassLoadingPolicy:ClassLoadingPolicy").equals(policy)) {
            policy = "global";
        }
        final String timeout = "60";

        selenium.type("propertyForm:propertySheet:propertSectionTextField:timeout:tiimeout", timeout);
        selenium.select("propertyForm:propertySheet:propertSectionTextField:ClassLoadingPolicy:ClassLoadingPolicy", "label="+policy);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
        clickAndWait("treeForm:tree:ct", "Please Register");
        clickAndWait("treeForm:tree:configuration:connectorService:connectorService_link", TRIGGER_CONNECTOR_SERVICE);
        assertEquals(timeout, selenium.getValue("propertyForm:propertySheet:propertSectionTextField:timeout:tiimeout"));
        assertEquals(policy, selenium.getValue("propertyForm:propertySheet:propertSectionTextField:ClassLoadingPolicy:ClassLoadingPolicy"));

    }
}

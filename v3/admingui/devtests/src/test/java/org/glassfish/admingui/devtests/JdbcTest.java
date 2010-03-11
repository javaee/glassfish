package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JdbcTest extends BaseSeleniumTestClass {

    @Test
    public void testPoolPing() {
//        openAndWait("/common/commonTask.jsf", "Common Tasks");
        clickAndWait("treeForm:tree:resources:JDBC:connectionPoolResources:amxppdomainresourcestypejdbc-connection-poolname__TimerPool:link", "Edit JDBC Connection Pool");
        clickAndWait("propertyForm:propertyContentPage:ping", "Ping Succeeded");
    }

    @Test
    public void testCreatingConnectionPool() {
        final String poolName = generateRandomString();
        final String description = "devtest test connection pool - " + poolName;

        openAndWait("/jdbc/jdbcConnectionPools.jsf", "JDBC Connection Pools");
        clickAndWait("propertyForm:poolTable:topActionsGroup1:newButton", "New JDBC Connection Pool (Step 1 of 2)");

        selenium.type("propertyForm:propertyContentPage:propertySheet:generalPropertySheet:jndiProp:name", poolName);
        selenium.select("propertyForm:propertyContentPage:propertySheet:generalPropertySheet:resTypeProp:resType", "label=javax.sql.DataSource");
        selenium.select("propertyForm:propertyContentPage:propertySheet:generalPropertySheet:dbProp:db", "label=Derby");
        clickAndWait("propertyForm:propertyContentPage:topButtons:nextButton", "New JDBC Connection Pool (Step 2 of 2)");

        selenium.type("form2:sheet:generalSheet:descProp:desc", description);
        clickAndWait("form2:propertyContentPage:topButtons:finishButton", "To store, organize, and retrieve data, most applications use relational databases.");
        assertTrue(selenium.isTextPresent(poolName) && selenium.isTextPresent(description));

        deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", poolName);
    }

    @Test
    public void testJdbcResources() {
        final String jndiName = generateRandomString();
        final String description = "devtest test jdbc resource - " + jndiName;

        openAndWait("/jdbc/jdbcResources.jsf", "JDBC Resources");
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", "New JDBC Resource");

        selenium.type("propertyForm:propertySheet:propertSectionTextField:nameNew:name", jndiName);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:descProp:desc", description);
        clickAndWait("propertyForm:basicTable:topActionsGroup1:addSharedTableButton", "Additional Properties (1)");

        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "testProp");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "testValue");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", "test description");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", "JDBC resources provide applications");

        assertTrue(selenium.isTextPresent(jndiName));
        assertTrue(selenium.isTextPresent(description));

        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", jndiName), "Edit JDBC Resource");

        assertEquals("testProp", selenium.getValue("propertyForm:basicTable:rowGroup1:0:col2:col1St"));
        assertEquals("testValue", selenium.getValue("propertyForm:basicTable:rowGroup1:0:col3:col1St"));
        assertEquals("test description", selenium.getValue("propertyForm:basicTable:rowGroup1:0:col4:col1St"));

        selenium.click("propertyForm:propertySheet:propertSectionTextField:statusProp:enabled");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", "JDBC resources provide applications with a means to connect to a database.");
        assertTrue(selenium.isTextPresent("false"));

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", jndiName);
    }
}
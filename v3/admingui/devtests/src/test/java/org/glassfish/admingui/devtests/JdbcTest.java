package org.glassfish.admingui.devtests;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 3, 2010
 * Time: 11:09:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class JdbcTest extends BaseSeleniumTestClass {

    @Test
    public void testPoolPing() {
        openAndWait("/common/commonTask.jsf", "Common Tasks");
        selenium.click("treeForm:tree:resources:JDBC:connectionPoolResources:amxppdomainresourcestypejdbc-connection-poolname__TimerPool:link");

        waitForPageLoad("Edit JDBC Connection Pool");
        selenium.click("propertyForm:propertyContentPage:ping");
        waitForPageLoad("Ping Succeeded");
    }

    @Test
    public void testCreatingConnectionPool() {
        final String poolName = generateRandomString();
        final String description = "devtest test connection pool - " + poolName;

        openAndWait("/jdbc/jdbcConnectionPools.jsf", "JDBC Connection Pools");
        selenium.click("propertyForm:poolTable:topActionsGroup1:newButton");
        waitForPageLoad("New JDBC Connection Pool (Step 1 of 2)");

        selenium.type("propertyForm:propertyContentPage:propertySheet:generalPropertySheet:jndiProp:name", poolName);
        selenium.select("propertyForm:propertyContentPage:propertySheet:generalPropertySheet:resTypeProp:resType", "label=javax.sql.DataSource");
        selenium.select("propertyForm:propertyContentPage:propertySheet:generalPropertySheet:dbProp:db", "label=Derby");
        selenium.click("propertyForm:propertyContentPage:topButtons:nextButton");
        waitForPageLoad("New JDBC Connection Pool (Step 2 of 2)");

        selenium.type("form2:sheet:generalSheet:descProp:desc", description);
        selenium.click("form2:propertyContentPage:topButtons:finishButton");
        waitForPageLoad("To store, organize, and retrieve data, most applications use relational databases.");
        assertTrue(selenium.isTextPresent(poolName) && selenium.isTextPresent(description));

        selenium.chooseOkOnNextConfirmation();
        selectTableRowByValue("propertyForm:poolTable", poolName);
        selenium.click("propertyForm:poolTable:topActionsGroup1:button1");
        waitForPageLoad(poolName, true);
        selenium.getConfirmation();
        assertFalse(selenium.isTextPresent(poolName) && selenium.isTextPresent(description));
    }
    
    @Test
    public void testJdbcResources() {
        final String jndiName = generateRandomString();
        final String description = "devtest test jdbc resource - " + jndiName;

		openAndWait("/jdbc/jdbcResources.jsf", "JDBC Resources");
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:newButton");
        waitForPageLoad("New JDBC Resource");

		selenium.type("propertyForm:propertySheet:propertSectionTextField:nameNew:name", jndiName);
		selenium.type("propertyForm:propertySheet:propertSectionTextField:descProp:desc", description);
		selenium.click("propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        waitForPageLoad("Additional Properties (1)");

		selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "testProp");
		selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "testValue");
		selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", "test description");
		selenium.click("propertyForm:propertyContentPage:topButtons:newButton");
        waitForPageLoad("JDBC resources provide applications");

        assertTrue(selenium.isTextPresent(jndiName));
		assertTrue(selenium.isTextPresent(description));

		selenium.click(getLinkIdByLinkText("propertyForm:resourcesTable", jndiName));
        waitForPageLoad("Edit JDBC Resource");

        assertEquals("testProp", selenium.getValue("propertyForm:basicTable:rowGroup1:0:col2:col1St"));
		assertEquals("testValue", selenium.getValue("propertyForm:basicTable:rowGroup1:0:col3:col1St"));
		assertEquals("test description", selenium.getValue("propertyForm:basicTable:rowGroup1:0:col4:col1St"));

		selenium.click("propertyForm:propertySheet:propertSectionTextField:statusProp:enabled");
		selenium.click("propertyForm:propertyContentPage:topButtons:saveButton");
		waitForPageLoad("New values successfully saved.");

		selenium.click("propertyForm:propertyContentPage:topButtons:cancelButton");
        waitForPageLoad("JDBC resources provide applications with a means to connect to a database.");
		assertTrue(selenium.isTextPresent("false"));

        selenium.chooseOkOnNextConfirmation();
		selectTableRowByValue("propertyForm:resourcesTable", jndiName);
		selenium.click("propertyForm:resourcesTable:topActionsGroup1:button1");
        selenium.getConfirmation();
		waitForPageLoad(jndiName, true);
    }
}
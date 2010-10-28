/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.admingui.devtests;


import org.junit.Ignore;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author anilam
 */
public class StandaloneTest  extends BaseSeleniumTestClass {
    public static final String TRIGGER_INSTANCES_PAGE = "Create and manage standalone instances.";
    public static final String TRIGGER_NEW_PAGE = "Configuration:";
    public static final String TRIGGER_GENERAL_INFO_PAGE = "General Information";
    public static final String TRIGGER_SYS_PROPS = "Configuration System Properties";

    @BeforeClass
    public static void beforeClass() {
        (new StandaloneTest()).deleteAllStandaloneInstances();
    }

    @Test
    public void testCreateAndDeleteStandaloneInstance() {
        String instanceName = "standAlone" + generateRandomString();
        createStandAloneInstance(instanceName);
        
        String prefix = getTableRowByValue("propertyForm:instancesTable", instanceName, "col1");
        assertTrue(selenium.isTextPresent(instanceName));
        assertEquals(instanceName, selenium.getText(prefix + "col1:link"));
        assertEquals(instanceName+"-config", selenium.getText(prefix + "col3:configlink"));
        assertEquals("localhost", selenium.getText(prefix + "col5:nodeAgentlink"));
        assertEquals("Stopped", selenium.getText(prefix + "col6"));
        assertEquals("100", selenium.getValue(prefix + "col2:weight"));

        startInstance(instanceName);
        assertEquals("Running", selenium.getText(prefix + "col6"));

        stopInstance(instanceName);
        assertEquals("Stopped", selenium.getText(prefix + "col6"));

        deleteStandAloneInstance(instanceName);
    }

    @Test
    public void testProperties() {
        String instanceName = "standAlone" + generateRandomString();
        createStandAloneInstance(instanceName);

        clickAndWait(getLinkIdByLinkText("propertyForm:instancesTable", instanceName), TRIGGER_GENERAL_INFO_PAGE);
        clickAndWait("propertyForm:standaloneInstanceTabs:standaloneProp", TRIGGER_SYS_PROPS);
        int sysPropCount = addTableRow("propertyForm:sysPropsTable", "propertyForm:sysPropsTable:topActionsGroup1:addSharedTableButton");
        selenium.type("propertyForm:sysPropsTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("propertyForm:sysPropsTable:rowGroup1:0:overrideValCol:overrideVal", "value");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        // Go to instance props page
        selenium.click("propertyForm:standaloneInstanceTabs:standaloneProp:instanceProps");
        waitForPageLoad(TRIGGER_SYS_PROPS, TIMEOUT, true);

        int instancePropCount = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        // Verify that properties were persisted
        clickAndWait("propertyForm:standaloneInstanceTabs:standaloneProp:configProps", TRIGGER_SYS_PROPS);
        assertTableRowCount("propertyForm:sysPropsTable", sysPropCount);
        selenium.click("propertyForm:standaloneInstanceTabs:standaloneProp:instanceProps");
        waitForPageLoad(TRIGGER_SYS_PROPS, TIMEOUT, true);
        assertTableRowCount("propertyForm:basicTable", instancePropCount);

        deleteStandAloneInstance(instanceName);
    }

    @Test
    public void testStandaloneInstanceResourcesPage() {
        final String jndiName = "jdbcResource"+generateRandomString();
        String target = "standAlone" + generateRandomString();
        final String description = "devtest test for standalone instance->resources page- " + jndiName;
        final String tableID = "propertyForm:resourcesTable";

        JdbcTest jdbcTest = new JdbcTest();
        jdbcTest.createJDBCResource(jndiName, description, target, MonitoringTest.TARGET_STANDALONE_TYPE);

        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", TRIGGER_INSTANCES_PAGE);
        clickAndWait(getLinkIdByLinkText("propertyForm:instancesTable", target), TRIGGER_GENERAL_INFO_PAGE);
        clickAndWait("propertyForm:standaloneInstanceTabs:resources", EnterpriseServerTest.TRIGGER_RESOURCES);
        assertTrue(selenium.isTextPresent(jndiName));

        int jdbcCount = getTableRowCountByValue(tableID, "JDBC Resources", "col3:type");
        int customCount = getTableRowCountByValue(tableID, "Custom Resources", "col3:type");

        EnterpriseServerTest adminServerTest = new EnterpriseServerTest();
        selenium.select("propertyForm:resourcesTable:topActionsGroup1:filter_list", "label=Custom Resources");
        adminServerTest.waitForTableRowCount(tableID, customCount);

        selenium.select("propertyForm:resourcesTable:topActionsGroup1:filter_list", "label=JDBC Resources");
        adminServerTest.waitForTableRowCount(tableID, jdbcCount);

        selectTableRowByValue("propertyForm:resourcesTable", jndiName);
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:button1");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:button1");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:button1");

        /*selenium.select("propertyForm:resourcesTable:topActionsGroup1:actions", "label=JDBC Resources");
        waitForPageLoad(JdbcTest.TRIGGER_NEW_JDBC_RESOURCE, true);
        clickAndWait("form:propertyContentPage:topButtons:cancelButton", JdbcTest.TRIGGER_JDBC_RESOURCES);*/

        jdbcTest.deleteJDBCResource(jndiName, target, MonitoringTest.TARGET_STANDALONE_TYPE);
    }

    public void createStandAloneInstance(String instanceName){
        gotoStandaloneInstancesPage();
        clickAndWait("propertyForm:instancesTable:topActionsGroup1:newButton", TRIGGER_NEW_PAGE );
        selenium.type("propertyForm:propertySheet:propertSectionTextField:NameTextProp:NameText", instanceName);
        selenium.select("propertyForm:propertySheet:propertSectionTextField:node:node", "label=localhost");
        selenium.select("propertyForm:propertySheet:propertSectionTextField:configProp:Config", "label=default-config");
        selenium.check("propertyForm:propertySheet:propertSectionTextField:configOptionProp:optC");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_INSTANCES_PAGE);
    }

    public void deleteStandAloneInstance(String instanceName) {
        gotoStandaloneInstancesPage();
        stopInstance(instanceName); // Just in case it's running. If it's not, the GUI will report an error, but that's OK
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
    }

    public void deleteAllStandaloneInstances() {
        gotoStandaloneInstancesPage();
        
        if (selenium.isTextPresent("Server Instances (0)")) {
            return;
        }

        // Stop all instances
        if (selectTableRowsByValue("propertyForm:instancesTable", "Running", "col0", "col6") > 0) {
            waitForButtonEnabled("propertyForm:instancesTable:topActionsGroup1:button3");
            selenium.chooseOkOnNextConfirmation();
            selenium.click("propertyForm:instancesTable:topActionsGroup1:button3");
            if (selenium.isConfirmationPresent()) {
                selenium.getConfirmation();
            }
            this.waitForButtonDisabled("propertyForm:instancesTable:topActionsGroup1:button3");
        }

        // Delete all instances
        deleteAllTableRows("propertyForm:instancesTable");
    }

    public void startInstance(String instanceName) {
        rowActionWithConfirm("propertyForm:instancesTable:topActionsGroup1:button2", "propertyForm:instancesTable", instanceName);
        waitForCondition("document.getElementById('propertyForm:instancesTable:topActionsGroup1:button2').value != 'Processing...'", 300000);
    }

    public void stopInstance(String instanceName) {
        String rowId = getTableRowByValue("propertyForm:instancesTable", instanceName, "col1");
        String status = selenium.getText(rowId+"col6");
        if (!"Stopped".equals(status)) {
            rowActionWithConfirm("propertyForm:instancesTable:topActionsGroup1:button3", "propertyForm:instancesTable", instanceName);
            waitForCondition("document.getElementById('propertyForm:instancesTable:topActionsGroup1:button3').value != 'Processing...'", 300000);
        }
    }

    public void gotoStandaloneInstancesPage() {
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", TRIGGER_INSTANCES_PAGE);
    }

}
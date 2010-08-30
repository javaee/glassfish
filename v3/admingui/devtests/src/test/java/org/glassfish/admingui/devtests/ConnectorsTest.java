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

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ConnectorsTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_CONNECTOR_CONNECTION_POOLS = "Click New to create a new connector connection pool.";
    private static final String TRIGGER_NEW_CONNECTOR_CONNECTION_POOL_STEP_1 = "New Connector Connection Pool (Step 1 of 2)";
    private static final String TRIGGER_NEW_CONNECTOR_CONNECTION_POOL_STEP_2 = "New Connector Connection Pool (Step 2 of 2)";
    private static final String TRIGGER_CONNECTOR_RESOURCE = "A connector resource is a program object";
    private static final String TRIGGER_NEW_CONNECTOR_RESOURCE = "New Connector Resource";
    private static final String TRIGGER_EDIT_CONNECTOR_RESOURCE = "Edit Connector Resource";

    @Test
    public void testConnectorResources() {
        String testPool = generateRandomString();
        String testConnector = generateRandomString();

        clickAndWait("treeForm:tree:resources:Connectors:connectorConnectionPools:connectorConnectionPools_link", TRIGGER_CONNECTOR_CONNECTION_POOLS);

        // Create new connection connection pool
        clickAndWait("propertyForm:poolTable:topActionsGroup1:newButton", TRIGGER_NEW_CONNECTOR_CONNECTION_POOL_STEP_1);

        selenium.type("propertyForm:propertySheet:generalPropertySheet:jndiProp:name", testPool);
        selenium.select("propertyForm:propertySheet:generalPropertySheet:resAdapterProp:db", "label=jmsra");
        waitForCondition("document.getElementById('propertyForm:propertySheet:generalPropertySheet:connectionDefProp:db').value != ''", 10000);

        selenium.select("propertyForm:propertySheet:generalPropertySheet:connectionDefProp:db", "label=javax.jms.QueueConnectionFactory");
        waitForButtonEnabled("propertyForm:title:topButtons:nextButton");

        clickAndWait("propertyForm:title:topButtons:nextButton", TRIGGER_NEW_CONNECTOR_CONNECTION_POOL_STEP_2);

        selenium.select("propertyForm:propertySheet:poolPropertySheet:transprop:trans", "label=NoTransaction");
        clickAndWait("propertyForm:propertyContentPage:topButtons:finishButton", TRIGGER_CONNECTOR_CONNECTION_POOLS);
        assertTrue(selenium.isTextPresent(testPool));

        // Create new connector resource which uses this new pool
        clickAndWait("treeForm:tree:resources:Connectors:connectorResources:connectorResources_link", TRIGGER_CONNECTOR_RESOURCE);

        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_CONNECTOR_RESOURCE);

        selenium.type("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", testConnector);
        selenium.select("form:propertySheet:propertSectionTextField:poolNameProp:PoolName", "label=" + testPool);

        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_CONNECTOR_RESOURCE);

        // Disable resource
        testDisableButton(testConnector,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CONNECTOR_RESOURCE,
                TRIGGER_EDIT_CONNECTOR_RESOURCE,
                "off");

        // Enable resource
        testEnableButton(testConnector,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CONNECTOR_RESOURCE,
                TRIGGER_EDIT_CONNECTOR_RESOURCE,
                "on");

        // Delete connector resource
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", testConnector);

        // Delete connector connection pool
        clickAndWait("treeForm:tree:resources:Connectors:connectorConnectionPools:connectorConnectionPools_link", TRIGGER_CONNECTOR_CONNECTION_POOLS);

        deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", testPool);
    }

    @Test
    public void testConnectorResourcesWithTargets() {
        String testPool = generateRandomString();
        String testConnector = generateRandomString();
        final String instanceName = generateRandomString();
        final String enableStatus = "Enabled on All Targets";
        final String disableStatus = "Disabled on All Targets";

        clickAndWait("treeForm:tree:resources:Connectors:connectorConnectionPools:connectorConnectionPools_link", TRIGGER_CONNECTOR_CONNECTION_POOLS);

        // Create new connection connection pool
        clickAndWait("propertyForm:poolTable:topActionsGroup1:newButton", TRIGGER_NEW_CONNECTOR_CONNECTION_POOL_STEP_1);

        selenium.type("propertyForm:propertySheet:generalPropertySheet:jndiProp:name", testPool);
        selenium.select("propertyForm:propertySheet:generalPropertySheet:resAdapterProp:db", "label=jmsra");
        waitForCondition("document.getElementById('propertyForm:propertySheet:generalPropertySheet:connectionDefProp:db').value != ''", 10000);

        selenium.select("propertyForm:propertySheet:generalPropertySheet:connectionDefProp:db", "label=javax.jms.QueueConnectionFactory");
        waitForButtonEnabled("propertyForm:title:topButtons:nextButton");

        clickAndWait("propertyForm:title:topButtons:nextButton", TRIGGER_NEW_CONNECTOR_CONNECTION_POOL_STEP_2);

        selenium.select("propertyForm:propertySheet:poolPropertySheet:transprop:trans", "label=NoTransaction");
        clickAndWait("propertyForm:propertyContentPage:topButtons:finishButton", TRIGGER_CONNECTOR_CONNECTION_POOLS);
        assertTrue(selenium.isTextPresent(testPool));

        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);

        // Create new connector resource which uses this new pool
        clickAndWait("treeForm:tree:resources:Connectors:connectorResources:connectorResources_link", TRIGGER_CONNECTOR_RESOURCE);

        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_CONNECTOR_RESOURCE);

        selenium.type("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", testConnector);
        selenium.select("form:propertySheet:propertSectionTextField:poolNameProp:PoolName", "label=" + testPool);

        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("form:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        selenium.type("form:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form:basicTable:rowGroup1:0:col4:col1St", "description");

        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", "label=" + instanceName);
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");

        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_CONNECTOR_RESOURCE);

        assertTrue(selenium.isTextPresent(testConnector));

        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", testConnector), TRIGGER_EDIT_CONNECTOR_RESOURCE);

        assertTableRowCount("propertyForm:basicTable", count);
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_CONNECTOR_RESOURCE);

        // Disable resource
        testDisableButton(testConnector,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CONNECTOR_RESOURCE,
                TRIGGER_EDIT_CONNECTOR_RESOURCE,
                disableStatus);
        // Enable resource
        testEnableButton(testConnector,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CONNECTOR_RESOURCE,
                TRIGGER_EDIT_CONNECTOR_RESOURCE,
                enableStatus);

        manageTargets(instanceName, testConnector);

        // Delete connector resource
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", testConnector);

        // Delete connector connection pool
        clickAndWait("treeForm:tree:resources:Connectors:connectorConnectionPools:connectorConnectionPools_link", TRIGGER_CONNECTOR_CONNECTION_POOLS);
        deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", testPool);

        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", instanceTest.TRIGGER_INSTANCES_PAGE);
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
        assertFalse(selenium.isTextPresent(instanceName));
    }

    private void manageTargets(String instanceName, String jndiName) {
        final String TRIGGER_EDIT_RESOURCE_TARGETS = "Resource Targets";
        final String enableStatus = "Enabled on All Targets";
        final String disableStatus = "Disabled on All Targets";
        final String TRIGGER_MANAGE_TARGETS = "Manage Targets";
        final String TRIGGGER_VALUES_SAVED = "New values successfully saved.";

        clickAndWait("treeForm:tree:resources:Connectors:connectorResources:connectorResources_link", TRIGGER_CONNECTOR_RESOURCE);
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", jndiName), TRIGGER_EDIT_CONNECTOR_RESOURCE);
        //Click on the target tab and verify whether the target is in the target table or not.
        clickAndWait("propertyForm:resEditTabs:targetTab", TRIGGER_EDIT_RESOURCE_TARGETS);
        assertTrue(selenium.isTextPresent(instanceName));

        //Enable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                "propertyForm:targetTable:topActionsGroup1:button2",
                "propertyForm:resEditTabs:general",
                "propertyForm:resEditTabs:targetTab",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                TRIGGER_EDIT_CONNECTOR_RESOURCE,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                enableStatus);

        //Disable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                "propertyForm:targetTable:topActionsGroup1:button3",
                "propertyForm:resEditTabs:general",
                "propertyForm:resEditTabs:targetTab",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                TRIGGER_EDIT_CONNECTOR_RESOURCE,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                disableStatus);

        //Test the manage targets
        clickAndWait("propertyForm:targetTable:topActionsGroup1:manageTargetButton", TRIGGER_MANAGE_TARGETS);
        //Remove the created instance from the selected targets.
        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_selected", "label=" + instanceName);
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        clickAndWait("form:propertyContentPage:topButtons:saveButton", TRIGGGER_VALUES_SAVED);
        assertFalse(selenium.isTextPresent(jndiName));
        //Go Back to Resources Page
        clickAndWait("treeForm:tree:resources:Connectors:connectorResources:connectorResources_link", TRIGGER_CONNECTOR_RESOURCE);
    }
}

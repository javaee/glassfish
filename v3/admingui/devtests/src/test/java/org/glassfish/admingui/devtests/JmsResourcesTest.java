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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class JmsResourcesTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_JMS_CONNECTION_FACTORIES = "Java Message Service (JMS) connection factories";
    private static final String TRIGGER_NEW_JMS_CONN_FACT = "New JMS Connection Factory";
    private static final String TRIGGER_EDIT_JMS_CONN_FACT = "Edit JMS Connection Factory";
    private static final String TRIGGER_JMS_DESTINATION_RESOURCES = "Click New to create a new destination resource. Click the name of a destination resource to modify its properties.";
    private static final String TRIGGER_NEW_JMS_DEST_RES = "New JMS Destination Resource";
    private static final String TRIGGER_EDIT_JMS_DEST_RES = "Edit JMS Destination Resource";

    private static final String TRIGGER_EDIT_RESOURCE_TARGETS = "Resource Targets";
    private static final String ENABLE_STATUS = "Enabled on All Targets";
    private static final String DISABLE_STATUS = "Disabled on All Targets";
    private static final String TRIGGER_MANAGE_TARGETS = "Manage Targets";
    private static final String TRIGGGER_VALUES_SAVED = "New values successfully saved.";

    @Test
    public void testAddingConnectionFactories() throws Exception {
        final String poolName = generateRandomString();
        final String description = "Test Pool - " + poolName;

        clickAndWait("treeForm:tree:resources:jmsResources:jmsConnectionFactories:jmsConnectionFactories_link", TRIGGER_JMS_CONNECTION_FACTORIES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_JMS_CONN_FACT);

        selenium.type("form:propertySheet:generalPropertySheet:jndiProp:jndiProp", poolName);
        selenium.select("form:propertySheet:generalPropertySheet:resType:resType", "label=javax.jms.TopicConnectionFactory");
        selenium.type("form:propertySheet:generalPropertySheet:descProp:descProp", description);
        selenium.select("form:propertySheet:poolPropertySheet:transprop:trans", "label=LocalTransaction");
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_JMS_CONNECTION_FACTORIES);
        assertTrue(selenium.isTextPresent(poolName));

        // This can't currently use testDisableButton/testEnableButton because the table is different from the others
        // The table should be fixed to be like the others (in terms of IDs) so the standard test API can be used here.
        selectTableRowByValue("propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");

        selectTableRowByValue("propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:deleteConnButton", "propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
    }

    @Test
    public void testAddingConnectionFactoriesWithTargets() throws Exception {
        final String poolName = generateRandomString();
        final String description = "Test Pool - " + poolName;
        final String instanceName = generateRandomString();
        
        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);

        clickAndWait("treeForm:tree:resources:jmsResources:jmsConnectionFactories:jmsConnectionFactories_link", TRIGGER_JMS_CONNECTION_FACTORIES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_JMS_CONN_FACT);

        selenium.type("form:propertySheet:generalPropertySheet:jndiProp:jndiProp", poolName);
        selenium.select("form:propertySheet:generalPropertySheet:resType:resType", "label=javax.jms.TopicConnectionFactory");
        selenium.type("form:propertySheet:generalPropertySheet:descProp:descProp", description);
        selenium.select("form:propertySheet:poolPropertySheet:transprop:trans", "label=LocalTransaction");
        
        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", "label=" + instanceName);
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");
        
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_JMS_CONNECTION_FACTORIES);
        assertTrue(selenium.isTextPresent(poolName));

        // This can't currently use testDisableButton/testEnableButton because the table is different from the others
        // The table should be fixed to be like the others (in terms of IDs) so the standard test API can be used here.
        selectTableRowByValue("propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");
     
        selectTableRowByValue("propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");

        manageJmsConnectionTargets(instanceName, poolName);
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:deleteConnButton", "propertyForm:resourcesTable", poolName, "colSelect", "colPoolName");
        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", instanceTest.TRIGGER_INSTANCES_PAGE);
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
        assertFalse(selenium.isTextPresent(instanceName));
    }

    private void manageJmsConnectionTargets(String instanceName, String jndiName) {

        clickAndWait("treeForm:tree:resources:jmsResources:jmsConnectionFactories:jmsConnectionFactories_link", TRIGGER_JMS_CONNECTION_FACTORIES);
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", jndiName), TRIGGER_EDIT_JMS_CONN_FACT);
        //Click on the target tab and verify whether the target is in the target table or not.
        clickAndWait("propertyForm:resEditTabs:targetTab", TRIGGER_EDIT_RESOURCE_TARGETS);
        assertTrue(selenium.isTextPresent(instanceName));

        //Enable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                "propertyForm:targetTable:topActionsGroup1:button2",
                "propertyForm:resEditTabs:general",
                "propertyForm:resEditTabs:targetTab",
                "propertyForm:propertyContentPage:propertySheet:generalPropertySheet:statusProp2:enabledStr",
                TRIGGER_EDIT_JMS_CONN_FACT,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                ENABLE_STATUS);

        //Disable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                "propertyForm:targetTable:topActionsGroup1:button3",
                "propertyForm:resEditTabs:general",
                "propertyForm:resEditTabs:targetTab",
                "propertyForm:propertyContentPage:propertySheet:generalPropertySheet:statusProp2:enabledStr",
                TRIGGER_EDIT_JMS_CONN_FACT,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                DISABLE_STATUS);
        //Test the manage targets
        clickAndWait("propertyForm:targetTable:topActionsGroup1:manageTargetButton", TRIGGER_MANAGE_TARGETS);
        //Remove the created instance from the selected targets.
        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_selected", "label=" + instanceName);
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        clickAndWait("form:propertyContentPage:topButtons:saveButton", TRIGGGER_VALUES_SAVED);
        assertFalse(selenium.isTextPresent(instanceName));
        //Go Back to Resources Page
        clickAndWait("treeForm:tree:resources:jmsResources:jmsConnectionFactories:jmsConnectionFactories_link", TRIGGER_JMS_CONNECTION_FACTORIES);
    }

    @Test
    public void testAddingDestinationResources() throws Exception {
        final String resourceName = generateRandomString();
        final String description = "Test Destination - " + resourceName;

        clickAndWait("treeForm:tree:resources:jmsResources:jmsDestinationResources:jmsDestinationResources_link", TRIGGER_JMS_DESTINATION_RESOURCES);
        sleep(1000);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_JMS_DEST_RES);
        selenium.type("form:propertyContentPage:propertySheet:propertSectionTextField:jndiProp:jndi", resourceName);
        selenium.type("form:propertyContentPage:propertySheet:propertSectionTextField:nameProp:name", "somePhysicalDestination");
        selenium.type("form:propertyContentPage:propertySheet:propertSectionTextField:descProp:desc", description);
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_JMS_DESTINATION_RESOURCES);
        assertTrue(selenium.isTextPresent(resourceName) && selenium.isTextPresent(description));

        // This can't currently use testDisableButton/testEnableButton because the table is different from the others
        // The table should be fixed to be like the others (in terms of IDs) so the standard test API can be used here.
        selectTableRowByValue("propertyForm:resourcesTable", resourceName, "colSelect", "colName");
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");

        selectTableRowByValue("propertyForm:resourcesTable", resourceName, "colSelect", "colName");
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:deleteDestButton", "propertyForm:resourcesTable", resourceName, "colSelect", "colName");
    }

    @Test
    public void testAddingDestinationResourcesWithTargets() throws Exception {
        final String resourceName = generateRandomString();
        final String instanceName = generateRandomString();
        final String description = "Test Destination - " + resourceName;

        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);

        clickAndWait("treeForm:tree:resources:jmsResources:jmsDestinationResources:jmsDestinationResources_link", TRIGGER_JMS_DESTINATION_RESOURCES);
        sleep(1000);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_JMS_DEST_RES);
        selenium.type("form:propertyContentPage:propertySheet:propertSectionTextField:jndiProp:jndi", resourceName);
        selenium.type("form:propertyContentPage:propertySheet:propertSectionTextField:nameProp:name", "somePhysicalDestination");
        selenium.type("form:propertyContentPage:propertySheet:propertSectionTextField:descProp:desc", description);

        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", "label=" + instanceName);
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");

        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_JMS_DESTINATION_RESOURCES);
        assertTrue(selenium.isTextPresent(resourceName) && selenium.isTextPresent(description));

        // This can't currently use testDisableButton/testEnableButton because the table is different from the others
        // The table should be fixed to be like the others (in terms of IDs) so the standard test API can be used here.
        selectTableRowByValue("propertyForm:resourcesTable", resourceName, "colSelect", "colName");
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:disableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:disableButton");

        selectTableRowByValue("propertyForm:resourcesTable", resourceName, "colSelect", "colName");
        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        selenium.click("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:enableButton");

        manageJmsDestinationTargets(instanceName, resourceName);
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:deleteDestButton", "propertyForm:resourcesTable", resourceName, "colSelect", "colName");
        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", instanceTest.TRIGGER_INSTANCES_PAGE);
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
        assertFalse(selenium.isTextPresent(instanceName));
    }

    private void manageJmsDestinationTargets(String instanceName, String jndiName) {

        clickAndWait("treeForm:tree:resources:jmsResources:jmsDestinationResources:jmsDestinationResources_link", TRIGGER_JMS_DESTINATION_RESOURCES);
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", jndiName), TRIGGER_EDIT_JMS_DEST_RES);
        //Click on the target tab and verify whether the target is in the target table or not.
        clickAndWait("propertyForm:resEditTabs:targetTab", TRIGGER_EDIT_RESOURCE_TARGETS);
        assertTrue(selenium.isTextPresent(instanceName));

        //Enable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                "propertyForm:targetTable:topActionsGroup1:button2",
                "propertyForm:resEditTabs:general",
                "propertyForm:resEditTabs:targetTab",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                TRIGGER_EDIT_JMS_DEST_RES,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                ENABLE_STATUS);

        //Disable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                "propertyForm:targetTable:topActionsGroup1:button3",
                "propertyForm:resEditTabs:general",
                "propertyForm:resEditTabs:targetTab",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                TRIGGER_EDIT_JMS_DEST_RES,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                DISABLE_STATUS);
        //Test the manage targets
        clickAndWait("propertyForm:targetTable:topActionsGroup1:manageTargetButton", TRIGGER_MANAGE_TARGETS);
        //Remove the created instance from the selected targets.
        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_selected", "label=" + instanceName);
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        clickAndWait("form:propertyContentPage:topButtons:saveButton", TRIGGGER_VALUES_SAVED);
        assertFalse(selenium.isTextPresent(instanceName));
        //Go Back to Resources Page
        clickAndWait("treeForm:tree:resources:jmsResources:jmsDestinationResources:jmsDestinationResources_link", TRIGGER_JMS_DESTINATION_RESOURCES);
    }

/*
    @Test
    public void testAddingTransport() {
        selenium.click("treeForm:tree:configurations:server-config:networkConfig:transports:transports_link");
        verifyTrue(selenium.isTextPresent("Click New to define a new transport. Click the name of an existing transport to modify its settings."));
        selenium.click("propertyForm:configs:topActionsGroup1:newButton");
        verifyTrue(selenium.isTextPresent("New Transport"));
        selenium.type("propertyForm:propertySheet:propertSectionTextField:IdTextProp:IdText", "transport");
        selenium.select("propertyForm:propertySheet:propertSectionTextField:ByteBufferType:ByteBufferType", "label=DIRECT");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:BufferSizeBytes:BufferSizeBytes", "16384");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:AcceptorThreads:AcceptorThreads", "2");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:MaxConnectionsCount:MaxConnectionsCount", "8192");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:IdleKeyTimeoutSeconds:IdleKeyTimeoutSeconds", "60");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:ReadTimeoutMillis:ReadTimeoutMillis", "60000");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:SelectorPollTimeoutMillis:SelectorPollTimeoutMillis", "2000");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:WriteTimeoutMillis:WriteTimeoutMillis", "60000");
        selenium.click("propertyForm:propertyContentPage:topButtons:newButton");
        verifyTrue(selenium.isTextPresent("Click New to define a new transport. Click the name of an existing transport to modify its settings."));
        assertTrue(selenium.isTextPresent("transport"));
        selenium.click("propertyForm:configs:rowGroup1:0:col1:link");
        verifyTrue(selenium.isTextPresent("Edit Transport"));
        verifyTrue(selenium.isElementPresent("propertyForm:propertySheet:propertSectionTextField:ByteBufferType:ByteBufferType"));
        verifyEquals("16384", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:BufferSizeBytes:BufferSizeBytes"));
        verifyEquals("2", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:AcceptorThreads:AcceptorThreads"));
        verifyEquals("8192", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:MaxConnectionsCount:MaxConnectionsCount"));
        verifyEquals("60", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:IdleKeyTimeoutSeconds:IdleKeyTimeoutSeconds"));
        verifyEquals("60000", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:ReadTimeoutMillis:ReadTimeoutMillis"));
        verifyEquals("2000", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:SelectorPollTimeoutMillis:SelectorPollTimeoutMillis"));
        verifyEquals("60000", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:WriteTimeoutMillis:WriteTimeoutMillis"));
        selenium.click("propertyForm:propertyContentPage:topButtons:cancelButton");
        verifyTrue(selenium.isTextPresent("Click New to define a new transport. Click the name of an existing transport to modify its settings."));
        selenium.click("propertyForm:configs:rowGroup1:0:col0:select");
        selenium.click("propertyForm:configs:topActionsGroup1:button1");
        assertTrue(selenium.getConfirmation().matches("^Selected Transport\\(s\\) will be deleted\\.  Continue[\\s\\S]$"));

    }
*/
}

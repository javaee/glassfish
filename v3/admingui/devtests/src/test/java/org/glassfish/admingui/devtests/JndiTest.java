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

public class JndiTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_CUSTOM_RESOURCES = "Custom resources are nonstandard resources";
    private static final String TRIGGER_NEW_CUSTOM_RESOURCE = "New Custom Resource";
    private static final String TRIGGER_EDIT_CUSTOM_RESOURCE = "Edit Custom Resource";
    private static final String TRIGGER_EDIT_EXTERNAL_RESOURCE = "Edit External Resource";
    private static final String TRIGGER_EXTERNAL_RESOURCES = "Manage external JNDI resources when";
    private static final String TRIGGER_NEW_EXTERNAL_RESOURCE = "New External Resource";

    private static final String TRIGGER_EDIT_RESOURCE_TARGETS = "Resource Targets";
    private static final String ENABLE_STATUS = "Enabled on All Targets";
    private static final String DISABLE_STATUS = "Disabled on All Targets";
    private static final String TRIGGER_MANAGE_TARGETS = "Manage Targets";
    private static final String TRIGGGER_VALUES_SAVED = "New values successfully saved.";

    @Test
    public void testCustomResources() {
        final String resourceName = generateRandomString();

        clickAndWait("treeForm:tree:resources:jndi:customResources:customResources_link", TRIGGER_CUSTOM_RESOURCES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_CUSTOM_RESOURCE);

        selenium.type("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        selenium.select("form:propertySheet:propertSectionTextField:cp:Classname", "label=java.lang.Double");
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_CUSTOM_RESOURCES);

        assertTrue(selenium.isTextPresent(resourceName));

        testDisableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "form1:propertySheet:propertSectionTextField:statusProp:enabled",
                "form1:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CUSTOM_RESOURCES,
                TRIGGER_EDIT_CUSTOM_RESOURCE,
                "off");
        testEnableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "form1:propertySheet:propertSectionTextField:statusProp:enabled",
                "form1:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CUSTOM_RESOURCES,
                TRIGGER_EDIT_CUSTOM_RESOURCE,
                "on");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
    }

    @Test
    public void testCustomResourcesWithTargets() {
        final String resourceName = generateRandomString();
        final String instanceName = generateRandomString();
       
        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);
        
        clickAndWait("treeForm:tree:resources:jndi:customResources:customResources_link", TRIGGER_CUSTOM_RESOURCES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_CUSTOM_RESOURCE);

        selenium.type("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        selenium.select("form:propertySheet:propertSectionTextField:cp:Classname", "label=java.lang.Double");
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form:basicTable:rowGroup1:0:col4:col1St", "description");
        
        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", "label=" + instanceName);
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_CUSTOM_RESOURCES);

        assertTrue(selenium.isTextPresent(resourceName));
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", resourceName), TRIGGER_EDIT_CUSTOM_RESOURCE);
        assertTableRowCount("form1:basicTable", count);
        clickAndWait("form1:propertyContentPage:topButtons:cancelButton", TRIGGER_CUSTOM_RESOURCES);

        testDisableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "form1:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "form1:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CUSTOM_RESOURCES,
                TRIGGER_EDIT_CUSTOM_RESOURCE,
                DISABLE_STATUS);
        testEnableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "form1:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "form1:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CUSTOM_RESOURCES,
                TRIGGER_EDIT_CUSTOM_RESOURCE,
                ENABLE_STATUS);
        manageCustomResourceTargets(instanceName, resourceName);
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", instanceTest.TRIGGER_INSTANCES_PAGE);
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
        assertFalse(selenium.isTextPresent(instanceName));
    }

    private void manageCustomResourceTargets(String instanceName, String jndiName) {
        
        clickAndWait("treeForm:tree:resources:jndi:customResources:customResources_link", TRIGGER_CUSTOM_RESOURCES);
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", jndiName), TRIGGER_EDIT_CUSTOM_RESOURCE);
        //Click on the target tab and verify whether the target is in the target table or not.
        clickAndWait("form1:resEditTabs:targetTab", TRIGGER_EDIT_RESOURCE_TARGETS);
        assertTrue(selenium.isTextPresent(instanceName));

        //Enable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                "propertyForm:targetTable:topActionsGroup1:button2",
                "propertyForm:resEditTabs:general",
                "form1:resEditTabs:targetTab",
                "form1:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                TRIGGER_EDIT_CUSTOM_RESOURCE,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                ENABLE_STATUS);

        //Disable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                "propertyForm:targetTable:topActionsGroup1:button3",
                "propertyForm:resEditTabs:general",
                "form1:resEditTabs:targetTab",
                "form1:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                TRIGGER_EDIT_CUSTOM_RESOURCE,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                DISABLE_STATUS);

        //Test the manage targets
        clickAndWait("propertyForm:targetTable:topActionsGroup1:manageTargetButton", TRIGGER_MANAGE_TARGETS);
        //Remove the created instance from the selected targets.
        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_selected", "label=" + instanceName);
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        clickAndWait("form:propertyContentPage:topButtons:saveButton", TRIGGGER_VALUES_SAVED);
        assertFalse(selenium.isTextPresent(jndiName));
        //Go Back to Resources Page
        clickAndWait("treeForm:tree:resources:jndi:customResources:customResources_link", TRIGGER_CUSTOM_RESOURCES);
    }
    
    @Test
    public void testExternalResources() {
        final String resourceName = generateRandomString();
        final String description = resourceName + " - description";

        clickAndWait("treeForm:tree:resources:jndi:externalResources:externalResources_link", TRIGGER_EXTERNAL_RESOURCES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_EXTERNAL_RESOURCE);

        selenium.type("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        selenium.select("form:propertySheet:propertSectionTextField:cp:Classname", "label=java.lang.Double");
        selenium.type("form:propertySheet:propertSectionTextField:jndiLookupProp:jndiLookup", resourceName);
        selenium.type("form:propertySheet:propertSectionTextField:descProp:desc", description);
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_EXTERNAL_RESOURCES);

        testDisableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "form:propertySheet:propertSectionTextField:statusProp:enabled",
                "form:propertyContentPage:topButtons:cancelButton",
                TRIGGER_EXTERNAL_RESOURCES,
                TRIGGER_EDIT_EXTERNAL_RESOURCE,
                "off");
        testEnableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "form:propertySheet:propertSectionTextField:statusProp:enabled",
                "form:propertyContentPage:topButtons:cancelButton",
                TRIGGER_EXTERNAL_RESOURCES,
                TRIGGER_EDIT_EXTERNAL_RESOURCE,
                "on");

//        selectTableRowByValue("propertyForm:resourcesTable", resourceName);
//        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:button3");
//        selenium.click("propertyForm:resourcesTable:topActionsGroup1:button3");
//        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:button3");
//
//        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", resourceName), TRIGGER_EDIT_EXTERNAL_RESOURCE);
//
//        assertEquals("off", selenium.getValue("form1:propertySheet:propertSectionTextField:statusProp:enabled"));
//        clickAndWait("form1:propertyContentPage:topButtons:cancelButton", TRIGGER_EXTERNAL_RESOURCES);
//
//        selectTableRowByValue("propertyForm:resourcesTable", resourceName);
//        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:button2");
//        selenium.click("propertyForm:resourcesTable:topActionsGroup1:button2");
//        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:button2");
//
//        clickAndWait("propertyForm:resourcesTable:rowGroup1:0:col1:link", TRIGGER_EDIT_EXTERNAL_RESOURCE);
//        assertEquals("on", selenium.getValue("form1:propertySheet:propertSectionTextField:statusProp:enabled"));
//        clickAndWait("form1:propertyContentPage:topButtons:cancelButton", TRIGGER_EXTERNAL_RESOURCES);

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
    }

    @Test
    public void testExternalResourcesTargets() {
        final String resourceName = generateRandomString();
        final String description = resourceName + " - description";
        final String instanceName = generateRandomString();

        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);

        clickAndWait("treeForm:tree:resources:jndi:externalResources:externalResources_link", TRIGGER_EXTERNAL_RESOURCES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_EXTERNAL_RESOURCE);

        selenium.type("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        selenium.select("form:propertySheet:propertSectionTextField:cp:Classname", "label=java.lang.Double");
        selenium.type("form:propertySheet:propertSectionTextField:jndiLookupProp:jndiLookup", resourceName);
        selenium.type("form:propertySheet:propertSectionTextField:descProp:desc", description);
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        selenium.type("form:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form:basicTable:rowGroup1:0:col4:col1St", "description");

        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", "label=" + instanceName);
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_EXTERNAL_RESOURCES);

        assertTrue(selenium.isTextPresent(resourceName));
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", resourceName), TRIGGER_EDIT_EXTERNAL_RESOURCE);
        assertTableRowCount("form:basicTable", count);
        clickAndWait("form:propertyContentPage:topButtons:cancelButton", TRIGGER_EXTERNAL_RESOURCES);

        testDisableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "form:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "form:propertyContentPage:topButtons:cancelButton",
                TRIGGER_EXTERNAL_RESOURCES,
                TRIGGER_EDIT_EXTERNAL_RESOURCE,
                DISABLE_STATUS);
        testEnableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "form:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "form:propertyContentPage:topButtons:cancelButton",
                TRIGGER_EXTERNAL_RESOURCES,
                TRIGGER_EDIT_EXTERNAL_RESOURCE,
                ENABLE_STATUS);

        manageExternalResourceTargets(instanceName, resourceName);
        //Delete the External resource
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", instanceTest.TRIGGER_INSTANCES_PAGE);
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
        assertFalse(selenium.isTextPresent(instanceName));
    }

    private void manageExternalResourceTargets(String instanceName, String jndiName) {

        clickAndWait("treeForm:tree:resources:jndi:externalResources:externalResources_link", TRIGGER_EXTERNAL_RESOURCES);
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", jndiName), TRIGGER_EDIT_EXTERNAL_RESOURCE);
        //Click on the target tab and verify whether the target is in the target table or not.
        clickAndWait("form:resEditTabs:targetTab", TRIGGER_EDIT_RESOURCE_TARGETS);
        assertTrue(selenium.isTextPresent(instanceName));

        //Enable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                "propertyForm:targetTable:topActionsGroup1:button2",
                "propertyForm:resEditTabs:general",
                "form:resEditTabs:targetTab",
                "form:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                TRIGGER_EDIT_EXTERNAL_RESOURCE,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                ENABLE_STATUS);

        //Disable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                "propertyForm:targetTable:topActionsGroup1:button3",
                "propertyForm:resEditTabs:general",
                "form:resEditTabs:targetTab",
                "form:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                TRIGGER_EDIT_EXTERNAL_RESOURCE,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                DISABLE_STATUS);

        //Test the manage targets
        clickAndWait("propertyForm:targetTable:topActionsGroup1:manageTargetButton", TRIGGER_MANAGE_TARGETS);
        //Remove the created instance from the selected targets.
        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_selected", "label=" + instanceName);
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        clickAndWait("form:propertyContentPage:topButtons:saveButton", TRIGGGER_VALUES_SAVED);
        assertFalse(selenium.isTextPresent(jndiName));
        //Go Back to Resources Page
        clickAndWait("treeForm:tree:resources:jndi:externalResources:externalResources_link", TRIGGER_EXTERNAL_RESOURCES);
    }
}

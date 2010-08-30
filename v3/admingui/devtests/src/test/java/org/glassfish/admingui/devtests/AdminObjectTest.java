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

public class AdminObjectTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_ADMIN_OBJECT_RESOURCES = "An administered object resource provides specialized functionality that is defined by the resource adapter for the deployed connector module.";
    private static final String TRIGGER_NEW_ADMIN_OBJECT_RESOURCE = "New Admin Object Resource";
    private static final String TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE = "Edit Admin Object Resource";

    @Test
    public void testAdminObjectResources() throws Exception {
        final String resName = generateRandomString();
        final String description = "Admin Object Resource - " + resName;

        //Go to Admin Object Resources Page.
        clickAndWait("treeForm:tree:resources:Connectors:adminObjectResources:adminObjectResources_link", TRIGGER_ADMIN_OBJECT_RESOURCES);

        //New Admin Object Resources
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_ADMIN_OBJECT_RESOURCE);

        selenium.type("form:propertySheet:propertSectionTextField:nameNew:name", resName);
        selenium.type("form:propertySheet:propertSectionTextField:descriptionProp:descAdaptor", description);
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        selenium.type("form:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_ADMIN_OBJECT_RESOURCES);

        assertTrue(selenium.isTextPresent(resName));
        assertTrue(selenium.isTextPresent(description));
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", resName), TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE);
        assertTableRowCount("propertyForm:basicTable", count);

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_ADMIN_OBJECT_RESOURCES);

        testDisableButton(resName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_ADMIN_OBJECT_RESOURCES,
                TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE,
                "off");
        testEnableButton(resName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_ADMIN_OBJECT_RESOURCES,
                TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE,
                "on");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resName);
    }

    @Test
    public void testAdminObjectResourcesWithTargets() {
        final String resName = generateRandomString();
        final String description = "Admin Object Resource - " + resName;
        final String instanceName = generateRandomString();
        final String enableStatus = "Enabled on All Targets";
        final String disableStatus = "Disabled on All Targets";

        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);

        //Go to Admin Object Resources Page.
        clickAndWait("treeForm:tree:resources:Connectors:adminObjectResources:adminObjectResources_link", TRIGGER_ADMIN_OBJECT_RESOURCES);

        //New Admin Object Resources
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_ADMIN_OBJECT_RESOURCE);

        selenium.type("form:propertySheet:propertSectionTextField:nameNew:name", resName);
        selenium.type("form:propertySheet:propertSectionTextField:descriptionProp:descAdaptor", description);
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        selenium.type("form:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form:basicTable:rowGroup1:0:col4:col1St", "description");

        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", "label=" + instanceName);
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");

        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_ADMIN_OBJECT_RESOURCES);

        assertTrue(selenium.isTextPresent(resName));
        assertTrue(selenium.isTextPresent(description));
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", resName), TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE);
        assertTableRowCount("propertyForm:basicTable", count);

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_ADMIN_OBJECT_RESOURCES);

        testDisableButton(resName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_ADMIN_OBJECT_RESOURCES,
                TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE,
                disableStatus);
        testEnableButton(resName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_ADMIN_OBJECT_RESOURCES,
                TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE,
                enableStatus);

        manageTargets(instanceName, resName);

        // Delete admin object resource
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resName);

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

        clickAndWait("treeForm:tree:resources:Connectors:adminObjectResources:adminObjectResources_link", TRIGGER_ADMIN_OBJECT_RESOURCES);
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", jndiName), TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE);
        //Click on the target tab and verify whether the target is in the target table or not.
        clickAndWait("propertyForm:resEditTabs:targetTab", TRIGGER_EDIT_RESOURCE_TARGETS);
        assertTrue(selenium.isTextPresent(instanceName));

        //Enable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                "propertyForm:targetTable:topActionsGroup1:button2",
                "propertyForm:resEditTabs:general",
                "propertyForm:resEditTabs:targetTab",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                enableStatus);

        //Disable all targets
        testEnableOrDisableTarget("propertyForm:targetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image",
                "propertyForm:targetTable:topActionsGroup1:button3",
                "propertyForm:resEditTabs:general",
                "propertyForm:resEditTabs:targetTab",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE,
                TRIGGER_EDIT_RESOURCE_TARGETS,
                disableStatus);

        //Test the manage targets
        clickAndWait("propertyForm:targetTable:topActionsGroup1:manageTargetButton", TRIGGER_MANAGE_TARGETS);
        //Remove the created instance from the selected targets.
        selenium.addSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_selected", "label=" + instanceName);
        selenium.click("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        clickAndWait("form:propertyContentPage:topButtons:saveButton", TRIGGGER_VALUES_SAVED);
        assertFalse(selenium.isTextPresent(instanceName));
        //Go Back to Resources Page
        clickAndWait("treeForm:tree:resources:Connectors:adminObjectResources:adminObjectResources_link", TRIGGER_ADMIN_OBJECT_RESOURCES);
    }
}

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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author anilam
 */
public class StandaloneTest  extends BaseSeleniumTestClass {
    final String TRIGGER_INSTANCES_PAGE = "Server Instances (";
    final String TRIGGER_NEW_PAGE = "Configuration:";

    @Test
    public void testCreateStandaloneInstance() {
        String instanceName = "standAlone" + generateRandomString();
        createStandAloneInstance(instanceName);
        
        String prefix = getTableRowByValue("propertyForm:instancesTable", instanceName, "col1");
        assertTrue(selenium.isTextPresent(instanceName));
        assertEquals(instanceName, selenium.getText(prefix + "col1:link"));
        assertEquals(instanceName+"-config", selenium.getText(prefix + "col3:configlink"));
        assertEquals("localhost", selenium.getText(prefix + "col5:nodeAgentlink"));
        assertEquals("Stopped", selenium.getText(prefix + "col6"));
        assertEquals("100", selenium.getValue(prefix + "col2:weight"));
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
    }

    @Test
    public void testDeleteStandaloneInstance() {
        String instanceName = "standAlone" + generateRandomString();
        createStandAloneInstance(instanceName);
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
        assertFalse(selenium.isTextPresent(instanceName));

    }

    @Test
    public void testStartStandaloneInstance() {
        String instanceName = "standAlone" + generateRandomString();
        createStandAloneInstance(instanceName);
        rowActionWithConfirm("propertyForm:instancesTable:topActionsGroup1:button2", "propertyForm:instancesTable", instanceName);
        waitForCondition("document.getElementById('propertyForm:instancesTable:topActionsGroup1:button2').value != 'Processing...'", 300000);
        String prefix = getTableRowByValue("propertyForm:instancesTable", instanceName, "col1");
        assertEquals("Running", selenium.getText(prefix + "col6"));
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
    }

    @Test
    public void testStopStandaloneInstance() {
        String instanceName = "standAlone" + generateRandomString();
        createStandAloneInstance(instanceName);

        //start it
        rowActionWithConfirm("propertyForm:instancesTable:topActionsGroup1:button2", "propertyForm:instancesTable", instanceName);
        waitForCondition("document.getElementById('propertyForm:instancesTable:topActionsGroup1:button2').value != 'Processing...'", 300000);
        String prefix = getTableRowByValue("propertyForm:instancesTable", instanceName, "col1");
        assertEquals("Running", selenium.getText(prefix + "col6"));
        
        //stop it
        rowActionWithConfirm("propertyForm:instancesTable:topActionsGroup1:button3", "propertyForm:instancesTable", instanceName);
        waitForCondition("document.getElementById('propertyForm:instancesTable:topActionsGroup1:button3').value != 'Processing...'", 300000);
        assertEquals("Stopped", selenium.getText(prefix + "col6"));
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
    }


    private void createStandAloneInstance(String instanceName){
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", TRIGGER_INSTANCES_PAGE);
        clickAndWait("propertyForm:instancesTable:topActionsGroup1:newButton", TRIGGER_NEW_PAGE );
        selenium.type("propertyForm:propertySheet:propertSectionTextField:NameTextProp:NameText", instanceName);
        selenium.select("propertyForm:propertySheet:propertSectionTextField:node:node", "label=localhost");
        selenium.select("propertyForm:propertySheet:propertSectionTextField:configProp:Config", "label=default-config");
        selenium.click("propertyForm:propertySheet:propertSectionTextField:configOptionProp:optC");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_INSTANCES_PAGE);
    }

}

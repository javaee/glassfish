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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author anilam
 */
public class NodeTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_NODES_PAGE = "Nodes \\(";
    private static final String TRIGGER_NEW_NODE_PAGE = "KeyFile:";
    private static final String TRIGGER_EDIT_NODE = "KeyFile:";
    private static final String TRIGGER_SAVE_SUCCESS = "New values successfully saved";
    private static final String TRIGGER_INSTANCES_PAGE = "Server Instances \\(";
    private static final String TRIGGER_NEW_INSTANCE_PAGE = "Configuration:";

    @Test
    public void testCreateAndDeleteNode() {
        final String nodeName = "testNode" + generateRandomString();

        createNode(nodeName);
        assertTrue(selenium.isTextPresent(nodeName));
        String prefix = getTableRowByValue("propertyForm:nodesTable", nodeName, "col1");
        assertTrue(selenium.isTextPresent(nodeName));
        assertEquals(nodeName, selenium.getText(prefix + "col1:link"));
        assertEquals("localhost", selenium.getText(prefix + "col2:nodeHostCol"));

        //Verify the node is created with the value specified.
        clickAndWait( getLinkIdByLinkText("propertyForm:nodesTable", nodeName), TRIGGER_EDIT_NODE) ;
        assertTrue(selenium.isTextPresent(nodeName));
        assertEquals("localhost", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:NodeHost:NodeHost"));
//        assertEquals("/NodeDir", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:NodeHome:NodeHome"));
//        assertEquals("/InstallDir", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:installdir:installDir"));
        assertEquals("24", selenium.getValue("propertyForm:propertySheet:sshConnector:sshNodeHome:sshport"));
        assertEquals("sshUserName", selenium.getValue("propertyForm:propertySheet:sshAuth:UserName:UserName"));
        assertEquals("sshKeyFile", selenium.getValue("propertyForm:propertySheet:sshAuth:Keyfile:Keyfile"));
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_NODES_PAGE);

        //Test Delete Node
        deleteRow("propertyForm:nodesTable:topActionsGroup1:button1", "propertyForm:nodesTable", nodeName);
    }

    @Test
    public void testUpdateNode() {
        final String nodeName = "testNode" + generateRandomString();
        createNode(nodeName);
        clickAndWait( getLinkIdByLinkText("propertyForm:nodesTable", nodeName), TRIGGER_EDIT_NODE);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:NodeHost:NodeHost", "localhosttest");
//        selenium.type("propertyForm:propertySheet:propertSectionTextField:NodeHome:NodeHome", "/tmp/test");
//        selenium.type("propertyForm:propertySheet:propertSectionTextField:installdir:installDir", "/tmp/installTest");
        selenium.type("propertyForm:propertySheet:sshConnector:sshNodeHome:sshport", "44");
        selenium.type("propertyForm:propertySheet:sshAuth:UserName:UserName", "sUserName");
        selenium.type("propertyForm:propertySheet:sshAuth:Keyfile:Keyfile", "sKeyFile");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_SAVE_SUCCESS );

        assertEquals("localhosttest", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:NodeHost:NodeHost" ));
//        assertEquals("/tmp/test", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:NodeHome:NodeHome" ));
//        assertEquals("/tmp/installTest", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:installdir:installDir" ));
        assertEquals("44", selenium.getValue("propertyForm:propertySheet:sshConnector:sshNodeHome:sshport" ));
        assertEquals("sUserName", selenium.getValue("propertyForm:propertySheet:sshAuth:UserName:UserName" ));
        assertEquals("sKeyFile", selenium.getValue("propertyForm:propertySheet:sshAuth:Keyfile:Keyfile" ));
        clickAndWait("treeForm:tree:nodeTreeNode:nodeTreeNode_link", TRIGGER_NODES_PAGE);
        deleteRow("propertyForm:nodesTable:topActionsGroup1:button1", "propertyForm:nodesTable", nodeName);
    }

    /* Create a Node,  create an instance with this node,  delete this node will cause error */
    @Test
    public void testDeleteWithInstance(){
        final String nodeName = "testNode" + generateRandomString();
        final String instanceName = "testInstance" + generateRandomString();

        createNode(nodeName);
        createInstance(instanceName, nodeName);
        clickAndWait("treeForm:tree:nodeTreeNode:nodeTreeNode_link", TRIGGER_NODES_PAGE);
        // This part shoudl fail?
        rowActionWithConfirm("propertyForm:nodesTable:topActionsGroup1:button1", "propertyForm:nodesTable", nodeName);
        waitForCondition("document.getElementById('propertyForm:nodesTable:topActionsGroup1:button1').value != 'Processing...'", 50000);
        assertTrue(selenium.isTextPresent("Remove instances before deleting node."));

        //cleanup
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", TRIGGER_INSTANCES_PAGE);
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);

        clickAndWait("treeForm:tree:nodeTreeNode:nodeTreeNode_link", TRIGGER_NODES_PAGE);
        deleteRow("propertyForm:nodesTable:topActionsGroup1:button1", "propertyForm:nodesTable", nodeName);
    }

    private void createNode(String nodeName){
        clickAndWait("treeForm:tree:nodeTreeNode:nodeTreeNode_link", TRIGGER_NODES_PAGE);
        clickAndWait("propertyForm:nodesTable:topActionsGroup1:newButton", TRIGGER_NEW_NODE_PAGE);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:nameProp:name", nodeName);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:NodeHost:NodeHost", "localhost");
//        selenium.type("propertyForm:propertySheet:propertSectionTextField:NodeHome:NodeHome", "/NodeDir");
//        selenium.type("propertyForm:propertySheet:propertSectionTextField:installdir:installDir", "/InstallDir");
        selenium.check("propertyForm:propertySheet:propertSectionTextField:force:force");
        selenium.type("propertyForm:propertySheet:sshConnector:sshNodeHome:sshport", "24");
        selenium.type("propertyForm:propertySheet:sshAuth:UserName:UserName", "sshUserName");
        selenium.type("propertyForm:propertySheet:sshAuth:Keyfile:Keyfile", "sshKeyFile");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_NODES_PAGE);
        assertTrue(tableContainsRow("propertyForm:nodesTable", "col1", nodeName));
    }

    private void createInstance(String instanceName, String nodeName){
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", TRIGGER_INSTANCES_PAGE);
        clickAndWait("propertyForm:instancesTable:topActionsGroup1:newButton", TRIGGER_NEW_INSTANCE_PAGE );
        selenium.type("propertyForm:propertySheet:propertSectionTextField:NameTextProp:NameText", instanceName);
        selenium.select("propertyForm:propertySheet:propertSectionTextField:node:node", "label="+nodeName);
        selenium.select("propertyForm:propertySheet:propertSectionTextField:configProp:Config", "label=default-config");
        selenium.click("propertyForm:propertySheet:propertSectionTextField:configOptionProp:optC");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_INSTANCES_PAGE);
        assertTrue(tableContainsRow("propertyForm:instancesTable", "col1", instanceName));
    }
}

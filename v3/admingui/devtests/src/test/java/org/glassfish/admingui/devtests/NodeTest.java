/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class NodeTest extends BaseSeleniumTestClass {
    public static final String TRIGGER_NODES_PAGE = "Nodes (";
    public static final String TRIGGER_NEW_NODE_PAGE = "KeyFile:";
    public static final String TRIGGER_EDIT_NODE = "KeyFile:";

    @Test
    public void testCreateAndDeleteNode() {
        final String nodeName = "testNode" + generateRandomString();

        //Test Node is created successfully
        clickAndWait("treeForm:tree:nodeTreeNode:nodeTreeNode_link", TRIGGER_NODES_PAGE);
        clickAndWait("propertyForm:nodesTable:topActionsGroup1:newButton", TRIGGER_NEW_NODE_PAGE);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:nameProp:name", nodeName);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:NodeHost:NodeHost", "localhost");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:NodeHome:NodeHome", "/NodeDir");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:installdir:installDir", "/InstallDir");
        selenium.click("propertyForm:propertySheet:propertSectionTextField:force:force");
        selenium.type("propertyForm:propertySheet:sshConnector:sshNodeHome:sshport", "24");
        selenium.type("propertyForm:propertySheet:sshAuth:UserName:UserName", "sshUserName");
        selenium.type("propertyForm:propertySheet:sshAuth:Keyfile:Keyfile", "sshKeyFile");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_NODES_PAGE);
        assertTrue(selenium.isTextPresent(nodeName));

        //Verify the node is created with the value specified.
        clickAndWait( getLinkIdByLinkText("propertyForm:nodesTable:rowGroup1:1:col1:link", nodeName), TRIGGER_EDIT_NODE) ;
        assertTrue(selenium.isTextPresent(nodeName));
        assertEquals("localhost", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:NodeHost:NodeHost"));
        assertEquals("/NodeDir", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:NodeHome:NodeHome"));
        assertEquals("/InstallDir", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:installdir:installDir"));
        assertEquals("24", selenium.getValue("propertyForm:propertySheet:sshConnector:sshNodeHome:sshport"));
        assertEquals("sshUserName", selenium.getValue("propertyForm:propertySheet:sshAuth:UserName:UserName"));
        assertEquals("sshKeyFile", selenium.getValue("propertyForm:propertySheet:sshAuth:Keyfile:Keyfile"));
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_NODES_PAGE);

        //Test Delete Node
        deleteRow("propertyForm:nodesTable:topActionsGroup1:button1", "propertyForm:nodesTable", nodeName);
        waitForCondition("document.getElementById('propertyForm:nodesTable:topActionsGroup1:button1').text != 'Processing...'", 10000);
        assertFalse(selenium.isTextPresent(nodeName));
    }

}

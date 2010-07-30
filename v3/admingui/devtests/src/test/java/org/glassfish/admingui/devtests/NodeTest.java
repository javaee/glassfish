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
    public static final String NODES_TITLE = "Nodes";
    public static final String NEW_NODE_TITLE = "New Node";
    public static final String EDIT_NODE_TITLE = "Edit Node";

    @Test
    public void testCreateNode() {
        final String nodeName = "testNode" + generateRandomString();

        //Test Node is created successfully
        clickAndWait("treeForm:tree:nodeTreeNode:nodeTreeNode_link", NODES_TITLE);
        clickAndWait("propertyForm:nodesTable:topActionsGroup1:newButton" , NEW_NODE_TITLE);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:nameProp:name", nodeName);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:NodeHost:NodeHost", "localhost");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:NodeHome:NodeHome", "/NodeDir");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:installdir:installDir", "/InstallDir");
        selenium.click("propertyForm:propertySheet:propertSectionTextField:force:sun_checkbox171");
        selenium.type("propertyForm:propertySheet:sshConnector:sshNodeHome:sshport", "24");
        selenium.type("propertyForm:propertySheet:sshAuth:UserName:UserName", "sshUserName");
        selenium.type("propertyForm:propertySheet:sshAuth:Keyfile:Keyfile", "sshKeyFile");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton" , NODES_TITLE);
        assertTrue(selenium.isTextPresent(nodeName));

        //Verify the node is created with the value specified.
        clickAndWait( getLinkIdByLinkText("propertyForm:nodesTable:rowGroup1:1:col1:link", nodeName), EDIT_NODE_TITLE) ;
        assertTrue(selenium.isTextPresent(nodeName));
        assertEquals("localhost", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:NodeHost:NodeHost"));
        assertEquals("/NodeDir", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:NodeHome:NodeHome"));
        assertEquals("/InstallDir", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:installdir:installDir"));
        assertEquals("24", selenium.getValue("propertyForm:propertySheet:sshConnector:sshNodeHome:sshport"));
        assertEquals("sshUserName", selenium.getValue("propertyForm:propertySheet:sshAuth:UserName:UserName"));
        assertEquals("sshKeyFile", selenium.getValue("propertyForm:propertySheet:sshAuth:Keyfile:Keyfile"));
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", NODES_TITLE);
        deleteRow("propertyForm:nodesTable:topActionsGroup1:button1", "propertyForm:nodesTable", nodeName);
        waitForButtonEnabled("propertyForm:nodesTable:topActionsGroup1:button1");
        assertFalse(selenium.isTextPresent(nodeName));
    }

}

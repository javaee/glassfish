/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
        clickAndWait( getLinkIdByLinkText("propertyForm:nodesTable", nodeName), TRIGGER_EDIT_NODE) ;

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

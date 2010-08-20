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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;



/**
 *
 * @author anilam
 */
public class ClusterTest extends BaseSeleniumTestClass {

    final String TRIGGER_CLUSTER_PAGE = "Clusters (";
    final String TRIGGER_NEW_PAGE = "Server Instances to be Created";
    final String TRIGGER_CLUSTER_GENERAL_PAGE = "Status:";
    final String TRIGGER_CLUSTER_INSTANCE_NEW_PAGE = "Node:";
    final String TRIGGER_CLUSTER_INSTANCES_PAGE = "Server Instances (";

    
    @Test
    public void testCreateClusterWithOneInstance() {
        String clusterName = "cluster" + generateRandomString();
        String instanceName = "instanceName" + generateRandomString();

        createCluster(clusterName,  instanceName);
        assertTrue(selenium.isTextPresent(clusterName));

        String prefix = getTableRowByValue("propertyForm:clustersTable", clusterName, "col1");
        assertEquals(clusterName, selenium.getText(prefix + "col1:link"));
        assertEquals(clusterName+"-config", selenium.getText(prefix + "col2:configlink"));
        assertEquals(instanceName, selenium.getText(prefix + "col3:iLink"));
        deleteRow("propertyForm:clustersTable:topActionsGroup1:button1", "propertyForm:clustersTable", clusterName);
    }


    @Test
    public void testStartAndStopClusterWithOneInstance() {
        String clusterName = "clusterName" + generateRandomString();
        String instanceName1 = "instanceName" + generateRandomString();
        String instanceName2 = "instanceName" + generateRandomString();

        createCluster(clusterName,  instanceName1);
        assertTrue(selenium.isTextPresent(clusterName));
        
        rowActionWithConfirm("propertyForm:clustersTable:topActionsGroup1:button2", "propertyForm:clustersTable", clusterName);
        waitForCondition("document.getElementById('propertyForm:clustersTable:topActionsGroup1:button2').value != 'Processing...'", 300000);
        String prefix = getTableRowByValue("propertyForm:clustersTable", clusterName, "col1");
        assertTrue( (selenium.getText( prefix + "col3").indexOf("Running") != -1));
        rowActionWithConfirm("propertyForm:clustersTable:topActionsGroup1:button3", "propertyForm:clustersTable", clusterName);
        waitForCondition("document.getElementById('propertyForm:clustersTable:topActionsGroup1:button3').value != 'Processing...'", 300000);
        assertTrue( (selenium.getText( prefix + "col3").indexOf("Stopped") != -1));
        deleteRow("propertyForm:clustersTable:topActionsGroup1:button1", "propertyForm:clustersTable", clusterName);

    }


    @Test
    public void testClusterGeneralPage() {
        String clusterName = "cluster" + generateRandomString();
        String instanceName = "instanceName" + generateRandomString();

        createCluster(clusterName,  instanceName);
        assertTrue(selenium.isTextPresent(clusterName));
        clickAndWait( getLinkIdByLinkText("propertyForm:clustersTable", clusterName), TRIGGER_CLUSTER_GENERAL_PAGE) ;
        assertEquals(clusterName, selenium.getText("propertyForm:propertySheet:propertSectionTextField:clusterNameProp:clusterName"));

        //ensure config link is fine.
        //TODO:  how to ensure thats the correct configuration page ?
        assertEquals(clusterName+"-config", selenium.getText("propertyForm:propertySheet:propertSectionTextField:configNameProp:configlink"));
        clickAndWait("propertyForm:propertySheet:propertSectionTextField:configNameProp:configlink", "Admin Service");

        //Back to the Clusters page,  ensure default value is there.
        clickAndWait("treeForm:tree:clusterTreeNode:clusterTreeNode_link", TRIGGER_CLUSTER_PAGE);
        clickAndWait( getLinkIdByLinkText("propertyForm:clustersTable", clusterName), TRIGGER_CLUSTER_GENERAL_PAGE) ;
        assertEquals("0 instance(s) running", selenium.getText("propertyForm:propertySheet:propertSectionTextField:instanceStatusProp:instanceStatusRunning"));
        assertEquals("1 instance(s) not running", selenium.getText("propertyForm:propertySheet:propertSectionTextField:instanceStatusProp:instanceStatusStopped"));

        //change value
        selenium.type("propertyForm:propertySheet:propertSectionTextField:gmsMulticastPort:gmsMulticastPort", "12345");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:gmsMulticastAddress:gmsMulticastAddress", "123.234.456.88");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:GmsBindInterfaceAddress:GmsBindInterfaceAddress", "${ABCDE}");
        selenium.click("propertyForm:propertySheet:propertSectionTextField:gmsEnabledProp:gmscb");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
        
        //ensure value is saved correctly
        assertEquals("12345", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:gmsMulticastPort:gmsMulticastPort"));
        assertEquals("123.234.456.88", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:gmsMulticastAddress:gmsMulticastAddress"));
        assertEquals("${ABCDE}", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:GmsBindInterfaceAddress:GmsBindInterfaceAddress"));
        assertEquals("off", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:gmsEnabledProp:gmscb"));
        clickAndWait("treeForm:tree:clusterTreeNode:clusterTreeNode_link", TRIGGER_CLUSTER_PAGE);
        deleteRow("propertyForm:clustersTable:topActionsGroup1:button1", "propertyForm:clustersTable", clusterName);
    }


    @Test
    public void testMultiDeleteClusters() {
        String clusterName1 = "cluster" + generateRandomString();
        String clusterName2 = "cluster" + generateRandomString();

        createCluster(clusterName1, null);
        createCluster(clusterName2, null);
        selenium.click("propertyForm:clustersTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
        //selenium.chooseOkOnNextConfirmation();
        selenium.click("propertyForm:clustersTable:topActionsGroup1:button1");
        if (selenium.isConfirmationPresent()) {
            selenium.getConfirmation();
        }
        waitForCondition("document.getElementById('propertyForm:clustersTable:topActionsGroup1:button1').value != 'Processing...'", 300000);
        sleep(10000);
        assertFalse(selenium.isTextPresent(clusterName1));
        assertFalse(selenium.isTextPresent(clusterName2));

    }

    @Test
    public void testClusterInstancesTab(){
        String clusterName = "cluster" + generateRandomString();
        String instanceName = "instanceName" + generateRandomString();
        String instanceName2 = "instanceName" + generateRandomString();

        createCluster(clusterName,  instanceName);
        assertTrue(selenium.isTextPresent(clusterName));
        clickAndWait( getLinkIdByLinkText("propertyForm:clustersTable", clusterName), TRIGGER_CLUSTER_GENERAL_PAGE) ;
        clickAndWait("propertyForm:clusterTabs:clusterInst", TRIGGER_CLUSTER_INSTANCES_PAGE);
        assertTrue(selenium.isTextPresent(instanceName));

        clickAndWait("propertyForm:instancesTable:topActionsGroup1:newButton", TRIGGER_CLUSTER_INSTANCE_NEW_PAGE  );
        selenium.type("propertyForm:propertySheet:propertSectionTextField:NameTextProp:NameText", instanceName2);
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_CLUSTER_INSTANCES_PAGE );
        assertTrue(selenium.isTextPresent(instanceName2));

        clickAndWait("treeForm:tree:clusterTreeNode:clusterTreeNode_link", TRIGGER_CLUSTER_PAGE);
        deleteRow("propertyForm:clustersTable:topActionsGroup1:button1", "propertyForm:clustersTable", clusterName);
    }


    private void createCluster(String clusterName,  String instanceName){
        clickAndWait("treeForm:tree:clusterTreeNode:clusterTreeNode_link", TRIGGER_CLUSTER_PAGE);
	clickAndWait("propertyForm:clustersTable:topActionsGroup1:newButton",  TRIGGER_NEW_PAGE);
	selenium.type("propertyForm:propertySheet:propertSectionTextField:NameTextProp:NameText", clusterName);
        if (instanceName!=null && !instanceName.equals("")){
            addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton", "Server Instances to be Created");
            selenium.type("propertyForm:basicTable:rowGroup1:0:col2:name", instanceName);
        }
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_CLUSTER_PAGE);

    }

}

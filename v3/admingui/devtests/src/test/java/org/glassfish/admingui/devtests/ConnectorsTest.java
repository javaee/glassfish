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

import static org.junit.Assert.assertTrue;

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
                TRIGGER_EDIT_CONNECTOR_RESOURCE);

        // Enable resource
        testEnableButton(testConnector,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CONNECTOR_RESOURCE,
                TRIGGER_EDIT_CONNECTOR_RESOURCE);

        // Delete connector resource
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", testConnector);

        // Delete connector connection pool
        clickAndWait("treeForm:tree:resources:Connectors:connectorConnectionPools:connectorConnectionPools_link", TRIGGER_CONNECTOR_CONNECTION_POOLS);

        deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", testPool);
    }
}
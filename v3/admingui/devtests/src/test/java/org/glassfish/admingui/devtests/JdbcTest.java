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
import static org.junit.Assert.assertTrue;

public class JdbcTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_JDBC_CONNECTION_POOLS = "To store, organize, and retrieve data, most applications use relational databases.";
    private static final String TRIGGER_JDBC_RESOURCES = "JDBC resources provide applications with a means to connect to a database.";
    private static final String TRIGGER_EDIT_JDBC_RESOURCE = "Edit JDBC Resource";
    private static final String TRIGGER_NEW_JDBC_CONNECTION_POOL_STEP_1 = "New JDBC Connection Pool (Step 1 of 2)";
    private static final String TRIGGER_NEW_JDBC_CONNECTION_POOL_STEP_2 = "New JDBC Connection Pool (Step 2 of 2)";
    private static final String TRIGGER_NEW_JDBC_RESOURCE = "New JDBC Resource";

    @Test
    public void testPoolPing() {
        clickAndWait("treeForm:tree:resources:JDBC:connectionPoolResources:amxppdomainresourcestypejdbc-connection-poolname__TimerPool:link", "Edit JDBC Connection Pool");
        clickAndWait("propertyForm:propertyContentPage:ping", "Ping Succeeded");
    }

    @Test
    public void testCreatingConnectionPool() {
        final String poolName = generateRandomString();
        final String description = "devtest test connection pool - " + poolName;

        clickAndWait("treeForm:tree:resources:JDBC:connectionPoolResources:connectionPoolResources_link", TRIGGER_JDBC_CONNECTION_POOLS);
        clickAndWait("propertyForm:poolTable:topActionsGroup1:newButton", TRIGGER_NEW_JDBC_CONNECTION_POOL_STEP_1);

        selenium.type("propertyForm:propertyContentPage:propertySheet:generalPropertySheet:jndiProp:name", poolName);
        selenium.select("propertyForm:propertyContentPage:propertySheet:generalPropertySheet:resTypeProp:resType", "label=javax.sql.DataSource");
        selenium.select("propertyForm:propertyContentPage:propertySheet:generalPropertySheet:dbProp:db", "label=Derby");
        clickAndWait("propertyForm:propertyContentPage:topButtons:nextButton", TRIGGER_NEW_JDBC_CONNECTION_POOL_STEP_2);

        selenium.type("form2:sheet:generalSheet:descProp:desc", description);
        clickAndWait("form2:propertyContentPage:topButtons:finishButton", TRIGGER_JDBC_CONNECTION_POOLS);
        assertTrue(selenium.isTextPresent(poolName) && selenium.isTextPresent(description));

        deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", poolName);
    }

    @Test
    public void testJdbcResources() {
        final String jndiName = generateRandomString();
        final String description = "devtest test jdbc resource - " + jndiName;

        clickAndWait("treeForm:tree:resources:JDBC:jdbcResources:jdbcResources_link", TRIGGER_JDBC_RESOURCES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_JDBC_RESOURCE);

        selenium.type("form:propertySheet:propertSectionTextField:nameNew:name", jndiName);
        selenium.type("form:propertySheet:propertSectionTextField:descProp:desc", description);
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_JDBC_RESOURCES);

        assertTrue(selenium.isTextPresent(jndiName));
        assertTrue(selenium.isTextPresent(description));

        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", jndiName), TRIGGER_EDIT_JDBC_RESOURCE);

        assertTableRowCount("propertyForm:basicTable", count);
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_JDBC_RESOURCES);

        testDisableButton(jndiName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_JDBC_RESOURCES,
                TRIGGER_EDIT_JDBC_RESOURCE);
        testEnableButton(jndiName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_JDBC_RESOURCES,
                TRIGGER_EDIT_JDBC_RESOURCE);

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", jndiName);
    }
}
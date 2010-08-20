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

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 10, 2010
 * Time: 3:48:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebContainerTest extends BaseSeleniumTestClass {
    private static final String TAB_GENERAL_PROPERTIES = "General Properties";
    private static final String TAB_SESSION_PROPERTIES = "Maximum number of seconds";
    private static final String TAB_MANAGER_PROPERTIES = "Number of seconds until";
    private static final String TAB_STORE_PROPERTIES = "Absolute or relative pathname";

    @Test
    public void testGeneralTab() {
        final String property = "property"+generateRandomString();
        final String value = generateRandomString();
        final String description = "Description for " + property;
        
		clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link", TAB_GENERAL_PROPERTIES);
        
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", property);
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", value);
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", description);
		clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

		clickAndWait("form1:webContainerTabs:sessionTab", TAB_SESSION_PROPERTIES);
		clickAndWait("form1:webContainerTabs:general", TAB_GENERAL_PROPERTIES);

        assertTableRowCount("form1:basicTable", count);
    }

    @Test
    public void testSessionProperties() {
        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link", TAB_GENERAL_PROPERTIES);
        clickAndWait("form1:webContainerTabs:sessionTab", TAB_SESSION_PROPERTIES);

        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");

        selenium.type("form1:sessionPropSheet:sessionPropSheetSection:SessionTimeoutProp:SessionTimeout", "300");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        clickAndWait("form1:webContainerTabs:general", TAB_GENERAL_PROPERTIES);

        clickAndWait("form1:webContainerTabs:sessionTab", TAB_SESSION_PROPERTIES);

        assertTableRowCount("form1:basicTable", count);
        assertEquals("300", selenium.getValue("form1:sessionPropSheet:sessionPropSheetSection:SessionTimeoutProp:SessionTimeout"));

        selenium.type("form1:sessionPropSheet:sessionPropSheetSection:SessionTimeoutProp:SessionTimeout", "300");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
    }

    @Test
    public void testManagerProperties() {
        final String reapInterval = Integer.toString(generateRandomNumber(100));
        final String maxSessions = Integer.toString(generateRandomNumber(1000));
        final String sessFileName = generateRandomString();

        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link", TAB_GENERAL_PROPERTIES);

        clickAndWait("form1:webContainerTabs:managerTab", TAB_MANAGER_PROPERTIES);

        selenium.type("form1:managerPropSheet:managerPropSheetSection:ReapIntervalProp:ReapInterval", reapInterval);
        selenium.type("form1:managerPropSheet:managerPropSheetSection:MaxSessionsProp:MaxSessions", maxSessions);
        selenium.type("form1:managerPropSheet:managerPropSheetSection:SessFileNameProp:SessFileName", sessFileName);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");
        
        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        clickAndWait("form1:webContainerTabs:sessionTab", TAB_SESSION_PROPERTIES);
        clickAndWait("form1:webContainerTabs:managerTab", TAB_MANAGER_PROPERTIES);

        assertEquals(reapInterval, selenium.getValue("form1:managerPropSheet:managerPropSheetSection:ReapIntervalProp:ReapInterval"));
        assertEquals(maxSessions, selenium.getValue("form1:managerPropSheet:managerPropSheetSection:MaxSessionsProp:MaxSessions"));
        assertEquals(sessFileName, selenium.getValue("form1:managerPropSheet:managerPropSheetSection:SessFileNameProp:SessFileName"));
        assertTableRowCount("form1:basicTable", count);
    }

    @Test
    public void testStoreProperties() {
        final String directory = generateRandomString();

        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link", TAB_GENERAL_PROPERTIES);

        clickAndWait("form1:webContainerTabs:storeTab", TAB_STORE_PROPERTIES);

        selenium.type("form1:storePropSheet:storePropSheetSection:DirectoryProp:Directory", directory);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        clickAndWait("form1:webContainerTabs:managerTab", TAB_MANAGER_PROPERTIES);
        clickAndWait("form1:webContainerTabs:storeTab", TAB_STORE_PROPERTIES);
        assertEquals(directory, selenium.getValue("form1:storePropSheet:storePropSheetSection:DirectoryProp:Directory"));
        assertTableRowCount("form1:basicTable", count);

        selenium.type("form1:storePropSheet:storePropSheetSection:DirectoryProp:Directory", "");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
    }
}

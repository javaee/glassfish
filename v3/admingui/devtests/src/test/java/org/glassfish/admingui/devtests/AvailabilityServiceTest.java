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

public class AvailabilityServiceTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_CONFIGURATION = "Configuration";
    private static final String TRIGGER_AVAILABILTY_SERVICE = "Availability for the server instance";
    private static final String TRIGGER_WEB_AVAILABILTY = "Availability for the web container";
    private static final String TRIGGER_EJB_AVAILABILTY = "Availability for the EJB container";
    private static final String TRIGGER_JMS_AVAILABILTY = "Availability for JMS";
    private static final String TRIGGER_SUCCESS_MSG = "New values successfully saved";

    @Test
    public void testAvailabilityService() {
        final String haAgentPort = Integer.toString(generateRandomNumber(65535));
        clickAndWait("treeForm:tree:configurations:default-config:default-config_turner:default-config_turner_image", TRIGGER_CONFIGURATION);
        clickAndWait("treeForm:tree:configurations:default-config:availabilityService:availabilityService_link", TRIGGER_AVAILABILTY_SERVICE);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:HAAgentPortProp:HAAgentPort", haAgentPort);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_SUCCESS_MSG);
        assertEquals(haAgentPort, selenium.getValue("propertyForm:propertySheet:propertSectionTextField:HAAgentPortProp:HAAgentPort"));

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "a");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "b");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", "c");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_SUCCESS_MSG);
        assertTableRowCount("propertyForm:basicTable", count);
    }

    @Test
    public void testWebContainerAvailability() {
        final String httpSessionStore = "jdbc/hastore" + Integer.toString(generateRandomNumber(100));
        clickAndWait("treeForm:tree:configurations:default-config:default-config_turner:default-config_turner_image", TRIGGER_CONFIGURATION);
        clickAndWait("treeForm:tree:configurations:default-config:availabilityService:availabilityService_link", TRIGGER_AVAILABILTY_SERVICE);
        clickAndWait("propertyForm:availabilityTabs:webAvailabilityTab", TRIGGER_WEB_AVAILABILTY);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:HttpSessionStoreProp:HttpSessionStore", httpSessionStore);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_SUCCESS_MSG);
        assertEquals(httpSessionStore, selenium.getValue("propertyForm:propertySheet:propertSectionTextField:HttpSessionStoreProp:HttpSessionStore"));

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "a");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "b");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", "c");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_SUCCESS_MSG);
        assertTableRowCount("propertyForm:basicTable", count);
    }

    @Test
    public void testEjbContainerAvailability() {
        final String httpSessionStore = "jdbc/hastore" + Integer.toString(generateRandomNumber(100));
        clickAndWait("treeForm:tree:configurations:default-config:default-config_turner:default-config_turner_image", TRIGGER_CONFIGURATION);
        clickAndWait("treeForm:tree:configurations:default-config:availabilityService:availabilityService_link", TRIGGER_AVAILABILTY_SERVICE);
        clickAndWait("propertyForm:availabilityTabs:ejbAvailabilityTab", TRIGGER_EJB_AVAILABILTY);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:SFSBStoreNameProp:SFSBStoreName", httpSessionStore);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_SUCCESS_MSG);
        assertEquals(httpSessionStore, selenium.getValue("propertyForm:propertySheet:propertSectionTextField:SFSBStoreNameProp:SFSBStoreName"));

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "a");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "b");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", "c");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_SUCCESS_MSG);
        assertTableRowCount("propertyForm:basicTable", count);

        clickAndWait("propertyForm:propertyContentPage:loadDefaultsButton", TRIGGER_EJB_AVAILABILTY);
        assertEquals("ha", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:HAPersistenceTypeProp:HAPersistenceType"));
    }

    @Test
    public void testJMSAvailability() {
        final String storePoolName = "test" + Integer.toString(generateRandomNumber(100));
        clickAndWait("treeForm:tree:configurations:default-config:default-config_turner:default-config_turner_image", TRIGGER_CONFIGURATION);
        clickAndWait("treeForm:tree:configurations:default-config:availabilityService:availabilityService_link", TRIGGER_AVAILABILTY_SERVICE);
        clickAndWait("propertyForm:availabilityTabs:jmsAvailabilityTab", TRIGGER_JMS_AVAILABILTY);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:StorePoolNameProp:StorePoolName", storePoolName);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_SUCCESS_MSG);
        assertEquals(storePoolName, selenium.getValue("propertyForm:propertySheet:propertSectionTextField:StorePoolNameProp:StorePoolName"));

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "a");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "b");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", "c");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_SUCCESS_MSG);
        assertTableRowCount("propertyForm:basicTable", count);
    }
}
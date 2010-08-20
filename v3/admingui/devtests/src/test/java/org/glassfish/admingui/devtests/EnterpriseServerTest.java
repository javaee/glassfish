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


public class EnterpriseServerTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_ADVANCED_APPLICATIONS_CONFIGURATION = "Enable reloading so that changes to deployed applications are detected and the modified classes reloaded. Also enable and configure automatic deployment of applications. Click Add Property to specify additional settings.";
    private static final String TRIGGER_GENERAL_INFORMATION = "General Information";
    private static final String TRIGGER_ADVANCED_DOMAIN_ATTRIBUTES = "Directory from which applications are deployed";
    private static final String TRIGGER_SYSTEM_PROPERTIES = "A system property defines a common value for a setting at the server level. You can refer to a system property in a text field by enclosing it in a dollar sign and curly braces.";

    @Test
    public void testAdvancedApplicationsConfiguration() {
        final String property = generateRandomString();
        final String value = property + "value";
        final String description = property + "description";

        clickAndWait("treeForm:tree:applicationServer:applicationServer_link", TRIGGER_GENERAL_INFORMATION);
        clickAndWait("propertyForm:serverInstTabs:advanced", TRIGGER_ADVANCED_APPLICATIONS_CONFIGURATION);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:reloadIntervalProp:ReloadInterval", "5");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:AdminTimeoutProp:AdminTimeout", "30");

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", property);
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", value);
        selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", description);

        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        clickAndWait("propertyForm:serverInstTabs:advanced:domainAttrs", TRIGGER_ADVANCED_DOMAIN_ATTRIBUTES);
        clickAndWait("propertyForm:serverInstTabs:advanced:appConfig", TRIGGER_ADVANCED_APPLICATIONS_CONFIGURATION);

        assertEquals("5", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:reloadIntervalProp:ReloadInterval"));
        assertEquals("30", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:AdminTimeoutProp:AdminTimeout"));
        
        assertTableRowCount("propertyForm:basicTable", count);
    }

    @Test
    public void testAdvancedDomainAttributes() {
        clickAndWait("treeForm:tree:applicationServer:applicationServer_link", TRIGGER_GENERAL_INFORMATION);
        clickAndWait("propertyForm:serverInstTabs:advanced", TRIGGER_ADVANCED_APPLICATIONS_CONFIGURATION);
        clickAndWait("propertyForm:serverInstTabs:advanced:domainAttrs", TRIGGER_ADVANCED_DOMAIN_ATTRIBUTES);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:localeProp:Locale", "fr");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);

        clickAndWait("propertyForm:serverInstTabs:advanced:appConfig", TRIGGER_ADVANCED_APPLICATIONS_CONFIGURATION);
        clickAndWait("propertyForm:serverInstTabs:advanced:domainAttrs", TRIGGER_ADVANCED_DOMAIN_ATTRIBUTES);

        assertEquals("fr", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:localeProp:Locale"));
        selenium.type("propertyForm:propertySheet:propertSectionTextField:localeProp:Locale", "");
        selenium.click("propertyForm:propertyContentPage:topButtons:saveButton");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
    }

    @Test
    public void testSystemProperties() {
        final String property = generateRandomString();
        final String value = property + "value";
        final String description = property + "description";

        clickAndWait("treeForm:tree:applicationServer:applicationServer_link", TRIGGER_GENERAL_INFORMATION);
        clickAndWait("propertyForm:serverInstTabs:token", TRIGGER_SYSTEM_PROPERTIES);

        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", property);
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", value);
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", description);

        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
        clickAndWait("form1:serverInstTabs:general", TRIGGER_GENERAL_INFORMATION);
        clickAndWait("propertyForm:serverInstTabs:token", TRIGGER_SYSTEM_PROPERTIES);

        assertTableRowCount("form1:basicTable", count);
    }
}

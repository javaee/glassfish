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

public class SecurityTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_SECURITY_REALMS = "Manage security realms.";
    private static final String TRIGGER_EDIT_REALM = "Edit Realm";
    private static final String TRIGGER_FILE_USERS = "File Users";
    private static final String TRIGGER_NEW_FILE_REALM_USER = "New File Realm User";
    private static final String TRIGGER_AUDIT_MODULES = "com.sun.enterprise.security.Audit";
    //"Use audit modules to develop an audit trail of all authentication and authorization decisions.";
    private static final String TRIGGER_NEW_AUDIT_MODULE = "New Audit Module";
    private static final String TRIGGER_EDIT_AUDIT_MODULE = "Edit Audit Module";
    private static final String TRIGGER_JACC_PROVIDERS = "Manage Java Authorization Contract for Containers (JACC) providers to define an interface for pluggable authorization providers.";
    private static final String TRIGGER_NEW_JACC_PROVIDER = "New JACC Provider";
    private static final String TRIGGER_EDIT_JACC_PROVIDER = "Edit JACC Provider";
    private static final String TRIGGER_MESSAGE_SECURITY_CONFIGURATIONS = "Message Security Configurations";
    private static final String TRIGGER_NEW_MESSAGE_SECURITY_CONFIGURATION = "New Message Security Configuration";
    private static final String TRIGGER_EDIT_MESSAGE_SECURITY_CONFIGURATION = "Edit Message Security Configuration";
    private static final String TRIGGER_EDIT_PROVIDER_CONFIGURATION = "Edit Provider Configuration";
    private static final String ADMIN_PWD_DOMAIN_ATTRIBUTES = "Domain Attributes";
    private static final String ADMIN_PWD_NEW_ADMINPWD = "New Administrator Password";
    private static final String ADMIN_PWD_SUCCESS = "New values successfully saved";

    @Test
    public void testNewSecurityRealm() {
        final String realmName = "TestRealm" + generateRandomString();
        final String contextName = "Context" + generateRandomString();

        clickAndWait("treeForm:tree:configurations:server-config:security:realms:realms_link", TRIGGER_SECURITY_REALMS);
        clickAndWait("propertyForm:realmsTable:topActionsGroup1:newButton", "Create a new security realm.");
        selenium.type("form1:propertySheet:propertySectionTextField:NameTextProp:NameText", realmName);
        selenium.select("form1:propertySheet:propertySectionTextField:cp:Classname", "label=com.sun.enterprise.security.auth.realm.file.FileRealm");
        selenium.type("form1:fileSection:jaax:jaax", contextName);
        selenium.type("form1:fileSection:keyFile:keyFile", "${com.sun.aas.instanceRoot}/config/testfile");
        clickAndWait("form1:propertyContentPage:topButtons:newButton", TRIGGER_SECURITY_REALMS);
        assertTrue(selenium.isTextPresent(realmName));

        deleteRow("propertyForm:realmsTable:topActionsGroup1:button1", "propertyForm:realmsTable", realmName);
    }

    @Test
    public void testAddUserToFileRealm() {
        final String userId = "user" + generateRandomString();
        final String password = "password" + generateRandomString();

        clickAndWait("treeForm:tree:configurations:server-config:security:realms:realms_link", TRIGGER_SECURITY_REALMS);
        clickAndWait(getLinkIdByLinkText("propertyForm:realmsTable", "file"), TRIGGER_EDIT_REALM);

        clickAndWait("form1:propertyContentPage:manageUsersButton", TRIGGER_FILE_USERS);
        clickAndWait("propertyForm:users:topActionsGroup1:newButton", TRIGGER_NEW_FILE_REALM_USER);

        selenium.type("propertyForm:propertySheet:propertSectionTextField:userIdProp:UserId", userId);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:newPasswordProp:NewPassword", password);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:confirmPasswordProp:ConfirmPassword", password);
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_FILE_USERS);
        assertTrue(selenium.isTextPresent(userId));

        deleteRow("propertyForm:users:topActionsGroup1:button1", "propertyForm:users", userId);
    }

    @Test
    public void testAddAuditModule() {
        final String auditModuleName = "auditModule" + generateRandomString();
        final String className = "org.glassfish.NonexistentModule";

        clickAndWait("treeForm:tree:configurations:server-config:security:auditModules:auditModules_link", TRIGGER_AUDIT_MODULES);
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_AUDIT_MODULE);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:IdTextProp:IdText", auditModuleName);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:classNameProp:ClassName", className);
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "property");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", "description");

        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_AUDIT_MODULES);
        assertTrue(selenium.isTextPresent(auditModuleName));

        clickAndWait(getLinkIdByLinkText("propertyForm:configs", auditModuleName), TRIGGER_EDIT_AUDIT_MODULE);
        assertTableRowCount("propertyForm:basicTable", count);

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_AUDIT_MODULES);

        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", auditModuleName);
    }

    @Test
    public void testAddJaccModule() {
        final String providerName = "testJaccProvider" + generateRandomString();
        final String policyConfig = "com.example.Foo";
        final String policyProvider = "com.example.Foo";

        clickAndWait("treeForm:tree:configurations:server-config:security:jaccProviders:jaccProviders_link", TRIGGER_JACC_PROVIDERS);
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_JACC_PROVIDER);

        selenium.type("propertyForm:propertySheet:propertSectionTextField:IdTextProp:IdText", providerName);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:policyConfigProp:PolicyConfig", policyConfig);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:policyProviderProp:PolicyProvider", policyProvider);

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "property");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", "description");

        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_JACC_PROVIDERS);
        assertTrue(selenium.isTextPresent(providerName));

        clickAndWait(getLinkIdByLinkText("propertyForm:configs", providerName), TRIGGER_EDIT_JACC_PROVIDER);
        assertEquals(policyConfig, selenium.getValue("propertyForm:propertySheet:propertSectionTextField:policyConfigProp:PolicyConfig"));
        assertEquals(policyProvider, selenium.getValue("propertyForm:propertySheet:propertSectionTextField:policyProviderProp:PolicyProvider"));

        assertTableRowCount("propertyForm:basicTable", count);
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_JACC_PROVIDERS);

        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", providerName);
    }

    @Test
    public void testAddMessageSecurityConfiguration() {
        final String providerName = "provider" + generateRandomString();
        final String providerType = selectRandomItem(new String[] {"client", "server", "client-server"});
        final String className = "com.example.Foo";
        final String LAYER_NAME = "HttpServlet";

        clickAndWait("treeForm:tree:configurations:server-config:security:messageSecurity:messageSecurity_link", TRIGGER_MESSAGE_SECURITY_CONFIGURATIONS);

        // Clean up, just in case...
        if (selenium.isTextPresent(LAYER_NAME)) {
            deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", LAYER_NAME);            
        }

        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_MESSAGE_SECURITY_CONFIGURATION);
        selenium.type("propertyForm:propertySheet:providerConfSection:ProviderIdTextProp:ProviderIdText", providerName);
        selenium.select("propertyForm:propertySheet:providerConfSection:ProviderTypeProp:ProviderType", "label="+providerType);
        selenium.type("propertyForm:propertySheet:providerConfSection:ClassNameProp:ClassName", className);
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", "property");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", "description");

        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_MESSAGE_SECURITY_CONFIGURATIONS);
        assertTrue(selenium.isTextPresent(LAYER_NAME));

        clickAndWait(getLinkIdByLinkText("propertyForm:configs", LAYER_NAME), TRIGGER_EDIT_MESSAGE_SECURITY_CONFIGURATION);
        clickAndWait("propertyForm:msgSecurityTabs:providers", "Provider Configurations");
        clickAndWait(getLinkIdByLinkText("propertyForm:configs", providerName), TRIGGER_EDIT_PROVIDER_CONFIGURATION);
        
        assertEquals(providerType, selenium.getValue("propertyForm:propertySheet:providerConfSection:ProviderTypeProp:ProviderType"));
        assertEquals(className, selenium.getValue("propertyForm:propertySheet:providerConfSection:ClassNameProp:ClassName"));
        assertTableRowCount("propertyForm:basicTable", count);

        clickAndWait("treeForm:tree:configurations:server-config:security:messageSecurity:messageSecurity_link", TRIGGER_MESSAGE_SECURITY_CONFIGURATIONS);
        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", LAYER_NAME);
    }

    @Test
    public void testNewAdminPassword() {
        final String userPassword = "admin" + generateRandomString();

        clickAndWait("treeForm:tree:nodes:nodes_link", ADMIN_PWD_DOMAIN_ATTRIBUTES);
        clickAndWait("propertyForm:domainTabs:adminPassword", ADMIN_PWD_NEW_ADMINPWD);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:newPasswordProp:NewPassword", userPassword);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:confirmPasswordProp:ConfirmPassword", userPassword);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", ADMIN_PWD_SUCCESS);
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 23, 2010
 * Time: 5:00:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class SecurityTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_SECURITY_REALMS = "Manage security realms.";
    private static final String TRIGGER_EDIT_REALM = "Edit Realm";
    private static final String TRIGGER_FILE_USERS = "File Users";
    private static final String TRIGGER_NEW_FILE_REALM_USER = "New File Realm User";
    private static final String TRIGGER_AUDIT_MODULES = "com.sun.enterprise.security.Audit"; 
            //"Use audit modules to develop an audit trail of all authentication and authorization decisions.";
    private static final String TRIGGER_NEW_AUDIT_MODULE = "New Audit Module";
    private static final String TRIGGER_EDIT_AUDIT_MODULE = "Edit Audit Module";

    @Test
    public void testNewSecurityRealm() {
        final String realmName = "TestRealm"+generateRandomString();
        final String contextName = "Context"+generateRandomString();

        clickAndWait("treeForm:tree:configuration:security:realms:realms_link", TRIGGER_SECURITY_REALMS);
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
        final String userId = "user"+generateRandomString();
        final String password = "password"+generateRandomString();

        clickAndWait("treeForm:tree:configuration:security:realms:realms_link", TRIGGER_SECURITY_REALMS);
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
        final String auditModuleName = "auditModule"+generateRandomString();
        final String className = "org.glassfish.NonexistentModule";

        clickAndWait("treeForm:tree:configuration:security:auditModules:auditModules_link", TRIGGER_AUDIT_MODULES);
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
}
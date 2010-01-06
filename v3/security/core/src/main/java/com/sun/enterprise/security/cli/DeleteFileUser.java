/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.security.cli;

import java.util.List;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.common.Util;
import org.jvnet.hk2.config.types.Property;

/**
 * Delete File User Command
 * Usage: delete-file-user [--terse=false] [--echo=false] [--interactive=true] 
 * [--host localhost] [--port 4848|4849] [--secure | -s] [--user admin_user]
 * [--passwordfile file_name] [--authrealmname authrealm_name] 
 * [--target target(Default server)] username
 *
 * @author Nandini Ektare
 */

@Service(name="delete-file-user")
@Scoped(PerLookup.class)
@I18n("delete.file.user")
public class DeleteFileUser implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = 
        new LocalStringManagerImpl(DeleteFileUser.class);    

    @Param(name="authrealmname", optional=true)
    String authRealmName;
    
    @Param(optional=true)
    String target;

    @Param(name="username", primary=true)
    String userName;

    @Inject
    Configs configs;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        
        final ActionReport report = context.getActionReport();

        List <Config> configList = configs.getConfig();
        Config config = configList.get(0);
        SecurityService securityService = config.getSecurityService();

        // ensure we have the file authrealm
        if (authRealmName == null) 
            authRealmName = securityService.getDefaultRealm();        

        AuthRealm fileAuthRealm = null;        
        for (AuthRealm authRealm : securityService.getAuthRealm()) {
            if (authRealm.getName().equals(authRealmName))                 
                fileAuthRealm = authRealm;            
        }        
        if (fileAuthRealm == null) {
            report.setMessage(localStrings.getLocalString(
                "delete.file.user.filerealmnotfound",
                "File realm {0} does not exist", authRealmName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;                                            
        }
        
        // Get FileRealm class name, match it with what is expected.
        String fileRealmClassName = fileAuthRealm.getClassname();
        
        // Report error if provided impl is not the one expected
        if (fileRealmClassName != null && 
            !fileRealmClassName.equals(
                "com.sun.enterprise.security.auth.realm.file.FileRealm")) {
            report.setMessage(
                localStrings.getLocalString(
                    "delete.file.user.realmnotsupported",
                    "Configured file realm {0} is not supported.", 
                    fileRealmClassName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;                
        }

        // ensure we have the file associated with the authrealm
        String keyFile = null;
        for (Property fileProp : fileAuthRealm.getProperty()) {
            if (fileProp.getName().equals("file"))
                keyFile = fileProp.getValue();
        }
        if (keyFile == null) {
            report.setMessage(
                localStrings.getLocalString("delete.file.user.keyfilenotfound",
                "There is no physical file associated with this file realm {0} ", 
                authRealmName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;                                            
        }
        
        // We have the right impl so let's try to remove one 
        try {
            FileRealm fr = new FileRealm(keyFile);
            try {
                fr.removeUser(userName);
                //fr.writeKeyFile(keyFile);
                if (Util.isEmbeddedServer()) {
                    fr.writeKeyFile(Util.writeConfigFileToTempDir(keyFile).getAbsolutePath());
                } else {
                    fr.writeKeyFile(keyFile);
                }
                CreateFileUser.refreshRealm(authRealmName);
            } catch (NoSuchUserException e) {
                report.setMessage(
                        localStrings.getLocalString("delete.file.user.usernotfound",
                        "There is no such existing user {0} in the file realm {1}.",
                        userName, authRealmName) + "  " + e.getLocalizedMessage());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setFailureCause(e);
            } catch (Exception e) {
                e.printStackTrace();
                report.setMessage(
                        localStrings.getLocalString("delete.file.user.userdeletefailed",
                        "Removing User {0} from file realm {1} failed",
                        userName, authRealmName) + "  " + e.getLocalizedMessage());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setFailureCause(e);
            }

        } catch (BadRealmException e) {
            report.setMessage(
                    localStrings.getLocalString(
                    "delete.file.user.realmcorrupted",
                    "Configured file realm {0} is corrupted.", authRealmName) +
                    "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        } catch (NoSuchRealmException e) {
            report.setMessage(
                    localStrings.getLocalString(
                    "delete.file.user.realmnotsupported",
                    "Configured file realm {0} is not supported.", authRealmName) +
                    "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}

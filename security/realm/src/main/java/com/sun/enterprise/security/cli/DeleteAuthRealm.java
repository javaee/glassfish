/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.AuthRealm;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.beans.PropertyVetoException;

/**
 * Delete Auth Realm Command
 * 
* Usage: delete-auth-realm [--terse=false] [--echo=false] 
 *        [--interactive=true] [--host localhost] [--port 4848|4849] 
 *        [--secure | -s] [--user admin_user] [--passwordfile file_name] 
 *        [--target target(Default server)] auth_realm_name
 *
 * @author Nandini Ektare
 */
@Service(name="delete-auth-realm")
@Scoped(PerLookup.class)
@I18n("delete.auth.realm")
public class DeleteAuthRealm implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = 
        new LocalStringManagerImpl(DeleteAuthRealm.class);

    @Param(name="authrealmname", primary=true)
    String authRealmName;
 
    @Inject
    SecurityService securityService;

    AuthRealm authRealm = null;
    
    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        try {            
            for (AuthRealm realm : securityService.getAuthRealm()) {
                if (realm.getName().equals(authRealmName)) {
                    authRealm = realm;
                }
            }

            if (authRealm == null) {
                report.setMessage(localStrings.getLocalString(
                    "delete.auth.realm.notfound", 
                    "Authrealm named {0} not found", authRealmName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            ConfigSupport.apply(new SingleConfigCode<SecurityService>() {
                public Object run(SecurityService param) 
                throws PropertyVetoException, TransactionFailure {
                    
                    param.getAuthRealm().remove(authRealm);
                    return null;
                }
            }, securityService);
        } catch(TransactionFailure e) {
            report.setMessage(localStrings.getLocalString(
                "delete.auth.realm.fail", "Deletion of Authrealm {0} failed", 
                authRealmName) + "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}

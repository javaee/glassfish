/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.v3.admin;

import java.beans.PropertyVetoException;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
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

/**
 * Delete System Property Command
 * 
 * Removes one system property of the domain, configuration, cluster, or server 
 * instance, at a time
 * 
 * Usage: delete-system-property [--terse=false] [--echo=false] [--interactive=true] 
 * [--host localhost] [--port 4848|4849] [--secure|-s=true] [--user admin_user] [
 * --passwordfile file_name] [--target target(Default server)] property_name
 * 
 */
@Service(name="delete-system-property")
@Scoped(PerLookup.class)
@I18n("delete.system.property")
public class DeleteSystemProperty implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteSystemProperty.class);

    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Param(name="property_name", primary=true)
    String propName;
    
    @Inject
    Server[] servers;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        
        try {
            for (final Server server : servers) {
                if (server.getName().equals(target)) {
                    if (ConfigSupport.apply(new SingleConfigCode<Server>() {

                        public Object run(Server param) throws PropertyVetoException, TransactionFailure {

                            for (SystemProperty prop : param.getSystemProperty()) {
                                if (prop.getName().equals(propName)) {
                                    return param.getSystemProperty().remove(prop);
                                }
                            }
                            // not found
                            return null;
                        }
                    }, server) == null) {
                        report.setMessage(localStrings.getLocalString("delete.system.property.doesNotExist",
                        "A system property named {0} does not exist.", propName));
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        return;
                    }
                }
            }
        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("delete.system.property.failed",
                    "System property {0} deletion failed", propName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
            return;
        } catch(Exception e) {
            report.setMessage(localStrings.getLocalString("delete.system.property.failed",
                    "System property {0} deletion failed", propName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}

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
package org.glassfish.admin.cli.resources;

import com.sun.enterprise.config.serverbeans.AdminObjectResource;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.config.serverbeans.CustomResource;
import com.sun.enterprise.config.serverbeans.ExternalJndiResource;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import com.sun.enterprise.config.serverbeans.MailResource;
import com.sun.enterprise.config.serverbeans.Resources;
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
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Create Resource Ref Command
 * 
 */
@Service(name="create-resource-ref")
@Scoped(PerLookup.class)
@I18n("create.resource.ref")
public class CreateResourceRef implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateResourceRef.class);
    
    @Param(optional=true, defaultValue="true")
    Boolean enabled;
    
    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Param(name="reference_name", primary=true)
    String refName;
    
    /* no target support
    @Inject
    Server server;
     */
    
    /* target support */
    @Inject
    Server[] servers;
    
    @Inject
    AdminObjectResource[] adminObjectResources;
    
    @Inject
    ConnectorResource[] connectorResources;
    
    @Inject
    CustomResource[] customResources;
        
    @Inject
    ExternalJndiResource[] externalJndiResources;
    
    @Inject
    JdbcResource[] jdbcResources;
    
    @Inject
    MailResource[] mailResources;
    
    @Inject
    Resources resources;
    
    
    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        
        // check if the resource exists before creating a reference
        if (!isResourceExists()) {
            report.setMessage(localStrings.getLocalString("create.resource.ref.resourceDoesNotExist",
            "Resource {0} does not exist", refName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        
        try {
            for (Server server : servers) {
                if (server.getName().equals(target)) {
                    // ensure we don't already have one of this name
                    if (server.isResourceRefExists( refName)) {
                        report.setMessage(localStrings.getLocalString("create.resource.ref.existsAlready",
                        "Resource ref {0} already exists", refName));
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        return;
                    }
                    
                    // create new ResourceRef as a child of Server
                    server.createResourceRef( enabled.toString(), refName);
                }
            }
        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("create.resource.ref.failed",
                    "Resource ref {0} creation failed", refName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
            return;
        } catch(Exception e) {
            report.setMessage(localStrings.getLocalString("create.resource.ref.failed",
                    "Resource ref {0} creation failed", refName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        report.setActionExitCode(ec);
    }
    
    private boolean isResourceExists() {
        for (AdminObjectResource resource : adminObjectResources) {
            if (resource.getJndiName().equals(refName)) {
                return true;
            }
        }
        for (ConnectorResource resource : connectorResources) {
            if (resource.getJndiName().equals(refName)) {
                return true;
            }
        }
        for (CustomResource resource : customResources) {
            if (resource.getJndiName().equals(refName)) {
                return true;
            }
        }
        for (ExternalJndiResource resource : externalJndiResources) {
            if (resource.getJndiName().equals(refName)) {
                return true;
            }
        }
        for (JdbcResource resource : jdbcResources) {
            if (resource.getJndiName().equals(refName)) {
                return true;
            }
        }
        for (MailResource resource : mailResources) {
            if (resource.getJndiName().equals(refName)) {
                return true;
            }
        }
        return false;
    }
}

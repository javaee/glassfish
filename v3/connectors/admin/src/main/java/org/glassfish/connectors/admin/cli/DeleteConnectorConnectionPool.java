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

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.Cluster;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.resource.common.ResourceStatus;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Delete Connector Connection Pool Command
 * 
 */
@Cluster(RuntimeType.ALL)
@Service(name="delete-connector-connection-pool")
@Scoped(PerLookup.class)
@I18n("delete.connector.connection.pool")
public class DeleteConnectorConnectionPool implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DeleteConnectorConnectionPool.class);

    private @Param(optional=true, obsolete = true)
    String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Param(optional=true, defaultValue="false")
    private Boolean cascade;
    
    @Param(name="poolname", primary=true)
    private String poolname;
    
    @Inject
    private Resources resources;
    
    @Inject
    private Server[] servers;
    
    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        
        if (poolname == null) {
            report.setMessage(localStrings.getLocalString("delete.connector.connection.pool.noJndiName",
                            "No id defined for connector connection pool."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        // ensure we already have this resource
        if(resources.getResourceByName(ConnectorConnectionPool.class, poolname) == null){
            report.setMessage(localStrings.getLocalString("delete.connector.connection.pool.notfound",
                    "A connector connection pool named {0} does not exist.", poolname));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {

            // if cascade=true delete all the resources associated with this pool
            // if cascade=false don't delete this connection pool if a resource is referencing it
            Object obj = deleteAssociatedResources(servers, resources,
                    cascade, poolname);
            if (obj instanceof Integer &&
                    (Integer) obj == ResourceStatus.FAILURE) {
                report.setMessage(localStrings.getLocalString(
                    "delete.connector.connection.pool.pool_in_use",
                    "Some connector resources are referencing connection pool {0}. Use 'cascade' " +
                            "option to delete them as well.", poolname));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            // delete connector connection pool
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    ConnectorConnectionPool cp = (ConnectorConnectionPool)
                            resources.getResourceByName(ConnectorConnectionPool.class, poolname);
                    if(cp != null){
                        return param.getResources().remove(cp);
                    }
                    // not found
                    return null;
                }
            }, resources) == null) {
                report.setMessage(localStrings.getLocalString("delete.connector.connection.pool.notfound",
                                "A connector connection pool named {0} does not exist.", poolname));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

        } catch(TransactionFailure tfe) {
            Logger.getLogger(DeleteConnectorConnectionPool.class.getName()).log(Level.SEVERE,
                    "Something went wrong in delete-connector-connection-pool", tfe);
            report.setMessage(tfe.getMessage() != null ? tfe.getLocalizedMessage() :
                localStrings.getLocalString("delete.connector.connection.pool.fail",
                            "Connector connection pool {0} delete failed ", poolname));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }

        report.setMessage(localStrings.getLocalString("delete.connector.connection.pool.success",
                "Connector connection pool {0} deleted successfully", poolname));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private Object deleteAssociatedResources(final Server[] servers, Resources resources,
            final boolean cascade, final String connPoolId) throws TransactionFailure {

        return ConfigSupport.apply(new SingleConfigCode<Resources>() {
            public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                Resource res = null;
                for (Resource resource : param.getResources()) {
                    if (resource instanceof ConnectorResource) {
                        if (((ConnectorResource)resource).getPoolName().equals(connPoolId)) {
                            if (cascade) {
                                // delete resource-refs
                                deleteResourceRefs(servers, ((ConnectorResource)resource).getJndiName());
                                res = resource;
                                break;
                            } else {
                                return ResourceStatus.FAILURE;
                            }
                        }
                    }
                 }
                 // delete connector-resource
                 if (res != null) {
                     param.getResources().remove(res);
                 }
                 return null;
            }
        }, resources);
    }

    private void deleteResourceRefs(Server[] servers, final String refName)
            throws TransactionFailure {
        for (Server server : servers) {
           server.deleteResourceRef(refName);
        }
    }
}

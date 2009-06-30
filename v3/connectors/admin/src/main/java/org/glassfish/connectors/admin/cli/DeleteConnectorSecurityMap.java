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
package org.glassfish.connectors.admin.cli;

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
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.SecurityMap;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Delete Connector Work Security Map
 *
 */
@Service(name="delete-connector-security-map")
@Scoped(PerLookup.class)
@I18n("delete.connector.security.map")
public class DeleteConnectorSecurityMap extends ConnectorSecurityMap implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DeleteConnectorSecurityMap.class);

    @Param(name="poolname")
    String poolName;

    @Param(name="mapname", primary=true)
    String mapName;

    @Param(optional = true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Inject
    ConnectorConnectionPool[] ccPools;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        // ensure we already have this resource
        if (!isResourceExists()) {
            report.setMessage(localStrings.getLocalString(
                    "delete.connector.security.map.notFound",
                    "A security map named {0} for connector connection pool {1} does not exist.",
                    mapName, poolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            final ConnectorConnectionPool pool = getPool(poolName, ccPools);
            // delete connector-security-map
            ConfigSupport.apply(new SingleConfigCode<ConnectorConnectionPool>() {

                public Object run(ConnectorConnectionPool param) throws PropertyVetoException,
                        TransactionFailure {

                    final List<SecurityMap> securityMaps = param.getSecurityMap();
                    if (securityMaps != null) {
                        for (SecurityMap map : securityMaps) {
                            if (map.getName().equals(mapName)) {
                                param.getSecurityMap().remove(map);
                                break;
                            }
                        }
                    }

                    return param;
                }
            }, pool);
        } catch (TransactionFailure tfe) {
            Logger.getLogger(DeleteConnectorSecurityMap.class.getName()).log(Level.SEVERE,
                    "delete-connector-security-map failed", tfe);
            report.setMessage(localStrings.getLocalString(
                    "delete.connector.security.map.fail",
                    "Unable to delete security map {0} for connector connection pool {1}",
                    mapName, poolName) + " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
            return;
        }
        
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private boolean isResourceExists() {
        for (ConnectorConnectionPool resource : ccPools) {
            if (resource.getName().equals(poolName)) {
                for (SecurityMap sm : resource.getSecurityMap()) {
                    if (sm.getName().equals(mapName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

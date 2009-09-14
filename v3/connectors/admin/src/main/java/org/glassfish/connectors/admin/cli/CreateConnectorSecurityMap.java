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
package org.glassfish.connectors.admin.cli;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.config.serverbeans.BackendPrincipal;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.SecurityMap;

import java.util.*;
import java.beans.PropertyVetoException;

/**
 * Create Connector SecurityMap command
 */
@Service(name="create-connector-security-map")
@Scoped(PerLookup.class)
@I18n("create.connector.security.map")
public class CreateConnectorSecurityMap extends ConnectorSecurityMap implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateConnectorSecurityMap.class);

    @Param(optional = true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Param(name = "poolname")
    String poolName;

    @Param(name = "principals", optional = true)
    List<String> principals;

    @Param(name = "usergroups", optional = true)
    List<String> userGroups;

    @Param(name = "mappedusername")
    String mappedusername;

    @Param(name="mappedpassword", optional=true)
    String mappedpassword; //TODO get this from local command thru REST?

    @Param(name = "mapname", primary = true)
    String securityMapName;

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

        if (securityMapName == null) {
            report.setMessage(localStrings.getLocalString("create.connector.security.map.noSecurityMapName",
                    "No security map name specified"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (principals == null && userGroups == null) {
            report.setMessage(localStrings.getLocalString("create.connector.security.map.noPrincipalsOrGroupsMap",
                    "Either the principal or the user group has to be specified while creating a security map. Both cannot be null."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (principals != null && userGroups != null) {
            report.setMessage(localStrings.getLocalString("create.connector.security.map.specifyPrincipalsOrGroupsMap",
                    "A work-security-map can have either (any number of) group mapping or (any number of) principals mapping but not both. Specify --principalsmap or --groupsmap."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (!doesPoolNameExist(poolName, ccPools)) {
            report.setMessage(localStrings.getLocalString("create.connector.security.map.noSuchPoolFound",
                    "Connector connection pool {0} does not exist. Please specify a valid pool name.", poolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (doesMapNameExist(poolName, securityMapName, ccPools)) {
            report.setMessage(localStrings.getLocalString("create.connector.security.map.duplicate",
                    "A security map named {0} already exists for connector connection pool {1}. Please give a different map name.",
                    securityMapName, poolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        //get all the security maps for this pool.....
        List<SecurityMap> maps = getAllSecurityMapsForPool(poolName, ccPools);

        if (principals != null) {
            for (String principal : principals) {
                if (isPrincipalExisting(principal, maps)) {
                    report.setMessage(localStrings.getLocalString("create.connector.security.map.principal_exists",
                            "The principal {0} already exists in connector connection pool {1}. Please give a different principal name.",
                            principal, poolName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }
        if (userGroups != null) {
            for (String userGroup : userGroups) {
                if (isUserGroupExisting(userGroup, maps)) {
                    report.setMessage(localStrings.getLocalString("create.connector.security.map.usergroup_exists",
                            "The user-group {0} already exists in connector connection pool {1}. Please give a different user-group name.",
                            userGroup, poolName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }

        ConnectorConnectionPool connPool = null;
        for (ConnectorConnectionPool ccp : ccPools) {
            if (ccp.getName().equals(poolName)) {
                connPool = ccp;
            }
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<ConnectorConnectionPool>() {

                public Object run(ConnectorConnectionPool ccp) throws PropertyVetoException, TransactionFailure {

                    List<SecurityMap> securityMaps = ccp.getSecurityMap();

                    SecurityMap newResource = ccp.createChild(SecurityMap.class);
                    newResource.setName(securityMapName);

                    if (principals != null) {
                        for (String p : principals) {
                            newResource.getPrincipal().add(p);
                        }
                    }

                    if (userGroups != null) {
                        for (String u : userGroups) {
                            newResource.getUserGroup().add(u);
                        }
                    }

                    BackendPrincipal backendPrincipal = newResource.createChild(BackendPrincipal.class);
                    backendPrincipal.setUserName(mappedusername);
                    if (mappedpassword != null && !mappedpassword.isEmpty()) {
                        backendPrincipal.setPassword(mappedpassword);
                    }
                    newResource.setBackendPrincipal(backendPrincipal);
                    securityMaps.add(newResource);
                    return newResource;
                }
            }, connPool);

        } catch (TransactionFailure tfe) {
            Object params[] = {securityMapName, poolName};
            report.setMessage(localStrings.getLocalString("create.connector.security.map.fail",
                    "Unable to create connector security map {0} for connector connection pool {1} ", params) +
                    " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    /*private boolean doesPoolNameExist(String poolName) {
        //check if the poolname exists.If it does not then throw an exception.
        boolean doesPoolExist = false;
        if (ccPools != null) {
            for (ConnectorConnectionPool ccp : ccPools) {
                if (ccp.getName().equals(poolName)) {
                    doesPoolExist = true;
                }
            }
        }
        return doesPoolExist;
    }

    private boolean doesMapNameExist(String poolName, String mapname) {
        //check if the mapname exists for the given pool name..
        List<SecurityMap> maps = getAllSecurityMapsForPool(poolName);

        boolean doesMapNameExist = false;
        if (maps != null) {
            for (SecurityMap sm : maps) {
                String name = sm.getName();
                if (name.equals(mapname)) {
                    doesMapNameExist = true;
                }
            }
        }
        return doesMapNameExist;
    }

    private List<SecurityMap> getAllSecurityMapsForPool(String poolName ) {
         List<SecurityMap> securityMaps = null;
         for (ConnectorConnectionPool ccp : ccPools) {
            if (ccp.getName().equals(poolName)) {
                securityMaps = ccp.getSecurityMap();
            }
         }
         return securityMaps;
    }

    private boolean isPrincipalExisting(String principal, List<SecurityMap> maps) {
        boolean exists = false;
        List<String> existingPrincipals = null;

        if (maps != null) {
            for (SecurityMap sm : maps) {
                existingPrincipals = sm.getPrincipal();
                if (existingPrincipals != null && principal != null) {
                    for (String ep : existingPrincipals) {
                        if (ep.equals(principal)) {
                            exists = true;
                            break;
                        }
                    }
                }
            }
        }
        return exists;
    }

    private boolean isUserGroupExisting(String usergroup, List<SecurityMap> maps) {
        boolean exists = false;
        List<String> existingUserGroups = null;
        if (maps != null) {
            for (SecurityMap sm : maps) {
                existingUserGroups = sm.getUserGroup();
                if (existingUserGroups != null && usergroup != null) {
                    for (String eug : existingUserGroups) {
                        if (eug.equals(usergroup)) {
                            exists = true;
                            break;
                        }
                    }
                }
            }
        }
        return exists;
    }*/
}

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

import org.glassfish.resource.common.ResourceStatus;
import org.glassfish.resource.common.ResourceConstants;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.Domain;

import java.util.Properties;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Create Connector Connection Pool Command
 *
 */
@Service(name="create-connector-connection-pool")
@Scoped(PerLookup.class)
@I18n("create.connector.connection.pool")
public class CreateConnectorConnectionPool implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateConnectorConnectionPool.class);

    @Param(name="raname")
    String raname;

    @Param(name="connectiondefinition")
    String connectiondefinition;

    @Param(name="steadypoolsize", optional=true)
    String steadypoolsize = "8";

    @Param(name="maxpoolsize", optional=true)
    String maxpoolsize = "32";

    @Param(name="maxwait", optional=true)
    String maxwait = "60000";

    @Param(name="poolresize", optional=true)
    String poolresize = "2";

    @Param(name="idletimeout", optional=true)
    String idletimeout = "300";

    @Param(name="isconnectvalidatereq", optional=true, defaultValue="false")
    Boolean isconnectvalidatereq;

    @Param(name="failconnection", optional=true, defaultValue="false")
    Boolean failconnection;

    @Param(name="leaktimeout", optional=true)
    String leaktimeout = "0";

    @Param(name="leakreclaim", optional=true, defaultValue="false")
    Boolean leakreclaim;

    @Param(name="creationretryattempts", optional=true)
    String creationretryattempts = "0";

    @Param(name="creationretryinterval", optional=true)
    String creationretryinterval = "10";

    @Param(name="lazyconnectionenlistment", optional=true, defaultValue="false")
    Boolean lazyconnectionenlistment;

    @Param(name="lazyconnectionassociation", optional=true, defaultValue="false")
    Boolean lazyconnectionassociation;

    @Param(name="associatewiththread", optional=true, defaultValue="false")
    Boolean associatewiththread;

    @Param(name="matchconnections", optional=true, defaultValue="false")
    Boolean matchconnections;

    @Param(name="maxconnectionusagecount", optional=true)
    String maxconnectionusagecount = "0";

    @Param(name="validateatmostonceperiod", optional=true)
    String validateatmostonceperiod;

    @Param(name="transactionsupport", acceptableValues="XATransaction,LocalTransaction,NoTransaction", optional=true)
    String transactionsupport;

    @Param(name="description", optional=true)
    String description;

    @Param(name="property", optional=true, separator=':')
    Properties properties;

    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Param(name="poolname", primary=true)
    String poolname;

    @Inject
    Resources resources;

    @Inject
    Domain domain;

    @Inject
    private Habitat habitat;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        Server targetServer = domain.getServerNamed(target);
        
        HashMap attrList = new HashMap();
        attrList.put(ResourceConstants.RES_ADAPTER_NAME, raname);
        attrList.put(ResourceConstants.CONN_DEF_NAME, connectiondefinition);
        attrList.put(ServerTags.DESCRIPTION, description);
        attrList.put(ResourceConstants.STEADY_POOL_SIZE, steadypoolsize);
        attrList.put(ResourceConstants.MAX_POOL_SIZE, maxpoolsize);
        attrList.put(ResourceConstants.MAX_WAIT_TIME_IN_MILLIS, maxwait);
        attrList.put(ResourceConstants.POOL_SIZE_QUANTITY, poolresize);
        attrList.put(ResourceConstants.IDLE_TIME_OUT_IN_SECONDS, idletimeout);
        attrList.put(ResourceConstants.IS_CONNECTION_VALIDATION_REQUIRED, isconnectvalidatereq.toString());
        attrList.put(ResourceConstants.CONN_FAIL_ALL_CONNECTIONS, failconnection.toString());
        attrList.put(ResourceConstants.VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS, validateatmostonceperiod);
        attrList.put(ResourceConstants.CONNECTION_LEAK_TIMEOUT_IN_SECONDS, leaktimeout);
        attrList.put(ResourceConstants.CONNECTION_LEAK_RECLAIM, leakreclaim.toString());
        attrList.put(ResourceConstants.CONNECTION_CREATION_RETRY_ATTEMPTS, creationretryattempts);
        attrList.put(ResourceConstants.CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS, creationretryinterval);
        attrList.put(ResourceConstants.LAZY_CONNECTION_ASSOCIATION, lazyconnectionassociation.toString());
        attrList.put(ResourceConstants.LAZY_CONNECTION_ENLISTMENT, lazyconnectionenlistment.toString());
        attrList.put(ResourceConstants.ASSOCIATE_WITH_THREAD, associatewiththread.toString());
        attrList.put(ResourceConstants.MATCH_CONNECTIONS, matchconnections.toString());
        attrList.put(ResourceConstants.MAX_CONNECTION_USAGE_COUNT, maxconnectionusagecount);
        attrList.put(ResourceConstants.CONNECTOR_CONNECTION_POOL_NAME, poolname);
        attrList.put(ResourceConstants.CONN_TRANSACTION_SUPPORT, transactionsupport);

        ResourceStatus rs;

        try {
            ConnectorConnectionPoolManager connPoolMgr = habitat.getComponent(ConnectorConnectionPoolManager.class);
            rs = connPoolMgr.create(resources, attrList, properties, targetServer);
        } catch(Exception e) {
            Logger.getLogger(CreateConnectorConnectionPool.class.getName()).log(Level.SEVERE,
                    "Unable to create connector connection pool " + poolname, e);
            String def = "Connector connection pool: {0} could not be created, reason: {1}";
            report.setMessage(localStrings.getLocalString("create.connector.connection.pool.fail",
                    def, poolname) + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        if (rs.getStatus() == ResourceStatus.FAILURE) {
            ec = ActionReport.ExitCode.FAILURE;
            if (rs.getMessage() != null) {
                report.setMessage(rs.getMessage());
            } else {
                 report.setMessage(localStrings.getLocalString("create.connector.connection.pool.fail",
                    "Connector connection pool {0} creation failed", poolname, ""));
            }
            if (rs.getException() != null)
                report.setFailureCause(rs.getException());
        }
        report.setActionExitCode(ec);
    }
}

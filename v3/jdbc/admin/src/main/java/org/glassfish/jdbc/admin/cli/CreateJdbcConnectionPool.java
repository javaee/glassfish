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
package org.glassfish.jdbc.admin.cli;

import org.glassfish.resource.common.ResourceConstants;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Domain;
import org.glassfish.resource.common.ResourceStatus;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner;
import org.jvnet.hk2.annotations.Inject;

import java.util.HashMap;
import java.util.Properties;
import org.glassfish.api.admin.ParameterMap;

/**
 * Create JDBC Connection Pool Command
 * 
 */
@Service(name="create-jdbc-connection-pool")
@Scoped(PerLookup.class)
@I18n("create.jdbc.connection.pool")
public class CreateJdbcConnectionPool implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateJdbcConnectionPool.class);    

    @Param(name="datasourceclassname", optional=true)
    String datasourceclassname;

    @Param(optional=true, acceptableValues="javax.sql.DataSource,javax.sql.XADataSource,javax.sql.ConnectionPoolDataSource,java.sql.Driver")
    String restype;

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

    @Param(optional=true)
    String initsql;
        
    @Param(name="isolationlevel", optional=true)
    String isolationlevel;
            
    @Param(name="isisolationguaranteed", optional=true, defaultValue="true")
    Boolean isisolationguaranteed;
                
    @Param(name="isconnectvalidatereq", optional=true, defaultValue="false")
    Boolean isconnectvalidatereq;
    
    @Param(optional=true, acceptableValues="auto-commit,meta-data,table,custom-validation")
    String validationmethod = "table";
    
    @Param(name="validationtable", optional=true)
    String validationtable;
    
    @Param(name="failconnection", optional=true, defaultValue="false")
    Boolean failconnection;
    
    @Param(name="allownoncomponentcallers", optional=true, defaultValue="false")
    Boolean allownoncomponentcallers;
    
    @Param(name="nontransactionalconnections", optional=true, defaultValue="false")
    Boolean nontransactionalconnections;
    
    @Param(name="validateatmostonceperiod", optional=true)
    String validateatmostonceperiod = "0";
    
    @Param(name="leaktimeout", optional=true)
    String leaktimeout = "0";
    
    @Param(name="leakreclaim", optional=true, defaultValue="false")
    Boolean leakreclaim;
    
    @Param(name="creationretryattempts", optional=true)
    String creationretryattempts = "0";
    
    @Param(name="creationretryinterval", optional=true)
    String creationretryinterval = "10";

    @Param(optional=true)
    String sqltracelisteners;
    
    @Param(name="statementtimeout", optional=true)
    String statementtimeout = "-1";
    
    @Param(name="lazyconnectionenlistment", optional=true, defaultValue="false")
    Boolean lazyconnectionenlistment;
    
    @Param(name="lazyconnectionassociation", optional=true, defaultValue="false")
    Boolean lazyconnectionassociation;
    
    @Param(name="associatewiththread", optional=true, defaultValue="false")
    Boolean associatewiththread;

    //@Param(optional=true, defaultValue="1")
    //String associatewiththreadconnectionscount;

    @Param(optional=true)
    String driverclassname;
    
    @Param(name="matchconnections", optional=true, defaultValue="false")
    Boolean matchconnections;
    
    @Param(name="maxconnectionusagecount", optional=true)
    String maxconnectionusagecount = "0";

    @Param(optional=true, defaultValue="false")
    Boolean ping;

    @Param(optional=true, defaultValue="true")
    Boolean pooling;

    @Param(optional=true, defaultValue="0")
    String statementcachesize;

    @Param(optional=true)
    String validationclassname;
    
    @Param(name="wrapjdbcobjects", optional=true, defaultValue="true")
    Boolean wrapjdbcobjects;
    
    @Param(name="description", optional=true)
    String description;
    
    @Param(name="property", optional=true, separator=':')
    Properties properties;
    
    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;
    
    @Param(name="jdbc_connection_pool_id", primary=true)
    String jdbc_connection_pool_id; 
  
    @Inject
    Resources resources;
    
    @Inject
    Domain domain;

    @Inject
    CommandRunner commandRunner;

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
        attrList.put(ResourceConstants.CONNECTION_POOL_NAME, jdbc_connection_pool_id);
        attrList.put(ResourceConstants.DATASOURCE_CLASS, datasourceclassname);
        attrList.put(ServerTags.DESCRIPTION, description);
        attrList.put(ResourceConstants.RES_TYPE, restype);
        attrList.put(ResourceConstants.STEADY_POOL_SIZE, steadypoolsize);
        attrList.put(ResourceConstants.MAX_POOL_SIZE, maxpoolsize);
        attrList.put(ResourceConstants.MAX_WAIT_TIME_IN_MILLIS, maxwait);
        attrList.put(ResourceConstants.POOL_SIZE_QUANTITY, poolresize);
        attrList.put(ResourceConstants.INIT_SQL, initsql);
        attrList.put(ResourceConstants.IDLE_TIME_OUT_IN_SECONDS, idletimeout);
        attrList.put(ResourceConstants.TRANS_ISOLATION_LEVEL, isolationlevel);
        attrList.put(ResourceConstants.IS_ISOLATION_LEVEL_GUARANTEED, isisolationguaranteed.toString());
        attrList.put(ResourceConstants.IS_CONNECTION_VALIDATION_REQUIRED, isconnectvalidatereq.toString());
        attrList.put(ResourceConstants.CONNECTION_VALIDATION_METHOD, validationmethod);
        attrList.put(ResourceConstants.VALIDATION_TABLE_NAME, validationtable);
        attrList.put(ResourceConstants.CONN_FAIL_ALL_CONNECTIONS, failconnection.toString());
        attrList.put(ResourceConstants.NON_TRANSACTIONAL_CONNECTIONS, nontransactionalconnections.toString());
        attrList.put(ResourceConstants.ALLOW_NON_COMPONENT_CALLERS, allownoncomponentcallers.toString());
        attrList.put(ResourceConstants.VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS, validateatmostonceperiod);
        attrList.put(ResourceConstants.CONNECTION_LEAK_TIMEOUT_IN_SECONDS, leaktimeout);
        attrList.put(ResourceConstants.CONNECTION_LEAK_RECLAIM, leakreclaim.toString());
        attrList.put(ResourceConstants.CONNECTION_CREATION_RETRY_ATTEMPTS, creationretryattempts);
        attrList.put(ResourceConstants.CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS, creationretryinterval);
        attrList.put(ResourceConstants.DRIVER_CLASSNAME, driverclassname);
        attrList.put(ResourceConstants.SQL_TRACE_LISTENERS, sqltracelisteners);
        attrList.put(ResourceConstants.STATEMENT_TIMEOUT_IN_SECONDS, statementtimeout);
        attrList.put(ResourceConstants.STATEMENT_CACHE_SIZE, statementcachesize);
        attrList.put(ResourceConstants.LAZY_CONNECTION_ASSOCIATION, lazyconnectionassociation.toString());
        attrList.put(ResourceConstants.LAZY_CONNECTION_ENLISTMENT, lazyconnectionenlistment.toString());
        attrList.put(ResourceConstants.ASSOCIATE_WITH_THREAD, associatewiththread.toString());
        //attrList.put(ResourceConstants.ASSOCIATE_WITH_THREAD_CONNECTIONS_COUNT, associatewiththreadconnectionscount);
        attrList.put(ResourceConstants.MATCH_CONNECTIONS, matchconnections.toString());
        attrList.put(ResourceConstants.MAX_CONNECTION_USAGE_COUNT, maxconnectionusagecount);
        attrList.put(ResourceConstants.PING, ping.toString());
        attrList.put(ResourceConstants.POOLING, pooling.toString());
        attrList.put(ResourceConstants.VALIDATION_CLASSNAME, validationclassname);
        attrList.put(ResourceConstants.WRAP_JDBC_OBJECTS, wrapjdbcobjects.toString());
        
        ResourceStatus rs;

        try {
            JDBCConnectionPoolManager connPoolMgr = new JDBCConnectionPoolManager();
            rs = connPoolMgr.create(resources, attrList, properties, targetServer);
        } catch(Exception e) {
            String actual = e.getMessage();
            String def = "JDBC connection pool: {0} could not be created, reason: {1}";
            report.setMessage(localStrings.getLocalString("create.jdbc.connection.pool.fail",
                    def, jdbc_connection_pool_id, actual));
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
                 report.setMessage(localStrings.getLocalString("create.jdbc.connection.pool.fail",
                    "JDBC connection pool {0} creation failed", jdbc_connection_pool_id, ""));
            }
            if (rs.getException() != null)
                report.setFailureCause(rs.getException());
        } else {
            if ("true".equalsIgnoreCase(ping.toString())) {
                ActionReport subReport = report.addSubActionsReport();
                ParameterMap parameters = new ParameterMap();
                parameters.set("pool_name", jdbc_connection_pool_id);
                commandRunner.getCommandInvocation("ping-connection-pool", subReport).parameters(parameters).execute();
                if (ActionReport.ExitCode.FAILURE.equals(subReport.getActionExitCode())) {
                    subReport.setMessage(localStrings.getLocalString("ping.create.jdbc.connection.pool.fail",
                            "\nAttempting to ping during JDBC Connection Pool " +
                            "Creation : {0} - Failed.", jdbc_connection_pool_id));
                    subReport.setActionExitCode(ActionReport.ExitCode.FAILURE);
                } else {
                    subReport.setMessage(localStrings.getLocalString("ping.create.jdbc.connection.pool.success",
                            "\nAttempting to ping during JDBC Connection Pool " +
                            "Creation : {0} - Succeeded.", jdbc_connection_pool_id));
                }
            }
        }
        report.setActionExitCode(ec);
    }
}

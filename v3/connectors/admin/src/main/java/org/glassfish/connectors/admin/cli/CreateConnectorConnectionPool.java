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
import org.glassfish.api.admin.config.Property;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import com.sun.enterprise.universal.glassfish.SystemPropertyConstants;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Domain;

import java.beans.PropertyVetoException;
import java.util.Properties;

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

    @Param(name="connectiondefinition", optional=true)
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
    
    @Param(name="property", optional=true)
    Properties properties;
    
    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;
    
    @Param(name="poolname", primary=true)
    String poolname;
  
    @Inject
    Resources resources;
    
    @Inject
    Domain domain;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
       
        Server targetServer = domain.getServerNamed(target);

        if (poolname == null) {
            report.setMessage(localStrings.getLocalString("create.connector.connection.pool.noJndiName",
                            "No pool name defined for connector connection pool."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
        // ensure we don't already have one of this name
        for (com.sun.enterprise.config.serverbeans.Resource resource : resources.getResources()) {
            if (resource instanceof ConnectorConnectionPool) {
                if (((ConnectorConnectionPool) resource).getName().equals(poolname)) {
                    report.setMessage(localStrings.getLocalString("create.connector.connection.pool.duplicate",
                            "A connector connection pool named {0} already exists.", poolname));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                }
            }
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {

                    ConnectorConnectionPool newResource =
                            ConfigSupport.createChildOf(param, ConnectorConnectionPool.class);
                    
                    newResource.setPoolResizeQuantity(poolresize);
                    newResource.setMaxWaitTimeInMillis(maxwait);
                    newResource.setMaxPoolSize(maxpoolsize);
                    newResource.setSteadyPoolSize(steadypoolsize);

                    newResource.setIsConnectionValidationRequired(
                                    isconnectvalidatereq.toString());
                    newResource.setIdleTimeoutInSeconds(idletimeout);
                    newResource.setFailAllConnections(failconnection.toString());
                    if (raname != null)
                        newResource.setResourceAdapterName(raname);
                    newResource.setConnectionDefinitionName(
                                    connectiondefinition);
                    newResource.setConnectionLeakTimeoutInSeconds(leaktimeout);
                    newResource.setConnectionLeakReclaim(leakreclaim.toString());
                    newResource.setConnectionCreationRetryIntervalInSeconds(
                                    creationretryinterval);
                    newResource.setConnectionCreationRetryAttempts(
                                    creationretryattempts);
                    newResource.setLazyConnectionAssociation(lazyconnectionassociation.toString());
                    newResource.setLazyConnectionEnlistment(lazyconnectionenlistment.toString());
                    newResource.setMatchConnections(matchconnections.toString());
                    newResource.setMaxConnectionUsageCount(maxconnectionusagecount);
                    newResource.setValidateAtmostOncePeriodInSeconds(validateatmostonceperiod);
                    newResource.setAssociateWithThread(
                                    associatewiththread.toString());
                    newResource.setTransactionSupport(transactionsupport);
                    
                    if (description != null) {
                        newResource.setDescription(description);
                    }
                    newResource.setName(poolname);
                    if (properties != null) {
                        for ( java.util.Map.Entry e : properties.entrySet()) {
                            Property prop = ConfigSupport.createChildOf(newResource,
                                Property.class);
                            prop.setName((String)e.getKey());
                            prop.setValue((String)e.getValue());
                            newResource.getProperty().add(prop);
                        }
                    }
                    param.getResources().add(newResource);
                    return newResource;
                }
            }, resources);

        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString(
                  "create.connector.connection.pool.fail", "Connector connection pool {0} create failed: {1}",
                poolname) + " " + tfe.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }

        report.setMessage(localStrings.getLocalString(
                "create.connector.connection.pool.success", "Connector connection pool {0} created successfully",
                poolname));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}

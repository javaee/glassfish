/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.connectors.admin.cli;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import org.glassfish.api.I18n;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;
import static org.glassfish.resource.common.ResourceConstants.*;
import org.glassfish.resource.common.ResourceStatus;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.*;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.admin.cli.resources.ResourceManager;


/**
 *
 * @author Jennifer Chou
 */
@Service (name=ServerTags.CONNECTOR_CONNECTION_POOL)
@Scoped(PerLookup.class)
@I18n("create.connector.connection.pool")
public class ConnectorConnectionPoolManager implements ResourceManager{

    @Inject
    Applications applications;

    @Inject
    ConnectorRuntime connectorRuntime;

    private static final String DESCRIPTION = ServerTags.DESCRIPTION;

    final private static LocalStringManagerImpl localStrings = 
        new LocalStringManagerImpl(ConnectorConnectionPoolManager.class);

    private String raname = null;
    private String connectiondefinition = null;
    private String steadypoolsize = "8";
    private String maxpoolsize = "32";
    private String maxwait = "60000";
    private String poolresize = "2";
    private String idletimeout = "300";
    private String isconnectvalidatereq = Boolean.FALSE.toString();
    private String failconnection = Boolean.FALSE.toString();
    private String validateAtmostOncePeriod = "0";
    private String connectionLeakTimeout = "0";
    private String connectionLeakReclaim = Boolean.FALSE.toString();
    private String connectionCreationRetryAttempts = "0";
    private String connectionCreationRetryInterval = "10";
    private String lazyConnectionEnlistment = Boolean.FALSE.toString();
    private String lazyConnectionAssociation = Boolean.FALSE.toString();
    private String associateWithThread = Boolean.FALSE.toString();
    private String matchConnections = Boolean.FALSE.toString();
    private String maxConnectionUsageCount = "0";
    private String ping = Boolean.FALSE.toString();
    private String pooling = Boolean.TRUE.toString();
    private String transactionSupport = null;

    private String description = null;
    private String poolname = null;

    public ConnectorConnectionPoolManager() {
    }

    public String getResourceType() {
        return ServerTags.CONNECTOR_CONNECTION_POOL;
    }

    public ResourceStatus create(Resources resources, HashMap attrList, 
                                    final Properties props, Server targetServer) 
                                    throws Exception {
        setParams(attrList);
        
        if (poolname == null) {
            String msg = localStrings.getLocalString("create.connector.connection.pool.noJndiName",
                            "No pool name defined for connector connection pool.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        // ensure we don't already have one of this name
        for (com.sun.enterprise.config.serverbeans.Resource resource : resources.getResources()) {
            /*if (resource instanceof BindableResource) {
                if (((BindableResource) resource).getJndiName().equals(poolname)) {
                    String msg = localStrings.getLocalString("create.connector.connection.pool.duplicate",
                            "A resource named {0} already exists.", poolname);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            } else*/ if (resource instanceof ConnectorConnectionPool) {
                if (((ResourcePool) resource).getName().equals(poolname)) {
                    String msg = localStrings.getLocalString("create.connector.connection.pool.duplicate",
                            "A resource named {0} already exists.", poolname);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            }
        }

        if (applications == null) {
            String msg = localStrings.getLocalString("noApplications",
                    "No applications found.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        try {
            ResourceStatus status = validateCnctorConnPoolAttrList(raname, connectiondefinition);
            if (status.getStatus() == ResourceStatus.FAILURE) {
                return status;
            }
        } catch(ConnectorRuntimeException cre) {
            Logger.getLogger(ConnectorConnectionPoolManager.class.getName()).log(Level.SEVERE,
                    "Could not find connection definitions from ConnectorRuntime for resource adapter "+ raname, cre);
            String msg = localStrings.getLocalString(
                  "create.connector.connection.pool.noConnDefs",
                  "Could not find connection definitions for resource adapter {0}",
                  raname) + " " + cre.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
            
        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {

                    ConnectorConnectionPool newResource = param.createChild(ConnectorConnectionPool.class);

                    newResource.setResourceAdapterName(raname);
                    newResource.setConnectionDefinitionName(connectiondefinition);
                    newResource.setValidateAtmostOncePeriodInSeconds(
                                    validateAtmostOncePeriod);
                    newResource.setSteadyPoolSize(steadypoolsize);
                    newResource.setPoolResizeQuantity(poolresize);
                    newResource.setMaxWaitTimeInMillis(maxwait);
                    newResource.setMaxPoolSize(maxpoolsize);
                    newResource.setMaxConnectionUsageCount(
                                    maxConnectionUsageCount);
                    newResource.setMatchConnections(matchConnections);
                    newResource.setLazyConnectionEnlistment(
                                    lazyConnectionEnlistment);
                    newResource.setLazyConnectionAssociation(
                                    lazyConnectionAssociation);
                    newResource.setIsConnectionValidationRequired(
                                    isconnectvalidatereq);
                    newResource.setIdleTimeoutInSeconds(idletimeout);
                    newResource.setFailAllConnections(failconnection);
                    newResource.setConnectionLeakTimeoutInSeconds(
                                    connectionLeakTimeout);
                    newResource.setConnectionLeakReclaim(
                                    connectionLeakReclaim);
                    newResource.setConnectionCreationRetryIntervalInSeconds(
                                    connectionCreationRetryInterval);
                    newResource.setConnectionCreationRetryAttempts(
                                    connectionCreationRetryAttempts);
                    newResource.setAssociateWithThread(
                                    associateWithThread);
                    newResource.setPooling(pooling);
                    newResource.setPing(ping);
                    if (transactionSupport != null) {
                        newResource.setTransactionSupport(transactionSupport);
                    }
                    if (description != null) {
                        newResource.setDescription(description);
                    }
                    newResource.setName(poolname);
                    if (props != null) {
                        for ( java.util.Map.Entry e : props.entrySet()) {
                            Property prop = newResource.createChild(Property.class);
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
            Logger.getLogger(ConnectorConnectionPoolManager.class.getName()).log(Level.SEVERE,
                    "create-connector-connection-pool failed", tfe);
            String msg = localStrings.getLocalString(
                  "create.connector.connection.pool.fail", "Connector connection pool {0} create failed: {1}",
                poolname) + " " + tfe.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        } /*catch(PropertyVetoException pve) {
            return (localStrings.getLocalString("create.jdbc.resource.fail", "{0} create failed ", id));
        }*/

        String msg = localStrings.getLocalString(
                "create.connector.connection.pool.success", "Connector connection pool {0} created successfully",
                poolname);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
         
    }

    public void setParams(HashMap attrList) {
        raname = (String) attrList.get(RES_ADAPTER_NAME);
        connectiondefinition = (String) attrList.get(CONN_DEF_NAME);
        steadypoolsize = (String) attrList.get(STEADY_POOL_SIZE);
        maxpoolsize = (String) attrList.get(MAX_POOL_SIZE);
        maxwait = (String) attrList.get(MAX_WAIT_TIME_IN_MILLIS);
        poolresize = (String) attrList.get(POOL_SIZE_QUANTITY);
        idletimeout = (String) attrList.get(IDLE_TIME_OUT_IN_SECONDS);
        isconnectvalidatereq = (String) attrList.get(IS_CONNECTION_VALIDATION_REQUIRED);
        failconnection = (String) attrList.get(FAIL_ALL_CONNECTIONS);
        validateAtmostOncePeriod = (String) attrList.get(VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS);
        connectionLeakTimeout = (String) attrList.get(CONNECTION_LEAK_TIMEOUT_IN_SECONDS);
        connectionLeakReclaim = (String) attrList.get(CONNECTION_LEAK_RECLAIM);
        connectionCreationRetryAttempts = (String) attrList.get(CONNECTION_CREATION_RETRY_ATTEMPTS);
        connectionCreationRetryInterval = (String) attrList.get(CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS);
        lazyConnectionEnlistment = (String) attrList.get(LAZY_CONNECTION_ENLISTMENT);
        lazyConnectionAssociation = (String) attrList.get(LAZY_CONNECTION_ASSOCIATION);
        associateWithThread = (String) attrList.get(ASSOCIATE_WITH_THREAD);
        matchConnections = (String) attrList.get(MATCH_CONNECTIONS);
        maxConnectionUsageCount = (String) attrList.get(MAX_CONNECTION_USAGE_COUNT);
        description = (String) attrList.get(DESCRIPTION);
        poolname = (String) attrList.get(CONNECTOR_CONNECTION_POOL_NAME);
        pooling = (String) attrList.get(POOLING);
        ping = (String) attrList.get(PING);
        transactionSupport = (String) attrList.get(CONN_TRANSACTION_SUPPORT);
    }
    
    private ResourceStatus validateCnctorConnPoolAttrList(String raName, String connDef)
            throws ConnectorRuntimeException {
        ResourceStatus status = isValidRAName(raName);
        if(status.getStatus() == ResourceStatus.SUCCESS) {
            if(!isValidConnectionDefinition(connDef,raName)) {

                String msg = localStrings.getLocalString("admin.mbeans.rmb.invalid_ra_connectdef_not_found",
                            "Invalid connection definition. Connector Module with connection definition {0} not found.", connDef);
                status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            }
        }
        return status;
    }

    private ResourceStatus isValidRAName(String raName) {
        //TODO turn on validation.  For now, turn validation off until connector modules ready
        //boolean retVal = false;
        ResourceStatus status = new ResourceStatus(ResourceStatus.SUCCESS, "");

        if ((raName == null) || (raName.equals(""))) {
            String msg = localStrings.getLocalString("admin.mbeans.rmb.null_res_adapter",
                    "Resource Adapter Name is null.");
            status = new ResourceStatus(ResourceStatus.FAILURE, msg);
        } else {
            // To check for embedded connector module
            // System RA, so don't validate
            if (!raName.equals(DEFAULT_JMS_ADAPTER) && !raName.equals(JAXR_RA_NAME)) {
                // Check if the raName contains double underscore or hash.
                // If that is the case then this is the case of an embedded rar,
                // hence look for the application which embeds this rar,
                // otherwise look for the webconnector module with this raName.

                int indx = raName.indexOf(EMBEDDEDRAR_NAME_DELIMITER);
                if (indx != -1) {
                    String appName = raName.substring(0, indx);
                    Application app = applications.getModule(Application.class, appName);
                    if (app == null) {
                        String msg = localStrings.getLocalString("admin.mbeans.rmb.invalid_ra_app_not_found",
                                "Invalid raname. Application with name {0} not found.", appName);
                        status = new ResourceStatus(ResourceStatus.FAILURE, msg);
                    }
                } else {
                    Application app = applications.getModule(Application.class, raName);
                    if (app == null) {
                        String msg = localStrings.getLocalString("admin.mbeans.rmb.invalid_ra_cm_not_found",
                                "Invalid raname. Connector Module with name {0} not found.", raName);
                        status = new ResourceStatus(ResourceStatus.FAILURE, msg);
                    }
                }
            }
        }

        return status;
    }

    private boolean isValidConnectionDefinition(String connectionDef,String raName)
            throws ConnectorRuntimeException {
        String[] names = connectorRuntime.getConnectionDefinitionNames(raName);
        for(int i = 0; i < names.length; i++) {
            if(names[i].equals(connectionDef)) {
                return true;
            }
        }
        return false;
    }
}

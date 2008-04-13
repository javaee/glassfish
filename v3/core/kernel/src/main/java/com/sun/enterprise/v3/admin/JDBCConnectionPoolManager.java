/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.admin;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import org.glassfish.api.I18n;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import static com.sun.enterprise.v3.admin.ResourceConstants.*;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import com.sun.enterprise.config.serverbeans.Property;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;


/**
 *
 * @author PRASHANTH ABBAGANI
 */
@I18n("add.resources")
class JDBCConnectionPoolManager implements ResourceManager{

    private static final String DESCRIPTION = ServerTags.DESCRIPTION;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ResourceFactory.class);    

    private String datasourceclassname = null;
    private String restype = null;
    private String steadypoolsize = "8";
    private String maxpoolsize = "32";
    private String maxwait = "60000";
    private String poolresize = "2";
    private String idletimeout = "300";
    private String isolationlevel = null;
    private String isisolationguaranteed = Boolean.TRUE.toString();
    private String isconnectvalidatereq = Boolean.FALSE.toString();
    private String validationmethod = "auto-commit";
    private String validationtable = null;
    private String failconnection = Boolean.FALSE.toString();
    private String allownoncomponentcallers = Boolean.FALSE.toString();
    private String nontransactionalconnections = Boolean.FALSE.toString();
    private String validateAtmostOncePeriod = "0";
    private String connectionLeakTimeout = "0";
    private String connectionLeakReclaim = Boolean.FALSE.toString();
    private String connectionCreationRetryAttempts = "0";
    private String connectionCreationRetryInterval = "10";
    private String statementTimeout = "-1";
    private String lazyConnectionEnlistment = Boolean.FALSE.toString();
    private String lazyConnectionAssociation = Boolean.FALSE.toString();
    private String associateWithThread = Boolean.FALSE.toString();
    private String matchConnections = Boolean.FALSE.toString();
    private String maxConnectionUsageCount = "0";
    private String wrapJDBCObjects = Boolean.FALSE.toString();

    private String description = null;
    private String jdbcconnectionpoolid = null; 

    public JDBCConnectionPoolManager() {
    }

    public ResourceStatus create(Resources resources, HashMap attrList, 
                                    final Properties props, Server targetServer) 
                                    throws Exception {
        setParams(attrList);
        if (jdbcconnectionpoolid == null) {
            String msg = localStrings.getLocalString("add.resources.noJdbcConnectionPoolId",
                            "No pool name defined for JDBC Connection pool.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        // ensure we don't already have one of this name
        for (com.sun.enterprise.config.serverbeans.Resource resource : resources.getResources()) {
            if (resource instanceof JdbcConnectionPool) {
                if (((JdbcConnectionPool) resource).getName().equals(jdbcconnectionpoolid)) {
                    String msg = localStrings.getLocalString("create.jdbc.connection.pool.duplicate",
                            "A JDBC connection pool named {0} already exists.", jdbcconnectionpoolid);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            }
        }
            
        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {

                    JdbcConnectionPool newResource = 
                            ConfigSupport.createChildOf(param, JdbcConnectionPool.class);
                    newResource.setWrapJdbcObjects(wrapJDBCObjects);
                    if (validationtable != null)
                        newResource.setValidationTableName(
                                        validationtable);
                    newResource.setValidateAtmostOncePeriodInSeconds(
                                    validateAtmostOncePeriod);
                    if (isolationlevel != null)
                        newResource.setTransactionIsolationLevel(isolationlevel);
                    newResource.setSteadyPoolSize(steadypoolsize);
                    newResource.setStatementTimeoutInSeconds(
                                    statementTimeout);
                    if (restype != null)
                        newResource.setResType(restype);
                    newResource.setPoolResizeQuantity(poolresize);
                    newResource.setNonTransactionalConnections(
                                    nontransactionalconnections);
                    newResource.setMaxWaitTimeInMillis(maxwait);
                    newResource.setMaxPoolSize(maxpoolsize);
                    newResource.setMaxConnectionUsageCount(
                                    maxConnectionUsageCount);
                    newResource.setMatchConnections(matchConnections);
                    newResource.setLazyConnectionEnlistment(
                                    lazyConnectionEnlistment);
                    newResource.setLazyConnectionAssociation(
                                    lazyConnectionAssociation);
                    newResource.setIsIsolationLevelGuaranteed(
                                    isisolationguaranteed);
                    newResource.setIsConnectionValidationRequired(
                                    isconnectvalidatereq);
                    newResource.setIdleTimeoutInSeconds(idletimeout);
                    newResource.setFailAllConnections(failconnection);
                    if (datasourceclassname != null)
                        newResource.setDatasourceClassname(
                                        datasourceclassname);
                    newResource.setConnectionValidationMethod(
                                    validationmethod);
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
                    newResource.setAllowNonComponentCallers(
                                    allownoncomponentcallers);
                    if (description != null) {
                        newResource.setDescription(description);
                    }
                    newResource.setName(jdbcconnectionpoolid);
                    if (props != null) {
                        for ( java.util.Map.Entry e : props.entrySet()) {
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
            String msg = localStrings.getLocalString(
                    "create.jdbc.connection.pool.fail", "JDBC connection pool {0} create failed ", 
                    jdbcconnectionpoolid);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        } /*catch(PropertyVetoException pve) {
            return (localStrings.getLocalString("create.jdbc.resource.fail", "{0} create failed ", id));
        }*/

        String msg = localStrings.getLocalString(
                "create.jdbc.connection.pool.success", "JDBC connection pool {0} created successfully", 
                jdbcconnectionpoolid);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
        
        
    }

    public void setParams(HashMap attrList) {
        datasourceclassname = (String) attrList.get(DATASOURCE_CLASS);
        restype = (String) attrList.get(RES_TYPE);
        steadypoolsize = (String) attrList.get(STEADY_POOL_SIZE);
        maxpoolsize = (String) attrList.get(MAX_POOL_SIZE);
        maxwait = (String) attrList.get(MAX_WAIT_TIME_IN_MILLIS);
        poolresize = (String) attrList.get(POOL_SIZE_QUANTITY);
        idletimeout = (String) attrList.get(IDLE_TIME_OUT_IN_SECONDS);
        isolationlevel = (String) attrList.get(TRANS_ISOLATION_LEVEL);
        isisolationguaranteed = (String) attrList.get(IS_ISOLATION_LEVEL_GUARANTEED);
        isconnectvalidatereq = (String) attrList.get(IS_CONNECTION_VALIDATION_REQUIRED);
        validationmethod = (String) attrList.get(CONNECTION_VALIDATION_METHOD);
        validationtable = (String) attrList.get(VALIDATION_TABLE_NAME);
        failconnection = (String) attrList.get(FAIL_ALL_CONNECTIONS);
        allownoncomponentcallers = (String) attrList.get(ALLOW_NON_COMPONENT_CALLERS);
        nontransactionalconnections = (String) attrList.get(NON_TRANSACTIONAL_CONNECTIONS);
        validateAtmostOncePeriod = (String) attrList.get(VALIDATE_ATMOST_ONCE_PERIOD);
        connectionLeakTimeout = (String) attrList.get(CONNECTION_LEAK_TIMEOUT);
        connectionLeakReclaim = (String) attrList.get(CONNECTION_LEAK_RECLAIM);
        connectionCreationRetryAttempts = (String) attrList.get(CONNECTION_CREATION_RETRY_ATTEMPTS);
        connectionCreationRetryInterval = (String) attrList.get(CONNECTION_CREATION_RETRY_INTERVAL);
        statementTimeout = (String) attrList.get(STATEMENT_TIMEOUT);
        lazyConnectionEnlistment = (String) attrList.get(LAZY_CONNECTION_ENLISTMENT);
        lazyConnectionAssociation = (String) attrList.get(LAZY_CONNECTION_ASSOCIATION);
        associateWithThread = (String) attrList.get(ASSOCIATE_WITH_THREAD);
        matchConnections = (String) attrList.get(MATCH_CONNECTIONS);
        maxConnectionUsageCount = (String) attrList.get(MAX_CONNECTION_USAGE_COUNT);
        wrapJDBCObjects = (String) attrList.get(WRAP_JDBC_OBJECTS);
        description = (String) attrList.get(DESCRIPTION);
        jdbcconnectionpoolid = (String) attrList.get(CONNECTION_POOL_NAME);
    }
    
    public ResourceStatus delete (Server[] servers, Resources resources, 
            final JdbcConnectionPool[] connPools, final String cascade, 
            final String jdbcconnectionpoolid) throws Exception {
        
        if (jdbcconnectionpoolid == null) {
            String msg = localStrings.getLocalString("jdbcConnPool.resource.noJndiName",
                            "No id defined for JDBC Connection pool.");
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            return status;
        }

        // ensure we already have this resource
        if (!isResourceExists(resources, jdbcconnectionpoolid)) {
            String msg = localStrings.getLocalString("delete.jdbc.connection.pool.notfound",
                    "A JDBC connection pool named {0} does not exist.", jdbcconnectionpoolid);
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            return status;
        }

        try {
            
            // if cascade=true delete all the resources associated with this pool 
            // if cascade=false don't delete this connection pool if a resource is referencing it
            Object obj = deleteAssociatedResources(servers, resources, 
                    Boolean.parseBoolean(cascade), jdbcconnectionpoolid);
            if (obj == Integer.valueOf(ResourceStatus.FAILURE)) {
                String msg = localStrings.getLocalString(
                    "delete.jdbc.connection.pool.pool_in_use", 
                    "JDBC Connection pool {0} delete failed ", jdbcconnectionpoolid);
                ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
                return status;
            }
            
            // delete jdbc connection pool
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    for (JdbcConnectionPool cp : connPools) {
                        if (cp.getName().equals(jdbcconnectionpoolid)) {
                            return param.getResources().remove(cp);
                        }
                    }
                    // not found
                    return null;
                }
            }, resources) == null) {
                String msg = localStrings.getLocalString("delete.jdbc.connection.pool.notfound", 
                                "A JDBC connection pool named {0} does not exist.", jdbcconnectionpoolid);
                ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
                return status;
            }
            
        } catch(TransactionFailure tfe) {
            String msg = tfe.getMessage() != null ? tfe.getMessage() :
                localStrings.getLocalString("jdbcConnPool.resource.deletionFailed", 
                            "JDBC Connection pool {0} delete failed ", jdbcconnectionpoolid);
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        }

        String msg = localStrings.getLocalString("jdbcConnPool.resource.deleteSuccess",
                "JDBC Connection pool {0} deleted successfully", jdbcconnectionpoolid);
        ResourceStatus status = new ResourceStatus(ResourceStatus.SUCCESS, msg);
        return status;
    }
    
    public ArrayList list(JdbcConnectionPool[] connPools) {
        ArrayList<String> list = new ArrayList();
        for (JdbcConnectionPool cp : connPools) {
            list.add(cp.getName());
        }
        return list;
    } 
    
    private boolean isResourceExists(Resources resources, String jdbcconnectionpoolid) {
        
        // ensure we don't already have one of this name
        for (com.sun.enterprise.config.serverbeans.Resource resource : resources.getResources()) {
            if (resource instanceof JdbcConnectionPool) {
                if (((JdbcConnectionPool) resource).getName().equals(jdbcconnectionpoolid)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    private Object deleteAssociatedResources(final Server[] servers, Resources resources, 
            final boolean cascade, final String connPoolId) throws TransactionFailure {
        
        return ConfigSupport.apply(new SingleConfigCode<Resources>() {
            public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                for (Resource resource : param.getResources()) {
                    if (resource instanceof JdbcResource) {
                        if (((JdbcResource)resource).getPoolName().equals(connPoolId)) {
                            if (cascade) {
                                // delete jdbc-resource
                                param.getResources().remove(resource);
                                // delete resource-refs
                                deleteResourceRefs(servers, ((JdbcResource)resource).getJndiName());
                            } else {
                                return Integer.valueOf(ResourceStatus.FAILURE);
                            }
                        }
                    }
                 }
                 return null;
            }
        }, resources);
        
    }
    
    private void deleteResourceRefs(Server[] servers, final String refName) 
            throws TransactionFailure {
        
        for (Server server : servers) {
            ResourceUtils.deleteResourceRef(server, refName);
        }
        
    }
}

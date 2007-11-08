/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.util.i18n.StringManager;

public class ResourceHelper extends ReferenceHelperBase {
    
    protected static final StringManager _strMgr=StringManager.getManager(ResourceHelper.class);            
    private static ResourceHelper _theInstance;    
    
    public ResourceHelper() {
        super();
    }
    
    protected Server[] getReferencingServers(ConfigContext configContext, String name) 
        throws ConfigException
    {
        return ServerHelper.getServersReferencingResource(configContext, name); 
    }
    
    protected Cluster[] getReferencingClusters(ConfigContext configContext, String name) 
        throws ConfigException
    {
        return ClusterHelper.getClustersReferencingResource(configContext, name);        
    }
        
    private synchronized static ResourceHelper getInstance()
    {
        if (_theInstance == null) {
            _theInstance = new ResourceHelper();
        }
        return _theInstance;
    }
    
    /**
     * Is the configuration referenced by anyone (i.e. any server instance or cluster
     */
    public static boolean isResourceReferenced(ConfigContext configContext, String resourceName) 
        throws ConfigException
    {
        return getInstance().isReferenced(configContext, resourceName);
    }
    
    /**
     * Return true if the configuration is referenced by no-one other than the given 
     * server instance.
     */
    public static boolean isResourceReferencedByServerOnly(ConfigContext configContext, 
        String resourceName, String serverName) throws ConfigException        
    {        
        return getInstance().isReferencedByServerOnly(configContext, resourceName, serverName);
    }
    
    /**
     * Return true if the configuration is referenced by no-one other than the given 
     * cluster.
     */
    public static boolean isResourceReferencedByClusterOnly(ConfigContext configContext, 
        String resourceName, String clusterName) throws ConfigException        
    {                       
        return getInstance().isReferencedByClusterOnly(configContext, resourceName, clusterName);
    }
    
    /**
     * Find all the servers or clusters associated with the given configuration and return them 
     * as a comma separated list.
     */
    public static String getResourceReferenceesAsString(ConfigContext configContext, String resourceName) 
        throws ConfigException
    {        
        return getInstance().getReferenceesAsString(configContext, resourceName);
    }    

    /**
     * Returns true if the named pool has a reference from a jdbc resource
     * that is used by the given server instance. 
     *
     * @param   ctx   config context
     * @param   poolName   jdbc resource pool name
     * @param   serverName  name of the server instance
     *
     * @return  true if the pool is used by the server instance
     *
     * @throw   ConfigException  if an error while parsing domain.xml
     */
    public static boolean isJdbcPoolReferenced(ConfigContext ctx, 
            String poolName, String serverName) throws ConfigException {

        if (ctx == null || poolName == null || serverName == null) {
            return false;
        }

        Resources rBean = ServerBeansFactory.getDomainBean(ctx).getResources();

        JdbcResource[] jdbcBeans = rBean.getJdbcResource();

        // no jdbc resource in the domain, so it is not possible 
        // for the jdbc pool to be in use by a server in this domain
        if (jdbcBeans == null) { 
            return false;
        }

        for (int i = 0; i <jdbcBeans.length; i++) {

            // jdbc resource is not referenced by the server instance
            if ( !ServerHelper.serverReferencesResource(
                    ctx, serverName, jdbcBeans[i].getJndiName()) ) {

                continue;
            } else {
                String pool = jdbcBeans[i].getPoolName();
                if ( (pool != null) && pool.equals(poolName) ) {
                    // jdbc pool is referenced by server (server->res->pool)
                    return true;
                }
            }
        }

        // no jdbc resource referred by this server is using this pool
        return false;
    }

    /**
     * Returns true if the named pool has a reference from a connector resource
     * that is used by the given server instance. 
     *
     * @param   ctx   config context
     * @param   poolName   connector pool name
     * @param   serverName  name of the server instance
     *
     * @return  true if the pool is used by the server instance
     *
     * @throw   ConfigException  if an error while parsing domain.xml
     */
    public static boolean isConnectorPoolReferenced(ConfigContext ctx, 
            String poolName, String serverName) throws ConfigException {

        if (ctx == null || poolName == null || serverName == null) {
            return false;
        }

        Resources rBean = ServerBeansFactory.getDomainBean(ctx).getResources();

        ConnectorResource[] conBeans = rBean.getConnectorResource();

        // no connector resource in the domain, so it is not possible 
        // for the connector pool to be in use by a server in this domain
        if (conBeans == null) { 
            return false;
        }

        for (int i = 0; i <conBeans.length; i++) {

            // connector resource is not referenced by the server instance
            if ( !ServerHelper.serverReferencesResource(
                    ctx, serverName, conBeans[i].getJndiName()) ) {

                continue;
            } else {
                String pool = conBeans[i].getPoolName();
                if ( (pool != null) && pool.equals(poolName) ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the resource type of a given resource
     */
    public static String getResourceType(ConfigContext ctx, String id) 
            throws ConfigException {

        Resources root = ((Domain)ctx.getRootConfigBean()).getResources();

        ConfigBean res = root.getJdbcResourceByJndiName(id);
        if ( res != null )
            return Resources.JDBC_RESOURCE;

        res = root.getMailResourceByJndiName(id);
        if( res != null )
            return Resources.MAIL_RESOURCE;

        res = root.getCustomResourceByJndiName(id);
        if( res != null )
            return Resources.CUSTOM_RESOURCE;

        res = root.getExternalJndiResourceByJndiName(id);
        if ( res != null )
            return Resources.EXTERNAL_JNDI_RESOURCE;

        res = root.getPersistenceManagerFactoryResourceByJndiName(id);
        if ( res != null)
            return Resources.PERSISTENCE_MANAGER_FACTORY_RESOURCE;

        res = root.getAdminObjectResourceByJndiName(id);
        if ( res != null )
            return Resources.ADMIN_OBJECT_RESOURCE;

        res = root.getConnectorResourceByJndiName(id);
        if ( res != null )
            return Resources.CONNECTOR_RESOURCE;

        res = root.getJdbcConnectionPoolByName(id);
        if ( res != null )
            return Resources.JDBC_CONNECTION_POOL;

        res = root.getConnectorConnectionPoolByName(id);
        if ( res != null )
            return Resources.CONNECTOR_CONNECTION_POOL;

        res = root.getResourceAdapterConfigByResourceAdapterName(id);
        if ( res != null )
            return Resources.RESOURCE_ADAPTER_CONFIG;

        return null;
    }
       
    public static boolean isSystemResource(ConfigContext ctx, String resourceName)
        throws ConfigException
    {
        ConfigBean bean = findResource(ctx, resourceName);
        if (bean == null) {
            throw new ConfigException(_strMgr.getString("noSuchResource", 
                resourceName));
        } 
        String objectType = null;
        try {
            objectType = bean.getAttributeValue(ServerTags.OBJECT_TYPE);
        } catch (Exception ex) {
            //if the object-type attribute does not exist, then assume that
            //the resource is not a system resource.
            return false;
        }
        if (objectType.equals(IAdminConstants.SYSTEM_ALL) || 
            objectType.equals(IAdminConstants.SYSTEM_ADMIN) ||
            objectType.equals(IAdminConstants.SYSTEM_INSTANCE)) {
            return true;
        } else {
            return false;
        }        
    }
      
    public static ConfigBean findResource(ConfigContext ctx, String id) 
            throws ConfigException {
        Resources root = ((Domain)ctx.getRootConfigBean()).getResources();
        return findResource(root, id);
    }

    public static ConfigBean findResource(Resources root, String id) 
            throws ConfigException {

        ConfigBean res = root.getJdbcResourceByJndiName(id);
        if ( res != null )
            return res;

        res = root.getMailResourceByJndiName(id);
        if( res != null )
            return res;

        res = root.getCustomResourceByJndiName(id);
        if( res != null )
            return res;

        res = root.getExternalJndiResourceByJndiName(id);
        if ( res != null )
            return res;

        res = root.getPersistenceManagerFactoryResourceByJndiName(id);
        if ( res != null)
            return res;

        res = root.getAdminObjectResourceByJndiName(id);
        if ( res != null )
            return res;

        res = root.getConnectorResourceByJndiName(id);
        if ( res != null )
            return res;

        res = root.getJdbcConnectionPoolByName(id);
        if ( res != null )
            return res;

        res = root.getConnectorConnectionPoolByName(id);
        if ( res != null )
            return res;

        res = root.getResourceAdapterConfigByResourceAdapterName(id);
        if ( res != null )
            return res;

        return null;
    }
}

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
package com.sun.enterprise.admin.event;

import java.util.List;
import java.util.ArrayList;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import com.sun.enterprise.config.serverbeans.ResourceAdapterConfig;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigAdd;
import com.sun.enterprise.config.ConfigDelete;
import com.sun.enterprise.config.ConfigChangeFactory;
import com.sun.enterprise.config.ConfigException;
import org.omg.CORBA.CTX_RESTRICT_SCOPE;

/**
 * Returns config change elements for dependent resources. This is a helper
 * class that makes an event self contained. 
 *
 * <p> Example: If a resource references a connection pool, corresponding
 * config change elements for that pool is sent as dependent config
 * changes in the event. If the pool is not initialized by the targeted
 * server already, it is initialized based on the dependent change list.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class DependencyResolver {

    /**
     * Constructor
     */
    DependencyResolver(ConfigContext ctx, String target) {
        _ctx    = ctx;
        _target = target;
    }

    /**
     * Resolves resource dependencies for resource creation.
     *
     * <pre>
     * Dependencies:
     *   JDBC Resource - Depends on JDBC connection pool.
     *   Persistence Manager Factory - Depends on JDBC resource.
     *   Connector Resource - Depends on Connector connection pool.
     * </pre>
     *
     * @param  resName  name of the resource
     * @param  action   type of event (example, deploy, undeploy, redeploy)
     * @param  typeInEvent  resource type as defined in ResourceDeployEvent
     *
     * @return a list of dependent config change objects
     *
     * @throws ConfigException  if an error while parsing configuration
     */
    List resolveResources(String resName, String action, String typeInEvent)
            throws ConfigException {

        List list = new ArrayList();
        if (resName == null 
                //|| BaseDeployEvent.REDEPLOY.equals(action)
                || BaseDeployEvent.REMOVE_REFERENCE.equals(action)
                || BaseDeployEvent.UNDEPLOY.equals(action)) {
            return list;
        }
            
        ConfigBean res = findResource(resName, typeInEvent); 
        if (res == null) {
            return list;
        }
        /*
        else if (BaseDeployEvent.REMOVE_REFERENCE.equals(action))
        {
            // add ConfigDelete change element for referenced resource 
            // it will not harm config because applying of changes 
            // will be done over runtime config
            list.addAll( getConfigDeleteForResource(res) );
            return list;
        }
        */
        
        // JDBC resource
        if (ResourceDeployEvent.RES_TYPE_JDBC.equals(typeInEvent)) {

            String poolName = ((JdbcResource) res).getPoolName();
            list.addAll(resolveJdbcConnectionPool(poolName));

        // Connector resource
        } else if (ResourceDeployEvent.RES_TYPE_CR.equals(typeInEvent)) {

            String poolName = ((ConnectorResource) res).getPoolName();
            list.addAll(resolveConnectorConnectionPool(poolName));

        // PMF resource
        } else if (ResourceDeployEvent.RES_TYPE_PMF.equals(typeInEvent)) {

            String jdbcResName = ((PersistenceManagerFactoryResource) res).
                                                    getJdbcResourceJndiName();
            list.addAll( resolveResources(jdbcResName, action, 
                                ResourceDeployEvent.RES_TYPE_JDBC) );
        }

        list.addAll( addConfigChangeForResource(res) );

        return list;
    }

    /**
     * Resolves application dependencies.
     *
     * @param  applicationName  name of the application
     * @param  action   type of event (example, deploy, undeploy, redeploy)
     *
     * @return a list of dependent config change objects
     *
     * @throws ConfigException  if an error while parsing configuration
     */
    List resolveApplications(String applicationName, String action)
            throws ConfigException {

        List list = new ArrayList();

        if (applicationName == null 
                || BaseDeployEvent.REMOVE_REFERENCE.equals(action)
                || BaseDeployEvent.REDEPLOY.equals(action)
                || BaseDeployEvent.UNDEPLOY.equals(action)) {
            return list;
        }

        ConfigBean app=ApplicationHelper.findApplication(_ctx,applicationName);
        if (app == null) {
            return list;
        }
        // xpath for this application
        String xpath = app.getXPath();

        // config change object for the application entry
        ConfigAdd configAdd = ConfigChangeFactory.createConfigAdd(_ctx, xpath);
        list.add(configAdd);

        // add resource adapter config
        ResourceAdapterConfig raConfig = 
            findResourceAdapterConfigByName(applicationName);

        // if resource adapter config is found
        if (raConfig != null) {
            ConfigAdd raConfigChange = 
                ConfigChangeFactory.createConfigAdd(_ctx, raConfig.getXPath());

            // add the resource adapter config to the dependent config changes
            list.add(raConfigChange);
        }

        return list;
    }

    /**
     * Returns config change for the given config bean.
     * 
     * @param  res  config bean
     * @return config change for the given bean
     *
     * @throws ConfigException  if a configuration parsing error
     */
    private List addConfigChangeForResource(ConfigBean res)
            throws ConfigException {
        
        List list = new ArrayList();
        if (res == null) {
            return list;
        }

        String xpath = res.getXPath();
        ConfigAdd configAdd = ConfigChangeFactory.createConfigAdd(_ctx, xpath);
        list.add(configAdd);

        return list;
    }

    /**
     * Returns the config delete change objects for an application.
     *
     * @param  applicationName  name of the application
     *
     * @return  config delete objects for the given application
     * @throws  ConfigException  if an error while parsing the config
     */
    List getConfigDeleteForApplication(String applicationName)
            throws ConfigException {
        
        List list = new ArrayList();

        if (applicationName != null) {
            ConfigBean app =
                ApplicationHelper.findApplication(_ctx,applicationName);
            String xpath = app.getXPath();
            ConfigDelete configDelete =
                ConfigChangeFactory.createConfigDelete(xpath);
            list.add(configDelete);
        }
        return list;
    }

    /**
     * Returns the config delete objects for the given resource.
     *
     * @param   resName  name of the resource
     * @param   resTypeInEvent  tyep of the resource defined 
     *                          in ResourceDeployEvent
     *
     * @return  returns the config delete objects for a resource
     * @throws  ConfigException  if an error while parsing config
     */
    List getConfigDeleteForResource(String resName, String resTypeInEvent)
            throws ConfigException {

        // type is used in finding resource since resource 
        // name space is not flat
        ConfigBean res = findResource(resName, resTypeInEvent); 
        return getConfigDeleteForResource(res);
    }

    /**
     * Creates ConfigDelete change object for referenced resource.
     *
     * @param  res  config bean
     * @return list containing config delete for the given bean
     *
     * @throws ConfigException  if an error while parsing config
     */ 
    List getConfigDeleteForResource(ConfigBean res)
            throws ConfigException {
        
        List list = new ArrayList();
        if (res == null) {
            return list;
        }
        String xpath = res.getXPath();
        ConfigDelete configDelete=ConfigChangeFactory.createConfigDelete(xpath);
        list.add(configDelete);
        return list;
    }

    /**
     * Returns dependency list for jdbc connection pool.
     * 
     * @param  poolName  name of jdbc connection pool
     * @return dependency list for jdbc connection pool
     * 
     * @throws ConfigException  if a configuration parsing error
     */
    private List resolveJdbcConnectionPool(String poolName) 
            throws ConfigException {

        List list  = new ArrayList();

        if (poolName == null) {
            return list;
        }

        Resources root = ((Domain)_ctx.getRootConfigBean()).getResources();
        JdbcConnectionPool pool = root.getJdbcConnectionPoolByName(poolName);

        // no pool found 
        if (pool == null) {
            return list;
        }
        list.addAll( addConfigChangeForResource(pool) );

        return list;
    }

    /**
     * Returns dependency list for a connector connection pool.
     * 
     * @param  poolName  name of connector connection pool
     * @return dependency list for connector connection pool
     * 
     * @throws  ConfigException  if a configuration parsing error
     */
    private List resolveConnectorConnectionPool(String poolName) 
            throws ConfigException {

        List list  = new ArrayList();

        if (poolName == null) {
            return list;
        }

        Resources root = ((Domain)_ctx.getRootConfigBean()).getResources();
        ConnectorConnectionPool pool = 
            root.getConnectorConnectionPoolByName(poolName);

        if (pool == null) {
            return list;
        }

        list.addAll( addConfigChangeForResource(pool) );

        return list;
    }

    /**
     * Returns the resource adapter config if it matches the given 
     * application name. RA in an EAR is defined as AppName#RAName.
     * This method matches with the AppName.
     *
     * @param  name   application name
     * @throws ConfigException  if an error while parsing the config
     */
    ResourceAdapterConfig findResourceAdapterConfigByName(String name) 
            throws ConfigException {

        // all resources
        Resources root = ((Domain)_ctx.getRootConfigBean()).getResources();

        // all resource adapter configs in the system
        ResourceAdapterConfig[] configs = root.getResourceAdapterConfig();


        if (configs != null) {
            for (int i=0; i<configs.length; i++) {
                String fullRAName  = configs[i].getResourceAdapterName();
                String raName = getApplicationNameFromRAName(fullRAName);

                if (raName.equals(name)) {
                    return configs[i];
                }
            }
        }

        // did not find any RA config that matches the name
        return null;
    }

    /**
     * Returns the application name from a RA name. RA name is defined 
     * as AppName#RAName. This method returns the AppName. 
     * 
     * @param  fullRAName  name of a resource adapter 
     * @return application name 
     */
    String getApplicationNameFromRAName(String fullRAName) {

        String appName      = null;

        // strip off the portion after #
        int idx = fullRAName.indexOf("#");
        if (idx > 0) {
            appName = fullRAName.substring(0, idx);
        } else {
            appName = fullRAName;
        }

        return appName;
    }

    /**
     * Locates a resource based on name and type.
     *
     * @param  resName  name of resource
     * @param  type  type of resource
     * 
     * @return config bean for the resource
     * @throws ConfigException  if a configuration parsing error
     */
    ConfigBean findResource(String resName, String type) 
            throws ConfigException {

        ConfigBean res = null;
        Resources root = ((Domain)_ctx.getRootConfigBean()).getResources();

        if (ResourceDeployEvent.RES_TYPE_JDBC.equals(type)) {
            res = root.getJdbcResourceByJndiName(resName);
        } else if (ResourceDeployEvent.RES_TYPE_MAIL.equals(type)) {
            res = root.getMailResourceByJndiName(resName);
        } else if (ResourceDeployEvent.RES_TYPE_CUSTOM.equals(type)) {
            res = root.getCustomResourceByJndiName(resName);
        } else if (ResourceDeployEvent.RES_TYPE_EXTERNAL_JNDI.equals(type)) {
            res = root.getExternalJndiResourceByJndiName(resName);
        } else if (ResourceDeployEvent.RES_TYPE_PMF.equals(type)) {
            res = root.getPersistenceManagerFactoryResourceByJndiName(resName);
        } else if (ResourceDeployEvent.RES_TYPE_AOR.equals(type)) {
            res = root.getAdminObjectResourceByJndiName(resName);
        } else if (ResourceDeployEvent.RES_TYPE_CR.equals(type)) {
            res = root.getConnectorResourceByJndiName(resName);
        } else if (ResourceDeployEvent.RES_TYPE_JCP.equals(type)) {
            res = root.getJdbcConnectionPoolByName(resName);
        } else if (ResourceDeployEvent.RES_TYPE_CCP.equals(type)) {
            res = root.getConnectorConnectionPoolByName(resName);
        } else if (ResourceDeployEvent.RES_TYPE_RAC.equals(type)) {
            res = root.getResourceAdapterConfigByResourceAdapterName(resName);
        }

        return res;
    }

    // ---- VARIABLE(S) - PRIVATE ------------------------
    private ConfigContext   _ctx;
    private String          _target;
}

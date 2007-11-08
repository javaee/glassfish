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

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.AppclientModule;
import com.sun.enterprise.config.serverbeans.ConnectorModule;
import com.sun.enterprise.config.serverbeans.LifecycleModule;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.admin.util.IAdminConstants;

import com.sun.enterprise.util.i18n.StringManager;

import java.util.ArrayList;

public class ApplicationHelper extends ReferenceHelperBase {
    
    protected static final StringManager _strMgr=StringManager.getManager(ApplicationHelper.class);        
    private static ApplicationHelper _theInstance;    
    
    public ApplicationHelper() {
        super();
    }
    
    protected Server[] getReferencingServers(ConfigContext configContext, String name) 
        throws ConfigException
    {
        return ServerHelper.getServersReferencingApplication(configContext, name); 
    }
    
    protected Cluster[] getReferencingClusters(ConfigContext configContext, String name) 
        throws ConfigException
    {
        return ClusterHelper.getClustersReferencingApplication(configContext, name);        
    }
        
    private synchronized static ApplicationHelper getInstance()
    {
        if (_theInstance == null) {
            _theInstance = new ApplicationHelper();
        }
        return _theInstance;
    }
    
    /**
     * Is the configuration referenced by anyone (i.e. any server instance or cluster
     */
    public static boolean isApplicationReferenced(ConfigContext configContext, String appName) 
        throws ConfigException
    {
        return getInstance().isReferenced(configContext, appName);
    }
    
    /**
     * Return true if the configuration is referenced by no-one other than the given 
     * server instance.
     */
    public static boolean isApplicationReferencedByServerOnly(ConfigContext configContext, 
        String appName, String serverName) throws ConfigException        
    {        
        return getInstance().isReferencedByServerOnly(configContext, appName, serverName);
    }
    
    /**
     * Return true if the configuration is referenced by no-one other than the given 
     * cluster.
     */
    public static boolean isApplicationReferencedByClusterOnly(ConfigContext configContext, 
        String appName, String clusterName) throws ConfigException        
    {                       
        return getInstance().isReferencedByClusterOnly(configContext, appName, clusterName);
    }
    
    /**
     * Find all the servers or clusters associated with the given configuration and return them 
     * as a comma separated list.
     */
    public static String getApplicationReferenceesAsString(ConfigContext configContext, String appName) 
        throws ConfigException
    {        
        return getInstance().getReferenceesAsString(configContext, appName);
    }    

    /**
     * Returns the type of a given application
     */
    public static String getApplicationType(ConfigContext ctx, String id) 
            throws ConfigException {
		ConfigBean appBean = findApplication(ctx, id);
		if (appBean instanceof J2eeApplication)
			return Applications.J2EE_APPLICATION;
		if (appBean instanceof EjbModule)
			return Applications.EJB_MODULE;
		if (appBean instanceof WebModule)
			return Applications.WEB_MODULE;
		if (appBean instanceof LifecycleModule)
			return Applications.LIFECYCLE_MODULE;
		if (appBean instanceof AppclientModule)
			return Applications.APPCLIENT_MODULE;
		if (appBean instanceof ConnectorModule)
			return Applications.CONNECTOR_MODULE;
		if (appBean instanceof Mbean)
			return Applications.MBEAN;
		return null;
	}

    public static ConfigBean findApplication(ConfigContext configContext,
                   String appName) throws ConfigException
    {
        Applications root = ((Domain)configContext.getRootConfigBean()).
                                    getApplications();

        ConfigBean app = root.getJ2eeApplicationByName(appName);
        if ( app != null)
            return app;

        app = root.getEjbModuleByName(appName);
        if ( app != null)
            return app;

        app = root.getWebModuleByName(appName);
        if ( app != null)
            return app;

        app = root.getConnectorModuleByName(appName);
        if ( app != null)
            return app;
        
        app = root.getAppclientModuleByName(appName);
        if ( app != null)
            return app;

        app = root.getLifecycleModuleByName(appName);
        if ( app != null)
            return app;

        app = root.getMbeanByName(appName);
        if ( app != null)
            return app;

        return null;
    }

    public static String[] getApplicationsInDomain(ConfigContext configContext)
        throws ConfigException
    {
        ArrayList result = new ArrayList();
        final Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);
        final Applications applications = domain.getApplications();
        AppclientModule[] appclients = applications.getAppclientModule();
        for (int i = 0; i < appclients.length; i++) {
            result.add(appclients[i].getName());
        }
        ConnectorModule[] connectors = applications.getConnectorModule();
        for (int i = 0; i < connectors.length; i++) {
            result.add(connectors[i].getName());
        }
        EjbModule[] ebjs = applications.getEjbModule();
        for (int i = 0; i < ebjs.length; i++) {
            result.add(ebjs[i].getName());
        }
        J2eeApplication[] apps = applications.getJ2eeApplication();
        for (int i = 0; i < apps.length; i++) {
            result.add(apps[i].getName());
        }
        LifecycleModule[] lifecycles = applications.getLifecycleModule();
        for (int i = 0; i < lifecycles.length; i++) {
            result.add(lifecycles[i].getName());
        }
        WebModule[] webs = applications.getWebModule();
        for (int i = 0; i < webs.length; i++) {
            result.add(webs[i].getName());
        }
        Mbean[] mbeans = applications.getMbean();
        for (int i = 0; i < mbeans.length; i++) {
            result.add(mbeans[i].getName());
        }
        return (String[])result.toArray(new String[result.size()]);
    }

    public static boolean isSystemApp(ConfigContext ctx, String appId)
        throws ConfigException
    {
        ConfigBean bean = findApplication(ctx, appId);
        if (bean == null) {
            throw new ConfigException(_strMgr.getString("noSuchApplication", 
                appId));
        } 
        String objectType = null;
        try {
            objectType = bean.getAttributeValue(ServerTags.OBJECT_TYPE);
        } catch (Exception ex) {
            //if the object-type attribute does not exist, then assume that
            //the app is not a system app.
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

    /**
     * Given an application ID, end point name and transformation rule name
     * this method returns the matching TransformationRule ConfigBean.
     *
     * @param cfgContext    Config context to be used
     * @param appId         Application or module name
     * @param epName        Endpoint's name
     * @param ruleName      Transformation rule's name
     */
    public static TransformationRule findTransformationRule(
        ConfigContext cfgContext, String appId, String epName, String
        ruleName) throws ConfigException {

        ConfigBean appBean = findApplication(cfgContext, appId);
        if (appBean == null) {
            throw new ConfigException(_strMgr.getString("noSuchApplication", 
                appId));
        } 
        WebServiceEndpoint wsep = null;
		if (appBean instanceof J2eeApplication) {
            wsep = ((J2eeApplication) appBean).getWebServiceEndpointByName(
                    epName);
        } else if (appBean instanceof EjbModule) {
            wsep = ((EjbModule) appBean).getWebServiceEndpointByName( epName);
        } else if (appBean instanceof WebModule) {
            wsep = ((WebModule) appBean).getWebServiceEndpointByName( epName);
        }
        if (wsep == null) {
            throw new ConfigException(_strMgr.getString("noSuchWSEP", 
                epName));
        } 
        TransformationRule tRule = wsep.getTransformationRuleByName(ruleName);
        if (tRule == null) {
            throw new ConfigException(
                _strMgr.getString("noSuchTransformationRule", ruleName));
        } else {
            return tRule;
        }

    }


}

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

/*
 * ServerBeansFactory.java
 *
 * Created on February 26, 2003, 1:44 PM
 */

package com.sun.enterprise.config.serverbeans;

import java.io.*;
import java.util.*;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import java.lang.reflect.Method;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.util.IAdminConstants;

/**
 * Factory for retrieving a particular config. bean given
 * the ConfigContext object.
 * This is the central place where a server resolves the references.
 * There should be no place in the code that accesses beans using context directly.
 *
 * @author Sridatta
 * @author  ruyak
 */
public final class ServerBeansFactory {
    /** local string manager for i18n */
    private static StringManager localStrings =
    StringManager.getManager("com.sun.enterprise.server");

    private ServerBeansFactory() {
        // disallow instantiation
    }

    /**
     * Get the config model for this server instance. This is the config
     * bean for the server that is defined by the config-ref child element.
     *
     * @return the config bean for this config ref model.
     */
    public static Config getConfigModel(ConfigContext configContext) throws ConfigException {
        if ( configContext == null ) {
            final String msg = localStrings.getString( "serverContext.config_context_is_null");
            throw new ConfigException(msg);
        }
        
        //create the config bean for this instance
        final Server server = ServerBeansFactory.getServerBean(configContext);
        final String configRef = server.getConfigRef();
        final String configXpath = ServerXPathHelper.getConfigIdXpath(configRef);
        final Config configModel = (Config)
            ConfigBeansFactory.getConfigBeanByXPath(configContext, configXpath);

        return configModel;
    }

    /**
    *
    */
    public static Domain getDomainBean(ConfigContext ctx) throws ConfigException {
       return (Domain)ctx.getRootConfigBean();
    }

    /**
     * Returns server config bean.
     *
     * @returns the server config bean
     */
    public static Server getServerBean(ConfigContext ctx) throws ConfigException {
       final String serverName = java.lang.System.getProperty("com.sun.aas.instanceName");
       String xpath = ServerXPathHelper.getServerIdXpath(serverName);
       final Server server =
            (Server) ConfigBeansFactory.getConfigBeanByXPath(ctx, xpath);
       return server;
    }

    /**
    * Returns the config bean for a particular instance.
    *
    * @returns the config bean for the instance
    */
    public static Config getConfigBean(ConfigContext ctx) throws ConfigException {
        return ServerBeansFactory.getConfigModel(ctx);
    }

    /**
     *
     * @ctx config context of the server.
     * @type type of the resource. maps to element name in dtd
     */
    public static ConfigBean[] getResources(ConfigContext ctx, String type) throws ConfigException {
       Resources r = getDomainBean(ctx).getResources();

       //ResourceRefs refs = getServerBean(ctx).getResourceRefs();

       // Implement the logic of getting only the resources referenced by the server

       // TBD
       //FIXME: validate the code.
       return r.getChildBeansByName(type);
    }

        /**
         * Get the connector service object from the given config
         * context
         * @param ctx the ConfigContext from which the connector
         * service should be obtained
         * @return the connector service object (might be null)
         */
    public static ConnectorService getConnectorServiceBean(ConfigContext ctx) throws ConfigException{
        return getConfigBean(ctx).getConnectorService();
    }


    /**
     *
     */
    public static SecurityService getSecurityServiceBean(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       return cfg.getSecurityService();
    }

    /**
     *
     */
    public static TransactionService getTransactionServiceBean(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       return cfg.getTransactionService();
    }

    /**
     *
     */
    public static HttpService getHttpServiceBean(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       HttpService httpService = cfg.getHttpService();
       return httpService;
    }

    /**
     *
     */
    public static JmsService getJmsServiceBean(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       JmsService jmsService = cfg.getJmsService();
       return jmsService;
    }

    /**
     *
     */
    public static JmsHost getJmsHostBean(ConfigContext ctx) throws ConfigException {
       JmsService jmsService = ServerBeansFactory.getJmsServiceBean(ctx);
       JmsHost[] hosts = jmsService.getJmsHost();
       return hosts[0];
    }

    /**
     *
     */
    public static IiopService getIiopServiceBean(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       IiopService iiopService = cfg.getIiopService();
       return iiopService;
    }

    /**
     *
     */
    public static JavaConfig getJavaConfigBean(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       JavaConfig javaConfig = cfg.getJavaConfig();
       return javaConfig;
    }

    /**
     *
     */
    public static String getAdminServiceLogLevel(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       LogService logService = cfg.getLogService();
       return logService.getModuleLogLevels().getAdmin();
    }

    /**
     *
     */
    public static String getEjbContainerLogLevel(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       LogService logService = cfg.getLogService();
       return logService.getModuleLogLevels().getEjbContainer();
    }


    /**
     *
     */
    public static String getWebContainerLogLevel(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       LogService logService = cfg.getLogService();
       return logService.getModuleLogLevels().getWebContainer();
    }


    /**
     *
     */
    public static String getMdbContainerLogLevel(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       LogService logService = cfg.getLogService();
       return logService.getModuleLogLevels().getMdbContainer();
    }

    /**
     *
     */
    public static String getSecurityServiceLogLevel(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       /*SecurityService securityService = cfg.getSecurityService();
       return securityService.getLogLevel();
        */
       LogService logService = cfg.getLogService();
       return logService.getModuleLogLevels().getSecurity();
    }

    /**
     *
     */
    public static String getTransactionServiceLogLevel(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       LogService logService = cfg.getLogService();
       return logService.getModuleLogLevels().getJts();

    }

    /**
     *
     */
    public static String getCorbaLogLevel(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       LogService logService = cfg.getLogService();
       return logService.getModuleLogLevels().getCorba();
    }

    /**
     *
     */
    public static String getRootLogLevel(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       LogService logService = cfg.getLogService();
       return logService.getModuleLogLevels().getRoot();
    }


    /**
     *
     */
    public static MdbContainer getMdbContainerBean(ConfigContext ctx) throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       MdbContainer container = cfg.getMdbContainer();
       return container;
    }

    /**
     *
     */
    public static DasConfig getDasConfigBean(ConfigContext ctx)
                                                throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       AdminService as = cfg.getAdminService();
       DasConfig ds = null;
       if(as != null) ds = as.getDasConfig();
       return ds; // can be null;
    }


    /**
     *
     */
    /*
    public static ApplicationConfig getApplicationConfigBean(ConfigContext ctx)
                                                throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       ApplicationConfig appConfig = cfg.getApplicationConfig();
       return appConfig;
    }
     */

    /**
     *
     */
    public static Applications getApplicationsBean(ConfigContext ctx)
                                                throws ConfigException {
       return ServerBeansFactory.getDomainBean(ctx).getApplications();
    }

    /**
     *
     */
    public static WebContainer getWebContainerBean(ConfigContext ctx)
                                                throws ConfigException {
       Config cfg = ServerBeansFactory.getConfigBean(ctx);
       WebContainer webContainer = cfg.getWebContainer();
       return webContainer;
    }

    /**
     * Returns the appropriate Server bean for an instance given an array
     * of Server beans from the domain.xml config.
     *
     * @returns the Server bean for the instance
     */
    public static Server getServerBeanFromArray(Server[] serverArray,String serverName) {
        for (int i=0;i<serverArray.length;i++) {
            if (serverArray[i].getName().equals(serverName)) {
                return serverArray[i];
            }
        }
        //if no match is made, then return first server bean in array
        return serverArray[0];
    }

        /**
           Indicate if the given {@link AdminObjectResource} resource is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param res the {@link AdminObjectResource} (possibly) referenced resource
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the resource is referenced within
           the context
        */
    public static boolean isReferenced(final AdminObjectResource res, final ConfigContext ctx) throws ConfigException {
        return isReferencedByResourceRef(res.getJndiName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link AdminObjectResource} resource within the given {@link
           ConfigContext}
           @param res the {@link AdminObjectResource}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final AdminObjectResource res, final ConfigContext ctx) throws ConfigException{
        return getResourceRefsReferencing(res.getJndiName(), ctx);
    }

        /**
           Indicate if the given {@link AppclientModule} element is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param cm the {@link AppclientModule}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the appclient module is referenced within
           the context
        */
    public static boolean isReferenced(final AppclientModule cm, final ConfigContext ctx) throws ConfigException {
        return isReferencedByApplicationRef(cm.getName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link AppclientModule} within the given {@link
           ConfigContext}
           @param cm the {@link AppclientModule}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final AppclientModule cm, final ConfigContext ctx) throws ConfigException{
        return getApplicationRefsReferencing(cm.getName(), ctx);
    }

        /**
           Indicate if the given {@link Config} resource is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param res the {@link Config} (possibly) referenced resource
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the resource is referenced within
           the context
        */
    public static boolean isReferenced(final Config res, final ConfigContext ctx) throws ConfigException {
        if (null == res || null == ctx) { return false; }
        for (final Iterator it = getServers(ctx).iterator(); it.hasNext();){
            if (res.getName().equals(((Server) it.next()).getConfigRef())){
                return true;
            }
        }

        for (final Iterator it = getClusters(ctx).iterator(); it.hasNext();){
            if (res.getName().equals(((Cluster) it.next()).getConfigRef())){
                return true;
            }
        }

        return false;
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link Config} resource within the given {@link
           ConfigContext}
           @param res the {@link Config}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty) containing either {@link Server} or {@link Cluster} objects.
        */
    public static Set getReferers(final Config res, final ConfigContext ctx) throws ConfigException{
        if (null == res || null == ctx) { return Collections.EMPTY_SET; }
        final Set result = new HashSet();
        for (final Iterator it = getServers(ctx).iterator(); it.hasNext();){
            final Server el = (Server) it.next();
            if (res.getName().equals(el.getConfigRef())){
                result.add(el);
            }
        }

        for (final Iterator it = getClusters(ctx).iterator(); it.hasNext();){
            final Cluster el = (Cluster) it.next();
            if (res.getName().equals(el.getConfigRef())){
                result.add(el);
            }
        }

        return result;
    }

        /**
           Indicate if the given {@link NodeAgent} resource is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param res the {@link NodeAgent} (possibly) referenced resource
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the resource is referenced within
           the context
        */
    public static boolean isReferenced(final NodeAgent res, final ConfigContext ctx) throws ConfigException {
        if (null == res || null == ctx) { return false; }
        for (final Iterator it = getServers(ctx).iterator(); it.hasNext();){
            if (res.getName().equals(((Server) it.next()).getNodeAgentRef())){
                return true;
            }
        }

        return false;
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link NodeAgent} resource within the given {@link
           ConfigContext}
           @param res the {@link NodeAgent}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty) containing either {@link Server} or {@link Cluster} objects.
        */
    public static Set getReferers(final NodeAgent res, final ConfigContext ctx) throws ConfigException{
        if (null == res || null == ctx) { return Collections.EMPTY_SET; }
        final Set result = new HashSet();
        for (final Iterator it = getServers(ctx).iterator(); it.hasNext();){
            final Server el = (Server) it.next();
            if (res.getName().equals(el.getNodeAgentRef())){
                result.add(el);
            }
        }
        return result;
    }

        /**
           Indicate if the given {@link ConnectorResource} resource is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param res the {@link ConnectorResource} (possibly) referenced resource
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the resource is referenced within
           the context
        */
    public static boolean isReferenced(final ConnectorResource res, final ConfigContext ctx) throws ConfigException {
        return isReferencedByResourceRef(res.getJndiName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link ConnectorResource} resource within the given {@link
           ConfigContext}
           @param res the {@link ConnectorResource}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final ConnectorResource res, final ConfigContext ctx) throws ConfigException{
        return getResourceRefsReferencing(res.getJndiName(), ctx);
    }

        /**
           Indicate if the given {@link Cluster} element is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param cm the {@link Cluster}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the cluster is referenced within
           the context
        */
    public static boolean isReferenced(final Cluster cm, final ConfigContext ctx) throws ConfigException {
        if (null == cm || null == ctx) {return false;}

        final Set refs = getClusterRefs(ctx);
        if (refs.isEmpty()) { return false; }
        for (final Iterator it = refs.iterator(); it.hasNext(); ){
            if (cm.getName().equals(((ClusterRef) it.next()).getRef())){
                return true;
            }
        }
        return false;
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link Cluster} within the given {@link
           ConfigContext}
           @param cm the {@link Cluster}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final Cluster cm, final ConfigContext ctx) throws ConfigException{
        if (null == cm || null == ctx) {return Collections.EMPTY_SET;}

        final Set refs = getClusterRefs(ctx);
        if (refs.isEmpty()) { return Collections.EMPTY_SET; }

        final Set result = new HashSet();
        for (final Iterator it = refs.iterator(); it.hasNext(); ){
            final ClusterRef ref = (ClusterRef) it.next();
            if (cm.getName().equals(ref.getRef())){
                result.add(ref);
            }
        }
        return result;
    }

        /**
           Indicate if the given {@link ConnectorConnectionPool} element is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param cm the {@link ConnectorConnectionPool}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the cluster is referenced within
           the context
        */
    public static boolean isReferenced(final ConnectorConnectionPool cm, final ConfigContext ctx) throws ConfigException {
        if (null == cm || null == ctx) {return false;}

        final Set crs = getConnectorResources(ctx);
        if (crs.isEmpty()) { return false; }
        for (final Iterator it = crs.iterator(); it.hasNext(); ){
            if (cm.getName().equals(((ConnectorResource) it.next()).getPoolName())){
                return true;
            }
        }
        return false;
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link ConnectorConnectionPool} within the given {@link
           ConfigContext}
           @param cm the {@link ConnectorConnectionPool}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final ConnectorConnectionPool cm, final ConfigContext ctx) throws ConfigException{
        if (null == cm || null == ctx) {return Collections.EMPTY_SET;}

        final Set crs = getConnectorResources(ctx);
        if (crs.isEmpty()) { return Collections.EMPTY_SET; }

        final Set result = new HashSet();
        for (final Iterator it = crs.iterator(); it.hasNext(); ){
            final ConnectorResource cr  = (ConnectorResource) it.next();
            if (cm.getName().equals(cr.getPoolName())){
                result.add(cr);
            }
        }
        return result;
    }

        /**
           Indicate if the given {@link ConnectorModule} element is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param cm the {@link ConnectorModule}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the connector module is referenced within
           the context
        */
    public static boolean isReferenced(final ConnectorModule cm, final ConfigContext ctx) throws ConfigException {
        return isReferencedByApplicationRef(cm.getName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link ConnectorModule} within the given {@link
           ConfigContext}
           @param cm the {@link ConnectorModule}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final ConnectorModule cm, final ConfigContext ctx) throws ConfigException{
        return getApplicationRefsReferencing(cm.getName(), ctx);
    }

        /**
           Indicate if the given {@link CustomResource} resource is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param res the {@link CustomResource} (possibly) referenced resource
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the resource is referenced within
           the context
        */
    public static boolean isReferenced(final CustomResource res, final ConfigContext ctx) throws ConfigException {
        return isReferencedByResourceRef(res.getJndiName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link CustomResource} resource within the given {@link
           ConfigContext}
           @param res the {@link CustomResource}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final CustomResource res, final ConfigContext ctx) throws ConfigException{
        return getResourceRefsReferencing(res.getJndiName(), ctx);
    }
        /**
           Indicate if the given {@link EjbModule} element is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param cm the {@link EjbModule}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the ejb module is referenced within
           the context
        */
    public static boolean isReferenced(final EjbModule cm, final ConfigContext ctx) throws ConfigException {
        return isReferencedByApplicationRef(cm.getName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link EjbModule} within the given {@link
           ConfigContext}
           @param cm the {@link EjbModule}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final EjbModule cm, final ConfigContext ctx) throws ConfigException{
        return getApplicationRefsReferencing(cm.getName(), ctx);
    }

        /**
           Indicate if the given {@link ExternalJndiResource} resource is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param res the {@link ExternalJndiResource} (possibly) referenced resource
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the resource is referenced within
           the context
        */
    public static boolean isReferenced(final ExternalJndiResource res, final ConfigContext ctx) throws ConfigException {
        return isReferencedByResourceRef(res.getJndiName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link ExternalJndiResource} resource within the given {@link
           ConfigContext}
           @param res the {@link ExternalJndiResource}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final ExternalJndiResource res, final ConfigContext ctx) throws ConfigException{
        return getResourceRefsReferencing(res.getJndiName(), ctx);
    }
        /**
           Indicate if the given {@link J2eeApplication} element is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param cm the {@link J2eeApplication}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the j2ee application is referenced within
           the context
        */
    public static boolean isReferenced(final J2eeApplication cm, final ConfigContext ctx) throws ConfigException {
        return isReferencedByApplicationRef(cm.getName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link J2eeApplication} within the given {@link
           ConfigContext}
           @param cm the {@link J2eeApplication}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final J2eeApplication cm, final ConfigContext ctx) throws ConfigException{
        return getApplicationRefsReferencing(cm.getName(), ctx);
    }

        /**
           Indicate if the given {@link JdbcResource} resource is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param res the {@link JdbcResource} (possibly) referenced resource
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the resource is referenced within
           the context
        */
    public static boolean isReferenced(final JdbcResource res, final ConfigContext ctx) throws ConfigException {
        return isReferencedByResourceRef(res.getJndiName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link JdbcResource} resource within the given {@link
           ConfigContext}
           @param res the {@link JdbcResource}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final JdbcResource res, final ConfigContext ctx) throws ConfigException{
        return getResourceRefsReferencing(res.getJndiName(), ctx);
    }
        /**
           Indicate if the given {@link LifecycleModule} element is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param cm the {@link LifecycleModule}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the lifecycle module is referenced within
           the context
        */
    public static boolean isReferenced(final LifecycleModule cm, final ConfigContext ctx) throws ConfigException {
        return isReferencedByApplicationRef(cm.getName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link LifecycleModule} within the given {@link
           ConfigContext}
           @param cm the {@link LifecycleModule}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final LifecycleModule cm, final ConfigContext ctx) throws ConfigException{
        return getApplicationRefsReferencing(cm.getName(), ctx);
    }
        /**
           Indicate if the given {@link MailResource} resource is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param res the {@link MailResource} (possibly) referenced resource
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the resource is referenced within
           the context
        */
    public static boolean isReferenced(final MailResource res, final ConfigContext ctx) throws ConfigException {
        return isReferencedByResourceRef(res.getJndiName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link MailResource} resource within the given {@link
           ConfigContext}
           @param res the {@link MailResource}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final MailResource res, final ConfigContext ctx) throws ConfigException{
        return getResourceRefsReferencing(res.getJndiName(), ctx);
    }
        /**
           Indicate if the given {@link PersistenceManagerFactoryResource} resource is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param res the {@link PersistenceManagerFactoryResource} (possibly) referenced resource
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the resource is referenced within
           the context
        */
    public static boolean isReferenced(final PersistenceManagerFactoryResource res, final ConfigContext ctx) throws ConfigException {
        return isReferencedByResourceRef(res.getJndiName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link PersistenceManagerFactoryResource} resource within the given {@link
           ConfigContext}
           @param res the {@link PersistenceManagerFactoryResource}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final PersistenceManagerFactoryResource res, final ConfigContext ctx) throws ConfigException{
        return getResourceRefsReferencing(res.getJndiName(), ctx);
    }

        /**
           Indicate if the given {@link ResourceAdapterConfig} resource is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param res the {@link ResourceAdapterConfig} (possibly) referenced resource
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the resource is referenced within
           the context
        */
    public static boolean isReferenced(final ResourceAdapterConfig res, final ConfigContext ctx) throws ConfigException {
        return isReferencedByResourceRef(res.getResourceAdapterName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link ResourceAdapterConfig} resource within the given {@link
           ConfigContext}
           @param res the {@link ResourceAdapterConfig}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final ResourceAdapterConfig res, final ConfigContext ctx) throws ConfigException{
        return getResourceRefsReferencing(res.getResourceAdapterName(), ctx);
    }
    
        /**
           Indicate if the given {@link Server} resource is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param res the {@link Server} (possibly) referenced resource
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the resource is referenced within
           the context
        */
    public static boolean isReferenced(final Server res, final ConfigContext ctx) throws ConfigException {
        return isReferencedByServerRef(res.getName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link Server} resource within the given {@link
           ConfigContext}
           @param res the {@link Server}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final Server res, final ConfigContext ctx) throws ConfigException{
        return getServerRefsReferencing(res.getName(), ctx);
    }
        /**
           Indicate if the given {@link WebModule} element is
           referenced by one or more other elements within the given
           {@link ConfigContext}.
           @param cm the {@link WebModule}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           the config context.
           @return true iff the web module is referenced within
           the context
        */
    public static boolean isReferenced(final WebModule cm, final ConfigContext ctx) throws ConfigException {
        for (final Iterator it = getVirtualServers(ctx).iterator(); it.hasNext(); ){
            if (cm.getName().equals(((VirtualServer) it.next()).getDefaultWebModule())){
                return true;
            }
        }
        
        return isReferencedByApplicationRef(cm.getName(), ctx);
    }
    
        /**
           Return a set containing all the elements that reference the
           given {@link WebModule} within the given {@link
           ConfigContext}
           @param cm the {@link WebModule}
           @param ctx the {@link ConfigContext}
           @throws ConfigException if there's a problem in accessing
           teh config context.
           @return a {@link java.util.Set} (never null, possibly
           empty)
        */
    public static Set getReferers(final WebModule cm, final ConfigContext ctx) throws ConfigException{
        return getApplicationRefsReferencing(cm.getName(), ctx);
    }
    
    private static boolean isReferencedByApplicationRef(final String name, final ConfigContext ctx) throws ConfigException {
        for (final Iterator it = getApplicationRefs(ctx).iterator(); it.hasNext(); ){
            if (name.equals(((ApplicationRef) it.next()).getRef())){
                return true;
            }
        }
        return false;
    }
    
    private static Set getApplicationRefsReferencing(final String name, final ConfigContext ctx) throws ConfigException {
        final Set refers = new HashSet();
        for (final Iterator it = getApplicationRefs(ctx).iterator(); it.hasNext(); ){
            final ApplicationRef ar = (ApplicationRef) it.next();
            if (name.equals(ar.getRef())){
                refers.add(ar);
            }
        }
        return refers;
    }
            
        
        /**
           Get all the application ref elements from the given
           configuration context wherever they are located.
           @param ctx the context from which the application refs are required.
           @return a {@link java.util.Set} of {@link ApplicationRef}
           objects from the given context. Never returns a null.
        */
    public static Set getApplicationRefs(final ConfigContext ctx) throws ConfigException {
        final Set refs = new HashSet();
        for (final Iterator it = getServers(ctx).iterator(); it.hasNext();){
            refs.addAll(Arrays.asList(((Server) it.next()).getApplicationRef()));
        }

        for (final Iterator it = getClusters(ctx).iterator(); it.hasNext();){
            refs.addAll(Arrays.asList(((Cluster) it.next()).getApplicationRef()));
        }

        return refs;
    }

    private static boolean isReferencedByResourceRef(final String name, final ConfigContext ctx) throws ConfigException {
        for (final Iterator it = getResourceRefs(ctx).iterator(); it.hasNext(); ){
            if (name.equals(((ResourceRef) it.next()).getRef())){
                return true;
            }
        }
        return false;
    }
    
    private static Set getResourceRefsReferencing(final String name, final ConfigContext ctx) throws ConfigException {
        final Set refers = new HashSet();
        for (final Iterator it = getResourceRefs(ctx).iterator(); it.hasNext(); ){
            final ResourceRef ar = (ResourceRef) it.next();
            if (name.equals(ar.getRef())){
                refers.add(ar);
            }
        }
        return refers;
    }
    private static boolean isReferencedByServerRef(final String name, final ConfigContext ctx) throws ConfigException {
        for (final Iterator it = getServerRefs(ctx).iterator(); it.hasNext(); ){
            if (name.equals(((ServerRef) it.next()).getRef())){
                return true;
            }
        }
        return false;
    }
    
    private static Set getServerRefsReferencing(final String name, final ConfigContext ctx) throws ConfigException {
        final Set refers = new HashSet();
        for (final Iterator it = getServerRefs(ctx).iterator(); it.hasNext(); ){
            final ServerRef ar = (ServerRef) it.next();
            if (name.equals(ar.getRef())){
                refers.add(ar);
            }
        }
        return refers;
    }
            
        
        /**
           Get all the resource ref elements from the given
           configuration context wherever they are located.
           @param ctx the context from which the resource refs are required.
           @return a {@link java.util.Set} of {@link ResourceRef}
           objects from the given context. Never returns a null.
        */
    public static Set getResourceRefs(final ConfigContext ctx) throws ConfigException {
        final Set refs = new HashSet();
        for (final Iterator it = getServers(ctx).iterator(); it.hasNext();){
            refs.addAll(Arrays.asList(((Server) it.next()).getResourceRef()));
        }

        for (final Iterator it = getClusters(ctx).iterator(); it.hasNext();){
            refs.addAll(Arrays.asList(((Cluster) it.next()).getResourceRef()));
        }

        return refs;
    }

        /**
           Get all the server ref elements from the given
           configuration context wherever they are located.
           @param ctx the context from which the server refs are required.
           @return a {@link java.util.Set} of {@link ServerRef}
           objects from the given context. Never returns a null.
        */
    public static Set getServerRefs(final ConfigContext ctx) throws ConfigException {
        final Set refs = new HashSet();
        for (final Iterator it = getLbConfigs(ctx).iterator(); it.hasNext();){
            refs.addAll(Arrays.asList(((LbConfig) it.next()).getServerRef()));
        }
        
        for (final Iterator it = getClusters(ctx).iterator(); it.hasNext();){
            refs.addAll(Arrays.asList(((Cluster) it.next()).getServerRef()));
        }
        return refs;
    }

        /**
           Get a set containing all the configs within the given
           context.
           @param ctx the context to be searched.
           @return a non-null, possibly empty {@link java.util.Set}
           containing any and all {@link LbConfig} instances found
           within the given context. All objects in this list
           will be LbConfig objects.
           @throws ConfigContext if there's a problem in accessing the
           context.
        */
    public static Set getConfigs(final ConfigContext ctx) throws ConfigException {
        if (null == ctx) {
            return Collections.EMPTY_SET;
        }
        final Domain dom = (Domain) ctx.getRootConfigBean();
        if (null == dom){
            return Collections.EMPTY_SET;
        }
        final Configs configs = dom.getConfigs();
        if (null == configs || null == configs.getConfig()){
            return Collections.EMPTY_SET;
        }
        final Set result = new HashSet();
        result.addAll(Arrays.asList(configs.getConfig()));
        return result;
    }
        
        /**
           Get a set containing all the clusters within the given
           context.
           @param ctx the context to be searched.
           @return a non-null, possibly empty {@link java.util.Set}
           containing any and all {@link Cluster} instances found
           within the given context. All objects in this list
           will be Cluster objects.
           @throws ConfigContext if there's a problem in accessing the
           context.
        */
    public static Set getClusters(final ConfigContext ctx) throws ConfigException {
        if (null == ctx) {
            return Collections.EMPTY_SET;
        }
        final Domain dom = (Domain) ctx.getRootConfigBean();
        if (null == dom){
            return Collections.EMPTY_SET;
        }
        final Clusters clusters = dom.getClusters();
        if (null == clusters || null == clusters.getCluster()){
            return Collections.EMPTY_SET;
        }
        final Set result = new HashSet();
        result.addAll(Arrays.asList(clusters.getCluster()));
        return result;
    }
        /**
           Get a set containing all the servers within the given
           context.
           @param ctx the context to be searched.
           @return a non-null, possibly empty {@link java.util.Set}
           containing any and all {@link Server} instances found
           within the given context. All objects in this list
           will be LbServer objects.
           @throws ServerContext if there's a problem in accessing the
           context.
        */
    public static Set getServers(final ConfigContext ctx) throws ConfigException {
        if (null == ctx) {
            return Collections.EMPTY_SET;
        }
        final Domain dom = (Domain) ctx.getRootConfigBean();
        if (null == dom){
            return Collections.EMPTY_SET;
        }
        final Servers servers = dom.getServers();
        if (null == servers || null == servers.getServer()){
            return Collections.EMPTY_SET;
        }
        final Set result = new HashSet();
        result.addAll(Arrays.asList(servers.getServer()));
        return result;
    }
        
        /**
           Get a set containing all the virtual servers within the given
           context.
           @param ctx the context to be searched.
           @return a non-null, possibly empty {@link java.util.Set}
           containing any and all {@link VirtualServer} instances found
           within the given context. All objects in this list
           will be VirtualServer objects.
           @throws ConfigContext if there's a problem in accessing the
           context.
        */
    public static Set getVirtualServers(final ConfigContext ctx) throws ConfigException {
        final Set result = new HashSet();
        for (final Iterator it = getHttpServices(ctx).iterator(); it.hasNext(); ){
            final ConfigBean [] vs = ((HttpService) it.next()).getVirtualServer();
            if (null != vs){
                result.addAll(Arrays.asList(vs));
            }
        }
        return result;
    }
        /**
           Get a set containing all the http services within the given
           context.
           @param ctx the context to be searched.
           @return a non-null, possibly empty {@link java.util.Set}
           containing any and all {@link HttpService} instances found
           within the given context. All objects in this list
           will be VirtualServer objects.
           @throws ConfigContext if there's a problem in accessing the
           context.
        */
    public static Set getHttpServices(final ConfigContext ctx) throws ConfigException {
        final Set result = new HashSet();
        for (final Iterator it = getConfigs(ctx).iterator(); it.hasNext(); ){
            result.add(((Config) it.next()).getHttpService());
        }
        return result;
    }
        /**
           Get all the cluster refs in the given context, wherever
           they are located.
           @param ctx the {@link ConfigContext} in which the search is
           to be conducted.
           @return a {@link java.util.Set} (never null, possibly
           empty) containing all {@link ClusterRef} beans within the
           given context.
           @throws ConfigException if there's a problem accesing the context.
         */
    public static Set getClusterRefs(final ConfigContext ctx) throws ConfigException {
        final Set lbconfigs = getLbConfigs(ctx);
        if (lbconfigs.isEmpty()){
            return Collections.EMPTY_SET;
        }
        final Set result = new HashSet();
        for (final Iterator it = lbconfigs.iterator(); it.hasNext();){
            final ConfigBean [] cr = ((LbConfig) it.next()).getClusterRef();
            if (null != cr){
                result.addAll(Arrays.asList(cr));
            }
        }
        return result;
    }

        /**
           Get a set containing all the connector resources within the
           given context.
           @param ctx the context to be searched.
           @return a non-null, possibly empty {@link java.util.Set}
           containing any and all {@link ConnectorResource} instances found
           within the given context. All objects in this list
           will be ConnectorResource objects.
           @throws ConfigContext if there's a problem in accessing the
           context.
        */

    public static Set getConnectorResources(final ConfigContext ctx) throws ConfigException {
        final Set result = new HashSet();
        final ConfigBean [] cr = getResources(ctx, ServerTags.CONNECTOR_RESOURCE);
        if (null != cr){
            result.addAll(Arrays.asList(cr));
        }
        return result;
    }
    

        /**
           Get a set containing all the lbconfigs within the given
           context.
           @param ctx the context to be searched.
           @return a non-null, possibly empty {@link java.util.Set}
           containing any and all {@link LbConfig} instances found
           within the given context. All objects in this list
           will be LbConfig objects.
           @throws ConfigContext if there's a problem in accessing the
           context.
        */
    public static Set getLbConfigs(final ConfigContext ctx) throws ConfigException {
        if (null == ctx) {
            return Collections.EMPTY_SET;
        }
        final Domain dom = (Domain) ctx.getRootConfigBean();
        if (null == dom){
            return Collections.EMPTY_SET;
        }
        final LbConfigs lbconfigs = dom.getLbConfigs();
        if (null == lbconfigs || null == lbconfigs.getLbConfig()){
            return Collections.EMPTY_SET;
        }
        final Set result = new HashSet();
        result.addAll(Arrays.asList(lbconfigs.getLbConfig()));
        return result;
    }
        
                    
   /**
    * Get virtual server given an Application
    * Virtual servers are maintained in the reference contained
    * in Server element. First, we need to find the server
    * and then get the virtual server from the correct reference
    *
    * @param ctx ConfigContext
    * @param appName Name of the app to get vs
    *
    * @return virtual servers as a string (separated by space or comma)
    *
    */
    public static String getVirtualServersByAppName(ConfigContext ctx,
                String appName)
                    throws ConfigException {

    ApplicationRef ar = getServerBean(ctx).getApplicationRefByRef(appName);
    if (ar == null) return null; //fixme should I throw an exception??

    return ar.getVirtualServers();


                                            /*
         VirtualServer[] vsList = getConfigBean(ctx).getHttpService().getVirtualServer();

         if (vsList == null) return null;

         String ret ="";
         for(int i=0;i< vsList.length; i++) {
             if(!ret.equals("")) {
                 ret+="'";
             }
            ret += vsList[i].getId();
         }

         return ret;
                                             */
    }
    /** Method to get all the MBean <i> definitions </i> from the domain.xml. These are just the MBeans that are available in 
     * the given domain. No consideration of references. This is to make sure that <i> names of applications, modules, mbeans
     * are unique in the domain, even if they're referenced from different servers. For example, following structure is to be deemed
     * invalid
     * &lt;applications&gt;
     *    &lt;mbean name="foo"&gt;
     *    &lt;mbean name="foo"&gt;
     * &lt;/applications&gt;
     */
    public static List<Mbean> getAllMBeanDefinitions(final ConfigContext cc) throws ConfigException {
        final Domain d          = ServerBeansFactory.getDomainBean(cc);
        final Applications as   = d.getApplications();
        final Mbean[] ma        = as.getMbean();
        final List<Mbean> ml    = Arrays.asList(ma);
        return ( Collections.unmodifiableList(ml) );
    }
    
    /** Method to get mbean definitions from domain.xml that are referenced from a given 
     * server name.
     * @param cc a config context
     * @param sn String representing the <i> name </i> of the server
     * @throws ConfigException
     */
    public static List<Mbean> getReferencedMBeans(final ConfigContext cc, final String sn) throws ConfigException {        
        final List<Mbean> em = new ArrayList<Mbean>();
        final Domain d = ServerBeansFactory.getDomainBean(cc);
        final Server[] ss = d.getServers().getServer();
        for(Server a : ss) {
            if (sn.equals(a.getName())) {
                ApplicationRef[] ars = a.getApplicationRef();
                for (ApplicationRef ar : ars) {
                    final Mbean tmp = getMBeanDefinition(cc, ar.getRef());
                    if (tmp != null)
                        em.add(tmp);
                }
            }
        }
        return ( Collections.unmodifiableList(em) );
    }
    
    public static boolean isReferencedMBean(final ConfigContext cc, final String sn, final String mn) throws ConfigException {
        final List<Mbean> rm = getReferencedMBeans(cc, sn);
        boolean refd = false;
        for (Mbean m : rm) {
            if (m.getName().equals(mn)) {
                refd = true;
                break;
            }
        }
        return ( refd );
    }

    /**
     * <ul>
     * <li>Returns true if all of the instances in the given cluster reference the given custom mbean
     * <li>Return false if all of the instances in the given cluster <i>do not</i> reference the given custom mbean
     * or if there are no instances in the cluster
     * <li>throw a ConfigException if some instances have the ref and some do not.
     * @param cc the config context
     * @param cn the cluster's name
     * @param mn the mbean's name
     * @throws com.sun.enterprise.config.ConfigException see above
     * @return see above
     */
    
    public static boolean isReferencedMBeanInCluster(final ConfigContext cc, final String cn, final String mn) throws ConfigException {
        // by definition a cluster has no referenced mbeans -- only instances in the cluster do.
        assert cc != null && cn != null && mn != null;
        // in case asserts are off, return false.  Normally I'd log it but this
        // file has no logger and I'm just visiting...
        if(cc == null || cn == null || mn == null)
            return false;
        
        final Server[] ss = ServerHelper.getServersInCluster(cc, cn);

        if(ss == null || ss.length < 1)
            return false;

        final boolean[] isrefs = new boolean[ss.length];
        
        for(int i = 0; i < ss.length; i++)
        {
            isrefs[i] = isReferencedMBean(cc, ss[i].getName(), mn);
        }

        // see if they are not all exactly the same -- simply compare the first
        // with all the others.
        for(int i = 1; i < ss.length; i++)
        {
            if(isrefs[i] != isrefs[0])
            {
                String messy = prepareMessyStringMessage(ss, isrefs);
                String msg = StringManager.getManager(ServerBeansFactory.class).getString("CorruptClusterConfig", 
                        new Object[] { cn, mn, messy} );
                throw new ConfigException(msg);
            }
        }
        return isrefs[0];   // they are all the same!
    }
    
    /** Method to get the completely enabled mbean definitions from the domain.xml, for a given server instance.
     * Note that an MBean is truly enabled if and only if it is enabled in its definition and a reference to its
     * definition from a server instance is enabled too.
     * @param cc server's ConfigContext
     * @param sn String representing name of a server instance
     * @return A List of Mbean elements. Never returns a null. Returns empty List in case no mbean is enabled
     * @throws ConfigException
     */
    public static List<Mbean> getFullyEnabledMBeans(final ConfigContext cc, final String sn) throws ConfigException {
        //while reaching here, if cc or sn are null, we are already in trouble
        final List<Mbean> em = new ArrayList<Mbean>();
        final Domain d = ServerBeansFactory.getDomainBean(cc);
        final Server[] ss = d.getServers().getServer();
        for(Server a : ss) {
            if (sn.equals(a.getName())) {
                ApplicationRef[] ars = a.getApplicationRef();
                for (ApplicationRef ar : ars) {
                    if (ar.isEnabled()) { // reference itself is enabled, this is generally not required, but to take care of some weird cases
                        final Mbean tmp = getMBeanDefinition(cc, ar.getRef());
                        if (tmp != null && tmp.isEnabled()) { // the definition is also enabled
                            //System.out.println(tmp.getName() + " will be loaded");
                            em.add(tmp);
                        }
                    }
                }
            }
        }
        return ( Collections.unmodifiableList(em) );
    }
    
    public static Mbean getMBeanDefinition(final ConfigContext cc, final String n) throws ConfigException {
        final Domain d          = ServerBeansFactory.getDomainBean(cc);
        final Applications a    = d.getApplications();
        final Mbean[] ms        = a.getMbean();
        for (Mbean m : ms) {
            if (n.equals(m.getName())) {
                return ( m );
            }
        }
        return ( null );
    }
    
    /** Method to get the completely enabled "user-defined" mbean definitions from the domain.xml, for a given server instance.
     * Note that an MBean is truly enabled if and only if it is enabled in its definition and a reference to its
     * definition from a server instance is enabled too.
     * @param cc server's ConfigContext
     * @param sn String representing name of a server instance
     * @return A List of Mbean elements. Never returns a null. Returns empty List in case no mbean is enabled
     * @throws ConfigException
     * @see #getFullyEnabledMBeans
     */
    public static List<Mbean> getFullyEnabledUserDefinedMBeans(final ConfigContext cc, final String sn) throws ConfigException {
        final List<Mbean> em    = ServerBeansFactory.getFullyEnabledMBeans(cc, sn);
        final List<Mbean> uem    = new ArrayList<Mbean>();
        for (Mbean m : em) {
           assert (m.isEnabled());
           if (IAdminConstants.USER.equals(m.getObjectType())) {
               uem.add(m);
           }
        }
        return ( Collections.unmodifiableList(uem) );
    }
    
    public static void addMbeanDefinition(final ConfigContext cc, final Mbean m) throws ConfigException {
        final Domain d          = ServerBeansFactory.getDomainBean(cc);
        final Applications as   = d.getApplications();
        as.addMbean(m);
    }
    
    public static void addMbeanReference(final ConfigContext cc, final String toThisMbean, final String server) throws ConfigException {
        final Domain d          = ServerBeansFactory.getDomainBean(cc);
        final Servers ss        = d.getServers();
        final Server s          = ss.getServerByName(server);
        final ApplicationRef ar = new ApplicationRef();
        ar.setRef(toThisMbean);
        ar.setEnabled(true);
        s.addApplicationRef(ar);
    }
    
    public static void addClusterMbeanReference(final ConfigContext cc, final String toThisMbean, final String clusterName) throws ConfigException {
        final Cluster cluster  = ClusterHelper.getClusterByName(cc, clusterName);
        final ApplicationRef ar = new ApplicationRef();
        ar.setRef(toThisMbean);
        ar.setEnabled(true);
        cluster.addApplicationRef(ar);
    }
    
    public static void removeMbeanDefinition(final ConfigContext cc, final String n) throws ConfigException {
        final Domain d          = ServerBeansFactory.getDomainBean(cc);
        final Applications as   = d.getApplications();
        as.removeMbean(as.getMbeanByName(n));
    }
    
    public static void removeMbeanReference(final ConfigContext cc, final String ref, final String server) throws ConfigException {
        final Domain d          = ServerBeansFactory.getDomainBean(cc);
        final Servers ss        = d.getServers();
        final Server s          = ss.getServerByName(server);
        final ApplicationRef ar = s.getApplicationRefByRef(ref);
        s.removeApplicationRef(ar);
    }

    public static void removeClusterMbeanReference(final ConfigContext cc, final String ref, final String clusterName) throws ConfigException {
        final Cluster cluster  = ClusterHelper.getClusterByName(cc, clusterName);
        final ApplicationRef ar = cluster.getApplicationRefByRef(ref);
        cluster.removeApplicationRef(ar);
    }

    public static Server[] getServersReferencingMBeanDefinition(final ConfigContext cc, final String name) throws ConfigException {
        //check to see if two or more servers have references to this mbean
        Server[] ss = null;
        final List<Server> list = new ArrayList<Server> ();
        ss = ServerBeansFactory.getDomainBean(cc).getServers().getServer();
        int numberOfReferencingServers = 0;
        for (Server s : ss) {
            boolean referenced = false;
            final String sName = s.getName();
            referenced = ServerBeansFactory.isReferencedMBean(cc, sName, name);
            if (referenced) {
                list.add(s);
            }
        }
        final Server[] rss = new Server[list.size()];
        return ( list.toArray(rss) );
    }
    private static String prepareMessyStringMessage(Server[] ss, boolean[] isrefs)
    {
        // the refs in this array of Servers are not the same.  I.e. we have a scary inconsistent
        // corrupted cluster configuration.  Let's painfully create a message for the user about it...
        StringBuilder sb = new StringBuilder();
        
        for(int i = 0; i < ss.length; i++)
        {
            if(i != 0)
                sb.append(", ");
            String s = isrefs[i] ? "referenced" : "not-referenced";
            sb.append(ss[i].getName()).append(":").append(s);
        }
        
        return sb.toString();
    }
}
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.NotificationListener;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.server.core.channel.ReconfigHelper;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigChange;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigUpdate;
import com.sun.enterprise.config.ConfigSet;
import com.sun.enterprise.config.ConfigAdd;
import com.sun.enterprise.config.ConfigDelete;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigChangeFactory;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import com.sun.enterprise.config.serverbeans.ResourceAdapterConfig;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.ServerContextImpl;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

//HERCULES:add
import java.util.HashSet;
import java.util.Set;
//end HERCULES:add

// distributed notification
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.admin.event.pluggable.NotificationFactory;

import com.sun.enterprise.admin.common.MBeanServerFactory;

import javax.management.ObjectInstance;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This class multicasts all received events to appropriate event listeners.
 */
public class AdminEventMulticaster {

    /**
     * Logger for admin service
     */
    static Logger logger = Logger.getLogger(AdminConstants.kLoggerName);

    private static AdminEventMulticaster adminEventMulticaster = null;

    /**
     * Hashmap of listeners, the keys are event names.
     */
    private HashMap listeners;

    /**
     * Hashmap of config categories, the keys are ConfigChangeEventListeners
     */
    private HashMap configCategoryList;

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( AdminEventMulticaster.class );

    /**
     * Private constructor
     */
    private AdminEventMulticaster() {
        listeners = new HashMap();
        listeners.put(AdminEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(ConfigChangeEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(MonitoringEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(ShutdownEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(BaseDeployEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(ApplicationDeployEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(ModuleDeployEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(ResourceDeployEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(LogLevelChangeEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(MonitoringLevelChangeEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(AdminEventListenerRegistry.notificationEventType,
                new CopyOnWriteArrayList());
        listeners.put(AuthRealmEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(AuditModuleEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(SecurityServiceEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(ElementChangeEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(UserMgmtEvent.eventType, new CopyOnWriteArrayList());
        listeners.put(EjbTimerEvent.eventType, new CopyOnWriteArrayList());
        configCategoryList = new HashMap();
    }

    /**
     * Get AdminEventMulticaster.
     */
    public static synchronized AdminEventMulticaster getInstance() {
        if (adminEventMulticaster == null) {
            adminEventMulticaster = create();
        }
        return adminEventMulticaster;
    }

    private static synchronized AdminEventMulticaster create() {
        return new AdminEventMulticaster();
    }

    /**
     * Multicast specified event to all registered listeners.
     */
    public static AdminEventResult multicastEvent(AdminEvent event) {
        AdminEventResult result = null;
        AdminEventMulticaster aem = getInstance();

        if ((AdminService.getAdminService() == null)
                || isLocal(event) || isLocalOld(event)) {

            event = deferredEventResolution(event);
            sendPreNotifyReconfigSignal(event);
            aem.processEvent(event);
            sendPostNotifyReconfigSignal(event);

            if (AdminService.getAdminService() != null) {
                // it send events to local JMX listeners
                getInstance().sendNotification(event);
            }

            result = AdminEventResult.getAdminEventResult(event);

            // The following code sets restart required to true, in case of
            // event processing error
            RRPersistenceHelper helper = new RRPersistenceHelper();
            helper.setRestartRequiredForServer(event, result);

            AdminEventResult.clearAdminEventResultFromCache(event);

        } else {
            PluggableFeatureFactory featureFactory =
              ApplicationServer.getServerContext().getPluggableFeatureFactory();

            NotificationFactory nFactory = 
                featureFactory.getNotificationFactory();
            EventDispatcher eDispatcher = nFactory.createEventDispatcher();

            result = eDispatcher.dispatch(event);
        } /*else {
            String currInstance = AdminService.getAdminService().getInstanceName();
            logger.log(Level.INFO, MULTICAST_NOT_SUPPORTED,
                     new String[] {currInstance, event.getInstanceName()});
			String msg = localStrings.getString( "admin.event.unable_to_multicast_events" );
            throw new UnsupportedOperationException( msg );
        }*/
        return result;
    }

    /**
     * Check whether the event is for this instance.
     * FIXME: remove this after moving to new code
     */
    private static boolean isLocalOld(AdminEvent event) {
        String source    = event.getInstanceName();
        String instName  = AdminService.getAdminService().getInstanceName();
        String tDest     = event.getTargetDestination();

        // target destination is populated only in the new version
        if ((instName != null) 
                && instName.equals(source) 
                && (tDest == null || tDest.length()==0)) {

            return true;
        } else {
            return false;
        }
    }

    private static boolean isLocal(AdminEvent event) {
        String source = event.getTargetDestination();
        String instName = 
            ApplicationServer.getServerContext().getInstanceName();

        String effective = event.getEffectiveDestination();
        
        // XXX FIXME: multiple destinations
        // if target destination is same as the current server instance
        // then broadcast the event locally.
        if (instName != null && instName.equals(source)) {
            return true;
        } else if (instName != null && instName.equals(effective)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Send a notification to non-java code prior to sending java notification.
     * This is required while sending a ConfigChangeEvent to another instance.
     */
    private static void sendPreNotifyReconfigSignal(AdminEvent event) {
        if (event instanceof ConfigChangeEvent) {
            ConfigChangeEvent cce = (ConfigChangeEvent)event;
            if (cce.isWebCoreReconfigNeeded()) {
                ReconfigHelper.sendReconfigMessage(event.getInstanceName());
            }
        }
    }

    /**
     * Send a notification to non-java code after sending java notification.
     * This is required while sending, a ApplicationDeployEvent or a
     * ModuleDeployEvent for web modules, to another instance.
     */
    private static void sendPostNotifyReconfigSignal(AdminEvent event) {
        if (event instanceof ApplicationDeployEvent
                || (event instanceof ModuleDeployEvent
                        && ((ModuleDeployEvent)event).getModuleType().equals(
                                ModuleDeployEvent.TYPE_WEBMODULE))) {
            ReconfigHelper.sendReconfigMessage(event.getInstanceName());
        }
    }

    /**
     * Notify failure in event handling. This method will typically be called
     * by admin event handling components, in case it gets exceptions or
     * errors from the listeners. However, in one case the listeners can call
     * it directly and that is when they want to notify the event handling
     * system that the event can not be handled and the user should restart
     * the application server to see the effects of the event. The event
     * framework will only handle error codes specified in AdminEventResult.
     * @param event the event that could not be processed
     * @param failureCode one of AdminEventResult.RESTART_NEEDED,
     *     AdminEventResult.RUNTIME_EXCEPTION, AdminEventResult.RUNTIME_ERROR,
     *     AdminEventResult.MBEAN_NOT_FOUND
     */
    public static void notifyFailure(AdminEvent event, String failureCode) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        result.setResultCode(failureCode);
    }

    /**
     * Add specified attribute to result of specified event, The listeners for
     * admin events can use this method to add attributes to the event result
     * sent back to the admin server. The admin server can then process these
     * attributes and take appropriate action. Since the result is sent back
     * to admin server over RMI, all attributes added must be serializable.
     * @param event the event that is being processed.
     * @param name name of the attribute
     * @param value value of the attribute
     * @throws IllegalArgumentException if attribute name is null or attribute
     *    value is not Serializable.
     */
    public static void addEventResultAttribute(AdminEvent event, String name,
            Object value) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        result.addAttribute(event.getEffectiveDestination(),name, value);
    }

    /**
     * Get value of specified attribute name from the results for specified
     * event. The method may return null if the attribute was never added or
     * a null value was associated explicitly to the specified name,
     * @param event the event that is being processed.
     * @param name name of the attribute
     * @throws IllegalArgumentException if attribute name is null.
     * @return value of the specified attribute.
     */
    public static Object getEventResultAttribute(AdminEvent event, String name) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        return result.getAttribute(event.getEffectiveDestination(),name);
    }

    /**
     * Remove specified attribute from results of the specified event.
     * @param event the event that is being processed.
     * @param name name of the attribute
     * @throws IllegalArgumentException if attribute name is null.
     */
    public static void removeEventResultAttribute(AdminEvent event, String name) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        result.removeAttribute(event.getEffectiveDestination(),name);
    }

    /**
     * This method has been added for JMX compatibility and is not yet
     * implemented.
     */
    public static void addNotificationListener(NotificationListener listener) {
    }

    /**
     * This method has been added for JMX compatibility and is not yet
     * implemented.
     */
    public static void removeNotificationListener(NotificationListener listener) {
    }

    /**
     * Add a listener of specified type.
     */
    static void addListener(String type, AdminEventListener listener) {
        AdminEventMulticaster aem = getInstance();
        List list = (List)aem.listeners.get(type);
        synchronized ( aem.listeners) {
            // If this impls  array for event type does not exist. 
            // Following creates it dynamically for this event type.
            if ( list == null ) {
                list = new CopyOnWriteArrayList();
                aem.listeners.put(type, list);
            }
        }
        synchronized (list) {
            list.add(listener);
        }
    }

    /**
     * Add a listener for ConfigChangeEvent that will be ivoked only when
     * the config changes match the specified config change category.
     */
    static void addListener(String type, ConfigChangeCategory category,
            ConfigChangeEventListener listener) {
        addListener(type, listener);
        AdminEventMulticaster aem = getInstance();
        synchronized (aem.configCategoryList) {
            aem.configCategoryList.put(listener, category);
        }
    }

    /**
     * Remove specified listener
     * HERCULES:mod
     * Fixed version takes into account that the same listener
     * may be registered for multiple event types
     */
    static void removeListener(AdminEventListener listener) {
        AdminEventMulticaster aem = getInstance();
        //String type = aem.getEventTypeFromListener(listener);
        String[] types = aem.getEventTypesFromListener(listener);
        //if (type != null) {
        for (int i=0; i<types.length; i++) {
            String type = (String)types[i];
            List list = (List)aem.listeners.get(type);
            if (list.contains(listener)) {
                synchronized (list) {
                    list.remove(listener);
                }
                if (type.equals(ConfigChangeEvent.eventType)) {
                    if (aem.configCategoryList.containsKey(listener)) {
                        synchronized (aem.configCategoryList) {
                            aem.configCategoryList.remove(listener);
                        }
                    }
                }
            }
        }
    }    

    /**
     * Utility method to determine event type(s) from an event listener.
     * HERCULES:add really part of fix for removeListener
     */
    private static String[] getEventTypesFromListener(AdminEventListener listener) {
        Class[] interfaces = listener.getClass().getInterfaces();
        Set typesSet = new HashSet();

        for (int i = 0; i < interfaces.length; i++) {
            String className = interfaces[i].getName();
            if (className.startsWith(ApplicationDeployEvent.eventType)) {
                typesSet.add(ApplicationDeployEvent.eventType);
            } else if (className.startsWith(ModuleDeployEvent.eventType)) {
                typesSet.add(ModuleDeployEvent.eventType);
            } else if (className.startsWith(ResourceDeployEvent.eventType)) {
                typesSet.add(ResourceDeployEvent.eventType);
            } else if (className.startsWith(BaseDeployEvent.eventType)) {
                typesSet.add(BaseDeployEvent.eventType);
            } else if (className.startsWith(ConfigChangeEvent.eventType)) {
                typesSet.add(ConfigChangeEvent.eventType);
            } else if (className.startsWith(MonitoringEvent.eventType)) {
                typesSet.add(MonitoringEvent.eventType);
            } else if (className.startsWith(ShutdownEvent.eventType)) {
                typesSet.add(ShutdownEvent.eventType);
            } else if (className.startsWith(AdminEvent.eventType)) {
                typesSet.add(AdminEvent.eventType);
            }
        }

        String[] template = new String[typesSet.size()];
        String[] types = (String[]) typesSet.toArray(template);
        return types;
    }    

    /**
     * Process event by invoking appropriate listener(s)
     */
    void processEvent(AdminEvent event) {
        String eventType = event.getType();
        boolean inited = initEventHandler(event);
        if (!inited) {
            // Do not process event if initialization failed, because listeners
            // are dependent on successful initlization of context in event
            return;
        }
        int lstnrCnt = 0;
        if (eventType.equals(ApplicationDeployEvent.eventType)) {
            lstnrCnt += handleApplicationDeployEvent(event);
            lstnrCnt += handleBaseDeployEvent(event);
        } else if (eventType.equals(ModuleDeployEvent.eventType)) {
            lstnrCnt += handleModuleDeployEvent(event);
            lstnrCnt += handleBaseDeployEvent(event);
        } else if (eventType.equals(ResourceDeployEvent.eventType)) {
            lstnrCnt += handleResourceDeployEvent(event);
            lstnrCnt += handleBaseDeployEvent(event);
        } else if (eventType.equals(BaseDeployEvent.eventType)) {
            lstnrCnt += handleBaseDeployEvent(event);
        } else if (eventType.equals(ConfigChangeEvent.eventType)) {
            lstnrCnt += handleConfigChangeEvent(event);
        } else if (eventType.equals(MonitoringEvent.eventType)) {
            lstnrCnt += handleMonitoringEvent(event);
        } else if (eventType.equals(MonitoringLevelChangeEvent.eventType)) {
            lstnrCnt += handleMonitoringLevelChangeEvent(event);
        } else if (eventType.equals(LogLevelChangeEvent.eventType)) {
            lstnrCnt += handleLogLevelChangeEvent(event);
        } else if (eventType.equals(ShutdownEvent.eventType)) {
            lstnrCnt += handleShutdownEvent(event);
        } else if (eventType.equals(AuthRealmEvent.eventType)) {
            lstnrCnt += handleAuthRealmEvent(event);
        } else if (eventType.equals(AuditModuleEvent.eventType)) {
            lstnrCnt += handleAuditModuleEvent(event);
        } else if (eventType.equals(SecurityServiceEvent.eventType)) {
            lstnrCnt += handleSecurityServiceEvent(event);
        } else if (eventType.equals(ElementChangeEvent.eventType)) {
            lstnrCnt += handleElementChangeEvent(event);
        } else if (eventType.equals(UserMgmtEvent.eventType)) {
            lstnrCnt += handleUserMgmtEvent(event);
        } else if (eventType.equals(EjbTimerEvent.eventType)) {
            lstnrCnt += handleEjbTimerEvent(event);
        } else {
            BaseAdminEventHandler  handler = new BaseAdminEventHandler(event);
            lstnrCnt += handler.processEvent();
        }
        if (lstnrCnt == 0) {
            handleNoListeners(event);
        }
        destroyEventHandler(event);
    }

    /**
     * Initialize event handler. Invoked just before event processing starts.
     * @return true if initialization is successful, false otherwise
     */
    private boolean initEventHandler(AdminEvent event) {
        //Begin EE: 4921345 Remove dependency on AdminService
        if (AdminService.getAdminService() == null) {
            return true;
        }
        //End EE: 4921345 Remove dependency on AdminService
        boolean success = true;
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        ConfigContext global =
                AdminService.getAdminService().getContext().getConfigContext();
        ConfigContext local = null;
        if ( (event.configChangeList != null &&
              event.configChangeList.size() > 0) ||
             (event.dependentChangeList != null &&
              event.dependentChangeList.size() > 0) ) {
            try {
                local = (ConfigContext)global.clone();

                // if dependent changes fails, it will be ignored
                applyDependentChangesToContext(local,event.dependentChangeList);

                applyChangesToContext(local, event.configChangeList);
            } catch (ConfigException ce) {
                result.setResultCode(AdminEventResult.CONFIG_SNAPSHOT_ERROR);
                result.addException(event.getEffectiveDestination(),ce);
                logger.log(Level.FINE, HANDLER_INIT_ERROR, ce.getMessage());
                debug(getStackTraceFromThrowable(ce));
                success = false;
            } catch (Throwable t) {
                handleError(event, t, result, HANDLER_INIT_ERROR);
                success = false;
            }
        } else {
            local = global;
        }
        event.setOldContext(global);
        event.setContext(local);
        return success;
    }

    /**
     * Sets the type of resource or application. Adds dependent 
     * config change objects. 
     * <p>
     * Note: This method is called when event reaches the remote server
     * 
     * @param  event  event that is about to be forwarded to the listener
     * @return  updated event
     */
    private static AdminEvent deferredEventResolution(AdminEvent event) {

        if (AdminService.getAdminService() == null) {
            return event;
        }

        ConfigContext ctx =
                AdminService.getAdminService().getContext().getConfigContext();

        // some event adjustmenets with effective context
        if (event instanceof ResourceDeployEvent) {

            // possibly event needs "deffered" type resoluion
            ResourceDeployEvent res = (ResourceDeployEvent) event;

            String type = res.getResourceType();
            if (type==null) {
                try {
                    type = EventBuilder.getResourceTypeByName(ctx,
                            res.getResourceName(), res.getTargetDestination());
                    res.setResourceType(type);
                } catch (Exception e) { }
            }

            // get rid of the resource from domain level for undeploy event
            try {
                String action = res.getAction();
                if (BaseDeployEvent.REMOVE_REFERENCE.equals(action) 
                        || BaseDeployEvent.UNDEPLOY.equals(action)) {
                        
                    String effective = res.getEffectiveDestination();
                    String resName = res.getResourceName();
                    DependencyResolver dr = 
                            new DependencyResolver(ctx, effective);
                    List list = dr.getConfigDeleteForResource(resName, type);
                    res.addDependentConfigChange(list);

                    // adds dependencies for pools if pool is not referenced

                    if ((type != null) && type.equals(res.RES_TYPE_JDBC)) {

                        ConfigBean jRes = dr.findResource(resName, type);

                        if (jRes instanceof JdbcResource) {
                            String pool = ((JdbcResource) jRes).getPoolName();
                            String server = AdminService.
                                getAdminService().getInstanceName();

                            if (!isJdbcPoolReferenced(ctx, pool, server,
                                                      resName)) {

                                res.addDependentConfigChange( 
                                    dr.getConfigDeleteForResource(pool, 
                                        ResourceDeployEvent.RES_TYPE_JCP) );
                            }
                        }

                    // connector connection pool
                    } else if ((type != null) && type.equals(res.RES_TYPE_CR)) {

                        ConfigBean cRes = dr.findResource(resName, type);

                        if (cRes instanceof ConnectorResource) {
                            String pool =
                                ((ConnectorResource) cRes).getPoolName();
                            String server = AdminService.
                                getAdminService().getInstanceName();

                            if (!isConnectorPoolReferenced(ctx, pool, server, 
                                                           resName)) {

                                res.addDependentConfigChange( 
                                    dr.getConfigDeleteForResource(pool,
                                        ResourceDeployEvent.RES_TYPE_CCP) );
                            }
                        }
                    }
                }
            } catch (Exception e) { }

        } else if (event instanceof BaseDeployEvent) {

            // possibly event needs "deffered" type resoluion
            BaseDeployEvent app = (BaseDeployEvent) event;
            String type = app.getJ2EEComponentType();
            if (type==null) {
                try {
                    event = EventBuilder.resolveModAppDeployEventType(app, ctx);
                } catch (Exception e) { }
            }

            // removes the application from the doman level for undeploy
            try {
                String action = app.getAction();
                if (BaseDeployEvent.REMOVE_REFERENCE.equals(action) 
                        || BaseDeployEvent.UNDEPLOY.equals(action)) {
                        
                    String effective = app.getEffectiveDestination();
                    String appName = app.getJ2EEComponentName();
                    DependencyResolver dr = 
                            new DependencyResolver(ctx, effective);
                    List list = dr.getConfigDeleteForApplication(appName);
                    app.addDependentConfigChange(list);

                    // remove resource adapter config if found
                    ResourceAdapterConfig raConfig = 
                        dr.findResourceAdapterConfigByName(appName);

                    if (raConfig != null) {
                        app.addDependentConfigChange( 
                            dr.getConfigDeleteForResource(raConfig) );
                    }
                }
            } catch (Exception e) { }
        }

        return event;
    }

    /**
     * Returns true if the named pool has a reference from a jdbc resource
     * that is used by the given server instance. 
     *
     * @param   ctx   config context
     * @param   poolName   jdbc resource pool name
     * @param   serverName  name of the server instance
     * @param   eResName  name of excluded resource
     *
     * @return  true if the pool is used by the server instance
     *
     * @throw   ConfigException  if an error while parsing domain.xml
     */
    private static boolean isJdbcPoolReferenced(ConfigContext ctx, 
            String poolName, String serverName, String eResName) 
            throws ConfigException {

        if (ctx==null || poolName==null || serverName==null || eResName==null) {
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
                String pool   = jdbcBeans[i].getPoolName();
                String rName  = jdbcBeans[i].getJndiName();

                // pool is used by another resource
                if ( (pool != null) && pool.equals(poolName) 
                        && (!eResName.equals(rName)) ) {

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
     * @param   eResName  name of excluded resource name
     *
     * @return  true if the pool is used by the server instance
     *
     * @throw   ConfigException  if an error while parsing domain.xml
     */
    private static boolean isConnectorPoolReferenced(ConfigContext ctx, 
            String poolName, String serverName, String eResName) 
            throws ConfigException {

        if (ctx==null || poolName==null || serverName==null || eResName==null) {
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
                String pool   = conBeans[i].getPoolName();
                String rName  = conBeans[i].getJndiName();

                // pool is used by another resource
                if ( (pool != null) && pool.equals(poolName) 
                        && (!eResName.equals(rName))) {

                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Applies dependent config changes if necessary.
     */
    private void applyDependentChangesToContext(ConfigContext ctx, List list) {
        try {
            if (list != null) {
                applyChangesToContext(ctx, (List)list);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Apply changes to the config context. Changes from the even are applied
     * to the specified context.
     * @deprecated <code>ConfigContext</code> is now refreshed using <code>
     * ConfigChange</code> objects in the event.
     */
    private void applyChangesToContext(ConfigContext configContext,
            List changeList) throws ConfigException {
        Iterator iter = changeList.iterator();
        ArrayList updates = new ArrayList();
        while (iter.hasNext()) {
            ConfigChange change = (ConfigChange)iter.next();
            if (change instanceof ConfigUpdate) {
                updates.add(change);
            } else {
                if(change instanceof ConfigAdd)
                {
                    // here we are trying to avoid config elements duplication  
                    // especially for applying of "dependent" changes
                    // this temporary solution should be removed after Toby will implement
                    // config-level validator rejecting such changes
                    try
                    {
                        if(ConfigBeansFactory.getConfigBeanByXPath(configContext, change.getXPath())!=null)
                        {
                            try
                            {
                                // we delete existing bean to get fresh copy (it is esp. significant for deployedModules)
                                ConfigDelete delete_prior = ConfigChangeFactory.createConfigDelete(change.getXPath());
                                configContext.updateFromConfigChange(delete_prior);
                            } catch(Exception e) {
                                logger.log(Level.WARNING, "event.delete_prior_ctx_failed", e);
                            } 
                        }
                    }
                    catch(Exception e) {
                        logger.log(Level.WARNING, "event.config_add_ctx_failed", e);
                    }
                   
                }
                //fix for bug# 6581710
                //if the application-ref is not meant for this instance 
                //then do not throw any exception
                try {
                    configContext.updateFromConfigChange(change);
                } catch (ConfigException ce) {
                    if (isAppRefConfigChangeForThisInst(change)) throw ce; 
                }
            }
        }
        iter = updates.iterator();
        while (iter.hasNext()) {
            ConfigUpdate update = (ConfigUpdate)iter.next();
            configContext.updateFromConfigChange(update);
        }
    }

    /**
     * Destroy event handler. Invoked after event processing is completed
     */
    private void destroyEventHandler(AdminEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        if (AdminEventResult.SUCCESS.equals(result.getResultCode())) {
            ServerContext ctx = AdminService.getAdminService().getContext();

            if (ctx instanceof ServerContextImpl) {
                ConfigContext oldCtx = ctx.getConfigContext();
                ConfigContext newCtx = event.getConfigContext();
                
                ((ServerContextImpl) ctx).setConfigContext(newCtx);
                
                // replace the old config context with the new one from 
                // ConfigFactory cache
                try {
                    ConfigFactory.replaceConfigContext(oldCtx, newCtx);
                } catch(Exception e) {
                    //ignore for now
                    logger.log(Level.WARNING, "event.replace_ctx_failed", e);
                }
                
            if (!AdminService.getAdminService().isDas()) {
                try {
                     AdminService.getAdminService().getContext().
                        getConfigContext().flush();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "event.flush_failed");
                }
            }

            } else {
                logger.log(Level.SEVERE, "event.unknown_serverctx_type", 
                           ctx.getClass().getName());
            }
        }
    }

    /**
     * Get listeners for specified event type
     */
    List getListeners(String type) {
        return (List)listeners.get(type);
    }

    /**
     * Handle application deployment event.
     * @return number of listeners invoked
     */
    private int handleApplicationDeployEvent(AdminEvent event) {
        ApplicationDeployEvent ade = (ApplicationDeployEvent)event;
        List list = getListeners(ApplicationDeployEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                ApplicationDeployEventListener listener =
                        (ApplicationDeployEventListener)iter.next();
                invokeApplicationDeployEventListener(listener, ade);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    /**
     * Invoke specified application deployment event listener
     */
    private void invokeApplicationDeployEventListener(
            ApplicationDeployEventListener listener,
            ApplicationDeployEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            String action = event.getAction();
            if (BaseDeployEvent.DEPLOY.equals(action)) {
                listener.applicationDeployed(event);
            } else if (BaseDeployEvent.UNDEPLOY.equals(action)) {
                listener.applicationUndeployed(event);
            } else if (BaseDeployEvent.REDEPLOY.equals(action)) {
                listener.applicationRedeployed(event);
            } else if (BaseDeployEvent.ENABLE.equals(action)) {
                listener.applicationEnabled(event);
            } else if (BaseDeployEvent.DISABLE.equals(action)) {
                listener.applicationDisabled(event);
            } 
         
            else if (BaseDeployEvent.ADD_REFERENCE.equals(action)) {
                listener.applicationReferenceAdded(event);
            } else if (BaseDeployEvent.REMOVE_REFERENCE.equals(action)) {
                listener.applicationReferenceRemoved(event);
            }
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }

    /**
     * Handle module deployment event.
     * @return number of listeners invoked
     */
    private int handleModuleDeployEvent(AdminEvent event) {
        ModuleDeployEvent mde = (ModuleDeployEvent)event;
        List list = getListeners(ModuleDeployEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                ModuleDeployEventListener listener =
                        (ModuleDeployEventListener)iter.next();
                invokeModuleDeployEventListener(listener, mde);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    private void invokeModuleDeployEventListener(
            ModuleDeployEventListener listener,
            ModuleDeployEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            String action = event.getAction();
            if (BaseDeployEvent.DEPLOY.equals(action)) {
                listener.moduleDeployed(event);
            } else if (BaseDeployEvent.UNDEPLOY.equals(action)) {
                listener.moduleUndeployed(event);
            } else if (BaseDeployEvent.REDEPLOY.equals(action)) {
                listener.moduleRedeployed(event);
            } else if (BaseDeployEvent.ENABLE.equals(action)) {
                listener.moduleEnabled(event);
            } else if (BaseDeployEvent.DISABLE.equals(action)) {
                listener.moduleDisabled(event);
            } 
            
            else if (BaseDeployEvent.ADD_REFERENCE.equals(action)) {
                listener.moduleReferenceAdded(event);
            } else if (BaseDeployEvent.REMOVE_REFERENCE.equals(action)) {
                listener.moduleReferenceRemoved(event);
            }
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }

    /**
     * Handle resource deployment event.
     * @return number of listeners invoked
     */
    private int handleResourceDeployEvent(AdminEvent event) {

        ResourceDeployEvent rde = (ResourceDeployEvent)event;
        List list = getListeners(ResourceDeployEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                ResourceDeployEventListener listener =
                        (ResourceDeployEventListener)iter.next();
                invokeResourceDeployEventListener(listener, rde);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    private void invokeResourceDeployEventListener(
            ResourceDeployEventListener listener,
            ResourceDeployEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            String action = event.getAction();
            if (BaseDeployEvent.DEPLOY.equals(action)) {
                listener.resourceDeployed(event);
            } else if (BaseDeployEvent.UNDEPLOY.equals(action)) {
                listener.resourceUndeployed(event);
            } else if (BaseDeployEvent.REDEPLOY.equals(action)) {
                listener.resourceRedeployed(event);
            } else if (BaseDeployEvent.ENABLE.equals(action)) {
                listener.resourceEnabled(event);
            } else if (BaseDeployEvent.DISABLE.equals(action)) {
                listener.resourceDisabled(event);
            } else if (BaseDeployEvent.ADD_REFERENCE.equals(action)) {
                listener.resourceReferenceAdded(event);
            } else if (BaseDeployEvent.REMOVE_REFERENCE.equals(action)) {
                listener.resourceReferenceRemoved(event);
            }
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }

    /**
     * Handle application deployment event.
     * @return number of listeners invoked
     */
    private int handleBaseDeployEvent(AdminEvent event) {
        BaseDeployEvent bde = (BaseDeployEvent)event;
        List list = getListeners(BaseDeployEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                BaseDeployEventListener listener =
                        (BaseDeployEventListener)iter.next();
                invokeBaseDeployEventListener(listener, bde);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    private void invokeBaseDeployEventListener(
            BaseDeployEventListener listener, BaseDeployEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            String componentType = event.getJ2EEComponentType();
            String action = event.getAction();
            if (BaseDeployEvent.APPLICATION.equals(componentType)) {
                if (BaseDeployEvent.DEPLOY.equals(action)) {
                    listener.applicationDeployed(event);
                } else if (BaseDeployEvent.UNDEPLOY.equals(action)) {
                    listener.applicationUndeployed(event);
                } else if (BaseDeployEvent.REDEPLOY.equals(action)) {
                    listener.applicationRedeployed(event);
                } else if (BaseDeployEvent.ENABLE.equals(action)) {
                    listener.applicationEnabled(event);
                } else if (BaseDeployEvent.DISABLE.equals(action)) {
                    listener.applicationDisabled(event);
                } 
                else if (BaseDeployEvent.ADD_REFERENCE.equals(action)) {
                    listener.applicationReferenceAdded(event);
                } else if (BaseDeployEvent.REMOVE_REFERENCE.equals(action)) {
                    listener.applicationReferenceRemoved(event);
                }
            } else if (BaseDeployEvent.MODULE.equals(componentType)) {
                if (BaseDeployEvent.DEPLOY.equals(action)) {
                    listener.moduleDeployed(event);
                } else if (BaseDeployEvent.UNDEPLOY.equals(action)) {
                    listener.moduleUndeployed(event);
                } else if (BaseDeployEvent.REDEPLOY.equals(action)) {
                    listener.moduleRedeployed(event);
                } else if (BaseDeployEvent.ENABLE.equals(action)) {
                    listener.moduleEnabled(event);
                } else if (BaseDeployEvent.DISABLE.equals(action)) {
                    listener.moduleDisabled(event);
                } 
                
                else if (BaseDeployEvent.ADD_REFERENCE.equals(action)) {
                    listener.moduleReferenceAdded(event);
                } else if (BaseDeployEvent.REMOVE_REFERENCE.equals(action)) {
                    listener.moduleReferenceRemoved(event);
                }
            } else if (BaseDeployEvent.RESOURCE.equals(componentType)) {
                if (BaseDeployEvent.DEPLOY.equals(action)) {
                    listener.resourceDeployed(event);
                } else if (BaseDeployEvent.UNDEPLOY.equals(action)) {
                    listener.resourceUndeployed(event);
                } else if (BaseDeployEvent.REDEPLOY.equals(action)) {
                    listener.resourceRedeployed(event);
                } else if (BaseDeployEvent.ENABLE.equals(action)) {
                    listener.resourceEnabled(event);
                } else if (BaseDeployEvent.DISABLE.equals(action)) {
                    listener.resourceDisabled(event);
                } 
                
                else if (BaseDeployEvent.ADD_REFERENCE.equals(action)) {
                    listener.resourceReferenceAdded(event);
                } else if (BaseDeployEvent.REMOVE_REFERENCE.equals(action)) {
                    listener.resourceReferenceRemoved(event);
                }
            }
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }

    /**
     * Handle config change event.
     * @return number of listeners invoked
     */
    private int handleConfigChangeEvent(AdminEvent event) {
        int listenersInvoked = 0;
        ConfigChangeEvent cce = (ConfigChangeEvent)event;
        List list = getListeners(ConfigChangeEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                ConfigChangeEventListener listener =
                        (ConfigChangeEventListener)iter.next();
                ConfigChangeCategory category = (ConfigChangeCategory)
                        configCategoryList.get(listener);
                boolean match = true;
                if (category != null) {
                    match = cce.matchXPathToPattern(
                            category.getConfigXPathPattern());
                }
                if (match) {
                    invokeConfigChangeEventListener(listener, cce);
                    ++listenersInvoked;
                }
            }
        }
        if (!cce.isAllXPathMatched()) {
            AdminEventResult result = AdminEventResult.getAdminEventResult(cce);
            //result.setResultCode(AdminEventResult.RESTART_NEEDED);
        }
        return listenersInvoked;
    }

    private void invokeConfigChangeEventListener(
            ConfigChangeEventListener listener, ConfigChangeEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            listener.configChanged(event);
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }

    /**
     * Handle monitoring event
     * @return number of listeners invoked
     */
    private int handleMonitoringEvent(AdminEvent event) {
        MonitoringEvent me = (MonitoringEvent)event;
        List list = getListeners(MonitoringEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                MonitoringEventListener listener =
                        (MonitoringEventListener)iter.next();
                invokeMonitoringEventListener(listener, me);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    private void invokeMonitoringEventListener(
            MonitoringEventListener listener,
            MonitoringEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            String action = event.getActionCode();
            if (MonitoringEvent.START_MONITORING.equals(action)) {
                listener.startMonitoring(event);
            } else if (MonitoringEvent.STOP_MONITORING.equals(action)) {
                listener.stopMonitoring(event);
            } else if (MonitoringEvent.GET_MONITOR_DATA.equals(action)) {
                listener.getMonitoringData(event);
            } else if (MonitoringEvent.LIST_MONITORABLE.equals(action)) {
                listener.listMonitorable(event);
            } else if (MonitoringEvent.SET_MONITOR_DATA.equals(action)) {
                listener.setMonitoringData(event);
            }
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }

    //********************************************************
    /**
     * Handle ElementChange event
     * @return number of listeners invoked
     */
    private int handleElementChangeEvent(AdminEvent event) {
        ElementChangeEvent me = (ElementChangeEvent)event;
        List list = getListeners(ElementChangeEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                ElementChangeEventListener listener =
                        (ElementChangeEventListener)iter.next();
                invokeElementChangeEventListener(listener, me);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    private void invokeElementChangeEventListener(
            ElementChangeEventListener listener,
            ElementChangeEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            listener.processEvent(event);
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }


    //********************************************************
    /**
     * Handle UserMgmt event
     * @return number of listeners invoked
     */
    private int handleUserMgmtEvent(AdminEvent event) {
        UserMgmtEvent me = (UserMgmtEvent)event;
        List list = getListeners(UserMgmtEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                UserMgmtEventListener listener =
                        (UserMgmtEventListener)iter.next();
                invokeUserMgmtEventListener(listener, me);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    private void invokeUserMgmtEventListener(
            UserMgmtEventListener listener,
            UserMgmtEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            int action = event.getActionType();
            if(action==event.ACTION_USERADD){
                listener.userAdded(event);
            } else if(action==event.ACTION_USERUPDATE) {
                listener.userUpdated(event);
            } else if(action==event.ACTION_USERREMOVE) {
                listener.userRemoved(event);
            }
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }
    //********************************************************
    /**
     * Handle EjbTimer event
     * @return number of listeners invoked
     */
    private int handleEjbTimerEvent(AdminEvent event) {
        EjbTimerEvent me = (EjbTimerEvent)event;
        List list = getListeners(EjbTimerEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                EjbTimerEventListener listener =
                        (EjbTimerEventListener)iter.next();
                invokeEjbTimerEventListener(listener, me);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    private void invokeEjbTimerEventListener(
            EjbTimerEventListener listener,
            EjbTimerEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            int action = event.getActionType();
            if(action==event.ACTION_MIGRATETIMER){
                int iCount = listener.migrateTimer(event, event.getFromServerName());
                result.addAttribute(event.getEffectiveDestination(),
                    event.EJB_TIMER_CALL_RESULT_ATTRNAME, new Integer(iCount)); 
            } else if(action==event.ACTION_LISTTIMERS) {
                String[] strs = listener.listTimers(event, event.getServerNames());
                result.addAttribute(event.getEffectiveDestination(),
                    event.EJB_TIMER_CALL_RESULT_ATTRNAME, strs); 
            }
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }
    //********************************************************
    /**
     * Handle AuthRealm event
     * @return number of listeners invoked
     */
    private int handleAuthRealmEvent(AdminEvent event) {
        AuthRealmEvent me = (AuthRealmEvent)event;
        List list = getListeners(AuthRealmEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                AuthRealmEventListener listener =
                        (AuthRealmEventListener)iter.next();
                invokeAuthRealmEventListener(listener, me);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    private void invokeAuthRealmEventListener(
            AuthRealmEventListener listener,
            AuthRealmEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            int action = event.getActionType();
            if(action==event.ACTION_CREATE){
                listener.authRealmCreated(event);
            } else if(action==event.ACTION_UPDATE) {
                listener.authRealmUpdated(event);
            } else if(action==event.ACTION_DELETE) {
                listener.authRealmDeleted(event);
            }
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }
    //********************************************************
    /**
     * Handle AuditModule event
     * @return number of listeners invoked
     */
    private int handleAuditModuleEvent(AdminEvent event) {
        AuditModuleEvent me = (AuditModuleEvent)event;
        List list = getListeners(AuditModuleEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                AuditModuleEventListener listener =
                        (AuditModuleEventListener)iter.next();
                invokeAuditModuleEventListener(listener, me);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    private void invokeAuditModuleEventListener(
            AuditModuleEventListener listener,
            AuditModuleEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            int action = event.getActionType();
            if(action==event.ACTION_CREATE){
                listener.auditModuleCreated(event);
            } else if(action==event.ACTION_UPDATE) {
                listener.auditModuleUpdated(event);
            } else if(action==event.ACTION_DELETE) {
                listener.auditModuleDeleted(event);
            }
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }
    //********************************************************
    /**
     * Handle SecurityService event
     * @return number of listeners invoked
     */
    private int handleSecurityServiceEvent(AdminEvent event) {
        SecurityServiceEvent me = (SecurityServiceEvent)event;
        List list = getListeners(SecurityServiceEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                SecurityServiceEventListener listener =
                        (SecurityServiceEventListener)iter.next();
                invokeSecurityServiceEventListener(listener, me);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    private void invokeSecurityServiceEventListener(
            SecurityServiceEventListener listener,
            SecurityServiceEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            int action = event.getActionType();
            if(action==event.ACTION_CREATE){
                listener.securityServiceCreated(event);
            } else if(action==event.ACTION_UPDATE) {
                listener.securityServiceUpdated(event);
            } else if(action==event.ACTION_DELETE) {
                listener.securityServiceDeleted(event);
            }
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }
    //********************************************************


    
    
    /**
     * Handle monitoring level change event
     * @return number of listeners invoked
     */
    private int handleLogLevelChangeEvent(AdminEvent event) {
        LogLevelChangeEvent llce = (LogLevelChangeEvent)event;
        List list = getListeners(LogLevelChangeEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                LogLevelChangeEventListener listener =
                        (LogLevelChangeEventListener)iter.next();
                invokeLogLevelChangeEventListener(listener, llce);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    private void invokeLogLevelChangeEventListener(
            LogLevelChangeEventListener listener,
            LogLevelChangeEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            listener.logLevelChanged(event);
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }

    /**
     * Handle monitoring level change event
     * @return number of listeners invoked
     */
    private int handleMonitoringLevelChangeEvent(AdminEvent event) {
        MonitoringLevelChangeEvent mlce = (MonitoringLevelChangeEvent)event;
        List list = getListeners(MonitoringLevelChangeEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                MonitoringLevelChangeEventListener listener =
                        (MonitoringLevelChangeEventListener)iter.next();
                invokeMonitoringLevelChangeEventListener(listener, mlce);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    private void invokeMonitoringLevelChangeEventListener(
            MonitoringLevelChangeEventListener listener,
            MonitoringLevelChangeEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            listener.monitoringLevelChanged(event);
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }

    private int handleShutdownEvent(AdminEvent event) {
	ShutdownEvent se = (ShutdownEvent)event;
        List list = getListeners(ShutdownEvent.eventType);
        if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                ShutdownEventListener listener =
                        (ShutdownEventListener)iter.next();
                invokeShutdownEventListener(listener, se);
            }
        }
        return ((list != null) ? list.size() : 0);
    }

    private void invokeShutdownEventListener(
	ShutdownEventListener listener,
        ShutdownEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        try {
            listener.startShutdown(event);          
        } catch (Throwable t) {
            handleListenerError(event,t, result);
        }
    }

    /**
     * Utility method to determine event type from an event listener.
     */
    private static String getEventTypeFromListener(AdminEventListener listener) {
        Class[] interfaces = listener.getClass().getInterfaces();
        String type = null;
        for (int i = 0; i < interfaces.length; i++) {
            String className = interfaces[i].getName();
            if (className.startsWith(ApplicationDeployEvent.eventType)) {
                type = ApplicationDeployEvent.eventType;
            } else if (className.startsWith(ModuleDeployEvent.eventType)) {
		//HERCULES: fix
                type = ModuleDeployEvent.eventType;
            } else if (className.startsWith(ResourceDeployEvent.eventType)) {
                type = ResourceDeployEvent.eventType;
            } else if (className.startsWith(BaseDeployEvent.eventType)) {
                type = BaseDeployEvent.eventType;
            } else if (className.startsWith(ConfigChangeEvent.eventType)) {
                type = ConfigChangeEvent.eventType;
            } else if (className.startsWith(MonitoringEvent.eventType)) {
                type = MonitoringEvent.eventType;
            } else if (className.startsWith(ShutdownEvent.eventType)) {
                type = ShutdownEvent.eventType;
            } else if (className.startsWith(AdminEvent.eventType)) {
                type = AdminEvent.eventType;
            }
            if (type != null) {
                break;
            }
        }
        return type;
    }

    /**
     * Refresh global config context. This is invoked after event processing is
     * successfully completed.
     */
    private void refreshConfigContext() {
        try {
            AdminService.getAdminService().getContext().getConfigContext().refresh(true);
        } catch (ConfigException ce) {
            warn("Unable to refresh ConfigContext upon receiving ConfigChangeEvent.");
            debug(ce);
        }
    }

    /**
     * Processing for the case when no listeners are available for a event.
     * When there are no listeners, assume that change can not be handled
     * without restart and set the status appropriately. This may not be
     * right thing to do because reconfig command executed directly from admin
     * server may have handled all the changes successfully even when there
     * are no listeners.
     */
    private void handleNoListeners(AdminEvent event) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        result.setResultCode(AdminEventResult.RESTART_NEEDED);
    }

    /**
     * Handle error in event listeners.
     * @param t throwable thrown by event listeners
     * @param result result for the event that was being processed
     */
    void handleListenerError(AdminEvent event, 
        Throwable t, AdminEventResult result) {
        t.printStackTrace();
        handleError(event, t, result, HANDLER_ERROR);
    }

    /**
     * Handle error by setting appropriate result code. 
     * @param t the exception (throwable) that was thrown. This should be
     *     an object of type <code>AdminEventListenerException</code> or a
     *     subclass of <code>java.lang.RuntimeException</code> or <code>
     *     java.lang.Error</code>.
     * @param result the result for the event
     * @param msg warning message prefix
     */
     private void handleError(AdminEvent event, Throwable t, 
     AdminEventResult result, String msg) {
        logger.log(Level.WARNING, msg, t.getMessage());
        String stackTrace = getStackTraceFromThrowable(t);
        debug(stackTrace);
        if (AdminEventResult.SUCCESS.equals(result.getResultCode())) {
            if (t instanceof AdminEventListenerException) {
                result.setResultCode(AdminEventResult.LISTENER_ERROR);
            } else if (t instanceof java.lang.RuntimeException) {
                result.setResultCode(AdminEventResult.RUNTIME_EXCEPTION);
            } else if (t instanceof java.lang.Error) {
                result.setResultCode(AdminEventResult.RUNTIME_ERROR);
            } else {
                // Treat this similar to RUNTIME_ERROR
                result.setResultCode(AdminEventResult.RUNTIME_ERROR);
            }
        }
        result.addException(event.getEffectiveDestination(),t);

    }

    private static String getStackTraceFromThrowable(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return (sw.toString());
    }

    static void warn(String s) {
        logger.warning(s);
    }

    static void debug(String s) {
        logger.fine(s);
    }

    static void debug(Throwable t) {
        logger.log(Level.FINE, t.getMessage(), t);
    }

    public void sendNotification(AdminEvent event) {

        AdminEventManager am = AdminEventManager.getAdminEventManager();
        MBeanLocator mbl = am.getMBeanLocator();
        Set objs =(Set) mbl.locate(event.getEventId());

        AdminEventResult  result = AdminEventResult.getAdminEventResult(event);

        if (objs == null || objs.size() == 0) {

            // FIXME: uncomment this when we migrate the 
            // legacy listener implementation
            //handleNoListeners(event);

            return;
        }

        Iterator it = objs.iterator();
        while ( it.hasNext() ) {
             try {
                ObjectInstance oi = (ObjectInstance) it.next();
                MBeanServerFactory.getMBeanServer().invoke(  oi.getObjectName(),
                   SEND_NOTIFICATION_METHOD,
                   new Object [] { event },
                   new String [] { SEND_NOTIFICATION_METHOD_SIG});
             } catch ( Throwable t) {
                 handleListenerError(event,t, result);
             }
         }
    }

    private boolean isAppRefConfigChangeForThisInst(ConfigChange change) {
        String instName = ApplicationServer.getServerContext().getInstanceName();
        Pattern p = Pattern.compile("/domain/servers/server\\[@name='(.*)'\\]/application\\-ref");
        Matcher m = p.matcher(change.getXPath());
        String cfgInst = null;
        if (m.lookingAt()) {
                cfgInst = m.group(1);
        }
        if (cfgInst == null) return true;
        return (instName.equals(cfgInst));
    }

    // ---- INSTANCE VARIABLES - PRIVATE -------------------------------------
    static final String MULTICAST_NOT_SUPPORTED = 
                            "event.multicast_not_supported";
    static final String HANDLER_INIT_ERROR = "event.handler_init_error";
    static final String HANDLER_ERROR = "event.handler_error";
    static final String SEND_NOTIFICATION_METHOD = "sendNotification";
    static final String SEND_NOTIFICATION_METHOD_SIG = 
                            "javax.management.Notification";

}

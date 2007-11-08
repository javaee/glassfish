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

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;

/**
 * Factory class for dynamic reconfig events. 
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class EventBuilder {
    
    /**
     * Returns a resource deploy event. This assumes that the event 
     * stack is populated before this is called.
     *
     * @param  action  event action code
     * @param  resName  name of the resource
     *
     * @throws  ConfigException  if an error while getting resource type
     */
    public ResourceDeployEvent createResourceDeployEvent(String action,
            String resName) throws ConfigException {

        EventStack stack = EventContext.getEventStackFromThreadLocal();

        return createResourceDeployEvent(action, resName, 
                    stack.getConfigContext(), 
                    (ArrayList)stack.getConfigChangeList(),
                    stack.getTarget()); 
    }

    /**
     * Returns a resource deploy event. 
     *
     * @param  action  event action code
     * @param  resName  name of the resource
     * @param  ctx      config context
     * @param  configChanges  list of config changes
     * @param  target   target name
     *
     * @throws  ConfigException  if an error while getting resource type
     */
    public ResourceDeployEvent createResourceDeployEvent(String action,
            String resName, ConfigContext ctx,  ArrayList configChanges, 
            String target) throws ConfigException {

        // name of the current server instance runtime
        String instName = LookupManager.getInstance().getLookup().lookup(ServerContext.class).getInstanceName();
        
        // resource type from target string
        String resType = getResourceTypeByName(ctx, resName, target);

        // resource deploy event
        ResourceDeployEvent rde = new ResourceDeployEvent(instName, 
                                            resName, resType, action);
        rde.setTargetDestination(target);

        // adds all the config changes
        DependencyResolver dr = new DependencyResolver(ctx, target);
        rde.addDependentConfigChange( 
            dr.resolveResources(resName, action, resType) );

        rde.addConfigChange( configChanges );

        // setEventKey

        return rde;
    }

    /** 
     * Helper method to obtain ResourceDeployEvent's resource type 
     * correspondent to its name
     *
     * @returns ResourceDeployEvent's resource type 
     *
     * @throws  ConfigException  if a config parsing exception
     */
    protected static String getResourceTypeByName(ConfigContext ctx, 
            String resName, String target) throws ConfigException {

        String type = getTypeFromTarget(target);
        if (type == null) {
            type = getResourceType(ctx, resName, true);
        }

        return convertResourceType(type);
    }

    /**
     * Returns the resource type of a given resource.
     *
     * @param  ctx  config context
     * @param  id  resource name
     * @param  includePool  if true, includes pool in the search
     *
     * @throws  ConfigException  if a config parsing exception
     */
    static String getResourceType(ConfigContext ctx, String id, 
            boolean includePool) throws ConfigException {

        Resources root = ((Domain)ctx.getRootConfigBean()).getResources();

        ConfigBean res = root.getJdbcResourceByJndiName(id);
        if ( res != null ) {
            return Resources.JDBC_RESOURCE;
        }

        res = root.getMailResourceByJndiName(id);
        if ( res != null ) {
            return Resources.MAIL_RESOURCE;
        }

        res = root.getCustomResourceByJndiName(id);
        if ( res != null ) {
            return Resources.CUSTOM_RESOURCE;
        }

        res = root.getExternalJndiResourceByJndiName(id);
        if ( res != null ) {
            return Resources.EXTERNAL_JNDI_RESOURCE;
        }

        res = root.getPersistenceManagerFactoryResourceByJndiName(id);
        if ( res != null) {
            return Resources.PERSISTENCE_MANAGER_FACTORY_RESOURCE;
        }

        res = root.getAdminObjectResourceByJndiName(id);
        if ( res != null ) {
            return Resources.ADMIN_OBJECT_RESOURCE;
        }

        res = root.getConnectorResourceByJndiName(id);
        if ( res != null ) {
            return Resources.CONNECTOR_RESOURCE;
        }

        res = root.getResourceAdapterConfigByResourceAdapterName(id);
        if ( res != null ) {
            return Resources.RESOURCE_ADAPTER_CONFIG;
        }

        if (includePool) {
            res = root.getJdbcConnectionPoolByName(id);
            if ( res != null ) {
                return Resources.JDBC_CONNECTION_POOL;
            }

            res = root.getConnectorConnectionPoolByName(id);
            if ( res != null ) {
                return Resources.CONNECTOR_CONNECTION_POOL;
            }
        }

        return null;
    }

    /**
     * Adds a resource deploy event to the event stack.
     * 
     * @param  action   event action code
     * @param  target   event target 
     * @param  resName  name of the resource
     * @throws ConfigException  if an error while parsing configuration
     */
    public void addResourceDeployEvent(String action, String target, 
            String resName) throws ConfigException {

        EventStack stack = EventContext.getEventStackFromThreadLocal();
        ConfigContext ctx = stack.getConfigContext();
        stack.setTarget(target);
        stack.setConfigChangeList( ctx.getConfigChangeList());

        ResourceDeployEvent event = createResourceDeployEvent(action, resName); 
        EventContext.addEvent(event);
    }

    /**
     * Creates log level change event.
     *
     * @param  ctx  config context
     * @param  target  event target
     * @param  newLogLevel  new log level
     * @param  moduleName  module name where log level was changed
     *
     * @throws  ConfigException  if a configuration parsing error
     */
    public LogLevelChangeEvent createLogLevelChangeEvent(ConfigContext ctx, 
		 String target, String newLogLevel, String moduleName) 
         throws ConfigException {

        // name of the current server instance runtime
        String instName = LookupManager.getInstance().getLookup(ServerContext.class).getInstanceName();
         
        // log level change event
        LogLevelChangeEvent lde = new LogLevelChangeEvent(instName);
        lde.setTargetDestination(target);

        // FIXME: get the change list from each thread local
        //rde.addConfigChange( ctx.getConfigChangeList() );

        // setEventKey

        // do i need to set old log level ???? 
        lde.setModuleName(moduleName);
        lde.setNewLogLevel(newLogLevel);
        return lde;
    }

    /**
     * Returns an application deploy event. This assumes that the event stack 
     * is populated before this method is called.
     *
     * <p> Example Usage:
     * <xmp>
     *   ConfigContext ctx = stack.getConfigContext();
     *   EventStack stack = EventContext.getEventStackFromThreadLocal();
     *   stack.setTarget(targetName);
     *   stack.setConfigChangeList(ctx.getConfigChangeList());
     *
     *   EventBuilder builder = new EventBuilder();
     *   ApplicationDeployEvent event = builder.createApplicationDeployEvent(
     *                  BaseDeployEvent.REMOVE_REFERENCE, referenceName);
     *
     *   AdminEventResult result = AdminEventMulticaster.multicast(event);
     *
     *   Instead of using multicaster, you may also schedule the event
     *   to be sent out by the interceptor. This will release the lock
     *   for concurrent config users. Call the following API if you want 
     *   to schedule the event:
     *
     *   EventContext.addEvent(event);
     * </xmp>
     *
     * @param  action   event action code
     * @param  appName  name of the application
     *
     * @return  application deploy event
     *
     * @throws  ConfigException  if an error while getting dependent
     *                           config elements
     */
    public ApplicationDeployEvent createApplicationDeployEvent(String action,
            String appName) throws ConfigException {

        return createApplicationDeployEvent(action, appName, false);
    }

    /**
     * Returns an application deploy event. This assumes that the event stack 
     * is populated before this method is called.
     *
     * @param  action   event action code
     * @param  appName  name of the application
     * @param  cascade  used by connector implementation to decide whether to 
     *                  remove resource adapter related resources
     *
     * @return  application deploy event
     *
     * @throws  ConfigException  if an error while getting dependent
     *                           config elements
     */
    public ApplicationDeployEvent createApplicationDeployEvent(String action,
            String appName, boolean cascade) throws ConfigException {

        return createApplicationDeployEvent(action, appName, cascade, false);
    }

    /**
     * Returns an application deploy event. This assumes that the event stack 
     * is populated before this method is called.
     *
     * @param  action   event action code
     * @param  appName  name of the application
     * @param  cascade  used by connector implementation to decide whether to 
     *                  remove resource adapter related resources
     * @param  forceDeploy whether a deployment is enforced
     *
     * @return  application deploy event
     *
     * @throws  ConfigException  if an error while getting dependent
     *                           config elements
     */
    public ApplicationDeployEvent createApplicationDeployEvent(String action,
            String appName, boolean cascade, boolean forceDeploy) 
                throws ConfigException {
        return createApplicationDeployEvent(action, appName, cascade, 
            forceDeploy, Constants.LOAD_UNSET);
    }

    /**
     * Returns an application deploy event. This assumes that the event stack
     * is populated before this method is called.
     *
     * @param  action   event action code
     * @param  appName  name of the application
     * @param  cascade  used by connector implementation to decide whether to
     *                  remove resource adapter related resources
     * @param  forceDeploy whether a deployment is enforced
     * @param  loadUnloadAction what the load/unload action is
     *
     * @return  application deploy event
     *
     * @throws  ConfigException  if an error while getting dependent
     *                           config elements
     */
    public ApplicationDeployEvent createApplicationDeployEvent(String action,
            String appName, boolean cascade, boolean forceDeploy, 
            int loadUnloadAction) throws ConfigException {
        EventStack stack = EventContext.getEventStackFromThreadLocal();
        String target = stack.getTarget();
        assert(target != null);

        // name of the current server instance
        String instName = LookupManager.getInstance().getLookup(ServerContext.class).getInstanceName();

        ApplicationDeployEvent ade = new ApplicationDeployEvent(instName, 
            appName, action, cascade, forceDeploy, loadUnloadAction);

        ade.setTargetDestination(target);

        // adds all the config changes
        DependencyResolver dr = 
            new DependencyResolver(stack.getConfigContext(), target);
        ade.addDependentConfigChange(dr.resolveApplications(appName, action));

        ade.addConfigChange( (ArrayList) stack.getConfigChangeList() );

        return ade;
    }

    /**
     * Returns a module deploy event. 
     *
     * @param  action      event action code
     * @param  moduleName  name of the module
     * @param  moduleType  type of the module 
     *                     (ex. ModuleDeployEvent.TYPE_WEBMODULE)
     *
     * @return  module deploy event
     *
     * @throws  ConfigException  if an error while getting dependent
     *                           config elements
     */
    public ModuleDeployEvent createModuleDeployEvent(String action, 
            String moduleName, String moduleType) 
            throws ConfigException {

        return createModuleDeployEvent(action, moduleName, moduleType, false);
    }

    /**
     * Returns a module deploy event. 
     *
     * @param  action      event action code
     * @param  moduleName  name of the module
     * @param  moduleType  type of the module 
     *                     (ex. ModuleDeployEvent.TYPE_WEBMODULE)
     * @param  cascade  used by connector implementation to decide whether to 
     *                  remove resource adapter related resources
     *
     * @return  module deploy event
     *
     * @throws  ConfigException  if an error while getting dependent
     *                           config elements
     */
    public ModuleDeployEvent createModuleDeployEvent(String action, 
            String moduleName, String moduleType, boolean cascade) 
            throws ConfigException {

        return createModuleDeployEvent(action, moduleName, moduleType, 
                    cascade, false);
    }

    /**
     * Returns a module deploy event. 
     *
     * @param  action      event action code
     * @param  moduleName  name of the module
     * @param  moduleType  type of the module 
     *                     (ex. ModuleDeployEvent.TYPE_WEBMODULE)
     * @param  cascade  used by connector implementation to decide whether to 
     *                  remove resource adapter related resources
     * @param  forceDeploy whether a deployment is enforced
     *
     * @return  module deploy event
     *
     * @throws  ConfigException  if an error while getting dependent
     *                           config elements
     */
    public ModuleDeployEvent createModuleDeployEvent(String action, 
            String moduleName, String moduleType, boolean cascade, 
            boolean forceDeploy) throws ConfigException {
        EventStack stack = EventContext.getEventStackFromThreadLocal();
        String target = stack.getTarget();
        assert(target != null);

        String instName = LookupManager.getInstance().getLookup(ServerContext.class).getInstanceName();

        ModuleDeployEvent mde = 
            new ModuleDeployEvent(instName, moduleName, moduleType, 
                                    action, cascade);
        mde.setTargetDestination(target);


        // sets the force deploy flag in the event
        mde.setForceDeploy(forceDeploy);

        // adds all the config changes
        DependencyResolver dr = 
            new DependencyResolver(stack.getConfigContext(), target);
        mde.addDependentConfigChange(dr.resolveApplications(moduleName,action));
        mde.addConfigChange( (ArrayList) stack.getConfigChangeList() );

        return mde;
    }

    /**
     * Creates config change event.
     *
     * @param  target  target for the event
     * @param  configChangeList  config change list
     *
     * @throws  ConfigException  if a configuration parsing error
     */
    public ConfigChangeEvent createConfigChangeEvent(String target,
            ArrayList configChangeList) throws ConfigException {

        String instName = LookupManager.getInstance().getLookup(ServerContext.class).getInstanceName();
        ConfigChangeEvent cce = 
            new ConfigChangeEvent(instName, configChangeList);
        cce.setTargetDestination(target);
        return cce;
    }

    /**
     * Creates a monitoring event.
     *
     * @param  ctx  config context
     * @param  target  target for the event
     * @param  component  name of monitoring module
     * @param  action  new action 
     * @param  command  jmx command
     *
     * @throws  ConfigException  if a configuration parsing error
     */
    public MonitoringEvent createMonitoringEvent(ConfigContext ctx, 
            String target, String component, String action, Object command) 
            throws ConfigException {
        
        String instName = LookupManager.getInstance().getLookup(ServerContext.class).getInstanceName();
        MonitoringEvent me = 
            new MonitoringEvent(instName, component, action, command);
        me.setTargetDestination(target);

        return me; 
    }
            
    /**
     * Creates monitoring level change event. 
     *
     * @param  ctx  config context
     * @param  target  target for the event
     * @param  component  name of monitoring module
     * @param  monitoringLevel  new monitoring level
     *
     * @throws  ConfigException  if a configuration parsing error
     */
    public MonitoringLevelChangeEvent createMonitoringLevelChangeEvent(
            ConfigContext ctx, String target, String component, 
            String monitoringLevel) throws ConfigException {

        String instName = LookupManager.getInstance().getLookup(ServerContext.class).getInstanceName();

        MonitoringLevelChangeEvent mle = 
            new MonitoringLevelChangeEvent(instName);
        mle.setTargetDestination(target);

        return mle;
    }

    /**
     * Converts config bean type to resource deploy event type.
     * 
     * @param  type  config bean type
     * @return resource deploy event type
     */
    protected static String convertResourceType(String type) {
        // XXX see if we can avoid this type conversion
        // may be do hash instead of string comparision

        if (type==null) {
            return null;
        } else {
            type = type.trim();
        }

        if ( type.equals(ServerTags.CUSTOM_RESOURCE)
                || type.equals(Resources.CUSTOM_RESOURCE) ) {
            return ResourceDeployEvent.RES_TYPE_CUSTOM;
        }
        if ( type.equals(ServerTags.EXTERNAL_JNDI_RESOURCE)
                || type.equals(Resources.EXTERNAL_JNDI_RESOURCE) ) {
            return ResourceDeployEvent.RES_TYPE_EXTERNAL_JNDI;
        }
        if ( type.equals(ServerTags.JDBC_RESOURCE)
                || type.equals(Resources.JDBC_RESOURCE) ) {
            return ResourceDeployEvent.RES_TYPE_JDBC;
        }
        if ( type.equals(ServerTags.MAIL_RESOURCE)
                || type.equals(Resources.MAIL_RESOURCE) ) {
            return ResourceDeployEvent.RES_TYPE_MAIL;
        }
        if ( type.equals(ServerTags.PERSISTENCE_MANAGER_FACTORY_RESOURCE)
            || type.equals(Resources.PERSISTENCE_MANAGER_FACTORY_RESOURCE)) {

            return ResourceDeployEvent.RES_TYPE_PMF;
        }
        if ( type.equals(ServerTags.ADMIN_OBJECT_RESOURCE)
                || type.equals(Resources.ADMIN_OBJECT_RESOURCE) ) {
            return ResourceDeployEvent.RES_TYPE_AOR;
        }
        if ( type.equals(ServerTags.CONNECTOR_RESOURCE)
                || type.equals(Resources.CONNECTOR_RESOURCE) ) {
            return ResourceDeployEvent.RES_TYPE_CR;
        }
        if ( type.equals(ServerTags.RESOURCE_ADAPTER_CONFIG)
                || type.equals(Resources.RESOURCE_ADAPTER_CONFIG) ) {
            return ResourceDeployEvent.RES_TYPE_RAC;
        }
        if ( type.equals(ServerTags.JDBC_CONNECTION_POOL)
                || type.equals(Resources.JDBC_CONNECTION_POOL) ) {
            return ResourceDeployEvent.RES_TYPE_JCP;
        }
        if ( type.equals(ServerTags.CONNECTOR_CONNECTION_POOL)
                || type.equals(Resources.CONNECTOR_CONNECTION_POOL) ) {
            return ResourceDeployEvent.RES_TYPE_CCP;
        }

        return type; // unsupported type
    }

    /**
     * Returns a module aor app deploy event. 
     *
     * @param  action  event action code
     * @param  name  name of the module or application
     * @param  ctx      config context
     * @param  configChanges  list of config changes
     * @param  target   target name
     *
     * @throws  ConfigException  if an error while getting resource type
     */
    public BaseDeployEvent createModAppDeployEvent(String action,
            String name, ConfigContext ctx,  ArrayList configChanges, 
            String target) throws ConfigException {

        // name of the current server instance runtime
        String instName = LookupManager.getInstance().getLookup(ServerContext.class).getInstanceName();
         
        BaseDeployEvent event=getAppOrModuleEvent(instName, ctx, name, action);
        event.setTargetDestination(target);
        
        // adds all the config changes
        DependencyResolver dr = new DependencyResolver(ctx, target);
        event.addDependentConfigChange(dr.resolveApplications(name,action));
        event.addConfigChange( configChanges );

        return event;
    }

    /**
     * Provides deferred type resolution (usually on remote instance side). 
     *
     * @param  eventToResolve  original event without resolved module type in it
     * @param  ctx  effective config context used for type resolution
     *
     * @returns DeployEvent of the proper type  
     *
     * @throws  ConfigException  if an error while getting resource type
     */
    public static BaseDeployEvent resolveModAppDeployEventType(
            BaseDeployEvent eventToResolve, ConfigContext ctx) 
            throws ConfigException {

        // name of the current server instance runtime
        String instName = eventToResolve.getInstanceName();
        String name = eventToResolve.getJ2EEComponentName();
        String action = eventToResolve.getAction();

        BaseDeployEvent event=getAppOrModuleEvent(instName, ctx, name, action);
        event.setCascade(eventToResolve.getCascade());
        event.setEventId(eventToResolve.getEventId());

        if (eventToResolve.getDependentChangeList()!=null) {
            event.addDependentConfigChange(
                eventToResolve.getDependentChangeList());
        }

        if (eventToResolve.getConfigChangeList()!=null) {
            event.addConfigChange(eventToResolve.getConfigChangeList());
        }

        if (eventToResolve.getTargetDestination()!=null) {
            event.setTargetDestination(eventToResolve.getTargetDestination());
        }

        return event;
    }

    /**
     * Creates application deploy event. 
     * 
     * @param  instName  name of server instance where the event is created
     * @param  ctx  config context
     * @param  name  name of application 
     * @param  action  event action - deploy/undeploy/redeploy
     *
     * @return application deploy event
     *
     * @throws ConfigException  if a configuration parsing error
     */
    private static BaseDeployEvent getAppOrModuleEvent(String instName,
            ConfigContext ctx, String name, String action) 
            throws ConfigException {

        String type = getAppOrModuleType(ctx, name);

        if (type!=null) {

            if (type.equals(BaseDeployEvent.APPLICATION)) {
                // application deploy event
                return (BaseDeployEvent)new ApplicationDeployEvent(instName, 
                                                            name, action);
            } else {
                // module deploy event
                return (BaseDeployEvent)new ModuleDeployEvent(instName, name,
                                                            type, action);
            }
        } else { //for deffered type set
            return new BaseDeployEvent(instName, null, name, action);
        }
    }

    /**
     * Returns application type. 
     * 
     * @param  ctx  config context
     * @param  name  name of application 
     *
     * @return application type
     * @throws ConfigException  if a configuration parsing error
     */
    private static String getAppOrModuleType(ConfigContext ctx, String name) 
                throws ConfigException {

        //first try to find 
        
        ConfigBean[] beans = ((Domain)ctx.getRootConfigBean()).
                                    getApplications().getAllChildBeans();
        ConfigBean bean = null;
        if (beans!=null) {
            for(int i=0; i<beans.length; i++) {
                if(name.equals(beans[i].getAttributeValue("name"))) {
                   bean = beans[i];
                   break;
                }
            }
        }

        if (bean==null) {
            return null;
        }

        String type = null;

        if (bean instanceof 
                com.sun.enterprise.config.serverbeans.J2eeApplication) {
            type = BaseDeployEvent.APPLICATION;
        }

        if (bean instanceof 
                com.sun.enterprise.config.serverbeans.ConnectorModule) {
            type = ModuleDeployEvent.TYPE_CONNECTOR;
        } else if (bean instanceof 
                com.sun.enterprise.config.serverbeans.EjbModule) {
            type = ModuleDeployEvent.TYPE_EJBMODULE;
        } else if (bean instanceof 
                com.sun.enterprise.config.serverbeans.WebModule) {
            type = ModuleDeployEvent.TYPE_WEBMODULE;
        } else if (bean instanceof 
                com.sun.enterprise.config.serverbeans.AppclientModule) {
            type = ModuleDeployEvent.TYPE_APPCLIENT;
        }

        return type;
    }

    /**
     * Returns the type (as defined in domain.xml) from a target. 
     * Target has the following three parts:
     *   <type>|<name-of-resORapp>|<type-of-resORapp>
     *
     * @param  target   target string
     */
    static String getTypeFromTarget(String target) {

        String type = null;
        String msg = "\n NAZRUL Could not determine resource type for target " 
        + target;

        try { 
            if (target != null) {
                StringTokenizer st = new StringTokenizer(target, "|");
                int tokens = st.countTokens();
                if (tokens == 3) {
                    String prefix = st.nextToken();
                    String name = st.nextToken();
                    type = st.nextToken();
                }
            }
        } catch (Exception e) { }

        return type;
    }
}

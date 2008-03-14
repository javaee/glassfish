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

/*
 * AMXUtil.java
 *
 * Created on July 18, 2006, 2:43 PM
 */

package com.sun.enterprise.tools.admingui.util;

import java.util.*;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.SystemInfo;
import com.sun.appserv.management.base.UploadDownloadMgr;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.DeployedItemRefConfigCR;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.HTTPListenerConfig;
import com.sun.appserv.management.config.IIOPServiceConfig;
import com.sun.appserv.management.config.IIOPListenerConfig;
import com.sun.appserv.management.config.J2EEApplicationConfig;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.config.ResourceRefConfigCR;
import com.sun.appserv.management.config.Enabled;
import com.sun.appserv.management.config.ServerConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo;
import com.sun.appserv.management.ext.wsmgmt.WebServiceMgr;
import com.sun.appserv.management.j2ee.J2EEDomain;
import com.sun.appserv.management.j2ee.J2EEServer;
import com.sun.appserv.management.j2ee.StateManageable;
import com.sun.appserv.management.monitor.MonitoringRoot;
import com.sun.appserv.management.monitor.ServerRootMonitor;
import com.sun.appserv.management.base.SystemInfo;
import com.sun.appserv.management.config.EJBModuleConfig;
import com.sun.appserv.management.config.WebModuleConfig;
import com.sun.appserv.management.config.RARModuleConfig;
import com.sun.appserv.management.config.AppClientModuleConfig;
import com.sun.appserv.management.config.LifecycleModuleConfig;
import com.sun.appserv.management.config.CustomMBeanConfig;

import com.sun.appserv.management.helper.LBConfigHelper;
import com.sun.appserv.management.util.misc.GSetUtil; 

import com.sun.enterprise.admin.common.MBeanServerFactory;

import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  

public class AMXUtil {

    private static ProxyFactory amxProxyFactory = null;
    private static DomainRoot domainRoot = null;
    private static DomainConfig domainConfig = null;
    private static J2EEDomain j2eeDomain = null;
    private static MonitoringRoot monitoringRoot = null;
    private static QueryMgr queryMgr = null;
    private static UploadDownloadMgr uploadDownloadMgr = null;
    private static WebServiceMgr webServiceMgr = null;
    private static LBConfigHelper lbConfigHelper = null;

    private AMXUtil() {} //dummy constructor, all static methods.
    
    public static DomainConfig getDomainConfig() {
        return domainConfig == null ? getDomainRoot().getDomainConfig() : domainConfig;
    }
    
    public static J2EEDomain getJ2EEDomain() {
        return j2eeDomain == null ? getDomainRoot().getJ2EEDomain() : j2eeDomain;
    }
    
    public static MonitoringRoot getMonitoringRoot() {
        return monitoringRoot ==  null ? getDomainRoot().getMonitoringRoot() : monitoringRoot;
    }
   
    public static WebServiceMgr getWebServiceMgr() {
        return webServiceMgr == null ? getDomainRoot().getWebServiceMgr() : webServiceMgr;
    }
    
    public static DomainRoot getDomainRoot() {
        return domainRoot == null ? getAMXProxyFactory().getDomainRoot() : domainRoot;
    }
    
    public static QueryMgr getQueryMgr() {
        return queryMgr == null ? getDomainRoot().getQueryMgr() : queryMgr;
    }
    
    public static UploadDownloadMgr getUploadDownloadMgr() {
        return uploadDownloadMgr == null ? getDomainRoot().getUploadDownloadMgr() : uploadDownloadMgr;
    }
   
    public static ProxyFactory getAMXProxyFactory() {
        return amxProxyFactory == null ? 
        ProxyFactory.getInstance( MBeanServerFactory.getMBeanServer()) : amxProxyFactory;
    }
    
    //IF the instance is not running or when no monitoring is on, null will be returned;
    public static ServerRootMonitor getServerRootMonitor(String instanceName){
        if (GuiUtil.isEmpty(instanceName))
            return null;
        return AMXUtil.getDomainRoot().getMonitoringRoot().getServerRootMonitorMap().get(instanceName);
    }
    
    public static LBConfigHelper getLBConfigHelper(){
        return ( lbConfigHelper == null)?  new LBConfigHelper(getDomainRoot()) : lbConfigHelper;
    }
        
    /**
     *	<p> Get the root of the Config mbean
     *
     *	@param	configName  The name of the config.	
     *	
     *	@return	config with the specified name,  or NULL if no such config exists.
     */
    public static ConfigConfig getConfig(String configName){
        if (GuiUtil.isEmpty(configName)) return null;
        Map<String,ConfigConfig> cmap = (Map) getDomainConfig().getConfigConfigMap();
        ConfigConfig config = cmap.get(configName);
        return config;
    }
    
    /**
     * <p> Get the name of the config based on instance or cluster name
     *
     * @param	name  The name of the server instance or cluster.
     *
     * @return String  null if the instance or cluster doesn't exist.
     */
    public static String getConfigName(String name){
        
        //try to see if its a server, if not, try cluster.
        ServerConfig serverConfig = getDomainConfig().getServerConfigMap().get(name);
        if (serverConfig != null){
            return serverConfig.getReferencedConfigName();
        }
        ClusterConfig clusterConfig = getDomainConfig().getClusterConfigMap().get(name);
        if (clusterConfig != null)
            return clusterConfig.getReferencedConfigName();
        return null;
    }
    
    /**
     * <p> Get the ConfigConfig mbean based on server instance name or Cluster name
     *
     * @param	instanceName  Server instance name or Cluster name
     *
     * @return null if the instance or cluster doesn't exist.
     */
    public static ConfigConfig getConfigByInstanceOrClusterName(String instanceName){
        boolean isEERunning = isEE();
        return getConfig( getConfigName(instanceName));
    }
    
    /**
     *	<p> Get the <http-listener> Config mbean given the config name and http-listener's name.
     *
     *	@param	configName  The name of the config.
     *  @param	listenerName  The name of the http-listener.	
     *	
     *	@return	http-listener mbean of the specified config name and listener name, or NULL if no such config exists.
     */
    public static HTTPListenerConfig getHTTPListenerConfig(String configName, String listenerName){
        if (GuiUtil.isEmpty(configName) || GuiUtil.isEmpty(listenerName))
            return null;
        ConfigConfig config = getConfig(configName);
        HTTPServiceConfig service = (config == null) ? null : config.getHTTPServiceConfig();
        if (service != null){
            Map<String,HTTPListenerConfig> listeners = service.getHTTPListenerConfigMap();
            HTTPListenerConfig listener =  listeners.get(listenerName);
            return listener;
        }
        return null;
    }
    
    /**
     *	<p> Get the <iiop-listener> Config mbean given the config name and iiop-listener's name.
     *
     *	@param	configName  The name of the config.
     *  @param	listenerName  The name of the http-listener.	
     *	
     *	@return	iiop-listener mbean of the specified config name and listener name, or NULL if no such config exists.
     */
    public static IIOPListenerConfig getIIOPListenerConfig(String configName, String listenerName){
        if (GuiUtil.isEmpty(configName) || GuiUtil.isEmpty(listenerName))
            return null;
        ConfigConfig config = getConfig(configName);
        IIOPServiceConfig service = (config == null) ? null : config.getIIOPServiceConfig();
        if (service != null){
            Map<String,IIOPListenerConfig> listeners = service.getIIOPListenerConfigMap();
            IIOPListenerConfig listener =  listeners.get(listenerName);
            return listener;
        }
        return null;
    }
    
    public static boolean isEE(){
        SystemInfo systemInfo = getDomainRoot().getSystemInfo();
        return systemInfo.supportsFeature(SystemInfo.CLUSTERS_FEATURE);
    }
    
    
    public static boolean supportCluster(){
        SystemInfo systemInfo = getDomainRoot().getSystemInfo();
        return systemInfo.supportsFeature(SystemInfo.CLUSTERS_FEATURE);
    }
    
    /**
     *	<p> Returns the value of the given property and MBean
     *
     *	@param	mbean MBean with properties 
     *   (extends <code>com.sun.appserv.management.config.PropertiesAccess</code>).
     *  @param	propName property name.	
     *	
     *	@return	String property value.
     */
    public static String getPropertyValue(PropertiesAccess mbean, String propName) {
        return mbean.getPropertyValue(propName);
    }   
     public static WebServiceEndpointInfo getWebServiceEndpointInfo(Object webServiceKey) {
        return AMXUtil.getWebServiceMgr().getWebServiceEndpointInfo(webServiceKey);
     }

     /**
     * Function to edit the properties on a server.
     * All MBeans that have Properties extend PropertiesAccess interface
     * @param handlerCtx Handler Context
     * @param config Config object whose properties have been modified 
     * eg.EJBContainerConfig
     */
     public static void editProperties(HandlerContext handlerCtx, PropertiesAccess config){
         Map<String,String> addProps = (Map)handlerCtx.getInputValue("AddProps");
         ArrayList removeProps = (ArrayList)handlerCtx.getInputValue("RemoveProps");
         if(removeProps != null){
             String[] remove = (String[])removeProps.toArray(new String[ removeProps.size()]);
             for(int i=0; i<remove.length; i++){
                 config.removeProperty(remove[i]);
             }
         }
         if(addProps != null ){
             for(String key: addProps.keySet()){
                 String value = addProps.get(key);
                 if (config.existsProperty(key))
                    config.setPropertyValue(key, value);
                 else
                     config.createProperty(key,value);
             }
         }
     }
     
     /*
      * update the properties of a config.
      */
     public static void updateProperties(PropertiesAccess config, Map<String,String>newProps, List ignore){
        java.util.Map<java.lang.String,java.lang.String>oldProps = config.getProperties();

        if (ignore == null)
            ignore = new ArrayList();
        //Remove any property that is no longer in the new list
        Iterator iter = oldProps.keySet().iterator();
        while(iter.hasNext()){
            Object key = iter.next();
            if (ignore.contains(key))
                continue;
            if (! newProps.containsKey(key)) {
                config.removeProperty( (String) key);
            }
        }
        
         //update the value if the value is different or create a new property if it doesn't exist before
        for(String propName : newProps.keySet()){
            String val = newProps.get(propName);
            if (config.existsProperty(propName)){
                String oldValue = config.getPropertyValue(propName);
                if ( ! oldValue.equals(val))
                    config.setPropertyValue(propName, val);
            }else
                config.createProperty(propName, val);
        }
     }
     
     /*  converts a Property Map to a Map where the name is preceded by PropertiesAccess.PROPERTY_PREFIX.
      *  This conversion is required when this Map is used as the optional parameter when creating a config.
      *  refer to the java doc of PropertiesAccess in AMX javadoc
      */
     public static Map convertToPropertiesOptionMap(Map<String,String> props, Map<String,String> convertedMap){
         if (convertedMap == null )
            convertedMap = new HashMap();
         if (props == null)
             return convertedMap;
         Set<String> keySet = props.keySet();
         for(String key : keySet){
             if (! GuiUtil.isEmpty((String)props.get(key)))
                convertedMap.put(PropertiesAccess.PROPERTY_PREFIX + key, (String)props.get(key));
         }
         return convertedMap;
     }
       
     
     /**
      * returns the Properties of a config, skipping those specified in the list thats passed in.
      * This is mostly for edit where we want to treat particular properties differently, and don't 
      * show that in the Properties table.
      * Normally, this is followed by updateProperites() with the ignore list the same as the skipList
      * specified here when user does a Save.
      */
     public static Map getNonSkipPropertiesMap(PropertiesAccess config, List skipList) {
        Map<String, String> props = config.getProperties();
        Map newMap = new HashMap<String, String>();
        
        for(String propsName : props.keySet()){
            if (skipList.contains(propsName))
                continue;
            newMap.put(propsName, props.get(propsName));
        }
        return newMap;
     }
        
     
     //Chagen the Property Value of a config
      public static void changeProperty(PropertiesAccess config, String propName, String propValue){
          if (config.existsProperty(propName)){
              if (GuiUtil.isEmpty(propValue))
                  config.removeProperty(propName);
              else
              if (! propValue.equals(config.getPropertyValue(propName)))
                  config.setPropertyValue(propName, propValue);
              //don't change the value if it is equal.
           }else{
              if (!GuiUtil.isEmpty(propValue))
                    config.createProperty(propName, propValue);
          }
      }
     
     
     public static String getAppType(String name){
        Set<AMXConfig> applications = AMXUtil.getQueryMgr().queryJ2EETypesSet(APP_TYPES);
        for(AMXConfig app : applications){
            if (app.getName().equals(name)){
                return app.getJ2EEType();
            }
        }
        return "";
    }
    
     static final private Set<String> APP_TYPES = GSetUtil.newUnmodifiableStringSet(
        J2EEApplicationConfig.J2EE_TYPE,
        WebModuleConfig.J2EE_TYPE,
        EJBModuleConfig.J2EE_TYPE,
        LifecycleModuleConfig.J2EE_TYPE,
        RARModuleConfig.J2EE_TYPE,
        AppClientModuleConfig.J2EE_TYPE,
        CustomMBeanConfig.J2EE_TYPE
     );
     
     
     /* 
      * Utility method to return the image and the state string for display.
      * This object must implmenent StateManageable. 
      * If this is a J2EEServer, then it will also looks at the restart required flag
      * The String returned will be the <img .. > + state 
      */
     public static String getStatusForDisplay(AMX obj, boolean includeString){
         if (! (obj instanceof StateManageable) )
            return "";
         int state = ((StateManageable) obj ).getstate();
         if ( (obj instanceof J2EEServer) && (state == StateManageable.STATE_RUNNING)){
             boolean restart = ( (J2EEServer) obj).getRestartRequired();
             if (restart){
                 String image = GuiUtil.getMessage("common.restartRequiredImage");
                 return (includeString) ? image + "&nbsp;" + GuiUtil.getMessage("common.restartRequired") : image;
             }
         }
         if (includeString)
            return getStatusImage(state) + "&nbsp;" + getStatusString(state);
         else 
             return getStatusImage(state);
     }
     
     
     public static String getStatusImage(int state) {
        return stateImageMap.get(state);
     }    
     
     public static String getStatusString(int state) {
        return stateMap.get(state);
     }  
     
    static private final Map<Integer, String> stateImageMap = new HashMap();
    static{
          stateImageMap.put(StateManageable.STATE_FAILED, GuiUtil.getMessage("common.failedImage"));
          stateImageMap.put(StateManageable.STATE_RUNNING, GuiUtil.getMessage("common.runningImage"));
          stateImageMap.put(StateManageable.STATE_STARTING, GuiUtil.getMessage("common.startingImage"));
          stateImageMap.put(StateManageable.STATE_STOPPED, GuiUtil.getMessage("common.stoppedImage"));
          stateImageMap.put(StateManageable.STATE_STOPPING, GuiUtil.getMessage("common.stoppingImage"));
      }
    
    static private final Map<Integer, String> stateMap = new HashMap();
    static{
          stateMap.put(StateManageable.STATE_FAILED, GuiUtil.getMessage("common.failedState"));
          stateMap.put(StateManageable.STATE_RUNNING, GuiUtil.getMessage("common.runningState"));
          stateMap.put(StateManageable.STATE_STARTING, GuiUtil.getMessage("common.startingState"));
          stateMap.put(StateManageable.STATE_STOPPED, GuiUtil.getMessage("common.stoppedState"));
          stateMap.put(StateManageable.STATE_STOPPING, GuiUtil.getMessage("common.stoppingState"));
      }
}


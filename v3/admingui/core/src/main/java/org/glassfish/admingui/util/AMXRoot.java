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
 * amxRoot.java
 *
 * Created on Feb 24, 2008, 2:45 PM
 * 
 * author:  anilam
 */

package org.glassfish.admingui.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.SystemInfo;
import com.sun.appserv.management.base.UploadDownloadMgr;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.HTTPListenerConfig;
import com.sun.appserv.management.config.IIOPServiceConfig;
import com.sun.appserv.management.config.IIOPListenerConfig;
import com.sun.appserv.management.config.J2EEApplicationConfig;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.ServerConfig;
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
import com.sun.appserv.management.config.JavaConfig;
//import com.sun.appserv.management.helper.LBConfigHelper;
import com.sun.appserv.management.util.misc.GSetUtil; 

import com.sun.appserv.management.client.ProxyFactory;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;


import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  


public class AMXRoot {

    private static AMXRoot amxRoot = null  ;

    private  final DomainRoot domainRoot;
    private  final DomainConfig domainConfig;
    private  final J2EEDomain j2eeDomain;
    private  final MonitoringRoot monitoringRoot;
    private  final QueryMgr queryMgr;
    private  final UploadDownloadMgr uploadDownloadMgr;
    private  final WebServiceMgr webServiceMgr;
    //private  final LBConfigHelper lbConfigHelper;

    private AMXRoot(DomainRoot dd) {
        System.out.println("=========== In AMX Root constructor, DomainRoot  = " + dd);
        domainRoot = dd;
        domainConfig = domainRoot.getDomainConfig();
        System.out.println("========= getDomainConfig() returns " + domainConfig);
        j2eeDomain = domainRoot.getJ2EEDomain();
        monitoringRoot = domainRoot.getMonitoringRoot();
        queryMgr = domainRoot.getQueryMgr();
        uploadDownloadMgr = domainRoot.getUploadDownloadMgr();
        webServiceMgr = domainRoot.getWebServiceMgr();
        //lbConfigHelper = new LBConfigHelper(domainRoot);
    }
    
    public static AMXRoot getInstance(){
	if (amxRoot == null){
    	    MBeanServer mMBeanServer = ManagementFactory.getPlatformMBeanServer();
            DomainRoot domainRoot = ProxyFactory.getInstance( mMBeanServer ).getDomainRoot();
            System.out.println("=============== domainRoot = " + domainRoot);
            System.out.println("============== domainRoot name = " + domainRoot.getAppserverDomainName());
            domainRoot.waitAMXReady();
	    amxRoot = new AMXRoot(domainRoot);
            System.out.println("========== amxRoot = " + amxRoot);
        
	} 
	return amxRoot;
    }
    public  DomainConfig getDomainConfig() {
        /*
        ConfigConfig config = getConfig("server-config");
        System.out.println("=========== ConfigConfig = " + config);
        JavaConfig javaConfig = config.getJavaConfig();
        System.out.println("============ JavaConfig = " + javaConfig);
        String[] options = javaConfig.getJVMOptions();
        System.out.println("========= options = " + options);
        */
        
        return domainConfig;
    }
    
    public  J2EEDomain getJ2EEDomain() {
        return j2eeDomain;
    }
    
    public  MonitoringRoot getMonitoringRoot() {
        return monitoringRoot ;
    }
   
    public  WebServiceMgr getWebServiceMgr() {
        return webServiceMgr;
    }
    
    public  DomainRoot getDomainRoot() {
        return domainRoot;
    }
    
    public  QueryMgr getQueryMgr() {
        return queryMgr;
    }
    
    public  UploadDownloadMgr getUploadDownloadMgr() {
        return uploadDownloadMgr;
    }
    
    
//    public LBConfigHelper getLBConfigHelper(){
//        return lbConfigHelper;
//    }
    
    //IF the instance is not running or when no monitoring is on, null will be returned;
    public  ServerRootMonitor getServerRootMonitor(String instanceName){
        if (GuiUtil.isEmpty(instanceName))
            return null;
        return monitoringRoot.getServerRootMonitorMap().get(instanceName);
    }
    
        
    /**
     *	<p> Get the root of the Config mbean
     *
     *	@param	configName  The name of the config.	
     *	
     *	@return	config with the specified name,  or NULL if no such config exists.
     */
    public ConfigConfig getConfig(String configName){
        if (GuiUtil.isEmpty(configName)) return null;
        Map<String,ConfigConfig> cmap = (Map) domainConfig.getConfigConfigMap();
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
    public String getConfigName(String name){
        
        Map sm = domainConfig.getStandaloneServerConfigMap();
        Map cm = domainConfig.getClusteredServerConfigMap();
        if(true)
            return "server-config";
        //try to see if its a server, if not, try cluster.
        ServerConfig serverConfig = domainConfig.getServerConfigMap().get(name);
        if (serverConfig != null){
            return serverConfig.getReferencedConfigName();
        }
        ClusterConfig clusterConfig = domainConfig.getClusterConfigMap().get(name);
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
    public ConfigConfig getConfigByInstanceOrClusterName(String instanceName){
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
    public  HTTPListenerConfig getHTTPListenerConfig(String configName, String listenerName){
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
    public  IIOPListenerConfig getIIOPListenerConfig(String configName, String listenerName){
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
    
    public  boolean isEE(){
        //TODO-V3
        SystemInfo systemInfo = domainRoot.getSystemInfo();
        if (systemInfo == null)
            return false;
        return systemInfo.supportsFeature(SystemInfo.CLUSTERS_FEATURE);
    }
    
    
    public  boolean supportCluster(){
        return false;
        
        //TODO-V3-AMX
        //SystemInfo systemInfo = domainRoot.getSystemInfo();
        //return systemInfo.supportsFeature(SystemInfo.CLUSTERS_FEATURE);
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
    public  String getPropertyValue(PropertiesAccess mbean, String propName) {
        return mbean.getPropertyValue(propName);
    }  
    
    
     public  WebServiceEndpointInfo getWebServiceEndpointInfo(Object webServiceKey) {
        return webServiceMgr.getWebServiceEndpointInfo(webServiceKey);
     }

     /**
     * Function to edit the properties on a server.
     * All MBeans that have Properties extend PropertiesAccess interface
     * @param handlerCtx Handler Context
     * @param config Config object whose properties have been modified 
     * eg.EJBContainerConfig
     */
     public  void editProperties(HandlerContext handlerCtx, PropertiesAccess config){
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
     public  void updateProperties(PropertiesAccess config, Map<String,String>newProps, List ignore){
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
     public  Map convertToPropertiesOptionMap(Map<String,String> props, Map<String,String> convertedMap){
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
     public  Map getNonSkipPropertiesMap(PropertiesAccess config, List skipList) {
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
      public  void changeProperty(PropertiesAccess config, String propName, String propValue){
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
     
     
     public  String getAppType(String name){
        Set<AMXConfig> applications = queryMgr.queryJ2EETypesSet(APP_TYPES);
        for(AMXConfig app : applications){
            if (app.getName().equals(name)){
                return app.getJ2EEType();
            }
        }
        return "";
    }
    
      final private Set<String> APP_TYPES = GSetUtil.newUnmodifiableStringSet(
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
     public  String getStatusForDisplay(AMX obj, boolean includeString){
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
     
     
     public  String getStatusImage(int state) {
        return stateImageMap.get(state);
     }    
     
     public  String getStatusString(int state) {
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


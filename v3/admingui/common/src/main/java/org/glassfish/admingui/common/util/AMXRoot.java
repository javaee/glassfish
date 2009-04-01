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
package org.glassfish.admingui.common.util;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.UploadDownloadMgr;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.config.ApplicationsConfig;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.ClustersConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.HTTPListenerConfig;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.IIOPListenerConfig;
import com.sun.appserv.management.config.IIOPServiceConfig;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.ServerConfig;
import com.sun.appserv.management.ext.realm.RealmsMgr;
import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo;
import com.sun.appserv.management.ext.wsmgmt.WebServiceMgr;
import com.sun.appserv.management.j2ee.J2EEDomain;
import com.sun.appserv.management.j2ee.J2EEServer;
import com.sun.appserv.management.j2ee.StateManageable;
import com.sun.appserv.management.monitor.MonitoringRoot;
import com.sun.appserv.management.monitor.ServerRootMonitor;
import com.sun.appserv.management.config.ConfigsConfig;

import com.sun.appserv.management.config.PropertyConfig;
import com.sun.appserv.management.config.ResourcesConfig;
import com.sun.appserv.management.config.ServersConfig;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;

import org.jvnet.hk2.component.Habitat;
import com.sun.appserv.management.client.AMXBooter;
import com.sun.appserv.management.ext.runtime.RuntimeMgr;


/**
 *
 *  @author  anilam
 */
public class AMXRoot {

    private static AMXRoot amxRoot = null  ;

    private  final DomainRoot domainRoot;
    private  final DomainConfig domainConfig;
    private  final ApplicationsConfig applicationsConfig;
    private  final ConfigsConfig configsConfig;
    private  final J2EEDomain j2eeDomain;
    private  final MonitoringRoot monitoringRoot;
    private  final QueryMgr queryMgr;
    private  final UploadDownloadMgr uploadDownloadMgr;
    private  final WebServiceMgr webServiceMgr;
    private  final MBeanServerConnection mbaenServerConnection;
    private  final ResourcesConfig resourcesConfig;
    private  final ServersConfig serversConfig;
    private  final ClustersConfig clustersConfig;
    private  final RealmsMgr realmsMgr;
    private  final RuntimeMgr runtimeMgr;
    //private  final LBConfigHelper lbConfigHelper;

    private AMXRoot(DomainRoot dd,  MBeanServerConnection msc) {
        //System.out.println("=========== In AMX Root constructor, DomainRoot  = " + dd);
        domainRoot = dd;
        domainConfig = dd.getDomainConfig();
        configsConfig = (ConfigsConfig) domainRoot.getQueryMgr().querySingletonJ2EEType("X-ConfigsConfig");
        j2eeDomain = domainRoot.getJ2EEDomain();
        monitoringRoot = domainRoot.getMonitoringRoot();
        queryMgr = domainRoot.getQueryMgr();
        uploadDownloadMgr = domainRoot.getUploadDownloadMgr();
        webServiceMgr = domainRoot.getWebServiceMgr();
        mbaenServerConnection = msc;
        resourcesConfig = domainConfig.getResourcesConfig();
        applicationsConfig = domainConfig.getApplicationsConfig();
        serversConfig = domainConfig.getServersConfig();
        clustersConfig = domainConfig.getClustersConfig();
        realmsMgr = domainRoot.getRealmsMgr();
        runtimeMgr = domainRoot.getRuntimeMgr();
        //lbConfigHelper = new LBConfigHelper(domainRoot);
    }

    /**
     *	<p> Use this method to get the singleton instance of this object.</p>
     *
     *	<p> On the first invokation of this method, it will obtain the official
     *	    MBeanServer from the <code>Habitat</code>.  This will cause the
     *	    MBeanServer to initialize when this is called, if it hasn't already
     *	    been initialized.</p>
     */
    public synchronized static AMXRoot getInstance() {
	if (amxRoot == null) {
	    // Get the ServletContext
	    ServletContext servletCtx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();

	    // Get the Habitat from the ServletContext
            Habitat habitat = (Habitat) servletCtx.getAttribute(
                org.glassfish.admingui.common.plugin.ConsoleClassLoader.HABITAT_ATTRIBUTE);

	    // Get the MBeanServer via the Habitat, we want the "official" one
	    MBeanServer mbs = (MBeanServer) habitat.getComponent(MBeanServer.class);
            if (mbs == null){
                 mbs = java.lang.management.ManagementFactory.getPlatformMBeanServer();
            }
            if (mbs == null){
                System.out.println("!!!!!!!!!!!!!!  Cannot get to MBeanServer");
            }
            AMXBooter.bootAMX(mbs);
            DomainRoot domainRoot = ProxyFactory.getInstance(mbs).getDomainRoot(); 
	    domainRoot.waitAMXReady();
            amxRoot = new AMXRoot(domainRoot, mbs);
	}
        return amxRoot;
    }

    /**
     * Returns the ${link MBeanServerConnection} for remote use. In the local
     * case it just returns the MBeanServer which is the super interface of
     * MBeanServerConnection. Using the returned object, the calling code can
     * call general MBeanServerConnection methods.
     * @return an instance of javax.management.MBeanServerConnection
     */
    public MBeanServerConnection getMBeanServerConnection() {
        return mbaenServerConnection;
        //for now, ideally, this should be got from AppserverConnectionSource
    }



    public  DomainConfig getDomainConfig() {
        return domainConfig;
    }
    
    public  ConfigsConfig getConfigsConfig() {
        return configsConfig;
    }
    
    public  ResourcesConfig getResourcesConfig() {
        return resourcesConfig;
    }

    public  ApplicationsConfig getApplicationsConfig() {
        return applicationsConfig;
    }
    
    public  ServersConfig getServersConfig() {
        return serversConfig;
    }
    
    public  ClustersConfig getClustersConfig() {
        return clustersConfig;
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
    
    public  RealmsMgr getRealmsMgr() {
        return realmsMgr;
    }
    
    public  RuntimeMgr getRuntimeMgr() {
        return runtimeMgr;
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
     *	<p> Get the ConfigConfig mbean based on its name
     *
     *	@param	configName  The name of the config.
     *
     *	@return	config with the specified name,  or NULL if no such config exists.
     */
    public ConfigConfig getConfig(String configName){
        if (GuiUtil.isEmpty(configName)) return null;
        Map<String,ConfigConfig> cmap = (Map) configsConfig.getConfigConfigMap();
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

        Map sm = domainConfig.getServersConfig().getStandaloneServerConfigMap();
        Map cm = domainConfig.getServersConfig().getClusteredServerConfigMap();
        if(true)
            return "server-config";
        //try to see if its a server, if not, try cluster.
        ServerConfig serverConfig = domainConfig.getServersConfig().getStandaloneServerConfigMap().get(name);
        if (serverConfig != null){
            return serverConfig.getConfigRef();
        }
        serverConfig = domainConfig.getServersConfig().getClusteredServerConfigMap().get(name);
        if (serverConfig != null){
            return serverConfig.getConfigRef();
        }
        ClusterConfig clusterConfig = domainConfig.getClustersConfig().getClusterConfigMap().get(name);
        if (clusterConfig != null)
            return clusterConfig.getConfigRef();
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
        return false;
    
        //TODO-V3
        /*
        SystemInfo systemInfo = domainRoot.getSystemInfo();
        if (systemInfo == null)
            return false;
        return systemInfo.supportsFeature(SystemInfo.CLUSTERS_FEATURE);
         * */
    }


    public  boolean supportCluster(){
        return false;

        //TODO-V3-AMX
        //SystemInfo systemInfo = domainRoot.getSystemInfo();
        //return systemInfo.supportsFeature(SystemInfo.CLUSTERS_FEATURE);
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
         ArrayList<String> removeProps = (ArrayList)handlerCtx.getInputValue("RemoveProps");
         if(removeProps != null){
             for(int i=0; i<removeProps.size(); i++){
                 config.removePropertyConfig(removeProps.get(i));
             }
         }
         if(addProps != null ){
             Map<String, PropertyConfig> pMap = config.getPropertyConfigMap();
             for(String key: addProps.keySet()){
                 String value = addProps.get(key);
                 if (pMap.containsKey(key))
                     pMap.get(key).setValue(value);
                 else
                     config.createPropertyConfig(key, value);
             }
         }
     }
     
     
     public  WebServiceEndpointInfo getWebServiceEndpointInfo(Object webServiceKey) {
        return webServiceMgr.getWebServiceEndpointInfo(webServiceKey);
     }



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

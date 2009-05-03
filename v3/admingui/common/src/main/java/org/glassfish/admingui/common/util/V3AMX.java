/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.util;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.management.Attribute;
import javax.management.AttributeList;

import javax.servlet.ServletContext;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.proxy.AMXBooter;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.intf.config.ConfigConfig;
import org.glassfish.admin.amx.intf.config.ConfigsConfig;
import org.glassfish.admin.amx.intf.config.DeployedItemRefConfig;
import org.glassfish.admin.amx.intf.config.DomainConfig;


import org.glassfish.admin.amx.intf.config.VirtualServerConfig;
import org.glassfish.admin.amx.intf.config.HttpServiceConfig;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkConfig;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkListener;
import org.glassfish.admin.amx.intf.config.grizzly.Protocol;
import org.jvnet.hk2.component.Habitat;

/**
 *
 * @author anilam
 */
public class V3AMX {
    private static V3AMX v3amx  ;
    private final DomainRoot domainRoot;
    private final DomainConfig domainConfig;
    private final ProxyFactory proxyFactory;
    private final MBeanServerConnection mbeanServer;

    private V3AMX(DomainRoot dd, MBeanServerConnection msc) {
        proxyFactory = ProxyFactory.getInstance(msc);
        domainRoot = dd;
        domainConfig =  domainRoot.getDomain().as(DomainConfig.class);
        mbeanServer = msc;
    }

     /**
     *	<p> Use this method to get the singleton instance of this object.</p>
     *
     *	<p> On the first invokation of this method, it will obtain the official
     *	    MBeanServer from the <code>Habitat</code>.  This will cause the
     *	    MBeanServer to initialize when this is called, if it hasn't already
     *	    been initialized.</p>
     */
    public synchronized static V3AMX getInstance() {
	if (v3amx == null) {
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
            DomainRoot domainRoot = ProxyFactory.getInstance(mbs).getDomainRootProxy();
            v3amx = new V3AMX( domainRoot, mbs);
	}
        return v3amx;
    }


    public DomainConfig getDomainConfig(){
        return domainConfig;
    }

    public DomainRoot getDomainRoot(){
        return domainRoot;
    }

    public ConfigsConfig getConfigsConfig(){
        return domainConfig.getConfigs();
    }

    public ProxyFactory getProxyFactory(){
        return proxyFactory;
    }

    public AMXProxy getProxy( ObjectName objName){
        return proxyFactory.getProxy(objName);
    }

    public MBeanServerConnection getMbeanServerConnection(){
        return mbeanServer;
    }

    public static ConfigConfig getServerConfig(String configName){
        if ((configName == null) || configName.equals(""))
                configName = "server-config";
        return V3AMX.getInstance().getConfigsConfig().getConfig().get(configName);
    }

    public static void setAttribute(String objectName, Attribute attributeName) {
	try {
	    setAttribute(new ObjectName(objectName), attributeName);
	}catch (javax.management.MalformedObjectNameException ex){
            System.out.println("MalformedObjectNameException: " + objectName);
            throw new RuntimeException(ex);
        }
    }

    public static void setAttribute(ObjectName objectName, Attribute attributeName) {
	try {
	    V3AMX.getInstance().getMbeanServerConnection().setAttribute(objectName, attributeName);
	} catch (Exception  ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void setAttributes(Object name, Map<String, Object> attrMap){
        try{
            ObjectName objectName = null;
            if (name instanceof String){
                objectName = new ObjectName((String) name);
            }else
                objectName = (ObjectName) name;
            AttributeList attrList = new AttributeList();
            for(String key : attrMap.keySet()){
                if (skipAttr.contains(key))
                    continue;
                Attribute attr = new Attribute(key, (String)attrMap.get(key));
                attrList.add(attr);
            }
            setAttributes(objectName, attrList);
        }catch(Exception ex){
            throw new RuntimeException (ex);
        }
    }
    
    public static void setAttributes(ObjectName objectName, AttributeList attrList) {
	try {
	    V3AMX.getInstance().getMbeanServerConnection().setAttributes(objectName, attrList);
	} catch (Exception  ex) {
            throw new RuntimeException(ex);
        }
    }


    //TODO:  will need to resolve system property, if the key starts with $
    public static String getHttpPortNumber(String serverName, String configName){
        StringBuffer ports = new StringBuffer();
        try{
//            ObjectName serverObj = new ObjectName("v3:pp=/domain/servers,type=server,name=" + serverName);
            //Map<String,NetworkListener> nls = V3AMX.getServerConfig(configName).child(NetworkConfig.class).child(NetworkListeners.class).childrenMap(NetworkListener.class);
            ConfigConfig config = V3AMX.getServerConfig(configName);
            Map<String, NetworkListener> nls = config.getNetworkConfig().as(NetworkConfig.class).getNetworkListeners().getNetworkListener();
            for (NetworkListener listener : nls.values()){
                String port = (String) listener.attributesMap().get("Port");
//                if (port.startsWith("$")) {
//                    port = listener.resolveToken((port.substring(2, port.length() - 1)), instanceName);
//                }
                ports = ports.append("," + port);
            }
            if (ports.length() > 0){
                ports.deleteCharAt(0);  //remove the first ','
            }
            return ports.toString();
        }catch(Exception ex){
            return "";
        }
    }


    //Application Utils
    /* returns the port number on which appName could be executed
     * will try to get a port number that is not secured.  But if it can't find one, a
     * secured port will be returned, prepanded with '-'
     */
    public static String getPortForApplication(String appName) {
        try{
            String objectNameStr ="v3:pp=/domain/servers,type=server,name=server";
            AMXProxy  server = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectNameStr));
            DeployedItemRefConfig appRef = server.childrenMap("application-ref").get(appName).as(DeployedItemRefConfig.class);
            NetworkListener listener = null;
            if (appRef == null) { // no application-ref found for this application, shouldn't happen for PE. TODO: think about EE
                listener = getListener();
            } else {
                String vsId = appRef.getVirtualServers();
                if (vsId == null || vsId.length() == 0) { // no vs specified
                    listener = getListener();
                } else {
                    listener = getListener(vsId);

                }
            }
            if (listener == null) {
                return null;
            }
            String port = (String) listener.attributesMap().get("Port");
            String security = (String)listener.findProtocol().attributesMap().get("SecurityEnabled");
            return ("true".equals(security)) ? "-" + port : port;
        }catch(Exception ex){
            return "";
        }
    }

    // returns a  http-listener that is linked to a non-admin VS
    private static NetworkListener getListener() {
        Map<String, VirtualServerConfig> vsMap = V3AMX.getServerConfig("server-config").getHttpService().getVirtualServer();
        return getOneVsWithNetworkListener(new ArrayList(vsMap.keySet()));
    }

    private static NetworkListener getListener(String vsIds) {
        return getOneVsWithNetworkListener(GuiUtil.parseStringList(vsIds, ","));
    }

    private static NetworkListener getOneVsWithNetworkListener(List<String> vsList) {
        if (vsList == null || vsList.size() == 0) {
            return null;
        }
        NetworkListener secureListener = null;
        HttpServiceConfig hConfig = V3AMX.getServerConfig("server-config").getHttpService();
        Map<String, VirtualServerConfig> vsMap = V3AMX.getServerConfig("server-config").getHttpService().getVirtualServer();
        for (String vsName : vsList) {
            if (vsName.equals("__asadmin")) {
                continue;
            }
            VirtualServerConfig vs = vsMap.get(vsName);
            String listener = (String) vs.attributesMap().get("NetworkListeners");
            if (GuiUtil.isEmpty(listener)) {
                continue;
            } else {
                List<String> hpList = GuiUtil.parseStringList(listener, ",");
                for (String one : hpList) {
                    NetworkListener oneListener = V3AMX.getServerConfig("server-config").getNetworkConfig().as(NetworkConfig.class).getNetworkListeners().getNetworkListener().get(one);
                    if (!"true".equals(oneListener.attributesMap().get("Enabled"))) {
                        continue;
                    }
                    String security = (String)oneListener.findProtocol().attributesMap().get("SecurityEnabled");
                    if ("true".equals(security)) {
                        secureListener = oneListener;
                        continue;
                    } else {
                        return oneListener;
                    }
                }
            }
        }
        return secureListener;
    }

    final private static List skipAttr = new ArrayList();
    static{
        skipAttr.add("Parent");
        skipAttr.add("Name");
    }


}

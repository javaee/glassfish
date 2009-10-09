/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.util;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.intf.config.Config;
import org.glassfish.admin.amx.intf.config.HttpService;
import org.glassfish.admin.amx.intf.config.VirtualServer;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkConfig;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkListener;


/**
 *
 * @author anilam
 */
public class V3AMXUtil {

    public static String getInstallDir(){
        return V3AMX.getInstance().getDomainRoot().getInstallDir();
    }

    public static Integer getAdminPort(){
        AMXConfigProxy amx = (AMXConfigProxy)V3AMX.getInstance().getAdminListener();
        String port = (String) amx.resolveAttribute("Port");
        return Integer.valueOf(port);
    }


    //TODO:  will need to resolve system property, if the key starts with $
    public static String getHttpPortNumber(String serverName, String configName){
        StringBuffer ports = new StringBuffer();
        try{
            Config config = V3AMX.getServerConfig(configName);
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
            AMXProxy  server = (AMXProxy) V3AMX.getInstance().getDomain().getServers().getServer().get("server");
            AMXProxy appRef = server.childrenMap("application-ref").get(appName);
            NetworkListener listener = null;
            if (appRef == null) { // no application-ref found for this application, shouldn't happen for PE. TODO: think about EE
                listener = getListener();
            } else {
                String vsId = (String)appRef.attributesMap().get("VirtualServers");
                if (vsId == null || vsId.length() == 0) { // no vs specified
                    listener = getListener();
                } else {
                    listener = getListener(vsId);

                }
            }
            if (listener == null) {
                return null;
            }
            String port = (String) listener.resolveAttribute("Port");
            String security = (String)listener.findProtocol().attributesMap().get("SecurityEnabled");
            return ("true".equals(security)) ? "-" + port : port;
        }catch(Exception ex){
            GuiUtil.getLogger().warning(ex.getMessage());
            return "";
        }
    }

    // returns a  http-listener that is linked to a non-admin VS
    private static NetworkListener getListener() {
        Map<String, VirtualServer> vsMap = V3AMX.getServerConfig("server-config").getHttpService().childrenMap(VirtualServer.class);
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
        HttpService hConfig = V3AMX.getServerConfig("server-config").getHttpService();
        Map<String, VirtualServer> vsMap = V3AMX.getServerConfig("server-config").getHttpService().childrenMap(VirtualServer.class);
        for (String vsName : vsList) {
            if (vsName.equals("__asadmin")) {
                continue;
            }
            VirtualServer vs = vsMap.get(vsName);
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

    
}

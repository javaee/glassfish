/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.util;


import java.util.ArrayList;
import java.util.HashMap;
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
    /* returns the launch link of the app.
     * will try to get a port number that is not secured.  But if it can't find one, a
     * secured port will be used.
     */
    public static String getLaunchLink(String serverName, String appName) {
        try{
            AMXProxy  server = (AMXProxy) V3AMX.getInstance().getDomain().getServers().getServer().get("server");
            AMXProxy appRef = server.childrenMap("application-ref").get(appName);
            Map<String,String> result = null;
            if (appRef == null) { // no application-ref found for this application, shouldn't happen for PE. TODO: think about EE
                result = getListener();
            } else {
                String vsId = (String)appRef.attributesMap().get("VirtualServers");
                if (vsId == null || vsId.length() == 0) { // no vs specified
                    result = getListener();
                } else {
                    result = getListener(vsId);

                }
            }
            if (result == null) {
                return null;
            }
            String vs = result.get("vs");
            if (vs.equals("server")){
                vs = serverName;   //this is actually the hostName, more readable for user in the launch URL.
            }
            String port = result.get("port");
            String protocol = result.get("protocol");
            return protocol + "://" + vs + ":" + port ;
        }catch(Exception ex){
            GuiUtil.getLogger().warning(ex.getMessage());
            return null;
        }
    }

    // returns a  http-listener that is linked to a non-admin VS
    private static Map getListener() {
        Map<String, VirtualServer> vsMap = V3AMX.getServerConfig("server-config").getHttpService().childrenMap(VirtualServer.class);
        return getOneVsWithNetworkListener(new ArrayList(vsMap.keySet()));
    }

    private static Map getListener(String vsIds) {
        return getOneVsWithNetworkListener(GuiUtil.parseStringList(vsIds, ","));
    }

    private static Map getOneVsWithNetworkListener(List<String> vsList) {
        Map result = new HashMap();
        if (vsList == null || vsList.size() == 0) {
            return null;
        }
        //Just to ensure we look at "server" first.
        if (vsList.contains("server")){
            vsList.remove("server");
            vsList.add(0, "server");
        }
        boolean found = false;
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
                        //use this secured port, but try to find one thats not secured.
                        result.put("protocol", "https");
                        result.put("port", oneListener.resolveAttribute("Port"));
                        result.put("vs", vsName);
                        found = true;
                        continue;
                    } else {
                        result.put("protocol", "http");
                        result.put("port", oneListener.resolveAttribute("Port"));
                        result.put("vs", vsName);
                        return result;
                    }
                }
            }
        }
        return found ? result : null;
    }

    
}

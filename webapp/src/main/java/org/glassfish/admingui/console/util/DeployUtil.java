/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * DeploymentHandler.java
 *
 */

package org.glassfish.admingui.console.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Level;
import org.glassfish.admingui.console.rest.RestUtil;
import java.util.*;
import java.net.InetAddress;


public class DeployUtil {

    /* reload application for each target.  If the app is disabled for the target, it is an no-op.
     * otherwise, the app is disabled and then enabled to force the reload.
     */


    static public boolean reloadApplication(String appName, List<String> targets){
        try{
            String decodedName = URLDecoder.decode(appName, "UTF-8");
            List clusters =  TargetUtil.getClusters();
            String clusterEndpoint = GuiUtil.getSessionValue("REST_URL")+"/clusters/cluster/";
            String serverEndpoint = GuiUtil.getSessionValue("REST_URL")+"/servers/server/";
            for(String targetName : targets){
                String endpoint ;
                if (clusters.contains(targetName)){
                    endpoint = clusterEndpoint + targetName + "/application-ref/" + decodedName ;
                }else{
                    endpoint = serverEndpoint + targetName + "/application-ref/"  + decodedName ;
                }
                String status = (String) RestUtil.getAttributesMap(endpoint).get("enabled");
                if ( Boolean.parseBoolean(status)){
                    Map attrs = new HashMap();
                    attrs.put("enabled", "false");
                    RestUtil.restRequest(endpoint, attrs, "POST", null, null, false, true);
                    attrs.put("enabled", "true");
                    RestUtil.restRequest(endpoint, attrs, "POST", null, null, false, true);
                }
            }
       }catch(Exception ex){
            GuiUtil.handleError(ex.getMessage());
            return false;
       }
        return true;
    }


    //This method returns the list of targets (clusters and standalone instances) of any deployed application
    static public List getApplicationTarget(String appName, String ref){
        List targets = new ArrayList();
        try{
            //check if any cluster has this application-ref
            List<String> clusters = TargetUtil.getClusters();
            for(String oneCluster:  clusters){
                List appRefs = new ArrayList(RestUtil.getChildMap(GuiUtil.getSessionValue("REST_URL")+"/clusters/cluster/"+oneCluster+"/"+ref).keySet());

                if (appRefs.contains(appName)){
                    targets.add(oneCluster);
                }
            }
            List<String> servers = TargetUtil.getStandaloneInstances();
            servers.add("server");
            for(String oneServer:  servers){
                List appRefs = new ArrayList(RestUtil.getChildMap(GuiUtil.getSessionValue("REST_URL") + "/servers/server/" + oneServer + "/" + ref).keySet());
                if (appRefs.contains(appName)){
                    targets.add(oneServer);
                }
            }

        }catch(Exception ex){
            GuiUtil.getLogger().info(GuiUtil.getCommonMessage("log.error.appTarget") + ex.getLocalizedMessage());
            if (GuiUtil.getLogger().isLoggable(Level.FINE)){
                ex.printStackTrace();
            }
        }
        return targets;
    }
    
    static public List<Map> getRefEndpoints(String name, String ref){
        List endpoints = new ArrayList();
        try{
            String encodedName = URLEncoder.encode(name, "UTF-8");
            //check if any cluster has this application-ref
            List<String> clusters = TargetUtil.getClusters();
            for(String oneCluster:  clusters){
                List appRefs = new ArrayList(RestUtil.getChildMap(GuiUtil.getSessionValue("REST_URL")+"/clusters/cluster/"+oneCluster+"/"+ref).keySet());
                if (appRefs.contains(name)){
                    Map aMap = new HashMap();
                    aMap.put("endpoint", GuiUtil.getSessionValue("REST_URL")+"/clusters/cluster/"+oneCluster+"/" + ref + "/" + encodedName);
                    aMap.put("targetName", oneCluster);
                    endpoints.add(aMap);
                }
            }
            List<String> servers = TargetUtil.getStandaloneInstances();
            servers.add("server");
            for(String oneServer:  servers){
                List appRefs = new ArrayList(RestUtil.getChildMap(GuiUtil.getSessionValue("REST_URL") + "/servers/server/" + oneServer + "/" + ref).keySet());
                if (appRefs.contains(name)){
                    Map aMap = new HashMap();
                    aMap.put("endpoint", GuiUtil.getSessionValue("REST_URL") + "/servers/server/" + oneServer + "/" + ref + "/" + encodedName);
                    aMap.put("targetName", oneServer);
                    endpoints.add(aMap);
                }
            }
        }catch(Exception ex){
            GuiUtil.getLogger().info(GuiUtil.getCommonMessage("log.error.getRefEndpoints")+ ex.getLocalizedMessage());
            if (GuiUtil.getLogger().isLoggable(Level.FINE)){
                ex.printStackTrace();
            }
        }
        return endpoints;
    }


    public static String getTargetEnableInfo(String appName, boolean useImage, boolean isApp){
        String prefix = (String) GuiUtil.getSessionValue("REST_URL");
        List clusters = TargetUtil.getClusters();
        List standalone = TargetUtil.getStandaloneInstances();
        String enabled = "true";
        int numEnabled = 0;
        int numDisabled = 0;
        String ref = "application-ref";
        if (!isApp) {
            ref = "resource-ref";
        }
        if (clusters.isEmpty() && standalone.isEmpty()){
            //just return Enabled or not.
            enabled = (String)RestUtil.getAttributesMap(prefix  +"/servers/server/server/"+ref+"/"+appName).get("enabled");
            //for DAS only system, there should always be application-ref created for DAS. However, for the case where the application is
            //deploye ONLY to a cluster/instance, and then that instance is deleted.  The system becomes 'DAS' only, but then the application-ref
            //for DAS will not exist. For this case, we just look at the application itself
            if (enabled == null){
                enabled = (String)RestUtil.getAttributesMap(prefix +"/applications/application/" + appName).get("enabled");
            }
            if (useImage){
                return (Boolean.parseBoolean(enabled))? "/resource/images/enabled.png" : "/resource/images/disabled.png";
            }else{
                return enabled;
            }
        }
        standalone.add("server");        
        List<String> targetList = new ArrayList<String> ();
        try {
            targetList = getApplicationTarget(URLDecoder.decode(appName, "UTF-8"), ref);
        } catch (Exception ex) {
            //ignore
        }
        for (String oneTarget : targetList) {
            if (clusters.contains(oneTarget)) {
                enabled = (String) RestUtil.getAttributesMap(prefix + "/clusters/cluster/" + oneTarget + "/" + ref + "/" + appName).get("enabled");
            } else {
                enabled = (String) RestUtil.getAttributesMap(prefix + "/servers/server/" + oneTarget + "/" + ref + "/" + appName).get("enabled");
            }
            if (Boolean.parseBoolean(enabled)) {
                numEnabled++;
            } else {
                numDisabled++;
            }
        }
                
        int numTargets = targetList.size();
        /*
        if (numEnabled == numTargets){
            return GuiUtil.getMessage("deploy.allEnabled");
        }
        if (numDisabled == numTargets){
            return GuiUtil.getMessage("deploy.allDisabled");
        }
         */
        return (numTargets==0) ?  GuiUtil.getMessage("deploy.noTarget") :
                GuiUtil.getMessage("deploy.someEnabled", new String[]{""+numEnabled, ""+numTargets });
        
    }

    private static String getVirtualServers(String target, String appName) {
        List clusters = TargetUtil.getClusters();
        List standalone = TargetUtil.getStandaloneInstances();
        standalone.add("server");
        String ep = (String)GuiUtil.getSessionValue("REST_URL");
        if (clusters.contains(target)){
            ep = ep + "/clusters/cluster/" + target + "/application-ref/" + appName;
        }else{
            ep = ep + "/servers/server/" + target + "/application-ref/" + appName;
        }
        String virtualServers =
                (String)RestUtil.getAttributesMap(ep).get("virtualServers");
        return virtualServers;
    }

    private static String calContextRoot(String contextRoot) {
        //If context root is not specified or if the context root is "/", ensure that we don't show two // at the end.
        //refer to issue#2853
        String ctxRoot = "";
        if ((contextRoot == null) || contextRoot.equals("") || contextRoot.equals("/")) {
            ctxRoot = "/";
        } else if (contextRoot.startsWith("/")) {
            ctxRoot = contextRoot;
        } else {
            ctxRoot = "/" + contextRoot;
        }
        return ctxRoot;
    }


    private static Set getURLs(List<String> vsList, String configName, Collection<String> hostNames, String target) {
        Set URLs = new TreeSet();
        if (vsList == null || vsList.size() == 0) {
            return URLs;
        }
        //Just to ensure we look at "server" first.
        if (vsList.contains("server")){
            vsList.remove("server");
            vsList.add(0, "server");
        }
        String ep = (String)GuiUtil.getSessionValue("REST_URL");
        ep = ep + "/configs/config/" + configName + "/http-service/virtual-server";
        Map vsInConfig = new HashMap();
        try{
            vsInConfig = RestUtil.getChildMap(ep);

        }catch (Exception ex){
            GuiUtil.getLogger().info(GuiUtil.getCommonMessage("log.error.getURLs") + ex.getLocalizedMessage());
                if (GuiUtil.getLogger().isLoggable(Level.FINE)){
                    ex.printStackTrace();
                }
        }
        String localHostName = null;
        try {
            localHostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {
            // ignore exception
        }

        for (String vsName : vsList) {
            if (vsName.equals("__asadmin")) {
                continue;
            }
            Object vs = vsInConfig.get(vsName);
            if (vs != null) {
                ep = (String)GuiUtil.getSessionValue("REST_URL") + "/configs/config/" +
                        configName + "/http-service/virtual-server/" + vsName;
                String listener = (String)RestUtil.getAttributesMap(ep).get("networkListeners");

                if (GuiUtil.isEmpty(listener)) {
                    continue;
                } else {
                    List<String> hpList = GuiUtil.parseStringList(listener, ",");
                    for (String one : hpList) {
                        ep = (String)GuiUtil.getSessionValue("REST_URL") +
"/configs/config/" + configName + "/network-config/network-listeners/network-listener/" + one;

                        Map nlAttributes = RestUtil.getAttributesMap(ep);
                        if ("false".equals((String)nlAttributes.get("enabled"))) {
                            continue;
                        }
//                        String security = (String)oneListener.findProtocol().attributesMap().get("SecurityEnabled");
                        ep = (String)GuiUtil.getSessionValue("REST_URL") + "/configs/config/" +
                                configName + "/network-config/protocols/protocol/" + (String)nlAttributes.get("protocol");
                        String security = (String)RestUtil.getAttributesMap(ep).get("securityEnabled");

                        String protocol = "http";
                        if ("true".equals(security))
                            protocol = "https";

                        String port = (String)nlAttributes.get("port");
                        if (port == null)
                            port = "";
                        String resolvedPort = RestUtil.resolveToken((String)GuiUtil.getSessionValue("REST_URL") +
                                "/servers/server/" + target, port);

                        for (String hostName : hostNames) {
                            if (localHostName != null && hostName.equalsIgnoreCase("localhost"))
                                hostName = localHostName;
//                            URLs.add("[" + target + "]  - " + protocol + "://" + hostName + ":" + resolvedPort + "[ " + one + " " + configName
//                                    + " " + listener + " " + target + " ]");
                            URLs.add(target + "@@@" + protocol + "://" + hostName + ":" + resolvedPort);
                        }
                    }
                }
            }
        }
        return URLs;
    }

    public static List<Map<String, String>> getTargetURLList(String appID, String contextRoot) {
        String ctxRoot = calContextRoot(contextRoot);
        Set<String> URLs = new TreeSet();
        List<String> targetList = DeployUtil.getApplicationTarget(appID, "application-ref");
        for(String target : targetList) {
            String ep = TargetUtil.getTargetEndpoint(target) + "/application-ref/" + appID;
            boolean enabled = Boolean.parseBoolean((String)RestUtil.getAttributesMap(ep).get("enabled"));
            if (!enabled)
                continue;

            String virtualServers = getVirtualServers(target, appID);
            String configName = TargetUtil.getConfigName(target);

            List clusters = TargetUtil.getClusters();
            List<String> instances = new ArrayList();
            if (clusters.contains(target)){
                instances = TargetUtil.getClusteredInstances(target);
            } else {
                instances.add(target);
            }

            for (String instance : instances) {
                Collection<String> hostNames = TargetUtil.getHostNames(instance);
                URLs.addAll(getURLs(GuiUtil.parseStringList(virtualServers, ","), configName, hostNames, instance));
            }
        }

	Iterator it = URLs.iterator();
	String url = null;
        ArrayList<Map<String, String>> list = new ArrayList();

	while (it.hasNext()) {
	    url = (String)it.next();
            String target = "";
            int i = url.indexOf("@@@");
            if (i >= 0) {
                target = url.substring(0, i);
                url = url.substring(i + 3);
            }

            HashMap<String, String> m = new HashMap();
            m.put("url", url + ctxRoot);
            m.put("target", target);
            list.add(m);
	}
        return list;


    }




}

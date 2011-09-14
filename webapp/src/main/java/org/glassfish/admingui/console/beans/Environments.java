/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console.beans;

import org.glassfish.admingui.console.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.glassfish.admingui.console.rest.RestUtil;
import org.glassfish.admingui.console.util.CommandUtil;

/**
 *
 * @author anilam
 */
@ManagedBean
@ViewScoped
public class Environments implements Serializable {

    transient private List<Map> services;
    transient private final List<Map> apps = new ArrayList<Map>();
    transient private final List<Map> appsForEnv = new ArrayList<Map>();
    transient private final List<Map> instancesForEnv = new ArrayList<Map>();


    public List<Map> getEnvsAndApps() {
        List envList = new ArrayList();
        String prefix = REST_URL+"/clusters/cluster/" ;
        try{
            List<String> clusterList = new ArrayList(RestUtil.getChildMap(prefix).keySet());
            //System.out.println("====== getEnvsAndApps --- " + clusterList);
            if ( (clusterList != null) && (! clusterList.isEmpty())){
                //For each cluster, consider that as Environment only if it has chlid element <virtual-machine-config>
                for(String oneCluster : clusterList){
                    List<String> instanceList = RestUtil.getChildNameList(prefix+oneCluster+"/server-ref");
                    if (instanceList == null || instanceList.isEmpty()){
                        continue;
                    }
                    //assume that if the cluster has VMC, thats an environment
                    if (RestUtil.doesProxyExist( prefix + oneCluster + "/virtual-machine-config/" + instanceList.get(0))){
                        List<String> apps = RestUtil.getChildNameList(prefix+oneCluster+"/application-ref");
                        System.out.println("======== getEnvsAndApps : " + apps );
                        Map env = new HashMap();
                        env.put("clusterName", oneCluster);
                        env.put("instanceCount",  instanceList.size());
                        env.put("instanceList", instanceList);
                        env.put("appList", apps);
                        envList.add(env);
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        //System.out.println("================= getEnvs() return envList=" + envList);
        return envList;
    }


    public List<Map> getAppsForEnv(String envName) {
        //System.out.println("========= in getAppsForEnv");
        synchronized (appsForEnv) {
            try {
                if (appsForEnv.isEmpty()) {
                    List<String> appsNameList = RestUtil.getChildNameList(REST_URL + "/clusters/cluster/" + envName + "/application-ref");
                    System.out.println("======== appsNameList = " + appsNameList);
                    for (String oneApp : appsNameList) {
                        String contextRoot = (String) RestUtil.getAttributesMap(REST_URL + "/applications/application/" + oneApp).get("contextRoot");
                        Map aMap = new HashMap();
                        aMap.put("appName", oneApp);
                        aMap.put("contextRoot", contextRoot);
                        appsForEnv.add(aMap);
                        System.out.println("======== add " +  oneApp);
                    }
                }
            } catch (Exception ex) {
            }
        }
        //System.out.println("================================== returns " + appsForEnv);
        return appsForEnv;
    }

    public List<Map> getInstancesForEnv(String envName) {
        synchronized (instancesForEnv) {
            try {
                if (instancesForEnv.isEmpty()){
                    Map attrs = new HashMap();
                    attrs.put("whichtarget", envName);
                    List<Map> iList =  RestUtil.getListFromREST(REST_URL+"/list-instances", attrs, "instanceList");
                    for(Map oneI : iList){
                        if("running".equals(((String)oneI.get("status")).toLowerCase())){
                            oneI.put("statusImage", "/images/running_small.gif");
                        }else{
                            oneI.put("statusImage", "/images/not-running_small.png");
                        }

                        instancesForEnv.add(oneI);
                    }
                }
            } catch (Exception ex) {

                }
        }
        return instancesForEnv;
    }

    public List<String> getInstancesNameForEnv(String envName){
        List<Map> instancesMap = getInstancesForEnv(envName);
        List<String> instancesName = new ArrayList();
        for(Map oneInstance : instancesMap){
            instancesName.add((String)oneInstance.get("name"));
        }
        return instancesName;
    }


    public List<Map> getApplications() {
        synchronized (apps) {
            if (apps.isEmpty()) {
                Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
                List<String> deployingApps = (List) sessionMap.get("_deployingApps");
                String endPoint = REST_URL+"/applications/list-applications";
                Map attrs = new HashMap();
                attrs.put("target", "domain");  //specify domain to get Paas deployed app.
                Map appData = (Map) RestUtil.restRequest(endPoint, attrs, "GET", null, null, false, true).get("data");
                Map<String, String> props = (Map) appData.get("properties");
                if (props != null) {
                    for (String appName : props.keySet()) {
                        Map app = new HashMap();
                        app.put("appName", appName);
                        app.put("notExist", false);
                        apps.add(app);
                        if (deployingApps != null && deployingApps.contains(appName)) {
                            deployingApps.remove(appName);
                        }
                    }
                }
                if (deployingApps != null) {
                    for (String one : deployingApps) {
                        Map app = new HashMap();
                        app.put("appName", one);
                        app.put("notExist", true);
                        apps.add(app);
                    }
                }
            }
        }
        return apps;
    }

    public List<Map> getServices() {
        synchronized (this) {
            if (services == null) {
                services = CommandUtil.listServices(null, null, null);

                //In the overview page, we don't want to show clusterInstance type.
                if (!services.isEmpty()) {
                    Iterator iter = services.iterator();
                    while (iter.hasNext()) {
                        Map oneEntry = (Map) iter.next();
                        if ("ClusterInstance".equals(oneEntry.get("serverType"))) {
                            iter.remove();
                        }
                        if ("Cluster".equals(oneEntry.get("serverType"))) {
                            oneEntry.put("serverType", "JavaEE");
                        }
                    }
                }
            }
        }
        return services;
    }

   
    public static final String REST_URL = "http://localhost:4848/management/domain";
}
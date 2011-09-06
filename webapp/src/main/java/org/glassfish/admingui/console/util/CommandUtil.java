/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.console.util;

import java.util.*;

import org.glassfish.admingui.console.rest.RestUtil;

/**
 *
 * @author anilam
 */
public class CommandUtil {
    public static String SERVICE_TYPE_RDMBS = "Datebase";
    public static final String SERVICE_TYPE_JAVAEE = "JavaEE";
    private static final String SERVICE_TYPE_LB = "LB";


    /**
     *	<p> This method returns the list of Services. </p>
     *
     *	@param	appName	    Name of Application. This is optional parameter, can be set to NULL.
     *	@param	type        Service type. Possible value is "Cluster", "ClusterInstance", "database".
     *                      This is optional parameter, can be set to Null.
     *  @param  scope       Scope of services.  Possible value is "external", "shared", "application".
     *                      This is optional parameter, can be set to NULL.
     *
     *	@return	<code>List<Map></code>  Each map represents one Service.
     */
    public static List<Map> listServices(String appName, String type, String scope){

        List<Map>services = null;
        //String endpoint = GuiUtil.getSessionValue("REST_URL")+"/list-services";
        String endpoint = "http://localhost:4848/management/domain/list-services";

        Map attrs = new HashMap();
        putOptionalAttrs(attrs, "appname", appName);
        putOptionalAttrs(attrs, "type", type);
        putOptionalAttrs(attrs, "scope", scope);


        services = getListFromREST(endpoint, attrs, "list");
        if (services == null){
            services = new ArrayList();
            /*
            //temp, just put in some dummy data.
            Map m1 = new HashMap();
            m1.put("serviceName", "admin-console-service");
            m1.put("applicationName", "admin-console");
            m1.put("type", "Cluster");
            m1.put("scope", "application");
            m1.put("ip-address", "NA");
            m1.put("instance-id", "1");
            services.add(m1);

            Map m2 = new HashMap();
            m2.put("serviceName", "admin-console-service.instance-1");
            m2.put("applicationName", "admin-console");
            m2.put("type", "ClusterInstance");
            m2.put("scope", "application");
            m2.put("ip-address", "127.0.0.1");
            m2.put("instance-id", "2");
            services.add(m2);

            Map m3 = new HashMap();
            m3.put("serviceName", "admin-console-service.instance-2");
            m3.put("applicationName", "admin-console");
            m3.put("type", "ClusterInstance");
            m3.put("scope", "application");
            m3.put("ip-address", "127.0.0.2");
            m3.put("instance-id", "3");
            services.add(m3);
             * 
             */
        }else {
            for(Map oneS : services){
                oneS.put("serviceName" , oneS.get("SERVICE-NAME"));
                oneS.put("serverType", oneS.get("SERVER-TYPE"));
                if ("Running".equals(oneS.get("STATE"))){
                    oneS.put("stateImage", "/images/running_small.gif");
                }
            }
        }
        System.out.println("======== CommandUtil.listServices():  services = " + services);
        return services;
    }


    /**
     *	<p> This method returns the list of Names of the existing Templates. </p>
     *
     *	@param	serviceType Acceptable value is "JavaEE", "Database" "LoadBalancer".
     *                      If set to NULL, all service type will be returned.
     *	@return	<code>List<String></code>  Returns the list of names of the template.
     */
    public static List<String> listTemplates(String serviceType){

        List<String>list = new ArrayList();
        //String endpoint = GuiUtil.getSessionValue("REST_URL")+"/list-javaEE";
        String endpoint = "http://localhost:4848/management/domain/list-javaEE";
        Map attrs = new HashMap();
        putOptionalAttrs(attrs, "type", serviceType);
        //javaEE = getListFromREST(endpoint, attrs);

        //provide dummy data as list-javaEE endpoint is not available yet.
        if (SERVICE_TYPE_JAVAEE.equals(serviceType)){
            list.add("Native");
            list.add("GLASSFISH_SMALL");
            list.add("GLASSFISH_TINY");
        }
        if (SERVICE_TYPE_RDMBS.equals(serviceType)){
            list.add("NDBative");
            list.add("DBSql");
        }
        if (SERVICE_TYPE_LB.equals(serviceType)){
            list.add("LBBative");
            list.add("MyLB");
        }
        return list;
    }
    

     /**
     *	<p> This method returns the list of of Services that is pre-selected by Orchestrator.  It is indexed by the serviceType.
     *  If a particular serviceType doesn't exist, it means the application doesn't require such service.  Service Configuration
     *  page in deployment wizard will not show that section.
     *
     *	@param	String  filePath  This is the absolutely file path of the uploaded application.
     * 
     *	@return	<code>List<Map<String, Map>></code>  Returns the list of Map of ServiceDescriptor.    It is indexed by the serviceType. 
     *           The Map is the properties of this service that should be shown to the user when that service is selected.  It is guaranteed
     *           that the templateName of the pre-selected service will be one of the member in the list returned by listTemplates() of the same type.
     *
     */
    public static List<Map<String, Map>> getPreSelectedServices(String filePath){
        List slist = new ArrayList();
        //Need to call backend get-service-metadata API.  For now, return dummy data.

        Map eeMap = new HashMap();
        eeMap.put("templateName", "Native");
        eeMap.put("serviceType", SERVICE_TYPE_JAVAEE);
        eeMap.put("virtualizationType", "Native");
        Map aMap = new HashMap();
        aMap.put(SERVICE_TYPE_JAVAEE, eeMap);
        slist.add(aMap);

        Map dbMap = new HashMap();
        dbMap.put("templateName", "DBNative");
        dbMap.put("serviceType", SERVICE_TYPE_RDMBS);
        dbMap.put("virtualizationType", "Native");
        Map bMap = new HashMap();
        bMap.put(SERVICE_TYPE_RDMBS, dbMap);
        slist.add(dbMap);
        return slist;
    }


    /*
     * <p> This method returns list of Map, each Map represents an environment.
     */
    public static List<Map> getEnvironments(){

        List envList = new ArrayList();
        String prefix = "http://localhost:4848/management/domain/clusters/cluster/" ;
        try{
            List<String> clusterList = new ArrayList(RestUtil.getChildMap(prefix).keySet());
            if ( (clusterList != null) && (! clusterList.isEmpty())){
                //For each cluster, consider that as Environment only if it has chlid element <virtual-machine-config>
                for(String oneCluster : clusterList){
                    Map attrs = new HashMap();
                    attrs.put("whichtarget", oneCluster);
                    List<Map> instanceList = getListFromREST("http://localhost:4848/management/domain/list-instances", attrs, "instanceList");
                    if (instanceList == null || instanceList.isEmpty()){
                        continue;
                    }
                    String vmcEndpoint = prefix + oneCluster + "/virtual-machine-config/" + instanceList.get(0).get("name");
                    try{
                        Map vmc = RestUtil.getEntityAttrs(vmcEndpoint, "entity");
                        Map env = new HashMap();
                        env.put("clusterName", oneCluster);
                        env.put("instanceCount",  instanceList.size());
                        env.put("template", vmc.get("template"));
                        envList.add(env);
                    }catch( Exception ex){
                        //no VMC exist, skip this cluster.
                        continue;
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        System.out.println("================= return envList=" + envList);
        return envList;
    }





    private static void putOptionalAttrs(Map attrs, String key, String value){
        if (!GuiUtil.isEmpty(value)){
            attrs.put(key, value);
        }
    }


    private static List<Map> getListFromREST(String endpoint, Map attrs, String listKey){
        List result = null;
        try{
            Map responseMap = RestUtil.restRequest( endpoint , attrs, "GET" , null, null, false, true);
            Map extraPropertiesMap = (Map)((Map)responseMap.get("data")).get("extraProperties");
            if (extraPropertiesMap != null){
                result = (List)extraPropertiesMap.get(listKey);
            }
        }catch (Exception ex){
            GuiUtil.getLogger().severe("cannot List Services");
        }
        return result;
    }

}

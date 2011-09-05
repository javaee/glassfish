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


        services = getListFromREST(endpoint, attrs);
        if (services == null){
            services = new ArrayList();
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
        }else
        { for(Map oneS : services){
              oneS.put("serviceName" , oneS.get("SERVICE-NAME"));
              oneS.put("serverType", oneS.get("SERVER-TYPE"));
              if ("Running".equals(oneS.get("STATE"))){
                  oneS.put("stateImage", "/images/running_small.gif");
              }
          }
        }
        System.out.println("======== services = " + services);
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
    
    // TODO: provide util method to get default service and its properties.




    private static void putOptionalAttrs(Map attrs, String key, String value){
        if (!GuiUtil.isEmpty(value)){
            attrs.put(key, value);
        }
    }


    private static List<Map> getListFromREST(String endpoint, Map attrs){
        List result = null;
        try{
            Map responseMap = RestUtil.restRequest( endpoint , attrs, "GET" , null, null, false, true);
            Map extraPropertiesMap = (Map)((Map)responseMap.get("data")).get("extraProperties");
            if (extraPropertiesMap != null){
                result = (List)extraPropertiesMap.get("list");
            }
        }catch (Exception ex){
            GuiUtil.getLogger().severe("cannot List Services");
        }
        return result;
    }

}

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
    public static String SERVICE_TYPE_RDMBS = "Database";
    public static final String SERVICE_TYPE_JAVAEE = "JavaEE";
    public static final String SERVICE_TYPE_LB = "LB";

    static final String REST_URL="http://localhost:4848/management/domain";

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


        services = RestUtil.getListFromREST(endpoint, attrs, "list");
        if (services == null){
            services = new ArrayList();
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
        //javaEE = RestUtil.getListFromREST(endpoint, attrs);

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
     *	@param	filePath  This is the absolute file path of the uploaded application.
     * 
     *	@return	raw meta data as returned by web service, e.g.
     *	<pre>
     *	{
     *    characteristics = {service-type = LB},
     *    init-type = lazy,
     *    name = basic_db_paas_sample-lb,
     *    service-type = LB,
     *  },
     *  {
     *    characteristics = {
     *      os-name = Linux,
     *      service-type = JavaEE,
     *    },
     *    configurations = {
     *      max.clustersize = 4,
     *      min.clustersize = 2
     *    },
     *    init-type = lazy,
     *    name = basic-db,
     *    service-type = JavaEE,
     *  },
     *  {
     *    characteristics = {
     *      os-name = 	Windows XP,
     *      service-type = Database,
     *    },
     *    init-type = lazy,
     *    name = default-derby-db-service,
     *    service-type = Database,
     *  }
     *	</pre>
    */
    public static List<Map<String, Object>> getPreSelectedServices(String filePath) {
        Map attrs = new HashMap();
        attrs.put("archive", filePath);
        Map appData = (Map) RestUtil.restRequest(REST_URL + "/applications/_get-service-metadata", attrs, "GET", null, null, false, true).get("data");

        return (List<Map<String, Object>>) ((Map) appData.get("extraProperties")).get("list");
    }

    private static void putOptionalAttrs(Map attrs, String key, String value){
        if (!GuiUtil.isEmpty(value)){
            attrs.put(key, value);
        }
    }

}

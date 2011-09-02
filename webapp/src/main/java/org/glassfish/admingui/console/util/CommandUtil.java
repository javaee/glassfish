/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.console.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.glassfish.admingui.console.rest.RestUtil;

/**
 *
 * @author anilam
 */
public class CommandUtil {


    //appName, type and scope are all optional argument to list-services.
    //pass in null if you don't need this option in the list-services command.
    public static List<Map> listServices(String appName, String type, String scope){

        List<Map>services = null;
        //String endpoint = GuiUtil.getSessionValue("REST_URL")+"/list-services";
        String endpoint = "http://localhost:4848/management/domain/list-services";

        Map attrs = new HashMap();
        putOptionalAttrs(attrs, "appname", appName);
        putOptionalAttrs(attrs, "type", type);
        putOptionalAttrs(attrs, "scope", scope);

        try{
            Map responseMap = RestUtil.restRequest( endpoint , attrs, "GET" , null, null, false, true);
            Map extraPropertiesMap = (Map)((Map)responseMap.get("data")).get("extraProperties");
            if (extraPropertiesMap != null){
                services = (List)extraPropertiesMap.get("list");
            }
        }catch (Exception ex){
            GuiUtil.getLogger().severe("cannot List Services");
        }
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
        }
        return services;

    }

    private static void putOptionalAttrs(Map attrs, String key, String value){
            if (!GuiUtil.isEmpty(value)){
                attrs.put(key, value);
            }
    }


}

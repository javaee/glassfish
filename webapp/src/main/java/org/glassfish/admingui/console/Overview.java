/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;

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
public class Overview {

    private List<Map> services;
    private final List<Map> apps = new ArrayList<Map>();

    public List<Map> getApplications() {
        synchronized (apps) {
            if (apps.isEmpty()) {
                Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
                List<String> deployingApps = (List) sessionMap.get("_deployingApps");
                String endPoint = "http://localhost:4848/management/domain/applications/list-applications";
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
        return services;
    }

    public List<Map> getEnvironments() {
        return CommandUtil.getEnvironments();
    }
}
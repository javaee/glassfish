/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.console.beans;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.*;
import org.glassfish.admingui.console.rest.RestUtil;
import org.glassfish.admingui.console.util.CommandUtil;
import org.glassfish.admingui.console.util.DeployUtil;
import javax.faces.context.FacesContext;

@ManagedBean(name="listApplicationsBean")
@ViewScoped
public class ListApplicationsBean {
    private Map appData;
    transient private final List<Map> apps = new ArrayList<Map>();

    public ListApplicationsBean() {
//        applications = getApplications();
    }
/**
    public List<Application> getApplications() {
        List<Application> apps = new ArrayList<Application>();
        String endPoint = "http://localhost:4848/management/domain/applications/list-applications";
        Map attrs = new HashMap();
        attrs.put("target", "domain");  //specify domain to get Paas deployed app.
        appData = (Map)RestUtil.restRequest(endPoint, attrs, "GET", null, null, false, true).get("data");
        Map<String, String> props = (Map)appData.get("properties");
        for (String prop : props.keySet()) {
            apps.add(new Application(prop, true, props.get(prop)));
        }
        return apps;
    }
**/

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
                        app.put("environment", getEnvironment(appName));
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


    public String getEnvironment(String appName) {
            List<String> targets = DeployUtil.getApplicationTarget(appName, "application-ref");
            if (targets.size() > 0) return targets.get(0);
            return "";
    }

}

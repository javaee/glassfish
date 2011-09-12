/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.console.beans;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.*;
import org.glassfish.admingui.console.rest.RestUtil;
import org.glassfish.admingui.console.util.CommandUtil;

@ManagedBean(name="listApplicationsBean")
@SessionScoped
public class ListApplicationsBean {
    private Application selectedApplication = new Application();
    private List applications;
    private Map appData;

    public ListApplicationsBean() {
//        applications = getApplications();
    }

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

    //get list of services for the specified application
    

    public static class Application {
        private String name;
        private Boolean enabled;
        private String engines;

        public Application() {
        }

        public Application(String name, Boolean enabled, String engines) {

            this.name = name;
            this.enabled = enabled;
            this.engines = engines;
        }

        public String getName() {
            return name;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public String getEngines() {
            return engines;
        }

        public List<Map> getServices() {
            return CommandUtil.listServices(name, null, null);
        }

        public List<Map> instances() {
//            return DeployUtil.getApplicationTarget(name, "application-ref");
            //TODO: This should get the list of targets for this application
	    //can call DeployUtil getRefEndpoints(String name, String ref) and get the Map key.
            List<Map> allServices = new ArrayList();
            Map<String, String> sMap = new HashMap();
            sMap.put("name", name+"mycluster.mycluster-1");
            sMap.put("type", "ClusterInstance");

            allServices.add(sMap);
            Map<String, String> sMap2 = new HashMap();
            sMap2.put("name", name+"default-derby-dbs");
            sMap2.put("type", "database");
            allServices.add(sMap2);
            return allServices;
        }

        public String getEnvironment() {
            /* CommandUtil.getEnvironments() has been moved to Environment Management Bean.
             * Commenting the following code, getEnvironments() is getting ALL env. for the system
             * not for indiviual app anyway.  Need to get the target of this app.
             */
            /*
            List<Map> environments = CommandUtil.getEnvironments();
            if (environments.size() == 0) return null;
            return (String)environments.get(0).get("Name");
             *
             */
            return "";
        }

    }
}

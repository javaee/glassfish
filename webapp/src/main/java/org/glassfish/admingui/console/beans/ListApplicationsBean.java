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

@ManagedBean(name="listApplicationsBean")
@SessionScoped
public class ListApplicationsBean {
    public List applications;
    Map appData;

    public ListApplicationsBean() {
        applications = getApplications();
    }

    Application selectedApplication = new Application();

    public Application getSelectedApplication() {
        return selectedApplication;
    }

    public void setSelectedApplication(Application selectedApplication) {
        this.selectedApplication = selectedApplication;
    }

    public List<Application> getApplications() {
        List<Application> apps = new ArrayList<Application>();
        String endPoint = "http://localhost:4848/management/domain/applications/list-applications";
        appData = (Map)RestUtil.restRequest(endPoint, new HashMap(), "GET", null, null, false, true).get("data");
        Map<String, String> props = (Map)appData.get("properties");
        for (String prop : props.keySet()) {
            apps.add(new Application(prop, true, props.get(prop)));
        }
        return apps;
    }

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

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public String getEngines() {
            return engines;
        }

    }
}

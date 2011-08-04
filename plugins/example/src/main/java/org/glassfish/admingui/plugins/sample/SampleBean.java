/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins.sample;

import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author jdlee
 */
@ManagedBean
@ViewScoped
public class SampleBean {
    public List<Application> getApplications() {
        List<Application> apps = new ArrayList<Application>();
        
        apps.add(new Application("application1", true, "web"));
        apps.add(new Application("application2", false, "ejb, web"));
        
        return apps;
    }
    
    public static class Application {
        private String name;
        private Boolean enabled;
        private String engines;

        public Application(String name, Boolean enabled, String engines) {
            this.name = name;
            this.enabled = enabled;
            this.engines = engines;
        }
        
        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getEngines() {
            return engines;
        }

        public void setEngines(String engines) {
            this.engines = engines;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

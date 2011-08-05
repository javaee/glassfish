/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU 
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You 
 * may not use this file except in compliance with the License.  You can 
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL 
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
    private List<String> availableServers = new ArrayList<String>();
    private List<String> selectedServers = new ArrayList<String>();

    public SampleBean() {
        selectedServers.add("server1");
        availableServers.add("server2");
        availableServers.add("server3");
        availableServers.add("server4");
        availableServers.add("server5");
    }
    
    public List<Application> getApplications() {
        List<Application> apps = new ArrayList<Application>();
        
        apps.add(new Application("application1", true, "web"));
        apps.add(new Application("application2", false, "ejb, web"));
        
        return apps;
    }

    public List<String> getAvailableServers() {
        return availableServers;
    }

    public void setAvailableServers(List<String> availableServers) {
        this.availableServers = availableServers;
    }

    public List<String> getSelectedServers() {
        return selectedServers;
    }

    public void setSelectedServers(List<String> selectedServers) {
        this.selectedServers = selectedServers;
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

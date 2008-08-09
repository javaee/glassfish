/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package org.glassfish.web.admin.monitor.telemetry;

import org.glassfish.flashlight.statistics.*;
import org.glassfish.flashlight.statistics.factory.CounterFactory;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.*;
import org.glassfish.flashlight.client.ProbeListener;
import org.glassfish.flashlight.provider.annotations.ProbeParam;
        
import org.glassfish.flashlight.provider.annotations.*;
import javax.servlet.Servlet;
import java.util.Collection;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;

/**
 * Provides the monitoring data at the Web container level
 *
 * @author Prashanth Abbagani
 */
public class ServletStatsTelemetry{
    private Collection<ProbeClientMethodHandle> handles;
    private boolean webMonitoringEnabled;
    private String moduleName;
    private String vsName;
    
    //@Inject
    //Logger logger;
    
    public ServletStatsTelemetry(TreeNode parent, String moduleName, String vsName, 
                                    boolean webMonitoringEnabled) {
        this.moduleName = moduleName;
        this.vsName = vsName;
        activeServletsLoadedCount.setName("activeServletsLoadedCount");
        parent.addChild(activeServletsLoadedCount);
        maxServletsLoadedCount.setName("maxServletsLoadedCount");
        parent.addChild(maxServletsLoadedCount);
        totalServletsLoadedCount.setName("totalServletsLoadedCount");
        parent.addChild(totalServletsLoadedCount);
        this.webMonitoringEnabled = webMonitoringEnabled;
    }

    private Counter activeServletsLoadedCount = CounterFactory.createCount();
    private Counter maxServletsLoadedCount = CounterFactory.createCount();
    private Counter totalServletsLoadedCount = CounterFactory.createCount();

    public void enableMonitoring(boolean isEnable) {
        //loop through the handles for this node and enable/disable the listeners
        //delegate the request to the child nodes
    }
    
    public void enableMonitoringForSubElements(boolean isEnable) {
        //loop through the children and enable/disable all
    }

    @ProbeListener("web:servlet::servletInitializedEvent")
    public void servletInitializedEvent(
                    @ProbeParam("servlet") Servlet servlet,
                    @ProbeParam("appName") String appName,
                    @ProbeParam("hostName") String hostName) {
	// handle the servlet loaded probe events
        System.out.println("Servlet Loaded event received - servletName = " + 
                             servlet.getServletConfig().getServletName() + 
                             ": appName = " + appName + ": hostName = " + hostName);
        if (!isValidEvent(appName, hostName)) {
            return;
        }
        activeServletsLoadedCount.increment();
        totalServletsLoadedCount.increment();
        if (activeServletsLoadedCount.getCount() > maxServletsLoadedCount.getCount()) {
            maxServletsLoadedCount.setCount(activeServletsLoadedCount.getCount());
        }
            
    }

    @ProbeListener("web:servlet::servletDestroyedEvent")
    public void servletDestroyedEvent(
                    @ProbeParam("servlet") Servlet servlet,
                    @ProbeParam("appName") String appName,
                    @ProbeParam("hostName") String hostName) {
	// handle the servlet loaded probe events
        System.out.println("Servlet Destroyed event received - servletName = " + 
                             servlet.getServletConfig().getServletName() + 
                             ": appName = " + appName + ": hostName = " + hostName);
        if (!isValidEvent(appName, hostName)) {
            return;
        }
        activeServletsLoadedCount.decrement();
    }

    public void setProbeListenerHandles(Collection<ProbeClientMethodHandle> handles) {
        this.handles = handles;
        if (!webMonitoringEnabled){
            //disable handles
            tuneProbeListenerHandles(webMonitoringEnabled);
        }
    }

    public void enableProbeListenerHandles(boolean isEnabled) {
        if (isEnabled != webMonitoringEnabled) {
            webMonitoringEnabled = isEnabled;
            tuneProbeListenerHandles(webMonitoringEnabled);
        }
    }    
    
    private void tuneProbeListenerHandles(boolean shouldEnable) {
        //disable handles
        for (ProbeClientMethodHandle handle : handles) {
            if (shouldEnable)
                handle.enable();
            else
                handle.disable();
        }
        
    }

    private boolean isValidEvent(String mName, String hostName) {
        //Temp fix, get the appname from the context root
        if ((moduleName == null) || (vsName == null)) {
            return true;
        }
        String appName = WebMonitorStartup.getAppName(mName);
        if ((moduleName.equals(appName)) && (vsName.equals(hostName))) {
            return true;
        }
        
        return false;
    }
}

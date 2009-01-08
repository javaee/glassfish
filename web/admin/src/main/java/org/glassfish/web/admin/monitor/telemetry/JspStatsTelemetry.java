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
import java.util.logging.Logger;


/**
 * Provides the monitoring data at the Web container level
 *
 * @author Prashanth Abbagani
 */
public class JspStatsTelemetry{
    private Collection<ProbeClientMethodHandle> handles;
    private boolean webMonitoringEnabled;
    private String moduleName;
    private String vsName;
    private Logger logger;
    private boolean isEnabled = true;
    private TreeNode jspNode = null;
    
    public JspStatsTelemetry(TreeNode parent, String moduleName, String vsName,
           boolean webMonitoringEnabled, Logger logger) {
        jspNode = parent;
        this.logger = logger;
        this.moduleName = moduleName;
        this.vsName = vsName;
        activeJspsLoadedCount.setName("activejspsloadedcount-count");
        parent.addChild(activeJspsLoadedCount);
        maxJspsLoadedCount.setName("maxjspsloadedcount-count");
        parent.addChild(maxJspsLoadedCount);
        totalJspsLoadedCount.setName("totaljspsloadedcount-count");
        parent.addChild(totalJspsLoadedCount);
        this.webMonitoringEnabled = webMonitoringEnabled;
    }

    private Counter activeJspsLoadedCount = CounterFactory.createCount();
    private Counter maxJspsLoadedCount = CounterFactory.createCount();
    private Counter totalJspsLoadedCount = CounterFactory.createCount();

    public void enableMonitoring(boolean flag) {
        //loop through the handles for this node and enable/disable the listeners
        if (isEnabled != flag) {
            for (ProbeClientMethodHandle handle : handles) {
                if (flag == true) 
                    handle.enable();
                else
                    handle.disable();
            }
            jspNode.setEnabled(flag);
            if (isEnabled) {
                //It means you are turning from ON to OFF, reset the statistics
                resetStats();
            }
            isEnabled = flag;
        }
    }
    
    @ProbeListener("web:jsp::jspLoadedEvent")
    public void jspLoadedEvent(
        @ProbeParam("jsp") Servlet jsp,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {
	// handle the servlet loaded probe events
        logger.finest("JSP Loaded event received - jspName = " + 
                             jsp.getServletConfig().getServletName() + 
                             ": appName = " + appName + ": hostName = " + hostName);
        if (!isValidEvent(appName, hostName)) {
            return;
        }
        activeJspsLoadedCount.increment();
        totalJspsLoadedCount.increment();
        if (activeJspsLoadedCount.getCount() > maxJspsLoadedCount.getCount()) {
            maxJspsLoadedCount.setCount(activeJspsLoadedCount.getCount());
        }
            
    }
/*
    @ProbeListener("web:jsp::jspDestroyedEvent")
    public void jspDestroyedEvent(
                    @ProbeParam("servlet") Servlet jsp,
                    @ProbeParam("appName") String appName,
                    @ProbeParam("hostName") String hostName) {
	// handle the jsp destroyed probe events
        logger.finest("JSP Destroyed event received - jspName = " + 
                             jsp.getServletConfig().getServletName() + 
                             ": appName = " + appName + ": hostName = " + hostName);
        activeJspsLoadedCount.decrement();
    }
*/
    public void setProbeListenerHandles(Collection<ProbeClientMethodHandle> handles) {
        this.handles = handles;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getModuleName() {
        return moduleName;
    }
    
    public String getVSName() {
        return vsName;
    }
    
    private boolean isValidEvent(String mName, String hostName) {
        //Temp fix, get the appname from the context root
        if ((moduleName == null) || (vsName == null)) {
            return true;
        }
        if ((moduleName.equals(mName)) && (vsName.equals(hostName))) {
            return true;
        }
        
        return false;
    }

    private void resetStats() {
        activeJspsLoadedCount.setReset(true);
        maxJspsLoadedCount.setReset(true);
        totalJspsLoadedCount.setReset(true);
    }
}

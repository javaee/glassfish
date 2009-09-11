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
package org.glassfish.web.admin.monitor;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.probe.provider.annotations.*;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 * Provides the monitoring data at the Web container level
 *
 * @author Prashanth Abbagani
 */
@AMXMetadata(type="servlet-mon", group="monitoring")
@ManagedObject
@Description("Web Container Servlet Statistics")
public class ServletStatsProvider {
    private String moduleName;
    private String vsName;
    private Logger logger;
    
    public ServletStatsProvider(String moduleName, String vsName,
            Logger logger) {
        this.logger = logger;
        this.moduleName = moduleName;
        this.vsName = vsName;
    }

    private CountStatisticImpl activeServletsLoadedCount = new CountStatisticImpl("ActiveServletsLoaded", "count", "Number of currently loaded servlets");
    private CountStatisticImpl maxServletsLoadedCount = new CountStatisticImpl("MaxServletsLoaded", "count", "Maximum number of servlets loaded which were active");
    private CountStatisticImpl totalServletsLoadedCount = new CountStatisticImpl("TotalServletsLoaded", "count", "Cumulative number of servlets that have been loaded into the web module");

    @ManagedAttribute(id="activeservletsloadedcount")
    @Description( "Number of currently loaded servlets" )
    public CountStatistic getActiveServletsLoaded() {
        return activeServletsLoadedCount;
    }

    @ManagedAttribute(id="maxservletsloadedcount")
    @Description( "Maximum number of servlets loaded which were active" )
    public CountStatistic getMaxServletsLoaded() {
        return maxServletsLoadedCount;
    }

    @ManagedAttribute(id="totalservletsloadedcount")
    @Description( "Cumulative number of servlets that have been loaded into the web module" )
    public CountStatistic getTotalServletsLoaded() {
        return totalServletsLoadedCount;
    }
    
    @ProbeListener("glassfish:web:servlet:servletInitializedEvent")
    public void servletInitializedEvent(
                    @ProbeParam("servletName") String servletName,
                    @ProbeParam("appName") String appName,
                    @ProbeParam("hostName") String hostName) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Servlet Loaded event received - " +
                          "servletName = " + servletName +
                          ": appName = " + appName + ": hostName = " +
                          hostName);
        }
        if (!isValidEvent(appName, hostName)) {
            return;
        }
        activeServletsLoadedCount.increment();
        totalServletsLoadedCount.increment();
        if (activeServletsLoadedCount.getCount() > maxServletsLoadedCount.getCount()) {
            maxServletsLoadedCount.setCount(activeServletsLoadedCount.getCount());
        }
            
    }

    @ProbeListener("glassfish:web:servlet:servletDestroyedEvent")
    public void servletDestroyedEvent(
                    @ProbeParam("servletName") String servletName,
                    @ProbeParam("appName") String appName,
                    @ProbeParam("hostName") String hostName) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Servlet Destroyed event received - " +
                          "servletName = " + servletName +
                          ": appName = " + appName + ": hostName = " +
                          hostName);
        }
        if (!isValidEvent(appName, hostName)) {
            return;
        }
        activeServletsLoadedCount.decrement();
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

    public String getModuleName() {
        return moduleName;
    }
    
    public String getVSName() {
        return vsName;
    }
    
    private void resetStats() {
        activeServletsLoadedCount.setCount(0);
        maxServletsLoadedCount.setCount(0);
        totalServletsLoadedCount.setCount(0);
    }
}

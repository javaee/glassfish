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

import java.util.logging.Logger;
import javax.servlet.Servlet;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.RangeStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.RangeStatisticImpl;
import org.glassfish.external.statistics.impl.StatisticImpl;
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
@AMXMetadata(type="jsp-mon", group="monitoring")
@ManagedObject
@Description("Web Container JSP Statistics")
public class JspStatsProvider{

    private static final String ACTIVE_JSPS_LOADED_DESCRIPTION =
        "Number of active JSP pages";
    private static final String TOTAL_JSPS_LOADED_DESCRIPTION =
        "Total number of JSP pages ever loaded";

    private String moduleName;
    private String vsName; 
    private Logger logger;
    private RangeStatisticImpl activeJspsLoadedCount;
    private CountStatisticImpl totalJspsLoadedCount;
    
    public JspStatsProvider(String moduleName, String vsName, Logger logger) {
        this.moduleName = moduleName;
        this.vsName = vsName;
        long curTime = System.currentTimeMillis();
        activeJspsLoadedCount = new RangeStatisticImpl(
            0L, 0L, 0L, "ActiveJspsLoaded", StatisticImpl.UNIT_COUNT,
            ACTIVE_JSPS_LOADED_DESCRIPTION, curTime, curTime);
        totalJspsLoadedCount = new CountStatisticImpl(
            "TotalJspsLoaded", StatisticImpl.UNIT_COUNT,
            TOTAL_JSPS_LOADED_DESCRIPTION);
    }

    @ManagedAttribute(id="activejspsloadedcount")
    @Description(ACTIVE_JSPS_LOADED_DESCRIPTION)
    public RangeStatistic getActiveJspsLoaded() {
        return activeJspsLoadedCount;
    }

    @ManagedAttribute(id="totaljspsloadedcount")
    @Description(TOTAL_JSPS_LOADED_DESCRIPTION)
    public CountStatistic getTotalJspsLoaded() {
        return totalJspsLoadedCount;
    }
    
    @ProbeListener("glassfish:web:jsp:jspLoadedEvent")
    public void jspLoadedEvent(
            @ProbeParam("jsp") Servlet jsp,
            @ProbeParam("appName") String appName,
            @ProbeParam("hostName") String hostName) {
        if (isValidEvent(appName, hostName)) {
            synchronized (activeJspsLoadedCount) {
                activeJspsLoadedCount.setCurrent(
                    activeJspsLoadedCount.getCurrent() + 1);
                if (activeJspsLoadedCount.getCurrent() > 
                        activeJspsLoadedCount.getHighWaterMark()) {
                    activeJspsLoadedCount.setHighWaterMark(
                        activeJspsLoadedCount.getCurrent());
                }
            }
            totalJspsLoadedCount.increment();
        }
    }

    @ProbeListener("glassfish:web:jsp:jspDestroyedEvent")
    public void jspDestroyedEvent(
            @ProbeParam("jsp") Servlet jsp,
            @ProbeParam("appName") String appName,
            @ProbeParam("hostName") String hostName) {
        if (isValidEvent(appName, hostName)) {
            synchronized (activeJspsLoadedCount) {
                activeJspsLoadedCount.setCurrent(
                    activeJspsLoadedCount.getCurrent() - 1);
            }
        }
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
        activeJspsLoadedCount.reset();
        totalJspsLoadedCount.setCount(0);
    }
}

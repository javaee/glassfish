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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;

import org.glassfish.flashlight.statistics.*;
import org.glassfish.flashlight.statistics.factory.CounterFactory;
import org.glassfish.flashlight.statistics.factory.TimeStatsFactory;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.*;
import org.glassfish.flashlight.client.ProbeListener;
import org.glassfish.flashlight.provider.annotations.ProbeParam;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import com.sun.enterprise.config.serverbeans.*;
import java.util.HashMap;

/**
 * Provides the monitoring data at the Web container level
 *
 * @author Prashanth Abbagani
 */
public class HttpServiceRequestTelemetry implements PostConstruct {
    //Provides the longest response time for a request - not a cumulative value, 
    //but the largest response time from among the response times.
    //private Counter maxTime = CounterFactory.createCount();
    //Provides cumulative value of the times taken to process each request. 
    //The processing time is the average of request processing times over the request count.
    //private Counter processingTime = CounterFactory.createCount();
    //Provides cumulative number of the requests processed so far.
    //private Counter requestCount = CounterFactory.createCount();
    //Provides the cumulative value of the error count. The error count represents 
    //the number of cases where the response code was greater than or equal to 400.
    private Counter errorCount = CounterFactory.createCount();
    private Counter error200Count = CounterFactory.createCount();
    private Counter error2xxCount = CounterFactory.createCount();
    private Counter error302Count = CounterFactory.createCount();
    private Counter error304Count = CounterFactory.createCount();
    private Counter error3xxCount = CounterFactory.createCount();
    private Counter error400Count = CounterFactory.createCount();
    private Counter error401Count = CounterFactory.createCount();
    private Counter error403Count = CounterFactory.createCount();
    private Counter error404Count = CounterFactory.createCount();
    private Counter error4xxCount = CounterFactory.createCount();
    private Counter error503Count = CounterFactory.createCount();
    private Counter error5xxCount = CounterFactory.createCount();
    private Counter errorOtherCount = CounterFactory.createCount();
    private TreeNode webRequestNode = null;
    private TimeStats requestProcessTime = TimeStatsFactory.createTimeStatsMilli();
    private Collection<ProbeClientMethodHandle> handles;
    private Logger logger;
    private boolean isEnabled = true;
    private HttpService httpService = null;
    private String virtualServerName = null;
    private HashMap<String, Counter> errorMap = new HashMap<String, Counter>();
    
    @Inject
    private static Domain domain;

    public HttpServiceRequestTelemetry(TreeNode parent, String vsName, Logger logger) {
        try {
            this.logger = logger;
            this.virtualServerName = vsName;
            webRequestNode = parent;
            //add maxTime attribute    
            Method m1 = requestProcessTime.getClass().getMethod("getMaximumTime", (Class[]) null);
            TreeNode maxTime = TreeNodeFactory.createMethodInvoker("maxTime", requestProcessTime, "request", m1);
            parent.addChild(maxTime);
            //add requestCount
            requestProcessTime.getCount();
            Method m2 = requestProcessTime.getClass().getMethod("getCount", (Class[]) null);
            TreeNode requestCount = TreeNodeFactory.createMethodInvoker("requestCount", requestProcessTime, "request", m2);
            parent.addChild(requestCount);
            //add processTime
            Method m3 = requestProcessTime.getClass().getMethod("getTime", (Class[]) null);
            TreeNode processingTime = TreeNodeFactory.createMethodInvoker("processingTime", requestProcessTime, "request", m3);
            parent.addChild(processingTime);
            //add errorCount
            errorCount.setName("errorCount");
            parent.addChild(errorCount);
            //add errorXXXCount varialbes
            error200Count.setName("count200-count");
            parent.addChild(error200Count);
            error2xxCount.setName("count2xx-count");
            parent.addChild(error2xxCount);
            error302Count.setName("count302-count");
            parent.addChild(error302Count);
            error304Count.setName("count304-count");
            parent.addChild(error304Count);
            error3xxCount.setName("count3xx-count");
            parent.addChild(error3xxCount);
            error400Count.setName("count400-count");
            parent.addChild(error400Count);
            error401Count.setName("count401-count");
            parent.addChild(error401Count);
            error403Count.setName("count403-count");
            parent.addChild(error403Count);
            error404Count.setName("count404-count");
            parent.addChild(error404Count);
            error4xxCount.setName("count4xx-count");
            parent.addChild(error4xxCount);
            error503Count.setName("count503-count");
            parent.addChild(error503Count);
            error5xxCount.setName("count5xx-count");
            parent.addChild(error5xxCount);
            errorOtherCount.setName("countother-count");
            parent.addChild(errorOtherCount);
            errorMap.put("200", error200Count);
            errorMap.put("2", error2xxCount);
            errorMap.put("302", error302Count);
            errorMap.put("304", error304Count);
            errorMap.put("3", error3xxCount);
            errorMap.put("400", error400Count);
            errorMap.put("401", error401Count);
            errorMap.put("403", error403Count);
            errorMap.put("404", error404Count);
            errorMap.put("4", error4xxCount);
            errorMap.put("503", error503Count);
            errorMap.put("5", error5xxCount);
            errorMap.put("9", errorOtherCount);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(HttpServiceRequestTelemetry.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(HttpServiceRequestTelemetry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void postConstruct() {
    }

    @ProbeListener("web:request::requestStartEvent")
    public void requestStartEvent(
        @ProbeParam("request") HttpServletRequest request,
        @ProbeParam("response") HttpServletResponse response,
        @ProbeParam("hostName") String hostName) {
        String vsName = HttpServiceTelemetryBootstrap.getVirtualServer(
            hostName, String.valueOf(request.getServerPort()));
        if ((vsName != null) && (vsName.equals(virtualServerName))) {
            requestProcessTime.entry();
            logger.finest("[TM]requestStartEvent received - virtual-server = " +
                                request.getServerName() + " : port = " +
                                request.getServerPort());
        }
    }

    @ProbeListener("web:request::requestEndEvent")
    public void requestEndEvent(
        @ProbeParam("request") HttpServletRequest request,
        @ProbeParam("response") HttpServletResponse response,
        @ProbeParam("statusCode") int statusCode) {

        String vsName = HttpServiceTelemetryBootstrap.getVirtualServer(request.getServerName(),
                                                String.valueOf(request.getServerPort()));
        if ((vsName != null) && (vsName.equals(virtualServerName))) {
            requestProcessTime.exit();
            incrementStatsCounter(statusCode);
            logger.finest("[TM]requestEndEvent received - virtual-server = " +
                                request.getServerName() + ": application = " +
                                request.getContextPath() + " : servlet = " +
                                request.getServletPath() + " :Response code = " +
                                statusCode + " :Response time = " +
                                requestProcessTime.getTime());
        }
    }

    
    public long getProcessTime() {
        return requestProcessTime.getTotalTime()/requestProcessTime.getCount();
    }

    
    public void setProbeListenerHandles(Collection<ProbeClientMethodHandle> handles) {
        this.handles = handles;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void enableMonitoring(boolean flag) {
        //loop through the handles for this node and enable/disable the listeners
        //delegate the request to the child nodes
        if (isEnabled != flag) {
            for (ProbeClientMethodHandle handle : handles) {
                if (flag == true) 
                    handle.enable();
                else
                    handle.disable();
            }
            webRequestNode.setEnabled(flag);
            isEnabled = flag;
        }
    }

    private void incrementStatsCounter(int statusCode) {
        Counter errorCounter = errorMap.get(Integer.toString(statusCode));
        if (errorCounter == null) 
            errorCounter = errorMap.get(Integer.toString(Math.round(statusCode/100)));
        if (errorCounter == null)
            errorCounter = errorMap.get("9");
        errorCounter.increment();
        if (statusCode > 400)
            errorCount.increment();
    }
}

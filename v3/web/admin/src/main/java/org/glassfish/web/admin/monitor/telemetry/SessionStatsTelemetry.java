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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.flashlight.statistics.*;
import org.glassfish.flashlight.statistics.factory.CounterFactory;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.*;
import org.glassfish.flashlight.client.ProbeListener;
import org.glassfish.flashlight.provider.annotations.ProbeParam;
        
import org.glassfish.flashlight.provider.annotations.*;
import javax.servlet.http.HttpSession;
import com.sun.logging.LogDomains;
import java.util.Collection;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;

/**
 * Provides the monitoring data at the Web container level
 *
 * @author Prashanth Abbagani
 */
public class SessionStatsTelemetry{
    private String moduleName;
    private String vsName;
    private TreeNode sessionTM;
    private TreeNode sessionChildren[];

    private Logger logger;    
    //Logger logger = LogDomains.getLogger(this.getClass().getName());
    private Collection<ProbeClientMethodHandle> handles;
    private boolean webMonitoringEnabled;
    
    public SessionStatsTelemetry(TreeNode parent, String moduleName, String vsName, 
                                    boolean webMonitoringEnabled, Logger logger) {
        this.logger = logger;
        this.moduleName = moduleName;
        this.vsName = vsName;
        this.webMonitoringEnabled = webMonitoringEnabled;
        activeSessionsCurrent.setName("activeSessionsCurrent");
        parent.addChild(activeSessionsCurrent);
        sessionsTotal.setName("sessionsTotal");
        parent.addChild(sessionsTotal);
        activeSessionsHigh.setName("activeSessionsHigh");
        parent.addChild(activeSessionsHigh);
        rejectedSessionsTotal.setName("rejectedSessionsTotal");
        parent.addChild(rejectedSessionsTotal);
        expiredSessionsTotal.setName("expiredSessionsTotal");
        parent.addChild(expiredSessionsTotal);
        persistedSessionsTotal.setName("persistedSessionsTotal");
        parent.addChild(persistedSessionsTotal);
        passivatedSessionsTotal.setName("passivatedSessionsTotal");
        parent.addChild(passivatedSessionsTotal);
        activatedSessionsTotal.setName("activatedSessionsTotal");
        parent.addChild(activatedSessionsTotal);
     }

    private Counter activeSessionsCurrent = CounterFactory.createCount();
    private Counter activeSessionsHigh = CounterFactory.createCount();
    private Counter sessionsTotal = CounterFactory.createCount();
    private Counter expiredSessionsTotal = CounterFactory.createCount();
    private Counter rejectedSessionsTotal = CounterFactory.createCount();
    private Counter persistedSessionsTotal = CounterFactory.createCount();
    private Counter passivatedSessionsTotal = CounterFactory.createCount();
    private Counter activatedSessionsTotal = CounterFactory.createCount();
    

    public void enableMonitoring(boolean isEnable) {
        //loop through the handles for this node and enable/disable the listeners
        //delegate the request to the child nodes
    }
    
    public void enableMonitoringForSubElements(boolean isEnable) {
        //loop through the children and enable/disable all
    }
    
    @ProbeListener("web:session::sessionCreatedEvent")
    public void sessionCreatedEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){

        logger.finest("[TM]sessionCreatedEvent received - session = " + 
                            session.toString() + ": appname = " + appName + 
                            ": hostName = " + hostName);
        if (!isValidEvent(appName, hostName)) {
            return;
        }
        sessionsTotal.increment();
        incrementActiveSessionsCurrent();
        //logger.log(Level.FINE, "[Logger] session created event");
    }

    @ProbeListener("web:session::sessionDestroyedEvent")
    public void sessionDestroyedEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){
        
        logger.finest("[TM]sessionDestroyedEvent received - session = " + 
                            session.toString() + ": appname = " + appName + 
                            ": hostName = " + hostName);
        if (!isValidEvent(appName, hostName)) {
            return;
        }
        activeSessionsCurrent.decrement();        
    }

    @ProbeListener("web:session::sessionRejectedEvent")
    public void sessionRejectedEvent(
        @ProbeParam("maxThresholdSize") int maxSessions,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){
        
        logger.finest("[TM]sessionRejectedEvent received - max sessions = " + 
                            maxSessions + ": appname = " + appName + 
                            ": hostName = " + hostName);
        if (!isValidEvent(appName, hostName)) {
            return;
        }
        activeSessionsCurrent.decrement();
        rejectedSessionsTotal.increment();
    }

    @ProbeListener("web:session::sessionExpiredEvent")
    public void sessionExpiredEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){
        
        logger.finest("[TM]sessionExpiredEvent received - session = " + 
                            session.toString() + ": appname = " + appName + 
                            ": hostName = " + hostName);
        if (!isValidEvent(appName, hostName)) {
            return;
        }
        activeSessionsCurrent.decrement();
        expiredSessionsTotal.increment();
    }

    @ProbeListener("web:session::sessionPersistedStartEvent")
    public void sessionPersistedStartEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){
        
        logger.finest("[TM]sessionPersistedStartEvent received - session = " + 
                            session.toString() + ": appname = " + appName + 
                            ": hostName = " + hostName);
        if (!isValidEvent(appName, hostName)) {
            return;
        }
    }

    @ProbeListener("web:session::sessionPersistedEndEvent")
    public void sessionPersistedEndEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){
        
        logger.finest("[TM]sessionPersistedEndEvent received - session = " + 
                            session.toString() + ": appname = " + appName + 
                            ": hostName = " + hostName);
        if (!isValidEvent(appName, hostName)) {
            return;
        }
        activeSessionsCurrent.decrement();
        persistedSessionsTotal.increment();
    }

    @ProbeListener("web:session::sessionActivatedStartEvent")
    public void sessionActivatedStartEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){
        
        logger.finest("[TM]sessionActivatedStartEvent received - session = " + 
                            session.toString() + ": appname = " + appName + 
                            ": hostName = " + hostName);
        if (!isValidEvent(appName, hostName)) {
            return;
        }
    }

    @ProbeListener("web:session::sessionActivatedEndEvent")
    public void sessionActivatedEndEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){
        
        logger.finest("[TM]sessionActivatedEndEvent received - session = " + 
                            session.toString() + ": appname = " + appName + 
                            ": hostName = " + hostName);
        if (!isValidEvent(appName, hostName)) {
            return;
        }
        incrementActiveSessionsCurrent();
        activatedSessionsTotal.increment();
    }

    @ProbeListener("web:session::sessionPassivatedStartEvent")
    public void sessionPassivatedStartEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){
        
        logger.finest("[TM]sessionPassivatedStartEvent  received - session = " + 
                            session.toString() + ": appname = " + appName + 
                            ": hostName = " + hostName);
        if (!isValidEvent(appName, hostName)) {
            return;
        }
    }

    @ProbeListener("web:session::sessionPassivatedEndEvent")
    public void sessionPassivatedEndEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){
        
        logger.finest("[TM]sessionPassivatedEndEvent received - session = " + 
                            session.toString() + ": appname = " + appName + 
                            ": hostName = " + hostName);
        if (!isValidEvent(appName, hostName)) {
            return;
        }
        activeSessionsCurrent.decrement();
        passivatedSessionsTotal.increment();
    }
    
    private void incrementActiveSessionsCurrent() {
        activeSessionsCurrent.increment();
        if (activeSessionsCurrent.getCount() > activeSessionsHigh.getCount()){
            activeSessionsHigh.setCount(activeSessionsCurrent.getCount());
        }
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

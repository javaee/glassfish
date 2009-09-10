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
import javax.servlet.http.HttpSession;
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
@AMXMetadata(type="session-mon", group="monitoring")
@ManagedObject
@Description( "Web Container Session Statistics" )
public class SessionStatsProvider{
    private String moduleName;
    private String vsName;
    private Logger logger;    
    
    public SessionStatsProvider(String moduleName, String vsName, Logger logger) {
        this.logger = logger;
        this.moduleName = moduleName;
        this.vsName = vsName;
     }

    private CountStatisticImpl activeSessionsCurrent = new CountStatisticImpl("ActiveSessionsCurrent", "count", "Number of currently active sessions");
    private CountStatisticImpl activeSessionsHigh = new CountStatisticImpl("ActiveSessionsHigh", "count", "Maximum number of concurrently active sessions");
    private CountStatisticImpl sessionsTotal = new CountStatisticImpl("SessionsTotal", "count", "Total number of sessions created");
    private CountStatisticImpl expiredSessionsTotal = new CountStatisticImpl("ExpiredSessionsTotal", "count", "Total number of expired sessions");
    private CountStatisticImpl rejectedSessionsTotal = new CountStatisticImpl("RejectedSessionsTotal", "count", "Total number of rejected sessions");
    private CountStatisticImpl persistedSessionsTotal = new CountStatisticImpl("PersistedSessionsTotal", "count", "Total number of persisted sessions");
    private CountStatisticImpl passivatedSessionsTotal = new CountStatisticImpl("PassivatedSessionsTotal", "count", "Total number of passivated sessions");
    private CountStatisticImpl activatedSessionsTotal = new CountStatisticImpl("ActivatedSessionsTotal", "count", "Total number of activated sessions");
    
    @ManagedAttribute(id="activesessionscurrent")
    @Description("Number of currently active sessions")
    public CountStatistic getActiveSessionsCurrent() {
        return activeSessionsCurrent;
    }

    @ManagedAttribute(id="sessionstotal")
    @Description("Total number of sessions created")
    public CountStatistic getSessionsTotal() {
        return sessionsTotal;
    }

    @ManagedAttribute(id="activesessionshigh")
    @Description("Maximum number of concurrently active sessions")
    public CountStatistic getActiveSessionsHigh() {
        return activeSessionsHigh;
    }

    @ManagedAttribute(id="rejectedsessionstotal")
    @Description("Total number of rejected sessions")
    public CountStatistic getRejectedSessionsTotal() {
        return rejectedSessionsTotal;
    }

    @ManagedAttribute(id="expiredsessionstotal")
    @Description("Total number of expired sessions")
    public CountStatistic getExpiredSessionsTotal() {
        return expiredSessionsTotal;
    }

    @ManagedAttribute(id="persistedsessionstotal")
    @Description("Total number of persisted sessions")
    public CountStatistic getPersistedSessionsTotal() {
        return persistedSessionsTotal;
    }

    @ManagedAttribute(id="passivatedsessionstotal")
    @Description("Total number of passivated sessions")
    public CountStatistic getPassivatedSessionsTotal() {
        return passivatedSessionsTotal;
    }

    @ManagedAttribute(id="activatedsessionstotal")
    @Description("Total number of activated sessions")
    public CountStatistic getActivatedSessionsTotal() {
        return activatedSessionsTotal;
    }
    
    @ProbeListener("glassfish:web:session:sessionCreatedEvent")
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
        incrementActiveSessionsCurrent();
        //logger.log(Level.FINE, "[Logger] session created event");
    }

    @ProbeListener("glassfish:web:session:sessionDestroyedEvent")
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

    @ProbeListener("glassfish:web:session:sessionRejectedEvent")
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
        //activeSessionsCurrent.decrement();
        sessionsTotal.increment(); //????
        rejectedSessionsTotal.increment();
    }

    @ProbeListener("glassfish:web:session:sessionExpiredEvent")
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
        expiredSessionsTotal.increment();
    }

    @ProbeListener("glassfish:web:session:sessionPersistedStartEvent")
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

    @ProbeListener("glassfish:web:session:sessionPersistedEndEvent")
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
        persistedSessionsTotal.increment();
    }

    @ProbeListener("glassfish:web:session:sessionActivatedStartEvent")
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

    @ProbeListener("glassfish:web:session:sessionActivatedEndEvent")
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

    @ProbeListener("glassfish:web:session:sessionPassivatedStartEvent")
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

    @ProbeListener("glassfish:web:session:sessionPassivatedEndEvent")
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
        passivatedSessionsTotal.increment();
    }
    
    public String getModuleName() {
        return moduleName;
    }
    
    public String getVSName() {
        return vsName;
    }
    
    private void incrementActiveSessionsCurrent() {
        activeSessionsCurrent.increment();
        if (activeSessionsCurrent.getCount() > activeSessionsHigh.getCount()){
            activeSessionsHigh.setCount(activeSessionsCurrent.getCount());
        }
        sessionsTotal.increment();
    }
    
    private boolean isValidEvent(String mName, String hostName) {
        //Temp fix, get the appname from the context root
        if ((moduleName == null) || (vsName == null)) {
            return true;
        }
        if (moduleName.equals(mName) && vsName.equals(hostName))
            return true;
        return false;
    }

    private void resetStats() {
        activeSessionsCurrent.setCount(0);
        activeSessionsHigh.setCount(0);
        sessionsTotal.setCount(0);
        expiredSessionsTotal.setCount(0);
        rejectedSessionsTotal.setCount(0);
        persistedSessionsTotal.setCount(0);
        passivatedSessionsTotal.setCount(0);
        activatedSessionsTotal.setCount(0);
    }
}

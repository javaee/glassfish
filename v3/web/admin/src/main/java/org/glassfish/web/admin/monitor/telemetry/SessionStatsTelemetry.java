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
import javax.servlet.http.HttpSession;

/**
 * Provides the monitoring data at the Web container level
 *
 * @author Prashanth Abbagani
 */
public class SessionStatsTelemetry{
    private TreeNode sessionTM;
    private TreeNode sessionChildren[];

    public SessionStatsTelemetry() {
        sessionTM = TreeNodeFactory.createTreeNode("session", this, "web");
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
        @ProbeParam("appName") String appName){
        
        sessionsTotal.increment();
        incrementActiveSessionsCurrent();
        System.out.println("[TM]sessionCreatedEvent received - session = " + 
                            session.toString() + ": appname = " + appName);
    }

    @ProbeListener("web:session::sessionDestroyedEvent")
    public void sessionDestroyedEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName) {
        
        activeSessionsCurrent.decrement();        
        System.out.println("[TM]sessionDestroyedEvent received - session = " + 
                            session.toString() + ": appname = " + appName);
    }

    @ProbeListener("web:session::sessionRejectedEvent")
    public void sessionRejectedEvent(
        @ProbeParam("maxThresholdSize") int maxSessions,
        @ProbeParam("appName") String appName){
        
        activeSessionsCurrent.decrement();
        rejectedSessionsTotal.increment();
        System.out.println("[TM]sessionRejectedEvent received - max sessions = " + 
                            maxSessions + ": appname = " + appName);
    }

    @ProbeListener("web:session::sessionExpiredEvent")
    public void sessionExpiredEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName ){
        
        activeSessionsCurrent.decrement();
        expiredSessionsTotal.increment();
        System.out.println("[TM]sessionExpiredEvent received - session = " + 
                            session.toString() + ": appname = " + appName);
    }

    @ProbeListener("web:session::sessionPersistedStartEvent")
    public void sessionPersistedStartEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName ) {
        
        System.out.println("[TM]sessionPersistedStartEvent received - session = " + 
                            session.toString() + ": appname = " + appName);
    }

    @ProbeListener("web:session::sessionPersistedEndEvent")
    public void sessionPersistedEndEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName) {
        
        activeSessionsCurrent.decrement();
        persistedSessionsTotal.increment();
        System.out.println("[TM]sessionPersistedEndEvent received - session = " + 
                            session.toString() + ": appname = " + appName);
    }

    @ProbeListener("web:session::sessionActivatedStartEvent")
    public void sessionActivatedStartEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName){
        
        System.out.println("[TM]sessionActivatedStartEvent received - session = " + 
                            session.toString() + ": appname = " + appName);
    }

    @ProbeListener("web:session::sessionActivatedEndEvent")
    public void sessionActivatedEndEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName){
        
        incrementActiveSessionsCurrent();
        activatedSessionsTotal.increment();
        System.out.println("[TM]sessionActivatedEndEvent received - session = " + 
                            session.toString() + ": appname = " + appName);
    }

    @ProbeListener("web:session::sessionPassivatedStartEvent")
    public void sessionPassivatedStartEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName) {
        
        System.out.println("[TM]sessionPassivatedStartEvent  received - session = " + 
                            session.toString() + ": appname = " + appName);
    }

    @ProbeListener("web:session::sessionPassivatedEndEvent")
    public void sessionPassivatedEndEvent(
        @ProbeParam("session") HttpSession session,
        @ProbeParam("appName") String appName) {
        
        activeSessionsCurrent.decrement();
        passivatedSessionsTotal.increment();
        System.out.println("[TM]sessionPassivatedEndEvent received - session = " + 
                            session.toString() + ": appname = " + appName);
    }
    
    private void incrementActiveSessionsCurrent() {
        activeSessionsCurrent.increment();
        if (activeSessionsCurrent.getCount() > activeSessionsHigh.getCount()){
            activeSessionsHigh.setCount(activeSessionsCurrent.getCount());
        }
    }
}

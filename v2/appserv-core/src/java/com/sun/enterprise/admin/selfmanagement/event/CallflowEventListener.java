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

package com.sun.enterprise.admin.selfmanagement.event;

import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.admin.monitor.callflow.EntityManagerMethod;
import com.sun.enterprise.admin.monitor.callflow.EntityManagerQueryMethod;
import com.sun.enterprise.admin.monitor.callflow.RequestType;
import com.sun.enterprise.admin.monitor.callflow.ComponentType;
import com.sun.enterprise.Switch;
import com.sun.enterprise.admin.monitor.callflow.Listener;

public class CallflowEventListener implements Listener {
    
    private  static TraceEventImpl traceImpl = null;
    private  static CallflowEventListener _instance = new CallflowEventListener();
    static CallflowEventListener getInstance() {
        return _instance;
    }
    
    // To give the call backs to the listener
    static synchronized void setTraceImpl(TraceEventImpl impl) {
        traceImpl = impl;
        // Switch.getSwitch().getCallFlowAgent().enableForNotifications(true);
    }
  
    static synchronized void register() {
        Switch.getSwitch().getCallFlowAgent().registerListener(getInstance());
    }

    static synchronized void unregister() {
        Switch.getSwitch().getCallFlowAgent().unregisterListener(getInstance());
    }
    
    public void requestStart(String requestId, 
                                    RequestType requestType,
                                    String callerIPAddress, String remoteUser) {
        traceImpl.requestStart(requestId, requestType, callerIPAddress,
                               System.nanoTime(), Thread.currentThread().toString());
    }

    public void requestEnd(String requestId) {
        traceImpl.requestEnd(requestId, System.nanoTime(), 
                                        Thread.currentThread().toString());
    }

    public void ejbMethodStart(String requestId, 
                       String methodName, String applicationName,
                       String moduleName, String componentName,
                       ComponentType componentType, String securityId,
                       String transactionId) {
        traceImpl.ejbMethodStart(requestId, methodName, componentType.toString(), applicationName,
                                 moduleName, componentName, transactionId, securityId,
                                 System.nanoTime(),Thread.currentThread().toString());
    }

    public void ejbMethodEnd(String requestId, Throwable exception) {
        traceImpl.ejbMethodEnd(requestId, exception, System.nanoTime(),
                                 Thread.currentThread().toString());
    }


    public void webMethodStart(String requestId, String methodName,
                               String applicationName, String moduleName,
                               String componentName, ComponentType componentType,
                               String callerPrincipal) {
        traceImpl.webMethodStart(requestId, methodName, applicationName, componentType.toString(), 
                                 componentName, callerPrincipal, System.nanoTime(), 
                                 Thread.currentThread().toString());
    }

    public void webMethodEnd(String requestId, Throwable exception) {
        traceImpl.webMethodEnd(requestId,exception, System.nanoTime(), 
                                 Thread.currentThread().toString());
    }

    public void entityManagerMethodStart(
            final String requestId, final EntityManagerMethod entityManagerMethod, 
            final String applicationName, final String moduleName, 
            final String componentName, final ComponentType componentType, 
            final String callerPrincipal) {
        //@TODO
    }

    public void entityManagerMethodEnd(String requestId) {
        //@TODO
    }

    public void entityManagerQueryStart(
            final String requestId, final EntityManagerQueryMethod queryMethod, 
            final String applicationName, final String moduleName, 
            final String componentName, final ComponentType componentType, 
            final String callerPrincipal) {
        //@TODO
    }

    public void entityManagerQueryEnd(final String requestId) {
        //@TODO
    }

}

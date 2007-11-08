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
/*
 * ReplicationLifecycleImpl.java
 *
 * Created on April 16, 2007, 3:11 PM
 *
 */

package com.sun.enterprise.ee.web.initialization;

import com.sun.corba.ee.org.objectweb.asm.tree.analysis.Value;
import org.apache.catalina.LifecycleException;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.appserv.server.ServerLifecycleImpl;

import com.sun.enterprise.web.ServerConfigLookup;

import com.sun.enterprise.ee.web.sessmgmt.JxtaReplicationReceiver;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationHealthChecker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Larry White
 */
public class ReplicationLifecycleImpl extends ServerLifecycleImpl {
    
    public void onTermination() throws ServerLifecycleException { 
        if(!isNativeReplicationEnabled()) {
            return;
        }
        JxtaReplicationReceiver jxtaReplicationReceiver 
            = JxtaReplicationReceiver.createInstance();
        //System.out.println("ReplicationLifecycleImpl:about to call JxtaReplicationReceiver.stop()");
        try {
            jxtaReplicationReceiver.stop();
        } catch (LifecycleException ex) {}
    }
    
    /**
     * Server is shutting down applications
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onShutdown() 
                        throws ServerLifecycleException {
        if(!isNativeReplicationEnabled()) {
            return;
        }
        
        //do not block this thread if the cluster as a whole is stopping        
        if(ReplicationHealthChecker.isClusterStopping()) {
            return;
        }
        //do not block this thread if jxta pipes have never
        //been initialized this means no replication enabled app was
        //ever deployed since this server instance started
        ReplicationHealthChecker healthChecker = ReplicationHealthChecker.getInstance();
        if(!healthChecker.isPipeInitializationCalled()) {
            return;
        }        
        int unloadWaitTime = getMaxSessionUnloadTimeInSeconds();  //7 minutes by default
        //zero value means do not wait at all
        if(unloadWaitTime == 0) {
            return;
        }
        JxtaReplicationReceiver jxtaReplicationReceiver 
            = JxtaReplicationReceiver.createInstance();
        //System.out.println("ReplicationLifecycleImpl:about to call JxtaReplicationReceiver.stop()");
        jxtaReplicationReceiver.repairOnCurrentThread();

        //FIXME for 9.1ur1 only do this if we have a background dispatcher
        //that we guarantee will call doneSignal.countdown()
        CountDownLatch doneSignal = ReplicationHealthChecker.getDoneSignal();
        ReplicationHealthChecker.setFlushThreadWaiting(true);
        try {
            doneSignal.await(unloadWaitTime, TimeUnit.SECONDS);
        } catch(InterruptedException ex) { 
        } finally {
            ReplicationHealthChecker.setFlushThreadWaiting(false);
        }
    }    
    
     private boolean isNativeReplicationEnabled() {
         ServerConfigLookup lookup = new ServerConfigLookup();
         return lookup.isGMSEnabled() && lookup.isNativeReplicationEnabledFromConfig();
     }
     
     private int getMaxSessionUnloadTimeInSeconds() {
         ServerConfigLookup lookup = new ServerConfigLookup();
         return lookup.getMaxSessionUnloadTimeInSecondsPropertyFromConfig();
     }
    
}

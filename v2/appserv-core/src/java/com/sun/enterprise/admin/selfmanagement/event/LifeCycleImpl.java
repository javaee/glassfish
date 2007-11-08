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

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.MBeanNotificationInfo;
import com.sun.enterprise.server.ServerContext;



/**
 * LifeCycleImpl is an MBean that implements the LifecycleListener
 * interface and is a subclass of NotificationBroadcasterSupport.
 */
public class LifeCycleImpl extends NotificationBroadcasterSupport implements LifeCycleImplMBean {
    
    public LifeCycleImpl() {
        DeclarativeLifecycleEventService.setLifeCycleImpl(this);
    }
    
    /**
     * Notification sequence number within the source object.
     */
    private long sequenceNumber = 0;
    
    
    public void onReady(ServerContext sc) {
        long seqno;
        synchronized (this) {
            seqno = sequenceNumber++;
        }
        //LifeCycleNotification n = new LifeCycleNotification(LifeCycleNotification.READY_EVENT, this, seqno, "Ready!!!");
        Notification n = new Notification(LifeCycleEvent.READY_EVENT, this, seqno, "Ready!!!");
        sendNotification(n);
        
    }
    
    public void onShutdown() {
        long seqno;
        synchronized (this) {
            seqno = sequenceNumber++;
        }
        //LifeCycleNotification n = new LifeCycleNotification(LifeCycleNotification.SHUTDOWN_EVENT, this, seqno, "Shutting Down!!!");
        Notification n = new Notification(LifeCycleEvent.SHUTDOWN_EVENT, this, seqno, "Shutting Down!!!");
	sendNotification(n);
        
    }
    
    public void onTermination() {
        long seqno;
        synchronized (this) {
            seqno = sequenceNumber++;
        }
        //LifeCycleNotification n = new LifeCycleNotification(LifeCycleNotification.TERMINATION_EVENT, this, seqno, "Terminated!!!");
        Notification n = new Notification(LifeCycleEvent.TERMINATION_EVENT, this, seqno, "Terminated!!!");
        sendNotification(n);
        
    }
    
    
    public MBeanNotificationInfo[] getNotificationInfo() {
        return notifsInfo.clone();
    }
    
    private static final String[] types =  {
        LifeCycleEvent.READY_EVENT,
                LifeCycleEvent.SHUTDOWN_EVENT,
                LifeCycleEvent.TERMINATION_EVENT
    };
    
    
    private static final MBeanNotificationInfo[] notifsInfo = {
        new MBeanNotificationInfo(
                types,
                "javax.management.Notification",
                "Notifications sent by the LifeCycleImpl MBean")
    };
    
    
}

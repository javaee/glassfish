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

import java.util.HashMap;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.MBeanNotificationInfo;
import com.sun.enterprise.server.ServerContext;

import com.sun.enterprise.admin.monitor.callflow.RequestType;
import static com.sun.appserv.management.event.TraceEventHelper.*;



/**
 * TraceEventImpl.java
 */
public class TraceEventImpl extends NotificationBroadcasterSupport 
                            implements TraceEventImplMBean {
    
    public TraceEventImpl() {
        CallflowEventListener.setTraceImpl(this);
    }
    
    /**
     * Notification sequence number within the source object.
     */
    private long sequenceNumber = 0;
    
    
    public void requestEnd(String requestId,
                long nanoTime, String threadId) {

        long seqno;
        synchronized (this) {
            seqno = sequenceNumber++;
        }
        HashMap map = new HashMap();
        map.put(REQUEST_ID,requestId);
        map.put(NANO_TIME,nanoTime);
        map.put(THREAD_ID, threadId);
        Notification n = new Notification(
                         REQUEST_END, this, seqno, "request end!");
        n.setUserData(map);
        sendNotification(n);
        
    }

    public void requestStart(String requestId, RequestType requestType, 
                String callerIPAddress, long nanoTime, String threadId) {

        long seqno;
        synchronized (this) {
            seqno = sequenceNumber++;
        }
        HashMap map = new HashMap();
        map.put(REQUEST_ID,requestId);
        map.put(REQUEST_TYPE,getRequestTypeString(requestType));
        map.put(CALLER_IPADDRESS,callerIPAddress);
        map.put(NANO_TIME,nanoTime);
        map.put(THREAD_ID, threadId);
        Notification n = new Notification(
                         REQUEST_START, this, seqno, "request start!");
        n.setUserData(map);
        sendNotification(n);
        
    }

    public void ejbMethodStart(String requestId, String methodName, 
                String componentType, String appName, String moduleName,
                String componentName, String tranId, String secId,
                long nanoTime, String threadId) {
        long seqno;
        synchronized (this) {
            seqno = sequenceNumber++;
        }
        HashMap map = new HashMap();
        map.put(REQUEST_ID,requestId);
        map.put(METHOD_NAME,methodName);
        map.put(COMPONENT_TYPE,componentType);
        map.put(APPLICATION_NAME,appName);
        map.put(MODULE_NAME,moduleName);
        map.put(COMPONENT_NAME,componentName);
        map.put(TRANSACTION_ID,tranId);
        map.put(SECURITY_ID,secId);
        map.put(NANO_TIME,nanoTime);
        map.put(THREAD_ID, threadId);
        Notification n = new Notification(
                         EJB_COMPONENT_METHOD_ENTRY, 
                         this, seqno, "ejb method start!");
        n.setUserData(map);
        sendNotification(n);
    }

    public void ejbMethodEnd(String requestId, Throwable exception, 
                long nanoTime, String threadId) {

        long seqno;
        synchronized (this) {
            seqno = sequenceNumber++;
        }
        HashMap map = new HashMap();
        map.put(REQUEST_ID,requestId);
        if (exception != null) {
            map.put(EXCEPTION,getExceptionString(exception));
            map.put(EXCEPTION_OBJECT,exception);
        }
        map.put(NANO_TIME,nanoTime);
        map.put(THREAD_ID, threadId);
        Notification n = new Notification(
                         EJB_COMPONENT_METHOD_EXIT, 
                         this, seqno, "ejb method end!");
        n.setUserData(map);
        sendNotification(n);
    }

  public void webMethodEnd(String requestId, Throwable exception,
                long nanoTime, String threadId) {

        long seqno;
        synchronized (this) {
            seqno = sequenceNumber++;
        }
        HashMap map = new HashMap();
        map.put(REQUEST_ID,requestId);
        if (exception != null) {
            map.put(EXCEPTION,getExceptionString(exception));
            map.put(EXCEPTION_OBJECT,exception);
        }
        map.put(NANO_TIME,nanoTime);
        map.put(THREAD_ID, threadId);
        Notification n = new Notification(
                         WEB_COMPONENT_METHOD_EXIT,
                         this, seqno, "web method end!");
        n.setUserData(map);
        sendNotification(n);
    }

 public void webMethodStart(String requestId, String methodName,
                String applicationName, String componentType, 
                String componentName, String callerPrincipal,
                long nanoTime, String threadId) {

        long seqno;
        synchronized (this) {
            seqno = sequenceNumber++;
        }
        HashMap map = new HashMap();
        map.put(REQUEST_ID,requestId);
        map.put(METHOD_NAME,methodName);
        map.put(COMPONENT_TYPE,componentType);
        map.put(COMPONENT_NAME,componentName);
        map.put(CALLER_PRINCIPAL,callerPrincipal);
        map.put(APPLICATION_NAME,applicationName);
        map.put(NANO_TIME,nanoTime);
        map.put(THREAD_ID, threadId);
        Notification n = new Notification(
                         WEB_COMPONENT_METHOD_ENTRY,
                         this, seqno, "web method start!");
        n.setUserData(map);
        sendNotification(n);
    }


    public MBeanNotificationInfo[] getNotificationInfo() {
        return notifsInfo.clone();
    }

    private String getRequestTypeString(RequestType requestType) {
       if (RequestType.REMOTE_WEB ==  requestType)
           return "remote web request";
       if (RequestType.REMOTE_EJB ==  requestType)
           return "remote ejb request";
       if (RequestType.REMOTE_ASYNC_MESSAGE ==  requestType)
           return "remote asynchronous message request";
       if (RequestType.TIMER_EJB ==  requestType)
           return "timer ejb request";
       if (RequestType.REMOTE_WEB_SERVICE ==  requestType)
           return "remote web service request";
       return "unknown request type";
    }

    private String getExceptionString(Throwable exception) {
            StringBuffer exceptionMsg = new StringBuffer();
            exceptionMsg.append(" Message: ");
            String message = exception.getMessage();
            exceptionMsg.append(message);
            String className = exception.getClass().getName();
            exceptionMsg.append(" Exception Class Name: ");
            exceptionMsg.append(className);
            if (exception.getCause() != null) {
                exceptionMsg.append(" Exception Cause Message: ");
                exceptionMsg.append(exception.getCause().getMessage());
                exceptionMsg.append(" Exception Cause Class Name: ");
                exceptionMsg.append(exception.getCause().getClass().getName());
            }
            return exceptionMsg.toString();
    }
    
    private static final String[] types =  {
            REQUEST_START,
            REQUEST_END,
            WEB_COMPONENT_METHOD_ENTRY,
            WEB_COMPONENT_METHOD_EXIT,
            EJB_COMPONENT_METHOD_ENTRY,
            EJB_COMPONENT_METHOD_EXIT
    };
    
    
    private static final MBeanNotificationInfo[] notifsInfo = {
        new MBeanNotificationInfo(
                types,
                "javax.management.Notification",
                "Notifications sent by the TraceEventImpl MBean")
    };

    
    
}

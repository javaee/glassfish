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
 * TraceEventHelper.java
 * 
 */

package com.sun.appserv.management.event; 

import java.util.Map;
import java.util.HashMap;
import javax.management.Notification;


/*
 *
 * @author      Sun Microsystems, Inc
 */
public class TraceEventHelper { 

    public static final String REQUEST_ID = 
                  "com.sun.appserv.management.event.trace.requestid";

    public static final String REQUEST_TYPE = 
                  "com.sun.appserv.management.event.trace.requesttype";

    public static final String CALLER_IPADDRESS = 
                  "com.sun.appserv.management.event.trace.callerip";

    public static final String CALLER_PRINCIPAL = 
                  "com.sun.appserv.management.event.trace.callerprincipal";

    public static final String NANO_TIME = 
                  "com.sun.appserv.management.event.trace.nanotime";

    public static final String THREAD_ID = 
                  "com.sun.appserv.management.event.trace.threadid";

    public static final String METHOD_NAME = 
                  "com.sun.appserv.management.event.trace.methodname";

    public static final String COMPONENT_TYPE = 
                  "com.sun.appserv.management.event.trace.componenttype";

    public static final String COMPONENT_NAME = 
                  "com.sun.appserv.management.event.trace.componentname";

    public static final String APPLICATION_NAME = 
                  "com.sun.appserv.management.event.trace.applicationname";

    public static final String MODULE_NAME = 
                  "com.sun.appserv.management.event.trace.modulename";

    public static final String TRANSACTION_ID = 
                  "com.sun.appserv.management.event.trace.transactionid";

    public static final String SECURITY_ID = 
                  "com.sun.appserv.management.event.trace.securityid";

    public static final String EXCEPTION = 
                  "com.sun.appserv.management.event.trace.exception";

    public static final String EXCEPTION_OBJECT = 
                  "com.sun.appserv.management.event.trace.exceptionobject";

   // event types
    public static final String REQUEST_START = "trace.request_start";
    public static final String REQUEST_END = "trace.request_end";
    public static final String WEB_COMPONENT_METHOD_ENTRY = 
                                "trace.web_component_method_entry";
    public static final String WEB_COMPONENT_METHOD_EXIT = 
                                "trace.web_component_method_exit";
    public static final String EJB_COMPONENT_METHOD_ENTRY = 
                                "trace.ejb_component_method_entry";
    public static final String EJB_COMPONENT_METHOD_EXIT = 
                                "trace.ejb_component_method_exit";




    public TraceEventHelper(Notification notif) {
        if (notif.getUserData() == null)
            throw new IllegalArgumentException();
        Object uData = notif.getUserData();
        if (!(uData instanceof Map)) {
            throw new IllegalArgumentException();
        }
        this.userData = (HashMap)((HashMap)uData).clone();
    }

    public TraceEventHelper(Map userData) {
        this.userData = (HashMap)((HashMap)userData).clone();
    }

    public String getRequestId() {
        return (String)userData.get(REQUEST_ID);
    }

    public String getRequestId(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (String)((Map)userData).get(REQUEST_ID);
        } else {
          return null;
        }
    }

    public String getRequestType() {
        return (String)userData.get(REQUEST_TYPE);
    }

    public static String getRequestType(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (String)((Map)userData).get(REQUEST_TYPE);
        } else {
          return null;
        }
    }

    public String getCallerIP() {
        return (String)userData.get(CALLER_IPADDRESS);
    }

    public static String getCallerIP(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (String)((Map)userData).get(CALLER_IPADDRESS);
        } else {
          return null;
        }
    }

    public String getCallerPrincipal() {
        return (String)userData.get(CALLER_PRINCIPAL);
    }

    public static String getCallerPrincipal(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (String)((Map)userData).get(CALLER_PRINCIPAL);
        } else {
          return null;
        }
    }

    public Long getNanoTime() {
        return (Long)userData.get(NANO_TIME);
    }

    public static Long getNanoTime(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (Long)((Map)userData).get(NANO_TIME);
        } else {
          return -1L;
        }
    }

    public String getThreadID() {
        return (String)userData.get(THREAD_ID);
    }

    public static String getThreadId(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (String)((Map)userData).get(THREAD_ID);
        } else {
          return null;
        }
    }

    public String getMethodName() {
        return (String)userData.get(METHOD_NAME);
    }

    public static String getMethodName(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (String)((Map)userData).get(METHOD_NAME);
        } else {
          return null;
        }
    }

    public String getComponentType() {
        return (String)userData.get(COMPONENT_TYPE);
    }

    public static String getComponentType(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (String)((Map)userData).get(COMPONENT_TYPE);
        } else {
          return null;
        }
    }

    public String getComponentName() {
        return (String)userData.get(COMPONENT_NAME);
    }

    public static String getComponentName(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (String)((Map)userData).get(COMPONENT_NAME);
        } else {
          return null;
        }
    }

    public String getApplicationName() {
        return (String)userData.get(APPLICATION_NAME);
    }

    public static String getApplicationName(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (String)((Map)userData).get(APPLICATION_NAME);
        } else {
          return null;
        }
    }

    public String getModuleName() {
        return (String)userData.get(MODULE_NAME);
    }

    public static String getModuleName(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (String)((Map)userData).get(MODULE_NAME);
        } else {
          return null;
        }
    }

    public String getTransactionID() {
        return (String)userData.get(TRANSACTION_ID);
    }

    public static String getTransactionID(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (String)((Map)userData).get(TRANSACTION_ID);
        } else {
          return null;
        }
    }

    public String getSecurityID() {
        return (String)userData.get(SECURITY_ID);
    }

    public static String getSecurityID(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (String)((Map)userData).get(SECURITY_ID);
        } else {
          return null;
        }
    }

    public String getExceptionString() {
        return (String)userData.get(EXCEPTION);
    }

    public static String getExceptionString(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (String)((Map)userData).get(EXCEPTION);
        } else {
          return null;
        }
    }

    public Throwable getExceptionObject() {
        return (Throwable)userData.get(EXCEPTION_OBJECT);
    }

    public static Throwable getExceptionObject(Notification notification) {
        Object userData = notification.getUserData();
        if (userData != null) {
            return (Throwable)((Map)userData).get(EXCEPTION_OBJECT);
        } else {
          return null;
        }
    }

   private Map userData = null;

}

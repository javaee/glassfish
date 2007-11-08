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
 * TraceEvent.java
 *
 */

package com.sun.enterprise.admin.selfmanagement.event;

import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationListener;
import javax.management.NotificationFilter;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import com.sun.enterprise.util.i18n.StringManager;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import static com.sun.appserv.management.event.TraceEventHelper.*;



/**
 *
 * @author Sun Micro Systems, Inc
 */
public class TraceEvent implements Event {
    
    private static Logger _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
    private static StringManager sm = StringManager.getManager(TraceEvent.class);
    
    public TraceEvent(String type, TraceEventNotificationFilter lfilter, String description) {
        this.type = type;
        this.lfilter = lfilter;
        if (description != null)
            this.description = description;
        else
            this.description = defaultDescription;
    }
    
    public ObjectName getObjectName() {
        return objName;
    }
    
    
    public String getType() {
        return type;
    }
    
    public NotificationFilter getNotificationFilter( ){
        return lfilter;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void destroy() {
        // do nothing
    }
    
    static ObjectName getTraceImplObjectName() {
        if (objName != null)
            return objName;
        try {
            objName =  new ObjectName( DOMAIN_NAME,DEFAULT_KEY, "trace");
        }catch (MalformedObjectNameException mex) {
            _logger.log(Level.WARNING,"smgt.internal_error", mex);
        }
        return objName;
    }

    public static boolean isValidType(String type) {
        if ("*".equals(type) || REQUEST_START.equals(type) ||
           REQUEST_END.equals(type) || WEB_COMPONENT_METHOD_ENTRY.equals(type) ||
           WEB_COMPONENT_METHOD_EXIT.equals(type) || EJB_COMPONENT_METHOD_ENTRY.equals(type) || 
           EJB_COMPONENT_METHOD_EXIT.equals(type) ) {
            return true;
        }
        return false;
    }
    
    private static ObjectName objName = null;
    private static String defaultDescription =
            sm.getString("selfmgmt_event.trace_event_description");
    private TraceEventNotificationFilter lfilter = null;
    private String description = null;
    private String type = null;

    /**
     * event types
    public static final String REQUEST_START = "trace.request_start";
    public static final String REQUEST_END = "trace.request_end";
    public static final String WEB_COMPONENT_METHOD_ENTRY = "trace.web_component_method_entry";
    public static final String WEB_COMPONENT_METHOD_EXIT = "trace.web_component_method_exit";
    public static final String EJB_COMPONENT_METHOD_ENTRY = "trace.ejb_component_method_entry";
    public static final String EJB_COMPONENT_METHOD_EXIT = "trace.ejb_component_method_exit";
     */
                                                                                                                                               

}

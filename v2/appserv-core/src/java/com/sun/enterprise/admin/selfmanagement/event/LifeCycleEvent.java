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
 * LifeCycleEvent.java
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



/**
 *
 * @author Sun Micro Systems, Inc
 */
public class LifeCycleEvent implements Event {
    
    private static Logger _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
    private static StringManager sm = StringManager.getManager(LifeCycleEvent.class);
    
    public LifeCycleEvent(String type, LifeCycleNotificationFilter lfilter, String description) {
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
    
    static ObjectName getLifeCycleImplObjectName() {
        if (objName != null)
            return objName;
        try {
            objName =  new ObjectName( DOMAIN_NAME,DEFAULT_KEY, "lifecycle");
        }catch (MalformedObjectNameException mex) {
            _logger.log(Level.WARNING,"smgt.internal_error", mex);
        }
        return objName;
    }

    public static boolean isValidType(String type) {
        if ("*".equals(type) || READY_EVENT.equals(type) ||
                SHUTDOWN_EVENT.equals(type) || TERMINATION_EVENT.equals(type) ) {
            return true;
        }
        return false;
    }
    
    private static ObjectName objName = null;
    private static String defaultDescription =
            sm.getString("selfmgmt_event.lifecycle_event_description");
    private LifeCycleNotificationFilter lfilter = null;
    private String description = null;
    private String type = null;

    /**
     * Constant to denote the eventType ready
     */
    public static final String READY_EVENT = "lifecycle.ready";
                                                                                                                                               
    /**
     * Constant to denote the eventType shutdown
     */
    public static final String SHUTDOWN_EVENT = "lifecycle.shutdown";
                                                                                                                                               
    /**
     * Constant to denote the eventType termination
     */
    public static final String TERMINATION_EVENT = "lifecycle.termination";

}

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
 * TraceEventFactory.java
 *
 */

package com.sun.enterprise.admin.selfmanagement.event;


import javax.management.NotificationEmitter;
import javax.management.MBeanServer;

import com.sun.enterprise.config.serverbeans.ElementProperty;
import java.util.logging.Level;
import static com.sun.enterprise.admin.selfmanagement.event.ManagementRuleConstants.*;

/**
 *
 * @author Sun Micro Systems, Inc
 */

public final class TraceEventFactory extends EventAbstractFactory {
    
    private TraceEventFactory( ) {
        super();
        EventBuilder.getInstance().addEventFactory(EVENT_TRACE, this);
        try {
            TraceEventImpl impl = (TraceEventImpl) getMBeanServer().instantiate(
                    "com.sun.enterprise.admin.selfmanagement.event.TraceEventImpl");
            getMBeanServer().registerMBean(impl,TraceEvent.getTraceImplObjectName());
        } catch (javax.management.ReflectionException rex) {
            _logger.log(Level.WARNING,"smgt.internal_error", rex);
        } catch (javax.management.InstanceAlreadyExistsException iex) {
            _logger.log(Level.WARNING,"smgt.internal_error", iex);
        } catch (Exception ex) {
            _logger.log(Level.WARNING,"smgt.internal_error", ex);
            
        }
    }

    private  synchronized void enableNotifications(boolean enable) {
        if (!notificationsEnabled) {
            CallflowEventListener.register();
            notificationsEnabled = true;
        } else if (!enable) {
            CallflowEventListener.unregister();
        }
    }
    
    public Event instrumentEvent(
            ElementProperty[] properties, String description ) {
        enableNotifications(true);
        String eventName = "*";
        String ipAddress = "*";
        String callerPrincipal = "*";
        String componentName = "*";
        for( int i = 0; i < properties.length; i++ ){
            ElementProperty property = properties[i];
            String propertyName = property.getName( ).toLowerCase( );
            if( propertyName.equals(PROPERTY_TRACE_NAME)) {
                eventName = "trace." + property.getValue( ).toLowerCase( );
                if (!TraceEvent.isValidType(eventName))
                    throw new IllegalArgumentException(
                            sm.getString(PROPERTY_TRACE_NAME,
                            "selfmgmt_event.invalid_event_property",EVENT_TRACE));
            }
            if(propertyName.equals(PROPERTY_TRACE_IPADDRESS)) {
                ipAddress = property.getValue( ).toLowerCase( );
            }
            if(propertyName.equals(PROPERTY_TRACE_CALLERPRINCIPAL)) {
                callerPrincipal  = property.getValue( ).toLowerCase( );
            }
            if(propertyName.equals(PROPERTY_TRACE_COMPONENTNAME)) {
                componentName  = property.getValue( ).toLowerCase( );
            }
        }
        return new TraceEvent(eventName, 
                   new TraceEventNotificationFilter(eventName, ipAddress, callerPrincipal, componentName), 
                   description);
    }
    
    static TraceEventFactory getInstance() {
        return instance;
    }
    private boolean notificationsEnabled = false;
    private static final TraceEventFactory instance = new TraceEventFactory();
}

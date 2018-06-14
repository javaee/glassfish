/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.naming;

import java.util.logging.Logger;
import java.util.logging.Level;
import javax.naming.Context;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.AttributeChangeNotification;
import javax.management.Notification;

/**
 * Implementation of the NamingService JMX MBean.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 1.3 $
 */

public final class NamingService
    extends NotificationBroadcasterSupport
    implements NamingServiceMBean, MBeanRegistration {
    
    private static final Logger log = LogFacade.getLogger();

    // ----------------------------------------------------- Instance Variables
        
    /**
     * Status of the Slide domain.
     */
    private State state = State.STOPPED;
    
    /**
     * Notification sequence number.
     */
    private long sequenceNumber = 0;
    
    /**
     * Old URL packages value.
     */
    private String oldUrlValue = "";
    
    /**
     * Old initial context value.
     */
    private String oldIcValue = "";
    
    
    // ---------------------------------------------- MBeanRegistration Methods
    
    public ObjectName preRegister(MBeanServer server, ObjectName name)
        throws Exception {
        return new ObjectName(OBJECT_NAME);
    }
    
    
    public void postRegister(Boolean registrationDone) {
        if (!registrationDone.booleanValue())
            destroy();
    }
    
    
    public void preDeregister()
        throws Exception {
    }
    
    
    public void postDeregister() {
        destroy();
    }
    
    
    // ----------------------------------------------------- SlideMBean Methods
    
    
    /**
     * Retruns the Catalina component name.
     */
    public String getName() {
        return NAME;
    }
    
    
    /**
     * Returns the state.
     */
    public State getState() {
        return state;
    }
    
    
    
    
    /**
     * Start the servlet container.
     */
    public void start()
        throws Exception {
        
        Notification notification = null;
        
        if (state != State.STOPPED)
            return;
        
        state = State.STARTING;
        
        // Notifying the MBEan server that we're starting
        
        notification = new AttributeChangeNotification
            (this, sequenceNumber++, System.currentTimeMillis(), 
             "Starting " + NAME, "State", "org.apache.naming.NamingServiceMBean$State", 
             State.STOPPED, State.STARTING);
        sendNotification(notification);
        
        try {
            
            String value = "org.apache.naming";
            String oldValue = System.getProperty(Context.URL_PKG_PREFIXES);
            if (oldValue != null) {
                oldUrlValue = oldValue;
                value = oldValue + ":" + value;
            }
            System.setProperty(Context.URL_PKG_PREFIXES, value);
            
            oldValue = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
            if (oldValue != null) {
                oldIcValue = oldValue;
            } else {
                System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                                   Constants.Package 
                                   + ".java.javaURLContextFactory");
            }
            
        } catch (Throwable t) {
            state = State.STOPPED;
            notification = new AttributeChangeNotification
                (this, sequenceNumber++, System.currentTimeMillis(), 
                 "Stopped " + NAME, "State", "org.apache.naming.NamingServiceMBean$State", 
                 State.STARTING, State.STOPPED);
            sendNotification(notification);
        }
        
        state = State.STARTED;
        notification = new AttributeChangeNotification
            (this, sequenceNumber++, System.currentTimeMillis(), 
             "Started " + NAME, "State", "org.apache.naming.NamingServiceMBean$State", 
             State.STARTING, State.STARTED);
        sendNotification(notification);
        
    }
    
    
    /**
     * Stop the servlet container.
     */
    public void stop() {
        
        Notification notification = null;
        
        if (state != State.STARTED)
            return;
        
        state = State.STOPPING;
        
        notification = new AttributeChangeNotification
            (this, sequenceNumber++, System.currentTimeMillis(), 
             "Stopping " + NAME, "State", "org.apache.naming.NamingServiceMBean$State", 
             State.STARTED, State.STOPPING);
        sendNotification(notification);
        
        try {    
            System.setProperty(Context.URL_PKG_PREFIXES, oldUrlValue);
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, oldIcValue);
        } catch (Throwable t) {
            log.log(Level.WARNING, LogFacade.UNABLE_TO_RESTORE_ORIGINAL_SYS_PROPERTIES, t);
        }
        
        state = State.STOPPED;
        
        notification = new AttributeChangeNotification
            (this, sequenceNumber++, System.currentTimeMillis(), 
             "Stopped " + NAME, "State", "org.apache.naming.NamingServiceMBean$State", 
             State.STOPPING, State.STOPPED);
        sendNotification(notification);
        
    }
    
    
    /**
     * Destroy servlet container (if any is running).
     */
    public void destroy() {
        
        if (getState() != State.STOPPED)
            stop();
        
    }
    
    
}

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
 * EventBuilder.java
 *
 */

package com.sun.enterprise.admin.selfmanagement.event;

import java.util.concurrent.ConcurrentHashMap;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.util.i18n.StringManager;
import java.util.Hashtable;

/**
 *
 * @author Sun Micro Systems, Inc
 */
public class EventBuilder {
    
    private static final EventBuilder instance = new EventBuilder( );
    
    private static final ConcurrentHashMap<String,EventAbstractFactory>
            eventFactoryMap = new ConcurrentHashMap<String,EventAbstractFactory>(10, 0.75f, 2);
    private boolean initialized = false;
    
    protected static final StringManager sm = StringManager.getManager(EventBuilder.class);
    
    /** Creates a new instance of EventBuilder */
    private EventBuilder() {
    }
    
    public static EventBuilder getInstance( ) {
        return instance;
    }
    
    public synchronized void addEventFactory(String eventType, EventAbstractFactory factory) {
        if ( eventFactoryMap.get( eventType ) == null)
            eventFactoryMap.put(eventType, factory);
    }
    
    // use static initialization to guarantee thread safety
     private static final class Initer {
         private static final Initer _Instance = new Initer();
         static Initer getIniter()  { return _Instance; }
         Initer() {
             LogEventFactory.getInstance();
             NotificationEventFactory.getInstance();
             LifeCycleEventFactory.getInstance();
             MonitorEventFactory.getInstance();
            TimerEventFactory.getInstance();
             TraceEventFactory.getInstance();
         }
     };

    
    /**
     * Given an event type, an event is returned. As of now, supported events
     * are "log","monitor","lifecycle","notification" and "timer" in PE and
     * in addition "gms" in EE.
     * @param eventType one of the supported event types
     * @param properties properties associated with the event
     * @param description event description
     * @return Event constructed event
     */
    public Event getEvent( String eventType,
            ElementProperty[] properties, String description ) {
        
        // guarantee that it's initialized
        // (better than before, but an ugly solution)
        Initer.getIniter();
        
        final EventAbstractFactory factory = eventFactoryMap.get( eventType );
        if( factory == null ) {
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.unknown_event_type",eventType));
        }
        
        return factory.instrumentEvent(properties, description);
    }

    public Event  getEvent( String eventType, String description,
                  Hashtable attributes, ElementProperty[] properties) {
        return getEvent(eventType,properties, description);
    }
    
    
}

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
 * TimerEventFactory.java
 *
 * Created on May 23, 2005, 3:57 PM
 */

package com.sun.enterprise.admin.selfmanagement.event;

import javax.management.timer.Timer;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.admin.selfmanagement.configuration.JavaBeanConfigurator;
import com.sun.enterprise.util.SystemPropertyConstants;
import static com.sun.enterprise.admin.selfmanagement.event.ManagementRuleConstants.*;
/**
 *
 * This is the factory to build and configure Timer Event
 * @author Sun Micro Systems, Inc
 */
public final class TimerEventFactory extends EventAbstractFactory{
    
    
    /** Creates a new instance of LogEventFactory */
    TimerEventFactory() {
        super();
        EventBuilder.getInstance().addEventFactory(EVENT_TIMER, this);
    }
    
    public Event instrumentEvent(
            ElementProperty[] properties, String description ) {
        // expected properties:
        // datestring : one which DateFormatter.parse understands
        // pattern : format dd/mm/yyyy hh:mm:ss
        // period
        // message
        // numberofoccurrences
        
        String datePattern = null;
        String dateString = null;
        long period = 0;
        long numberOfOccurrences = 0;
        String message = "timer notification";
        String type = "timer";
        SimpleDateFormat format  = null;
        
        for( int i = 0; i < properties.length; i++ ){
            ElementProperty property = properties[i];
            String propertyName = property.getName( ).toLowerCase( );
            if (propertyName.equals(PROPERTY_TIMER_PATTERN))
                datePattern = property.getValue();
            if (propertyName.equals(PROPERTY_TIMER_DATESTRING))
                dateString = property.getValue();
            if (propertyName.equals(PROPERTY_TIMER_PERIOD))
                period = Long.parseLong(property.getValue());
            if (propertyName.equals(PROPERTY_TIMER_NUMBER_OF_OCCURRENCES) )
                numberOfOccurrences = Long.parseLong(property.getValue());
            if (propertyName.equals(PROPERTY_TIMER_MESSAGE) )
                message = property.getValue();
            
        }
        
        /**
        if (dateString == null)
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property","dateString","timer"));
        **/
        if (datePattern == null)
            datePattern = defaultPattern;
        
        
        Date d = null;
        try {
            if (dateString ==  null)
                d = new Date();
            else
                d = new SimpleDateFormat(datePattern).parse(dateString);
        } catch (java.text.ParseException pex) {
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property","dateString","timer"),pex);
        }
        Integer id = 0;
        if (period > 0) {
            if (numberOfOccurrences > 0 )
                id = getTimer().addNotification(type,message,null,d,period,numberOfOccurrences);
            else id = getTimer().addNotification(type,message,null,d,period);
            
        } else {
            id = getTimer().addNotification(type,message,null,d);
        }
        return new TimerEvent(id, new TimerEventFilter(id), description);
    }
    
    void removeEvent(int id) {
        try {
            getTimer().removeNotification(id);
        } catch (javax.management.InstanceNotFoundException iex) {
            _logger.log(Level.WARNING,"smgt.internal_error", iex);
        }
    }
    
    private synchronized Timer getTimer() {
        if (timer != null)
            return timer;
        try {
            timer = (Timer)getMBeanServer().instantiate("javax.management.timer.Timer");
            getMBeanServer().registerMBean(timer,TimerEvent.getTimerObjectName());
            timer.start();
            return timer;
        } catch (Exception ex) {
            _logger.log(Level.WARNING,"smgt.internal_error", ex);
        }
        return timer;
    }
    
    static TimerEventFactory getInstance() {
        return instance;
    }
    
    private static final TimerEventFactory instance = new TimerEventFactory();
    private static final String defaultPattern = "MM/dd/yyyy HH:mm:ss";
    private static javax.management.timer.Timer timer = null;
    
}

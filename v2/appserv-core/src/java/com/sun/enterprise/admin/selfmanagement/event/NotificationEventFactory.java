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
 * NotificationEventFactory.java
 *
 */

package com.sun.enterprise.admin.selfmanagement.event;

import javax.management.NotificationEmitter;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.admin.selfmanagement.configuration.JavaBeanConfigurator;
import com.sun.enterprise.util.SystemPropertyConstants;
import static com.sun.enterprise.admin.selfmanagement.event.ManagementRuleConstants.*;
import com.sun.enterprise.server.ApplicationServer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * This is the factory to build and configure Timer Event
 * @author Sun Micro Systems, Inc
 */
public class NotificationEventFactory extends EventAbstractFactory{
    
            /** Creates a new instance of LogEventFactory */
        private NotificationEventFactory() {
        super();
        EventBuilder.getInstance().addEventFactory(EVENT_NOTIFICATION, this);
    }
    
    public Event instrumentEvent(
            ElementProperty[] properties, String description ) {
        // sourcembean
        //String sourceMbean = null;
        String sourceMbeanObjName = null;
        String sourceMbeanName = null;
        for( int i = 0; i < properties.length; i++ ){
            ElementProperty property = properties[i];
            String propertyName = property.getName( ).toLowerCase( );
            if (propertyName.equals(PROPERTY_NOTIFICATION_SOURCE_OBJ_NAME)) {
                sourceMbeanObjName = property.getValue();
            } else if (propertyName.equals(PROPERTY_NOTIFICATION_SOURCEMBEAN)) {
                sourceMbeanName = property.getValue();
            }
        }
        if (sourceMbeanName ==  null && sourceMbeanObjName == null) {
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property","sourceMBean","notification"));
        }
        String sourceMbean = null;
        if(sourceMbeanObjName != null) {
            //final String serverNameVal  = System.getProperty("com.sun.aas.instanceName");
            Pattern pat = Pattern.compile("\\$\\{instance.name\\}");
            Matcher m = pat.matcher(sourceMbeanObjName);
            if(m.find()) {
                sourceMbean = m.replaceAll(instanceName);
            } else {
                sourceMbean = sourceMbeanObjName;
            }
        } else if(sourceMbeanName != null) {
            sourceMbean = ManagementRulesMBeanHelper.getObjName(sourceMbeanName);
        }
        /*if (!(sourceMbean.endsWith(",server=" + instanceName))) {
            sourceMbean = sourceMbean + ",server=" + instanceName;
        }*/
 
        try {
            ObjectName oName = new ObjectName(sourceMbean);
            return new NotificationEvent(oName, description);
        } catch (MalformedObjectNameException ex) {
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property","sourceMBean","notification"));
        }
    }
    
    static NotificationEventFactory getInstance() {
        return instance;
    }
    
    
    private static final NotificationEventFactory instance = new NotificationEventFactory();
    private static final String instanceName = (ApplicationServer.getServerContext()).getInstanceName();    
}

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
package com.sun.enterprise.admin.alert;

import java.util.logging.ErrorManager;

import javax.management.NotificationListener;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.MBeanServer;

import com.sun.enterprise.admin.server.core.AdminService;


import java.util.List;
import java.util.Iterator;

/**
 * Class MBeanRegistrationEventListener listens to MBeanRegistered event.
 * The main purpose of this class is to introspect the 'name' element in
 * objectName of the MBean (or Monitor) registered and check to see if it 
 * matches with any of the names listed for alert-subscription. If it matches
 * then the listener will be subscribed with the MBean.
 *
 * @AUTHOR: Hemanth Puttaswamy 
 */
public class MBeanRegistrationEventListener implements NotificationListener {
    // All the alert subscriptions from domain.xml 
    private List alertSubscriptions;

    private MBeanServer mbeanServer; 

    private static final String NAME_PROPERTY_KEY = "name";
 
    /**
     * Constructor which accepts a list of AlertSubscriptions based on
     * domain.xml entries.
     */
    public MBeanRegistrationEventListener( List alertSubscriptions ) {
        this.alertSubscriptions = alertSubscriptions;
        mbeanServer = 
            AdminService.getAdminService().getAdminContext().getMBeanServer();
    }


    /**
     * If the event type is REGISTRATION_NOTIFICATION and if the 'name' element
     * in registered MBean's ObjectName matches one of names in the alert
     * subscriptions then we will add the Notification Listener and the Filter
     * to the MBean.
     */
    public void handleNotification( Notification notification,
        Object handBack )
    {
        if( !notification.getType().equals( 
            MBeanServerNotification.REGISTRATION_NOTIFICATION  ) )
        {
            // We are only interested in Registration event
            return;
        }
        try {
            MBeanServerNotification mbeanServerNotification =
                (MBeanServerNotification) notification;
            String registeredMbeanName =
                mbeanServerNotification.getMBeanName().getKeyProperty( 
                    NAME_PROPERTY_KEY );
            // Registered ObjectName doesn't have the 'name' key value. So,
            // no action needs to be taken.
            if( registeredMbeanName == null ) return;
            Iterator iterator = alertSubscriptions.iterator( );
            while ( iterator.hasNext( ) ) {
                AlertSubscriptionInfo subscription = 
                    (AlertSubscriptionInfo)iterator.next();
                if( matches( subscription.getMonitorNames(), 
                    registeredMbeanName ))
                {    
                    mbeanServer.addNotificationListener( 
                        mbeanServerNotification.getMBeanName(), 
                            subscription.getNotificationListener(),
                            subscription.getNotificationFilter(), null );
                }
            }
        } catch( Exception e ) {
             new ErrorManager().error( "Error In " +
                  " MBeanServerRegistrationEventListener  ", e,
                  ErrorManager.GENERIC_FAILURE );
        }
    }

    /**
     * A simple utility method to check to see if the registeredMBeanName
     * matches with one of the monitorNames for alert subscription.
     */
    private boolean matches( List monitorNames, String registeredMBeanName ) {
        Iterator iterator = monitorNames.iterator( );
        while( iterator.hasNext( ) ) {
            if( registeredMBeanName.equals( (String) iterator.next() ) ) {
                return true;
            }
        }
        return false;
    }
}
                     

    

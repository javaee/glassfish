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

import javax.management.NotificationListener;
import javax.management.NotificationFilter;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.ErrorManager;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.AlertService;
import com.sun.enterprise.config.serverbeans.AlertSubscription;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ListenerConfig;
import com.sun.enterprise.config.serverbeans.FilterConfig;

import com.sun.enterprise.server.logging.LogMBean;
import com.sun.appserv.management.alert.LogDomains;


/**
 * AlertConfigurator reads the domain.xml entries and configures the 
 * NotificationListeners and NotificationFilters and keeps it ready to
 * subscribe (add) to the Target MBeans(Monitors) to recieve alerts. 
 *
 * @AUTHOR: Hemanth Puttaswamy 
 */
public class AlertConfigurator {

    // A standard name for MBeanServerDelegate 
    private static final String MBEAN_SERVER_DELEGATE_OBJECT_NAME =
        "JMImplementation:type=MBeanServerDelegate";   

    // A standard name for MBeanServerDelegate 
    private static final String DEFAULT_FILTER_CLASS_NAME =
        "com.sun.appserv.management.alert.MailFilter";

    // A standard name for MBeanServerDelegate 
    private static final String LOG_MBEAN_NAME = "LogMBean";

    private static final AlertConfigurator instance = new AlertConfigurator( );

    /**
     * A Singleton Accessor method.
     */
    public static AlertConfigurator getAlertConfigurator( ) {
        return instance;
    }
   
    private AlertConfigurator( ) { }

    /**
     * Get AlertService config from domain.xml 
     */
    private AlertService getAlertService( ) {
        try {
            ServerContext sc = ApplicationServer.getServerContext();
            if( sc == null ) {
                return null;
            }
            return ServerBeansFactory.getConfigBean(
                sc.getConfigContext()).getAlertService( );
        } catch( Exception e ) {
            new ErrorManager().error( "Error In getAlertService  ", e,
                ErrorManager.GENERIC_FAILURE );
        }
        return null;
    }

    /**
     * Configure will be called only once to read the domain.xml entries
     * and configure the Notification Listener and Filter. It will also
     * create a list of AlertSubscription objects and creates 
     * MBeanServerRegistrationEventListener to listen to RegisterMBean events
     * and add the Notification Listener if required.
     */
    public void configure( ) {
        Logger logger = LogDomains.getAlertLogger( );
        if( logger.isLoggable( Level.FINE ) ) {
            logger.log( Level.FINE, "AlertConfigurator.configure called.." );
        }
        AlertService alertService = getAlertService( );
        if( alertService != null ) {
            int count = alertService.sizeAlertSubscription( );
            if( count == 0 ) return;

            List alertSubscriptionList = new ArrayList( );
            for( int i = 0; i < count; i++ ) {
                AlertSubscription subscription = 
                    alertService.getAlertSubscription( i );
                NotificationListener listener = configureNotificationListener( 
                    subscription.getListenerConfig( ) );
                NotificationFilter filter = configureNotificationFilter( 
                    subscription.getFilterConfig( ) );
                alertSubscriptionList.add( new AlertSubscriptionInfo(
                    subscription.getListenerConfig().getSubscribeListenerWith( ),
                        listener, filter )); 
                String monitorNames = subscription.getListenerConfig(
                    ).getSubscribeListenerWith( );
                if( logger.isLoggable( Level.FINE ) ) {
                    logger.log( Level.FINE, 
                        "AlertConfigurator.configure monitorNames.." + 
                             monitorNames );
                }
                StringTokenizer tokenizer = new StringTokenizer( monitorNames,
                    "," );
                // If we are interested in listening to LogMBean event
                // event as well. Subscribe it with an explicit call
                // as LogMBean would've been registered already by now and
                // will never recieve the LogMBean registered notification.
                while( tokenizer.hasMoreTokens( ) ) {
                    String token = tokenizer.nextToken().trim();
                    if( token.equals( LOG_MBEAN_NAME ) ) {
                        LogMBean.getInstance( ).addNotificationListener(
                            listener, filter, null );
                    }
                }
            }
            // Now, we have read all the domain.xml alert subscriptions.
            // We can register a Notification Listener to listen to
            // MBean registered event and then we can introspect the name
            // to see if it matches to add the Listeners capable of
            // emitting alerts.
            MBeanRegistrationEventListener registrationListener =
                 new MBeanRegistrationEventListener( alertSubscriptionList );
            readyForMBeanRegistrationEvent( registrationListener );
        }
    }

    /**
     * Initializes the NotificationListener based on ListenerConfig in 
     * domain.xml .
     */
    private NotificationListener configureNotificationListener( 
        ListenerConfig listenerConfig ) 
    {
        Logger alertLogger = LogDomains.getAlertLogger( );
        if( alertLogger.isLoggable( Level.FINE ) ) {
            alertLogger.log( Level.FINE, 
             "ConfigureNotificationListener called with className ..." +
             listenerConfig.getListenerClassName( ) ); 
        }
        NotificationListener listener = null;
        try {
            listener = (NotificationListener) instantiateAndConfigure( 
                listenerConfig.getListenerClassName( ), 
                    listenerConfig.getElementProperty( ) );
        } catch( Exception e ) {    
            new ErrorManager().error(
                "Error In Notification Listener Config ", e,
                          ErrorManager.GENERIC_FAILURE );
        }
        return listener;
    }

    /**
     * Initializes the NotificationFilter based on FilterConfig in domain.xml .
     */
    private NotificationFilter configureNotificationFilter( 
        FilterConfig filterConfig ) 
    {
        String filterClassName = DEFAULT_FILTER_CLASS_NAME;
        ElementProperty[] properties = null;
        if( filterConfig != null ) {
            filterClassName = filterConfig.getFilterClassName();
            properties = filterConfig.getElementProperty();
        }
        NotificationFilter filter = null;
        try {
            filter = (NotificationFilter) instantiateAndConfigure( 
                filterClassName, properties );
        } catch( Exception e ) {
            new ErrorManager().error(
                "Error In Notification Filter Config ", e,
                          ErrorManager.GENERIC_FAILURE );
        }
        return filter;
    }


    /**
     *  Utility method to set properties to the instantiated Object.
     */
    private void setProperties( Object o, ElementProperty[] properties ) {
        if( properties == null ) return;
        Method[] methods = null;
        try { 
            methods = getDeclaredMethods( o.getClass( ) );
            for( int i = 0; i < properties.length; i++ ) {
                ElementProperty property = properties[i];
                String propertyName = property.getName( ).toLowerCase( );
                String propertyValue = property.getValue( );
                for( int j = 0; j < methods.length; j++ ) {
                    String methodName = methods[j].getName().toLowerCase();
                    if ( ( methodName.startsWith( "set" ) )
                       && ( methodName.endsWith( propertyName ) ) )
                    {
                        Class[] parameterTypes = methods[j].getParameterTypes( );
                        if( parameterTypes.length != 1 ) {
                            new ErrorManager().error(
                                "Only one Parameter is allowed for the setter " +
                                " Method: " + methodName + 
                                " has invalid signature", new Exception(), 
                                ErrorManager.GENERIC_FAILURE );
                        }

                        String parameterType = parameterTypes[0].getName(); 
                        Object[] parameters = new Object[1];

                        if( parameterType.equals( "java.lang.String") ) {
                            parameters[0] = propertyValue;
                        } else if( parameterType.equals( "byte" ) ) {
                            parameters[0] = 
                                new Byte( propertyValue.getBytes()[0]);
                        } else if( parameterType.equals( "int" ) ) {
                            parameters[0] = new Integer(propertyValue);
                        } else if( parameterType.equals( "float" ) ) {
                            parameters[0] = new Float(propertyValue);
                        } else if( parameterType.equals( "double") ) {
                            parameters[0] = new Double(propertyValue);
                        } else if( parameterType.equals( "char" ) ) {
                            parameters[0] = 
                                new Character(propertyValue.charAt(0));
                        } else if( parameterType.equals("boolean") ) {
                            parameters[0] = new Boolean(propertyValue);
                        } else if( parameterType.equals("long") ) {
                            parameters[0] = new Long(propertyValue);
                        } else if( parameterType.equals("short") ) {
                            parameters[0] = new Short(propertyValue);
                        } else {
                            new ErrorManager().error(
                                "Only the basic primitive types can be set " +
                                "as properties to NotificationListener and " +
                                " NotificationFilter ", new Exception(), 
                                ErrorManager.GENERIC_FAILURE );
                            continue;
                        }
                        methods[j].invoke( o,  parameters );
                    }
                }
            }
        } catch( Exception e ) {
            new ErrorManager().error(
                "Error While Setting properties to Notification Listener or " +
                " Filter ", e, ErrorManager.GENERIC_FAILURE );
        }
    }


    /**
     *  A Utility method to instantiate a class and set the properties.
     */
    private Object instantiateAndConfigure( String className, 
        ElementProperty[] properties )
    {
         Logger alertLogger = LogDomains.getAlertLogger();
         if( alertLogger.isLoggable( Level.FINE ) ) {
             alertLogger.log( Level.FINE, 
                 "instantiateAndConfigure called with className.." + 
                 className ); 
         }
         Object o = getInstance( className );
         if( ( o != null )
           &&( properties != null ) )
         {
             if( alertLogger.isLoggable( Level.FINE ) ) {
                 alertLogger.log( Level.FINE, 
                     "instantiateAndConfigure setting Properties.." ); 
             }
             setProperties( o, properties );
         }
         return o;
    }

    /**
     * A Utility method to get an instance of the class.
     * _REVISIT_: Check to see if there is a utility method to do this.
     * If yes, this method can be taken out.
     */
    private Object getInstance( final String className ) {
        if( className == null ) return null;
        return  AccessController.doPrivileged( new PrivilegedAction() {
                public Object run() {
                    try {
                        ClassLoader cl =
                            Thread.currentThread().getContextClassLoader();
                        if (cl == null)
                            cl = ClassLoader.getSystemClassLoader();
                        return Class.forName( className, true, cl).newInstance();                    } catch( Exception e ) {
                        new ErrorManager().error(
                            "Error In Instantiating Class " + className, e,
                            ErrorManager.GENERIC_FAILURE );
                    }
                    return null;
               }
           }
       );
    }

    /**
     * Utility method to get the declared fields.
     */
    private final Method[] getDeclaredMethods(final Class clz) {
        return (Method[]) AccessController.doPrivileged(new PrivilegedAction() {            public Object run() {
                return clz.getDeclaredMethods();
            }
        });
    }


    /**
     * Registers the MBeanRegistrationEventListener with MBean Server Delegate.
     */
    private void readyForMBeanRegistrationEvent( 
        NotificationListener registrationListener ) 
    {
        try {
            MBeanServer mbeanServer = 
                AdminService.getAdminService().getAdminContext(
                    ).getMBeanServer();
            mbeanServer.addNotificationListener( 
                new ObjectName( MBEAN_SERVER_DELEGATE_OBJECT_NAME ),
                registrationListener, (NotificationFilter) null,(Object) null );
        } catch( Exception e ) {
            new ErrorManager().error(
                "Error In registerning MBeanServerNotificationListener ", e,
                ErrorManager.GENERIC_FAILURE );
        }
    }
}

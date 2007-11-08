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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.event;

import java.util.ArrayList;
import java.util.HashMap;

import com.sun.enterprise.admin.event.availability.*;
import com.sun.enterprise.admin.event.http.*;
import com.sun.enterprise.admin.event.jms.*;
import com.sun.enterprise.admin.event.log.*;
import com.sun.enterprise.admin.event.tx.*;
import com.sun.enterprise.admin.event.wsmgmt.WebServiceEndpointEvent;
import com.sun.enterprise.admin.event.wsmgmt.WebServiceEndpointEventListener;
import com.sun.enterprise.admin.event.wsmgmt.TransformationRuleEvent;
import com.sun.enterprise.admin.event.wsmgmt.TransformationRuleEventListener;
import com.sun.enterprise.admin.event.wsmgmt.RegistryLocationEvent;
import com.sun.enterprise.admin.event.wsmgmt.RegistryLocationEventListener;
import com.sun.enterprise.admin.event.selfmanagement.ManagementRuleEvent;
import com.sun.enterprise.admin.event.selfmanagement.ManagementRuleEventListener;


//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Event Listener registry for admin events. This is a singleton object that
 * tracks all registered event handlers.
 */
public class AdminEventListenerRegistry {

    /**
     * Constant used to refer to Notification Event listenerMap
     */
    static final String notificationEventType = "javax.management.Notification";

    /**
     * Add an event listener of type BaseDeployEventListener to the registry.
     * Once registered using this method, a BaseDeployEventListener receives
     * all subsequent BaseDeployEvents.
     */
    public static void addBaseDeployEventListener(
            BaseDeployEventListener listener) {
        AdminEventMulticaster.addListener(BaseDeployEvent.eventType, listener);
    }

    /**
     * Add an event listener of type ApplicationDeployEventListener to the
     * registry. Once registered using this method, an
     * ApplicationDeployEventListener receives all subsequent
     * ApplicationDeployEvents.
     */
    public static void addApplicationDeployEventListener(
            ApplicationDeployEventListener listener) {
        AdminEventMulticaster.addListener(ApplicationDeployEvent.eventType, listener);
    }

    /**
     * Add an event listener of type ModuleDeployEventListener to the registry.
     * Once registered using this method, a ModuleDeployEventListener receives
     * all subsequent ModuleDeployEvents.
     */
    public static void addModuleDeployEventListener(
            ModuleDeployEventListener listener) {
        AdminEventMulticaster.addListener(ModuleDeployEvent.eventType, listener);
    }

    /**
     * Add an event listener of type ResourceDeployEventListener to the
     * registry. Once registered using this method, a
     * ResourceDeployEventListener receives all subsequent ResourceDeployEvents.
     */
    public static void addResourceDeployEventListener(
            ResourceDeployEventListener listener) {
        AdminEventMulticaster.addListener(ResourceDeployEvent.eventType, listener);
    }

    /**
     * Add an event listener of type ConfigChangeEventListener to the registry.
     * Once registered using this method, a ConfigChangeEventListener receives
     * all subsequent ConfigChangeEvents.
     */
    public static void addConfigChangeEventListener(
            ConfigChangeEventListener listener) {
        AdminEventMulticaster.addListener(ConfigChangeEvent.eventType, listener);
    }

    /**
     * Add an event listener of type MonitoringEventListener to the registry.
     * Once registered using this method, a MonitoringEventListener receives
     * all subsequent MonitoringEvents.
     */
    public static void addMonitoringEventListener(
            MonitoringEventListener listener) {
        AdminEventMulticaster.addListener(MonitoringEvent.eventType, listener);
    }

    /**
     * Remove specified event listener from event registry.
     * @param listener the event listener to remove
     */
    public static void removeEventListener(AdminEventListener listener) {
        AdminEventMulticaster.removeListener(listener);
    }

    /**
     * Add a listener for ConfigChangeEvent that is invoked only if any of
     * the config attributes as defined by ConfigChangeCategory have been
     * changed.
     * @see com.sun.enterprise.admin.event.ConfigChangeCategory
     */
    public static void addConfigChangeEventListener(
            ConfigChangeCategory category, ConfigChangeEventListener listener) {
        AdminEventMulticaster.addListener(ConfigChangeEvent.eventType, category,
                listener);
    }

    /**
     * Add a listener for LogLevelChangeEvent 
     * @see com.sun.enterprise.admin.event.LogLevelChangeEvent
     */
    public static void addLogLevelChangeEventListener(
            LogLevelChangeEventListener listener) {
        AdminEventMulticaster.addListener(
                LogLevelChangeEvent.eventType, listener);
    }

    /**
     * Add a listener for ElementChangeEvent 
     * @see com.sun.enterprise.admin.event.ElementChangeEvent
     */
    public static void addElementChangeEventListener(
            ElementChangeEventListener listener) {
        AdminEventMulticaster.addListener(
                ElementChangeEvent.eventType, listener);
    }

    /**
     * Add a listener for SecurityServiceEvent 
     * @see com.sun.enterprise.admin.event.SecurityServiceEvent
     */
    public static void addSecurityServiceEventListener(
            SecurityServiceEventListener listener) {
        AdminEventMulticaster.addListener(
                SecurityServiceEvent.eventType, listener);
    }

    /**
     * Add a listener for AuditModuleEvent 
     * @see com.sun.enterprise.admin.event.AuditModuleEvent
     */
    public static void addAuditModuleEventListener(
            AuditModuleEventListener listener) {
        AdminEventMulticaster.addListener(
                AuditModuleEvent.eventType, listener);
    }

    /**
     * Add a listener for AuthRealmEvent 
     * @see com.sun.enterprise.admin.event.AuthRealmEvent
     */
    public static void addAuthRealmEventListener(
            AuthRealmEventListener listener) {
        AdminEventMulticaster.addListener(
                AuthRealmEvent.eventType, listener);
    }

    /**
     * Add a listener for UserMgmtEvent 
     * @see com.sun.enterprise.admin.event.UserMgmtEvent
     */
    public static void addUserMgmtEventListener(
            UserMgmtEventListener listener) {
        AdminEventMulticaster.addListener(
                UserMgmtEvent.eventType, listener);
    }

    /**
     * Add a listener for EjbTimerEvent 
     * @see com.sun.enterprise.admin.event.EjbTimerEvent
     */
    public static void addEjbTimerEventListener(
            EjbTimerEventListener listener) {
        AdminEventMulticaster.addListener(
                EjbTimerEvent.eventType, listener);
    }

    /**
     * Add a listener for MonitoringLevelChangeEvent 
     * @see com.sun.enterprise.admin.event.MonitoringLevelChangeEvent
     */
    public static void addMonitoringLevelChangeEventListener(
            MonitoringLevelChangeEventListener listener) {
        AdminEventMulticaster.addListener(
                MonitoringLevelChangeEvent.eventType, listener);
    }

    /**
     * Add a listener for ShutdownEvent 
     * @see com.sun.enterprise.admin.event.ShutdownEvent
     */
    public static void addShutdownEventListener(ShutdownEventListener listener) {
        AdminEventMulticaster.addListener(ShutdownEvent.eventType, listener);
    }

    /**
     * Add a listener for AdminEvent 
     * @see com.sun.enterprise.admin.event.AdminEvent
     *
     * @param eventType     type of the event
     * @param listener      listener implemention for notifications
     *
     * @throws IllegalArgumentException if event type and listener type 
     *                                  are not compatible
     */
    public static void addEventListener(
            String eventType, AdminEventListener listener) {

        validateRegistration(eventType, listener);

        AdminEventMulticaster.addListener(
                eventType, listener);
    }

    //---- PRIVATE VARS and PRIVATE METHODS------------------

    private static void validateRegistration(String eventType,AdminEventListener
                                    listener) {

        if ( (eventType == null) || (listener == null)) {
            String msg = localStrings.getString("admin.null_not_supported");
            throw new IllegalArgumentException(msg);
        }

        // Expected listener interface class
        Class listenerImplClass = (Class) listenerMap.get(eventType);
        if (listenerImplClass == null) {
            // event type is not valid or not supported.
            String msg = localStrings.getString("admin.event.invalid_event_type"                                , eventType);
            throw new IllegalArgumentException(msg);
        }
        
        // get the interfaces of the passed in class
        Class[] classes = listener.getClass().getInterfaces();

        if (classes == null) {
            String msg = localStrings.getString("admin.event.does_not_implement"                            ,listener.getClass().getName());
            throw new IllegalArgumentException(msg);
        }

        boolean found = false;
        // go through all interface names
        for(int i=0; i< classes.length; i++) {
          String cName = classes[i].getName(); 
          if (cName != null ) {
            // compare interface name to expected interface name
            if (cName.equals(listenerImplClass.getName() )){
                found = true;
                break;
            }
          }
        }
        if(!found) {
            String msg = localStrings.getString("admin.event.not_compatible",
                    eventType,listenerImplClass.getName(),
                        listener.getClass().getName())  ;
            throw new IllegalArgumentException();
        }
        
    }

    private  static HashMap listenerMap = null;

    static {

        listenerMap = new HashMap();

        listenerMap.put(AdminEvent.eventType, AdminEventListener.class);
        listenerMap.put(ConfigChangeEvent.eventType,
                        ConfigChangeEventListener.class);
        listenerMap.put(MonitoringEvent.eventType,
                        MonitoringEventListener.class);
        listenerMap.put(ShutdownEvent.eventType, ShutdownEventListener.class);
        listenerMap.put(BaseDeployEvent.eventType,
                        BaseDeployEventListener.class);
        listenerMap.put(ApplicationDeployEvent.eventType,
                        ApplicationDeployEventListener.class);
        listenerMap.put(ModuleDeployEvent.eventType,
                        ModuleDeployEventListener.class);
        listenerMap.put(ResourceDeployEvent.eventType,
                        ResourceDeployEventListener.class);
        listenerMap.put(LogLevelChangeEvent.eventType,
                        LogLevelChangeEventListener.class);
        listenerMap.put(MonitoringLevelChangeEvent.eventType,
                        MonitoringLevelChangeEventListener.class);
        //listenerMap.put(AdminEventListenerRegistry.notificationEventType,
        //        Listener.class);
        listenerMap.put(AvailabilityServiceEvent.eventType,
                        AvailabilityServiceEventListener.class);
        listenerMap.put(AuthRealmEvent.eventType,
                        AuthRealmEventListener.class);
        listenerMap.put(AuditModuleEvent.eventType,
                        AuditModuleEventListener.class);
        listenerMap.put(SecurityServiceEvent.eventType,
                        SecurityServiceEventListener.class);
        listenerMap.put(ElementChangeEvent.eventType,
                        ElementChangeEventListener.class);
        listenerMap.put(UserMgmtEvent.eventType, UserMgmtEventListener.class);
        listenerMap.put(EjbTimerEvent.eventType, EjbTimerEventListener.class);
        listenerMap.put(HSServiceEvent.eventType,
                        HSServiceEventListener.class);
        listenerMap.put(HSAccessLogEvent.eventType,
                        HSAccessLogEventListener.class);
        listenerMap.put(HSHttpAccessLogEvent.eventType,
                        HSHttpAccessLogEventListener.class);
        listenerMap.put(HSHttpListenerEvent.eventType,
                        HSHttpListenerEventListener.class);
        listenerMap.put(HSSslEvent.eventType, HSSslEventListener.class);
        listenerMap.put(HSVirtualServerEvent.eventType,
                        HSVirtualServerEventListener.class);
        listenerMap.put(HSHttpProtocolEvent.eventType,
                        HSHttpProtocolEventListener.class);
        listenerMap.put(HSHttpFileCacheEvent.eventType,
                        HSHttpFileCacheEventListener.class);
        listenerMap.put(HSConnectionPoolEvent.eventType,
                        HSConnectionPoolEventListener.class);
        listenerMap.put(HSKeepAliveEvent.eventType,
                        HSKeepAliveEventListener.class);
        listenerMap.put(HSRequestProcessingEvent.eventType,
                        HSRequestProcessingEventListener.class);
        listenerMap.put(JmsServiceEvent.eventType,
                        JmsServiceEventListener.class);
        listenerMap.put(JmsHostEvent.eventType,
                        JmsHostEventListener.class);
        listenerMap.put(MessageSecurityConfigEvent.eventType,
                        MessageSecurityConfigEventListener.class);
        listenerMap.put(LogServiceEvent.eventType,
                        LogServiceEventListener.class);
        listenerMap.put(DynamicReconfigEvent.eventType,
                        DynamicReconfigEventListener.class);
        listenerMap.put(TransactionsRecoveryEvent.eventType,
                        TransactionsRecoveryEventListener.class);
        listenerMap.put(JTSEvent.eventType,
                        JTSEventListener.class);
        listenerMap.put(WebServiceEndpointEvent.eventType,
                        WebServiceEndpointEventListener.class);
        listenerMap.put(TransformationRuleEvent.eventType,
                        TransformationRuleEventListener.class);
        listenerMap.put(RegistryLocationEvent.eventType,
                        RegistryLocationEventListener.class);
        listenerMap.put(MBeanElementChangeEvent.EVENT_TYPE, MBeanElementChangeEventListener.class);
        listenerMap.put(ManagementRuleEvent.eventType,
                        ManagementRuleEventListener.class);
        listenerMap.put(ClusterEvent.eventType,
                        ClusterEventListener.class);
    }

    // i18n StringManager
    private static StringManager localStrings = StringManager.getManager( 
            AdminEventListenerRegistry.class );

}

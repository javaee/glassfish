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


package com.sun.enterprise.admin.selfmanagement.event;

//jdk imports
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.logging.LogManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Hashtable;

//JMX imports
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;


//core imports
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.server.core.AdminService;
import static com.sun.enterprise.admin.selfmanagement.event.ManagementRuleConstants.*;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Cluster;

public class ManagementRulesMBeanHelper {
    
    private static Logger _logger = 
               LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
    
    private static String instanceName = (ApplicationServer.getServerContext()).getInstanceName(); 
    private static ConfigContext configCtx = (ApplicationServer.getServerContext()).getConfigContext();

    private static ConfigContext getConfigContext() {
        return AdminService.getAdminService().getAdminContext().getAdminConfigContext();
    }
    
    /**
     * Gets the registred actions in the domain.
     * @param enabled          if true, gets only enabled actions, otherwise all actions.
     *
     * @returns registered actions
     * @throws ConfigException
     */
    public static List<String> getAllActionMBeans(boolean enabled) throws ConfigException {
        List<String> actionMBeanList = new ArrayList<String>();
        List<Mbean> mbeanList = ServerBeansFactory.getAllMBeanDefinitions(getConfigContext());
        for (Mbean  mbean : mbeanList) {
            if (enabled && !mbean.isEnabled())
                continue;
            String implClassName = mbean.getImplClassName();
            if (implementsInterface(implClassName, "javax.management.NotificationListener"))
                actionMBeanList.add(mbean.getName());
        }
        return actionMBeanList;
    }
    
    private static boolean implementsInterface(String implClassName, String interfaceName) {
        Class ifList[] = null;
        Class class1 = null;
        try {
            ClassLoader mbeanClLoader = getMBeanClassLoader();
            if (mbeanClLoader != null)
                class1  = Class.forName(implClassName,false,mbeanClLoader);
            else
                class1  = Class.forName(implClassName);
            while (class1 != null) {
                ifList = class1.getInterfaces();
                if(ifList != null) {
                    for (int i=0; i<ifList.length; i++) {
                        String canonicalName = ifList[i].getCanonicalName().trim();
                        if( canonicalName.equals(interfaceName) ) {
                            return true;
                        }
                    }
                    class1 = class1.getSuperclass();
                }
            }
        } catch (Exception ex) {
            _logger.log(Level.FINE, " An unexpcted exception occurred " , ex);
        }
        return false;
    }

    private static ClassLoader getMBeanClassLoader() {
        try {
            return (ClassLoader) Class.forName(
            "com.sun.enterprise.admin.mbeans.custom.loading.MBeanClassLoader").
            newInstance();
        } catch (Exception ex) {
            _logger.log(Level.FINE, " An unexpcted exception occurred " , ex);
        }
       return null;
    
    }
    
    /**
     * Gets the list of event types.
     * @param isEE  if true, gets events for EE,  otherwise gets events for PE.
     *
     * @returns list of event types
     */
    public static List<String> getEventTypes(boolean isEE) {
        List<String> eventTypeList = new ArrayList<String>();
        eventTypeList.add(EVENT_LIFECYCLE);
        eventTypeList.add(EVENT_LOG);
        eventTypeList.add(EVENT_TIMER);
        eventTypeList.add(EVENT_TRACE);
        eventTypeList.add(EVENT_MONITOR);
        eventTypeList.add(EVENT_NOTIFICATION);
        if (isEE) {
            eventTypeList.add(EVENT_CLUSTER);
        }
        return eventTypeList;
    }
    
    /**
     * Gets the properties for a given event type.
     * @param eventType  for a given event type, gets the associated property names.
     *
     * @returns list of property names associated with an event type
     */
    public static List<String> getEventProperties(String eventType) {
        if (eventType == null || "".equals(eventType))
            return new ArrayList<String>(0);
        List<String> valueList = null;
        if (eventType.equals(EVENT_LIFECYCLE)) {
            valueList = new ArrayList<String>(1);
            valueList.add(PROPERTY_LIFECYCLE_NAME);
            return valueList;
        }
        if (eventType.equals(EVENT_MONITOR)) {
            valueList = new ArrayList<String>();
            valueList.add(PROPERTY_MONITOR_OBSERVED_OBJ);
            valueList.add(PROPERTY_MONITOR_OBSERVED_OBJ_MBEAN_NAME);
            valueList.add(PROPERTY_MONITOR_OBSERVED_ATTRIBUTE);
            valueList.add(PROPERTY_MONITOR_GRANULARITY_PERIOD);
            valueList.add(PROPERTY_MONITOR_NUMBERTYPE);
            valueList.add(PROPERTY_MONITOR_DIFFERENCEMODE);
            valueList.add(PROPERTY_MONITOR_INIT_THRESHOLD);
            valueList.add(PROPERTY_MONITOR_OFFSET);
            valueList.add(PROPERTY_MONITOR_MODULUS);
            valueList.add(PROPERTY_MONITOR_LOW_THRESHOLD);
            valueList.add(PROPERTY_MONITOR_HIGH_THRESHOLD);
            valueList.add(PROPERTY_MONITOR_STRING_TO_COMPARE);
            valueList.add(PROPERTY_MONITOR_STRING_NOTIFY);
            return valueList;
        }
        if (eventType.equals(EVENT_TRACE)) {
            valueList = new ArrayList<String>(1);
            valueList.add(PROPERTY_TRACE_NAME);
            return valueList;
        }
        if (eventType.equals(EVENT_LOG)) {
            valueList = new ArrayList<String>(2);
            valueList.add(PROPERTY_LOG_LOGGERNAME);
            valueList.add(PROPERTY_LOG_LEVEL);
            return valueList;
        }
        if (eventType.equals(EVENT_TIMER)) {
            valueList = new ArrayList<String>(5);
            valueList.add(PROPERTY_TIMER_PATTERN);
            valueList.add(PROPERTY_TIMER_DATESTRING);
            valueList.add(PROPERTY_TIMER_PERIOD);
            valueList.add(PROPERTY_TIMER_NUMBER_OF_OCCURRENCES);
            valueList.add(PROPERTY_TIMER_MESSAGE);
            return valueList;
        }
        if (eventType.equals(EVENT_NOTIFICATION)) {
            valueList = new ArrayList<String>(2);
            valueList.add(PROPERTY_NOTIFICATION_SOURCEMBEAN);
            valueList.add(PROPERTY_NOTIFICATION_SOURCE_OBJ_NAME);
            return valueList;
        }
        if (eventType.equals(EVENT_CLUSTER)) {
            valueList = new ArrayList<String>(2);
            valueList.add(PROPERTY_CLUSTER_NAME);
            valueList.add(PROPERTY_CLUSTER_SERVERNAME);
            return valueList;
        }
        return new ArrayList<String>(0);
    }
    
    
    public static List<String> getEventPropertyValues(String eventType, String propertyName)
    throws ConfigException {
        if (eventType == null || "".equals(eventType))
            return new ArrayList<String>(0);
        List<String> valueList = null;
        if (eventType.equals(EVENT_LIFECYCLE)) {
            if (PROPERTY_LIFECYCLE_NAME.equals(propertyName)) {
                valueList = new ArrayList<String>(3);
                valueList.add("ready");
                valueList.add("shutdown");
                valueList.add("termination");
                return valueList;
            }
        }
        if (eventType.equals(EVENT_MONITOR)) {
            if (PROPERTY_MONITOR_OBSERVED_OBJ.equals(propertyName)) {
                return new ArrayList<String>(0);
            }
            if (PROPERTY_MONITOR_OBSERVED_OBJ_MBEAN_NAME.equals(propertyName)) {
                return new ArrayList<String>(0);
            }
            if (PROPERTY_MONITOR_OBSERVED_ATTRIBUTE.equals(propertyName)) {
                return new ArrayList<String>(0);
            }
            if (PROPERTY_MONITOR_GRANULARITY_PERIOD.equals(propertyName)) {
                return new ArrayList<String>(0);
            }
            if (PROPERTY_MONITOR_TYPE.equals(propertyName)) {
                valueList =  new ArrayList<String>(3);
                valueList.add(PROPERTY_MONITOR_COUNTER);
                valueList.add(PROPERTY_MONITOR_GAUGE);
                valueList.add(PROPERTY_MONITOR_STRING);
                return valueList;
            }
            if (PROPERTY_MONITOR_NUMBERTYPE.equals(propertyName)) {
                valueList = new ArrayList<String>(6);
                valueList.add("long");
                valueList.add("int");
                valueList.add("short");
                valueList.add("double");
                valueList.add("float");
                valueList.add("byte");
                return valueList;
            }
            if (PROPERTY_MONITOR_DIFFERENCEMODE.equals(propertyName)) {
                valueList = new ArrayList<String>(2);
                valueList.add("true");
                valueList.add("false");
                return valueList;
            }
            if (PROPERTY_MONITOR_INIT_THRESHOLD.equals(propertyName)) {
                return new ArrayList<String>(0);
            }
            if (PROPERTY_MONITOR_OFFSET.equals(propertyName)) {
                return new ArrayList<String>(0);
            }
            if (PROPERTY_MONITOR_MODULUS.equals(propertyName)) {
                return new ArrayList<String>(0);
            }
            if (PROPERTY_MONITOR_LOW_THRESHOLD.equals(propertyName)) {
                return new ArrayList<String>(0);
            }
            if (PROPERTY_MONITOR_HIGH_THRESHOLD.equals(propertyName)) {
                return new ArrayList<String>(0);
            }
            if (PROPERTY_MONITOR_STRING_TO_COMPARE.equals(propertyName)) {
                return new ArrayList<String>(0);
            }
            if (PROPERTY_MONITOR_STRING_NOTIFY.equals(propertyName)) {
                valueList =  new ArrayList<String>(2);
                valueList.add(PROPERTY_MONITOR_STRING_NOTIFY_MATCH);
                valueList.add(PROPERTY_MONITOR_STRING_NOTIFY_DIFFER);
                return valueList;
            }
        }
        if (EVENT_TRACE.equals(eventType)) {
            if (PROPERTY_TRACE_NAME.equals(propertyName)) {
                valueList = new ArrayList<String>(6);
                valueList.add("web_component_method_entry");
                valueList.add("web_component_method_exit");
                valueList.add("request_start");
                valueList.add("request_end");
                valueList.add("ejb_component_method_entry");
                valueList.add("ejb_component_method_exit");
                return valueList;
            }
        }
        if (EVENT_LOG.equals(eventType)) {
            if (PROPERTY_LOG_LOGGERNAME.equals(propertyName)) {
                valueList = new ArrayList<String>();
                Enumeration<String> loggerNames = LogManager.getLogManager().getLoggerNames();
                while (loggerNames.hasMoreElements()) {
                    valueList.add(loggerNames.nextElement());
                }
                return valueList;
            }
            if (PROPERTY_LOG_LEVEL.equals(propertyName)) {
                valueList = new ArrayList<String>();
                valueList.add(Level.ALL.getName());
                valueList.add(Level.CONFIG.getName());
                valueList.add(Level.FINE.getName());
                valueList.add(Level.FINER.getName());
                valueList.add(Level.FINEST.getName());
                valueList.add(Level.INFO.getName());
                valueList.add(Level.SEVERE.getName());
                valueList.add(Level.WARNING.getName());
                return valueList;
            }
        }
        if (EVENT_TIMER.equals(eventType)) {
            if (PROPERTY_TIMER_PATTERN.equals(propertyName) ||
                    PROPERTY_TIMER_DATESTRING.equals(propertyName) ||
                    PROPERTY_TIMER_PERIOD.equals(propertyName) ||
                    PROPERTY_TIMER_NUMBER_OF_OCCURRENCES.equals(propertyName) ||
                    PROPERTY_TIMER_MESSAGE.equals(propertyName) ) {
                return new ArrayList<String>(0);
            }
        }
        if (EVENT_NOTIFICATION.equals(eventType)) {
            if (PROPERTY_NOTIFICATION_SOURCEMBEAN.equals(propertyName)) {
                return getAllNotificationEmitterMbeans(true);
                // return new ArrayList<String>(0);
            }
            if (PROPERTY_NOTIFICATION_SOURCE_OBJ_NAME.equals(propertyName)) {
                return new ArrayList<String>(0);
            }
        }
        if (EVENT_CLUSTER.equals(eventType)) {
            if (PROPERTY_CLUSTER_NAME.equals(propertyName)) {
                valueList = new ArrayList<String>(3);
                valueList.add("start");
                valueList.add("stop");
                valueList.add("fail");
                return valueList;
            }
            if (PROPERTY_CLUSTER_SERVERNAME.equals(propertyName)) {
                valueList = new ArrayList<String>();
                Server servers[] = ServerHelper.getServersInDomain(getConfigContext());
                for (Server server : servers) {
                    valueList.add(server.getName());
                }
                return valueList;
            }
        }
        return new ArrayList<String>(0);
    }
    
    public static List<String> getAllNotificationEmitterMbeans(boolean enabled) throws ConfigException {
        List<String> customMBeanList = new ArrayList<String>();
        List<Mbean> mbeanList = ServerBeansFactory.getAllMBeanDefinitions(getConfigContext());
        for (Mbean  mbean : mbeanList) {
            if (enabled && !mbean.isEnabled())
                continue;
            String implClassName = mbean.getImplClassName();
            if (implementsInterface(implClassName, "javax.management.NotificationEmitter"))
                customMBeanList.add(mbean.getName());
        }
        return customMBeanList;
    }
    
    
    /**
     * Gets the registred MBeans registered in the server's MBean server.
     * @param filter  ObjectName filter for quering MBean server.
     *
     * @returns list of registered mbeans
     * @throws MalformedObjectNameException
     */
    public static Set<ObjectName> getRegisteredMBeans(String filter) throws MalformedObjectNameException {
        ObjectName userFilteredObjectName = null;
        MBeanServer mbeanServer = MBeanServerFactory.getMBeanServer();
        if (filter != null) {
            userFilteredObjectName = new ObjectName(filter);
            return mbeanServer.queryNames(userFilteredObjectName, null);
        }
        String monitoringObjectNameFilter = "com.sun.appserv:category=monitor,*";
        String jsr77ObjectNameFilter = "com.sun.appserv:category=runtime,*";
        ObjectName monitoringFilteredObjectName = null;
        ObjectName jsr77FilteredObjectName = null;
        try {
            monitoringFilteredObjectName = new ObjectName(monitoringObjectNameFilter);
            jsr77FilteredObjectName = new ObjectName(jsr77ObjectNameFilter);
        } catch (Exception ex) {
            _logger.log(Level.FINE, " An unexpcted exception occurred " , ex);
        }
        Set<ObjectName> setObjNames = mbeanServer.queryNames(monitoringFilteredObjectName, null);
        setObjNames.addAll(mbeanServer.queryNames(jsr77FilteredObjectName,null));
        return setObjNames;
    }
    
    /**
     * Gets the attributes for a given ObjectName.
     * @param objName  ObjectName for which the attributes are required.
     *
     * @returns list of attributes
     * @throws InstanceNotFoundException,IntrospectionException, ReflectionException
     */
    public static List<String> getAttributes(ObjectName objName) throws InstanceNotFoundException, IntrospectionException,
            ReflectionException {
        if (objName == null)
            return new ArrayList<String>(0);
        MBeanInfo mbInfo =  MBeanServerFactory.getMBeanServer().getMBeanInfo(objName);
        MBeanAttributeInfo attrInfoArr[] = mbInfo.getAttributes();
        List<String> attributes = new ArrayList<String>();
        for (MBeanAttributeInfo attrInfo : attrInfoArr) {
            attributes.add(attrInfo.getName());
        }
        return attributes;
    }
    
    public static List<String> getMBeanAttributes(String objectNameStr) throws MalformedObjectNameException,
            InstanceNotFoundException, IntrospectionException,
            ReflectionException {
        ObjectName objName = new ObjectName(objectNameStr);
        return getAttributes(objName);
    }
    
    public static List<String> getNotificationTypes(ObjectName objName) throws InstanceNotFoundException, IntrospectionException,
            ReflectionException {
        if (objName == null)
            return new ArrayList<String>(0);
        MBeanInfo mbInfo =  MBeanServerFactory.getMBeanServer().getMBeanInfo(objName);
        MBeanNotificationInfo notifInfoArr[] = mbInfo.getNotifications();
        List<String> notifications = new ArrayList<String>();
        for (MBeanNotificationInfo notifInfo : notifInfoArr) {
            String notifTypes[] = notifInfo.getNotifTypes();
            for (String notifType : notifTypes)
                notifications.add(notifType);
        }
        return notifications;
    }
    
    public static List<String> getNotificationTypes(String objectNameStr) throws MalformedObjectNameException,
            InstanceNotFoundException, IntrospectionException,
            ReflectionException {
        ObjectName objName = new ObjectName(objectNameStr);
        return getNotificationTypes(objName);
    }
    
    
    
    public static List<String> getAttributes(String dottedName) { return null; }
    
    public static List<String> getDottedNames(String dottedName) { return null; }
    
    public static String getObjName(String mBeanName) {
        String objName = null;
        String cascadedObjName = null;
        if (mBeanName != null) {
            try {
                Domain domain = ServerBeansFactory.getDomainBean(configCtx);
                ApplicationRef appRef = verifyMBean(mBeanName, domain);
                if (appRef != null) {
                    Applications apps = domain.getApplications();
                    Mbean definedMBean = apps.getMbeanByName(mBeanName);
                    if(definedMBean != null) {
                        objName = definedMBean.getObjectName();
                        cascadedObjName = getCascadingAwareObjectName(new ObjectName(objName)).toString();
                    }
                }
            } catch(ConfigException ex) {
                _logger.log(Level.INFO, "An unexpected exception occured.", ex);
            } catch (Exception ex) {
                _logger.log(Level.INFO, "An unexpected exception occured.", ex);
            } 
        }                                                                                                                                   
        //objName = ObjectNames.getApplicationObjectName(instanceName,mBeanName);
        /*if (!(objName.endsWith(",server=" + instanceName))) {
            objName = objName + ",server=" + instanceName;
        }*/
        return cascadedObjName;
    }
   
    public static ObjectName getCascadingAwareObjectName(final ObjectName configON) throws RuntimeException {
        try {
            final String serverNameKey  = "server";
            //final String serverNameVal  = System.getProperty("com.sun.aas.instanceName");
            final Hashtable properties  = configON.getKeyPropertyList();
            properties.put(serverNameKey, instanceName);
            final ObjectName ron = new ObjectName(configON.getDomain(), properties);
            return ( ron );
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*public static String getRealObjectName(final ObjectName configON, String key) 
        throws RuntimeException {
        try {
            final String serverNameVal  = System.getProperty("com.sun.aas.instanceName");
            if(!(configON.getKeyProperty(key).equals(serverNameVal))) {
                //If the object name doesn't already have the specified key value
                final Hashtable properties  = configON.getKeyPropertyList();
                properties.put(key, serverNameVal);
                final ObjectName ron = new ObjectName(configON.getDomain(), properties);
                return ( ron.toString() );
            } else {
                return ( configON.toString() );
            }
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }*/
 
    private static ApplicationRef verifyMBean(String mBeanName, Domain domain) {
        ApplicationRef appRef = null;
        
        try {
            Server instanceBean = ServerBeansFactory.getServerBean(configCtx);
            appRef = instanceBean.getApplicationRefByRef(mBeanName);
                                                                                                                                               
            if (appRef == null) {
                //check at cluster level, if this instance is part of cluster
                Clusters clusters = domain.getClusters();
                Cluster[] cluster = clusters.getCluster();
                for (Cluster val : cluster) {
                    if ((val.getServerRefByRef(instanceName)) != null) {
                        appRef = val.getApplicationRefByRef(mBeanName);
                        break;
                    }
                }
            }
                                                                                                                                               
            //should have obtained the app reference, if all well
            if (appRef != null) {
                //check there exists a definition
                Applications apps = domain.getApplications();
                Mbean definedMBean = apps.getMbeanByName(mBeanName);
                if (definedMBean == null ) {
                    appRef = null;
                }
            }
        } catch (ConfigException ex) {
            _logger.log(Level.INFO, "smgt.config_error", ex);
        }
                                                                                                                                               
        return appRef;
    }

}


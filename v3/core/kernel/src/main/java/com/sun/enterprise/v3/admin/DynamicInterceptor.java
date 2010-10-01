/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package com.sun.enterprise.v3.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;

import java.io.ObjectInputStream;

import javax.management.*;  // we'll need just about all of them, so avoid clutter
import javax.management.loading.ClassLoaderRepository;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
    This Interceptor wraps the real MBeanServer so that additional interceptor code can be
    "turned on" at a later point.  However, it must be possible to start the MBeanServer even before
    the JVM calls main().  Therefore,
    <b>This class must not depend on anything that can't initialize before the JVM calls main()</b>.
    <i>This includes things like logging which is not happy being invoked
    that early.</i>
    <p>
    When instantiated at startup, the instance of this class that wraps the real MBeanServer
    is termed the "Primary Interceptor".  There can only be one such Interceptor for each
    *real* MBeanServer.  MBeanServer #0 is the Platform MBeanServer, and this class <b>must</b> be
    used for GlassFish.  Additional MBeanServers can be created if desired.
    <p>
    This class can also be used to implement an Interceptor which can be set for use by the Primary
    Interceptor.  Such interceptors are used only for get/setAttribute(s) and invoke(), though
    the use of them could be expanded for other methods.
    <p>
    Note that many methods are declared 'final' for efficiency.  If a subclass needs
    to override a method, remove 'final'. Until that time, we might as well remain efficient,
    since most methods won't be overridden.
 */
public class DynamicInterceptor implements MBeanServer
{
    private volatile MBeanServer mDelegateMBeanServer;
    private static HashMap<String, MBeanServerConnection> instanceConnections;
    

    public DynamicInterceptor() {
        mDelegateMBeanServer    = null;
        instanceConnections = new HashMap<String, MBeanServerConnection>();
    }

    private String getInstance(final ObjectName o) throws InstanceNotFoundException {
        String j2eeTypeProp = o.getKeyProperty("j2eeType");

        // This is kludge; we set the JDK sys property in domain template but that
        // gets set for DAS and instances. For instance, we want only default MBean server
        // So if this instance is not DAS, we just return server so the instances will
        // use default MbeanServer.
        if(!MbeanService.getInstance().isDas())
                return "server";

        // J2EEDomain is on the DAS
        if ("J2EEDomain".equals(j2eeTypeProp))
            return "server";

        // If its a J2EEServer that we are looking at
        String name;
        if (j2eeTypeProp != null && j2eeTypeProp.equals("J2EEServer"))
            name = o.getKeyProperty("name");
        else
            // if its any other MO that has a J2EEServer as a parent
            name = o.getKeyProperty("J2EEServer");
        return MbeanService.getInstance().isValidServer(name) ? name : null;
    }

    private MBeanServerConnection getInstanceConnection(String instanceName) throws InstanceNotFoundException {
        if(!instanceConnections.containsKey(instanceName)) {
            synchronized(this) {
                try {
                    String urlStr = "service:jmx:rmi:///jndi/rmi://" +
                            MbeanService.getInstance().getHost(instanceName) + ":" +
                            MbeanService.getInstance().getJMXPort(instanceName) + "/jmxrmi";
                    JMXServiceURL url = new JMXServiceURL(urlStr);
                    JMXConnector jmxConn = JMXConnectorFactory.connect(url);
                    MBeanServerConnection conn = jmxConn.getMBeanServerConnection();
                    instanceConnections.put(instanceName, conn);
                } catch(Exception ex) {
                     throw new InstanceNotFoundException(ex.getLocalizedMessage());
                }
            }
        }
        return instanceConnections.get(instanceName);
    }

    /**
        Get the MBeanServer to which the request can be delegated.
     */
    public MBeanServer getDelegateMBeanServer() {
        return mDelegateMBeanServer;
    }

    public void setDelegateMBeanServer(final MBeanServer server)  {
        mDelegateMBeanServer    = server;
    }

    public Object invoke( final ObjectName objectName, final String operationName,
                          final Object[] params, final String[] signature)
            throws ReflectionException, InstanceNotFoundException, MBeanException {
        String instance = getInstance(objectName);
        if( (instance == null) || (instance.equals("server")))
            return getDelegateMBeanServer().invoke( objectName, operationName, params, signature );
        try {
            return getInstanceConnection(instance).invoke(objectName, operationName, params, signature);
        } catch (IOException ioex) {
            throw new MBeanException(ioex);
        }
    }
    
    public final Object getAttribute(final ObjectName objectName, final String attributeName)
            throws InstanceNotFoundException, AttributeNotFoundException, MBeanException, ReflectionException {
        String instance = getInstance(objectName);
        if( (instance == null) || (instance.equals("server")))
            return getDelegateMBeanServer().getAttribute( objectName, attributeName);
        try {
            return getInstanceConnection(instance).getAttribute(objectName, attributeName);
        } catch (IOException ioex) {
            throw new MBeanException(ioex);
        }
    }
    
    public void setAttribute(final ObjectName objectName, final Attribute attribute) throws
            InstanceNotFoundException, AttributeNotFoundException, MBeanException,
            ReflectionException, InvalidAttributeValueException {
        String instance = getInstance(objectName);
        if( (instance == null) || (instance.equals("server"))) {
            getDelegateMBeanServer().setAttribute( objectName, attribute );
            return;
        }
        try {
            getInstanceConnection(instance).setAttribute(objectName, attribute);
        } catch (IOException ioex) {
            throw new MBeanException(ioex);
        }
    }

    public final AttributeList getAttributes(final ObjectName objectName, final String[] attrNames)
            throws InstanceNotFoundException, ReflectionException {
        String instance = getInstance(objectName);
        if( (instance == null) || (instance.equals("server")))
            return getDelegateMBeanServer().getAttributes( objectName, attrNames );
        try {
            return getInstanceConnection(instance).getAttributes(objectName, attrNames);
        } catch (IOException ioex) {
            throw new InstanceNotFoundException(ioex.getLocalizedMessage());
        }
    }

    public AttributeList setAttributes (final ObjectName objectName, final AttributeList attributeList)
            throws InstanceNotFoundException, ReflectionException {
        String instance = getInstance(objectName);
        if( (instance == null) || (instance.equals("server")))
            return getDelegateMBeanServer().setAttributes( objectName, attributeList );
        try {
            return getInstanceConnection(instance).setAttributes(objectName, attributeList);
        } catch (IOException ioex) {
            throw new InstanceNotFoundException(ioex.getLocalizedMessage());
        }
    }
    
    public final ObjectInstance registerMBean(final Object obj, final ObjectName objectName)
            throws NotCompliantMBeanException, MBeanRegistrationException, InstanceAlreadyExistsException {
        return getDelegateMBeanServer().registerMBean( obj, objectName );
    }
    
    public final void unregisterMBean(final ObjectName objectName)
            throws InstanceNotFoundException, MBeanRegistrationException {
        getDelegateMBeanServer().unregisterMBean( objectName );
    }
	
    public final Integer getMBeanCount() {
        return getDelegateMBeanServer().getMBeanCount( );
    }

    public final Set queryMBeans( final ObjectName objectName, final QueryExp expr ) {
        try {
            String instance = getInstance(objectName);
            if( (instance == null) || (instance.equals("server")))
                return getDelegateMBeanServer().queryMBeans( objectName, expr );
            return getInstanceConnection(instance).queryMBeans(objectName, expr);
        } catch (Exception ex) {
            return null;
        }
    }

    public final MBeanInfo getMBeanInfo( final ObjectName objectName)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException {
        String instance = getInstance(objectName);
        if( (instance == null) || (instance.equals("server")))
            return getDelegateMBeanServer().getMBeanInfo( objectName );
        try {
            return getInstanceConnection(instance).getMBeanInfo(objectName);
        } catch (IOException ioex) {
            throw new InstanceNotFoundException(ioex.getLocalizedMessage());
        }
    }

    public final boolean isRegistered( final ObjectName objectName) {
        try {
            String instance = getInstance(objectName);
            if( (instance == null) || (instance.equals("server")))
                return getDelegateMBeanServer().isRegistered( objectName );
            return getInstanceConnection(instance).isRegistered(objectName);
        } catch (Exception ex) {
            return false;
        }
    }

    public final void addNotificationListener( final ObjectName objectName,
                                               final NotificationListener notificationListener,
                                               final NotificationFilter notificationFilter, final Object obj)
            throws InstanceNotFoundException {
        getDelegateMBeanServer().addNotificationListener(objectName, notificationListener, notificationFilter, obj);
    }

    public final void addNotificationListener(final ObjectName objectName, final ObjectName objectName1,
                                              final NotificationFilter notificationFilter, final Object obj)
            throws InstanceNotFoundException {
        getDelegateMBeanServer().addNotificationListener(objectName, objectName1, notificationFilter, obj);
    }

    public final ObjectInstance createMBean( final String str, final ObjectName objectName)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
            MBeanException, NotCompliantMBeanException {
        return getDelegateMBeanServer().createMBean (str, objectName);
    }

    public final ObjectInstance createMBean( final String str, final ObjectName objectName,
                                             final ObjectName objectName2)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException, InstanceNotFoundException {
        return getDelegateMBeanServer().createMBean (str, objectName, objectName2);
    }

    public final ObjectInstance createMBean( final String str, final ObjectName objectName, final Object[] obj,
                                             final String[] str3)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
        return getDelegateMBeanServer().createMBean (str, objectName, obj, str3);
    }

    public final ObjectInstance createMBean ( final String str, final ObjectName objectName,
                                              final ObjectName objectName2, final Object[] obj, final String[] str4)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
            MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
       return getDelegateMBeanServer().createMBean (str, objectName, objectName2, obj, str4);
    }

    public final ObjectInputStream deserialize (String str, byte[] values)
            throws OperationsException, ReflectionException {
        return getDelegateMBeanServer().deserialize (str, values);
    }

    public final ObjectInputStream deserialize( final ObjectName objectName, final byte[] values)
            throws InstanceNotFoundException, OperationsException {
        return getDelegateMBeanServer().deserialize (objectName, values);
    }

    public final ObjectInputStream deserialize( final String str, final ObjectName objectName, byte[] values)
            throws InstanceNotFoundException, OperationsException, ReflectionException {
        return getDelegateMBeanServer().deserialize (str, objectName, values);
    }

    public final String getDefaultDomain() {
        return getDelegateMBeanServer().getDefaultDomain();
    }
    
    public final ObjectInstance getObjectInstance(ObjectName objectName) throws InstanceNotFoundException {
        String instance = getInstance(objectName);
        if( (instance == null) || (instance.equals("server")))
            return getDelegateMBeanServer().getObjectInstance(objectName);
        try {
            return getInstanceConnection(instance).getObjectInstance(objectName);
        } catch (IOException ioex) {
            throw new InstanceNotFoundException(ioex.getLocalizedMessage());
        }
    }
    
    public final Object instantiate( final String str) throws ReflectionException, MBeanException {
        return getDelegateMBeanServer().instantiate(str);
    }
    
    public final Object instantiate( final String str, final ObjectName objectName)
            throws ReflectionException, MBeanException, InstanceNotFoundException {
        return getDelegateMBeanServer().instantiate(str, objectName);
    }
    
    public final Object instantiate( final String str, final Object[] obj, final String[] str2)
            throws ReflectionException, MBeanException {
        return getDelegateMBeanServer().instantiate(str, obj, str2);
    }
    
    public final Object instantiate( final String str, final ObjectName objectName, final Object[] obj,
                                     final String[] str3)
            throws ReflectionException, MBeanException, InstanceNotFoundException {
        return getDelegateMBeanServer().instantiate(str, objectName, obj, str3);
    }

    public final boolean isInstanceOf ( final ObjectName objectName,  final String str)
            throws InstanceNotFoundException {
        String instance = getInstance(objectName);
        if( (instance == null) || (instance.equals("server")))
            return getDelegateMBeanServer().isInstanceOf(objectName, str);
        try {
            return getInstanceConnection(instance).isInstanceOf(objectName, str);
        } catch (IOException ioex) {
            throw new InstanceNotFoundException(ioex.getLocalizedMessage());
        }
    }

    public final Set queryNames( final ObjectName objectName, final QueryExp queryExp) {
        try {
            String instance = getInstance(objectName);
            if( (instance == null) || (instance.equals("server")))
                return getDelegateMBeanServer().queryNames( objectName, queryExp);
            return getInstanceConnection(instance).queryNames(objectName, queryExp);
        } catch (Exception e) {
            return null;
        }
    }

    public final void removeNotificationListener(final ObjectName objectName,  final ObjectName objectName1)
            throws InstanceNotFoundException, ListenerNotFoundException {
        getDelegateMBeanServer().removeNotificationListener( objectName, objectName1);
    }

    public final void removeNotificationListener( final ObjectName objectName,
                                                  final NotificationListener notificationListener)
            throws InstanceNotFoundException, ListenerNotFoundException {
        getDelegateMBeanServer().removeNotificationListener( objectName, notificationListener);
    }
      
    public final void removeNotificationListener( final ObjectName objectName,
                                                  final NotificationListener notificationListener,
                                                  final NotificationFilter notificationFilter, final Object obj)
            throws InstanceNotFoundException, ListenerNotFoundException {
        getDelegateMBeanServer().removeNotificationListener(objectName, notificationListener, notificationFilter, obj);
    }
    
    public final void removeNotificationListener( final ObjectName objectName, final ObjectName objectName1,
                                                  final NotificationFilter    notificationFilter, final Object obj)
            throws InstanceNotFoundException, ListenerNotFoundException {
        getDelegateMBeanServer().removeNotificationListener( objectName, objectName1, notificationFilter, obj);
    }

    public final ClassLoader getClassLoader( final ObjectName objectName) throws InstanceNotFoundException {
        return getDelegateMBeanServer().getClassLoader( objectName );
    }
    
    public final ClassLoader getClassLoaderFor( final ObjectName objectName) throws InstanceNotFoundException {
        return getDelegateMBeanServer().getClassLoaderFor( objectName );
    }
    
    public final ClassLoaderRepository getClassLoaderRepository() {
    	return getDelegateMBeanServer().getClassLoaderRepository();
    }
    
    public final String[] getDomains() {
        return getDelegateMBeanServer().getDomains();
    }
}

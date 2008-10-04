/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.amx.internal;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanServerInvocationHandler;

import com.sun.appserv.management.util.jmx.JMXUtil;

/**
 * Enables assertions on the desired classes, then loads them.
 * @author lloyd
 */
public final class LoadSanityChecks {
    private LoadSanityChecks() {}
    
    private static final ObjectName NAME = JMXUtil.newObjectName("amx-support:name=sanity-checks");

        public static synchronized ObjectName
    load(final MBeanServer mbeanServer) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, InstanceAlreadyExistsException
    {
        final ObjectName objectName = NAME;
        
        if ( mbeanServer.isRegistered(objectName) ) 
        {
            return objectName;
        }
        
        final Class<LoadSanityChecks> myClass = LoadSanityChecks.class;
        final ClassLoader cl = myClass.getClassLoader();
        cl.setPackageAssertionStatus( myClass.getPackage().getName(), true );
        cl.setClassAssertionStatus( "org.glassfish.admin.amx.internal.SanityChecks", true );

        final SanityChecks checks = SanityChecks.newInstance(mbeanServer);
        
        // ensure that the name wasn't altered during registration
        if ( ! mbeanServer.registerMBean( checks, objectName ).getObjectName().equals(objectName) )
        {
            throw new IllegalStateException();
        }
        
        return objectName;
    }
    
    public static SanityChecksMBean getSanityChecksMBean(final MBeanServer mbeanServer)
    {
        try
        {
            final ObjectName objectName = load(mbeanServer);
            return JMXUtil.newProxyInstance( mbeanServer, objectName, SanityChecksMBean.class );
        }
        catch( Exception e )
        {
            throw new RuntimeException(e);
        }
    }
}

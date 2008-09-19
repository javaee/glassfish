/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.amx.internal;

import com.sun.appserv.management.util.jmx.JMXUtil;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.MBeanServerInvocationHandler;

/**
 *
 * @author lloyd
 */
public final class LoadSanityChecks {
    private final MBeanServer mMBeanServer;
    
    public LoadSanityChecks(final MBeanServer server) {
        mMBeanServer = server;
        final ClassLoader cl = this.getClass().getClassLoader();
        cl.setPackageAssertionStatus( this.getClass().getPackage().getName(), true );
        cl.setClassAssertionStatus( "org.glassfish.admin.amx.internal.SanityChecks", true );
    }

    public SanityChecksMBean load() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
    {
        ObjectName objectName = JMXUtil.newObjectName( "amx-support:name=sanity-checks");
        final SanityChecks checks = SanityChecks.newInstance(mMBeanServer);
        objectName = mMBeanServer.registerMBean( checks, objectName ).getObjectName();

        final SanityChecksMBean  mb = JMXUtil.newProxyInstance( mMBeanServer, objectName, SanityChecksMBean.class );
        
        return mb;
    }
}

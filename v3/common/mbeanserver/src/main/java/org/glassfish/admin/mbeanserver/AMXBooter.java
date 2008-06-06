/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.admin.mbeanserver;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.glassfish.api.Async;
import org.glassfish.api.Startup;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;


/**
    Registers the AMXBooter MBean.

    Public API is the name of the booter MBean: "amx-support:name=amx-booter" along with the
    methods found in AMXStartupServiceMBean, in particular bootAMX(),
 */
public final class AMXBooter implements  AMXBooterMBean
{
    private final MBeanServer mMBeanServer;
    private final ObjectName  mObjectName;
    private ObjectName        mDomainRootObjectName;
    
    //@Inject
   // private org.glassfish.admin.amx.config.AMXConfigRegistrar mConfigRegistrar;
   
    private static void debug( final String s ) { System.out.println(s); }
    
    /** ObjectName of the AMXBooter MBean */
    public static final ObjectName BOOTER_OBJECT_NAME = Util.newObjectName( "amx-support:name=booter" );
    
    private AMXBooter( final MBeanServer mbeanServer)
    {
        mMBeanServer = mbeanServer;
        mObjectName = BOOTER_OBJECT_NAME;
        mDomainRootObjectName = null;
        
        if ( mMBeanServer.isRegistered(mObjectName) )
        {
            throw new IllegalStateException();
        }
    }
    
    /**
        Create an instance of the booter.
     */
        public static synchronized AMXBooter
    create( final MBeanServer server )
    {
        final AMXBooter  booter = new AMXBooter(server);
        final ObjectName objectName = BOOTER_OBJECT_NAME;
        
        try
        {
            if ( ! server.registerMBean( booter, objectName ).getObjectName().equals(objectName) )
            {
                throw new IllegalStateException();
            }
        }
        catch( JMException e )
        {
            throw new IllegalStateException(e);
        }
        return booter;
    }
    
    /** ObjectName of the MBean which actually laods AMX MBeans; that MBean references this constant */
    public static final ObjectName STARTUP_OBJECT_NAME = Util.newObjectName( "amx-support:name=startup" );
    
    /**
        We need to dynamically load the AMX module.  HOW?  we can't depend on the amx-impl module.
        
        For now though, assume that a well-known MBean is available through other means via
        the amx-impl module.
     */
    public synchronized ObjectName bootAMX()
    {
        debug( "AMXBooter.bootAMX: assuming that amx-impl loads through other means" );
        if ( mDomainRootObjectName == null )
        {
            if ( ! mMBeanServer.isRegistered(STARTUP_OBJECT_NAME) )
            {
                throw new IllegalStateException( "AMX MBean not yet available: STARTUP_OBJECT_NAME" );
            }
            
            try
            {
                mDomainRootObjectName = (ObjectName)mMBeanServer.invoke( STARTUP_OBJECT_NAME, "startAMX", null, null);
            }
            catch( final JMException e )
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        
        return mDomainRootObjectName;
    }
    
    /**
        Return the JMXServiceURLs for all connectors we've loaded.
     */
        public JMXServiceURL[]
    getJMXServiceURLs()
    {
        final ObjectName queryPattern = Util.newObjectName("jmxremote:type=jmx-connector,*");
        final Set<ObjectName>  objectNames = mMBeanServer.queryNames(queryPattern, null);
        
        final List<JMXServiceURL> urls = new ArrayList<JMXServiceURL>();
        for( final ObjectName objectName : objectNames )
        {
            try
            {
                urls.add( (JMXServiceURL)mMBeanServer.getAttribute( objectName, "Address" ) );
            }
            catch( JMException e )
            {
                e.printStackTrace();
                // ignore
            }
        }
        
        return urls.toArray( new JMXServiceURL[urls.size()] );
    }
}



















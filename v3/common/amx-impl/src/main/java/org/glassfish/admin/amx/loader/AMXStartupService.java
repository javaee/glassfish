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
package org.glassfish.admin.amx.loader;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;
import javax.management.MBeanServerInvocationHandler;
import javax.management.remote.JMXServiceURL;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;


import org.glassfish.api.Startup;
import org.glassfish.api.Async;


import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.TimingDelta;

import com.sun.enterprise.management.mbeanserver.AppserverMBeanServerFactory;

import org.glassfish.admin.amx.util.SingletonEnforcer;

/**
    Startup service that waits for AMX to be pinged to load.  At startup, it registers
    itself as an MBean after first loading a JMXXConnector so that the outside world can
    "talk" to it. This initial sequence is very fast (~20ms), but does not load any
    AMX MBeans, not even DomainRoot.
    <p>
    Later, the {@link #startAMX} method can be invoked on the MBean to cause AMX
    to load all the AMX MBeans.
 */
@Service
@Async
public final class AMXStartupService
    implements  Startup,
                org.jvnet.hk2.component.PostConstruct,
                AMXStartupServiceMBean
{
    private static void debug( final String s ) { System.out.println(s); }
    
    @Inject(name=AppserverMBeanServerFactory.OFFICIAL_MBEANSERVER)
    private MBeanServer mMBeanServer;
    
    @Inject
    private AMXConfigRegistrar mConfigRegistrar;
    
    private static final ObjectName OBJECT_NAME = JMXUtil.newObjectName( "amx-support:name=startup" );
    
    public AMXStartupService()
    {
    }
    
    public void postConstruct()
    {
        SingletonEnforcer.register( this.getClass(), this );
        final TimingDelta delta = new TimingDelta();
        if ( mMBeanServer == null ) throw new Error( "AMXStartup: null MBeanServer" );
        if ( mConfigRegistrar == null ) throw new Error( "AMXStartup: null AMXConfigRegistrar" );
        
        try
        {
            mMBeanServer.registerMBean( this, OBJECT_NAME );
        }
        catch( JMException e )
        {
            throw new Error(e);
        }
        
        StartAMX.init(mMBeanServer, mConfigRegistrar);
        
        // nothing to talk to if the connectors aren't started!
        StartAMX.getInstance().startConnectors();
        
        //debug( "Initialized (async) AMX Startup service in " + delta.elapsedMillis() + " ms " );
    }
    
        public synchronized ObjectName
    getDomainRootObjectName()
    {
        try
        { 
            // might not be ready yet
            return Util.getExtra(ProxyFactory.getInstance( mMBeanServer ).getDomainRoot()).getObjectName();
        }
        catch( Exception e )
        {
            return null;
        }
    }
    
    public JMXServiceURL getJMXServiceURL()
    {
        return StartAMX.getInstance().getJMXServiceURL();
    }
    
        public static AMXStartupServiceMBean
    getAMXStartupServiceMBean( final MBeanServer mbs )
    {
        AMXStartupServiceMBean ss = null;
        
        if ( mbs.isRegistered( OBJECT_NAME ) )
        {
            ss = (AMXStartupServiceMBean)
                MBeanServerInvocationHandler.newProxyInstance( mbs, OBJECT_NAME, AMXStartupServiceMBean.class, false);
        }
        return ss;
    }
    
         public static ObjectName
    invokeStartAMX(final MBeanServer mbs )
    {
        return getAMXStartupServiceMBean(mbs).startAMX();
    }
    
        public synchronized ObjectName
    startAMX()
    {
        final TimingDelta delta = new TimingDelta();

        StartAMX.getInstance().startAMX();
        
        final DomainRoot domainRoot = ProxyFactory.getInstance( mMBeanServer ).getDomainRoot();
        domainRoot.waitAMXReady();
        
        debug( "AMXStartupService: Loaded AMX MBeans in " + delta.elapsedMillis() + " ms " );
        return getDomainRootObjectName();
    }
    
    public Startup.Lifecycle getLifecycle() { return Startup.Lifecycle.SERVER; }
}





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

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.TimingDelta;
import org.glassfish.admin.amx.config.AMXConfigRegistrar;
import org.glassfish.admin.amx.util.SingletonEnforcer;
import org.glassfish.admin.mbeanserver.AppserverMBeanServerFactory;
import org.glassfish.api.Async;
import org.glassfish.api.Startup;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import org.glassfish.admin.mbeanserver.AMXBooter;

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
                org.jvnet.hk2.component.PreDestroy,
                AMXStartupServiceMBean
{
    private static void debug( final String s ) { System.out.println(s); }
    
    @Inject//(name=AppserverMBeanServerFactory.OFFICIAL_MBEANSERVER)
    private MBeanServer mMBeanServer;
    
    @Inject
    private AMXConfigRegistrar mConfigRegistrar;
    
    public AMXStartupService()
    {
    }
    
    private static ObjectName getObjectName()
    {
        return AMXBooter.STARTUP_OBJECT_NAME;
    }
    
    public void postConstruct()
    {
        SingletonEnforcer.register( this.getClass(), this );
        final TimingDelta delta = new TimingDelta();
        if ( mMBeanServer == null ) throw new Error( "AMXStartup: null MBeanServer" );
        if ( mConfigRegistrar == null ) throw new Error( "AMXStartup: null AMXConfigRegistrar" );
        
        try
        {
            mMBeanServer.registerMBean( this, getObjectName() );
        }
        catch( JMException e )
        {
            throw new Error(e);
        }
        
        StartAMX.init(mMBeanServer, mConfigRegistrar);
        
        // nothing to talk to if the connectors aren't started!
        //StartAMX.getInstance().startConnectors();
        
        //debug( "Initialized (async) AMX Startup service in " + delta.elapsedMillis() + " ms " );
    }

    public void preDestroy() {
        StartAMX.stopAMX();
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
    
    public JMXServiceURL[] getJMXServiceURLs()
    {
        try
        {
            return (JMXServiceURL[])mMBeanServer.getAttribute( AMXBooter.BOOTER_OBJECT_NAME, "JMXServiceURLs" );
        }
        catch ( final JMException e )
        {
            throw new RuntimeException(e);
        }
    }
    
        public static AMXStartupServiceMBean
    getAMXStartupServiceMBean( final MBeanServer mbs )
    {
        AMXStartupServiceMBean ss = null;
        
        if ( mbs.isRegistered( getObjectName() ) )
        {
            ss = AMXStartupServiceMBean.class.cast(
                MBeanServerInvocationHandler.newProxyInstance( mbs, getObjectName(), AMXStartupServiceMBean.class, false));
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





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
package org.glassfish.admin.amx.impl.loader;

import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.base.QueryMgr;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.core.proxy.AMXBooter;

import org.glassfish.admin.amx.util.TimingDelta;
import org.glassfish.admin.amx.util.FeatureAvailability;
import org.glassfish.admin.amx.impl.util.SingletonEnforcer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.remote.JMXServiceURL;

import java.util.Set;
import java.util.Collection;

import org.glassfish.admin.amx.base.MBeanTracker;
import org.glassfish.admin.amx.base.MBeanTrackerMBean;
import org.glassfish.admin.amx.impl.util.ImplUtil;
import org.glassfish.admin.amx.impl.util.InjectedValues;

import org.glassfish.admin.amx.impl.util.Issues;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.mbeanserver.PendingConfigBeans;
import org.glassfish.admin.mbeanserver.BooterNewMBean;
import org.glassfish.admin.mbeanserver.AMXLoader;
import org.glassfish.admin.mbeanserver.AMXStartupServiceNewMBean;
import org.jvnet.hk2.component.Habitat;

/**
    Startup service that waits for AMX to be pinged to load.  At startup, it registers
    itself as an MBean after first loading a JMXXConnector so that the outside world can
    "talk" to it. This initial sequence is very fast (~20ms), but does not load any
    AMX MBeans, not even DomainRoot.
    <p>
    Later, the {@link #startAMX} method can be invoked on the MBean to cause AMX
    to load all the AMX MBeans.
    @see org.glassfish.admin.mbeanserver.AMXStartupServiceNewMBean
 */
@Service
public final class AMXStartupServiceNew
    implements  org.jvnet.hk2.component.PostConstruct,
                org.jvnet.hk2.component.PreDestroy,
                AMXStartupServiceNewMBean
{
    private static void debug( final String s ) { System.out.println(s); }
    
    @Inject
    Habitat  mHabitat;
    
    @Inject
    InjectedValues  mInjectedValues;
    
    @Inject//(name=AppserverMBeanServerFactory.OFFICIAL_MBEANSERVER)
    private MBeanServer mMBeanServer;
    
    @Inject
    private volatile PendingConfigBeans mPendingConfigBeans;
    
    private volatile ObjectName      mAMXLoaderObjectName;
    
    private volatile MBeanTracker     mSupport;
    
    private static final ObjectName MBEAN_TRACKER_OBJECT_NAME = JMXUtil.newObjectName(AMXLoader.AMX3_SUPPORT_DOMAIN, "type=mbean-tracker");
    
    public static MBeanTrackerMBean getMBeanTracker( final MBeanServer server )
    {
        return MBeanServerInvocationHandler.newProxyInstance( server, MBEAN_TRACKER_OBJECT_NAME, MBeanTrackerMBean.class, false);
    }
    
    public AMXStartupServiceNew()
    {
        //debug( "AMXStartupServiceNew.AMXStartupServiceNew()" );
       // debug( this.getClass().getName() );
    }
    
    public void postConstruct()
    {
        final TimingDelta delta = new TimingDelta();
        
        SingletonEnforcer.register( this.getClass(), this );
        
        if ( mMBeanServer == null ) throw new Error( "AMXStartup: null MBeanServer" );
        if ( mPendingConfigBeans == null ) throw new Error( "AMXStartup: null mPendingConfigBeans" );
        
        try
        {
            // StandardMBean is required because interface and class are in different packages
           final StandardMBean mbean = new StandardMBean(this, AMXStartupServiceNewMBean.class);
           mMBeanServer.registerMBean( mbean, OBJECT_NAME);
           
           mSupport = new MBeanTracker();
           //final StandardMBean supportMBean = new StandardMBean(mSupport, MBeanTrackerMBean.class);
           mMBeanServer.registerMBean( mSupport, MBEAN_TRACKER_OBJECT_NAME );
        }
        catch( final Exception e )
        {
            e.printStackTrace();
            throw new Error(e);
        }
        //debug( "AMXStartupServiceNew.postConstruct(): registered: " + OBJECT_NAME );
        ImplUtil.getLogger().fine( "Initialized AMXStartupServiceNew in " + delta.elapsedMillis() + " ms, registered as " + OBJECT_NAME);
    }

    public void preDestroy() {
        ImplUtil.getLogger().info( "AMXStartupService.preDestroy(): stopping AMX" );
        unloadAMXMBeans();
    }
    
    public JMXServiceURL[] getJMXServiceURLs()
    {
        try
        {
            return (JMXServiceURL[])mMBeanServer.getAttribute( BooterNewMBean.OBJECT_NAME, "JMXServiceURLs" );
        }
        catch ( final JMException e )
        {
            throw new RuntimeException(e);
        }
    }

    /**
        Return a proxy to the AMXStartupServiceNew.
     */
        public static AMXStartupServiceNewMBean
    getAMXStartupServiceMBeanProxy( final MBeanServer mbs )
    {
        AMXStartupServiceNewMBean ss = null;
        
        if ( mbs.isRegistered( OBJECT_NAME ) )
        {
            ss = AMXStartupServiceNewMBean.class.cast(
                MBeanServerInvocationHandler.newProxyInstance( mbs, OBJECT_NAME, AMXStartupServiceNewMBean.class, false));
        }
        return ss;
    }


        public synchronized ObjectName
    getDomainRoot()
    {
        try
        { 
            // might not be ready yet
            return getDomainRootProxy().extra().objectName();
        }
        catch( Exception e )
        {
            // not there
        }
        return null;
    }
    
        DomainRoot
    getDomainRootProxy()
    {
        return ProxyFactory.getInstance( mMBeanServer ).getDomainRoot();
    }
    
        public ObjectName
    loadAMXMBeans()
    {
        ObjectName objectName = AMXBooter.findDomainRoot(mMBeanServer);
        if ( objectName == null )
        {
            try
            {
                objectName = _loadAMXMBeans();
            }
            catch( final Exception e )
            {
                debug( "AMXStartupServiceNew.loadAMXMBeans: " + e );
                throw new RuntimeException(e);
            }
        }
        return objectName;
    }
    
    private static final String AMX_LOADER_DEFAULT_OBJECTNAME    = AMXLoader.LOADER_PREFIX + "core";
    private static ObjectName LOADER_OBJECTNAME = null;
    
        public static synchronized ObjectName
    loadAMX( final MBeanServer mbeanServer )
    {
        if ( LOADER_OBJECTNAME == null )
        {
            final boolean inDAS = true;
            Issues.getAMXIssues().notDone( "LoadAMX.loadAMX(): determine if this is the DAS" );
            
            if ( inDAS )
            {
                final Loader loader = new Loader();
                
                final ObjectName tempObjectName  = JMXUtil.newObjectName( AMX_LOADER_DEFAULT_OBJECTNAME );
                
                try
                {
                    LOADER_OBJECTNAME  =
                        mbeanServer.registerMBean( loader, tempObjectName ).getObjectName();
                }
                catch( final Exception e)
                {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
        return LOADER_OBJECTNAME;
    }

    
        public synchronized ObjectName
    _loadAMXMBeans()
    {
        // loads the high-level AMX MBeans, like DomainRoot, QueryMgr, etc
        mAMXLoaderObjectName = loadAMX( mMBeanServer );
        //ImplUtil.getLogger().info( "AMXStartupServiceNew._loadAMXMBeans(): loaded name = " + mAMXLoaderObjectName);
        
        try
        {
            // Find and load any additional AMX subsystems
            final Collection<AMXLoader> loaders = mHabitat.getAllByContract(AMXLoader.class);
            //ImplUtil.getLogger().info( "AMXStartupServiceNew._loadAMXMBeans(): found this many loaders: " + loaders.size() );
            for( final AMXLoader loader : loaders )
            {
                try
                {
                    //ImplUtil.getLogger().info( "AMXStartupServiceNew._loadAMXMBeans: loading: "  + loader.getClass().getName() );
                    final ObjectName top = loader.loadAMXMBeans();
                    //ImplUtil.getLogger().info( "AMXStartupServiceNew._loadAMXMBeans: loaded: "  + loader.getClass().getName() );
                }
                catch( final Exception e )
                {
                    e.printStackTrace();
                   // ImplUtil.getLogger().info( "AMXStartupServiceNew._loadAMXMBeans: AMXLoader failed to load: " + e );
                }
            }
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }
        finally
        {
            FeatureAvailability.getInstance().registerFeature( FeatureAvailability.AMX_READY_FEATURE, getDomainRoot() );
        }
        
        return getDomainRoot();
    }
    
    public synchronized void unloadAMXMBeans()
    {
        if ( getDomainRoot() != null )
        { 
            final Collection<AMXLoader> loaders = mHabitat.getAllByContract(AMXLoader.class);
            for( final AMXLoader loader : loaders )
            {
                try
                {
                    loader.unloadAMXMBeans();
                }
                catch( final Exception e )
                {
                    ImplUtil.getLogger().info( "AMXLoader failed to unload: " + e );
                }
            }
            
            ImplUtil.unregisterAMXMBeans( getDomainRootProxy() );
        }
    }

   // public Startup.Lifecycle getLifecycle() { return Startup.Lifecycle.SERVER; }
}











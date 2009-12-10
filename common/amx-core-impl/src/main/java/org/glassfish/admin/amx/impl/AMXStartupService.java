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
package org.glassfish.admin.amx.impl;

import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;

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
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import org.glassfish.admin.amx.base.MBeanTracker;
import org.glassfish.admin.amx.base.MBeanTrackerMBean;
import org.glassfish.admin.amx.base.SystemInfo;
import org.glassfish.admin.amx.impl.mbean.DomainRootImpl;
import org.glassfish.admin.amx.impl.mbean.SystemInfoFactory;
import org.glassfish.admin.amx.impl.mbean.SystemInfoImpl;
import org.glassfish.admin.amx.impl.util.ImplUtil;
import org.glassfish.admin.amx.impl.util.InjectedValues;

import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.mbeanserver.AMXStartupServiceMBean;
import org.glassfish.api.amx.AMXLoader;
import org.jvnet.hk2.component.Habitat;

import org.glassfish.external.amx.BootAMXMBean;
import org.glassfish.external.amx.AMXGlassfish;
import org.glassfish.external.amx.MBeanListener;

import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;


import org.glassfish.admin.amx.util.jmx.stringifier.StringifierRegistryIniter;
import org.glassfish.admin.amx.util.stringifier.StringifierRegistryImpl;
import org.glassfish.admin.amx.util.stringifier.StringifierRegistryIniterImpl;

import org.glassfish.external.amx.AMXGlassfish;
import org.glassfish.external.amx.AMXUtil;

import com.sun.enterprise.config.serverbeans.AmxPref;

/**
An {@link AMXLoader} responsible for loading core amx MBeans
 */
@Service
public final class AMXStartupService
        implements org.jvnet.hk2.component.PostConstruct,
        org.jvnet.hk2.component.PreDestroy,
        AMXStartupServiceMBean
{
    private static void debug(final String s)
    {
        System.out.println(s);
    }

    @Inject
    Habitat mHabitat;

    @Inject
    InjectedValues mInjectedValues;

    @Inject
    private MBeanServer mMBeanServer;

    @Inject
    Events mEvents;

    private volatile MBeanTracker mMBeanTracker;

    public static MBeanTrackerMBean getMBeanTracker(final MBeanServer server)
    {
        return MBeanServerInvocationHandler.newProxyInstance(server, MBeanTrackerMBean.MBEAN_TRACKER_OBJECT_NAME, MBeanTrackerMBean.class, false);
    }

    public AMXStartupService()
    {
        new StringifierRegistryIniterImpl(StringifierRegistryImpl.DEFAULT);
        new StringifierRegistryIniter(StringifierRegistryImpl.DEFAULT);
    }

    private final class ShutdownListener implements EventListener
    {
        public void event(EventListener.Event event)
        {
            if (event.is(EventTypes.SERVER_SHUTDOWN))
            {
                shutdown();
            }
        }

    }

    private void shutdown()
    {
        ImplUtil.getLogger().fine("AMXStartupService: shutting down AMX MBeans");
        unloadAMXMBeans();
        
        final ObjectName allAMXPattern = AMXUtil.newObjectName(AMXGlassfish.DEFAULT.amxJMXDomain(), "*");
        final Set<ObjectName> remainingAMX = mMBeanServer.queryNames( allAMXPattern, null);
        if ( remainingAMX.size() != 0 )
        {
            ImplUtil.getLogger().log( java.util.logging.Level.WARNING, "AMXStartupService.shutdown: MBeans have not been unregistered: " + remainingAMX);
            try
            {
                Thread.sleep(1000);
            }
            catch( final InterruptedException e )
            {
            }
        }
        ImplUtil.getLogger().info("AMXStartupService: has been shut down and all AMX MBeans unregistered, remaining MBeans: " + mMBeanServer.queryNames( allAMXPattern, null));
    }

    public void postConstruct()
    {
        final TimingDelta delta = new TimingDelta();

        SingletonEnforcer.register(this.getClass(), this);

        if (mMBeanServer == null)
        {
            throw new Error("AMXStartup: null MBeanServer");
        }

        try
        {
            // StandardMBean is required because interface and class are in different packages
            final StandardMBean mbean = new StandardMBean(this, AMXStartupServiceMBean.class);
            mMBeanServer.registerMBean(mbean, OBJECT_NAME);

            mMBeanTracker = new MBeanTracker( AMXGlassfish.DEFAULT.amxJMXDomain() );
            
            final AmxPref amxPref = InjectedValues.getInstance().getAMXPrefs();
            mMBeanTracker.setEmitMBeanStatus( amxPref == null ? false : Boolean.valueOf(amxPref.getEmitRegisrationStatus()) );
        
            //final StandardMBean supportMBean = new StandardMBean(mMBeanTracker, MBeanTrackerMBean.class);
            mMBeanServer.registerMBean(mMBeanTracker, MBeanTrackerMBean.MBEAN_TRACKER_OBJECT_NAME);
        }
        catch (final Exception e)
        {
            ImplUtil.getLogger().log( Level.INFO, "Fatal error loading AMX", e);
            throw new Error(e);
        }
        //debug( "AMXStartupService.postConstruct(): registered: " + OBJECT_NAME );
        ImplUtil.getLogger().fine("Initialized AMXStartupServiceNew in " + delta.elapsedMillis() + " ms, registered as " + OBJECT_NAME);

        mEvents.register(new ShutdownListener());
    }

    public void preDestroy()
    {
        ImplUtil.getLogger().info("AMXStartupService.preDestroy(): stopping AMX");
        unloadAMXMBeans();
    }

    public JMXServiceURL[] getJMXServiceURLs()
    {
        try
        {
            return (JMXServiceURL[]) mMBeanServer.getAttribute(AMXGlassfish.DEFAULT.getBootAMXMBeanObjectName(), "JMXServiceURLs");
        }
        catch (final JMException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
    Return a proxy to the AMXStartupService.
     */
    public static AMXStartupServiceMBean getAMXStartupServiceMBeanProxy(final MBeanServer mbs)
    {
        AMXStartupServiceMBean ss = null;

        if (mbs.isRegistered(OBJECT_NAME))
        {
            ss = AMXStartupServiceMBean.class.cast(
                    MBeanServerInvocationHandler.newProxyInstance(mbs, OBJECT_NAME, AMXStartupServiceMBean.class, false));
        }
        return ss;
    }

    public synchronized ObjectName getDomainRoot()
    {
        try
        {
            // might not be ready yet
            return getDomainRootProxy().extra().objectName();
        }
        catch (Exception e)
        {
            // not there
        }
        return null;
    }

    DomainRoot getDomainRootProxy()
    {
        return ProxyFactory.getInstance(mMBeanServer).getDomainRootProxy(false);
    }

    public ObjectName loadAMXMBeans()
    {
        ObjectName objectName = AMXGlassfish.DEFAULT.domainRoot();
        if ( ! mMBeanServer.isRegistered(objectName) )
        {
            try
            {
                objectName = _loadAMXMBeans();
            }
            catch (final Exception e)
            {
                debug("AMXStartupServiceNew.loadAMXMBeans: " + e);
                throw new RuntimeException(e);
            }
        }
        return objectName;
    }

    /** also works as a loaded/not loaded flag: null if not yet loaded */
    private volatile ObjectName DOMAIN_ROOT_OBJECTNAME = null;

    private synchronized ObjectName loadDomainRoot()
    {
        if (DOMAIN_ROOT_OBJECTNAME != null)
        {
            return DOMAIN_ROOT_OBJECTNAME;
        }

        final DomainRootImpl domainRoot = new DomainRootImpl();
        DOMAIN_ROOT_OBJECTNAME = AMXGlassfish.DEFAULT.domainRoot();
        try
        {
            DOMAIN_ROOT_OBJECTNAME = mMBeanServer.registerMBean(domainRoot, DOMAIN_ROOT_OBJECTNAME).getObjectName();
            loadSystemInfo();
        }
        catch (final Exception e)
        {
            final Throwable rootCause = ExceptionUtil.getRootCause(e);
            ImplUtil.getLogger().log( Level.INFO, "Fatal error loading AMX DomainRoot", rootCause);
            throw new RuntimeException(rootCause);
        }

        return DOMAIN_ROOT_OBJECTNAME;
    }

    protected final ObjectName loadSystemInfo()
            throws NotCompliantMBeanException, MBeanRegistrationException,
                   InstanceAlreadyExistsException
    {
        final SystemInfoImpl systemInfo = SystemInfoFactory.createInstance(mMBeanServer);

        ObjectName systemInfoObjectName =
                ObjectNameBuilder.buildChildObjectName(mMBeanServer, DOMAIN_ROOT_OBJECTNAME, SystemInfo.class);

        systemInfoObjectName = mMBeanServer.registerMBean(systemInfo, systemInfoObjectName).getObjectName();

        return systemInfoObjectName;
    }

    /** run each AMXLoader in its own thread */
    private static final class AMXLoaderThread extends Thread
    {
        private final AMXLoader mLoader;

        private volatile ObjectName mTop;

        private final CountDownLatch mLatch;

        public AMXLoaderThread(final AMXLoader loader)
        {
            mLoader = loader;
            mLatch = new CountDownLatch(1);
        }

        public void run()
        {
            try
            {
                ImplUtil.getLogger().fine("AMXStartupServiceNew.AMXLoaderThread: loading: " + mLoader.getClass().getName());
                mTop = mLoader.loadAMXMBeans();
                //ImplUtil.getLogger().info( "AMXStartupService.AMXLoaderThread: loaded: "  + mLoader.getClass().getName() );
            }
            catch (final Exception e)
            {
                ImplUtil.getLogger().log( Level.INFO, "AMXStartupServiceNew._loadAMXMBeans: AMXLoader failed to load", e);
            }
            finally
            {
                mLatch.countDown();
            }
        }

        public ObjectName waitDone()
        {
            try
            {
                mLatch.await();
            }
            catch (InterruptedException e)
            {
            }
            return mTop;
        }

        public ObjectName top()
        {
            return mTop;
        }
    }

    class MyListener extends MBeanListener.CallbackImpl {
        @Override
        public void mbeanRegistered(final ObjectName objectName, final MBeanListener listener) {
            super.mbeanRegistered(objectName,listener);
            // verification code, nothing more to do
            //debug( "MBean registered: " + objectName );
        }
    }

    public synchronized ObjectName _loadAMXMBeans()
    {
        // self-check important MBeans
        final AMXGlassfish amxg = AMXGlassfish.DEFAULT;
        final MBeanListener<MyListener> bootAMXListener = amxg.listenForBootAMX(mMBeanServer, new MyListener() );
        
        final MBeanListener<MyListener> domainRootListener = amxg.listenForDomainRoot(mMBeanServer, new MyListener() );
        
        // loads the high-level AMX MBeans, like DomainRoot, QueryMgr, etc
        loadDomainRoot();
        FeatureAvailability.getInstance().registerFeature(FeatureAvailability.AMX_CORE_READY_FEATURE, getDomainRoot());
        ImplUtil.getLogger().fine("AMXStartupServiceNew: AMX core MBeans are ready for use, DomainRoot = " + getDomainRoot());

        try
        {
            // Find and load any additional AMX subsystems
            final Collection<AMXLoader> loaders = mHabitat.getAllByContract(AMXLoader.class);
            //ImplUtil.getLogger().info( "AMXStartupService._loadAMXMBeans(): found this many loaders: " + loaders.size() );
            final AMXLoaderThread[] threads = new AMXLoaderThread[loaders.size()];
            int i = 0;
            for (final AMXLoader loader : loaders)
            {
                threads[i] = new AMXLoaderThread(loader);
                threads[i].start();
                ++i;
            }
            // don't mark AMX ready until all loaders have finished
            for (final AMXLoaderThread thread : threads)
            {
                thread.waitDone();
            }
        }
        catch (Throwable t)
        {
            ImplUtil.getLogger().log( Level.INFO, "_loadAMXMBeans", t);
        }
        finally
        {
            FeatureAvailability.getInstance().registerFeature(FeatureAvailability.AMX_READY_FEATURE, getDomainRoot());
            ImplUtil.getLogger().info("AMXStartupServiceNew: AMX ready for use, DomainRoot = " + getDomainRoot());
        }
        
        // sanity-check (self-test) our listeners
        if ( bootAMXListener.getCallback().getRegistered() == null )
        {
            throw new IllegalStateException( "BootAMX listener was not called" );
        }
        if ( domainRootListener.getCallback().getRegistered() == null )
        {
            throw new IllegalStateException( "DomainRoot listener was not called" );
        }

        return getDomainRoot();
    }

    public synchronized void unloadAMXMBeans()
    {
        if (getDomainRoot() != null)
        {
            final Collection<AMXLoader> loaders = mHabitat.getAllByContract(AMXLoader.class);
            for (final AMXLoader loader : loaders)
            {
                if (loader == this)
                {
                    continue;
                }

                try
                {
                    loader.unloadAMXMBeans();
                }
                catch (final Exception e)
                {
                    ImplUtil.getLogger().info("AMXLoader failed to unload: " + e);
                }
            }

            ImplUtil.unregisterAMXMBeans(getDomainRootProxy());
        }
    }

    // public Startup.Lifecycle getLifecycle() { return Startup.Lifecycle.SERVER; }
}











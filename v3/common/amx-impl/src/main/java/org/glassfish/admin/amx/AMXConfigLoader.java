package org.glassfish.admin.amx;

import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;
import javax.management.ObjectInstance;

import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;

import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

import java.lang.reflect.Proxy;


import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.util.misc.RunnableBase;


/**
 * @author llc
 */
public final class AMXConfigLoader
{
    private static void debug( final String s ) { System.out.println(s); }
    
    private volatile MBeanServer mMBeanServer;
    
    private final LinkedBlockingQueue<ConfigBean> mPendingConfigBeans = new LinkedBlockingQueue<ConfigBean>();
    
    private AMXConfigLoaderThread mLoaderThread = null;
    
    @SuppressWarnings("unchecked")
    final ConfigBean asConfigBean( final Object o )
    {
        return (o instanceof ConfigBean) ? (ConfigBean)o : null;
    }
    
        public
    AMXConfigLoader()
    {
    }

    /**
        No items will be processd until {@link #start} is called.
     */
        protected void
    handleConfigBean( final ConfigBean cb )
    {
        //debug( "### handleConfigBean" );
        mPendingConfigBeans.add( cb );
    }
    
    /**
        Enable registration of MBeans, queued until now.
     */
        public synchronized void
    start( final MBeanServer server )
    {
        if ( mLoaderThread == null )
        {
            mMBeanServer    = server;
            
            mLoaderThread   = new AMXConfigLoaderThread( mPendingConfigBeans );
            mLoaderThread.submit( RunnableBase.HowToRun.RUN_IN_SEPARATE_THREAD );
        }
    }
    
    private final class AMXConfigLoaderThread extends RunnableBase
    {
        private final LinkedBlockingQueue<ConfigBean> mQueue;
        volatile boolean    mQuit = false;
        
        AMXConfigLoaderThread( final LinkedBlockingQueue<ConfigBean> queue )
        {
            super( "AMXConfigLoader.AMXConfigLoaderThread", null );
            mQueue = queue;
        }
        
        void quit() { mQuit = true; }
        
            protected void
        doRun() throws Exception
        {
            while ( ! mQuit )
            {
                final ConfigBean cb = mQueue.take();
                
                try 
                {
                    registerConfigBeanAsMBean( cb );
                }
                catch( Throwable t )
                {
                    t.printStackTrace();
                }
            }
        }
    }
    

    /**
     */
        protected ObjectName
    registerConfigBeanAsMBean( final ConfigBean cb )
    {
        ObjectName objectName = cb.getObjectName();
        if ( objectName != null )
        {
            throw new IllegalArgumentException( "ConfigBean " + cb + " already registered as " + objectName );
        }
        
        final Class<? extends ConfigBeanProxy> configuredClass = cb.getProxyType();
        // should be getting @AMXInfo, and using meta annotation
        final AMXConfigInfo amxConfigInfo = configuredClass.getAnnotation( AMXConfigInfo.class );
        if ( amxConfigInfo == null )
        {
            throw new IllegalArgumentException( "Missing @AMXConfigInfo" );
        }
        final Class<? extends AMXConfig> amxInterface = amxConfigInfo.amxInterface();
        
        // check class itself first for metadata, if missing find it from the AMXConfigInfo instead
        AMXMBeanMetadata metadata    = configuredClass.getAnnotation( AMXMBeanMetadata.class );
        if ( metadata == null )
        {
            // the default
            metadata = AMXConfigInfo.class.getAnnotation( AMXMBeanMetadata.class );
        }
        
        final AMXObjectNameInfo objectNameInfo = configuredClass.getAnnotation( AMXObjectNameInfo.class );
        if ( objectNameInfo == null )
        {
            throw new IllegalArgumentException( "Missing @AMXObjectNameInfo" );
        }
        
        // debug( "Preparing ConfigBean for registration with ObjectNameInfo = " + objectNameInfo.toString() + ", AMXMBeanMetaData = " + metadata );

        objectName = buildObjectName( cb, objectNameInfo );
    
        try
        {
            final ObjectInstance instance = mMBeanServer.registerMBean( new Dummy(cb), objectName );
            objectName = instance.getObjectName();
            cb.setObjectName( objectName );
            debug( "REGISTERED MBEAN: " + JMXUtil.toString(objectName) +
                " using ObjectNameInfo = " + objectNameInfo.toString() +
                ", AMXMBeanMetaData = " + metadata + "\n");
        }
        catch( final JMException e )
        {
            debug( ExceptionUtil.toString(e) );
        }

        return objectName;
    }
    
        ObjectName
    buildObjectName(
        final ConfigBean b,
        final AMXObjectNameInfo info)
    {
        final String name = b.rawAttribute( info.nameHint() );
        
        final String nameString = "amx:j2eeType=" + info.j2eeType() + ",name=" + name;
        
        return JMXUtil.newObjectName( nameString );
    }
}



















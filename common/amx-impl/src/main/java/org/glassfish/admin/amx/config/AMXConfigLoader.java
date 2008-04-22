/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.amx.config;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import org.glassfish.admin.amx.loader.AMXConfigVoid;
import org.glassfish.admin.amx.logging.AMXMBeanRootLogger;
import org.glassfish.admin.amx.mbean.AMXImplBase;
import org.glassfish.admin.amx.mbean.Delegate;
import org.glassfish.admin.amx.mbean.MBeanImplBase;
import org.glassfish.admin.amx.util.AMXConfigInfoResolver;
import org.glassfish.admin.amx.util.FeatureAvailability;
import org.glassfish.admin.amx.util.ObjectNames;
import org.glassfish.api.amx.AMXConfigInfo;
import org.glassfish.api.amx.AMXMBeanMetadata;
import org.jvnet.hk2.config.*;

import javax.management.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;


/**
 * @author llc
 */
public final class AMXConfigLoader extends MBeanImplBase
    implements AMXConfigLoaderMBean, TransactionListener
{
    private static void debug( final String s ) {
        Logger.getAnonymousLogger().fine(s);
    }
    
    private volatile MBeanServer mMBeanServer;
    
    private final LinkedBlockingQueue<Job> mPendingConfigBeans = new LinkedBlockingQueue<Job>();
    
    private AMXConfigLoaderThread mLoaderThread = null;
    
    private final Transactions  mTransactions = Transactions.get();
    private final Logger mLogger = AMXMBeanRootLogger.getInstance();

        public
    AMXConfigLoader()
    {
        if ( mTransactions == null ) throw new IllegalStateException();
        
        mTransactions.addTransactionsListener( this );
    }
 
    
        private boolean
    unregisterOneMBean( final ObjectName objectName )
    {
        boolean success = false;
        try
        {
            debug( "unregisterOneMBean: " + objectName );
            if ( mMBeanServer.isRegistered(objectName) )
            {
                mMBeanServer.unregisterMBean( objectName );
            }
        }
        catch( Exception e )
        {
            debug( "unregisterOneMBean: " + objectName + " FAILED: " + ExceptionUtil.toString(e));
        }
        return success;
    }
    
        private void
    unregisterMBeans( final ObjectName objectName )
    {
        if ( objectName == null) throw new IllegalArgumentException();
        
        final AMX root = ProxyFactory.getInstance(mMBeanServer).getProxy( objectName );
        
        if ( root instanceof Container )
        {
            // unregister all Containees first
            final Set<AMX>  all = ((Container)root).getContaineeSet();
            for( final AMX amx : all )
            {
                unregisterMBeans( Util.getObjectName(amx) );
            }
        }
        
        unregisterOneMBean( objectName );
    }
    
        private void
    configBeanRemoved( final ConfigBean cb )
    {
        if ( cb.getObjectName() != null)
        {
            unregisterMBeans( cb.getObjectName() );
        }
        else
        {
            // might or might not be there, but make sure it's gone!
            mPendingConfigBeans.remove( cb );
        }
    }
    
        private void
    issueAttributeChange(
        final ConfigBean cb,
        final String     attrName,
        final Object     oldValue,
        final Object     newValue,
        final long       whenChanged )
    {
        final ObjectName objectName = cb.getObjectName();
        if ( objectName == null )
        {
            throw new IllegalArgumentException( "Can't issue attribute change for null ObjectName for ConfigBean " + cb.getProxyType().getName() );
        }
    
        debug( "issueAttributeChange: " + attrName + ": {" + oldValue + " => " + newValue + "}");
        
        final AMXConfigImplBase amx = AMXConfigImplBase.class.cast( AMXImplBase.__getObjectRef__(mMBeanServer, objectName) );
        amx.issueAttributeChangeForXmlAttrName( attrName, oldValue, newValue, whenChanged );
    }
    
    private void sortAndDispatch(
        final List<PropertyChangeEvent> events,
        final long    whenChanged )
    {
debug( "AMXConfigLoader.sortAndDispatch: " + events.size() + " events" );
        final List<ConfigBean> newConfigBeans   = new ArrayList<ConfigBean>();
        final List<PropertyChangeEvent> remainingEvents = new ArrayList<PropertyChangeEvent>();

        //
        // Process all ADD and REMOVE events first, placing leftovers into 'remainingEvents'
        // We do this even if AMX is *not* running, because they new ConfigBeans need to go
        // into the queue for when and if AMX starts running.
        // 
        for ( final PropertyChangeEvent event : events) 
        {
            final Object oldValue = event.getOldValue();
            final Object newValue = event.getNewValue();
            final Object source   = event.getSource();
            final String propertyName = event.getPropertyName();
            
            if ( oldValue == null && newValue instanceof ConfigBeanProxy )
            {
                // ADD: a new ConfigBean was added
                final ConfigBeanProxy cbp = (ConfigBeanProxy)newValue;
                final ConfigBean cb = asConfigBean( ConfigBean.unwrap(cbp) );
                final Class<? extends ConfigBeanProxy> proxyClass = cb.getProxyType();
                debug( "AMXConfigLoader.sortAndDispatch: process new ConfigBean: " + proxyClass.getName() );
                final boolean doWait = amxIsRunning();
                handleConfigBean( cb, doWait );   // wait until registered
                newConfigBeans.add( cb );
            }
            else if ( newValue == null && oldValue instanceof ConfigBeanProxy && amxIsRunning() )
            {
                // REMOVE
                final ConfigBeanProxy cbp = (ConfigBeanProxy)oldValue;
                final ConfigBean cb = asConfigBean( ConfigBean.unwrap( cbp ) );
                debug( "AMXConfigLoader.sortAndDispatch: remove (recursive) ConfigBean: " + cb.getObjectName() );
                configBeanRemoved( cb );
            }
            else
            {
                remainingEvents.add( event );
            }
        }
        
        // we can't issue events if AMX is not running!
        if ( amxIsRunning() )
        {
            for ( final PropertyChangeEvent event : remainingEvents) 
            {
                final Object oldValue = event.getOldValue();
                final Object newValue = event.getNewValue();
                final Object source   = event.getSource();
                final String propertyName = event.getPropertyName();
                final String sourceString = (source instanceof ConfigBeanProxy) ? ConfigSupport.proxyType((ConfigBeanProxy)source).getName() : "" + source;
                
                debug( "AMXConfigLoader.sortAndDispatch (ATTR change): name = " + propertyName +
                        ", oldValue = " + oldValue + ", newValue = " + newValue + ", source = " + sourceString );
                if ( source instanceof ConfigBeanProxy )
                {
                    // CHANGE
                    final ConfigBeanProxy cbp = (ConfigBeanProxy)source;
                    final ConfigBean cb = asConfigBean( ConfigBean.unwrap( cbp ) );
                    final Class<? extends ConfigBeanProxy> proxyClass = ConfigSupport.proxyType(cbp);
                    
                    // change events without prior add
                    // we shouldn't have to check for this, but it's a bug in the caller: no even for
                    // new ConfigBean, but changes come along anyway
                    if ( cb.getObjectName() == null )
                    {
                        if ( ! newConfigBeans.contains(cb) )
                        {
                            debug( "AMXConfigLoader.sortAndDispatch: process new ConfigBean (WORKAROUND): " + proxyClass.getName() );
                            handleConfigBean( cb, false );
                            newConfigBeans.add( cb );
                        }
                    }
                    else
                    {
                        issueAttributeChange( cb, propertyName, oldValue, newValue, whenChanged);
                    }
                }
                else
                {
                    debug( "AMXConfigLoader.sortAndDispatch: WARNING: source is not a ConfigBean" );
                }
            }
        }
    }

        public void
    transactionCommited( final List<PropertyChangeEvent> changes)
    {
        //final PropertyChangeEvent[] changesArray = new PropertyChangeEvent[changes.size()];
        //changes.toArray( changesArray );
        sortAndDispatch( changes, System.currentTimeMillis() );
    }
    
    /*
    @Override
		protected void
	postRegisterHook( Boolean registrationDone )
	{	
        super.postRegisterHook( registrationDone );
        
		if ( registrationDone.booleanValue() )
		{
            mTransactions.addTransactionsListener( this );
		}
	}
    */
    
        public void
    handleNotification( final Notification notif, final Object handback)
    {
    }
    
    @Override
        protected void
	postDeregisterHook()
	{
        super.postDeregisterHook();
        mTransactions.removeTransactionsListener( this );
	}



    private static final class Job
    {
        final ConfigBean mConfigBean;
        final CountDownLatch mLatch;
        
        public Job( final ConfigBean configBean, final CountDownLatch latch )
        {
            mConfigBean = configBean;
            mLatch      = latch;
        }
        
        public void releaseLatch()
        {
            if ( mLatch != null )
            {
                mLatch.countDown();
            }
        }
    }
    
    /**
        No items will be processd until {@link #start} is called.
     */
        protected void
    handleConfigBean( final ConfigBean cb, final boolean waitDone )
    {
        if ( cb.getObjectName() == null)
        {
            final CountDownLatch  latch = waitDone ? new CountDownLatch(1) : null;
            final Job job = new Job( cb, latch);
            
            mPendingConfigBeans.add( job );
            
            if ( latch != null )
            {
                try
                {
                    latch.await();
                }
                catch( InterruptedException e )
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

        private static AMXConfigInfo
    getAMXConfigInfo( final ConfigBean cb )
    {
        final Class<? extends ConfigBeanProxy> theClass = cb.getProxyType();
        final AMXConfigInfo amxConfigInfo = theClass.getAnnotation( AMXConfigInfo.class );
        if ( amxConfigInfo == null )
        {
            throw new IllegalArgumentException( "ConfigBean has no @AMXConfigInfo: " + theClass.getName() );
        }
        return amxConfigInfo;
    }

     /**
        Internal nodes that don't get registered as MBeans throw a small monkey wrench
        into things; examples include &lt;configs> and &lt;resources> which are elements
        that contain only children, no Attributes of their own, and thus are not represented
        as MBeans.
     */
        private static ConfigBean
    getActualParent( final ConfigBean configBean )
    {
        ConfigBean parent = asConfigBean( configBean.parent() );
        if ( parent != null )
        {
            //debug( "config bean  " + configBean.getProxyType().getName() + " has parent " + configBean.parent().getProxyType().getName() );
            
            // if it has an ObjectName, then its a valid parent (this is the quick check).
            // If not, it might not yet have one; use the annotation
            final ObjectName parentObjectName    = parent.getObjectName();
            if ( parentObjectName == null )
            {
                final AMXConfigInfo amxConfigInfo = getAMXConfigInfo( parent );
                
                //debug( "amxInterface() for " + parent.getProxyType().getName() + " = " + amxConfigInfo.amxInterface().getName() );
                final AMXConfigInfoResolver resolver = new AMXConfigInfoResolver(amxConfigInfo);
                if ( resolver.amxInterface() == AMXConfigVoid.class )
                {
                    // parent is an internal node only, try its parent
                    parent = getActualParent( parent );
                }
            }
            else
            {
                // valid parent with an ObjectName
            }
        }
        else
        {
            if ( ! configBean.getProxyType().getName().endsWith( "Domain" ) )
            {
                debug( "WARNING: parent is null for " + configBean.getProxyType().getName() + " (bug in ConfigBeans)");
            }
        }
        
        return parent;
    }
    
        private static ObjectName
    getActualParentObjectName( final ConfigBean configBean )
    {
        ObjectName  parentObjectName = null;
        
        final ConfigBean parent = getActualParent( configBean );
        if ( parent != null )
        {
            parentObjectName    = parent.getObjectName();
        }
        
        return parentObjectName;
    }    
     /**
        @return a ConfigBean, or null if it's not a ConfigBean
     */
    @SuppressWarnings("unchecked")
    static ConfigBean asConfigBean( final Object o )
    {
        return (o instanceof ConfigBean) ? (ConfigBean)o : null;
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
            mLoaderThread.setDaemon(true);
            mLoaderThread.start();
        
            // Make the listener start listening
            final ObjectName objectName = JMXUtil.newObjectName( "amx-support", "name=amx-config-loader" );
            try
            {
                server.registerMBean( this, objectName );
            }
            catch( Exception e )
            {
                throw new RuntimeException(e);
            }
        }
    }
    
        private synchronized boolean
    amxIsRunning()
    {
        return mLoaderThread != null;
    }
    
    private final class AMXConfigLoaderThread extends Thread
    {
        private final LinkedBlockingQueue<Job> mQueue;
        volatile boolean    mQuit = false;
        
        AMXConfigLoaderThread( final LinkedBlockingQueue<Job> queue )
        {
            super( "AMXConfigLoader.AMXConfigLoaderThread" );
            mQueue = queue;
        }
        
        void quit() { mQuit = true; }
        
            private ObjectName
        registerOne( final Job job )
        {
            final ConfigBean cb = job.mConfigBean;
            
            ObjectName objectName = cb.getObjectName();
            try 
            {
                // If the ObjectName is null, then it hasn't been registered
                // Due to recursive registration of parents, we could encounter beans
                // that are parents, and thus already registered.
                if ( objectName == null )
                {
                    objectName = registerConfigBeanAsMBean( cb );
                }
            }
            catch( Throwable t )
            {
                t.printStackTrace();
            }
            finally
            {
                job.releaseLatch();
            }
            
            return objectName;
        }
        
        public void run()
        {
            try
            {
                doRun();
            }
            catch( Throwable t )
            {
                t.printStackTrace();
            }
        }
        
            protected void
        doRun() throws Exception
        {
            /*
               First pass *only*: 
               Note when we initially empty the queue; this signifies that
               AMX is "ready" for callers that just started it.
             */
            Job job = mQueue.take();  // block until first item is ready
            while ( (! mQuit) && job != null )
            {
                final ObjectName objectName = registerOne(job);
                //debug( "REGISTERED: " + objectName );
                job = mQueue.peek();  // don't block, loop exits when queue is first emptied
                if ( job != null )
                {
                    job = mQueue.take();
                }
            }
            
            FeatureAvailability.getInstance().registerFeature( FeatureAvailability.AMX_READY_FEATURE, Boolean.TRUE );
            
            // ongoing processing once initial queue has been emptied: blocking behavior
            while ( ! mQuit )
            {
                job = mQueue.take();
                registerOne(job);
            }
        }
    }
    
    /**
        Register the ConfigBean, first registering its parent, parent's parent, etc if not
        already present.
     */
        private ObjectName
    registerConfigBeanAsMBean( final ConfigBean cb )
    {
        ObjectName objectName = null;
        
        if ( getAMXConfigInfo(cb) != null )
        {
            final ConfigBean parentCB = getActualParent(cb);
            if ( parentCB != null && parentCB.getObjectName() == null )
            {
                //debug( "REGISTER parent first: " + parentCB.getProxyType().getName() );
                registerConfigBeanAsMBean( parentCB );
                //debug( "REGISTERED parent: " + parentCB.getProxyType().getName() + " as " + JMXUtil.toString(parentCB.getObjectName()) );
            }
           objectName =  _registerConfigBeanAsMBean( cb, parentCB );
           assert cb.getObjectName() != null;
        }
        else
        {
            debug( "NOTE: ConfigBean has no @AMXConfigInfo: " + cb.getProxyType().getName() + " (IGNORING)");
        }
        return objectName;
    }
    
         private AMXMBeanMetadata
    getAMXMBeanMetadata( final ConfigBean cb )
    {
        final Class<? extends ConfigBeanProxy> cbClass = cb.getProxyType();
        
        // check class itself first for metadata, if missing find it from the AMXConfigInfo instead
        AMXMBeanMetadata metadata    = cbClass.getAnnotation( AMXMBeanMetadata.class );
        if ( metadata == null )
        {
            // the default
            metadata = AMXConfigInfo.class.getAnnotation( AMXMBeanMetadata.class );
        }
        
        return metadata;
    }
    
    /**
        Parent must have been registered already.
     */
        private ObjectName
    _registerConfigBeanAsMBean(
        final ConfigBean cb,
        final ConfigBean parentCB )
    {
        final Class<? extends ConfigBeanProxy> cbClass = cb.getProxyType();
        
        ObjectName objectName = cb.getObjectName();
        if ( objectName != null )
        {
            throw new IllegalArgumentException( "ConfigBean " + cbClass.getName() + " already registered as " + objectName );
        }
        if ( parentCB != null && parentCB.getObjectName() == null )
        {
            throw new IllegalArgumentException( "ConfigBean parent " + parentCB.getProxyType().getName() +
                " must be registered first before child = " +cbClass.getName() );
        }
        
        final AMXConfigInfo amxConfigInfo = getAMXConfigInfo( cb );
        
        // don't process internal nodes like <configs>, <resources>, etc; these contain
        // nothing but child nodes.  Such nodes use AMXConfigVoid.
        final AMXConfigInfoResolver resolver = new AMXConfigInfoResolver(amxConfigInfo);
        final Class<? extends AMXConfig> amxInterface = resolver.amxInterface();
        if ( amxInterface != AMXConfigVoid.class )
        {
            // if the specified interface is the base interface AMXConfig, then
            // the resulting interface is a combination of AMXConfig and the interface of the ConfigBean
            final boolean autoInterface = amxInterface == AMXConfig.class;
            final Class<?> supplementaryIntf = autoInterface ? (cbClass.isInterface() ? cbClass : null) : null;
            
            final AMXMBeanMetadata metadata        = getAMXMBeanMetadata(cb);
            
            // debug( "Preparing ConfigBean for registration with ObjectNameInfo = " + objectNameInfo.toString() + ", AMXMBeanMetaData = " + metadata );

            objectName = buildObjectName( cb, resolver );
        
            objectName  = createAndRegister( cb, amxInterface, supplementaryIntf, objectName );
            debug( "REGISTERED MBEAN: " + JMXUtil.toString(objectName) + " ===> USING " +
                " AMXConfigInfo = " + amxConfigInfo.toString() +
                ", AMXMBeanMetaData = " + metadata + "\n");
        }
        
        return objectName;
    }
    
    /**
		@return the fully qualified type as required by AMX.FULL_TYPE
	 */
		protected static String
	getFullType( final ConfigBean cb, final ObjectName proposedObjectName )
	{
        String fullType = "";
        if ( cb != null )
        {
            ObjectName objectName = (proposedObjectName != null) ? proposedObjectName : cb.getObjectName();
            final String j2eeType = objectName.getKeyProperty(AMX.J2EE_TYPE_KEY);
            
            final ConfigBean parent = getActualParent( cb );
            if ( parent == null )
            {
                fullType = j2eeType;
            }
            else
            {
                fullType = getFullType(parent, null) + "." + j2eeType;
            }
            //debug( "Full type for " + cb.getObjectName() + " = " + fullType );
        }
        
		return( fullType );
	}

    private ObjectName createAndRegister(
        final ConfigBean cb,
        final Class<? extends AMXConfig>  amxInterface,
        final Class<?>  supplementaryIntf,
        final ObjectName objectNameIn )
    {
        ObjectName  objectName = objectNameIn;
        
        final String j2eeType = objectNameIn.getKeyProperty(AMX.J2EE_TYPE_KEY);
        final String fullType = getFullType(cb, objectName );

        //debug( "Full type for " + objectNameIn + " = " + fullType );
        
        final Delegate delegate = new DelegateToConfigBeanDelegate( cb );
        ObjectName parentObjectName = getActualParentObjectName( cb );
        
        if ( parentObjectName == null  )
        {
            if ( amxInterface == com.sun.appserv.management.config.DomainConfig.class )
            {
                parentObjectName = ObjectNames.getInstance().getDomainRootObjectName();
            }
            else
            {
                //debug( "WARNING: All ConfigBeans must have a parent!  No parent for " + cb.getProxyType().getName() );
                throw new IllegalArgumentException( "All AMXConfig MBeans must have a parent!  No parent for " + cb.getProxyType().getName() );
            }
        }
        
        final AMXConfigImplBase impl = new AMXConfigImplBase( j2eeType, fullType, parentObjectName, amxInterface, supplementaryIntf, delegate );
        
        try
        {
            final ObjectInstance instance = mMBeanServer.registerMBean( impl, objectNameIn );
            objectName = instance.getObjectName();
            cb.setObjectName( objectName );
        }
        catch( final JMException e )
        {
            debug( ExceptionUtil.toString(e) );
            objectName = null;
        }
        return objectName;
    }

        private String
    getJ2EETypeField( final Class<? extends AMXConfig>  amxInterface )
    {
        String j2eeType = null;
        try {
            j2eeType	= (String)ClassUtil.getFieldValue( amxInterface, "J2EE_TYPE" );
        }
        catch( Exception e )
        {
            // this is NOT OK: should be specified if something other than generic
            //debug( "No J2EE_TYPE field found in " + amxInterface.getName() );
            throw new IllegalArgumentException(e);
        }
        return j2eeType;
    }
        private String
    getJ2EEType(
        final ConfigBean cb,
        final AMXConfigInfoResolver info)
    {
        final Class<? extends AMXConfig> amxInterface = info.amxInterface();
        
        String j2eeType = null;
        // if a specific AMX interface was specified (not the base interface AMXConfig), 
        // use its J2EE_TYPE field preferentially
        if ( amxInterface != AMXConfig.class )
        {
            j2eeType = getJ2EETypeField( amxInterface );
        }
        if ( j2eeType == null )
        {
            // Use the value from the annotation
            j2eeType = info.j2eeType();
            
            // if the value is empty, derive one from the fully-qualified interface name
            if ( j2eeType.length() == 0 )
            {
                // don't allow "." in the type; it will confuse the "full type" attribute
                final String configInterfaceName = cb.getProxyType().getName().replace(".", "_");
                j2eeType = XTypes.PREFIX + "CFG-" + configInterfaceName;
                debug( "Using DERIVED j2eeType of " + j2eeType + " for " + configInterfaceName );
            }
        }
        assert j2eeType != null && j2eeType.startsWith( XTypes.PREFIX );
        
        if ( j2eeType == null )
        {
            throw new RuntimeException( "AMXConfigLoader.getJ2EEType: j2eeType is null" );
        }
        if ( ! j2eeType.startsWith( XTypes.PREFIX ) )
        {
            throw new RuntimeException( "AMXConfigLoader.getJ2EEType: j2eeType just start with " + XTypes.PREFIX);
        }
        
        return j2eeType;
    }
    
        public static String
    getName(
        final ConfigBean cb,
        final AMXConfigInfo infoIn)
    {
        String name = null;
        
        final AMXConfigInfo info = infoIn == null ? getAMXConfigInfo(cb) : infoIn;
        
        if ( info.singleton() )
        {
            name = AMX.NO_NAME;
        }
        else
        {
            name = cb.rawAttribute( info.nameHint() );
        }
        
        return name;
    }
    
        private static String
    whackIllegals( final String s )
    {
        final char sub = '_';
        String result = s.replace( ':', sub );
        
        result = result.replace( ',', sub );
        
        return result;
    }
    
        private ObjectName
    buildObjectName(
        final ConfigBean cb,
        final AMXConfigInfoResolver info )
    {
        final String j2eeType = getJ2EEType( cb, info );
        final String name     = whackIllegals( getName( cb, info.getAMXConfigInfo() ) );
        
        String parentProps = "";
        String domain = AMX.JMX_DOMAIN;
        final ConfigBean parent = getActualParent(cb);
        if ( parent != null )
        {
            final ObjectName parentObjectName = parent.getObjectName();
            if ( parentObjectName != null )
            {
                domain = parentObjectName.getDomain();
                
                // a child's ObjectName is parentJ2EEType=parentName,<all other parent properties>
                final String ancestorProps = Util.getAdditionalProps( parentObjectName );
                final AMXConfigInfo parentAMXConfigInfo = getAMXConfigInfo(parent);
                final String parentProp = parentAMXConfigInfo.omitAsAncestorInChildObjectName() ? "" : Util.getSelfProp( parentObjectName );
                
                parentProps = Util.concatenateProps( parentProp, ancestorProps );
            }
        }
        
        final String requiredProps = Util.makeRequiredProps( j2eeType, name );
        final String allProps = Util.concatenateProps( requiredProps, parentProps );
        
        final ObjectName objectName = Util.newObjectName( domain, allProps );

        return objectName;
    }
}














































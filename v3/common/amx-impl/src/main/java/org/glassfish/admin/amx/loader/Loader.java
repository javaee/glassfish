/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.admin.amx.loader;

import com.sun.appserv.management.util.jmx.JMXUtil;
import org.glassfish.admin.amx.mbean.DomainRootImpl;
import org.glassfish.admin.amx.util.FeatureAvailability;

import javax.management.*;

/**
	Implements loading of all MBean API MBeans WITHIN the DAS (Domain Admin Server).
 */
public final class Loader extends LoaderBase
{
	// private boolean			            mQueuedAll;
	//private List<LoaderOfOld>			mLoaders;
	// private Map<ObjectName,ObjectName>	mOldToNewObjectNames;
	//private LoaderRegThread             mRegThread;
	
    //private final Set<String> JMX_DOMAINS_OF_INTEREST;

	// private final DeferredRegistrationThread	mDeferredRegistrationThread;
	
		public
	Loader()
	{
        //JMX_DOMAINS_OF_INTEREST =  GSetUtil.newUnmodifiableStringSet( ObjectNames.kDefaultIASDomainName );
		//mOldToNewObjectNames	= Collections.synchronizedMap( new HashMap<ObjectName,ObjectName>() );
		
		//mQueuedAll	= false;
		
		//mLoaders	= null;
		
		//mRegThread      = null;
		
		//mDeferredRegistrationThread	= new DeferredRegistrationThread();
		//mDeferredRegistrationThread.start();
				
	}
	
	    protected void
	preRegisterHook()
	{
		//mRegThread	= new LoaderRegThread( this, mLogger );
		//mRegThread.start();
	}
    
    @Override
        protected final void
    postRegisterHook( final Boolean registrationSucceeded )
    {
        super.postRegisterHook( registrationSucceeded );
        
        if ( registrationSucceeded.booleanValue() )
        {
            FeatureAvailability.getInstance().registerFeature(
                FeatureAvailability.AMX_LOADER_FEATURE, getObjectName() );
        }
    }
    
		public void
	handleNotification(
		final Notification	notifIn, 
		final Object		handback) 
	{        
		final String	type	= notifIn.getType();
		
		if ( notifIn instanceof MBeanServerNotification )
		{
			final MBeanServerNotification	notif	= (MBeanServerNotification)notifIn;
			final ObjectName objectName	= notif.getMBeanName();
            
            if ( type == MBeanServerNotification.REGISTRATION_NOTIFICATION )
            {
                if ( JMXUtil.toString(objectName).equals( "amx:j2eeType=J2EEServer,name=server" ) )
                {
                    debug( "Loader.handleNotification:  REGISTER for: " + JMXUtil.toString(objectName) );
                }
            }
            else if ( type == MBeanServerNotification.UNREGISTRATION_NOTIFICATION )
            {
                //debug( "Loader.handleNotification:  UNREGISTER for: " + JMXUtil.toString(objectName) );
            }
            
            /*
			if ( JMX_DOMAINS_OF_INTEREST.contains( objectName.getDomain() ) &&
                    shouldSync( objectName ) )
			{
				final boolean	register	=
					type == MBeanServerNotification.REGISTRATION_NOTIFICATION;
                
                // put it at the end of the registration or unregistration queue
				mRegThread.enqueue( register, objectName );
			}
            */
		}
	}
	
	private static final long	WAIT_THRESHOLD_MILLIS	= 5 * 1000;	// 5 seconds
	
    // might not be needed; see dependent code
    private static final boolean    WAIT_FOR_REGISTRATION = false;
    
		public void
	handleMBeanRegistered( final ObjectName	oldObjectName )
		throws InstanceNotFoundException
	{
		trace( "handleMBeanRegistered: " + oldObjectName );
		
    /*
		if ( shouldSync( oldObjectName ) )
		{
			final long	start	= now();
			
            if ( WAIT_FOR_REGISTRATION )
            {
                while ( ! getMBeanServer().isRegistered( oldObjectName ) &&
                        (now() - start) < WAIT_THRESHOLD_MILLIS )
                {
                    mySleep( 50 );
                    debug( "SLEPT for 50ms waiting for " + oldObjectName );
                }
            }
			
			if ( ! getMBeanServer().isRegistered( oldObjectName ) )
			{
				trace( "Loader.handleMBeanRegistered: not found: " + JMXUtil.toString(oldObjectName) );
				throw new InstanceNotFoundException( JMXUtil.toString(oldObjectName) );
			}
			
			try
			{
				sync( oldObjectName );
			}
			catch( Exception e )
			{
				final Throwable	rootCause	= ExceptionUtil.getRootCause( e );
				if ( rootCause instanceof DeferRegistrationException )
				{
					mDeferredRegistrationThread.defer( oldObjectName );
				}
			}
		}
    */
	}
	
	/**
		Cascaded MBeans are problematic because some of them get registered before 
		MBeans that contain them.  For most MBeans this is a non-issue, but the workaround
		code in LoaderOfOldMonitor.MyOldTypes.oldTypeToJ2EEType() can't deal with this situation,
		thus this facility exists to allow retry of problematically-registered MBeans.
		
		The need will go away if the underlying monitoring MBeans are fixed so that the workaround
		code is no longer needed.
	private final class DeferredRegistrationThread extends Thread
	{
		private final List<DeferredItem>	mDeferredItems;
		private boolean		mSleeping;
		
		private final class DeferredItem
		{
			public long					mStartMillis;
			public final ObjectName		mObjectName;
			
				public
			DeferredItem( final ObjectName objectName )
			{
				mObjectName		= objectName;
				mStartMillis	= now();
			}
		};
		
		private final long  RETRY_INTERVAL_MILLIS   = 4000;
		private final long	MAX_DELAY_MILLIS	    = 1 * 30 * 1000;
		
			public
		DeferredRegistrationThread( )
		{
			mDeferredItems	= Collections.synchronizedList( new ArrayList<DeferredItem>() );
			
			mSleeping	= false;
		}
		
			private synchronized void
		internalDefer( final DeferredItem item )
		{
			// even though it's a synchronized List, we still need to do this because
			// takeAllItems() needs to be able to make an array the clearing the list,
			// which is two operations.
			synchronized( mDeferredItems )
			{
				mDeferredItems.add( item );
			}
		}
		
			public synchronized void
		defer( final ObjectName oldObjectName )
		{
			logFine( "Deferring registration for " + quote( oldObjectName ) );
			
			internalDefer( new DeferredItem( oldObjectName ) );
			
			if ( mSleeping )
			{
				this.interrupt();
			}
			
		}
		
			private DeferredItem[]
		takeAllItems()
		{
			synchronized( mDeferredItems )
			{
				final DeferredItem[]	items	= new DeferredItem[ mDeferredItems.size() ];
				mDeferredItems.toArray( items );
				mDeferredItems.clear();
				return( items );
			}
		}
		
			private ObjectName
		retry( final ObjectName oldObjectName )
		{
			return sync( oldObjectName );
		}
		
		
			private void
		retryItem( final DeferredItem	item)
		{
			final ObjectName	oldObjectName	= item.mObjectName;
			
			final String	prefix	= "DeferredRegistrationThread.retryItem: ";
			final long	elapsed	= now() - item.mStartMillis;
			try
			{
				final ObjectName	result	= retry( oldObjectName );
				
				final String msg    = prefix + "deferred registration SUCCEEDED after " +
					elapsed + " milliseconds for " + quote( oldObjectName ) +
					", amx ObjectName = " + quote( result );
			    
				getMBeanLogger().info( msg );
			}
			catch( Throwable t )
			{
				final Throwable	rootCause	= ExceptionUtil.getRootCause( t );
				
				if ( rootCause instanceof DeferRegistrationException )
				{
					if ( elapsed < MAX_DELAY_MILLIS )
					{
						logWarning( prefix +
						"deferred registration RETRY failed after " +
							elapsed + " milliseconds for " + quote( oldObjectName ) + " (DEFERRING AGAIN)" );
						internalDefer( item );
					}
					else
					{
						logWarning( prefix +
							"Deferred registration FAILED for " + quote( oldObjectName ) + 
							"after deferral of " + elapsed + " ms, ignoring MBean." );
					}
				}
				else
				{
					logWarning( prefix +
						"Deferred registration FAILED for " + quote( oldObjectName ) + 
						"due to Exception of type " + rootCause.getClass().getName() );
				}
			}
		}
		
			private void
		checkList()
		{
			final DeferredItem[]	items	= takeAllItems();
			
			logFine( "DeferredRegistrationThread.checkList: numItems = " + items.length );
					
			for( int i = 0; i < items.length; ++i )
			{
				final DeferredItem	item	= items[ i ];
				
				if ( getMBeanServer().isRegistered( item.mObjectName ) )
				{
					retryItem( item );
				}
				else
				{
					logInfo(
						"DeferredRegistrationThread.checkList: " +
						"MBean is no longer registered: " + quote( item.mObjectName ) );
				}
			}
		}
		
			public void
		run()
		{
			while ( true )
			{
				try
				{					
					getMBeanLogger().fine( "DeferredRegistrationThread.run: CHECKING LIST@" + now() );
					checkList();
					
					// force a delay for efficiency in batching deferred items
					final long	sleepMillis	= mDeferredItems.size() == 0 ? 60 * 1000 : RETRY_INTERVAL_MILLIS;
					getMBeanLogger().fine( "DeferredRegistrationThread.run: SLEEPING FOR: " + sleepMillis + "@" + now()  );
					mSleeping	= true;
					final boolean	interrupted	= mySleep( sleepMillis );
					mSleeping	= false;
				}
				catch( Throwable t )
				{
					getMBeanLogger().warning( "DeferredRegistrationThread.run: caught Throwable:\n" +
						ExceptionUtil.getStackTrace( t ) );
				}
			}
		}
	}
	 */
	
		public void
	handleMBeanUnregistered( final ObjectName	oldObjectName )
		throws InstanceNotFoundException, MBeanRegistrationException
	{
		trace( "handleMBeanUnregistered: " + oldObjectName );
		
        /*
		final ObjectName	newObjectName	=
			mOldToNewObjectNames.remove( oldObjectName );
			
		if ( newObjectName != null && getMBeanServer().isRegistered( newObjectName ) )
		{
		    debug( "unregistering: " + newObjectName + " corresponding to " + oldObjectName );
			getMBeanServer().unregisterMBean( newObjectName );
		}
        */
	}
	
    /*
		synchronized ObjectName
	registerNew(
		final Object		impl,
		final ObjectName	implObjectName,
		final ObjectName	oldObjectName )
		throws MBeanRegistrationException,
			InstanceAlreadyExistsException, NotCompliantMBeanException
	{
		//debug( "registering: ", JMXUtil.toString(implObjectName), " corresponding to ", JMXUtil.toString(oldObjectName) );
        
		final ObjectName	resultName	=
			getMBeanServer().registerMBean( impl, implObjectName ).getObjectName();
			
		mOldToNewObjectNames.put( oldObjectName, resultName );
		
		return( resultName );
	}
    */
	  
/*      
		private void
	addLoaders()
	{
		assert( getMBeanServer() != null );
		assert( getMBeanLogger() != null );
		
		final List<LoaderOfOld>	loaders	= new ArrayList<LoaderOfOld>();
		loaders.add( new LoaderOfOldConfig( this ) );
		loaders.add( new LoaderOfOld77( this ) );
		loaders.add( new LoaderOfOldMonitor( this ) );
		mLoaders	= Collections.unmodifiableList( loaders );
	}
		

		private boolean
	shouldSync( final ObjectName oldObjectName )
	{
        final String  jmxDomain   = oldObjectName.getDomain();
        final boolean applicable  = ObjectNames.kDefaultIASDomainName.equals( jmxDomain );
        
		return applicable ? (findLoaderOfOld( oldObjectName ) != null) : false;
	}
	
		private LoaderOfOld
	findLoaderOfOld( final ObjectName candidate )
	{
		LoaderOfOld	oldLoader	= null;
		
		for( final LoaderOfOld loo : mLoaders )
		{
			if ( loo.shouldSync( candidate ) )
			{
				oldLoader	= loo;
				break;
			}
		}
		return( oldLoader );
	}

    private final Object CONFIG_SYNC  = new Object();
    
		public  final ObjectName
	sync( final ObjectName	oldObjectName )
	{
		if ( ! mStarted )
		{
			throw new IllegalStateException();
		}
        
		if ( ! shouldSync( oldObjectName ) )
		{
			throw new IllegalArgumentException( oldObjectName.toString() );
		}
        
        ObjectName  result = null;
        *
            Synchronize for config; ConfigFactory can call sync() explicitly, and there
            is a race condition that can attempt duplicate registration with the
            one to be done or already done or in progress by handleMBeanRegistered().
         *
        final String category = oldObjectName.getKeyProperty( "category" );
        if ( category != null && category.equals( "config" ) )
        {
            synchronized( CONFIG_SYNC )
            {
               result = _sync( oldObjectName );
            }
        }
        else
        {
             result = _sync( oldObjectName );
        }
        return result;
    }
    
		private  ObjectName
	_sync( final ObjectName	oldObjectName )
	{
        if ( ! getMBeanServer().isRegistered( oldObjectName ) )
        {
			throw new RuntimeException( new InstanceNotFoundException() );
        }
        
        // out of order registration can result in trying to register the same MBean
        // more than once.
        synchronized( this )
        {
            ObjectName	result	= mOldToNewObjectNames.get( oldObjectName );
            if ( result == null )
            {
                try
                {
                    final LoaderOfOld	loaderOfOld	= findLoaderOfOld( oldObjectName );
                    
                    if ( loaderOfOld != null )
                    {
                        result	= loaderOfOld.syncWithOld( oldObjectName );
                        if ( result == null )
                        {
                            throw new IllegalArgumentException( oldObjectName.toString() );
                        }
                    }
                }
                catch( Exception e )
                {
                    final String msg    = ExceptionUtil.toString( e );
                    debug( msg );
                    getMBeanLogger().warning( msg );
                    
                    if ( e instanceof RuntimeException )
                    {
                        throw (RuntimeException)e;
                    }
                    else
                    {
                        throw new RuntimeException( e );
                    }
                }
            }
		
            return( result );
        }
	}
	
		private void
	queueAll()
	{
		for( final LoaderOfOld oldLoader : mLoaders )
		{
			final List<ObjectName>	oldObjectNames	= oldLoader.findAllOld();
			mRegThread.enqueue( true, oldObjectNames );
			
			getMBeanLogger().fine( "Loader: Queued " + oldObjectNames.size() +
				" MBeans for loader " + oldLoader.getClass().getName() );
		}
	}
*/
	
	
		protected void
	startHook()
	{
	    super.startHook();
	    
	    //addLoaders();
		//queueAll();
		//mQueuedAll	= true;
	}
	
		public boolean
	isStarted( )
	{
		//return super.isStarted() && mQueuedAll && mRegThread.isQueueEmpty();
        return super.isStarted();
	}
    
        public final void
    waitAll()
    {
        //mRegThread.waitAll();
        //debug( "waitAll() DONE" );
    }
	
	    protected Object
    createDomainRoot()
    {
        return new DomainRootImpl();
    }
    
	    public boolean
	isDAS()
	{
	    return true;
	}
	
/*
	    public ObjectName
	resyncAMXMBean( final ObjectName amx )
	    throws InstanceNotFoundException, MBeanRegistrationException
	{
	    if ( ! getMBeanServer().isRegistered( amx ) )
	    {
	        throw new InstanceNotFoundException();
	    }
	    if ( ! getAMXJMXDomainName().equals( amx.getDomain() ) )
	    {
	        throw new IllegalArgumentException( "" + amx );
	    }
	    
	    debug( "resyncAMXMBean: looking for matching delegate MBean" );
	    ObjectName    old    = null;
	    for( final ObjectName oldTemp : mOldToNewObjectNames.keySet() )
	    {
	        if ( mOldToNewObjectNames.get( oldTemp ).equals( amx ) )
	        {
	            old = oldTemp;
	            debug( "resyncAMXMBean: found matching delegate MBean: " + old );
	            break;
	        }
	    }
	    
	    if ( old == null )
	    {
	        throw new IllegalArgumentException( "" + amx );
	    }
	    
	    debug( "resyncAMXMBean: removing mapping from: " + old + " TO " + amx );
        mOldToNewObjectNames.remove( old );
	    debug( "resyncAMXMBean: unregistering: " + amx );
        getMBeanServer().unregisterMBean( amx );
	    debug( "resyncAMXMBean: handleMBeanRegistered: " + amx );
        handleMBeanRegistered( old );
	        
	    final ObjectName    newAMX = mOldToNewObjectNames.get( old );
	    assert( newAMX != null );
	    
	    debug( "resyncAMXMBean: new ObjectName: " + newAMX );
	    return newAMX;
	}
*/
}









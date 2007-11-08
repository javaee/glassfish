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
 
/*
 */

package com.sun.enterprise.management.support;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;
import java.io.IOException;

import java.lang.reflect.Proxy;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.MBeanServerNotification;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.NotCompliantMBeanException;

import javax.management.relation.MBeanServerNotificationFilter;


import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.util.jmx.stringifier.NotificationStringifier;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.jmx.MBeanProxyHandler;
import com.sun.appserv.management.util.jmx.MBeanServerConnectionSource;
import com.sun.appserv.management.util.jmx.stringifier.StringifierRegistryIniter;

import com.sun.appserv.management.util.misc.ExceptionUtil;

import com.sun.appserv.management.util.stringifier.StringifierRegistryIniterImpl;
import com.sun.appserv.management.util.stringifier.StringifierRegistryImpl;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.client.ProxyFactory;

import com.sun.enterprise.management.support.TypeInfos;

/**
	
 */
abstract class LoaderBase extends MBeanImplBase
	implements LoaderMBean, LoaderRegHandler
{
	protected volatile boolean    mStarted;
	protected final ObjectNames   mObjectNames;
	
		public
	LoaderBase()
	{
	    debug( "LoaderBase" );

	    final boolean   offline = false;
		BootUtil.init( false );
		
		mServer			= null;
		
		new StringifierRegistryIniterImpl( StringifierRegistryImpl.DEFAULT );
		new StringifierRegistryIniter( StringifierRegistryImpl.DEFAULT );
		
		mStarted	    = false;
		mObjectNames	= ObjectNames.getInstance( getAMXJMXDomainName() );
	}
    
		public String
	getAMXJMXDomainName()
	{
		return( BootUtil.getInstance().getAMXJMXDomainName() );
	}
	
	
		public String
	getAdministrativeDomainName()
	{
		return( BootUtil.getInstance().getAppserverDomainName() );
	}
	
	
		public void
	handleNotification(
		final Notification	notifIn, 
		final Object		handback) 
	{
	    /* nothing by default */
	}
	
		public void
	handleMBeanRegistered( final ObjectName	oldObjectName )
		throws InstanceNotFoundException
	{
	}
	
		public void
	handleMBeanUnregistered( final ObjectName	oldObjectName )
		throws InstanceNotFoundException, MBeanRegistrationException
	{
	}
	
		protected static long
	now()
	{
		return( System.currentTimeMillis() );
	}
	
		protected ObjectName
	loadSystemInfo( final MBeanServer server )
		throws NotCompliantMBeanException, MBeanRegistrationException,
		InstanceAlreadyExistsException
	{
	    debug( "loadSystemInfo" );
	    
		final BootUtil	bootUtil	= BootUtil.getInstance();
		
		final SystemInfoImpl	systemInfo	= new SystemInfoImpl( server, bootUtil );
		
		final ObjectName	tempName	= mObjectNames.getSingletonObjectName( systemInfo.J2EE_TYPE );
		
		final ObjectName objectName	= mServer.registerMBean( systemInfo, tempName ).getObjectName();
		
		debug( "loaded SystemInfo" );
		return( objectName );
	}
		
	    protected void
	preRegisterHook()
	{
	}
    
    
	    protected void
	postRegisterHook()
	{
	}
	
		public final ObjectName
	preRegister(
		final MBeanServer	server,
		final ObjectName	objectNameIn)
		throws Exception
	{
	    debug( "preRegister" );
		final ObjectName	superObjectName	= super.preRegister( server, objectNameIn );
		
		final String    domain  = BootUtil.getInstance().getAMXSupportJMXDomain();
		mSelfObjectName	= Util.newObjectName( domain, LOADER_NAME_PROPS );
			
		preRegisterHook();
		
		try
		{
			loadSystemInfo( server );
			
			final MBeanServerNotificationFilter filter	=
				new MBeanServerNotificationFilter();

            filter.enableAllObjectNames();
            
            if ( mServer != server )
            {
                throw new IllegalStateException();
            }
		
			JMXUtil.listenToMBeanServerDelegate( mServer, this, filter, null );
			
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
		
	    debug( "preRegister done: " + mSelfObjectName );
		return( mSelfObjectName );
	}
	
		public void
	postRegister( Boolean registrationDone )
	{
		super.postRegister( registrationDone );
		
		if ( registrationDone.booleanValue() )
		{
			initLOADER( getMBeanServer(), getObjectName() );
            
		}
		start();
        
        postRegisterHook();
	}

		protected boolean
	mySleep( final long millis )
	{
		boolean	interrupted	= false;
		
		try
		{
			Thread.sleep( millis );
		}
		catch( InterruptedException e )
		{
			Thread.interrupted();
			interrupted	= true;
		}
		
		return interrupted;
	}
	
		protected void
	startHook()
	{
	    // nothing 
	}
	
	
	private final class CheckStartedThread extends Thread
	{
	    public void CheckStartedThread()    {}
	    
	        public void
	    run()
	    {
	        final long   AMX_READY_SLEEP_DURATION  = 100;
	        
	        while ( ! isStarted() )
	        {
	            debug( "Waiting " + AMX_READY_SLEEP_DURATION + "ms for AMX to start");
	            sleepMillis( AMX_READY_SLEEP_DURATION );
	        }

            BootUtil.getInstance().setAMXReady( true );
	    }
	}
	

		public boolean
	isStarted( )
	{
		return( mStarted );
	}

	protected abstract Object    createDomainRoot();
	
	public  abstract boolean   isDAS();
    
		private final void
	loadDomainRoot()
	{
	    debug( "loadDomainRoot ENTER" );
	    final Object    domainRoot  = createDomainRoot();
	    if ( domainRoot != null )
	    {
    		ObjectName objectName	= mObjectNames.getDomainRootObjectName( );
    	    
    	    debug( "Registering DomainRoot, impl class = " + domainRoot.getClass().getName() );
    		try
    		{
    			objectName  = mServer.registerMBean( domainRoot, objectName ).getObjectName();
    	        debug( "Registered DomainRoot: " + objectName );
    		}
    		catch( final Exception e )
    		{
    	        final Throwable rootCause   = ExceptionUtil.getRootCause(e);
    	        debug( "Exception loading DomainRoot: " +
    	             rootCause + ", msg=" + rootCause.getMessage() );
    	        throw new RuntimeException( rootCause );
    		}
    	}
    	else
    	{
	        debug( "loadDomainRoot: NULL" );
    	}
    	
	    debug( "loadDomainRoot DONE" );
	}

		public final DomainRoot
	getDomainRoot()
	{
		final ProxyFactory    factory    =
				ProxyFactory.getInstance( getMBeanServer() );
		
		return( factory.getDomainRoot() ); 
	}
	
	private static LoaderMBean	LOADER	= null;
	
		private static void
	initLOADER( final MBeanServer server, final ObjectName loaderObjectName )
	{
		assert( LOADER == null );
		
		try
		{
			final MBeanServerConnectionSource conn	= new MBeanServerConnectionSource( server );
			final MBeanProxyHandler	handler	= new MBeanProxyHandler( conn, loaderObjectName );
			
			LOADER	= (LoaderMBean)
					Proxy.newProxyInstance( LoaderMBean.class.getClassLoader(), new Class[] { LoaderMBean.class }, handler);
		}
		catch( IOException e )
		{
			assert( false ) : "can't happen";
			throw new RuntimeException( e );
		}
	}
	
	
		public static LoaderMBean
	getLoader( final MBeanServer server )
	{
		return( LOADER );
	}
	
	
		protected void
	start()
	{
		synchronized( this )
		{
			if ( mStarted )
			{
				throw new IllegalArgumentException( "Can't start Loader twice" );
			}
			mStarted	= true;
		}
		
		new AMXDebugSupport( mServer );
		loadDomainRoot();
		
		startHook();
		
		(new CheckStartedThread()).start();
	}
	
	
	    public ObjectName
	sync( final ObjectName name)
	{
	    throw new UnsupportedOperationException( "sync" );
	}
	

    /**
        See com.sun.enterprise.admin.server.core.notifyAMXThatAdminServiceIsReady()
        Make a [synchronous] call to AMX. Semantics are that this should
        be a quick call, not a long-running one.
     */
	    public void
	adminServiceReady()
	{
	    // nothing by default
	}
	
	    public ObjectName
	resyncAMXMBean( final ObjectName amx )
	    throws InstanceNotFoundException, MBeanRegistrationException
	{
	    // do nothing
	    return amx;
	}
}









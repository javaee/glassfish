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
package org.glassfish.admin.amx.mbean;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.NotificationService;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.OverflowHandler;

import javax.management.*;
import java.io.Serializable;
import java.util.*;


/**
 */
public final class NotificationServiceImpl extends AMXNonConfigImplBase
	implements NotificationListener, NotificationEmitter
{
	private Map<ObjectName,NotificationFilter>	mListenees;
	private final Object	        mUserData;
	
	private final Map<ObjectName,NotificationFilter> mIncludePatterns;
	
	private final Map<Object,NotificationBuffer>		mBuffers;
	
		public
	NotificationServiceImpl(
        final ObjectName parentObjectName,
		final Object	userData,
		final int		bufferSize  )
	{
        super( NotificationService.J2EE_TYPE, NotificationService.J2EE_TYPE, parentObjectName, NotificationService.class, null );
        
		if ( userData == null || !(userData instanceof Serializable) )
		{
			throw new IllegalArgumentException();
		}
		
		mUserData	= userData;

		mListenees		= Collections.synchronizedMap( new HashMap<ObjectName,NotificationFilter>() );
		
		mBuffers		= Collections.synchronizedMap( new HashMap<Object,NotificationBuffer>() );
		
		mIncludePatterns	= new HashMap<ObjectName,NotificationFilter>();
	}
	
		public final Object
	getUserData()
	{
		return( mUserData );
	}
	
	private final String[]	NOTIF_TYPES	= new String[]
	{
		NotificationService.BUFFER_OVERFLOW_NOTIFICATION_TYPE,
	};
	
		public MBeanNotificationInfo[]
	getNotificationInfo()
	{
		final MBeanNotificationInfo	info	= new MBeanNotificationInfo(
			NOTIF_TYPES,
			Notification.class.getName(),
			"" );
		final MBeanNotificationInfo[]	selfInfos	= new MBeanNotificationInfo[] { info };
		
		return( JMXUtil.mergeMBeanNotificationInfos( super.getNotificationInfo(), selfInfos ) );
	}

	/** 
	 */
		protected void
	issueBufferOverflowNotification( final Notification	oldNotif )
	{
		if ( shouldEmitNotifications() )
		{
			sendNotification( NotificationService.BUFFER_OVERFLOW_NOTIFICATION_TYPE,
					NotificationService.OVERFLOWED_NOTIFICATION_KEY,
					oldNotif );
		}
	}

		public void
	handleNotification(
		final Notification	notif, 
		final Object		handback) 
	{
		synchronized( mBuffers )
		{
			for( final Object id : mBuffers.keySet() )
			{
				getBuffer( id ).bufferNotification( notif );
			}
		}
		
		// let all listeners know...
		sendNotification( notif );
	}

		private NotificationBuffer
	getBuffer( final Object	bufferID )
	{
		return mBuffers.get( bufferID );
	}
	
	private final class OverflowHandlerImpl implements OverflowHandler
	{
		public OverflowHandlerImpl()	{}
		
			public void
		handleBufferOverflow( Object o )
		{
			final Notification notif	= (Notification)o;
			issueBufferOverflowNotification( notif );
		}
	}
	
	private static int	sBufferID	= 0;
		public Object
	createBuffer(
		final int					bufferSize,
		final NotificationFilter	filter )
	{
		final OverflowHandler	handler	= new OverflowHandlerImpl();
		
		final NotificationBuffer	buffer	=
			new NotificationBuffer( bufferSize, filter, handler );
		
		Object	id	= null;
		
		synchronized( mBuffers )
		{
			++sBufferID;
			id	= "" + sBufferID;
			
			mBuffers.put( id, buffer );
		}
		
		return( id );
	}
	
		public void
	removeBuffer( final Object	bufferID )
	{
		final NotificationBuffer	buffer	= mBuffers.remove( bufferID );
	}

		public Map
	getBufferNotifications(
		final Object	bufferID,
		final long		sequenceNumberIn )
	{
		final NotificationBuffer	buffer	= getBuffer( bufferID );
		
		return( buffer.getNotifications( sequenceNumberIn ) );
	}
	
	
		public String
	getGroup()
	{
		return( AMX.GROUP_UTILITY );
	}
	
		protected Set<ObjectName>
	getMatchingObjectNames( final ObjectName	pattern )
	{
		Set<ObjectName>	s	= null;
		
		if ( pattern.isPattern() )
		{
			s	= JMXUtil.queryNames( getMBeanServer(), pattern, null );
		}
		else
		{
			s	= GSetUtil.newSingletonSet( pattern );
		}
		
		return( s );
	}
	
		protected void
	listenToIfMatch( final ObjectName objectName )
	{
		if ( ! mListenees.keySet().contains( objectName ) )
		{
			final String	defaultDomain	= getMBeanServer().getDefaultDomain();
			
			for( final ObjectName pattern : mIncludePatterns.keySet() )
			{
				if ( JMXUtil.matchesPattern( defaultDomain, pattern, objectName ) )
				{
					final NotificationFilter	filter	=mIncludePatterns.get( pattern );
				
					listenToSingle( objectName, filter );
				}
			}
		}
	}
	
		protected void
	listenToSingle(
		final ObjectName			objectName,
		final NotificationFilter	filter )
	{
		mListenees.put( objectName, filter );
		try
		{
			getMBeanServer().addNotificationListener( objectName, this, filter, null );
		}
		catch( Exception e )
		{
			mListenees.remove( objectName );
		}
	}
	
		public void
	listenTo(
		final ObjectName			pattern,
		final NotificationFilter	filter )
		throws InstanceNotFoundException
	{
		mIncludePatterns.put( pattern, filter );
		
		final Set<ObjectName>	listenees	= getMatchingObjectNames( pattern );
		
		final MBeanServer	server	= getMBeanServer();
		for( final ObjectName objectName : listenees )
		{
			if ( objectName.equals( getObjectName() ) )
			{
				continue;
			}
			
			listenToSingle( objectName, filter );
		}
	}
	
		private void
	checkListeningTo( final ObjectName objectName )
	{
		if ( ! mListenees.containsKey( objectName ) )
		{
			throw new IllegalArgumentException( objectName.toString() );
		}
	}
	
		public void
	dontListenTo( final ObjectName pattern )
	{
		mIncludePatterns.remove( pattern );
		
		final Set<ObjectName>	listenees	= getMatchingObjectNames( pattern );
		
		final MBeanServer	server	= getMBeanServer();
		for( final ObjectName objectName : listenees )
		{
			try
			{
				server.removeNotificationListener( objectName, this );
				mListenees.remove( objectName );
			}
			catch( ListenerNotFoundException e )
			{
			}
			catch( InstanceNotFoundException e )
			{
			}
		}
	}
	
	
		public Set<ObjectName>
	getListeneeObjectNameSet()
	{
		final Set<ObjectName>	objectNames	= new HashSet<ObjectName>();
		
		synchronized( mListenees )
		{
			objectNames.addAll( mListenees.keySet() );
		}
		
		return( objectNames );
	}
	
		private NotificationFilter
	_getFilter( final ObjectName objectName)
	{
		final NotificationFilter filter	= mListenees.get( objectName );
		
		return( filter );
	}
	
		public NotificationFilter
	getFilter( final ObjectName objectName)
	{
		checkListeningTo( objectName );
		
		return( _getFilter( objectName ) );
	}
	
	// tracks coming and going of MBeans being listened to which
	// match our patterns...
	private final class RegistrationListener implements NotificationListener
	{
		public RegistrationListener()	{}
		
			public void
		handleNotification(
			final Notification	notifIn, 
			final Object		handback) 
		{
			if ( notifIn instanceof MBeanServerNotification )
			{
				final MBeanServerNotification	notif	= (MBeanServerNotification)notifIn;
				
				final ObjectName	objectName	= notif.getMBeanName();
				final String	type	= notif.getType();
				
				if ( type.equals( MBeanServerNotification.REGISTRATION_NOTIFICATION  ) )
				{
					listenToIfMatch( objectName );
				}
				else if ( type.equals( MBeanServerNotification.UNREGISTRATION_NOTIFICATION  ) )
				{
					dontListenTo( objectName );
				}
			}
			
		}
	}
	
    @Override
		protected void
	preRegisterDone()
        throws Exception
	{
		super.preRegisterDone();
		
		// it's crucial we listen for registration/unregistration events
		// so that any patterns are maintained.
		JMXUtil.listenToMBeanServerDelegate( getMBeanServer(),
			new RegistrationListener(), null, null );
	}

		public void
	preDeregisterHook()
        throws Exception
	{
		super.preDeregisterHook();
		
		synchronized( mListenees )
		{
			final Set<ObjectName> s	= getListeneeObjectNameSet();
			
			final ObjectName[]	objectNames	= new ObjectName[ s.size() ];
			s.toArray( objectNames );
			
			for( int i = 0; i < objectNames.length; ++i )
			{
				dontListenTo( objectNames[ i ] );
			}
		}
		
		synchronized( mBuffers )
		{
			for( final NotificationBuffer buffer : mBuffers.values() )
			{
				removeBuffer( buffer );
			}
		}
	}
}












/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package samples.amx;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.MBeanServerNotification;

/**
	Displays any {@link javax.management.Notification} received.
	<p>
	Note that most {@link com.sun.appserv.management.base.AMX} Notifications include a Map in the
	userData field of the Notification.
	
	@see com.sun.appserv.management.base.AMX
 */
public final class SampleListener implements NotificationListener
{
	private Map	mNotifs;
	
		public
	SampleListener()
	{
		mNotifs	= null;
		clearNotifs();
	}
	
	/**
		Keep a Map, keyed by Notification type, of all Notifications received.
	 */
		private synchronized void
	addNotif( final Notification	notif )
	{
		final String	type	= notif.getType();
		
		List	list	= null;
		
		if ( ! mNotifs.keySet().contains( type ) )
		{
			clearNotifs( type );
		}
		
		list	= (List)mNotifs.get( type );
		
		list.add( notif );
	}
	
	/**
		Return a Map, keyed by Notification type, of all Notifications received so far.
	 */
		public Map
	getNotifsReceived()
	{
		return( mNotifs );
	}
	
		public List
	getNotifsReceived( final String type )
	{
		return( (List)mNotifs.get( type ) );
	}
	
	/**
		Clear the history of Notifications received.
	 */
		public synchronized Map
	clearNotifs()
	{
		final Map	existing	= getNotifsReceived();
		
		mNotifs	= Collections.synchronizedMap( new HashMap() );
		
		return( existing );
	}
	
		public synchronized List
	clearNotifs( final String type )
	{
		final Map	existing	= getNotifsReceived();
		
		final List newList	= Collections.synchronizedList( new ArrayList() );
		
		final List	existingList	= (List)existing.get( type );
		
		mNotifs.put( type, newList );
		
		return( existingList );
	}
	
	/**
		The Notification is delivered here.
	 */
		public void
	handleNotification(
		final Notification	notif, 
		final Object		handback) 
	{
		final String	type		= notif.getType();
		final Object	userData	= notif.getUserData();
		
		addNotif( notif );
		
		SampleUtil.println( "SampleListener: received: " + SampleUtil.toString( notif ) );
		SampleUtil.println( "" );
	}
	
	
	
}

/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/NotificationBuilder.java,v 1.2 2005/11/08 22:40:24 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:40:24 $
 */

package com.sun.cli.jmxcmd.util.jmx;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

import javax.management.Notification;
import javax.management.ObjectName;

/**
	Base class for building AMX Notifications.  AMX Notifications
	all place a Map in the userData field.  This class takes care
	of building Notifications with correct time stamp, sequence number,
	etc.  It also enforces certain conventions.
	<p>
	A convenience routine is provided for adding additional fields to
	the Map--putMapData().
 */
public class NotificationBuilder
{
	private long				mSequenceNumber	= 0;
	private final String		mNotificationType;
	private final Object		mSource;
	
		protected synchronized long
	nextSequenceNumber( )
	{
		return( mSequenceNumber++ );
	}
	
		public
	NotificationBuilder(
		final String		notificationType,
		final Object		source )
	{
		mNotificationType	= notificationType;
		mSource				= source;
	}
	
		public final String
	getNotificationType()
	{
		return( mNotificationType );
	}
	
		public final Object
	getSource()
	{
		return( mSource );
	}
	
	
		protected final long
	now()
	{
		return( System.currentTimeMillis() );
	}
	
	/**
		Build a new Notification with an existing Map.
	 */
		public Notification
	buildNewWithMap(
		final String	                          message,
	    final Map<String,? extends Serializable>  userDataMap )
	{
		final Notification notif	= new Notification(
			mNotificationType,
			mSource,
			nextSequenceNumber(),
			now(),
			message);
	
	    if ( userDataMap != null )
	    {
		    notif.setUserData( userDataMap );
		}
		else
		{
		    notif.setUserData( new HashMap<String,Serializable>() );
		}
		
		return( notif );
	}
	
	
	
	/**
		Build a new Notification without any values in its Map
		and no message.
	 */
		public Notification
	buildNew()
	{
		return buildNew( mNotificationType );
	}
	
	
	/**
		Build a new Notification without any values in its Map.
		@param message
	 */
		public Notification
	buildNew( final String message )
	{
	    return buildNewWithMap( message, null );
	}
	
	/**
		Build a new Notification with one key/value for the Map.
		public Notification
	buildNew(
		final String	    key,
		final Serializable	value )
	{
	    if ( value instanceof Map )
	    {
	        throw new IllegalArgumentException("use buildNewWithMap" );
	    }

		final Notification	notif = buildNew();
		
		if ( key != null )
		{
			putMapData( notif, key, value );
		}
		
		return( notif );
	}
	 */
	
	/**
		Build a new Notification with one key/value for the Map.
		public Notification
	buildNew(
		final String        key,
		final Serializable  value,
		final String        message )
	{
		final Notification	notif = buildNew( message );
		
		if ( key != null )
		{
			putMapData( notif, key, value );
		}
		
		return( notif );
	}
	 */
	
	
	
	/**
		Put a single key/value pair into the user data Map.
	 */
		public static final void
	putMapData(
		final Notification	notif,
		final String		key,
		final Serializable	value )
	{
	    final Map<String,Serializable>   userData    =
	        (Map<String,Serializable>)notif.getUserData();
	        
		userData.put( key, value );
	}
	
	/**
		Put all key/value pairs into the user data Map.
	 */
		public static final void
	putAllMapData(
		final Notification	              notif,
		final Map<String,? extends Serializable>    additionalUserData )
	{
	    final Map<String,Serializable>   userData    =
	        (Map<String,Serializable>)notif.getUserData();
	        
		userData.putAll( additionalUserData );
	}
	
}






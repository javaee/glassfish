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
        @SuppressWarnings("unchecked")
	    final Map<String,Serializable>   userData    = (Map<String,Serializable>)notif.getUserData();
	        
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
        @SuppressWarnings("unchecked")
	    final Map<String,Serializable>   userData    = Map.class.cast(notif.getUserData());
	        
		userData.putAll( additionalUserData );
	}
	
}






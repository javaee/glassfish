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

import java.util.Map;
import java.util.HashMap;

import javax.management.Notification;
import javax.management.NotificationFilter;

import com.sun.appserv.management.base.NotificationService;
import com.sun.appserv.management.util.misc.CircularList;
import com.sun.appserv.management.util.misc.OverflowHandler;



/**
	A circular buffer for Notifications.
 */
final class NotificationBuffer
{
	private final CircularList<Notification> mNotifications;
	private final NotificationFilter	mFilter;
	private final int					mBufferSize;
	long								mNextSequenceNumber;
	private OverflowHandler				mOverflowHandler;
	
	
		protected synchronized long
	nextSequenceNumber( )
	{
		return( mNextSequenceNumber++ );
	}
	
		protected synchronized long
	getNextSequenceNumber( )
	{
		return( mNextSequenceNumber );
	}
	
		public
	NotificationBuffer(
		final int					bufferSize,
		final NotificationFilter	filter,
		final OverflowHandler		handler )
	{
		mFilter					= filter;
		mBufferSize				= bufferSize;
		mNotifications			= new CircularList<Notification>( Notification.class, bufferSize );
		mNextSequenceNumber		= 1;
		mOverflowHandler		= handler;
	}

	/**
		Buffer the Notification if it matches the filter.
	 */
		public void
	bufferNotification( final Notification	notif ) 
	{
		if ( mFilter == null || mFilter.isNotificationEnabled( notif ) )
		{
			synchronized( mNotifications )
			{
				mNotifications.add( notif );
				nextSequenceNumber();
			}
		}
	}
	
	
	/**
		Get the number of Notifications available.  Once the buffer is full, it will stay
		full until cleared, so that getSize() will return the same number as getCapacity().
	 */
		public int
	getBufferSize()
	{
		return( mBufferSize );
	}
	
		public void
	clear()
	{
		synchronized( mNotifications )
		{
			mNotifications.clear();
		}
	}


	private final Notification[]	EMPTY_NOTIFS	= new Notification[ 0 ];
	private final Long				LONG_ZERO		= Long.valueOf(0);
	
	/**
		Key within the Map returned by getNotifications() that yields the Long
		for the next sequence number.
	 */
	public static final String	NEXT_SEQUENCE_NUMBER_KEY	=
		NotificationService.NEXT_SEQUENCE_NUMBER_KEY;
	
	/**
		Key within the Map returned by getNotifications() that yields the
		Notifications[].
	 */
	public static final String	NOTIFICATIONS_KEY	=
		NotificationService.NOTIFICATIONS_KEY;
	
	/**
		Get all outstanding Notifications which have a sequence number
		that is equal to or greater than the specified one.  The sequence
		number in this case is the overarching one maintained by this buffer,
		and has nothing to do with the sequence number within any particular
		Notification.
		<p>
		A sequence number of 0 means all Notifications.
		<p>
		The array returned contains the following:
		<nl>
		<li>result[ 0 ] is the next sequence number (for the next call)</li>
		<li>result[ 1 ] a Notification[] of the available Notifications</li>
		</nl>
		
		@return result[ 0 ] = next sequence number, result[ 1 ] = Notification[]
	 */
		public Map<String,Object>
	getNotifications( final long sequenceNumberIn ) 
	{
		if ( sequenceNumberIn < 0 )
		{
			throw new IllegalArgumentException( "" + sequenceNumberIn );
		}
		
		final Map<String,Object>	result	= new HashMap<String,Object>();
		result.put( NEXT_SEQUENCE_NUMBER_KEY, LONG_ZERO );
		result.put( NOTIFICATIONS_KEY, EMPTY_NOTIFS );
		
		synchronized( mNotifications )
		{
			final int	numNotifsAvailable	= mNotifications.size();
			final long	nextAvailSequenceNumber	= getNextSequenceNumber();
			result.put(NEXT_SEQUENCE_NUMBER_KEY,
                                    Long.valueOf(nextAvailSequenceNumber));
			
			if ( numNotifsAvailable != 0 )
			{
				final long	lastAvailSequenceNumber		= nextAvailSequenceNumber - 1;
				final long	firstAvailSequenceNumber	= 1 + (lastAvailSequenceNumber - numNotifsAvailable);
				
				assert( firstAvailSequenceNumber >= 1 );
				
				final long	requestedSequenceNumber	= sequenceNumberIn == 0 ?
					firstAvailSequenceNumber : sequenceNumberIn;
					
				if ( requestedSequenceNumber >= firstAvailSequenceNumber &&
					requestedSequenceNumber <= lastAvailSequenceNumber)
				{
					final int	numMatches	= 1 +
						(int)(lastAvailSequenceNumber - requestedSequenceNumber);
					
					final Notification[]	notifs	= new Notification[ numMatches ];
					
					final int	startIndex	= (int)
						(requestedSequenceNumber - firstAvailSequenceNumber);
					for( int i = 0 ; i < numMatches; ++i )
					{
						notifs[ i ]	= (Notification)
							mNotifications.get( startIndex + i );
					}
					
					result.put( NOTIFICATIONS_KEY, notifs );
				}
			}
		}
		
		assert( result.get( NOTIFICATIONS_KEY ) instanceof Notification[] );
		assert( result.get( NEXT_SEQUENCE_NUMBER_KEY ) instanceof Long );
		
		return( result );
	}
}












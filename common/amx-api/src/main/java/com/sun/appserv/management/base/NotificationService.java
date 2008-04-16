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
 

package com.sun.appserv.management.base;

import javax.management.InstanceNotFoundException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.util.Map;
import java.util.Set;

/**
	Provides enhanced abilities for working with Notifications.  Listens 
	to other MBeans (as configured) and collects the Notifications
	which can later be retrieved by calling takeNotifications().  The listening
	is quite powerful; both "listen to" and "don't listen to" ObjectNames or
	ObjectName patterns may be specified, and are dynamically maintained.
	<p>
	All received Notifications are resent to all listeners on this MBean.  This
	makes it possible to listen to a single MBean for all "interesting"
	Notifications across many different MBeans, even if these MBeans
	are dynamically registered and unregistered.
	<p>
	Buffering is also available, via a named buffer facility.  Creation of a
	buffer together with an optional filter allows a caller to buffer 
	Notifications of interest which can later be retrieved as a batch.  This
	facility may be of particular use for clients which disconnect and
	reconnect.
	<p>
	When a buffer overflows, a notification of type BUFFER_OVERFLOW_NOTIFICATION_TYPE
	is emitted
 */
public interface NotificationService
	extends AMX, NotificationListener, Singleton
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.NOTIFICATION_SERVICE;
	
	/**
		When the buffer overflows this Notification is issued.  The user
		data of the Notification contains the Notification that was discarded.
	 */
	public static final String	BUFFER_OVERFLOW_NOTIFICATION_TYPE	=
		XTypes.NOTIFICATION_SERVICE + ".BufferOverflow";
		
	/**
		Key for accessing the overwritten Notification with the Notification
		of type BUFFER_OVERFLOW_NOTIFICATION_TYPE.
	 */
	public static final String	OVERFLOWED_NOTIFICATION_KEY	=
		XTypes.NOTIFICATION_SERVICE + ".OverflowedNotification";
	
	
	/**
		The user data supplied when the instance was created.
	 */
	public Object	getUserData();
							
	/**
		Listen for Notifications from an  using the specified filter,
		which may be null, in which case all Notifications are heard.
		The ObjectName may also be a pattern, in which case all s
		matching the pattern are listened to.
		<p>
		Note that Notifications may also be manually forced into the
		service by calling handlingNotification().
		
		@param pattern	name of  to listen to
		@param filter
	 */
	public void	listenTo( ObjectName pattern,
						final NotificationFilter filter )
							throws InstanceNotFoundException;
	
	
	/**
		Stop listening for Notifications on the specified . It may
		also be a pattern, in which case listening is stopped on all
		s matching the pattern.
			
		@param objectName	name of  which should no longer be listened to.
	 */
	public void	dontListenTo( ObjectName objectName )
					throws InstanceNotFoundException;
	
	
	/**
		Get the filter being used for a particular .
		
		@param objectName
		@return NotificationFilter
	 */
	public NotificationFilter		getFilter( ObjectName objectName);
	
	
	/**
		Create a new buffer of the specified size.
		
		@param bufferSize	maximum number of Notifications to be buffered
		@param filter		filter for Notifications to be buffered
		@return id of the newly-created buffer
	 */
	public Object	createBuffer( int bufferSize, NotificationFilter filter );
	
	/**
		Remove the specified buffer.
	 */
	public void	removeBuffer( Object id );
	
	/**
		Key within the Map returned by getNotifications() that yields the Long
		for the next sequence number.
	 */
	public static final String	NEXT_SEQUENCE_NUMBER_KEY	= "NextSequenceNumber";
	
	/**
		Key within the Map returned by getNotifications() that yields the
		Notifications[].
	 */
	public static final String	NOTIFICATIONS_KEY			= "Notifications";
	
	/**
		Get all outstanding Notifications which have a sequence number
		that is equal to or greater than the specified one.
		A sequence number of 0 means all Notifications.  The sequence
		number in this case is the overarching one maintained by this buffer,
		and has nothing to do with the sequence number within any particular
		Notification.
		<p>
		Notifications are never removed from the buffer; be sure to use the
		returned sequence number as a means of fetching new Notifications.
		<p>
		The Map is keyed by the following:
		<nl>
		<li>NEXT_SEQUENCE_NUMBER_KEY  returns the Long for the next sequence number
			for subsequent calls to getNotifications()
			</li>
		<li>NOTIFICATIONS_KEY keys the Notification[]</li>
		</nl>
		
		@return result[ 0 ] = next sequence number, result[ 1 ] = Notification[]
	 */
	public Map<String,Object>	getBufferNotifications( final Object bufferID, final long sequenceNumberIn );


	/**
		Get the MBeans to which this service listens.
	 */
	public Set<ObjectName>		getListeneeSet();
}

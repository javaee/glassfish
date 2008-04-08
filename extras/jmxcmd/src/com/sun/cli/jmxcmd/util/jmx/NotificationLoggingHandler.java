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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/NotificationLoggingHandler.java,v 1.1 2005/11/08 22:40:24 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2005/11/08 22:40:24 $
 */

package com.sun.cli.jmxcmd.util.jmx;

import java.util.logging.LogRecord;
import java.util.logging.Handler;

import javax.management.ObjectName;
import javax.management.Notification;

import com.sun.appserv.management.base.AMXMBeanLogging;
import com.sun.cli.jmxcmd.util.jmx.NotificationSender;
import com.sun.cli.jmxcmd.util.jmx.NotificationBuilder;

/**
	A handler which will emit Notifications when configured to do so.
 */
public final class NotificationLoggingHandler
	extends Handler
{
	private final Object				mSource;
	private boolean						mEmitNotifications;
	private final NotificationSender	mSender;
	private NotificationBuilder			mBuilder;
	
		public
	NotificationLoggingHandler(
		final Object				source,
		final NotificationSender	sender )
	{
		mSource				= source;
		mEmitNotifications	= false;
		mSender				= sender;
		mBuilder			= null;
	}
	
	/*
		protected NotificationBuilder
	getBuilder()
	{
		if ( mBuilder == null )
		{
			synchronized( this )
			{
				mBuilder	=
					new NotificationBuilder(
						AMXMBeanLogging.LOG_RECORD_NOTIFICATION_TYPE, mSource );
			}
		}
		return( mBuilder );
	}
	
		public boolean
	getEmitNotifications()
	{
		return( mEmitNotifications );
	}
	
		public void
	setEmitNotifications( final boolean emit)
	{
		mEmitNotifications	= emit;
	}
	*/
	
		public void
	close()
	{
	}
	
		public void
	flush()
	{
	}
	
		private Notification
	buildNew( final LogRecord record )
	{
		final Notification	notif	=
			mBuilder.buildNew( record.toString() );
		
		return( notif );
	}
	
		public void
	publish( final LogRecord record )
	{
		if ( false )
		{
			final Notification notif	= buildNew( record );
			mSender.sendNotification( notif );
		}
	}
	
		protected void
	reportError(
		String		msg,
		Exception	ex,
		int			code)
	{	
		// do nothing
	}
}

















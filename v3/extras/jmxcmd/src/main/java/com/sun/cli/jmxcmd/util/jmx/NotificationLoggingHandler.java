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

package com.sun.cli.jmxcmd.util.jmx;

import java.util.logging.LogRecord;
import java.util.logging.Handler;

import javax.management.ObjectName;
import javax.management.Notification;

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

















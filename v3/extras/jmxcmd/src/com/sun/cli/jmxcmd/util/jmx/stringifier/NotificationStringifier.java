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
package com.sun.cli.jmxcmd.util.jmx.stringifier;

import java.util.Date;
import javax.management.Notification;
import javax.management.MBeanServerNotification;

import com.sun.cli.jcmd.util.stringifier.Stringifier;
import com.sun.cli.jcmd.util.stringifier.SmartStringifier;
import com.sun.cli.jcmd.util.misc.StringUtil;

public class NotificationStringifier implements Stringifier
{
	public static final NotificationStringifier	DEFAULT	= new NotificationStringifier();
	
	protected Options	mOptions;
	
	public final static class Options
	{
		// don't make 'final' fields; allow changes after instantiation
		public boolean	mIncludeObjectName;
		public boolean	mIncludeTimeStamp;
		public boolean	mIncludeType;
		public boolean	mIncludeSequenceNumber;
		public boolean	mIncludeUserData;
		public String	mDelim;
		
			public
		Options()
		{
			mIncludeObjectName		= true;
			mIncludeTimeStamp		= true;
			mIncludeType			= true;
			mIncludeSequenceNumber	= true;
			mIncludeUserData		= false;
			mDelim	= ", ";
		}
		
	}
	
	
		public
	NotificationStringifier( )
	{
		mOptions	= new Options();
	}
	
		public
	NotificationStringifier( Options options )
	{
		mOptions	= options;
	}
		
		protected void
	append( StringBuffer b, Object o)
	{
		if ( b.length() != 0 )
		{
			b.append( mOptions.mDelim );
		}
		
		b.append( SmartStringifier.toString( o ) );
	}
	
		public String
	stringify( Object o )
	{
		final Notification	notif	= (Notification)o;
		
		return( _stringify( notif ).toString() );
	}
	
		public static String
	toString( Object o )
	{
		return( DEFAULT.stringify( o ) );
	}
	
		protected StringBuffer
	_stringify( Notification notif )
	{
		final StringBuffer	b	= new StringBuffer();
		
		if ( mOptions.mIncludeSequenceNumber )
		{
			append( b, "#" + notif.getSequenceNumber() );
		}

		if ( mOptions.mIncludeTimeStamp )
		{
			append( b, new Date( notif.getTimeStamp() ) );
		}

		if ( mOptions.mIncludeObjectName )
		{
			append( b, StringUtil.quote( notif.getSource() ) );
		}

		if ( mOptions.mIncludeType )
		{
			append( b, notif.getType() );
		}

		if ( mOptions.mIncludeUserData )
		{
			append( b, StringUtil.quote( notif.getUserData() ) );
		}
		
		if ( notif instanceof MBeanServerNotification )
		{
			// this should really be done in a MBeanServerNotificationStringifier!
			final MBeanServerNotification	n	= (MBeanServerNotification)notif;
			
			append( b, StringUtil.quote( n.getMBeanName() ) );
		}
		
		return( b );
	}
}




















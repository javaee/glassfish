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

import javax.management.AttributeChangeNotification;

import com.sun.cli.jcmd.util.stringifier.Stringifier;
import com.sun.cli.jcmd.util.stringifier.SmartStringifier;


public class AttributeChangeNotificationStringifier
				extends NotificationStringifier implements Stringifier
{
	public static final AttributeChangeNotificationStringifier	DEFAULT	=
			new AttributeChangeNotificationStringifier();
	
		public
	AttributeChangeNotificationStringifier( )
	{
		super();
	}
	
		public
	AttributeChangeNotificationStringifier( Options options )
	{
		super( options );
	}
	
		public String
	stringify( Object o )
	{
		final AttributeChangeNotification	notif	= (AttributeChangeNotification)o;
		
		final StringBuffer	b	= super._stringify( notif );
		append( b, "" );
		
		final String	attrName	= notif.getAttributeName();
		final String	oldValue	= SmartStringifier.toString( notif.getOldValue() );
		final String	newValue	= SmartStringifier.toString( notif.getNewValue() );
		
		final String msg	= attrName + ": " + oldValue + " => " + newValue;
		b.append( msg );
		
		return( b.toString() );
	}
}




















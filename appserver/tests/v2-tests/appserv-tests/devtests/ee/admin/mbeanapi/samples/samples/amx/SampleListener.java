/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

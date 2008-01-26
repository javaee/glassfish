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
package com.sun.enterprise.management.support;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import javax.management.Notification;
import javax.management.MBeanNotificationInfo;


import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.NotificationEmitterService;

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;



/**
	A key aspect of the implementation is that it maintains a list of all types
	of Notifications it has seen emitted, paired with the implementing class
	so that getNotificationInfo() can return infos that accurately reflect
	what it is likely to emit.  This approach is used instead of statically
	coding Notification classes/types because it cannot be known in advance
	which type(s) will actually be emitted.
 */
public final class NotificationEmitterServiceImpl extends AMXImplBase
	implements NotificationEmitterService
{
	/**
		Each class of Notification may have one or more types associated with it,
		stored in a Set&lt;String&gt;:<br>
		Map&ltnotif classname, Set&ltString of notif type&gt;&gt;
	 */
	private final Map<String,Set<String>>	mEmittedTypes;
	
		public
	NotificationEmitterServiceImpl( )
	{
		mEmittedTypes	= Collections.synchronizedMap( new HashMap<String,Set<String>>() );
	}

		public String
	getGroup()
	{
		return( AMX.GROUP_UTILITY );
	}
	
		public boolean
	getMBeanInfoIsInvariant()
	{
		return( false );	// MBeanNotificationInfo may grow as we learn of new types
	}
	
	
		public MBeanNotificationInfo[]
	getNotificationInfo()
	{
		final MBeanNotificationInfo[]	superTypes	= super.getNotificationInfo();
		
		final Set<MBeanNotificationInfo>	dynamicInfos	=
		    new HashSet<MBeanNotificationInfo>();
		
		/*
			Go through our list of class/types we've seen emitted so far
			and generate MBeanNotificationInfo for each class of Notification
		 */
		for( final String notifClass : mEmittedTypes.keySet() )
		{
			final Set<String>	types		= mEmittedTypes.get( notifClass );
				
			final MBeanNotificationInfo	info	= new MBeanNotificationInfo(
				GSetUtil.toStringArray( types ), notifClass, "" );
			
			dynamicInfos.add( info );
		}
		
		final MBeanNotificationInfo[]	dynamicInfosArray	= (MBeanNotificationInfo[])
			dynamicInfos.toArray( new MBeanNotificationInfo[ dynamicInfos.size() ] );
		
		// now merge what we've found with super's types
		
		final MBeanNotificationInfo[]	allInfos	=
			JMXUtil.mergeMBeanNotificationInfos( superTypes, dynamicInfosArray );
		
		return allInfos;
	}
	
	
	/** 
		Cumulatively update our Map of Notification classes and the types corresponding
		to each class. A few of the standard ones are:

<code>
		javax.management.AttributeChangeNotification =>
			{ AttributeChangeNotification.ATTRIBUTE_CHANGE }
			
		javax.management.MBeanServerNotification =>
			{	MBeanServerNotification.REGISTRATION_NOTIFICATION,
				MBeanServerNotification.UNREGISTRATION_NOTIFICATION }
		
		Various others are likely to be just generic Notification:
		javax.management.Notification =>
			{
				x.y.z,
				a.b.c,
				...
			}
</code>
	 */
		private void
	cumulateTypes( final Notification notif )
	{
		final String notifClass	= notif.getClass().getName();
		final String type		= notif.getType();
		
		if ( ! mEmittedTypes.containsKey( notifClass ) )
		{
			final Set<String>	types	= GSetUtil.newStringSet( type );
			mEmittedTypes.put( notifClass, types );
		}
		else
		{
			final Set<String>	types	= mEmittedTypes.get( notifClass );
			if ( ! types.contains( type ) )
			{
				types.add( type );
			}
		}
	}

	/**
		Note that this method may be used to test the functionality of this service
		by asking it to emit any desired Notification.
		<p>
		Note that this method is likely to be called from many different threads.
	 */
		public void
	emitNotification( final Notification notif )
	{
		cumulateTypes( notif );
		
		if ( getListenerCount() != 0 )
		{
			sendNotification( notif );
		}
	}
}












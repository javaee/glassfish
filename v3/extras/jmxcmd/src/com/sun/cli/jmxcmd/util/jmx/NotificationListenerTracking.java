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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/NotificationListenerTracking.java,v 1.1 2005/11/08 22:40:24 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2005/11/08 22:40:24 $
 */
package com.sun.cli.jmxcmd.util.jmx;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import javax.management.NotificationListener;
import javax.management.NotificationFilter;


/**
 */
public class NotificationListenerTracking
{
	// NotificationListeners are not unique, so we can't use a Map
    private final List<NotificationListenerInfo>   mInfos;
    
		public
	NotificationListenerTracking( boolean synchronize )
	{
	    final List<NotificationListenerInfo> infos    =
	        new ArrayList<NotificationListenerInfo>();
	    
	    mInfos  = synchronize ? Collections.synchronizedList( infos ) : infos;
	}
	
	    public void
	addNotificationListener(
	    NotificationListener listener,
	    NotificationFilter   filter,
	    Object               handback )
	{
	    final NotificationListenerInfo  info    =
	        new NotificationListenerInfo( listener, filter, handback );
	        
	    mInfos.add( info );
	}
	
	
        public int
    getListenerCount()
    {
        return mInfos.size();
    }
	
	    private final boolean
	listenersEqual(
	    final NotificationListener listener1,
	    final NotificationListener listener2)
	{
	    return( listener1 == listener2 );
	}
	
        private final boolean
	handbacksEqual(
	    final Object handback1,
	    final Object handback2)
	{
	    return( handback1 == handback2 );
	}
	
	/**
	    Remove <b>all instances</b> of the specified listener and return
	    their corresponding NotificationListenerInfo.
	    This behavior matches the behavior of
	    {@link javax.management.NotificationEmitter}.
	    
	    @return list of NotificationListenerInfo
	 */
	    public List<NotificationListenerInfo>
	removeNotificationListener( final NotificationListener listener )
	{
	    final Iterator iter   = mInfos.iterator();
	    
	    final List<NotificationListenerInfo>    results = new ArrayList<NotificationListenerInfo>();
	    
	    while( iter.hasNext() )
	    {
	        final NotificationListenerInfo  info =
	            (NotificationListenerInfo)iter.next();
	        
	        if ( listenersEqual( listener, info.getListener() )  )
	        {
	            iter.remove();
	            results.add( info ); 
	        }
	    }
	    
	    return( results );
	}
	
	/**
	    Remove <b>the first instance</b> of the specified listener/filter/handback
	    combination and return its corresponding NotificationListenerInfo.
	    This behavior matches the behavior of
	    {@link javax.management.NotificationEmitter}.
	    
	    @return list of NotificationListenerInfo
	 */
	    public NotificationListenerInfo
	removeNotificationListener(
	    final NotificationListener listener,
	    final NotificationFilter   filter,
	    final Object               handback )
	{
	    final Iterator iter   = mInfos.iterator();
	    NotificationListenerInfo result  = null;
	    
	    while( iter.hasNext() )
	    {
	        final NotificationListenerInfo  info =
	            (NotificationListenerInfo)iter.next();
	        
	        if ( listenersEqual( listener, info.getListener() ) &&
	            handbacksEqual( handback, info.getHandback() ) )
	        {
	            iter.remove();
	            result  = info;
	            break;
	        }
	    }
	    
	    return( result );
	}
}

























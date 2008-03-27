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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/NotificationEmitterSupport.java,v 1.4 2005/11/15 20:21:48 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/15 20:21:48 $
 */
package com.sun.cli.jmxcmd.util.jmx;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;

import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationListener;
import javax.management.NotificationFilter;
import javax.management.relation.MBeanServerNotificationFilter;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.Notification;
import javax.management.AttributeChangeNotification;
import javax.management.MBeanServerNotification;
import javax.management.ListenerNotFoundException;


import com.sun.cli.jcmd.util.misc.ListUtil;

/**
	Features:
	<ul>
	<li>Maintains information on all NotificationListeners so that queries can
	be made on the number of listeners, and the number of listeners of each type</li>
	<li>optionally sends all Notifications asynchronously via a separate Thread</li>
	</ul>
 */
public class NotificationEmitterSupport
    extends NotificationBroadcasterSupport
{
	private final boolean		mAsyncDelivery;
	private SenderThread		mSenderThread;
	private final Map<String,Integer>   mListenerTypeCounts;
	private final NotificationListenerTracking   mListeners;
	
	
		public
	NotificationEmitterSupport(
		final boolean	asyncDelivery)
	{
		mAsyncDelivery	= asyncDelivery;
		// don't create a thread until needed
		mSenderThread	= null;
		
		mListenerTypeCounts = Collections.synchronizedMap( new HashMap<String,Integer>() );
		
		mListeners = new NotificationListenerTracking( true );
	}
	
		private synchronized SenderThread
	getSenderThread()
	{
		if ( mSenderThread == null )
		{
			mSenderThread	= mAsyncDelivery ? new SenderThread() : null;
			if ( mSenderThread != null )
			{
				mSenderThread.start();
			}
		}
		
		return( mSenderThread );
	}
	
		public synchronized void
	cleanup()
	{
		if ( mSenderThread != null )
		{
			mSenderThread.quit();
			mSenderThread	= null;
		}
	}
	
	/**
		Synchronously (on current thread), ensure that all Notifications
		have been delivered.
	 */
		public void
	sendAll( )
	{
		if ( mSenderThread != null )
		{
			mSenderThread.sendAll();
		}
	}
	
		public int
	getListenerCount()
	{
		return( mListeners.getListenerCount() );
	}
	
		public int
	getNotificationTypeListenerCount( final String type )
	{
	    final Integer   count   = mListenerTypeCounts.get( type );
	    
	    int resultCount = 0;
	    
	    if ( count == null )
	    {
	        final Integer allCount  = mListenerTypeCounts.get( WILDCARD_TYPE );
	        if ( allCount != null )
	        {
	            resultCount = allCount;
	        }
	        else
	        {
	            // no wildcards are in use
	        }
	    }
	    
		return( resultCount );
	}
	
	
	private static final String[]   NO_TYPES  = new String[0];
	private static final String     WILDCARD_TYPE  = "***";
	private static final String[]   ALL_TYPES  = new String[] { WILDCARD_TYPE };
	
	private static final String[]   ATTRIBUTE_CHANGE_TYPES  = new String[]
	{
	    AttributeChangeNotification.ATTRIBUTE_CHANGE
	};
	
	private static final String[]   MBEAN_SERVER_NOTIFICATION_TYPES  = new String[]
	{
	    MBeanServerNotification.REGISTRATION_NOTIFICATION,
	    MBeanServerNotification.UNREGISTRATION_NOTIFICATION,
	};
	
	
	private final Integer   COUNT_1 = new Integer( 1 );
	
	    private void
	incrementListenerCountForType( final String type )
	{
	    synchronized( mListenerTypeCounts )
	    {
	        final Integer count   = mListenerTypeCounts.get( type );
	        
	        final Integer newCount  = (count == null ) ?
	                                    COUNT_1 : new Integer( count.intValue() + 1 );
	        
	        mListenerTypeCounts.put( type, newCount );
	    }
	}
	
	   private void
	decrementListenerCountForType( final String type )
	{
	    synchronized( mListenerTypeCounts )
	    {
	        final Integer count   = mListenerTypeCounts.get( type );
	        if ( count == null )
	        {
	            throw new IllegalArgumentException( type );
	        }
	        
	        final int   oldValue    = count.intValue();
	        if ( oldValue == 1 )
	        {
	            mListenerTypeCounts.remove( count );
	        }
	        else
	        {
	            mListenerTypeCounts.put( type, new Integer( oldValue - 1 ) );
	        }
	    }
	}
	
	
	    private static String[]
	getTypes(
		final NotificationFilter filter )
	{
	    String[]    types   = NO_TYPES;
	    
	    if ( filter instanceof NotificationFilterSupport )
	    {
	        final NotificationFilterSupport fs  = (NotificationFilterSupport)filter;
	        
	        types   = ListUtil.toStringArray( fs.getEnabledTypes() );
	    }
	    else if ( filter instanceof AttributeChangeNotificationFilter )
	    {
	        types   = ATTRIBUTE_CHANGE_TYPES;
	    }
	    else if ( filter instanceof MBeanServerNotificationFilter )
	    {
	        types   = MBEAN_SERVER_NOTIFICATION_TYPES;
	    }
	    else
	    {
	        // no filter, or non-standard one, have to assume all types
	        types   = ALL_TYPES;
	    }
	    
	    return types;
	}
	
	    private void
	addFilterTypeCounts( final NotificationFilter filter )
	{
	    String[]  types  = getTypes( filter );
    	    
	    for( String type : types )
	    {
	        incrementListenerCountForType( type );
	    }
	}
	
	    private void
	removeFilterTypeCounts( final NotificationFilter filter )
	{
    	final String[]  types   = getTypes( filter );
    	    
	    for( String type : types )
	    {
	        decrementListenerCountForType( type );
	    }
	}
	
	    private void
	removeFilterTypeCounts( final List<NotificationListenerInfo> infos )
	{
	    for( NotificationListenerInfo info : infos )
	    {
	        removeFilterTypeCounts( info.getFilter() );
	    }
	}
	
		public void
	addNotificationListener(
		final NotificationListener listener,
		final NotificationFilter filter,
		final Object handback)
	{
		super.addNotificationListener( listener, filter, handback );
		
		mListeners.addNotificationListener( listener, filter, handback );
		addFilterTypeCounts( filter );
	}
	
		public void
	removeNotificationListener(final NotificationListener listener)
		throws ListenerNotFoundException
	{
		super.removeNotificationListener( listener );
		
		final List<NotificationListenerInfo>    infos =
		    mListeners.removeNotificationListener( listener );
		removeFilterTypeCounts( infos );
	}
	
		public void
	removeNotificationListener(
		final NotificationListener	listener,
		final NotificationFilter	filter,
		final Object				handback)
		throws ListenerNotFoundException
	{
		super.removeNotificationListener( listener, filter, handback );
		
		mListeners.removeNotificationListener( listener );
		if ( filter != null )
		{
		    removeFilterTypeCounts( filter );
		}
		
	}
	
		protected void
	internalSendNotification( final Notification notif )
	{
		super.sendNotification( notif );
	}

	/**
		Send the Notification.  If created with async=true,
		then this routine returns immediately and the Notification is sent
		on a separate Thread.
	 */
		public void
	sendNotification( final Notification notif )
	{
		if ( getListenerCount() != 0 )
		{
            final SenderThread senderThread = getSenderThread();
			if ( senderThread != null )
			{
				senderThread.enqueue( notif );
			}
			else
			{
				internalSendNotification( notif );
			}
		}
	}
	
	private final class SenderThread extends Thread
	{
		private volatile boolean	mQuit;
		private final BlockingQueue<Notification>	mPendingNotifications;
		
        private static final int  MAX_PENDING = 128;
        private static final long TIMEOUT_MILLIS = 5 * 1000;    // 5 seconds
        
			public
		SenderThread( final String name )
		{
            super( "NotificationEmitterSupport.SenderThread-" + name);
			mQuit	= false;
            final boolean beFair = true;
			mPendingNotifications = new ArrayBlockingQueue<Notification>( MAX_PENDING, beFair) );
		}
        
        	public
		SenderThread()
		{
            this( "unspecified_name" );
        }
		
			public void
		quit()
		{
			mQuit	= true;
			synchronized( this )
			{
				this.notify();
			}
		}
		
			private void
		enqueue( final Notification notif )
		{
			mPendingNotifications.put( notif );
		}
		
        /**
            Send all pending Notifications.  If Notifications are sent, return immediately
            after sending the last one.  If none are available, block for up to the specified
            timeout.
            @param timeoutMillisIn
            @return the number of Notifications sent
         */
			public int
		sendAll( final long timeoutMillisIn )
            throws InterruptedException
		{
			Notification notif	= null;
            
            long timeoutMillis = timeoutMillisIn;
			int		 numSent	= 0;
			while ( (notif = mPendingNotification.poll(timeoutMillis, TimeUnit.MILLISECONDS)) != null )
			{
				internalSendNotification( notif );
                ++numSent;
                timeoutMillis = 0;  // process only those that remain; return after sending.
			}
			
			return( numSent );
		}
		
			public void
		run()
		{
			mQuit	= false;
			
			while ( ! mQuit )
			{
                int	numSent = 0;
                
				try
				{
                    numSent = sendAll( TIMEOUT_MILLIS );
				}
				catch( final InterruptedException e )
				{
                    // exit the thread
                    assert numSent == 0;
				}
				
				if ( numSent == 0 )
                {
                    break;
				}
			}
            
            cleanup();
            sendAll(0);  // in case Notifications were added just now while doing cleanup()
		}
	}
}






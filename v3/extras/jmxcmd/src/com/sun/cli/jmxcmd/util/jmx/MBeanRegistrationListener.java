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

import java.io.IOException;

import javax.management.ObjectName;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.NotificationFilter;
import javax.management.MBeanServerNotification;
import javax.management.MBeanServerConnection;
import javax.management.InstanceNotFoundException;
import com.sun.appserv.management.base.AMXRootLogger;

/**
	Convenience base class for listening to 
	{@link MBeanServerNotification} notifications.
	A class extending this class must implement {@link #mbeanRegistered}
	and {@link #mbeanUnregistered}.
	<p>
	The class is designed to start listening upon creation.
	The caller should call cleanup() when listening is no longer
	desired.  Once cleanup() is called, no further listening can
	be done; a new MBeanRegistrationListener should be instantiated
	if further listening is desired.
 */
public abstract class MBeanRegistrationListener extends NotificationListenerBase
{
    private final ObjectName    mRegUnregFilter;
    private final String        mDefaultDomain;
    
	/**
	    If 'constrain' is non-null, then all registration and unregistration
	    events will be filtered through it.  Only those MBeans
	    matching will be passed through to {@link #mbeanRegistered}
	    and {@link #mbeanUnregistered}.
	    
	    @param conn
	    @param constrain     optional fixed or pattern ObjectName
	 */
	    public
	MBeanRegistrationListener(
	    final MBeanServerConnection conn,
	    final ObjectName    constrain )
		throws InstanceNotFoundException, IOException
	{
	    super( conn,
	        JMXUtil.getMBeanServerDelegateObjectName() );
	    mRegUnregFilter = constrain;
	    
	    mDefaultDomain  = conn.getDefaultDomain();
	}
	
	/**
	    Calls this( conn, null ).
	    @param conn
	 */
	   public
	MBeanRegistrationListener( final MBeanServerConnection conn)
		throws InstanceNotFoundException, IOException
	{
	    this( conn, (ObjectName)null );
	}
    
    protected abstract void mbeanRegistered( final ObjectName objectName );
    protected abstract void mbeanUnregistered( final ObjectName objectName );
        
        public void
	handleNotification( final Notification notifIn, final Object handback)
	{
		if ( ! (notifIn instanceof MBeanServerNotification) )
		{
		    throw new IllegalArgumentException( notifIn.toString() );
		}
		
		final MBeanServerNotification notif	= (MBeanServerNotification)notifIn;
		final ObjectName objectName	= notif.getMBeanName();
		final String     type	= notif.getType();
		
		final boolean matchesFilter   = (mRegUnregFilter == null) ||
			JMXUtil.matchesPattern( mDefaultDomain, mRegUnregFilter, objectName );
			
		if ( matchesFilter )
		{
            if ( type.equals( MBeanServerNotification.REGISTRATION_NOTIFICATION  ) )
            {
                mbeanRegistered( objectName );
            }
            else if ( type.equals( MBeanServerNotification.UNREGISTRATION_NOTIFICATION  ) )
            {
                mbeanUnregistered( objectName );
            }
        }
	}
}





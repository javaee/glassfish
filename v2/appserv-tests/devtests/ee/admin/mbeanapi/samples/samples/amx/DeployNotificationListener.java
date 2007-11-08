/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package samples.amx;

import java.util.Map;

import javax.management.NotificationListener;
import javax.management.Notification;

import com.sun.appserv.management.deploy.DeploymentStatus;
import com.sun.appserv.management.deploy.DeploymentMgr;
import com.sun.appserv.management.deploy.DeploymentSupport;
import com.sun.appserv.management.deploy.DeploymentProgress;


/**
	A JMX NotificationListener for deployment.
 */
public final class DeployNotificationListener
		implements NotificationListener
{
	private final Object		mDeployID;
	private boolean				mIsCompleted;
	private DeploymentStatus	mDeploymentStatus;
	
		public
	DeployNotificationListener( final Object	deployID )
	{
		mDeployID		= deployID;
		mIsCompleted	= false;
	}
	
		public boolean
	isCompleted()
	{
		return( mIsCompleted );
	}
	
		public DeploymentStatus
	getDeploymentStatus()
	{
		return( mDeploymentStatus );
	}
	
		public synchronized void
	handleNotification(
		final Notification	notif, 
		final Object		handback) 
	{
		try
		{
			realHandleNotification( notif, handback );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
		Note that Notifications are not guaranteed to be delivered in order.
		Thus, it is theoretically possible for a DEPLOYMENT_COMPLETED_NOTIFICATION_TYPE
		to be received before a DEPLOYMENT_STARTED_NOTIFICATION_TYPE.
	 */
		public void
	realHandleNotification(
		final Notification	notif, 
		final Object		handback) 
	{
		final String	type	= notif.getType();
		final Map		m	= (Map)notif.getUserData();
		final Object	deployID	= m.get( DeploymentMgr.NOTIF_DEPLOYMENT_ID_KEY );
		
		if ( deployID.equals( mDeployID ) )
		{
			if ( type.equals( DeploymentMgr.DEPLOYMENT_STARTED_NOTIFICATION_TYPE ) )
			{
				SampleUtil.println( "Deployment started for " + deployID);
			}
			else if ( type.equals( DeploymentMgr.DEPLOYMENT_COMPLETED_NOTIFICATION_TYPE ) )
			{
				final Map	statusData	= (Map)
					m.get( DeploymentMgr.NOTIF_DEPLOYMENT_COMPLETED_STATUS_KEY );
				
				final DeploymentStatus	status	= 
					DeploymentSupport.mapToDeploymentStatus( statusData );
				
				SampleUtil.println( "Deployment completed for " + deployID + " with status: " + 
					status.getStageStatus() );
				
				mIsCompleted	= true;
				mDeploymentStatus	= status;
			}
			else if ( type.equals( DeploymentMgr.DEPLOYMENT_PROGRESS_NOTIFICATION_TYPE ) )
			{
				final Map	progressData	= (Map)
					m.get( DeploymentMgr.NOTIF_DEPLOYMENT_PROGRESS_KEY );
				
				final DeploymentProgress	progress	= 
					DeploymentSupport.mapToDeploymentProgress( progressData );
					
				SampleUtil.println( "Deployment progress for " + deployID + " = " + 
					progress.getProgressPercent() + "%" );
			}
		}
	}
}
	
	
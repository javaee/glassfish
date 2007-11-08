package com.sun.enterprise.admin.mbeanapi.deployment;

import java.io.*;
import java.util.*;

import javax.management.Notification;
import javax.management.NotificationListener;

import com.sun.appserv.management.deploy.DeploymentMgr;
import com.sun.appserv.management.deploy.DeploymentProgress;
import com.sun.appserv.management.deploy.DeploymentStatus;
import com.sun.appserv.management.deploy.DeploymentSupport;

/**
 */
public class DeployListener implements NotificationListener
{
    private Object              mDeployID;
    private boolean	            mIsCompleted;
    private DeploymentStatus    mFinalStatus;

    public DeployListener()
    {
        mDeployID		= null;
        mIsCompleted	= false;
    }
   
    public boolean isCompleted()
    {
        return( mIsCompleted );
    }
		
    public synchronized void handleNotification(
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
		
    public synchronized DeploymentStatus getFinalStatus()
    {
        return mFinalStatus;
    }

    private synchronized void setFinalStatus(final DeploymentStatus status)
    {
        mFinalStatus = status;
    }

    public void realHandleNotification(
        final Notification	notif, 
        final Object		handback) 
    {
		final String 	type	= notif.getType();
		final Map 		m		= (Map)notif.getUserData();
		final Object deployID	= m.get(DeploymentMgr.NOTIF_DEPLOYMENT_ID_KEY);

		System.err.println( deployID + ": received " + type );
	
		if ( type.equals( DeploymentMgr.DEPLOYMENT_STARTED_NOTIFICATION_TYPE ) )
		{
			assert( mDeployID == null );
			mDeployID	= deployID;
		}
		else if ( deployID.equals( mDeployID ) )
		{
			assert( deployID != null && deployID.equals( mDeployID ) );
			System.err.println( 
				"DeployCmd.handleNotification: " + deployID + ": " + type );
				
			if ( type.equals( 
					DeploymentMgr.DEPLOYMENT_COMPLETED_NOTIFICATION_TYPE ) )
			{
				final Map	statusData	= (Map)
				m.get( DeploymentMgr.NOTIF_DEPLOYMENT_COMPLETED_STATUS_KEY );
					
				DeploymentStatus status	= 
					DeploymentSupport.mapToDeploymentStatus( statusData );
					
				assert( mDeployID.equals( deployID ) )  :
					"deploy ID mismatch: " + mDeployID + " != " + deployID;
					
                setFinalStatus(status);

				mIsCompleted	= true;
			}
			else if ( type.equals( 
						DeploymentMgr.DEPLOYMENT_ABORTED_NOTIFICATION_TYPE ) )
			{
				Throwable t = new Exception( deployID + 
					": aborted. Please check server log for details." );

                final Map statusData = new HashMap();
                statusData.put(DeploymentStatus.STAGE_STATUS_KEY, 
                        new Integer(DeploymentStatus.STATUS_CODE_FAILURE));
                statusData.put(DeploymentStatus.STAGE_STATUS_MESSAGE_KEY, 
                        t.getMessage());
                statusData.put(DeploymentStatus.STAGE_THROWABLE_KEY, t);
                final DeploymentStatus status = DeploymentSupport.
                    mapToDeploymentStatus( statusData );

                setFinalStatus(status);

				mIsCompleted    = true;
			}
			else if ( type.equals( 
						DeploymentMgr.DEPLOYMENT_PROGRESS_NOTIFICATION_TYPE ) )
			{
				final Map	progressData	= (Map)
				m.get( DeploymentMgr.NOTIF_DEPLOYMENT_PROGRESS_KEY );

				final DeploymentProgress	progress	= 
					DeploymentSupport.mapToDeploymentProgress( progressData );

				System.err.println( 
					deployID + ": " + progress.getProgressPercent() + "%" );
			}
			else
			{
				assert( false ) : 
					"Unknown deployment notification type: " + type;
			}
		}
    }
}

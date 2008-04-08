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
package com.sun.appserv.management.helper;

import java.util.Map;
import java.io.Serializable;

import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.ListenerNotFoundException;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.misc.TypeCast;

import com.sun.appserv.management.deploy.DeploymentStatus;
import com.sun.appserv.management.deploy.DeploymentMgr;
import static com.sun.appserv.management.deploy.DeploymentMgr.*;
import com.sun.appserv.management.deploy.DeploymentSupport;
import com.sun.appserv.management.deploy.DeploymentProgress;


/**
    A NotificationListener designed to listen to the {@link DeploymentMgr}.
    <p>
	Note that Notifications are not guaranteed to be delivered in order.
	Thus, it is theoretically possible for a DEPLOYMENT_COMPLETED_NOTIFICATION_TYPE
	to be received before a DEPLOYMENT_STARTED_NOTIFICATION_TYPE.
	<p>
	A subclass may choose to override {@link #deploymentStarted},
	{@link #deploymentProgress} and 
	@since AppServer 9.0
 */
public class DeployNotificationListener
    implements NotificationListener
{
	private final Object		mDeployID;
	private boolean				mIsCompleted;
	private DeploymentStatus	mDeploymentStatus;
	private DeploymentProgress  mDeploymentProgress;
	private final DeploymentMgr mDeploymentMgr;
	
		public
	DeployNotificationListener(
	    final DeploymentMgr deploymentMgr,
	    final Object        deployID )
	{		
		if ( deploymentMgr == null || deployID == null )
		{
		    throw new IllegalArgumentException();
		}

		mIsCompleted	= false;
		mDeploymentStatus    = null;
		mDeploymentProgress  = null;
		mDeploymentMgr  = deploymentMgr;
		mDeployID       = deployID;
		mDeploymentMgr.addNotificationListener( this, null, null );
	}
	
	/**
	    @return the DeploymentMgr in use for deployment.
	 */
	    public DeploymentMgr
	getDeploymentMgr()
	{
	    return mDeploymentMgr;
	}
	
	/**
	    @return the deployID this listener responds to
	 */
	    public Object
	getDeployID()
	{
	    return mDeployID;
	}
	
	/**
	    Return true if deployment has finished.
	 */
		public boolean
	isCompleted()
	{
		return( mIsCompleted );
	}
	
	
	/**
	    @return DeploymentStatus, may be null if not finished or abnormally terminated.
	 */
		public DeploymentStatus
	getDeploymentStatus()
	{
		return( mDeploymentStatus );
	}
	
	
	/**
	    @return the latest DeploymentProgress, may be null
	 */
		public DeploymentProgress
	getDeploymentProgress()
	{
		return( mDeploymentProgress );
	}
	
	/**
	    Callback for all Notifications occurring during deployment.
	    Note for public use.
	 */
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
	    Deployment has finished.  For convenience, the DeploymentStatus
	    has been extracted from the Notification.
	 */
	    protected void
    deploymentDone(
        final Notification      notif,
        final DeploymentStatus  status )
    {
		//SampleUtil.println( "Deployment completed for " + deployID + " with status: " + 
			//status.getStageStatus() );
		mIsCompleted	= true;
		mDeploymentStatus	= status;
    }
    
    /**
	    Deployment has been cancelled.  There may or may not be a
	    DeploymentStatus.
	 */
	    protected void
    deploymentAborted(
        final Notification      notif,
        final DeploymentStatus  status )
    {
		mIsCompleted	    = true;
		mDeploymentStatus	= status;
    }
    
    
	/**
	    Deployment progress.  For convenience, the DeploymentProgress
	    has been extracted from the Notification.
	 */
	    protected void
    deploymentProgress(
        final Notification       notif,
        final DeploymentProgress status )
    {
    	mDeploymentProgress	= status;
    }
	
	/**
	    Deployment has begun.  The Notification contains no additional
	    data.
	 */
	    protected void
	deploymentStarted( final Notification notif )
	{
	}
	
	/**
	    Clean things up.  This mainly means
	    removing this listener from the DeploymentMgr.
	 */
	    public void
	cleanup()
	{
		try
		{
            mDeploymentMgr.removeNotificationListener( this, null, null );
        }
        catch( ListenerNotFoundException e )
        {
        }
	}
	
	/**
	    Handle a received Notification.
	 */
		protected final void
	realHandleNotification(
		final Notification	notif, 
		final Object		handback) 
	{
		final String	type	= notif.getType();
		final Map<String,?>		m	= TypeCast.asMap( notif.getUserData() );
		final Object	deployID	= m.get( DeploymentMgr.NOTIF_DEPLOYMENT_ID_KEY );
		
		if ( deployID.equals( mDeployID ) )
		{
			if ( type.equals( DeploymentMgr.DEPLOYMENT_STARTED_NOTIFICATION_TYPE ) )
			{
			    deploymentStarted( notif );
			}
			else if ( type.equals( DeploymentMgr.DEPLOYMENT_ABORTED_NOTIFICATION_TYPE ) )
			{
				try
				{
			        deploymentAborted( notif, null );
				}
				finally
				{
				    cleanup();
				}
			}
			else if ( type.equals( DeploymentMgr.DEPLOYMENT_COMPLETED_NOTIFICATION_TYPE ) )
			{
				final Map<String,Serializable>	statusData	= (Map<String,Serializable>)
				    Util.getAMXNotificationValue( notif, NOTIF_DEPLOYMENT_COMPLETED_STATUS_KEY );
				
				final DeploymentStatus	status	= 
					DeploymentSupport.mapToDeploymentStatus( statusData );
					
				try
				{
				    deploymentDone( notif, status );
				}
				finally
				{
				    cleanup();
				}
			}
			else if ( type.equals( DeploymentMgr.DEPLOYMENT_PROGRESS_NOTIFICATION_TYPE ) )
			{
				final Map<String,Serializable>	statusData	= (Map<String,Serializable>)
				    Util.getAMXNotificationValue( notif, NOTIF_DEPLOYMENT_PROGRESS_KEY );
				    
				final DeploymentProgress	progress	= 
					DeploymentSupport.mapToDeploymentProgress( statusData );
				
            	deploymentProgress( notif, progress );
			}
		}
	}
}
	
	
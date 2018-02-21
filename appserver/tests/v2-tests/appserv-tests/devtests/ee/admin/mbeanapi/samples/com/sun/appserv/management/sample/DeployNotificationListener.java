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

package com.sun.appserv.management.sample;

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
	
	

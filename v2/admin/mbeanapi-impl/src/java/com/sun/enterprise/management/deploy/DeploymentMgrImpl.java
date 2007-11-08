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
package com.sun.enterprise.management.deploy;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.management.ObjectName;
import javax.management.Notification;


import com.sun.appserv.management.base.Utility;
import com.sun.appserv.management.base.Singleton;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.deploy.DeploymentMgr;

import com.sun.enterprise.management.support.UniqueIDGenerator;

import com.sun.appserv.management.deploy.DeploymentSupport;
import com.sun.appserv.management.deploy.DeploymentSource;
import com.sun.appserv.management.deploy.DeploymentStatus;
import com.sun.appserv.management.deploy.DeploymentProgress;
import com.sun.appserv.management.base.UploadDownloadMgr;

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.jmx.NotificationBuilder;

import com.sun.enterprise.management.support.AMXNonConfigImplBase;


/**
	Implementation note--the design of this class is unnecessarily
	complicated due to the inclusion of certain polling-style methods
	such as takeNotifications() and getFinalDeploymentStatus().  If this
	aspect of the API can be eliminated, the implemention of this class
	as well as DeployThread can be simplified.  This limitation was
	driven by the issue of not having Notification support in the http
	connector used by the deployment team.
 */
public final class DeploymentMgrImpl extends AMXNonConfigImplBase
	implements Utility, Singleton, DeploymentMgr
{
	/**
		A Map keyed by deployID to values of DeployThread
	 */
	private final Map<Object,DeployThread>	mDeployThreads;
	
		public void
	remove( final String name )
	{
		throw new RuntimeException( "not applicable" );
	}
	
	private final UniqueIDGenerator	mDeployIDs;
	
	private long	mDeploymentCompletedNotificationSequenceNumber;
	
		public
	DeploymentMgrImpl( )
	{
		mDeployThreads	= Collections.synchronizedMap( new HashMap<Object,DeployThread>() );
		mDeployIDs		= new UniqueIDGenerator( "deploy:" );
		
		mDeploymentCompletedNotificationSequenceNumber	= 0;
	}
	
	private final Set<String>	NOTIF_TYPES	= GSetUtil.newUnmodifiableStringSet(
    		DEPLOYMENT_STARTED_NOTIFICATION_TYPE,
    		DEPLOYMENT_ABORTED_NOTIFICATION_TYPE,
    		DEPLOYMENT_PROGRESS_NOTIFICATION_TYPE,
    		DEPLOYMENT_COMPLETED_NOTIFICATION_TYPE );
	
		protected Set<String>
	getNotificationTypes( Set<String> existing )
	{
	    existing.addAll( NOTIF_TYPES );
	    return existing;
	}


	private static final long 	SECOND_MILLIS	= 60 * 1000;
	private static final long 	MINUTE_MILLIS	= 60 * SECOND_MILLIS;
	private static final long	DEPLOY_KEEP_ALIVE_MILLIS	= 15 * MINUTE_MILLIS;
	
	
		private DeployThread
	removeDeployThread( final Object	deployID)
	{
		trace( "\n###Removing deploy thread: " + deployID );
		return( (DeployThread)mDeployThreads.remove( deployID ) );
	}
	
	/**
		Cleanup any threads that have been done for a proscribed
		amount of time given by UPLOAD_KEEP_ALIVE_MILLIS.  We don't want to clean them
		up immediately because the client should have a reasonable chance to 
		get the status after completion.
	 */
		private final void
	staleDeployCheck()
	{
		final Set<Object> keySet		= mDeployThreads.keySet();
		
		synchronized( mDeployThreads )
		{
			final String[]	deployIDs	= GSetUtil.toStringArray( keySet );
		
			for( final String deployID : deployIDs )
			{
				final DeployThread	deployThread	= (DeployThread)mDeployThreads.get( deployID );
				
				if ( deployThread.getMillisSinceDone() > DEPLOY_KEEP_ALIVE_MILLIS )
				{
					removeDeployThread( deployID );
				}
			}
		}
	}
	
		private DeployThread
	addDeployThread(
		final Object				deployID )
	{
		final DeploymentCallback	callback	=
			new InternalDeploymentCallback( deployID );
			
		final DeployThread	deployThread	=
			new DeployThread( deployID, callback, null );
		mDeployThreads.put( deployThread.getID(), deployThread );
		
		return( deployThread );
	}

		public Object
	initDeploy()
	{
		final Object	deployID	= mDeployIDs.createID();
		
		final DeployThread	deployThread	= addDeployThread( deployID );
		
		return( deployID );
	}
	
		public void
	startDeploy(
		final Object	deployID,
		final Object	uploadID,
		final Object	planUploadID,
		final Map<String,String>		options)
	{
		staleDeployCheck();
		
		final UploadDownloadMgr	mgr	= getUploadDownloadMgr();
		
		final File	deployFile	= mgr.takeUpload( uploadID );
		final File	planFile	= planUploadID == null ? null : mgr.takeUpload( planUploadID );
		
		final DeployThreadParams	params	=
			new DeployThreadParams(
				getQueryMgr(),
				options,
				deployFile,
				planFile );
		
		startDeploy( deployID, params );
	}
	
    public void
    startDeploy(
        Object deployID,
        Map<String,? extends Serializable> sourceData,
        Map<String,? extends Serializable> planData,
        Map<String,String> options)
	{
		final DeploymentSource	source	=
			DeploymentSupport.mapToDeploymentSource( sourceData );
			
		final DeploymentSource	plan	= planData == null ?
			null : DeploymentSupport.mapToDeploymentSource( planData );
			
		final DeployThreadParams	params	=
			new DeployThreadParams( getQueryMgr(), options, source, plan );
		
		startDeploy( deployID, params );
	}
	
		private void
	startDeploy(
		final Object				deployID,
		final DeployThreadParams	params )
	{
		final DeployThread	deployThread	= getDeployThread( deployID );
		if ( deployThread == null )
		{
			throw new IllegalArgumentException( deployID.toString() );
		}
		deployThread.setParams( params );
		
		/**
			Issue a DEPLOYMENT_STARTED_NOTIFICATION_TYPE *before*
			starting the thread, because a client could receive
			other Notifications (even "done") before the "started" one.
		 */
		issueDeploymentStartedNotification( deployID );
		
		deployThread.start();
	}

	   	private DeployThread
	getDeployThread( Object deployID )
	{
		final DeployThread	deployThread	= (DeployThread)mDeployThreads.get( deployID );
		if ( deployThread == null )
		{
			final IllegalArgumentException	e	= new IllegalArgumentException( "" + deployID );
			
			e.printStackTrace();
			throw e;
		}
		return( deployThread );
	}
	
		private boolean
	notifsAreDone( final Notification[] notifs )
	{
		boolean	done	= false;
		
		for( int i = notifs.length -1; i >= 0; --i )
		{
			final String	notifType	= notifs[ notifs.length - 1].getType();
		
			if ( notifType.equals( DEPLOYMENT_COMPLETED_NOTIFICATION_TYPE ) ||
						notifType.equals( DEPLOYMENT_ABORTED_NOTIFICATION_TYPE ) )
			{
				done	= true;
				break;
			}
		}
		
		return( done );
	}
    
   		public Notification[]
    takeNotifications( final Object	deployID)
    {
		final DeployThread	deployThread	= getDeployThread( deployID );
		
		final Notification[]	notifs	= deployThread.takeNotifications();
		
		return( notifs );
    }
	
		public boolean
	abortDeploy(final Object deployID)
	{
		final DeployThread	deployThread	= getDeployThread( deployID );
		
		final boolean	abortedSuccessfully	= deployThread.quit();
		
		issueDeploymentAbortedNotification( deployID );
		
		return( abortedSuccessfully );
	}
	
	
		private void
	issueNotification(
		final Object		deployID,
		final Notification	notif )
	{
		// send it, for normal callers
		sendNotification( notif );
		
		trace( "\nDeploymentMgrImpl.issueNotification: sent notification for " +
			deployID + " = " + notif.toString() );
		
		// queue it, for pollers
		final DeployThread	deployThread	= getDeployThread( deployID );
		deployThread.queueNotification( notif );
	}
	
	/** 
	 */
		protected void
	issueDeploymentStartedNotification( final Object deployID )
	{
		final NotificationBuilder	builder	=
			getNotificationBuilder( DEPLOYMENT_STARTED_NOTIFICATION_TYPE );
		
		final Notification	notif	= builder.buildNew( );
		builder.putMapData( notif, NOTIF_DEPLOYMENT_ID_KEY, (Serializable)deployID );
		
		issueNotification( deployID, notif );
	}
	
	/** 
	 */
		protected void
	issueDeploymentDoneNotification(
		final Object			deployID,
		final DeploymentStatus	deploymentStatus )
	{
		final NotificationBuilder	builder	=
			getNotificationBuilder( DEPLOYMENT_COMPLETED_NOTIFICATION_TYPE );
		
		final Notification	notif	= builder.buildNew( );
		builder.putMapData( notif, NOTIF_DEPLOYMENT_ID_KEY, (Serializable)deployID );
		builder.putMapData( notif, NOTIF_DEPLOYMENT_COMPLETED_STATUS_KEY, (Serializable)deploymentStatus.asMap() );
		
		issueNotification( deployID, notif );
	}
	
	/** 
	 */
		protected void
	issueDeploymentProgressNotification(
		final Object				deployID,
		final DeploymentProgress	progress)
	{
		final NotificationBuilder	builder	=
			getNotificationBuilder( DEPLOYMENT_PROGRESS_NOTIFICATION_TYPE );
		
		final Notification	notif	= builder.buildNew( );
		builder.putMapData( notif, NOTIF_DEPLOYMENT_ID_KEY, (Serializable)deployID );
		builder.putMapData( notif, NOTIF_DEPLOYMENT_PROGRESS_KEY, (Serializable)progress.asMap() );
		
		issueNotification( deployID, notif );
	}
	
	/** 
	 */
		protected void
	issueDeploymentAbortedNotification( final Object deployID )
	{
		final NotificationBuilder	builder	=
			getNotificationBuilder( DEPLOYMENT_ABORTED_NOTIFICATION_TYPE );
		
		final Notification	notif	= builder.buildNew( );
		builder.putMapData( notif, NOTIF_DEPLOYMENT_ID_KEY, (Serializable)deployID );
		
		issueNotification( deployID, notif );
	}

	private final class InternalDeploymentCallback implements DeploymentCallback
	{
		final Object	mDeployID;
	
			public
		InternalDeploymentCallback( Object deployID )
		{
			mDeployID	= deployID;
		}
		
			public void
		deploymentDone(
			final DeploymentStatus	status )
		{
			issueDeploymentDoneNotification( mDeployID, status );
		}
		
			public void
		deploymentProgress(
			final DeploymentProgress	progress )
		{
			issueDeploymentProgressNotification( mDeployID, progress );
		}
	}

		public Map<String,Serializable>
	undeploy(
		final String	moduleID,
		final Map<String,String>		optionalParams)
	{
		final Undeployer		undeployer	= new Undeployer( moduleID, optionalParams );
		final DeploymentStatus	undeploymentStatus	= undeployer.undeploy();
		
		return( undeploymentStatus.asMap() );
	}

		public Map<String,Serializable>
	getFinalDeploymentStatus (Object deployID)
	{
		final DeployThread	deployThread	= removeDeployThread( deployID );
		
		if ( deployThread == null )
		{
			throw new IllegalArgumentException( deployID.toString() );
		}
		
		return( deployThread.getDeploymentStatus().asMap() );
	} 

		private UploadDownloadMgr
	getUploadDownloadMgr()
	{
		return( getDomainRoot().getUploadDownloadMgr() );
	}


		public Object
	initiateFileUpload( long totalSize )
		throws IOException
	{
		return initiateFileUpload( null, totalSize );
	}
	
		public Object
	initiateFileUpload( final String name, final long totalSize )
		throws IOException
	{
	    debug( "initiateFileUpload(" + name + ", " + totalSize + ")" );
		return( getUploadDownloadMgr().initiateUpload( name, totalSize ) );
	}

		public boolean
	uploadBytes(
		final Object	uploadID,
		final byte[]	bytes)
		throws IOException
	{
		return( getUploadDownloadMgr().uploadBytes( uploadID, bytes ) );
	}
	
	
		public Object
	initiateFileDownload(
		final String	moduleID,
		final String	filename)
		throws IOException
	{
		final DownloadFileSource	source	= new DownloadFileSource( moduleID, filename );
		final File			theFile	= source.getDownloadFile( );
		final boolean		deleteWhenDone	= source.isTempFile();
		
		return( getUploadDownloadMgr().initiateDownload( theFile, deleteWhenDone ) );
	}

    	
    /**
    	Get the total length the download will be, in bytes.
    	
     	@param downloadID the file download operation id, from initiateFileDownload()
     */
    	public long
    getDownloadLength( final Object downloadID )
    {
		return( getUploadDownloadMgr().getDownloadLength( downloadID ) );
    }
    
    
    	public byte[]
    downloadBytes(
    	final Object	downloadID,
    	final int		requestSize )
    	throws IOException
    {
		return( getUploadDownloadMgr().downloadBytes( downloadID, requestSize ) );
    }

}








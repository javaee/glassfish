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
 
/*
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/com/sun/enterprise/management/deploy/DeployThread.java,v 1.4 2006/03/17 03:24:31 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2006/03/17 03:24:31 $
 */
package com.sun.enterprise.management.deploy;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import javax.management.Notification;

import com.sun.appserv.management.deploy.DeploymentProgress;
import com.sun.appserv.management.deploy.DeploymentSource;
import com.sun.appserv.management.deploy.DeploymentStatus;

import com.sun.appserv.management.deploy.DeploymentProgressImpl;
import com.sun.appserv.management.deploy.DeploymentStatusImpl;

/**
	This implementation of DeployThread is a stub implementation designed
	to replace the normal implementation when unit tests are being run.
 */
public final class DeployThread extends Thread
{
	private DeployThreadParams				mParams;
	private final Object					mDeployID;
	private final DeploymentCallback		mDeploymentCallback;
	
	private boolean		mQuit;
	private boolean		mDone;
	private Throwable	mThrowable;
	private long		mDoneMillis;
	private DeploymentStatusImpl	mDeploymentStatus;
	
	private final List	mQueuedNotifications;
	
	
		public
	DeployThread(
		final Object				id,
		final DeploymentCallback	deploymentCallback,
		final DeployThreadParams	params )
	{
		mDeployID	= id;
		mDeploymentCallback	= deploymentCallback;
		mParams		= params;
		
		mDeploymentStatus	= null;
		mThrowable	= null;
		mDone		= false;
		
		mQuit		= false;
		
		mDoneMillis	= 0;
		
		mQueuedNotifications	= new ArrayList<Notification>();
		
	}
	
	
		public void
	setParams( final DeployThreadParams params )
	{
		if ( mParams != null )
		{
			throw new IllegalArgumentException();
		}
		
		mParams	= params;
	}
	
		public void
	queueNotification( final Notification notif )
	{
		if ( isDone() )
		{
			throw new IllegalArgumentException(
				"Notification cannot be queued after being done for: " + mDeployID);
		}
		
		synchronized( mQueuedNotifications )
		{
			mQueuedNotifications.add( notif );
		}
	}
	
		public Notification[]
	takeNotifications()
	{
		Notification[]	notifs	= null;
		
		synchronized( mQueuedNotifications )
		{
			notifs	= new Notification[ mQueuedNotifications.size() ];
			mQueuedNotifications.toArray( notifs );
			mQueuedNotifications.clear();
		}
		
		return( notifs );
	}
	
		public DeploymentStatus
	getDeploymentStatus( )
	{
		return( mDeploymentStatus );
	}
	
	
		public boolean
	quit()
	{
		mQuit	= true;
		this.interrupt();
		
		while ( ! isDone() )
		{
			try
			{
				Thread.sleep( 200 );
			}
			catch( InterruptedException e )
			{
			}
		}
		
		return( true );
	}
	
		static private void
	trace( Object o )
	{
		System.out.println( o.toString() );
	}
	
	
		private DeploymentStatusImpl
	deploy(
		final DeployThreadParams	params,
		final DeploymentCallback	callback )
		throws InterruptedException
	{
		int	percent = 0;
		
		while ( percent < 100 )
		{
			percent	+= 10;
			
			final DeploymentProgress	progress	=
				new DeploymentProgressImpl( 
					(byte)percent, "percent done", null);
				
			callback.deploymentProgress( progress );
			Thread.sleep( 1 );
		}
		
		final DeploymentStatusImpl	deploymentStatus	=
			new DeploymentStatusImpl( 
			0,
			"completed",
			"description",
			null );
		
		return( deploymentStatus );
	}

		public void
	run()
	{
		mThrowable	= null;
		mDone		= false;
		
		//trace( "DeployThread.run: starting: " + getID() );
		try
		{
			if ( mParams == null )
			{
				throw new IllegalArgumentException( "no params specified" );
			}
			
			//trace( "DeployThread.run: calling deploy() for: " + getID() );
			mDeploymentStatus	= deploy( mParams, mDeploymentCallback );
			//trace( "DeployThread.run: deploy() successful for: " + getID()  );
		}
		catch( Throwable t )
		{
			mDeploymentStatus	=
			new DeploymentStatusImpl(
				-1,
				"failure",
				"description",
				null );
				
			mThrowable	= t;
			
			mDeploymentStatus.setStageThrowable( t );
		}
		
		try
		{
			mDeploymentCallback.deploymentDone( mDeploymentStatus );
			
			// success or failure, always kill the file
			if ( mParams.getDeployFile() != null )
			{
				//trace( "DeployThread.run: deleting deploy file: " + mParams.getDeployFile() );
				mParams.getDeployFile().delete();
			}
			
			// success or failure, always kill the file
			if ( mParams.getPlanFile() != null )
			{
				//trace( "DeployThread.run: deleting plan file: " + mParams.getPlanFile() );
				mParams.getPlanFile().delete();
			}
		}
		finally
		{
			mDoneMillis	= System.currentTimeMillis();
			mDone	= true;
		}
		
		//trace( "DeployThread.run: done with: " + getID() );
	}
	
	/**
		@return the number of milliseconds since the deploy finished
	 */
		public long
	getMillisSinceDone()
	{
		return( isDone() ? (System.currentTimeMillis() - mDoneMillis) : 0  );
	}
	
	/**
		@return the ID of this DeployThread
	 */
		public Object
	getID()
	{
		return( mDeployID );
	}
	
	
	/**
		@return the ID of this DeployThread
	 */
		public DeploymentProgress
	getDeploymentProgress()
	{
		final byte		progressPercent			= 0;
		final String	description				= "<no description>";
		
		final DeploymentProgressImpl	progress	=
			new DeploymentProgressImpl( progressPercent, description, null);
		
		return( progress );
	}
	
	
	/**
		@return true if done (success or failure)
	 */
		public boolean
	isDone()
	{
		return( mDone );
	}
	
	/**
		@return true if done and success
	 */
		public boolean
	getSuccess()
	{
		return( isDone() && mDeploymentStatus != null );
	}
	
	/**
		Return any Throwabe that caused a failure
		
		@return the Throwable, or null if none.
	 */
		public Throwable
	getThrowable()
	{
		return( mThrowable );
	}
}















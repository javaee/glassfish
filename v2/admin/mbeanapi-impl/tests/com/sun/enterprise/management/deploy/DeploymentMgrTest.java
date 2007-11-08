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

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;

import javax.management.ObjectName;
import javax.management.AttributeList;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.ListenerNotFoundException;

import com.sun.appserv.management.deploy.DeploymentMgr;
import com.sun.appserv.management.deploy.DeploymentMgr;
import com.sun.appserv.management.deploy.DeploymentProgress;
import com.sun.appserv.management.deploy.DeploymentStatus;
import com.sun.appserv.management.deploy.DeploymentSupport;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.DeployedItemRefConfigCR;


import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.TypeCast;


import com.sun.enterprise.management.AMXTestBase;
import com.sun.enterprise.management.Capabilities;

import com.sun.enterprise.management.PropertyKeys;
import com.sun.enterprise.management.Capabilities;


/**
 */
public final class DeploymentMgrTest extends AMXTestBase
{
		public
	DeploymentMgrTest( )
		throws IOException
	{
	}
	
	    public static Capabilities
	getCapabilities()
	{
	    return getOfflineCapableCapabilities( false );
	}
	
	
	private static final class MyNotificationListener
		implements NotificationListener
	{
		private final Object	mDeployID;
		private boolean			mIsCompleted;
		
			public
		MyNotificationListener( final Object	deployID )
		{
			mDeployID		= deployID;
			mIsCompleted	= false;
		}
		
			public boolean
		isCompleted()
		{
			return( mIsCompleted );
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
		
			public void
		realHandleNotification(
			final Notification	notif, 
			final Object		handback) 
		{
			final String	type	= notif.getType();
			final Map<String,Serializable>		m	= TypeCast.asMap( notif.getUserData() );
			final Object	deployID	= m.get( DeploymentMgr.NOTIF_DEPLOYMENT_ID_KEY );
			
			//trace( deployID + ": received " + type );
			if ( deployID.equals( mDeployID ) )
			{
				if ( type.equals( DeploymentMgr.DEPLOYMENT_STARTED_NOTIFICATION_TYPE ) )
				{
				}
				else if ( deployID.equals( mDeployID ) )
				{
					assert( deployID != null && deployID.equals( mDeployID ) );
					//trace( "DeploymentMgrTest.handleNotification: " + deployID + ": " + type );
					
					if ( type.equals( DeploymentMgr.DEPLOYMENT_COMPLETED_NOTIFICATION_TYPE ) )
					{
						final Map<String,Serializable>	statusData	= TypeCast.asMap( 
							m.get( DeploymentMgr.NOTIF_DEPLOYMENT_COMPLETED_STATUS_KEY ) );
						
						final DeploymentStatus	status	= 
							DeploymentSupport.mapToDeploymentStatus( statusData );
						
						assert( mDeployID.equals( deployID ) )  :
							"deploy ID mismatch: " + mDeployID + " != " + deployID;
						
						mIsCompleted	= true;
					}
					else if ( type.equals( DeploymentMgr.DEPLOYMENT_PROGRESS_NOTIFICATION_TYPE ) )
					{
						final Map<String,Serializable>	progressData	= TypeCast.asMap( 
							m.get( DeploymentMgr.NOTIF_DEPLOYMENT_PROGRESS_KEY ) );
						TypeCast.checkMap( progressData, String.class, Serializable.class);
						
						final DeploymentProgress	progress	= 
							DeploymentSupport.mapToDeploymentProgress( progressData );
						
						//trace( deployID + ": " + progress.getProgressPercent() + "%" );
					}
					else
					{
						assert( false ) : "Unknown deployment notification type: " + type;
					}
				}
			}
		}
	}
	
	
		public DeploymentMgr
	getDeploymentMgr()
	{
		return( getDomainRoot().getDeploymentMgr() );
	}
	
	
		public Object
	uploadFile( final String name, final InputStream is  )
		throws IOException
	{
		final DeploymentMgr	mgr	= getDeploymentMgr();
		assert( mgr != null );
		//mgr.setTrace( true );
		
		final int	totalSize	= is.available();
		final int	chunkSize	= 1 + 32 * 1024;	// a screwball size
		
		final Object	uploadID	= mgr.initiateFileUpload( name, totalSize );
		int remaining	= totalSize;
		boolean	done	= false;
		while ( remaining != 0 )
		{
			final int	actual	= remaining < chunkSize ? remaining : chunkSize;
			
			final byte[]	bytes	= new byte[ actual ];
			is.read( bytes );
			done	= mgr.uploadBytes( uploadID, bytes );
			remaining	-= actual;
		}
		assert( done );
		
		return( uploadID );
	}
	
		public Object
	uploadFile( final File theFile  )
		throws IOException
	{
		final FileInputStream	is	= new FileInputStream( theFile );
		Object	id	= null;
		
		try
		{
			id	= uploadFile( theFile.getName(), is );
		}
		finally
		{
			is.close();
		}
		return( id );
	}
	
		public void
	testDownloadFile(
	    final String  moduleID,
	    final String  filename,
	    final int     chunkSize )
		throws IOException
	{
		final DeploymentMgr	mgr	=
				getDomainRoot().getDeploymentMgr();
		
		final Object	id	= mgr.initiateFileDownload( moduleID, filename );
		
		//trace( "downloading for: " + id );
		
		final int	actualChunkSize	= chunkSize < mgr.MAX_DOWNLOAD_CHUNK_SIZE ?
			chunkSize : mgr.MAX_DOWNLOAD_CHUNK_SIZE;
			
		final long	length	= mgr.getDownloadLength( id );
		long	doneSoFar	= 0;
		while ( doneSoFar < length  )
		{
			final byte[]	bytes	= mgr.downloadBytes( id, actualChunkSize );
			
			doneSoFar	+= bytes.length;
		}
	}
	
	private final int	K			= 1024;
	private final int	MEGABYTE	= K * K;
	
		public void
	XXXXtestDownloadFile1()
		throws IOException
	{
	    printVerbose( "testDownloadFile1" );
		testDownloadFile( "moduleID", "filename", MEGABYTE );
	    printVerbose( "testDownloadFile1 DONE" );
	}
	 
		public void
	testUndeployNonExistentModule()
	{
	    printVerbose( "testUndeployNonExistentModule" );
		try
		{
			getDeploymentMgr().undeploy( "does_not_exist", null );
		}
		catch( Exception e )
		{
			// good!
			final Throwable t	= ExceptionUtil.getRootCause( e );
			assert( t instanceof IllegalArgumentException );
		}
	    printVerbose( "testUndeployNonExistentModule DONE" );
	}
	
		private DeploymentStatus
	deploy( final File theFile )
		throws IOException
	{
		//printVerbose( "Uploading: " + quote( theFile ) );
		final Object		uploadID	= uploadFile( theFile );
		final DeploymentMgr	mgr	= getDeploymentMgr();
		
		final Object	deployID	= mgr.initDeploy( );
		assert( deployID instanceof String );
		final MyNotificationListener myListener	= new MyNotificationListener( deployID);
		
		DeploymentStatus	status	= null;
		mgr.addNotificationListener( myListener, null, null );
		try
		{
			final Map<String,String> options	= new HashMap<String,String>();
			options.put( DeploymentMgr.DEPLOY_OPTION_FORCE_KEY, Boolean.TRUE.toString() );
			options.put( DeploymentMgr.DEPLOY_OPTION_VERIFY_KEY, Boolean.TRUE.toString() );
			options.put( DeploymentMgr.DEPLOY_OPTION_DESCRIPTION_KEY, "test deploy" );
			options.put( DeploymentMgr.DEPLOY_OPTION_AVAILABILITY_ENABLED_KEY, Boolean.FALSE.toString() );
			options.put( DeploymentMgr.DEPLOY_OPTION_ENABLE_KEY, Boolean.TRUE.toString() );
			
			mgr.startDeploy( deployID, uploadID, null, null);
					
			printVerbose( NEWLINE + "Deploying: " + quote( theFile ) +
				", deploy options: " + MapUtil.toString( options, ", ") );
			while ( ! myListener.isCompleted() )
			{
				try
				{
					//trace( "testDeployFile: sleeping for: " + deployID);
					Thread.sleep( 500 );
				}
				catch( InterruptedException e )
				{
				}
			}
			
			final Map<String,Serializable>	deploymentStatusMap	= mgr.getFinalDeploymentStatus( deployID );
			status	= DeploymentSupport.mapToDeploymentStatus( deploymentStatusMap );
		}
		finally
		{
			try
			{
				mgr.removeNotificationListener( myListener );
			}
			catch( Exception e )
			{
			}
		}
		
		return( status );
	}
	
	    protected void
	removeAllRefs( final String moduleID )
	{
	    final Set<DeployedItemRefConfig>    refs    =
	        getQueryMgr().queryJ2EETypeNameSet( XTypes.DEPLOYED_ITEM_REF_CONFIG, moduleID );
	    
	    for( final DeployedItemRefConfig ref : refs )
	    {
	        final DeployedItemRefConfigCR container = (DeployedItemRefConfigCR)ref.getContainer();
	        
	        container.removeDeployedItemRefConfig( ref.getName() );
	    }
	}
	
		public void
	undeploy( final String moduleID )
	{
	    removeAllRefs( moduleID );
	    
		final DeploymentMgr	mgr	= getDeploymentMgr();
		
		final long	start	= now();
		final DeploymentStatus	status	= DeploymentSupport.mapToDeploymentStatus( mgr.undeploy( moduleID, null ) );
		assert( status.getStageStatus() == DeploymentStatus.STATUS_CODE_SUCCESS );
		
		printElapsed( "undeploy " + moduleID, start );
	}
	
	 private static final Set<String> ARCHIVE_SUFFIXES   =
	     GSetUtil.newUnmodifiableStringSet(
	        "ear", "war", "rar", "jar"
	     );
	     
	    private void
	addArchivesInDirectory(
	    final File       dir,
	    final List<File> archives )
	{
	    assert dir.isDirectory();
	    
	    final File[]   contents    = dir.listFiles();
	    for( final File f : contents )
	    {
	        if ( f.isDirectory() )
	        {
	            addArchivesInDirectory( f, archives );
	        }
	        else
	        {
    	        final String name   = f.getName();
    	        final int idx   = name.lastIndexOf( "." );
    	        final String suffix = (idx <= 0 ? "" : name.substring( idx + 1, name.length() )).toLowerCase();
    	        
    	        if ( ARCHIVE_SUFFIXES.contains(suffix ) )
    	        {
    	            archives.add( f );
    	        }
	        }
	    }
	}
	
		public void
	testDeployUndeployModules( )
	{
		final String	filesString	= getEnvString( PropertyKeys.ARCHIVES_TO_DEPLOY_KEY, "" ).trim();
		final String[]	names	= filesString.split( PropertyKeys.ARCHIVES_DELIM );
		
		final List<File>    archives    = new ArrayList<File>();
		for( int i = 0; i < names.length; ++i )
		{
		    names[ i ]  = names[i].trim();
		    
		    final File  f   = new File( names[ i ] );
		    if ( ! f.exists() )
		    {
		        warning( "File " + f + " does not exist" );
		    }
		    if ( f.isDirectory() )
		    {
		        addArchivesInDirectory( f, archives);
		    }
		    else
		    {
		        archives.add( f );
		    }
		}
		
		List<String> moduleIDs    = deployModules( archives );
		final List<Exception>   results = undeployModules( moduleIDs );
		
		// now deploy again and leave in place, for the use of subsequent unit tests
		moduleIDs   = deployModules( archives );
		
		// now add references to them in server
		final StandaloneServerConfig server   =
		    getDomainConfig().getStandaloneServerConfigMap().get( "server" );
		for( final String moduleID : moduleIDs )
		{
		    server.createDeployedItemRefConfig( moduleID );
		    println( "Added ref to: " + moduleID );
		}
	}
	
	
		protected List<Exception>
	undeployModules( final List<String> moduleIDs )
	{
	    final List<Exception>   results = new ArrayList<Exception>();
	    
		for ( final String moduleID : moduleIDs )
		{
		    if ( moduleID != null )
		    {
				try
				{
				    undeploy( moduleID );
				    println( "Undeployed: " + moduleID );
				    results.add( null );
				}
				catch( Exception e )
				{
				    warning( "FAILURE undeploying module " + moduleID );
				    results.add( e );
				}
		    }
		}
		return results;
	}
	
	/**
		Test deployment and undeployment of modules specified by PropertyKeys.ARCHIVES_TO_DEPLOY_KEY.
		@return moduleIDs for deployed items
	 */
		protected List<String>
	deployModules( final List<File> files )
	{
		final List<String>  moduleIDs   = new ArrayList<String>();
		
		if ( files.size() == 0 )
		{
			warning( "testDeployUndeployModules: no modules specified via property " +
				PropertyKeys.ARCHIVES_TO_DEPLOY_KEY + ", NO MODULES WILL BE DEPLOYED." );
		}
		else
		{
			final Set<String>	failedSet	= new HashSet<String>();
			final Set<String>	successSet	= new HashSet<String>();
			
			for( final File theFile : files )
			{
				try
				{
					if ( theFile.exists() )
					{
						final long	start	= now();
						final DeploymentStatus	status	= deploy( theFile );
						final long elapsed = now() - start;
						
						String msg  = "Deployed: " +
						    quote( theFile ) + " in " +  elapsed + "ms";
						if ( getVerbose() )
						{
						    msg = msg + ", DeploymentStatus = " + status;
						}
						println( msg );
						    
						if ( status.getStageStatus() != DeploymentStatus.STATUS_CODE_SUCCESS )
						{
							warning( "DeploymentMgrTest.testDeployUndeployModules: expected STATUS_CODE_SUCCESS " +
							"for " + quote( theFile.toString() ) +
							", got " + status.getStageStatus() );
							failedSet.add( theFile.toString() );
							continue;
						}
						
						String	moduleID	=
							(String)status.getAdditionalStatus().get( DeploymentStatus.MODULE_ID_KEY );
							
					    if ( moduleID == null )
					    {
					        moduleID	=(String) status.getAdditionalStatus().get( "moduleid" );
					        assert( moduleID != null );
							
    						warning( "WARNING: used 'moduleid' instead of " +
    							"DeploymentStatus.MODULE_ID_KEY (" + DeploymentStatus.MODULE_ID_KEY +
    							") as workaround for bug #6218705" );
						}
						moduleIDs.add( moduleID );
						
						successSet.add( theFile.toString() );
					}
					else
					{
						warning( "testDeployUndeployModules: file " +
						    quote( theFile.toString() ) + " does not exist." );
					}
				}
				catch( Throwable t )
				{
					warning( "Error deploying archive: " + quote( theFile.toString() ) );
				}
			}
			
			if ( failedSet.size() != 0 )
			{
				failure( "testDeployUndeployModules: failure count = " +
				    failedSet.size() + " modules: " + toString( failedSet ) );
			}
		}
	    return moduleIDs;
	}
}












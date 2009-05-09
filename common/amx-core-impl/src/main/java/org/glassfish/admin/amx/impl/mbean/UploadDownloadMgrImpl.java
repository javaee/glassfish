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
package org.glassfish.admin.amx.impl.mbean;

import org.glassfish.admin.amx.base.Singleton;
import org.glassfish.admin.amx.base.Utility;
import org.glassfish.admin.amx.base.UploadDownloadMgr;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.impl.util.UniqueIDGenerator;

import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class UploadDownloadMgrImpl extends AMXImplBase
	//implements Utility, Singleton, UploadDownloadMgr
{
	/**
		A Map keyed by uploadID to values of UploadInfo
	 */
	private final Map<Object,UploadInfo>	mUploadInfos;
	
	/**
		A Map keyed by downloadID to values of DownloadInfo
	 */
	private final Map<Object,DownloadInfo>	mDownloadInfos;
	
	
	private final UniqueIDGenerator	mUploadIDs;
	private final UniqueIDGenerator	mDownloadIDs;
	
		public
	UploadDownloadMgrImpl(final ObjectName parentObjectName)
	{
        super( parentObjectName, UploadDownloadMgr.class );
        
		mUploadInfos	= Collections.synchronizedMap( new HashMap<Object,UploadInfo>() );
		mDownloadInfos	= Collections.synchronizedMap( new HashMap<Object,DownloadInfo>() );
		
		mUploadIDs		= new UniqueIDGenerator( "upload-" );
		mDownloadIDs	= new UniqueIDGenerator( "download-" );
	}
	
	
	/**
		Cleanup any threads that have been done for a proscribed
		amount of time given by UPLOAD_KEEP_ALIVE_MILLIS.
	 */
		private final void
	staleUploadCheck()
		throws IOException
	{
		String[]	keys	= null;
		
		synchronized( mUploadInfos )
		{
			keys	= SetUtil.toStringArray( mUploadInfos.keySet() );
		}
		
		// block other threads from trying to do the same stale check
		synchronized( this )
		{
			for( int i = 0; i < keys.length; ++i )
			{
				final Object		key	= keys[ i ];
				
				final UploadInfo	info	= mUploadInfos.get( key );
				
				if ( info != null && info.getMillisSinceLastAccess() > UPLOAD_KEEP_ALIVE_MILLIS )
				{
					trace( "Cleaning up stale upload: " + info.getID() );
					mUploadInfos.remove( key );
					info.cleanup();
				}
			}
		}
	}
	
		private String
	mangleUploadName( final String name )
	{
		String	result	= name;
		
		if ( result != null )
		{
			result	= result.replaceAll( "/", "_" );
			result	= result.replaceAll( "\\\\", "_" );
			result	= result.replaceAll( ":", "_" );

		}
		
		return( result );
	}

		public Object
	initiateUpload(
		final String	name,
		final long		totalSize )
		throws IOException
	{
		staleUploadCheck();
		
		final String	actualName	= mangleUploadName( name );
		
		final UploadInfo	info	= new UploadInfo( mUploadIDs.createID(),  actualName, totalSize );
		
		mUploadInfos.put( info.getID(), info );
		
		return( info.getID() );
	}

		public boolean
	uploadBytes(
		final Object	uploadID,
		final byte[]	bytes)
		throws IOException
	{
		final UploadInfo	info	= mUploadInfos.get( uploadID );
		
		if ( info == null )
		{
			throw new IllegalArgumentException( "" + uploadID );
		}
		
		boolean	done	= false;
		synchronized( info )
		{
			done	= info.write( bytes );
		}
		
		return( done );
	}
	
	
		public File
	takeUpload( final Object uploadID )
	{
		// don't remove it until we find out it's done
		final UploadInfo	info	= mUploadInfos.get( uploadID );
		
		if ( info == null )
		{
			throw new IllegalArgumentException( "" + uploadID );
		}
		
		synchronized( info )
		{
			// by being synchronized, we can safely block any uploadBytes() activity
			// while we check for it being done
			if ( ! info.isDone() )
			{
				throw new IllegalArgumentException( "not done:" + uploadID );
			}
			
			mUploadInfos.remove( uploadID );
		}
		return( info.getFile() );
	}
	

	private static final long	SECOND_MILLIS	= 60 * 1000;
	private static final long	UPLOAD_KEEP_ALIVE_MILLIS	= 60 * SECOND_MILLIS;
	private static final long	DOWNLOAD_KEEP_ALIVE_MILLIS	= 180 * SECOND_MILLIS;
	
	


	/**
		Cleanup any downloads that have been accessed for a proscribed
		amount of time given by DOWNLOAD_KEEP_ALIVE_MILLIS.
	 */
		private final void
	staleDownloadCheck()
		throws IOException
	{
		String[]	keys	= null;
		
		synchronized( mDownloadInfos )
		{
			keys	= SetUtil.toStringArray( mDownloadInfos.keySet() );
		}
		
		// block other threads from trying to do the same stale check
		synchronized( this )
		{
			for( int i = 0; i < keys.length; ++i )
			{
				final Object		key	= keys[ i ];
				
				final DownloadInfo	info	= mDownloadInfos.get( key );
				
				if ( info != null  && info.isDone() )
				{
					mDownloadInfos.remove( key );
					trace( "Cleaning up stale download: " + info.getID() +
						"length was " + info.getLength() );
					info.cleanup();
				}
			}
		}
	}
	
	
	
		public Object
	initiateDownload(
		final File	theFile,
		boolean		deleteWhenDone )
		throws IOException
	{
		//setTrace( true );
		staleDownloadCheck();
		
		final DownloadInfo	info	=
			new DownloadInfo( mDownloadIDs.createID(),  theFile, deleteWhenDone );
		
		trace( "Created download info: " + info.getID() );
		mDownloadInfos.put( info.getID(), info );
		
		return( info.getID() );
	}
    

	   	private DownloadInfo
	getDownloadInfo( Object downloadID )
	{
		final DownloadInfo	info	= mDownloadInfos.get( downloadID );
		if ( info == null )
		{
			throw new IllegalArgumentException( "" + downloadID );
		}
		return( info );
	}

    	
    /**
    	Get the total length the download will be, in bytes.
    	
     	@param downloadID the file download operation id, from initiateFileDownload()
     */
    	public long
    getDownloadLength( final Object downloadID )
    {
    	try
    	{
			final DownloadInfo	info	= getDownloadInfo( downloadID );
			return( info.getLength() );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			assert( false );
		}
		return( 0 );
    }
    
    	public int
    getMaxDownloadChunkSize()
    {
    	return( 5 * 1024 * 1024 );
    }
    
    	public byte[]
    downloadBytes(
    	final Object	downloadID,
    	final int		requestSize )
    	throws IOException
    {
    	if ( requestSize > getMaxDownloadChunkSize() )
    	{
			trace( "Request too large: " + requestSize );
    		throw new IllegalArgumentException( "request too large: " + requestSize );
    	}
    	
		final DownloadInfo	info	= getDownloadInfo( downloadID );
		
		final byte[]	bytes	= info.read( requestSize );
		
		if ( info.isDone() )
		{
			trace( "download done: " + info.getID() );
			staleDownloadCheck();
		}
		
		return( bytes );
    }

}








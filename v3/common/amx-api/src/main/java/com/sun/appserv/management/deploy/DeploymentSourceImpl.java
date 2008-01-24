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
package com.sun.appserv.management.deploy;

import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.jar.JarInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import com.sun.appserv.management.base.MapCapableBase;
import com.sun.appserv.management.deploy.DeploymentSource;


/**
 */

public final class DeploymentSourceImpl
	extends MapCapableBase
	implements DeploymentSource
{
	/**
		Key corresponding to getArchive()
	 */
	public static final String	ARCHIVE_FILE_KEY	= "Archive";
	
	/**
		Key corresponding to isComplete()
	 */
	public static final String	IS_COMPLETE_ARCHIVE_KEY	= "IsCompleteArchive";
	
	/**
		Key corresponding to getEntriesAdded()
	 */
	public static final String	ENTRIES_ADDED_KEY	= "EntriesAdded";
	
	/**
		Key corresponding to getEntriesRemoved()
	 */
	public static final String	ENTRIES_REMOVED_KEY	= "EntriesRemoved";
	
	/**
		Key corresponding to getEntriesDeleted()
	 */
	public static final String	ENTRIES_DELETED_KEY	= "EntriesDeleted";
	
		public
	DeploymentSourceImpl( final DeploymentSource src )
	{
		this( src.asMap() );
	}
	
		public <T extends Serializable>
	DeploymentSourceImpl( final Map<String, T> m )
	{
		super( m, DEPLOYMENT_SOURCE_CLASS_NAME);
		checkValidType( m, DEPLOYMENT_SOURCE_CLASS_NAME );
		
		validateThrow();
	}
	
		public <T extends Serializable>
	DeploymentSourceImpl(
		final String	archiveFile,
		final boolean	isCompleteArchive,
		final String[]	entriesAdded,
		final String[]	entriesRemoved,
		final String[]	entriesDeleted,
		final Map<String,T>		other )
	{
		super( other, DEPLOYMENT_SOURCE_CLASS_NAME);
		
		putField( ARCHIVE_FILE_KEY, archiveFile );
		putField( IS_COMPLETE_ARCHIVE_KEY, new Boolean( isCompleteArchive ) );
		putField( ENTRIES_ADDED_KEY, entriesAdded );
		putField( ENTRIES_REMOVED_KEY, entriesRemoved );
		putField( ENTRIES_DELETED_KEY, entriesDeleted );
		
		validateThrow();
	}
	
	
		protected boolean
	validate()
	{
		boolean valid	= validateNullOrOfType( ARCHIVE_FILE_KEY, String.class );
		
		if ( valid )
		{
			valid	= validateNullOrOfType( IS_COMPLETE_ARCHIVE_KEY, Boolean.class );
		}
		
		if ( valid )
		{
			valid	= validateNullOrOfType( ENTRIES_ADDED_KEY, String[].class ) &&
						validateNullOrOfType( ENTRIES_REMOVED_KEY, String[].class ) &&
						validateNullOrOfType( ENTRIES_DELETED_KEY, String[].class );
		}
		
		return( valid );
	}
	

		public String
	getMapClassName()
	{
		return( DEPLOYMENT_SOURCE_CLASS_NAME );
	}
	
		public File
    getArchive()
    {
    	return( getFile( ARCHIVE_FILE_KEY ) );
    }
       
    	public JarInputStream
    getArchiveAsStream()
    	throws IOException
    {
    	return( new JarInputStream( new FileInputStream( getArchive() ) ) );
    }
    
		public boolean
    isCompleteArchive()
    {
    	return( getboolean( IS_COMPLETE_ARCHIVE_KEY ) );
    }
    
    	public String[]
    getEntriesAdded()
    {
    	return( getStringArray( ENTRIES_ADDED_KEY ) );
    }
    
    	public String[]
    getEntriesRemoved()
    {
    	return( getStringArray( ENTRIES_REMOVED_KEY ) );
    }
    
		public String[]
	getEntriesDeleted()
	{
    	return( getStringArray( ENTRIES_DELETED_KEY ) );
	}
}









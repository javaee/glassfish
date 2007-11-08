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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/CmdEnvImpl.java,v 1.3 2005/12/25 03:45:29 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:29 $
 */
 

package com.sun.cli.jmx.cmd;

import java.util.Properties;
import java.util.Iterator;
import java.util.Enumeration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

class ValueData
{
	Object		mValue;
	boolean		mAllowPersistence;
	
	ValueData( Object value  )
	{
		mValue				= value;
		mAllowPersistence	= false;
	}
	
	ValueData( Object value, boolean allowPersistence )
	{
		mValue					= value;
		mAllowPersistence		= allowPersistence;
	}
}

public final class CmdEnvImpl implements CmdEnv
{
	private final java.util.HashMap	mEnv;
	
	
		private final static void
	dm( Object o )
	{
		System.out.println( o.toString() );
	}
	
		public
	CmdEnvImpl()
	{
		mEnv	= new java.util.HashMap();
	}
	
		public void
	put( String key, Object value, boolean allowPersistence )
	{
		final ValueData	data	= new ValueData( value, allowPersistence);
		
		mEnv.put( key, data );
	}
	
		public Object
	get( String key )
	{
		final ValueData	data	= (ValueData)mEnv.get( key );
		final Object	value	= (data != null) ? data.mValue : null;
		
		return( value );
	}
	
		public void
	remove( String key )
	{
		mEnv.remove( key );
	}
	
		public java.util.Set
	getKeys()
	{
		return( mEnv.keySet( ) );
	}
		
		public boolean
	isPersistable( String key )
	{
		final ValueData	data			= (ValueData)mEnv.get( key );
		final boolean	isPersistable	= data != null && data.mAllowPersistence;
		
		return( isPersistable );
	}
	
			Properties
	exportProperties( )
	{
		final Properties	props	= new Properties();
		
		final Iterator iter	= getKeys().iterator();
		while( iter.hasNext() )
		{
			final String	key	= (String)iter.next();
			
			if ( isPersistable( key ) )
			{
				props.put( key, get( key ).toString() );
			}
		}
		
		return( props );
	}
	
		void
	importProperties( Properties props  )
	{
		final Enumeration propertyNames	= props.propertyNames();
		
		while ( propertyNames.hasMoreElements() )
		{
			final String	name	= (String)propertyNames.nextElement();
			final String	value	= props.getProperty( name );
			
			put( name, value, true );
		}
	}
	
	private final static String	HEADER	= "jmxadmin environment properties";
	
		void
	load( File file )
		throws IOException
	{
		FileInputStream	inputStream	= null;
		
		try
		{
			inputStream	= new FileInputStream( file );
			
			final Properties	props	= new Properties();
			props.load( inputStream );
			inputStream.close();
		
			importProperties( props );
		}
		catch( FileNotFoundException e )
		{
			// ignore--OK if it does not exist
			if ( inputStream != null )
			{
				inputStream.close();
			}
		}
	}
	
		void
	store( File file )
		throws FileNotFoundException, IOException
	{
		final Properties	props	= exportProperties( );
		
		FileOutputStream	outputStream	= null;
		
		try
		{
		 	outputStream	= new FileOutputStream( file );
			props.store( outputStream, HEADER );
			outputStream.close();
		}
		catch( Exception e )
		{
			System.err.println( "can't store environment" );
		}
		finally
		{
			if ( outputStream != null )
			{
				outputStream.close();
			}
		}
	}
	
};





	
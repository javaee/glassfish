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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/support/AliasMgrHashMapImpl.java,v 1.3 2005/12/25 03:45:43 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:43 $
 */
 

package com.sun.cli.jmx.support;

import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;

public final class AliasMgrHashMapImpl implements AliasMgrSPI
{
	final HashMap	mMap;
	String			mFilename;
	
	public final static String	DEFAULT_FILENAME	= "aliases.txt";
	
		public
	AliasMgrHashMapImpl()
	{
		mMap	= new HashMap();
	}
	
		public void
	create( String aliasName, String value ) throws Exception
	{
		mMap.put( aliasName, value );
		
		save();
	}
	
		public String
	get( String aliasName )
	{
		return( (String)mMap.get( aliasName ) );
	}
	
		public void
	delete( String aliasName )
		throws Exception
	{
		mMap.remove( aliasName );
		save();
	}
	
		public Set
	getNames()
	{
		return( mMap.keySet() );
	}
	
		public void
	save(  )
		throws IOException
	{
		if ( mFilename != null )
		{
			save( new File( mFilename ) );
		}
	}
	
		public void
	save( File theFile )
		throws IOException
	{
		mFilename	= theFile.getPath();
		
		final Set		names	= getNames();
		final Iterator	iter	= names.iterator();
		final String []	pairs	= new String [ names.size() ];
		
		for( int i = 0; i < pairs.length; ++i )
		{
			final String	name	= (String)iter.next();

			pairs[ i ]	= name + "=" + get( name );
		}
		
		final FileWriter	out	= new FileWriter( theFile );
		
		for( int i = 0; i < pairs.length; ++i )
		{
			out.write( pairs[ i ] + "\n" );
		}
		
		out.close();
	}
	
		private String
	readLine( FileReader in )
		throws IOException
	{
		StringBuffer	buf	= new StringBuffer();
		
		while ( true )
		{
			final int	i =in.read();
			if ( i < 0 )
			{
				return( null );
			}
		
			final char	theChar	= (char)i;
			if ( theChar == '\n' || theChar == '\r' )
			{
				// ignore blank lines
				if ( buf.length() == 0 )
					continue;
				break;
			}
			
			buf.append( theChar );
		}
		return( buf.toString() );
	}
	
		public void
	load( File theFile )
		throws Exception
	{
		mFilename	= theFile.getPath();
		
		final FileReader	in	= new FileReader( theFile );
		
		while ( true )
		{
			final String	pair	= readLine( in );
			if ( pair == null )
				break;
				
			final int		separatorOffset	= pair.indexOf( '=' );
			
			final String name	= pair.substring( 0, separatorOffset );
			final String value	= pair.substring( separatorOffset + 1, pair.length() );
			
			create( name, value );
		}
		
		in.close();
	}
};



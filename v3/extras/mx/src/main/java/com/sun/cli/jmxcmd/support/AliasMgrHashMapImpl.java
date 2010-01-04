/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/AliasMgrHashMapImpl.java,v 1.5 2004/10/14 19:06:28 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2004/10/14 19:06:28 $
 */
 

package com.sun.cli.jmxcmd.support;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;

import com.sun.cli.jcmd.util.misc.StringEscaper;
import com.sun.cli.jcmd.util.misc.SafeSave;
import org.glassfish.admin.amx.util.StringUtil;

public final class AliasMgrHashMapImpl implements AliasMgrSPI
{
	final Map<String,String>	mMap;
	String			mFilename;
	Flusher			mFlusher;
	final StringEscaper	mEscaper;
	
	private boolean	mNeedsFlush;
	private static long	FLUSH_INTERVAL_MILLIS	= 1 * 1000;
	
		public
	AliasMgrHashMapImpl()
	{
		mEscaper	= new StringEscaper( CHARS_NEEDING_ESCAPING );
		
		mMap	= new HashMap<String,String>();
	}
	
	public boolean	needsFlush()	{ return( mNeedsFlush ); }
	
	
	/*
		Newline and carriage return may be problems when persisting to properties file
	 */
	static final private char	NEWLINE		='\n';
	static final private char	RETURN		='\r';
	static final private String	CHARS_NEEDING_ESCAPING	= ("" + NEWLINE) + RETURN;
	
	
		private String
	escapeAliasValue( String s )
	{
		return( mEscaper.escape( s ) );
	}
	
		private String
	unescapeAliasValue( String s )
	{
		return( mEscaper.unescape( s ) );
	}
	
		public synchronized void
	create( String aliasName, String value ) throws Exception
	{
		mMap.put( aliasName, value );
		createFlusher();
	}
	
		public synchronized String
	get( String aliasName )
	{
		return( mMap.get( aliasName ) );
	}
	
		public synchronized void
	delete( String aliasName )
		throws Exception
	{
		mMap.remove( aliasName );
		createFlusher();
	}
	
		public synchronized Set<String>
	getNames()
	{
		return( mMap.keySet() );
	}
	
		public synchronized void
	save(  )
		throws IOException
	{
		if ( mFilename != null )
		{
			save( new File( mFilename ) );
		}
	}
	
	
		private void
	createFlusher()
	{
		mNeedsFlush	= true;
		new Flusher();
	}
	
	private class Flusher implements Runnable
	{
			public
		Flusher()
		{
			if ( mFlusher == null )
			{
				mFlusher	= this;
				new Thread( null, this, "AliasMgrHashMapImpl.Flusher" + this.toString() ).start();
			}
		}
		
			public void
		run()
		{
			try
			{
				Thread.sleep( FLUSH_INTERVAL_MILLIS );
			}
			catch( InterruptedException e )
			{
			}
			
			if ( needsFlush() )
			{
				try
				{
					save();
				}
				catch( IOException e )
				{
					// hmmm...
				}
			}
			
			mFlusher	= null;
		}
	}
	
		public synchronized void
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

			pairs[ i ]	= name + "=" + escapeAliasValue( get( name ) );
		}
		
		
		final File			temp	= SafeSave.getTempFile( theFile );
		final FileWriter	out		= new FileWriter( temp );
		
		for( int i = 0; i < pairs.length; ++i )
		{
			out.write( pairs[ i ] + StringUtil.LS );
		}
		
		out.close();
		
		SafeSave.replaceWithNew( theFile, temp);
		mNeedsFlush		= false;
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
	
		public synchronized void
	load( File theFile )
		throws Exception
	{
		mFilename	= theFile.getPath();
		
		final FileReader	in	= new FileReader( theFile );
		
		while ( true )
		{
			final String	line	= readLine( in );
			if ( line == null )
				break;
			try
			{
				final String pair	= unescapeAliasValue( line );
					
				final int		separatorOffset	= pair.indexOf( '=' );
				
				final String name	= pair.substring( 0, separatorOffset );
				final String value	= pair.substring( separatorOffset + 1, pair.length() );
				
				create( name, value );
			}
			catch( Exception e )
			{
				System.err.println( "Illegal alias pair: " + line );
			}
		}
		
		in.close();
	}
};



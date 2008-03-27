/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/FileUtils.java,v 1.2 2005/11/08 22:39:22 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:39:22 $
 */
 

package com.sun.cli.jcmd.util.misc;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
	The API that should be used to output from a Cmd running within the framework.
 */
public final class FileUtils
{
	private	FileUtils()	{}
	
	
		public static String
	fileToString( final File src )
		throws FileNotFoundException, IOException
	{
	    return fileToString( src, 32 * 1024 );
	}
	
		public static String
	fileToString( final File src, final int readBufferSize )
		throws FileNotFoundException, IOException
	{
		final FileReader	in	= new FileReader( src );

		final long  length  = src.length();
		if ( length > 1024 * 1024 * 1024 )
		{
		    throw new IllegalArgumentException();
		}
		
		final char[]	readBuffer	= new char[ readBufferSize ];
		
		final StringBuilder	result	= new StringBuilder( (int)length );
		try
		{
			while ( true )
			{
				final int numRead	= in.read( readBuffer, 0, readBufferSize );
				if ( numRead < 0 )
					break;
				
				result.append( readBuffer, 0, numRead );
			}
		}
		finally
		{
			in.close();
		}
		
		return( result.toString() );
	}
	
		public static void
	stringToFile( final String s, final File dest )
		throws IOException
	{
		final FileWriter	out	= new FileWriter( dest );
		
		try
		{
			out.write( s );
		}
		finally
		{
			out.close();
		}
	}
};



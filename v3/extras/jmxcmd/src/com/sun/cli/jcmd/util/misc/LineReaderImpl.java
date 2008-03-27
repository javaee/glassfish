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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/LineReaderImpl.java,v 1.1 2005/11/08 22:39:22 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2005/11/08 22:39:22 $
 */
 
package com.sun.cli.jcmd.util.misc;


import java.io.InputStream;
import java.io.InputStreamReader;

/**
	Reads a line from the specified input stream, outputs
	the prompt to System.out.
 */
public class LineReaderImpl implements LineReader
{
	final InputStreamReader	mInputStreamReader;
	
		public 
	LineReaderImpl( InputStream inputStream )
	{
		mInputStreamReader	= new InputStreamReader( inputStream );
	}

		public String
	readLine( String prompt )
		throws java.io.IOException
	{
		final StringBuffer	line	= new StringBuffer();
		
		if ( prompt != null )
		{
			System.out.print( prompt );
		}
		
		while ( true )
		{
			final int	value	= mInputStreamReader.read();
			if ( value < 0 )
			{
				if ( line.length() != 0 )
				{
					// read a line but saw EOF before a newline
					break;
				}
				return( null );
			}
			
			final char	theChar	= (char)value;
			if ( theChar == '\n' )
				break;
			
			line.append( theChar );
		}
		
		return( line.toString().trim() );
	}
}





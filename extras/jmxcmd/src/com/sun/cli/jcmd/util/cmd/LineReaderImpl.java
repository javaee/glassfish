/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/LineReaderImpl.java,v 1.3 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:18 $
 */
 
package com.sun.cli.jcmd.util.cmd;


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





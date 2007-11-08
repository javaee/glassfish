/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/appserv-tests/devtests/ee/admin/mbeanapi/samples/samples/amx/LineReaderImpl.java,v 1.1 2005/01/27 21:34:04 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2005/01/27 21:34:04 $
 */
 
package samples.amx;


import java.io.InputStream;
import java.io.InputStreamReader;

/**
	Reads a line from the specified input stream, outputs
	the prompt to System.out.
 */
public class LineReaderImpl
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





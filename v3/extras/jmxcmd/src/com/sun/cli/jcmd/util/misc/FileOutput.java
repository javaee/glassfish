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
package com.sun.cli.jcmd.util.misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.FileNotFoundException;

/**
	Directs output to a file. Lazy initialization; the file
	is not actually opened until output is sent.
 */
public final class FileOutput implements Output
{
    private PrintStream mOut;
    private final File  mFile;
    private final boolean  mAppend;
    
		public
	FileOutput( final File f )
	{
	    this( f, false );
	}
	
		public
	FileOutput( final File f, boolean append )
	{
	    mOut    = null;
	    mFile   = f;
	    mAppend = append;
	}
	
	    private void
	lazyInit()
	{
	    if ( mOut == null ) synchronized( this )
	    {
    	    if ( mOut == null )
    	    {
    	        try
    	        {
    	            mOut    = new PrintStream( new FileOutputStream( mFile, mAppend) );
    	        }
    	        catch( Exception e )
    	        {
    	            // don't use System.out/err; possible infinite recursion
    	            throw new RuntimeException( "Can't create file: " + mFile +
    	                ", exception = " + e );
    	        }
    	    }
    	}
	}
	
		public void
	print( final Object o )
	{
	    lazyInit();
	    mOut.print( o.toString() );
	}
	
		public void
	println( Object o )
	{
	    lazyInit();
	    mOut.println( o.toString() );
	}
	
		public void
	printError( final Object o )
	{
	    lazyInit();
	    println( "ERROR: " + o );
	}
	
		public boolean
	getDebug()
	{
	    lazyInit();
		return( false );
	}
	
		public void
	printDebug( final Object o )
	{
	    lazyInit();
	    println( "DEBUG: " + o );
	}
	
	
		public void
	close( )
	{
	    if ( mOut != null )
	    {
    	    try
    	    {
    	        mOut.close();
    	    }
    	    finally
    	    {
    	        mOut    = null;
    	    }
	    }
	}
};



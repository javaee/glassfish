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

package com.sun.enterprise.cli.framework;

import java.io.PrintStream;
import java.io.OutputStream;


/**
	non-public helper classes
 */
class NullOutput implements IOutput
{
	NullOutput( )
	{
	}

	public void print( String msg )
	{
		// do nothing
	}

	public void print( Object msg )
	{
		// do nothing
	}

	public void println( String msg )
	{
		// do nothing
	}

	public void println( Object msg )
	{
		// do nothing
	}

	public void close()
	{
		// do nothing
	}

	public void flush()
	{
		// do nothing
	}
}


class Output implements IOutput
{
    OutputStream mOutputStream  = null;
    PrintStream  mPrint         = null;
    boolean      mCloseWhenDone = false;

    public Output( OutputStream outputStream, boolean closeWhenDone )
		   throws Exception
    {
	mPrint  = new PrintStream( outputStream );

	// don't assign these unless we were successful creating mPrintWriter
	mOutputStream   = outputStream;
	mCloseWhenDone  = closeWhenDone;
    }

    /**
	Call to release/close the output stream when done.
	This prevents files from remaining open for a long time.
    */
    public void close()
    {
	if ( mCloseWhenDone )
	{
	    try
	    {
		mOutputStream.close();
	    }
	    catch( Exception e )
	    {
	    }
	}
	mOutputStream   = null;
	mPrint          = null;
	mCloseWhenDone  = false;
    }


    public void print( String msg )
    {
	mPrint.print( msg );
    }


    public void print( Object msg )
    {
	mPrint.println( msg.toString() );
    }


    public void println( String msg )
    {
	mPrint.println( msg );
    }


    public void println( Object msg )
    {
	mPrint.println( msg.toString() );
    }


    public void flush()
    {
        try
	{
	    mOutputStream.flush();
	}
	catch( Exception e )
	{
	}
    }
}


/**
	A base class for all different types of outputs.
 */
public abstract class GenericOutput implements IOutput
{
	private IOutput mOutput = null;


	/**
		A generic constructor with an OutputStream object and a boolean.

		@param out              is the OutputStream object to be set.
		@param closeWhenDone    is the boolen tag.
	 */
	protected GenericOutput( OutputStream out, boolean closeWhenDone )
	{
		try
		{
			if ( out != null )
			{
				mOutput = new Output( out, closeWhenDone );
			}
			else
			{
				mOutput = new NullOutput();
			}

		}
		catch ( Exception e )
		{
			mOutput = new NullOutput();
		}
	}


	/**
		Print a string.

		@param s - The String to be printed
	 */
	public void print( String message )
	{
		mOutput.print( message );
	}


	/**
		Print a string.

		@param msg - object on which toString() will be called.
	 */
	public void print( Object msg )
	{
		mOutput.print( msg.toString() );
	}


	/**
		Print a String and then terminate the line.

		@param s - The String to be printed
	 */
	public void println( String message )
	{
		mOutput.println( message );
	}


	/**
		Print a String and then terminate the line.

		@param msg - object on which toString() will be called.
	 */
	public void println( Object msg )
	{
		mOutput.println( msg.toString() );
	}


	/**
		Closes the underlying output stream.
	 */
	public void	close()
	{
		mOutput.close();
		mOutput = new NullOutput();
	}


	/**
		Flushes this output stream and forces any buffered output bytes to
		be written out.
	 */
	public void	flush()
	{
		mOutput.flush();
	}


}

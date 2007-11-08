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
package com.sun.enterprise.admin.util;

//test

// JDK includes
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

// our includes
import com.sun.enterprise.admin.util.Assert;
import com.sun.enterprise.admin.util.ExceptionUtil;
import com.sun.enterprise.admin.util.ThrowableToString;


/**
	Base functionality for output of debugging information.
 */
interface IOutput
{
 	public void		print( String msg );
 	public void		println( String msg );

 	public void		close();

 	public void		flush();
}

class NullOutput implements IOutput
{
	NullOutput( )
	{
	}

 		public void
 	print( String msg )
 	{
 		// do nothing
 	}

 		public void
 	println( String msg )
 	{
 		// do nothing
 	}

 		public void
 	close()
 	{
 		// do nothing
 	}

 		public void
 	flush()
 	{
 		// do nothing
 	}
}

/**
	non-public helper class
 */
 class Output implements IOutput
 {
 	OutputStream	mOutputStream	= null;
 	PrintStream		mPrint			= null;
 	boolean			mCloseWhenDone	= false;

 		public
 	Output(
 		OutputStream	outputStream,
 		boolean			closeWhenDone )
 		throws Exception
 	{
 		Assert.assertit( outputStream != null, "null outputStream" );

 		mPrint	= new PrintStream( outputStream );

 		// don't assign these unless we were successful creating mPrintWriter
 		mOutputStream	= outputStream;
 		mCloseWhenDone	= closeWhenDone;
 	}

 	/**
 		Call to release/close the output stream when done.

 		This prevents files from remaining open for a long time.
 	 */
 		public void
 	close()
 	{
 		if ( mCloseWhenDone )
 		{
 			try
 			{
 				mOutputStream.close();
 			}
 			catch( Exception e )
 			{
 				ExceptionUtil.ignoreException( e );
 			}
 		}
		mOutputStream	= null;
		mPrint	= null;
		mCloseWhenDone	= false;
 	}

 		public void
 	print( String msg )
 	{
 		mPrint.print( msg );
 	}

 		public void
 	println( String msg )
 	{
 		mPrint.println( msg );
 	}

 		public void
 	flush()
 	{
		try
		{
			mOutputStream.flush();
		}
		catch( Exception e )
		{
			ExceptionUtil.ignoreException( e );
		}
 	}
 }



	
/*
	Implementation notes:

	  I eliminated all tests for null by defining a NullOutput class.
	This results in a much cleaner design than testing everywhere
	for null.  There is a class invariant that "output always exists".
	Referring to (2), this also allows things to not fail during early
	system initialization.



 */
/**
	Debug provides debugging output facilities.
	<p>
	Basic use:
	1. Initialize it by calling setDefaultOutput() or setFile()
	in your main() routine or whenever you want to start it.
	2. Invoke Debug.print() or Debug.println() as desired.


	You can disable Debug output by calling setEnabled( false ).
	This will leave the output alone, but temporarily disable output.
	This is one reason everything should be routed through internalPrint().

 */
public class Debug
{
    public final static int LOW = 1,
						    MEDIUM = 2,
						    HIGH = 3,
						    kDefaultSeverity = LOW;

    private static int				sDebugLevel	= kDefaultSeverity;
    private static IOutput			sOutput		= new NullOutput();
    static
    {
    	sOutput	= createDefaultOutput();
    }
    private static boolean			sEnabled	= false;


    	private static Output
    createOutput(
    	OutputStream		outputStream,
    	boolean				closeWhenDone ) throws Exception
    {
    	// caution: don't close old before successfully creating new
    	final Output	newOutput	= new Output( outputStream, closeWhenDone);

    	// success--close possible existing output
    	cleanup();
    	sOutput	= newOutput;

    	return( newOutput );
    }

    	private static IOutput
    createDefaultOutput()
    {
    	IOutput	output	= null;

    	try
    	{
    		output	= createOutput
            ( System.err, false );
    	}
    	catch( Exception e )
    	{
    		ExceptionUtil.ignoreException( e );
    		output	= new NullOutput();
    	}
    	return( output );
    }




    	private static String
    getSeverityString( int severity )
    {
    	Assert.assertit( isValidSeverity( severity ), "illegal severity" );
    	
    	String	s	= null;

    	switch( severity )
    	{
    		case LOW:		s	= "LOW";	break;
    		case MEDIUM:	s	= "MEDIUM";	break;
    		case HIGH:		s	= "HIGH";	break;
    	}
    	return( s );
    }

    	private static String
    getPrefixString( int severity )
    {
    	return( "DEBUG: severity " + getSeverityString( severity ) + ": " );
    }

    	private static boolean
    testSeverity( int severity )
    {
    	return( severity >= sDebugLevel );
    }

    	private static void
    flush() throws Exception
    {
		sOutput.flush();
    }
    


		private static void
	internalPrintWithException(
		Object		msg,
		int			severity,
		boolean		newline ) throws Exception
	{
    	Assert.assertit( msg != null, "null msg" );

		if( testSeverity( severity ) )
		{
			final String	maybeNewline	= newline ? "\n" : "";

			final String	wholeMsg	=
				getPrefixString( severity ) + msg.toString();
			if ( newline )
			{
				sOutput.println( wholeMsg );
			}
			else
			{
				sOutput.print( wholeMsg );
			}
		}
	}

		private static void
	internalPrint( Object msg, int severity, boolean newline )
	{
    	Assert.assertit( isValidSeverity( severity ), "illegal severity" );
    	
		if ( sEnabled )
		{
			try
			{
				internalPrintWithException( msg, severity, newline );
			}
			catch( Exception e )
			{
				ExceptionUtil.ignoreException( e );
			}
		}
	}



    /**
		 Set the output file name and force new output to this file
		 immediately.
		 <p>
		 Any existing output will be flushed and closed.
	*/
    	public static void
    setFile( String name ) throws Exception
    {
    	final FileOutputStream	outputStream	= new FileOutputStream( name );

    	sOutput	= createOutput( outputStream, true );
    }

    /**
		 Set the output to the default (standard error) and force
		 new output to this immediately.
		 <p>
		 Any existing output will be flushed and closed.
	*/
    	public static void
    setDefaultOutput( )
    {
    	createDefaultOutput();
    }


    /**
		Return the current enabled status.
	*/
   		static public boolean
   	getEnabled( )
   	{
   		return( sEnabled );
   	}


    /**
		Set the enabled status.  It can be reenabled later without affecting
		the current output settings.

		@param enabled	whether Debug should be enabled or not.
	*/
   		static public void
   	setEnabled( boolean enabled )
   	{
   		sEnabled	= enabled;
   	}

		static private boolean
	isValidSeverity(int severity)
	{
		return( severity == LOW || severity == MEDIUM || severity == HIGH );
	}
	
    /**
		 Set the desired debug level cutoff value.  When a call is made,
		 output is produced only if the severity level meets or exceeds
		 the value set here.

		 @param severity	the new severity cutoff
	*/
    	public static void
    setDebugLevel(int severity )
    {
    	Assert.assertit( isValidSeverity( severity ), "illegal severity" );
    		
		sDebugLevel	= severity;
    }


    /**
		 Get the current debug level cutoff value.
	*/
    	public static int
    getDebugLevel()
    {
		return sDebugLevel;
    }


    /**
		Print the message if the severity level meets or exceeds the current
		severity cutoff.

		@param msg			object on which toString() will be called.
		@param severity		severity level (LOW, MEDIUM, HIGH)
	*/
		public static void
    print( Object msg, int severity )
    {
    	internalPrint( msg, severity, false );
    }


    /**
		Print the message if the severity level meets or exceeds the current
		severity cutoff as a new line.

		@param msg			object on which toString() will be called.
		@param severity		severity level (LOW, MEDIUM, HIGH)
	*/
    	public static void
    println( Object msg, int severity )
    {
    	internalPrint( msg, severity, true );
    }

    /**
		Print the message with severity LOW.

		@param msg			object on which toString() will be called.
	*/
    	public static void
    print( Object msg )
    {
		internalPrint( msg, kDefaultSeverity, false);
    }


    /**
		Print the message with severity LOW as a new line.

		@param msg			object on which toString() will be called.
	*/
		public static void
    println( Object msg )
    {
		internalPrint( msg, kDefaultSeverity, true );
    }

    /**
		Print this Throwable and its backtrace.


		@param e			Exception on which printStackTrace() will be called.
	*/
        public static void
    printStackTrace( Throwable e )
    {
    	Assert.assertit( e != null, "null exception" );
        
        internalPrint( new ThrowableToString( e ), HIGH, false );
    }

    /**
    	Close any open files or data.  Resets to state in which all print()
    	calls will be ignored.
	*/
    	public static void
    cleanup()
    {
		sOutput.close();
		sOutput		= new NullOutput();
    }

}


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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/ListenCmd.java,v 1.3 2005/12/25 03:45:38 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:38 $
 */
 
package com.sun.cli.jmx.cmd;

import java.io.File;
import java.io.PrintStream;

import javax.management.ObjectName;
import javax.management.AttributeChangeNotification;
import javax.management.MBeanServerNotification;
import javax.management.NotificationListener;
import javax.management.monitor.MonitorNotification;
import javax.management.Notification;

import com.sun.cli.util.stringifier.*;



class MyNotificationListener implements NotificationListener
{
	MyCmdOutput 		mOutput;
	boolean				mPaused	= false;
	SmartStringifier	mStringifier;
	
		static SmartStringifier
	setupStringifier()
	{
		// set up our options the way we want them
		final NotificationStringifier.Options options	=
					new NotificationStringifier.Options();
		options.mDelim	= "\n";
		
		// create a new registry and put our versions in place
		final StringifierRegistry	myRegistry	=
			new StringifierRegistry( StringifierRegistry.DEFAULT );
		
		// register our stringifiers with desired options
		myRegistry.add( Notification.class,
			new NotificationStringifier( options ));
		
		myRegistry.add( AttributeChangeNotification.class,
			new AttributeChangeNotificationStringifier( options ) );
		
		myRegistry.add( MBeanServerNotification.class,
			new MBeanServerNotificationStringifier( options ) );
		
		myRegistry.add( MonitorNotification.class,
			new MonitorNotificationStringifier( options ) );
			
		final SmartStringifier s	= new SmartStringifier( myRegistry, ",", true);
		
		return( s );
	}
	
		public
	MyNotificationListener( MyCmdOutput output )
	{
		mOutput			= output;
		mStringifier	= setupStringifier();
	}
	
		public void
	setOutput( MyCmdOutput output )
	{
		assert( mOutput != null );
		mOutput.close();
		mOutput	= output;
	}
	
		public void
	handleNotification( Notification notif, Object o)
	{
		if ( ! mPaused )
		{
			final String	msg	=  mStringifier.stringify( notif ) + "\n";
			
			mOutput.println( msg );
		}
	}
	
		public void
	togglePaused( )
	{
		mPaused	= ! mPaused;
	}
	
		public void
	setPaused( boolean paused )
	{
		mPaused	= paused;
	}

};

class MyCmdOutput implements CmdOutput
{
	private final CmdOutput		mFileOutput;
	private final CmdOutput		mStdOutput;
	
	MyCmdOutput()
	{
		mFileOutput	= new CmdOutputNull();
		mStdOutput	= new CmdOutputImpl( System.out, System.err );
	}
	
	MyCmdOutput( File theFile, boolean echoToStdOut )
		throws java.io.IOException
	{
		mFileOutput	= new CmdOutputToFile( theFile );
		
		if ( echoToStdOut )
		{
			mStdOutput	= new CmdOutputImpl( System.out, System.err);
		}
		else
		{
			mStdOutput	= new CmdOutputNull();
		}
	}
		public void
	print( Object o )
	{
		mFileOutput.print( o );
		mStdOutput.print( o );
	}
	
		public void
	println( Object o )
	{
		print( o + "\n" );
	}
	
		public void
	printError( Object o )
	{
		println( o );
	}
	
		public void
	printDebug( Object o )
	{
		println( o );
	}
	
		public void
	close()
	{
		if ( mFileOutput instanceof CmdOutputToFile )
		{
			((CmdOutputToFile)mFileOutput).close();
		}
	}
}
	
public class ListenCmd extends JMXCmd
{
		public
	ListenCmd( final CmdEnv env )
	{
		super( env );
	}
	
	private final static String	STOP_OPTION		= "stop";
	private final static String	PAUSE_OPTION	= "pause";
	private final static String	FILE_OPTION		= "file";
	private final static String	STDOUT_OPTION	= "stdout";
	
	static private final String	OPTIONS_INFO	=
		STOP_OPTION +
		" " + PAUSE_OPTION +
		" " + FILE_OPTION + ",1" +
		" " + STDOUT_OPTION;
	
		ArgHelper.OptionsInfo
	getOptionInfo()
		throws ArgHelper.IllegalOptionException
	{
		return( new ArgHelperOptionsInfo( OPTIONS_INFO ) );
	}
	
		int
	getNumRequiredOperands()
	{
		return( 0 );
	}
	
		public String
	getUsage()
	{
		return( CmdStrings.LISTEN_HELP.toString() );
	}
	
		public static String []
	getNames( )
	{
		return( new String [] { "listen" } );
	}
	
	
	private final static MyNotificationListener	sNotificationListener	=
			new MyNotificationListener( new MyCmdOutput( ) );
	
		void
	startListening( final String []	targets, String filename)
		throws Exception
	{
		if ( filename != null )
		{
			boolean	toStdOut	= getBoolean( STDOUT_OPTION, null ) != null;
			
			final File	theFile	= new File( filename );
			
			sNotificationListener.setOutput( new MyCmdOutput( theFile, toStdOut ) );
		}
		
		getProxy().mbeanListen( true, targets, sNotificationListener, null, null );
		sNotificationListener.setPaused( false );
	}
	
		void
	stopListening( final String []	targets )
		throws Exception
	{
		getProxy().mbeanListen( false, targets, sNotificationListener, null, null );
	}
	
		void
	pauseListening()
	{
		sNotificationListener.togglePaused( );
	}
	
	
		void
	executeInternal()
		throws Exception
	{
		final String []	targets	= getTargets();
		
		final String	fileOption	= getString( FILE_OPTION, null );
		final Boolean	stopOption	= getBoolean( STOP_OPTION, null );
		final Boolean	pauseOption	= getBoolean( PAUSE_OPTION, null );
		
		establishProxy();
		if ( stopOption != null )
		{
			if ( fileOption != null )
			{
				throw new IllegalArgumentException( STOP_OPTION + " cannot specify a file"  );
			}
			if ( ! stopOption.booleanValue() )
			{
				throw new IllegalArgumentException( STOP_OPTION + " cannot be false"  );
			}
			stopListening( targets );
		}
		else if ( pauseOption != null )
		{
			if ( fileOption != null )
			{
				throw new IllegalArgumentException( PAUSE_OPTION + " cannot specify a file"  );
			}
			if ( ! stopOption.booleanValue() )
			{
				throw new IllegalArgumentException( PAUSE_OPTION + " cannot be false"  );
			}
			
			pauseListening();
		}
		else if ( getBoolean( STOP_OPTION, null ) == null  &&
			getBoolean( PAUSE_OPTION, null ) == null )
		{
			// if it's not a pause or stop, then it's a start
			startListening( targets, fileOption );
		}
		else
		{
			throw new ArgHelper.IllegalOptionException( "illegal options" );
		}
	}
}

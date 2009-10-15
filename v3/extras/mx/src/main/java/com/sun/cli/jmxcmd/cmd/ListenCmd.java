/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/ListenCmd.java,v 1.10 2004/03/13 01:47:22 llc Exp $
 * $Revision: 1.10 $
 * $Date: 2004/03/13 01:47:22 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.io.File;

import javax.management.ObjectName;
import javax.management.AttributeChangeNotification;
import javax.management.MBeanServerNotification;
import javax.management.NotificationListener;
import javax.management.monitor.MonitorNotification;
import javax.management.Notification;

import org.glassfish.admin.amx.util.stringifier.SmartStringifier;
import org.glassfish.admin.amx.util.stringifier.StringifierRegistry;
import org.glassfish.admin.amx.util.stringifier.StringifierRegistryImpl;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;


import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.framework.CmdOutput;
import com.sun.cli.jcmd.framework.CmdOutputNull;
import com.sun.cli.jcmd.framework.CmdOutputN;
import com.sun.cli.jcmd.framework.CmdOutputToFile;
import com.sun.cli.jcmd.framework.IllegalUsageException;

import org.glassfish.admin.amx.util.jmx.stringifier.NotificationStringifier;
import org.glassfish.admin.amx.util.jmx.stringifier.AttributeChangeNotificationStringifier;
import org.glassfish.admin.amx.util.jmx.stringifier.MBeanServerNotificationStringifier;
import org.glassfish.admin.amx.util.jmx.stringifier.MonitorNotificationStringifier;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;


import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;

import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;
import org.glassfish.admin.amx.util.ArrayConversion;





	
/**
	Listens for notifications from MBeans.
 */
public class ListenCmd extends JMXCmd
{
	private static final Set<MyNotificationListener>		sListeners	= new HashSet<MyNotificationListener>();
	
	
		public
	ListenCmd( final CmdEnv env )
	{
		super( env );
	}
	
	private final static OptionInfo	FILE_OPTION		= new OptionInfoImpl( "file", "f", PATH_ARG);
	private final static OptionInfo	STDOUT_OPTION	= new OptionInfoImpl( "stdout", "t" );
	private final static OptionInfo	NAME_OPTION		= new OptionInfoImpl( "name", "n", "listener-name", true);
	
	public static final String	LISTEN_CMD	= "listen";
	public static final String	DEFINE_CMD	= "define-listener";
	public static final String	DELETE_CMD	= "delete-listener";
	public static final String	START_CMD	= "start-listener";
	public static final String	STOP_CMD	= "stop-listener";
	public static final String	LIST_CMD	= "list-listeners";
	
	static final OptionsInfoImpl	DEFINE_OPTIONS_INFO	= new OptionsInfoImpl( 
			new OptionInfo[] { FILE_OPTION, STDOUT_OPTION, NAME_OPTION } );

	
	private final static String	NAME				= "listen";
		
	static final class ListenCmdHelp extends CmdHelpImpl
	{
		private static final String	LISTENER_NAME			= "listener-name";
		private static final String	LISTENER_NAME_OP		= "<" + LISTENER_NAME + ">";
		
		public	ListenCmdHelp()	{ super( getCmdInfos() ); }
		
		private final static String	SYNOPSIS	= "listen for notifications";
		
		private final static String	LISTEN_TEXT		=
	"Listens for notifications emitted from the specified targets. " + 
	"Output is emitted to the console unless a file is specified.";
		
		
		public String	getName()		{	return( NAME ); }
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( LISTEN_TEXT ); }
	}
		public CmdHelp
	getHelp()
	{
		return( new ListenCmdHelp() );
	}
	
	private final static OperandsInfoImpl	LISTENER_NAME_OPERAND	=
		new OperandsInfoImpl( "<listener-name>", 1, 1 );
	
	private final static CmdInfo	DEFINE_INFO	=
		new CmdInfoImpl( DEFINE_CMD, DEFINE_OPTIONS_INFO, TARGETS_OPERAND_INFO );
		
	private final static CmdInfo	DELETE_INFO	=
		new CmdInfoImpl( DELETE_CMD, LISTENER_NAME_OPERAND );
		
	private final static CmdInfo	START_INFO	=
		new CmdInfoImpl( START_CMD, LISTENER_NAME_OPERAND );
		
	private final static CmdInfo	STOP_INFO	=
		new CmdInfoImpl( STOP_CMD, LISTENER_NAME_OPERAND );
		
	private final static CmdInfo	LIST_INFO	= new CmdInfoImpl( LIST_CMD );
	
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( DEFINE_INFO, DELETE_INFO, START_INFO, STOP_INFO, LIST_INFO ) );
	}
	
		boolean
	startListener( final ListenerInfo	info, CmdOutput stdout)
		throws Exception
	{
		CmdOutput	output	= null;
		
		if ( info.mFilename != null )
		{
			final File	theFile	= new File( info.mFilename );
			
			final CmdOutput	toFile		= new CmdOutputToFile( theFile );
			final CmdOutput toStdout	= info.mStdout ? stdout : (CmdOutput)new CmdOutputNull();
			output		= new CmdOutputN( new CmdOutput[] { toFile, toStdout } );
		}
		else
		{
			output	= stdout;
		}
		
		final MyNotificationListener	listener	=
					new MyNotificationListener( info, output );
		
		sListeners.add( listener );
			
		final ObjectName[] listeners	=
			getProxy().mbeanListen( true, info.mTargets, listener, null, null );
		
		if ( listeners.length != 0 )
		{
			println( "Listener " + quote( info.mName ) + " started listening to:\n" +
				ArrayStringifier.stringify( listeners, "\n" ) );
		}
		else
		{
			println( "WARNING: Listener " + quote( info.mName ) +
				" has no approprite objects to listen to." );
		}
		
		return( listeners.length == 1 );
	}
	
		void
	startListeners( final String []	listeners )
		throws Exception
	{
		stopListeners( listeners );
		
		for( int i = 0; i < listeners.length; ++i )
		{
			final String	listenerName	= listeners[ i ];
			
			final ListenerInfo	info	= getListenerInfo( listenerName );
			if ( info != null )
			{
				startListener( info, getOutput() );
			}
			else
			{
				println( "Listener has not been defined: " + listenerName );
			}
		}
	}
	
		ListenerInfo
	getListenerInfo( final String name )
	{
		ListenerInfo	info	= null;
		
		final String	data	= (String)envGet( LISTENER_PREFIX + name );
		if ( data != null )
		{
			info	= new ListenerInfo( data );
		}
		
		return( info );
	}
	
	static final String	LISTENER_PREFIX	= "LISTENER_";
	
		void
	defineListener(
		String		name,
		String[]	targets,
		String		filename,
		boolean		stdout  )
	{
		final ListenerInfo	info	= new ListenerInfo( name, targets, filename, stdout );
		
		envPut( LISTENER_PREFIX + name, info.toString(), true );
		println( "Created listener " + name );
	}
	
	
	
		Set<String>
	getAllListenerNames( )
	{
		Set<String>	names	= new HashSet<String>();
		
		final Set<String>	listeners		= getEnvKeys( LISTENER_PREFIX + ".*" );
		final int	prefixLength	= LISTENER_PREFIX.length();
		
		final Iterator<String>	iter	= listeners.iterator();
		while ( iter.hasNext() )
		{
			final String	m	= iter.next();
			
			names.add( m.substring( prefixLength, m.length() ) );
		}
		return( names );
	}
	
		Set
	getListenerNames( final String[] listenerNames )
	{
		Set	names	= new HashSet();
		
		if ( listenerNames == null || listenerNames.length == 0 )
		{
			names	= getAllListenerNames();
		}
		else
		{
			names	= ArrayConversion.arrayToSet( listenerNames );
		}
		
		return( names );
	}
	
		void
	stopListeners( final String []	listenersToStop )
		throws Exception
	{
		final Set	listenersSetToStop	= getListenerNames( listenersToStop );
		
		final Iterator<MyNotificationListener>	iter	= sListeners.iterator();
		while ( iter.hasNext() )
		{
			final MyNotificationListener	listener	= (MyNotificationListener)iter.next();
			
			if ( listenersSetToStop.contains( listener.getListenerName() ) )
			{
				println( "Stopping: " + listener.getListenerName() );
				sListeners.remove( listener );
				
				getProxy().mbeanListen( false, listener.getTargets(), listener, null, null );
			}
		}
	}
	
		void
	deleteListeners( final String[] names )
		throws Exception
	{
		stopListeners( names );
		
		for( int i = 0; i < names.length; ++i )
		{
			final String		listenerName	= names[ i ];
			
			if ( envGet( LISTENER_PREFIX + listenerName ) != null )
			{
				envRemove( LISTENER_PREFIX + listenerName );
				println( "Deleted listener: " + listenerName );
			}
			else
			{
				printError( "Listener " + quote( listenerName ) + " has not been defined." );
			}
		}
	}
	
		void
	listListeners(  )
	{
		final Set	listeners	= getAllListenerNames();
		
		if ( listeners.size() == 0 )
		{
			println( "No listeners defined." );
		}
		else
		{
			final Iterator	iter	= listeners.iterator();
			while ( iter.hasNext() )
			{
				final String	name	= (String)iter.next();
				
				final ListenerInfo	info	= getListenerInfo( name );
				showMonitor( info );
			}
		}
	}
	
		void
	showMonitor( final ListenerInfo	m )
	{
		println( "Listener: " + m.mName );
		println( "\tstatus: " + getListenerStatus( m.mName ) );
		println( "\tfilename: " + m.mFilename );
		println( "\tstdout: " + m.mStdout );
		println( "\ttargets: " + ArrayStringifier.stringify( m.mTargets, ", " ) );
	}
	
		String
	getListenerStatus( final String name )
	{
		String	status	= "";
		
		final MyNotificationListener	listener	= getListener( name );
		
		if ( listener == null )
		{
			status	= "stopped";
		}
		else
		{
			status	= "running";
		}
		
		return( status );
	}
	
		MyNotificationListener
	getListener( String name )
	{
		MyNotificationListener	listener	= null;
		
		final Iterator<MyNotificationListener>	iter	= sListeners.iterator();
		while ( iter.hasNext() )
		{
			final MyNotificationListener	l	= (MyNotificationListener)iter.next();
			
			if ( l.getListenerName().equals( name ) )
			{
				listener	= l;
				break;
			}
		}
		
		return( listener );
	}
	
	
		protected void
	executeInternal()
		throws Exception
	{
		final String	cmd	= getSubCmdNameAsInvoked();
		
		if ( cmd.equals( DEFINE_CMD ) )
		{
			final String []	targets	= getTargets();
		
			final String	nameOption		= getString( NAME_OPTION.getShortName(), null );
			final String	fileOption		= getString( FILE_OPTION.getShortName(), null );
			final boolean	stdoutOption	= getBoolean( STDOUT_OPTION.getShortName(),
													Boolean.FALSE ).booleanValue();
			defineListener( nameOption, targets, fileOption, stdoutOption );
		}
		else if ( cmd.equals( DELETE_CMD ) )
		{
			requireNumOperands( 1 );
			deleteListeners( getOperands() );
		}
		else if ( cmd.equals( START_CMD ) )
		{
			requireNumOperands( 1 );
			establishProxy();
			startListeners( getOperands() );
		}
		else if ( cmd.equals( STOP_CMD ) )
		{
			requireNumOperands( 1 );
			startListeners( getOperands() );
		}
		else if ( cmd.equals( LIST_CMD ) )
		{
			listListeners(  );
		}
		else
		{
			throw new IllegalUsageException( cmd );
		}
	}
	
	
	class ListenerInfo
	{
		public final String		mName;
		public final String		mFilename;
		public final boolean	mStdout;
		public final String[]	mTargets;
		
		ListenerInfo( String name, String[] targets,
			String filename, boolean stdout  )
		{
			mName		= name;
			mTargets	= targets;
			mFilename	= filename;
			mStdout		= stdout;
		}
		
		ListenerInfo( String data )
		{
			final String[]	pairs	= data.split( "" + DELIM );
			
			if ( pairs.length != 4 )
			{
				throw new IllegalArgumentException( "MonitorInfo requires 4 values" );
			}
			mName		= pairs[ 0 ];
			mFilename	= pairs[ 1 ].equals( "null" ) ? null : pairs[ 1 ];
			mTargets	= pairs[ 2 ].split( "" + LIST_DELIM );
			mStdout		= new Boolean( pairs[ 3 ] ).booleanValue();
		}
		
		final static char	NVDELIM		= '=';
		final static char	DELIM		= '\t';
		final static char	LIST_DELIM	= ':';
		
		/**
			Convert to a form suitable for reinstantiating.
		 */
			public String
		toString()
		{
			final String	f	=
			mName + DELIM +
			mFilename + DELIM +
			ArrayStringifier.stringify( mTargets, "" + LIST_DELIM ) + DELIM +
			mStdout;
			
			return( f );
		}
	}
	
	


static class MyNotificationListener implements NotificationListener
{
	ListenerInfo		mInfo;
	CmdOutput 			mOutput;
	SmartStringifier	mStringifier;

	
		public
	MyNotificationListener( ListenerInfo info, CmdOutput output )
	{
		mInfo			= info;
		mOutput			= output;
		mStringifier	= setupStringifier();
	}
	
	String		getListenerName()	{ return( mInfo.mName ); }
	String[]	getTargets()		{ return( mInfo.mTargets ); }
	
		static SmartStringifier
	setupStringifier()
	{
		// set up our options the way we want them
		final NotificationStringifier.Options options	=
					new NotificationStringifier.Options();
		options.mDelim	= "\n";
		
		// create a new registry and put our versions in place
		final StringifierRegistry	myRegistry	=
			new StringifierRegistryImpl( StringifierRegistryImpl.DEFAULT );
		
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
	
		public void
	setOutput( CmdOutput output )
	{
		assert( mOutput != null );
		mOutput.close();
		mOutput	= output;
	}
	
		public void
	handleNotification( Notification notif, Object o)
	{
		final String	msg	=  mStringifier.stringify( notif ) + "\n";
		
		mOutput.println( msg );
	}
};

}







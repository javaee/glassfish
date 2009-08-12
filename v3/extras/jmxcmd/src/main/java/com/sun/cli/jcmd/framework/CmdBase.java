/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdBase.java,v 1.24 2005/11/08 22:39:16 llc Exp $
 * $Revision: 1.24 $
 * $Date: 2005/11/08 22:39:16 $
 */
 

package com.sun.cli.jcmd.framework;

import java.io.IOException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.sun.cli.jcmd.JCmdKeys;

import org.glassfish.admin.amx.util.stringifier.SmartStringifier;
import org.glassfish.admin.amx.util.stringifier.Stringifier;
import org.glassfish.admin.amx.util.stringifier.StringifierRegistryImpl;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;
import com.sun.cli.jcmd.util.misc.TokenizerException;
import org.glassfish.admin.amx.util.ExceptionUtil;

import com.sun.cli.jcmd.util.cmd.ArgHelper;
import com.sun.cli.jcmd.util.cmd.ParsedOption;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import com.sun.cli.jcmd.util.cmd.ArgHelperImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionDependency;
import com.sun.cli.jcmd.util.cmd.DisallowedCmdDependency;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.ImmutableOptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionInfoCLIPValidator;
import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.LineReaderImpl;
import com.sun.cli.jcmd.util.misc.StringSource;
import com.sun.cli.jcmd.util.misc.PackageStringSources;
import com.sun.cli.jcmd.util.misc.Formatter;
import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.StringUtil;


/**
	Useful base class Cmd classes. Features:
	
	- help and version support
	- wrappers to access various CmdEnv entities
	- abstraction of ArgHelper over user-supplied command line
	- support for certain environment variables
	<p>
	Command ouput:  CmdBase implements CmdOutput by first
	looking for the environment variable CMD_OUTPUT.
	If one is found, it is used; otherwise a new one is created with debugging
	off.
	<p>
	Command tokens: it is expected that the command tokens will be found
	in the environment variable TOKENS.
	
 */
public abstract class CmdBase implements Cmd
{
	/**
		The long option name for getting help
	 */
	public static final String	HELP_OPTION_LONG	= "--help";
	/**
		The short option name for getting help
	 */
	public static final String	HELP_OPTION_SHORT	= "-?";
	/**
		The long option name for getting the version
	 */
	public static final String	VERSION_OPTION_LONG	= "--version";
	/**
		The short option name for getting the version
	 */
	public static final String	VERSION_OPTION_SHORT	= "-V";
	
	/**
		The standard "verbose" option, which may be used by any command that wants to
		support a verbose mode.
	 */
	protected static final OptionInfo	VERBOSE_OPTION	= new ImmutableOptionInfo( createVerboseOption() );
	
	/**
		Create the standard "verbose" option.
	 */
		protected static OptionInfo
	createVerboseOption()
	{
		return( new OptionInfoImpl( "verbose", "v" ) );
	}
	
	private final CmdEnv		mEnv;
	private final String []		mTokens;
	private ArgHelper			mArgHelper;
	
	private CmdOutput			mOutput;
	
	private Stringifier			mDebugStringifier	=
		new SmartStringifier( StringifierRegistryImpl.DEFAULT, "\n", false );
	
	/**
		debug message
	 */
		protected void
	dm( Object o )
	{
		if ( getDebug() )
		{
			mOutput.printDebug( mDebugStringifier.stringify( o ) );
		}
	}
	
	/**
		Instantiate a new CmdBase using the specifed environment.
		
		The tokens the command should act upon must be found in the environment
		under the key TOKENS.
		
		@param env	the CmdEnv to be used by this command
	 */
		protected
	CmdBase( final CmdEnv env )
	{
		mEnv		= env;
		
		if ( envGet( CmdEnvKeys.CMD_OUTPUT ) != null )
		{
			mOutput	= (CmdOutput)envGet( CmdEnvKeys.CMD_OUTPUT );
		}
		else
		{
			// NOTE: debugging off in this state
			mOutput	= new CmdOutputImpl( System.out, System.err );
			envPut( CmdEnvKeys.CMD_OUTPUT, mOutput, false);
		}
		
		mTokens		= (String [])env.get( CmdEnvKeys.TOKENS );
		if ( mTokens == null )
		{
			assert( false );
			throw new IllegalArgumentException( );
		}
		
		// can't instantiate here; need the result of virtual method getOptionsInfo()
		mArgHelper	= null;
	}
	

	

	/**
		Initialize the ArgHelper using the tokens that were supplied when this
		command was created.
		
		@throws		IllegalOptionException
	 */
		protected void
	initArgHelper()
		throws IllegalOptionException, IllegalUsageException, TokenizerException, CmdException
	{
		if ( mArgHelper == null )
		{
			// the first token is the subcommand name; skip it 
			final java.util.ListIterator<String>	iter	=
				Arrays.asList( mTokens ).listIterator( 1 );
			
			final OptionsInfo		optionsInfo	= getOptionsInfo();
			
			mArgHelper	= new ArgHelperImpl( iter, addGlobalOptions( optionsInfo ) );
			
			checkCmdDependencies( mArgHelper, optionsInfo );
			
			warnMultipleOption( optionsInfo.getOptionInfos() );
		}
	}
	
		protected OptionsInfo
	addGlobalOptions( OptionsInfo	optionsInfo )
		throws IllegalOptionException
	{
		final OptionsInfoImpl	amelioratedOptions	=
			new OptionsInfoImpl( optionsInfo, OptionInfoCLIPValidator.INSTANCE);
		
		amelioratedOptions.addBoolean( HELP_OPTION_LONG, HELP_OPTION_SHORT, false );
		amelioratedOptions.addBoolean( VERSION_OPTION_LONG, VERSION_OPTION_SHORT, false );
		
		return( amelioratedOptions );
	}
	
	
	/**
		Get the CmdFactory object in use.
		
		@return		the CmdFactory that created this command
	 */
		protected CmdFactory
	getCmdFactory()
	{
		return( (CmdFactory)envGet( CmdEnvKeys.CMD_FACTORY ) );
	}
		
	/**
		Get the name of this CLI.
		
		@return		the CLI name as understood by the user
	 */
		protected String
	getCLIName()
	{
		return( (String)envGet( JCmdKeys.CLI_NAME_OPTION.getLongName() ) );
	}
	
	
	
	/**
		Remove a [key, value] associated from the CmdEnv.
		
		@param key		the key for the association
	 */
		protected void
	envRemove( String key)
	{
		mEnv.remove( key );
	}
	
	/**
		Lookup a value by its key.
		
		@param key				the key to use to look up the value
		@return the value, or null if not found
	 */
		protected Object
	envGet( String key)
	{
		return( mEnv.get( key ) );
	}
	
		protected boolean
	envIsPersistable( String key )
	{
		return( mEnv.isPersistable( key ) );
	}
	
	/**
		Lookup a value by its key.
		
		@param key				the key to use to look up the value
		@param defaultValue		optional default value if not found
		@return the value, or defaultValue if not found
	 */
		protected Object
	envGet( String key, Object defaultValue )
	{
		Object value	= envGet( key );
		
		if ( value == null )
		{
			value	= defaultValue;
		}

		return( value );
	}
	
	/**
		Get all keys currently used in the CmdEnv.
		
		@return Set of all keys in the environment (Strings)
	 */
		protected Set<String>
	getEnvKeys(  )
	{
		return( mEnv.getKeys() );
	}
	
	
	/**
		Get all keys currently used in the CmdEnv that match the specified
		regular java.util.regex expression.
		
		@return Set of all keys in the environment (Strings)
	 */
		protected java.util.Set<String>
	getEnvKeys( String regExp )
	{
		final Set<String>		filteredSet	= new HashSet<String>();
		
		final Pattern	 pattern	= Pattern.compile( regExp );
		
		for( final String key : getEnvKeys() )
		{
			final Matcher	m	= pattern.matcher( key );
			
			if ( m.matches() )
			{
				filteredSet.add( key );
			}
			
		}

		return( filteredSet );
	}
	
	/**
		Insert the pair [key, value] into the environment with its
		persistence flag set to true if it should be persisted when the
		environment is saved.
		
		@param key				the key
		@param value			the value associated with key
		@param allowPersistence	true if the key/value should be persisted
	 */
		protected void
	envPut( String key, Object value, boolean allowPersistence )
	{
		mEnv.put( key, value, allowPersistence);
	}
	
	/**
		Get the ArgHelper which was created using the tokens supplied to this
		command.
		
		@return	 the ArgHelper
	 */
		ArgHelper
	getArgHelper()
	{
		return( mArgHelper );
	}
	
	/**
		Count the number of options found.
		
		@return	the number of options found
	 */
		protected int
	countOptions( )
		throws IllegalOptionException
	{
		return( getArgHelper().countOptions( ) );
	}
	
	/**
		Get an String option by name.
		
		@param name				the option name
		@param defaultValue		optional default value (may be null)
		@return	the option value or defaultValue if not found
	 */
		protected String
	getString( String name, String defaultValue)
		throws IllegalOptionException
	{
		return( getArgHelper().getStringValue( name, defaultValue ) );
	}
	
	
	/**
		Get an Integer option by name.
		
		@param name				the option name
		@param defaultValue		optional default value (may be null)
		@return	the option value or defaultValue if not found
	 */
		protected Integer
	getInteger( String name, Integer defaultValue )
		throws IllegalOptionException
	{
		return( getArgHelper().getIntegerValue( name, defaultValue ) );
	}
	
	/**
		Get a Boolean option by name.
		
		@param name				the option name
		@param defaultValue		optional default value (may be null)
		@return	the option value or defaultValue if not found
	 */
		protected Boolean
	getBoolean( String name, Boolean defaultValue)
		throws IllegalOptionException
	{
		return( getArgHelper().getBooleanValue( name, defaultValue ) );
	}
	
	
	
		protected String
	promptUser( String msg )
		throws IOException
	{
		final LineReaderImpl	reader	= new LineReaderImpl( System.in );
		
		final String password	= reader.readLine( msg );
		
		return( password );
	}
	
	/**
		Prompt the user for sensitive data.  Returning a char[]
		allows it to be zeroed. For now, it doesn't do this properly.
	 */
		protected char[]
	promptUserSecure( String msg )
		throws IOException
	{
		// should fix so that String is never used so as to avoid security issue
		return( promptUser( msg ).toCharArray() );
	}
	
	
	/**
		Return true if verbose option was specified as true, or if it was set
		in the environment. 
		
		The calling command must have specified verbose as a legal option.
		
		@return	 true if verbose, false otherwise.
	 */
		protected boolean
	getVerbose()
	{
		boolean	verbose	= false;

		try
		{
			final Boolean v	= getBoolean( VERBOSE_OPTION.getShortName(), null );
			if ( v != null )
			{
				verbose	= v.booleanValue();
			}
			else
			{
				String	value	= (String)envGet( CmdEnvKeys.VERBOSE );
				if ( Boolean.TRUE.toString().equals( value ) )
				{
					verbose	= true;
				}
			}
		}
		catch( IllegalOptionException e )
		{
			assert( false );
		}
		
		return( verbose );
	}
	
	
	/**
		Get all operands for this command invocation.
		
		@return	array of Strings, empty array if none
	 */
		protected String []
	getOperands( )
	{
		return( getArgHelper().getOperands( ) );
	}
	
	/**
		Get the subcommand name as it was typed by the user.  Some commands
		may have multiple names; this is how a Cmd can figure out the name
		by which it was invoked.
	 */
		protected String
	getSubCmdNameAsInvoked( )
	{
		return( mTokens[ 0 ] );
	}
	
	
	/**
		Same as requireNumOperands( msg )
		
		@param numRequiredOperands		number of required operands
	 */
		protected void
	requireNumOperands( final int numRequiredOperands)
		 throws WrongNumberOfOperandsException
	{
		final String []	operands	= getOperands();
		
		if ( operands.length < numRequiredOperands )
		{
			requireNumOperandsFailed( operands.length, numRequiredOperands, "");
		}
	}	
	
	/**
		Verify that the number of operands is at least the specified number
		and throw an exception if not.
		
		Subclasses may wish to call this to verify they have the minimum number
		of operands.
		
		@param numRequiredOperands		number of required operands
	 */
		protected void
	requireNumOperands( final int numRequiredOperands, String msg)
		 throws WrongNumberOfOperandsException
	{
		final String []	operands	= getOperands();
		
		if ( operands.length < numRequiredOperands )
		{
			requireNumOperandsFailed( operands.length, numRequiredOperands, msg);
		}
	}
	
	/**
		Get the minimum number of operands required for this command.  By default, no operands
		are required (but they may be present).

		@return required number of operands
	 */
		protected int
	getNumRequiredOperands()
	{
		final Class		myClass	= this.getClass();
		final String	cmdName	= getSubCmdNameAsInvoked();
		
		int	numOperands	= 0;
		
		try
		{
			final CmdInfos	infos	= getCmdInfos( myClass );
			final CmdInfo	info	= infos.get( cmdName );
			
			numOperands	= info.getOperandsInfo().getMinOperands();
		}
		catch( Exception e )
		{
			final String	msg	= getFormatter().format( PackageStrings.CmdBase_CmdImproperlyImplemented,
								cmdName, e.getClass().getName());
			printDebug( ExceptionUtil.getStackTrace( e ) );
			
			throw new IllegalArgumentException( msg );
			
		}
		
		printDebug( "num required operands: " + numOperands );
		return( numOperands );
	}
	
	
	/**
		The check for the required number of operands failed.
		
		@param numSupplied		number supplied
		@param numRequired		minimum number required
		@param msg				message to include in exception
		@throws WrongNumberOfOperandsException
	 */
		void
	requireNumOperandsFailed( int numSupplied, int numRequired, String additionalMsg )
		 throws WrongNumberOfOperandsException
	{
		final String msg	= getFormatter().format( PackageStrings.CmdBase_WrongNumOperands,
								new Integer( numSupplied ), new Integer( numRequired ),
								additionalMsg );
		throw new WrongNumberOfOperandsException( msg );
	}
	
	
	protected static final OptionsInfo	EMPTY_OPTIONS_INFO	= new OptionsInfoImpl();
	
	/**
		A few specialty commands may want to override this, but generally the options
		should exist in the CmdInfo.
	 */
		protected OptionsInfo
	getOptionsInfo()
		throws IllegalOptionException, CmdException
	{
		final CmdInfos	infos	= getMyCmdInfos();
		assert( infos != null );
		
		final CmdInfo	info	= infos.get( getSubCmdNameAsInvoked() );
		assert( info != null );
		
		return( info.getOptionsInfo() );
	}
	
		protected CmdOutput
	getOutput()
	{
		return( mOutput );
	}
	
		public void
	print( Object o )
	{
		mOutput.print( o );
	}
	
		public void
	println( Object o )
	{
		mOutput.println( o );
	}
	
		public void
	printError( Object o )
	{
		mOutput.printError( o );
	}
	
		public boolean
	getDebug()
	{
		return( mOutput.getDebug() );
	}
	
		public void
	printDebug( Object o )
	{
		mOutput.printDebug( o );
	}
	
		public void
	close()
	{
	}

		protected void
	printVerbose( Object o )
	{
		if ( getVerbose() )
		{
			println( o );
		}
	}
		
		protected static String
	quote( Object o )
	{
		return( StringUtil.quote( o ) );
	}
	
		protected static String
	quote( Object o,  char leftHandChar )
	{
		return( StringUtil.quote( o, leftHandChar ) );
	}
		
	/**
		Check that DisallowedCmdDependencies are OK.

		@throws Exception
	 */
		protected void
	checkCmdDependencies(
		final ArgHelper 	argHelper,
		final OptionsInfo	optionsInfo )
		throws IllegalUsageException, IllegalOptionException
	{
		final String	cmdName	= getSubCmdNameAsInvoked();
		
		/*
			Go through each OptionInfo, and if it has any DisallowedCmdDependency,
			verify that the option is not included
		 */
		final Iterator infoIter	= optionsInfo.getOptionInfos().iterator();
		while ( infoIter.hasNext() )
		{
			final OptionInfo	info	= (OptionInfo)infoIter.next();
			
			final Set			dependencies	= info.getDependencies();
			final Iterator		dependencyIter	= dependencies.iterator();
			while ( dependencyIter.hasNext() )
			{
				final OptionDependency	d	= (OptionDependency)dependencyIter.next();
				
				if ( d instanceof DisallowedCmdDependency &&
					argHelper.getOptionValues( info.getShortName() ) != null )
				{
					// option exists
					if ( ((DisallowedCmdDependency)d).isDisallowed( cmdName ) )
					{
						final String	msg	= getFormatter().format(
							PackageStrings.CmdBase_OptionDisallowed,  cmdName, info.getLongName() );
							
						throw new IllegalUsageException( msg );
					}
				}
			}
		}
	}
	
	/**
		Preparation before calling execute()

		@throws Exception
	 */
		protected void
	preExecute()
		throws Exception
	{
		initArgHelper();
		getArgHelper().checkRequirements();
		checkForGlobalOptions();
	}
	

	
	/**
		Check if any global options are being used.  These include:
			help	(--help or -?
			version	(--version or -V)
	 */
		protected void
	checkForGlobalOptions()
		throws IllegalOptionException,
		HelpOptionException, VersionOptionException
	{
		if ( getBoolean( HELP_OPTION_SHORT, null ) != null )
		{
			throw new HelpOptionException( getSubCmdNameAsInvoked(), "");
		}
		else if ( getBoolean( VERSION_OPTION_SHORT, null ) != null  )
		{
			throw new VersionOptionException( getSubCmdNameAsInvoked(), "");
		}
	}
	
	
		void
	checkRequiredOperands()
		throws IllegalUsageException, CmdException
	{
		final String	cmdName	= getSubCmdNameAsInvoked();
		final CmdInfo	cmdInfo	= getMyCmdInfos().get( cmdName );
		
		if ( cmdInfo != null )
		{
			if ( getOperands().length < cmdInfo.getOperandsInfo().getMinOperands() )
			{
				final String	msg	=
					getFormatter().format( PackageStrings.CmdBase_OperandsRequired,
						cmdName, new Integer( cmdInfo.getOperandsInfo().getMinOperands() ) );
				
				throw new IllegalUsageException( cmdName, msg );
			}
			
			final int	maxOperands	= cmdInfo.getOperandsInfo().getMaxOperands();
			if ( getOperands().length > maxOperands )
			{
				if ( maxOperands == 0 )
				{
					final String	msg	= getFormatter().format( PackageStrings.CmdBase_NoOperands, cmdName );
						
					throw new IllegalUsageException( cmdName, msg);
				}
				else
				{
					final String	msg	= getFormatter().format(
						PackageStrings.CmdBase_NoMoreThanOperands,
						cmdName,
						new Integer( maxOperands ) );
					
					throw new IllegalUsageException( cmdName, msg );
				}
			}
		}
	}
	
	/**
		Run the command.  The environment must contain the tokens in the variable
		TOKENS.  The first token must be the name of the subcommand.
	 */
		public final void
	execute( )
		throws Exception
	{
		try
		{
			preExecute();
			
			checkRequiredOperands();
			
			executeInternal( );
		}
		catch( Exception e )
		{
			handleException( e );
		}
	}
	
	/**
		An exception occured while processing the command.
		
		@param e	the exception
	 */
		protected void
	handleException( final Exception e ) throws Exception
	{
		// do nothing by default except propogate it
		throw e;
	}
	
	/**
		Everything has been set up; go ahead and run
	 */
	abstract protected void			executeInternal( ) throws Exception;
	
	/**
		Get a CmdHelp describing for this command.
	 */
		public CmdHelp
	getHelp( )
	{
		final String	thisClassName	= this.getClass().getName();
		final String	helpClassName	= thisClassName + "Help";
		CmdHelp			help	= null;
		
		try
		{
			final Class helpClass	= ClassUtil.getClassFromName( helpClassName );
			
			help	= (CmdHelp)helpClass.newInstance();
		}
		catch( Exception e )
		{
			// OK, it doesn't have one or we can't access it
			e.printStackTrace();
		}
		
		return( help );
	}
	
	/**
		Get a String describing the usage of this command.
	 */
		protected String
	getUsage( )
	{
		return( getHelp().toString() );
	}
		
		
	private static final Class []	EMPTY_SIG	= new Class [ 0 ];
	private static final Object []	EMPTY_ARGS	= new Object [ 0 ];
	
	/**
		Get a String[] of all the names used by this command.
		
		Expects to find a static method getNames() in the command.
		
		@param theClass	the class of the command
	 */
		public static String []
	getCmdNames( final Class theClass )
		throws java.lang.NoSuchMethodException,
			java.lang.IllegalAccessException, java.lang.reflect.InvocationTargetException
	{
		final CmdInfos	infos	= getCmdInfos( theClass );
		
		return( infos.getNames() );
	}
	
	/**
		Get a String[] of all the names used by this command.
		
		Expects to find a static method getNames() in the command.
		
		@param theClass	the class of the command
	 */
		public static CmdInfos
	getCmdInfos( final Class<?> theClass )
		throws java.lang.NoSuchMethodException,
			java.lang.IllegalAccessException, java.lang.reflect.InvocationTargetException
	{
		CmdInfos	infos	= null;
		
		final java.lang.reflect.Method	m	= theClass.getDeclaredMethod( "getCmdInfos", EMPTY_SIG );
	
		if ( m != null )
		{
			infos	= (CmdInfos)m.invoke( theClass, EMPTY_ARGS);
		}
		
		return( infos );
	}
	
		public CmdInfos
	getMyCmdInfos(  )
		throws CmdException
	{
		CmdInfos	infos	= null;
		
		try
		{
			infos	= getCmdInfos( this.getClass() );
		}
		catch( Exception e )
		{
			final String	cmd	= getSubCmdNameAsInvoked();
			final String	msg	= getFormatter().format( PackageStrings.CmdBase_CmdImproperlyImplemented,
								cmd, e.getClass().getName() );
			
			printDebug( ExceptionUtil.getStackTrace( e ) );
			throw new CmdException( cmd, msg );
		}
		
		return( infos );
	}
	
	/**
		Get a String listing all the names used by this command
		
		@param theClass	the class of the command
	 */
		public static String
	getAlsoKnownAs( Class theClass )
	{
		String	aka	= "";
		
		try
		{
			aka	= ArrayStringifier.stringify( getCmdNames( theClass ), " " );
		}
		catch( Exception e )
		{
			// Hmmm..
		}
		
		return( "Also known as: " + aka );
	}
	
		protected void
	warnMultipleOption( String name )
		throws IllegalOptionException
	{
		final ArgHelper	argHelper	= getArgHelper();
		
		final ParsedOption[]	options	= argHelper.getOptionInstances( name );
		if ( options.length >= 2 )
		{
			for( int i = 0; i < options.length - 1; ++i )
			{	
				final String	msg	=
					getFormatter().format( PackageStrings.CmdBase_WarningDuplicateOption,
						options[ i ].getName(),
						options[ i ].getValues() );
				println( msg );
			}
		}
	}
	
		Formatter
	getFormatter()
	{
		return( new Formatter( getStringSource() ) );
	}
		
	
		protected void 
	warnMultipleOption( List<OptionInfo>	optionInfos )
		throws IllegalOptionException
	{
		for( final OptionInfo info : optionInfos )
		{
			warnMultipleOption( info.getShortName() );
		}
	}
	
	/**
		Get the StringSource for CmdBase.
	 */
		protected StringSource
	getStringSource()
	{
		return( PackageStringSources.get( CmdBase.class, null) );
	}
	
	/**
		Print the usage of this command.
	 */
		public void
	printUsage()
	{
		println( getUsage() );
		
		println( getAlsoKnownAs( this.getClass() ) );
	}
	
	/**
		Get the CmdEventMgr. It may then be used to register listeners.
	 */
		protected CmdEventMgr
	getCmdEventMgr()
	{
		return( (CmdEventMgr)envGet( CmdEnvKeys.CMD_EVENT_MGR ) );
	}
	
		protected CmdHistory
	getCmdHistory()
	{
		return( (CmdHistory)envGet( CmdEnvKeys.CMD_HISTORY ) );
	}
	
		protected CmdAliasMgr
	getCmdAliasMgr()
	{
		return( (CmdAliasMgr)envGet( CmdEnvKeys.CMD_ALIAS_MGR ) );
	}
	
		protected CmdHistory
	setCmdHistory( CmdHistory history )
	{
		final CmdHistory	old	= getCmdHistory();
		envPut( CmdEnvKeys.CMD_HISTORY, history, false );
		
		return( old );
	}
	
	protected final static String	PATH_OPERAND	= "path";
	protected final static String	PATH_ARG		= PATH_OPERAND;
	protected final static String	NEWLINE			= "\n";
	
	
		protected static long
	now()
	{
		return( System.currentTimeMillis() );
	}
	
}


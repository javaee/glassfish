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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/CmdStrings.java,v 1.3 2005/12/25 03:45:32 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:32 $
 */
 
package com.sun.cli.jmx.cmd;

import java.lang.reflect.Field;
import java.util.ArrayList;
import com.sun.cli.util.StringSource;

/*
	Gets strings from CmdStrings class
 */
final class CmdStringsSource implements StringSource
{
	CmdStringsSource()
	{
	}
	
	
		public String
	getString( String id )
	{
		final String	value	= CmdStrings.getFieldValue( id );
		
		return( value );
	}
	
		public String
	getString( String id, String defaultValue )
	{
		String	value	= CmdStrings.getFieldValue( id );
		
		if ( value == null )
		{
			value	= defaultValue;
		}
		return( value );
	}
}

public final class CmdStrings
{
	private static final CmdStringsSource	GETTER = new CmdStringsSource();
	private final static String				SYNTAX_DELIM	= "\n\n";
	private final static String				CMD_DELIM	= "";
	
	
	public static final class CmdHelp
	{
		private final String	mID;
		private final String	mName;
		
			private 
		CmdHelp( String id )
		{
			mID		= id;
			mName	= id.toLowerCase( );
		}
		
			private 
		CmdHelp( String id, String name )
		{
			mID		= id;
			mName	= name;
		}
		
			public String
		getName()
		{
			return( mName );
		}
		
			public String
		getSynopsis()
		{
			return( GETTER.getString( mID + "_SYNOPSIS" ) );
		}
		
			public String
		getSyntax()
		{
			return( GETTER.getString( mID + "_SYNTAX" ) );
		}
		
			public String
		getText()
		{
			return( GETTER.getString( mID + "_TEXT" ) );
		}
		
			public String
		toString()
		{
			return( CMD_DELIM + getSynopsis() + CMD_DELIM + SYNTAX_DELIM + getSyntax() + SYNTAX_DELIM + getText() );
		}
	}
	
	public final static CmdHelp	GET_HELP			= new CmdHelp( "GET" );
	public final static CmdHelp	SET_HELP			= new CmdHelp( "SET"  );
	public final static CmdHelp	FIND_HELP			= new CmdHelp( "FIND" );
	public final static CmdHelp	INSPECT_HELP		= new CmdHelp( "INSPECT" );
	public final static CmdHelp	INVOKE_HELP			= new CmdHelp( "INVOKE" );
	public final static CmdHelp	CREATE_HELP			= new CmdHelp( "CREATE" );
	public final static CmdHelp	DELETE_HELP			= new CmdHelp( "DELETE" );
	public final static CmdHelp	COUNT_HELP			= new CmdHelp( "COUNT" );
	public final static CmdHelp	DOMAINS_HELP		= new CmdHelp( "DOMAINS" );
	public final static CmdHelp	SETENV_HELP			= new CmdHelp( "SETENV" );
	
	public final static CmdHelp	TARGET_HELP			= new CmdHelp( "TARGET" );
	public final static CmdHelp	CONFIGURE_HELP		= new CmdHelp( "CONFIGURE" );
	//public final static CmdHelp	LIST_HELP		= new CmdHelp( "LIST" );
	public final static CmdHelp	CONNECT_HELP		= new CmdHelp( "CONNECT" );
	public final static CmdHelp	LISTEN_HELP			= new CmdHelp( "LISTEN" );
	
	public final static CmdHelp	CREATE_ALIAS_HELP	= new CmdHelp( "CREATE_ALIAS", "create-alias" );
	public final static CmdHelp	DELETE_ALIAS_HELP	= new CmdHelp( "DELETE_ALIAS", "delete-alias" );
	public final static CmdHelp	RESOLVE_ALIAS_HELP	= new CmdHelp( "RESOLVE_ALIAS", "resolve-alias" );
	public final static CmdHelp	LIST_ALIASES_HELP	= new CmdHelp( "LIST_ALIASES", "list-aliases" );
	public final static CmdHelp	SOURCE_HELP			= new CmdHelp( "SOURCE", "source" );
	
	public final static CmdHelp	HELP_HELP			= new CmdHelp( "HELP" );
	
	
	private final static CmdHelp []	ALL_HELP	= _getAllHelp();
	
	
		public static CmdHelp []
	getAllHelp()
	{
		return( ALL_HELP );
	}
	
		private static CmdHelp []
	_getAllHelp()
	{
		final Field []	fields	= CmdStrings.class.getFields();
		
		final ArrayList	list	= new ArrayList();
		
		for( int i = 0; i < fields.length; ++i )
		{
			final Field	field	= fields[ i ];
			
			if ( field.getName().endsWith( "_HELP" ) )
			{
				try
				{
					final CmdHelp	help	= (CmdHelp)field.get( CmdStrings.class );
					
					list.add( help );
				}
				catch( IllegalAccessException e )
				{
					System.err.println( "can't access field: " + field.getName() );
				}
			}
		}
		
		final CmdHelp []	cmdHelps	= new CmdHelp [ list.size() ];
		list.toArray( cmdHelps );
		
		return( cmdHelps );
	}
	
		public static CmdHelp
	getHelp( String cmd )
	{
		CmdHelp	help	= null;
		
		for( int i = 0; i < ALL_HELP.length; ++i )
		{
			if ( ALL_HELP[ i ].getName().equalsIgnoreCase( cmd ) )
			{
				help	= ALL_HELP[ i ];
				break;
			}
		}
		
		return( help );
	}
	
	public final static String	USAGE		= "USAGE: ";
	public final static String	NO_SUCH_COMMAND	= "No such command: ";
	
	//-------------------- private fields ------------------------------
	
		static String
	getFieldValue( String name )
	{
		String	result	= null;
		
		try
		{
			final Field	field	= CmdStrings.class.getDeclaredField( name  );
			
			result	= (String)field.get( CmdStrings.class );
		}
		catch( Exception e )
		{
			result	= name;
		}
		
		return( result );
	}
	
	// generic JMX commands
	private final static String	GET_SYNOPSIS	= "get: display one or more attributes on the specified target(s).";
	private final static String	GET_SYNTAX		= "get attr[,attr]* [target]+";
	private final static String	GET_TEXT		= 
"Specify the attributes in a comma-separated list. " +
"The following special targets are also available:\n"+
"*  all attributes\n" +
"*r all read-only attributes\n" +
"*w all writeable attributes\n" +
"\n'get' Examples: \n" +
"get * *                    -- gets all attributes on all MBeans\n" +
"get Count,Timeout MyMBean  -- gets the Count and Timeout attributes on the MBean 'MyMBean'\n";
	
	
	private final static String	SET_SYNOPSIS	= "set: set one or more attributes on the specified target(s)";
	private final static String	SET_SYNTAX		= "set attr=value[,attr=value]* [target]+";
	private final static String	SET_TEXT		= 
"Specify a comma-separated list of name-value pairs.\n" +
"\n'set' Examples: \n" +
"set Timeout=10,Count=20 MyMBean  -- sets the Timeout attribute to 10 and Count attribute to 20 " +
"on the MBean 'MyMBean'\n";
	
	
	private final static String	FIND_SYNOPSIS	= "find: display MBeans matching name, pattern, or alias";
	private final static String	FIND_SYNTAX		= "find [--regex <expr> | --java-regex <expr>] [--current ] [--add] [--remove] [target]*";
	private final static String	FIND_TEXT		=
"Specify 0 or more names/patterns/aliases.  All matching MBeans will be displayed with their fully-qualified " +
"names. If no targets are specified or '*' is specified, then all MBeans are displayed.\n" +
"\nExamples:\n" +
"\nfind           -- displays all MBeans" +
"\nfind :*        -- displays all MBeans in the default domain" +
"\nfind type=test -- displays all MBeans whose ObjectName contains the property 'type' with value 'test'\n" +
"\nfind --regex n*=test* *:* -- displays all MBeans whose ObjectName contains a property whose name starts with 'n' and whose value starts with 'test'\n" +
"\n\nOptions:\n" +
"current -- display the results of the last find\n" +
"add -- add the results of this invocation to the current set\n" +
"remove -- remove the results of this invocation from the current set\n" +
"regex -- utilize a regular expression for MBean Property name(s) or value(s)\n" +
"\nNote on regex--the syntax used is that as documented in java.util.regex\n" +
"";
	
	private final static String	INSPECT_SYNOPSIS= "inspect: display attributes, operations, constructors, etc";
	private final static String	INSPECT_SYNTAX	= "inspect [--all ] [--summary] [--nodescription] " +
		"--attributes=[attr]+] [--operations=[op]+ ] [--constructors] [--notifications=[notif]+] [target]+";
	private final static String	INSPECT_TEXT	=
"One or more targets may be specified.  The output will be displayed for each resulting MBean. " +
"The following options are available:\n" +
"--all           display all available information\n" +
"--summary       display a summary only\n" +
"--nodescription omit descriptions (avoids clutter if there are none)\n" +
"--attrs         display the specified attributes (* = all attributes)\n" +
"--operations    display the specified operations (* = all operations)\n" +
"--constructors  display all available constructors\n" +
"--notifications display the specified notifications (* = all notifications)\n";

		
	private final static String	INVOKE_SYNOPSIS = "invoke: invokes an MBean operation";
	private final static String	INVOKE_SYNTAX	= "" +
			"cmd:[arg-value[,arg-value]*] [target]+\n" +
			"   cmd:arg-name=arg-value[,arg-name=arg-value]* [target]+";
	private final static String	INVOKE_TEXT		=
"The invoke operation is unusual in that no special command is entered. Instead, the MBean operation name is used " +
"directly with a special syntax.\n\n" +
"There are two forms of invocation--Ordered and Named.  Ordered invocation requires the parameters in " +
"the correct order as a comma-separated list. " +
"Named invocation relies on an operation's parameter names, which may or may not be " +
"available for some MBeans.  You can use the 'inspect' command to see if parameter names are available for an MBean." +
"\nNamed invocation also works for operations taking a Properties object; excess parameters are supplied to the " +
"operation in the Properties object.\n\n" +
"All forms of invocation use the MBeanInfo to determine the correct match for an operation. " +
"For an operation to be available for invocation, it must restrict its use of data " + 
"types to the following:\n" +
"   char, byte, short, int, long, float, double\n" +
"   Character, Byte, Short, Integer, Long, Float, Double, Number, BigNumber\n" +
"   String, Object, Properties\n" +
"   [] all arrays of the above types\n\n" +
"Type-casts may be used, but are rarely needed.  Use a type cast to force a number to a String or int to Integer, etc. " +
"\n\nArrays are denoted using curly braces, and may be nested.  Examples of arrays:\n" +
"   {1,2,3}\n" +
"   {hello,there}\n" +
"   {hello,1,there,2}\n" +
"   {{1,2},{3,4}}\n" +
"\nIf a type-cast is applied to an array, then all elements of that array must be compatible with it.  The following type-cast " +
"forces the value to be converted to an array of String (which would match 'String []' in an operation):\n" +
"   (String){1,2,3}\n" +
"\nStrings may be quoted with the double-quote character \".  This is not required, but can be useful to force a value's type " +
"to be a String.\n";

	private final static String	CREATE_SYNOPSIS	= "create: create and register an MBean";
	private final static String	CREATE_SYNTAX	=
"create --class=<classame> [--args=<args>] <name>";
	private final static String	CREATE_TEXT		=
"TBD";

	private final static String	COUNT_SYNOPSIS	= "count: count the number of registered MBeans";
	private final static String	COUNT_SYNTAX	= "count";
	private final static String	COUNT_TEXT		= "TBD";

	private final static String	DOMAINS_SYNOPSIS	= "domains: display the available domains";
	private final static String	DOMAINS_SYNTAX		= "domains";
	private final static String	DOMAINS_TEXT		= "TBD";

	private final static String	SOURCE_SYNOPSIS	= "source: read commands file a file";
	private final static String	SOURCE_SYNTAX	= "source <filename>";
	private final static String	SOURCE_TEXT		= "TBD";



	private final static String	DELETE_SYNOPSIS	= "delete: unregister an MBean";
	private final static String	DELETE_SYNTAX	=
"delete <fully-qualified-name>";
	private final static String	DELETE_TEXT		=
"TBD";


	private final static String	LISTEN_SYNOPSIS	= "listen: listen for notifications";
	private final static String	LISTEN_SYNTAX	=
"listen [ --stop | --pause ] [--file=filename]  target [[target]+]";
	private final static String	LISTEN_TEXT		=
"Listens for notifications emitted from the specified targets. " + 
"If neither stop nor pause is specified, then listening starts.  Output is emitted to the console unless a file is specified.";
	
	
	/*
	// generic invocations
	private final static String	LIST_SYNOPSIS	= "list: invokes the list() operation on the specified target(s)";
	private final static String	LIST_SYNTAX		= "list [target]+";
	private final static String	LIST_TEXT		=
"This command is equivalent to 'list: <targets>', but has some additional advantages.  First, it may supply superior output " +
"formatting.  Second, it may be invoked without targets.  This mplicitly invokes list() on all MBeans.";
	
*/

	// built-ins
	private final static String	TARGET_SYNOPSIS	= "target: displays or sets the target MBean(s)";
	private final static String	TARGET_SYNTAX	= "target [[target]+]";
	private final static String	TARGET_TEXT		=
"With no arguments, displays the current target(s).  Otherwise, the targets are taken as specified.";

	private final static String	SETENV_SYNOPSIS	= "setenv: sets a jmxadmin variable";
	private final static String	SETENV_SYNTAX	= "setenv [name=value | name]";
	private final static String	SETENV_TEXT		=
"setenv -- displays all environment variables\n" +
"setenv name=value -- sets variable 'name' to 'value'\n" +
"setenv name -- removes variable 'name'\n" +
"\nNote: variables persist across invocations of jmxadmin.  However, they are neither imported " +
"nor exported to/from the shell.\n" +
"";

	private final static String	CONFIGURE_SYNOPSIS	= "configure: configure jmxadmin";
	private final static String	CONFIGURE_SYNTAX	=	"configure | show-config | add-provider <classname> | " +
														"remove-provider <classname> | " +
														"add-cmd <classname> | remove-cmd <classname>";
	private final static String	CONFIGURE_TEXT		=
"The configure command can be invoked with various names, each of which functions as its own command.\n\n" +
"The possible invocation names are:\n" +
"configure: display this help\n" +
"add-provider -- add the JSR 160 provider with the specified classname\n" +
"remove-provider -- remove the JSR 160 provider with the specified classname\n" +
"add-cmd -- add a new command with the specified classname\n" +
"remove-cmd -- remove the command with the specified classname\n" +
"";

	
	// built-ins
	private final static String	CONNECT_SYNOPSIS	= "connect: connect to an MBeanServer";
	private final static String	CONNECT_SYNTAX		=
"(1) connect [--host h] --port p [--protocol prot] [--user u] [--password-file f] [--options key=value[,key=value]*] [name]\n" +
"(2) connect <name>\n" +
"(3) connect\n" +
"(4) connect --list\n";
	private final static String	CONNECT_TEXT		=
"Connects to the specified host and port with optional username and password and protocol.\n" +
"Several variants of this command are available:\n" +
"(1) Makes a connection to the specified server and associates the name with it (if specified).\n" +
"(2) Same as (1), but uses <name> to lookup the connection parameters.\n" +
"(3) Makes a connection to the default server.\n" +
"(4) Lists the active connections.\n" + 
"\nNotes:\n" +
"If --host is not specified, then localhost is used.\n" +
"If --protocol is not specified, the jmxmp is used.  \n" +
"If user and password-file are not specified, no user and password are used." +
"Additional options as name/value pairs may be specified and will be passed to the JMX connector " +
"as additional configuration data.\n" + 
"\nThe file specified by --password-file should be of the following format:\n" +
"user1=password1\n" +
"user2=password2\n" +
"...\n" +
"In this case, the --user option is required, and will be used to lookup the password in the file." +
"";

	
	// alias commands
	private final static String	CREATE_ALIAS_SYNOPSIS	= "create-alias: creates a persistent alias for an MBean name or pattern";
	private final static String	CREATE_ALIAS_SYNTAX		= "create-alias [--replace] [name=value]+";
	private final static String	CREATE_ALIAS_TEXT		=
"Creates an alias for the specified value.  No interpretation is given to the value supplied when creating an alias.  However, " +
"an alias must ultimately resolve to an ObjectName or ObjectName pattern.  Aliases may contain other aliases in any combination " +
"with ObjectNames.  The only restriction is that the space character is reserved as a delimiter. Therefore, ObjectNames " +
"containing spaces may not be aliased.\n\n" +
"Once created, an alias may be used anywhere an ObjectName may be used. Aliases are persisted in the file system local to where " +
"the CLISupportMBean is running.\n\n" +
"create-alias Examples: \n" +
"   create-alias test=:type=test,name=TestStandard\n" +
"   create-alias system=system:*\n" +
"   create-alias all-test=*:type=test\n" +
"";
	
	private final static String	DELETE_ALIAS_SYNOPSIS	= "delete-alias: deletes a persistent alias";
	private final static String	DELETE_ALIAS_SYNTAX		= "delete-alias [name]+";
	private final static String	DELETE_ALIAS_TEXT		= "Deletes an existing alias or aliases.";
	
	private final static String	RESOLVE_ALIAS_SYNOPSIS	= "resolve-alias: display the value of an alias";
	private final static String	RESOLVE_ALIAS_SYNTAX	= "resolve-alias [name]+";
	private final static String	RESOLVE_ALIAS_TEXT		=
"The alias value is displayed. A recursive option is planned.";
	
	private final static String	LIST_ALIASES_SYNOPSIS	= "list-aliases: display aliases";
	private final static String	LIST_ALIASES_SYNTAX		= "list-aliases";
	private final static String	LIST_ALIASES_TEXT		= "Aliases are listed, along with their values.";
	
	
	// other
	private final static String	HELP_SYNOPSIS	= "help: displays help";
	private final static String	HELP_SYNTAX		= "help [cmd]*";
	private final static String	HELP_TEXT		=
"To see all commands, type 'help'.  To see help for a particular command, " +
"type 'help cmd'.";
	
}


/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/GenerateMBeansCmd.java,v 1.3 2004/01/31 04:44:02 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/01/31 04:44:02 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import javax.management.MBeanInfo;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;

import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;

import org.glassfish.admin.amx.util.jmx.MBeanInterfaceGenerator;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;





/**
	Validates the correctness of MBeans.
 */
public class GenerateMBeansCmd extends JMXCmd
{
		public
	GenerateMBeansCmd( final CmdEnv env )
	{
		super( env );
	}
	
	

	static final class GenerateMBeansCmdHelp extends CmdHelpImpl
	{
		private final static String	DELIM	= " ";
		
		private final static String	SYNOPSIS	= "create java source files for MBean interfaces";
			
		private final static String	TEXT		=
		"For the specified targets, generates .java files containing the interface(s) " +
		"corresponding to the specified target(s).  These interfaces may then be used " +
		
		"to create proxies on the original MBeans.";

		public	GenerateMBeansCmdHelp()	{ super( getCmdInfos() ); }
		
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( TEXT ); }
	}
	
		public CmdHelp
	getHelp()
	{
		return( new GenerateMBeansCmdHelp() );
	}
	
	private final static OptionInfo VERBOSE_OPTION		= createVerboseOption();
	private final static OptionInfo NO_COMMENTS_OPTION	= new OptionInfoImpl( "no-comments", "n" );
	private final static OptionInfo SAVE_OPTION			= new OptionInfoImpl( "save", "s" );
	
	private static final OptionInfo[]	OPTIONS_INFO_ARRAY	=
	{
		VERBOSE_OPTION,
		NO_COMMENTS_OPTION,
		SAVE_OPTION,
	};
	
	final static String	NAME		= "generate-mbeans";


	private final static CmdInfo	CMD_INFO	=
		new CmdInfoImpl( NAME, new OptionsInfoImpl( OPTIONS_INFO_ARRAY ), TARGETS_OPERAND_INFO );
		
		
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( CMD_INFO ) );
	}
	
	private static final class MyBeanGenerator	extends MBeanInterfaceGenerator
	{
		private final ObjectName[]	mObjectNames;
		
			public
		MyBeanGenerator( ObjectName[] objectNames )
		{
			super();
			mObjectNames	= objectNames;
		}

			public String
		getHeaderComment( final MBeanInfo info )
		{
			String	header	= "Generated: " + new java.util.Date().toString() ;
			
			header	= header + "\n" + "Generated from:\n" +
				ArrayStringifier.stringify( mObjectNames, "\n" ) + "\n";
				
			return( makeJavadocComment( header ) );
		}
		

			public String
		getClassname( final MBeanInfo info )
		{
			// mangle ObjectName
			final char[]		chars	= ObjectName.quote( mObjectNames[ 0 ].toString() ).toCharArray();
			final StringBuffer	buf	= new StringBuffer();
			
			for( int i = 0; i < chars.length; ++i )
			{
				final char	c	= chars[ i ];
				
				if ( c == '.' || c == '=')
				{
					buf.append( "_" );
				}
				else if ( c == ':' || c == ',' )
				{
					buf.append( "__" );
				}
				else if ( c == '-' )
				{
					++i;	// skip it
					if ( i < chars.length &&
							chars[ i ] >= 'a' &&
							chars[ i ] <= 'z' )
					{
						buf.append( ("" + chars[ i ] ).toUpperCase() );
					}
					else
					{
						--i;
						buf.append( "_" );
					}
				}
				else if ( ! Character.isJavaIdentifierPart( c ) )
				{
					chars[ i ]	= '_';
				}
				else
				{
					buf.append( c );
				}
			}
			
			return( buf.toString() + "MBean" );
		}
	};
	
	

	private final static class GeneratedClass
	{
		public final String	mClassname;
		public final String	mPackageName;
		public final String	mClassSource;
		
			public
		GeneratedClass( String packageName, String classname, String classSource )
		{
			mPackageName	= packageName;
			mClassname		= classname;
			mClassSource	= classSource;
		}
	}
	
	/**
		Generate MBeans for the specified ObjectNames.  If an MBean has an interface identical to another,
		only a single interface is emitted.
		
		@param objectNames	the MBeans for which interfaces should be generated
		@param emitComments	emit javadoc comments if true, otherwise emit no javadoc comments
	 */
		GeneratedClass[]
	generate( ObjectName[]	objectNames, boolean emitComments, boolean display )
		throws IOException, ReflectionException, IntrospectionException, InstanceNotFoundException
	{
		final MBeanServerConnection		conn	= getConnection();
		final String[]					interfaces	= new String[ objectNames.length ];
		final Map<ObjectName,MBeanInfo> infosMap	= new HashMap<ObjectName,MBeanInfo>();
		final Map<String,Set<ObjectName>> intfToObjectNamesMap = new HashMap<String,Set<ObjectName>>();
		
		// Map the list of MBeanInfos into Sets of ObjectNames with the same interface
		for( int i = 0; i < objectNames.length; ++i )
		{
			final MBeanInfo info	= conn.getMBeanInfo( objectNames[ i ] );
			
			// map the ObjectName to its info
			infosMap.put( objectNames[ i ], info );
			
			// make a giant String representing the interface of the MBean
			final String	infoString	=
								ArrayStringifier.stringify( info.getAttributes(), "," ) +
								ArrayStringifier.stringify( info.getOperations(), "," );
			
			Set<ObjectName>	names	= intfToObjectNamesMap.get( infoString );
			if ( names == null )
			{
				names	= new HashSet<ObjectName>();
				names.add( objectNames[ i ] );
			}
			else
			{
				names.add( objectNames[ i ] );
			}
			intfToObjectNamesMap.put( infoString, names );
		}
		
		final List<GeneratedClass>	generatedClasses	= new ArrayList<GeneratedClass>();
		final Iterator<String>	iter	= intfToObjectNamesMap.keySet().iterator();
		while ( iter.hasNext() )
		{
			final String	key				= iter.next();
			final Set<ObjectName>		objectNameSet	= intfToObjectNamesMap.get( key );
			
			final ObjectName[]	sharedNames	=
				(ObjectName[])objectNameSet.toArray( new ObjectName[ objectNameSet.size() ] );

			final MBeanInterfaceGenerator	generator	= new MyBeanGenerator( sharedNames );
			final MBeanInfo	info	= (MBeanInfo)infosMap.get( sharedNames[ 0 ] );
			
			if ( info.getAttributes().length != 0 || info.getOperations().length != 0 )
			{
				final String classSource	= generator.generate( info, emitComments );
				
				final GeneratedClass	gen = new GeneratedClass( generator.getPackageName( info ),
					generator.getClassname( info ), classSource );
				
				generatedClasses.add( gen );
				
				if ( display )
				{
					println( classSource );
					println( "" );
					println( "" );
				}
			}
		}
		
		return( (GeneratedClass[])generatedClasses.toArray( new GeneratedClass[ generatedClasses.size() ] ) );
	}
	
		private void
	save( GeneratedClass[] gen )
		throws FileNotFoundException, IOException
	{
		File dir	= new File( "./mbeans" );
		
		dir.mkdir();
		
		for( int i = 0; i < gen.length; ++i )
		{
			final File	saveFile	= new File( dir.toString() + "/" + gen[ i ].mClassname + ".java" );
			saveFile.createNewFile();
			
			final FileWriter	out	= new FileWriter( saveFile );
			try
			{
				out.write( gen[ i ].mClassSource );
			}
			finally
			{
				out.close();
			}
		}
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		String [] targets	= getTargets();
		
		if ( targets == null || targets.length == 0 )
		{
			targets	= new String[] { "*" };
		}
		
		final boolean noComments	= getBoolean( NO_COMMENTS_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
		final boolean save			= getBoolean( SAVE_OPTION.getShortName(), Boolean.FALSE ).booleanValue();

		establishProxy();
		
		final ObjectName[]	objectNames	= resolveTargets( getProxy(),  targets );
		if ( objectNames.length != 0 )
		{
			println( "Generating .java files for " + objectNames.length + " mbeans." );
			final boolean	display	= getVerbose() || ! save;
			final GeneratedClass[]	gen	= generate( objectNames, ! noComments, display );
			
			if ( save )
			{
				save( gen );
			}
		}
		else
		{
			println( "No targets specified." );
		}
	}
}







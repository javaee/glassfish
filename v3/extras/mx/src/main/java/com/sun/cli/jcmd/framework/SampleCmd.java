/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/SampleCmd.java,v 1.8 2003/12/19 01:48:07 llc Exp $
 * $Revision: 1.8 $
 * $Date: 2003/12/19 01:48:07 $
 */
 
package com.sun.cli.jcmd.framework;

import com.sun.cli.jcmd.framework.CmdBase;
import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import com.sun.cli.jcmd.util.cmd.DisallowedOptionDependency;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;


/**
	An example of how to write a Cmd to plug into the framework.
 */
public class SampleCmd extends CmdBase
{
	/**
		The command is passed its environment upon instantiation.   The environment contains
		many useful items, most of which are accessible indirectly through methods inherited
		from CmdBase.
	 */
		public
	SampleCmd( final CmdEnv env )
	{
		super( env );
	}
	

	/**
		Each command must provide a CmdHelp.  It can be created in any desired way--hardcoded
		here or read from properties, xml, etc.
	 */
	static final class SampleCmdHelp extends CmdHelpImpl
	{
			public
		SampleCmdHelp()	{ super( getCmdInfos() ); }
		
		static final String	SYNOPSIS		= "demonstrate a sample command";
		static final String	SOURCE_TEXT		= "Demonstrates a sample command.";

		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( SOURCE_TEXT ); }
	}
	
		public CmdHelp
	getHelp()
	{
		return( new SampleCmdHelp() );
	}
	
	static private final String		NAME				= "sample-cmd";
	
	
	private final static OptionInfo 	BOOLEAN_OPTION				= new OptionInfoImpl( "boolean", "b" );
	private final static OptionInfo 	INTEGER_OPTION				= new OptionInfoImpl( "integer", "i", "any-integer");
	private final static OptionInfo 	STRING_OPTION				= new OptionInfoImpl( "string", "s", "any-string");
	private final static OptionInfo 	ANTISOCIAL_OPTION			= new OptionInfoImpl( "antisocial", "a" );
	
	/**
		The best way to define options is to create OptionInfo instances.  This allows the ability
		to add dependencies, decouple the code from option names, etc.
	 */
	private static final OptionInfo[]	OPTIONS_INFO_ARRAY	=
	{
		BOOLEAN_OPTION,
		INTEGER_OPTION,
		STRING_OPTION,
		ANTISOCIAL_OPTION,
	};
	
	static
	{
		// disallow use of the other options when this one is used.
		ANTISOCIAL_OPTION.addDependency(
			new DisallowedOptionDependency( BOOLEAN_OPTION, INTEGER_OPTION, STRING_OPTION ) );
	}
	
	/**
		Create it once--it never changes.
	 */
	private static final OptionsInfo	OPTIONS_INFO = new OptionsInfoImpl( OPTIONS_INFO_ARRAY );
	
	
	private final static CmdInfo	SAMPLE_INFO	=
		new CmdInfoImpl( NAME, OPTIONS_INFO, new OperandsInfoImpl( "[value[ value]*]", 0 ) );
		
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( SAMPLE_INFO ) );
	}
	
	
		
	/**
		Execute the command.  To print out normal status, use println().  To print out an error
		condition, either throw an exception, or use printError() and then throw an Exception. A command
		that fails should generally always throw an exception.  Use CmdException or one of its
		subclasses when it makes sense.
	 */
		protected void
	executeInternal()
		throws Exception
	{
		final String [] operands	= getOperands();
		
		// any valid name for the option may be used to get it
		final Boolean	booleanOption		= getBoolean( BOOLEAN_OPTION.getShortName(), null );
		final Integer	integerOption		= getInteger( INTEGER_OPTION.getShortName(), null );
		final String	stringOption		= getString( STRING_OPTION.getShortName(), null );
		final String	antisocialOption	= getString( ANTISOCIAL_OPTION.getShortName(), null );
	
		println( "SampleCmd was invoked with the name: " + getSubCmdNameAsInvoked() );
		
		for( int i = 0; i < operands.length; ++i )
		{
			println( "operands[ " + i + " ] = " + operands[ i ] );
		}
		
		if ( booleanOption != null )
		{
			println( "Boolean option " + BOOLEAN_OPTION.getLongName() +
				" was specified with value " + booleanOption );
		}
		
		if ( integerOption != null )
		{
			println( "Integer option " + INTEGER_OPTION.getLongName() +
				" was specified with value " + integerOption );
		}
		
		if ( stringOption != null )
		{
			println( "String option " + STRING_OPTION.getLongName() +
				" was specified with value " + stringOption );
		}
		
		if ( antisocialOption != null )
		{
			println( "Antisocial option " + ANTISOCIAL_OPTION.getLongName() + " was specified" );
		}
	}
}







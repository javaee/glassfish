/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdHelpImpl.java,v 1.5 2003/12/19 18:38:59 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2003/12/19 18:38:59 $
 */
 
package com.sun.cli.jcmd.framework;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

import com.sun.cli.jcmd.util.cmd.CmdInfos;

/**
	A useful base class for CmdHelp.
 */
public abstract class CmdHelpImpl implements CmdHelp
{
	private final static String		SYNTAX_DELIM	= "\n\n";
	private final static String		CMD_DELIM	= "";
	private final CmdInfos			mInfos;
	
		protected
	CmdHelpImpl( CmdInfos	infos )
	{
		mInfos	= infos;
	}
		
		public String
	getName()
	{
		return( mInfos.getNames()[ 0 ] );
	}
	
		public String[]
	getNames()
	{
		return( mInfos.getNames() );
	}
	
		public String
	getSyntax(  )
	{
		return( mInfos.toString() );
	}
	
		protected String
	formSynopsis( String explanation )
	{
		/*
		String	synopsis	= null;
		
		final String	primaryName = getName();
		final String[]	names 		= getNames();
		
		if ( names == null || names.length <= 1 )
		{
			synopsis	= primaryName + ": " + explanation;
		}
		else
		{
			synopsis	= primaryName + " (" +
							ArrayStringifier.stringify( names, "/") + "): " + explanation;
		}
		return( synopsis );
		*/
		return( explanation );
	}
	
		public String
	toString()
	{
		return( CMD_DELIM + getSynopsis() + CMD_DELIM +
		SYNTAX_DELIM + getSyntax() + SYNTAX_DELIM + getText() );
	}

}






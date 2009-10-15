/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/DisallowedCmdDependency.java,v 1.4 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:18 $
 */
package com.sun.cli.jcmd.util.cmd;

import java.util.Set;
import org.glassfish.admin.amx.util.ArrayConversion;



/**
	A dependency denoting a disallowed command.
 */
public final class DisallowedCmdDependency extends OptionDependency
{
	private final Set	mDisallowedNames;
	
	public Set	getDisallowedNames()	{ return mDisallowedNames; }
	
	
		public boolean
	isDisallowed( String cmdName )
	{
		return( mDisallowedNames.contains( cmdName ) );
	}
	
	/**
		Create a new instance specifying that the command names may not
		be used.
	 */
		public
	DisallowedCmdDependency( final String[] names )
	{
		super( (OptionInfo)null );
		
		mDisallowedNames	= ArrayConversion.arrayToSet( names );
	}
}


/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/OptionInfo.java,v 1.6 2005/11/08 22:39:19 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2005/11/08 22:39:19 $
 */
package com.sun.cli.jcmd.util.cmd;

import java.util.Set;

		
/**
	Internal class used to keep information about the options.
 */
public interface OptionInfo
{
	/**
		Prefix for a short option.
	 */
	public final static String	SHORT_OPTION_PREFIX	= "-";
	
	/**
		Prefix for a long option.
	 */
	public final static String	LONG_OPTION_PREFIX		= "--";
	
	/**
		Prefix for either a short or long option
	 */
	public final static String	OPTION_PREFIX	= SHORT_OPTION_PREFIX;
	
	
	public String	getLongName();
	public String	getShortName();
	public int		getNumValues();
	public String[]	getValueNames();
	public boolean	isBoolean();
	public boolean	isRequired();
	
	public boolean	equals(	Object rhs );
	
	/**
		Return the synonyms for this option, including the long and short name and any 
		additional synonyms.
	 */
	public Set<String>	getSynonyms();
	/**
	 */
	public void 			addDependency( OptionDependency 	dependency );
	
	/**
	 */
	public Set<OptionDependency> getDependencies(  );
	
	/**
		Return true if the name matches either the short or long names or a synonym.
		The name must have the appropriate prefix already in place.
		
		All option names are case-sensitive.
		
		@param name		an option name beginning with "-" or "--"
		@return			true if a match, false otherwise
	 */
	public boolean	matches( String name );
	
	/**
		Convert to an equivalent String form suitable for re-parsing
	 */
	public String	toString();
	
	/**
		Convert to an equivalent String form suitable for display
	 */
	public String	toDisplayString();
}



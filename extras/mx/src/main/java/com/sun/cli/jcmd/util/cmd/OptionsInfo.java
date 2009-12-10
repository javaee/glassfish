/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/OptionsInfo.java,v 1.6 2005/11/08 22:39:19 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2005/11/08 22:39:19 $
 */
 
package com.sun.cli.jcmd.util.cmd;

import java.util.List;

/**
	Interface used to access information about the available legal options while
	parsing the input.
	<p>
	The term 'token' as used here is a String as parsed from the command line. Example:
	<p>
	jmxcmd connect --host localhost --port=9998 llcs
	<p>
	parses into the tokens:
	<p>
	connect		(subcommand, not an option)
	--host
	localhost
	--port=9998
	llcs		(operand)
 */
public interface OptionsInfo
{
	/**
		Convert a parsed token to an option name.  The leading "-" or "--"
		should be preserved.  The implementor must account for the case in which
		an option has a "=value" part by stripping it and returning the preeceding
		name.
		<p>
		Examples:
		<p>
		--count=3	=> --count
		-h			=> -h
		--verbose	=> verbose
		
		@param	token	the token as parsed
		@return		the name found with the token.
	 */
	public String		tokenToOptionName( String token );
	
	/**
		Extract the data from a parsed token.  If there is no data, return null.
		<p>
		Because the user may use either the "--count=3" or "--count 3" forms, there
		may or may not be data in the token.
		<p>
		Examples:
		<p>
		--count=3	=> "3"
		--h			=> null
		--verbose	=> null
		
		@param	token	the token as parsed
		@return		the data found with the token (null if none)
	 */
	public String		tokenToOptionData( String token );
	
	/**
		Decide whether the token contains a legal option.  If the option is
		apparently a grouped boolean (eg -xyz instead of -x -y -z), this routine
		should return false.
		<p>
		This call must return false if the option does not start with "-".
		
		@param	token	the token as parsed
		@return		true if this token contains a legal option, false otherwise.
	 */
	public boolean		isLegalOption( String token );
	
	/**
		Decide whether the token is a Boolean option.
		
		@param	token	the token as parsed
		@return		true if this token is a Boolean option
	 */
	public boolean		isBoolean( String token );
	
	/**
		Decide how many values the option associated with this token has.  Generally
		the number of value should be fixed (usually 0 or 1), but in theory it could vary.
		
		Note that the long option for "--stuff=1,2,3" can mean either that "stuff" has a single
		value equal to "1,2,3" or 3 values {1,2,3}.  How it is parsed depends on the value
		returned by this method; a value of 1 means don't interpret; any other non-zero
		value means the "," will be interpreted as a value delimiter.
		
		Boolean options *must* return 0.
		
		@param	token	the token as parsed
		@return		the number of values for the option described by the token.
	 */
	public int			getNumValues( String token );
	
	/**
		Return info about all supported options.
		
		@return		list of options, in the order they were originally added
	 */
	public List<OptionInfo> 		getOptionInfos();
	
	
	
	/**
		Return info about a particular option
		
		@return		info for the option, or null if not found
	 */
	public OptionInfo	getOptionInfo( String name );
};

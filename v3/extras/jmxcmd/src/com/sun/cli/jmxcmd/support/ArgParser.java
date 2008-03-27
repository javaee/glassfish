/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/ArgParser.java,v 1.1 2003/11/21 21:23:48 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:48 $
 */
 

 
package com.sun.cli.jmxcmd.support;

import java.lang.reflect.Array;
import java.util.ArrayList;

public interface ArgParser
{
	public final static char		DEFAULT_ESCAPE_CHAR		= '\\';
	public final static char		DEFAULT_ARG_DELIM		= ',';
	public final static char		DEFAULT_ARRAY_LEFT		= '{';
	public final static char		DEFAULT_ARRAY_RIGHT		= '}';
	
	public final static String		DEFAULT_ESCAPABLE_CHARS_WITHIN_LITERAL_STRING	=
										DEFAULT_ESCAPE_CHAR + "n" + "r" + "t" + "\"";
	
	// note: n, r are for newline, carriage return
	public final static String		DEFAULT_ESCAPABLE_CHARS	=
										DEFAULT_ESCAPABLE_CHARS_WITHIN_LITERAL_STRING +
										",(){}" + "\"" ;
	
	
	/**
		Parse simple names eg a,b,c.
		
		@param input	the string to parse
		@return		array of String
	 */
	public String []	ParseNames( String input )
							throws ArgParserException;
							
		
	/**
		Parse the input and return ParseResult [], one for
		each argument.
		
		@param input		the string to parse
		@param namedArgs	true if arguments have names

		@return		array of ParseResult
	 */
	public ParseResult []	Parse( String input, boolean namedArgs  )
								throws ArgParserException;
};

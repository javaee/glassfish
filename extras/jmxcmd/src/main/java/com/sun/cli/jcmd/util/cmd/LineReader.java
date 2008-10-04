/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/LineReader.java,v 1.3 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:18 $
 */
 
package com.sun.cli.jcmd.util.cmd;


/**
	Reads a line, outputting an optional prompt first.
 */
public interface LineReader
{
	/**
		Reads a line, outputting an optional prompt first.  If the prompt is null
		then no prompt is printed.
	 */
	public String	readLine( String prompt ) throws java.io.IOException;
}


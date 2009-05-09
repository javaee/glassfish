/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/IllegalOptionException.java,v 1.4 2005/11/15 20:21:43 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/15 20:21:43 $
 */
 
package com.sun.cli.jcmd.util.cmd;



/**
	Thrown by several of the methods of this class if there is a problem
	accessing an option as requested.
 */
public final class IllegalOptionException extends Exception
{
    static final long serialVersionUID = -4231642164843669915L;
    
		public
	IllegalOptionException( String msg )
	{
		super( msg );
	}
}



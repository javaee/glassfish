/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/WrongNumberOfOperandsException.java,v 1.3 2005/11/15 20:21:42 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/15 20:21:42 $
 */
package com.sun.cli.jcmd.framework;
 

public class WrongNumberOfOperandsException extends CmdException
{
    static final long serialVersionUID = -1964494745766466406L;
    
	WrongNumberOfOperandsException( String msg )
	{
		super( msg, WRONG_NUMBER_OF_OPERANDS_ERROR );
	}
}

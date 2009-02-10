/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/OptionInfoValidator.java,v 1.3 2005/11/08 22:39:19 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:19 $
 */
package com.sun.cli.jcmd.util.cmd;


public interface OptionInfoValidator
{
	public void	validateOption( OptionInfo optionInfo ) throws IllegalOptionException;
};





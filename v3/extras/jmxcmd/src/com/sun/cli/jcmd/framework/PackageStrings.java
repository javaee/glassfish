/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/PackageStrings.java,v 1.2 2004/03/01 20:21:36 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2004/03/01 20:21:36 $
 */
package com.sun.cli.jcmd.framework;

import com.sun.cli.jcmd.util.misc.StringSource;
import com.sun.cli.jcmd.util.misc.StringStringSource;

/**
	 Strings for the package com.sun.cli.jcmd.framework.
	 <p>
	 These strings are the English strings and are compiled in. The intent is that
	 they will be moved into a properties file in the future.
 */
public final class PackageStrings extends StringStringSource
{
	static final String CmdBase_WarningDuplicateOption	= "CmdBase.WarningDuplicateOption";
	static final String CmdBase_OptionDisallowed		= "CmdBase.OptionDisallowed";
	static final String CmdBase_OperandsRequired		= "CmdBase.OperandsRequired";
	static final String CmdBase_NoOperands				= "CmdBase.NoOperands";
	static final String CmdBase_NoMoreThanOperands		= "CmdBase.NoMoreThanOperands";
	static final String CmdBase_CmdImproperlyImplemented	= "CmdBase.CmdImproperlyImplemented";
	static final String CmdBase_WrongNumOperands		= "CmdBase.WrongNumOpernads";
	
	private static final String STRINGS	= 
	CmdBase_WarningDuplicateOption + "=Warning: ignored duplicate option: \"{0}={1}\".\n" +
	CmdBase_OptionDisallowed + "=Command \"{0}\" does not accept option \"{1}\".\n" +
	CmdBase_OperandsRequired + "=Command \"{0}\" requires {1} operand(s).\n" +
	CmdBase_NoOperands + "=Command \"{0}\" takes no operands.\n" +
	CmdBase_WrongNumOperands + "=Illegal number of operands: {0} supplied, {1} required ({3}).\n" +
	CmdBase_NoMoreThanOperands + "=Command \"{0}\" takes no more than {1} operands.\n" +
	CmdBase_CmdImproperlyImplemented + "=Command \"{0}\" not properly implemented, threw exception of class {1}.\n" +
	"";
	
		public
	PackageStrings( StringSource delegate )
	{
		super( STRINGS, delegate );
	}
};




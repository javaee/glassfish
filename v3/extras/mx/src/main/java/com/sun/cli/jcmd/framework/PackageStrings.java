/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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




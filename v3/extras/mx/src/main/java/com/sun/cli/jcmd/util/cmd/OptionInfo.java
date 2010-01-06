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



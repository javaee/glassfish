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

package com.sun.cli.jcmd.util.cmd;

import java.util.Set;
import java.util.Collections;

		
/**
	Internal class used to keep information about the options.
 */
public final class ImmutableOptionInfo implements OptionInfo
{
	final OptionInfo	mInfo;
	
	public String	getLongName()		{	return( mInfo.getLongName() ); }
	public String	getShortName()		{	return( mInfo.getShortName() ); }
	public int		getNumValues()		{	return( mInfo.getNumValues() ); }
	public String[]	getValueNames()		{	return( mInfo.getValueNames() ); }
	public boolean	isBoolean()			{	return( mInfo.isBoolean() ); }
	public boolean	isRequired()		{	return( mInfo.isRequired() ); }
	public boolean	matches( String name )	{	return( mInfo.matches( name ) ); }
	public String	toString()				{	return( mInfo.toString() ); }
	public String	toDisplayString()		{	return( mInfo.toDisplayString() ); }
	public boolean	equals(	Object rhs )	{	return( mInfo.equals( rhs ) ); }
	
	/**
		Create a new immutable wrapper around the specified OptionInfo.
		
		@param info		the wrapped info
	 */
		public
	ImmutableOptionInfo( OptionInfo info )
	{
		mInfo	= info;
	}
	
	public java.util.Set<String>	getSynonyms()
	{
		return( Collections.unmodifiableSet( mInfo.getSynonyms() ) );
	}
	
		public void
	addDependency( OptionDependency 	dependency )
	{
		throw new IllegalArgumentException( "Attempt to modify immutable OptionInfo" );
	}
	
		public Set<OptionDependency>
	getDependencies(  )
	{
		return( Collections.unmodifiableSet( mInfo.getDependencies() ) );
	}
}



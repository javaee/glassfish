/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;

/**
 */
public class CmdInfoImpl implements CmdInfo
{
	private final String		mName;
	private final OptionsInfo	mOptionsInfo;
	private final OperandsInfo	mOperandsInfo;
	
	private final static String	SPACE	= " ";
	
	
	public String		getName()				{ return( mName ); }
	public OptionsInfo	getOptionsInfo()		{ return( mOptionsInfo ); }
	public OperandsInfo	getOperandsInfo()		{ return( mOperandsInfo ); }
	
		public
	CmdInfoImpl( String name )
	{
		this( name, OptionsInfoImpl.NONE, OperandsInfoImpl.NONE );
	}
	
		public
	CmdInfoImpl( String name, OperandsInfo operandsInfo )
	{
		this( name, OptionsInfoImpl.NONE, operandsInfo );
	}
	
		public
	CmdInfoImpl( String name, OptionsInfo optionsInfo, OperandsInfo operandsInfo )
	{
		mName					= name;
		mOptionsInfo			= optionsInfo == null ? OptionsInfoImpl.NONE : optionsInfo;
		mOperandsInfo			= operandsInfo == null ? OperandsInfoImpl.NONE : operandsInfo;
	}
	
		public String
	toString()
	{
		return( getSyntax() );
	}
	
		public String
	getSyntax()
	{
		final StringBuffer	buf	= new StringBuffer();
		
		buf.append( getName() );
		
		if ( mOptionsInfo != null && mOptionsInfo.getOptionInfos().size() != 0 )
		{
			buf.append( SPACE );
			buf.append( mOptionsInfo.toString() );
		}
		
		if ( mOperandsInfo != null )
		{
			final String	s	= mOperandsInfo.toString();
			if ( s.length() != 0 )
			{
				buf.append( SPACE );
				buf.append(  mOperandsInfo.toString() );
			}
		}
		
		return( buf.toString() );
	}
}






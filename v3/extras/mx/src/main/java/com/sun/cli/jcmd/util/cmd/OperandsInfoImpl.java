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

/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/OperandsInfoImpl.java,v 1.5 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2005/11/08 22:39:18 $
 */
 
package com.sun.cli.jcmd.util.cmd;

import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

/**
	Information about the operands a command supports
 */
public final class OperandsInfoImpl implements OperandsInfo
{
	final String	mDescription;
	final int		mMinOperands;
	final int		mMaxOperands;
	
	public final static int	NO_MAX	= OperandsInfo.NO_MAX;
	
	public static final OperandsInfoImpl	NONE	= new OperandsInfoImpl( null, 0, 0);
	
	
		public 
	OperandsInfoImpl()
	{
		this( null, 0, NO_MAX  );
	}
	
		public 
	OperandsInfoImpl( String description )
	{
		this( description, 0, NO_MAX );
	}
	
		public 
	OperandsInfoImpl( String description, int minOperands)
	{
		this( description, minOperands, NO_MAX );
	}
	
		private static String
	normalizeDescription( String d, int minOperands )
	{
		String	s	= d == null ? "" : d;
		
		if ( s.length() != 0 && s.indexOf( "<" ) < 0)
		{
			if (s.indexOf( "[" ) < 0 )
			{
				// operands should have <> around them, ensure this is the case
				String[]	values	= d.split( " " );
				
				for( int i = 0; i < values.length; ++i )
				{
					values[ i ]	= StringUtil.quote( values[ i ], '<' );
				}
				
				s	= ArrayStringifier.stringify( values, " " );
			}
			
			// if operands are optional, ensure [] surrounds them
			if ( minOperands == 0  && ! s.startsWith( "[" ) )
			{
				s	= StringUtil.quote( s, '[' );
			}
		}
		
		return( s );
	}

		public
	OperandsInfoImpl( String description, int minOperands, int maxOperands )
	{
		if ( minOperands < 0 || maxOperands < minOperands )
		{
			new Exception().printStackTrace();
			throw new IllegalArgumentException( "Illegal min/max operands: " +
				minOperands + ", " + maxOperands );
		}
		
		mMinOperands	= minOperands;
		mMaxOperands	= maxOperands;
		mDescription	= normalizeDescription( description, minOperands);
	}
	
	
		public int
	getMinOperands()
	{
		return( mMinOperands );
	}
	
		public int
	getMaxOperands()
	{
		return( mMaxOperands );
	}

		public String
	toString()
	{
		return( mDescription );
	}
}






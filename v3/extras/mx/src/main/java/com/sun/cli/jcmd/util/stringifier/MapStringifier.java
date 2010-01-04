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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/stringifier/MapStringifier.java,v 1.2 2005/11/08 22:39:27 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:39:27 $
 */
 
package org.glassfish.admin.amx.util.stringifier;

import java.util.Map;
import org.glassfish.admin.amx.util.MapUtil;

/**
	Stringifies an Iterator, using an optional element Stringifier
 */
 
public final class MapStringifier implements Stringifier
{
	private final String	mItemsDelim;

		public 
	MapStringifier(  )
	{
		this( "," );
	}
	
		public 
	MapStringifier( final String delim )
	{
		mItemsDelim		= delim;
	}
	
	
	/*
		Static variant when direct call will suffice.
	 */
		public static String
	stringify( final Map m, final String delim )
	{
		if ( m == null )
		{
			return( "null" );
		}

		final MapStringifier	stringifier	= new MapStringifier( delim );
		
		return( stringifier.stringify( m ) );
	}
	
	/*
		Static variant when direct call will suffice.
	 */
		public String
	stringify( Object o )
	{
		assert( o instanceof Map ) : "not a Map: " + o.getClass().getName();
		
		return( MapUtil.toString( (Map)o, mItemsDelim ) );
	}
	
	
	public final static MapStringifier DEFAULT = new MapStringifier( "," );
}


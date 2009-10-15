/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/StringifiedList.java,v 1.4 2005/11/08 22:39:24 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:24 $
 */
 
package com.sun.cli.jcmd.util.misc;

import java.util.List;
import java.util.ArrayList;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;


/**
	Maintain a list of Strings, ensuring that a String constructor may be
	used to repopulate the list and that toString() generates a String appropriate
	for such use.
	
	NOTE: this should be improved so that items may contain the delimiter
 */
public class StringifiedList
{
	final List<String>	mItems;
	final char		mDelim;
	
	public final static char	DEFAULT_DELIM	= ',';
	
	/**
		Create a new list with the delimiter DEFAULT_DELIM
	 */
		public
	StringifiedList( final String listString )
	{
		this( listString, DEFAULT_DELIM );
	}
	
	/**
		Create a new list with the specified delimiter 
	 */
		public
	StringifiedList( final String[] items, final char delim )
	{
		mDelim	= delim;
		mItems	= new ArrayList<String>();
		
		for( int i = 0; i < items.length; ++i )
		{
			append( items[ i ] );
		}
	}
	
	/**
		Create a new list with the specified delimiter, with contents taken from the 
		supplied String.
		
		@param listString	the string containing 0 or more items for the list
		@param delim		the delimiter between items
	 */
		public
	StringifiedList( final String listString, final char delim )
	{
		mDelim	= delim;
		
		mItems	= new ArrayList<String>();
		
		if ( listString != null )
		{
			final StringEscaper	escaper	= new StringEscaper( "" + mDelim );
		
			final String []	list	= listString.trim().split( "" + delim );
			
			// first listed should be first in priority, so add to end
			for ( int i = 0; i < list.length; ++i )
			{
				mItems.add( escaper.unescape( list[ i ] ) );
			}
		}
	}
	
		public String
	toString()
	{
		final StringEscaper	escaper	= new StringEscaper( "" + mDelim );
		final String[]		items	= toArray();
		
		for( int i = 0; i < items.length; ++i )
		{
			items[ i ]	= escaper.escape( items[ i ] );
		}

		return( ArrayStringifier.stringify( items, "" + mDelim ) );
	}
	
		public String []
	toArray()
	{
		return( (String [])mItems.toArray( new String[ mItems.size() ] ) );
	}
	
		public boolean
	exists( final String name )
	{
		return( mItems.contains( name ) );
	}
	
		public java.util.Iterator<String>
	iterator()
	{
		return( mItems.iterator() );
	}
	
	
		public void
	prepend( String item )
	{
		mItems.add( 0, item );
	}
	
		public void
	append( String item )
	{
		mItems.add( item );
	}
	
	
		public void
	remove( String item )
	{
		if ( exists( item ) )
		{
			mItems.remove( item );
		}
	}
}


/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.amx.j2ee.util;

import org.glassfish.admin.amx.j2ee.util.J2EEUtil;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;
import org.glassfish.admin.amx.util.stringifier.Stringifier;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.management.j2ee.statistics.Statistic;

/**
	Stringifier for javax.management.j2ee.Statistic 
 */
public class StatisticStringifier implements Stringifier
{
	public static final StatisticStringifier	DEFAULT	= new StatisticStringifier();
	
		public
	StatisticStringifier( )
	{
	}
	
	private final static String	DELIM	= ", ";
	private final static String	GET		= "get";
	
	
	private static final String[]	ORDERED_VALUES	=
	new String[]
	{
		"Name",
		"Description",
		"Unit",
		"StartTime",
		"LastSampleTime",
		"Count",
		"Low", "Current", "High",
		"LowerBound", "UpperBound",
		"Current",
	};
	
		private static String
	createNameValuePair( final String name, final Object value )
	{
		final String valueString	= value instanceof String ?
			StringUtil.quote( (String)value ) : SmartStringifier.toString( value );
			
		return name + "=" + valueString;
	}
	
	/**
		Stringify by accessing all public getXXX() methods that return a value
		and don't throw exceptions.
	 */
		public String
	stringify( Object o )
	{
		final Statistic		statistic	= (Statistic)o;
		
		final StringBuffer	buf	= new StringBuffer();
		
		buf.append( statistic.getName() + ": " );
		
		final SortedMap<String,Object> pairs =
		    new TreeMap<String,Object>( J2EEUtil.statisticToMap( statistic ) );
		
		// first emit the standard value names in a proscribed order
		for( int i = 0; i < ORDERED_VALUES.length; ++i )
		{
			final String	name	= ORDERED_VALUES[ i ];
			if ( pairs.containsKey( name ) )
			{
				final Object value	= pairs.get( name );
				buf.append( createNameValuePair( name, value ) );
				buf.append( DELIM );
				pairs.remove( name );
			}
		}
		
		
		final Iterator	iter	= pairs.keySet().iterator();
		while ( iter.hasNext() )
		{	
			final String	name	= (String)iter.next();
			final Object	value	= pairs.get( name );
			
			buf.append( createNameValuePair( name, value ) );
			buf.append( DELIM );
			
		}
		
		String	result	= buf.toString();
		if ( result.endsWith( DELIM ) )
		{
			result	= result.substring( 0, result.length() - DELIM.length() );
		}
		
		
		return( result );
	}
}




















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
package com.sun.cli.jmxcmd.util.jmx.stringifier;

import java.util.Set;
import java.util.List;
import java.util.Iterator;

import javax.management.openmbean.TabularData;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularType;

import com.sun.cli.jcmd.util.misc.StringUtil;
import com.sun.cli.jcmd.util.stringifier.Stringifier;
import com.sun.cli.jcmd.util.stringifier.SmartStringifier;


public class TabularDataStringifier implements Stringifier
{
	public static final TabularDataStringifier	DEFAULT	= new TabularDataStringifier();
	
		public
	TabularDataStringifier( )
	{
	}
	
		public String
	stringify( Object o )
	{
		final StringBuffer	buf	= new StringBuffer();
		buf.append( "Tabular data:\n" );
		
		final TabularData	data	= (TabularData)o;
		final TabularType	type	= data.getTabularType();
		
		final List		indexNames	= type.getIndexNames();
		final Set		rowKeys		= data.keySet();
		final Iterator	rowIter	= rowKeys.iterator();
		int				rowIndex	= 0;
		while ( rowIter.hasNext() )
		{
			final Object[]		key		= (Object[])rowIter.next();
			final CompositeData	item	= data.get( key );
			
			final String	s	= SmartStringifier.toString( item );
			
			// emit the row index followed by the row
			buf.append( "[" + rowIndex + "] " );
			buf.append( s + "\n" );
			
			++rowIndex;
		}
		
		return( buf.toString() );
	}
}




















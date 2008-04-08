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
import java.util.Iterator;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import com.sun.cli.jcmd.util.misc.StringUtil;
import com.sun.cli.jcmd.util.stringifier.Stringifier;
import com.sun.cli.jcmd.util.stringifier.SmartStringifier;


public class CompositeDataStringifier implements Stringifier
{
	public static final CompositeDataStringifier	DEFAULT	= new CompositeDataStringifier();
	
		public
	CompositeDataStringifier( )
	{
	}
	
		public String
	stringify( Object o )
	{
		final StringBuffer	buf	= new StringBuffer();
		buf.append( "Composite data:\n" );
		
		final CompositeData	data	= (CompositeData)o;
		final CompositeType	type	= data.getCompositeType();
		
		final Set		keySet	= type.keySet();
		final Iterator	iter	= keySet.iterator();
		while ( iter.hasNext() )
		{
			final String	key	= (String)iter.next();
			final Object	item	= data.get( key );
			
			final String	s	= SmartStringifier.toString( item );
			buf.append( key + "=" + s + "\n" );
		}
		
		return( buf.toString() );
	}
}




















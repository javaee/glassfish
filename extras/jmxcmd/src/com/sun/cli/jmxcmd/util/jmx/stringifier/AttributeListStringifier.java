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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/stringifier/AttributeListStringifier.java,v 1.3 2005/11/15 20:21:49 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/15 20:21:49 $
 */
 
package com.sun.cli.jmxcmd.util.jmx.stringifier;

import java.util.Iterator;

import javax.management.Attribute;
import javax.management.AttributeList;

import com.sun.cli.jcmd.util.stringifier.Stringifier;
import com.sun.cli.jcmd.util.stringifier.IteratorStringifier;

public final class AttributeListStringifier implements Stringifier
{
	final String	mDelim;
	
	public final static AttributeListStringifier	DEFAULT	= new AttributeListStringifier();
	
		public
	AttributeListStringifier()
	{
		this( "\n" );
	}
	
		public
	AttributeListStringifier( String delim )
	{
		mDelim	= delim;
		
	}
		public String
	stringify( Object o )
	{
		final AttributeList	attrList	= (AttributeList)o;
		final Iterator	iter	= attrList.iterator();
		
		return( IteratorStringifier.stringify( iter, mDelim ) );
	}
}

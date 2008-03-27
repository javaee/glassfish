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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/AttributeComparator.java,v 1.2 2005/11/08 22:40:21 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:40:21 $
 */
 

package com.sun.cli.jmxcmd.util.jmx;

import javax.management.Attribute;

public final class AttributeComparator implements java.util.Comparator
{
	public static final AttributeComparator	INSTANCE	= new AttributeComparator();
	
	private	AttributeComparator()	{}
	
		public int
	compare( Object o1, Object o2 )
	{
		final Attribute	attr1	= (Attribute)o1;
		final Attribute	attr2	= (Attribute)o2;
		
		int	result	= attr1.getName().compareTo( attr2.getName() );
		if ( result == 0 )
		{
			result	= attr1.getValue().toString().compareTo( attr2.getValue().toString() );
		}
		
		return( result );
	}
	
		public boolean
	equals( Object other )
	{
		return( other instanceof AttributeComparator );
	}
}



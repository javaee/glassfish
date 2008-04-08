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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/MethodComparator.java,v 1.2 2005/11/08 22:39:22 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:39:22 $
 */
 

package com.sun.cli.jcmd.util.misc;

import java.lang.reflect.Method;

/**
	Caution: this Comparator may be inconsistent with equals() because it ignores the description.
 */
public final class MethodComparator implements java.util.Comparator
{
	public static final MethodComparator	INSTANCE	= new MethodComparator();
	
	private	MethodComparator()	{}
	
		public int
	compare( Object o1, Object o2 )
	{
		final Method	m1	= (Method)o1;
		final Method	m2	= (Method)o2;
		
		int	result	= m1.getName().compareTo( m2.getName() );
		if ( result == 0 )
		{
			result	= m1.toString().compareTo( m2.toString() );
		}
		
		return( result );
	}
	
		public boolean
	equals( Object other )
	{
		return( other instanceof MethodComparator );
	}
}
	
	




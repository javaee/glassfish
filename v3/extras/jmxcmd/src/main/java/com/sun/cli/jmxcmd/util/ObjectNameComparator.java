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
package com.sun.cli.jmxcmd.util;

import javax.management.ObjectName;

public final class ObjectNameComparator implements java.util.Comparator<ObjectName>
{
	public static final ObjectNameComparator	INSTANCE	= new ObjectNameComparator();
	
	private	ObjectNameComparator()	{}
	
		public int
	compare( final ObjectName o1, final ObjectName o2 )
	{
	    int result  = 0;
	    
	    if ( o1 == null && o2 == null )
	    {
	        result  = 0;
	    }
	    else if ( o1 == null )
	    {
	        result  = -1;
	    }
	    else if ( o2 == null )
	    {
	        result  = 1;
	    }
	    else
	    {
    		final String name1	= o1.getCanonicalName();
    		final String name2	= o2.getCanonicalName();
    		
    		result  = name1.toString().compareTo( name2.toString() );
		}
		
		return result;
	}
	
		public boolean
	equals( final Object other )
	{
		return( other instanceof ObjectNameComparator );
	}
}



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
package com.sun.cli.jcmd.util.misc;

/**
	Provides a variety of useful utilities for computing hashCode().
 */
 
public final class ObjectUtil
{
		private
	ObjectUtil( )
	{
		// disallow instantiation
	}
	
	
		public static int
	hashCode( final boolean value )
	{
	    return value ? 1 : 0;
	}
	
		public static int
	hashCode( final Object... items )
	{
	    int result  = 0;
	    
	    for( final Object item : items )
	    {
	        result ^= hashCode( item );
	    }
	    return result;
	}
	
		public static int
	hashCode( final Object o )
	{
	    return o == null ? 0 : o.hashCode();
	}
	
		public static int
	hashCode( final long value )
	{
	    return (int)value ^ (int)(value >> 32);
	}
	
		public static int
	hashCode( final double value )
	{
	    return new Double( value ).hashCode();
	}
	
        public static boolean
    equals( final Object s1, final Object s2 )
    {
        boolean equals  = false;
        
        if ( s1 == s2 )
        {
            equals  = true;
        }
        else if ( s1 != null )
        {
            equals  = s1.equals( s2 );
        }
        else
        {
            // s1 is null and s2 isn't
            equals  = false;
        }
        
        return equals;
    }
}


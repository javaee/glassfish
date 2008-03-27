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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/MBeanAttributeInfoComparator.java,v 1.4 2005/11/08 22:40:22 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:40:22 $
 */
 

package com.sun.cli.jmxcmd.util.jmx;

import javax.management.MBeanAttributeInfo;

import com.sun.cli.jmxcmd.util.jmx.stringifier.MBeanAttributeInfoStringifier;
import com.sun.cli.jmxcmd.util.jmx.stringifier.MBeanFeatureInfoStringifierOptions;

/**
	Caution: this Comparator may be inconsistent with equals() because it ignores the description.
 */
public final class MBeanAttributeInfoComparator implements java.util.Comparator
{
	private static final MBeanAttributeInfoStringifier		ATTRIBUTE_INFO_STRINGIFIER	=
		new MBeanAttributeInfoStringifier( new MBeanFeatureInfoStringifierOptions( false, ",") );
	
	public static final MBeanAttributeInfoComparator		INSTANCE	= new MBeanAttributeInfoComparator();
	
	private	MBeanAttributeInfoComparator()	{}
	
		public int
	compare( Object o1, Object o2 )
	{
		final String	s1	= ATTRIBUTE_INFO_STRINGIFIER.stringify( (MBeanAttributeInfo)o1 );
		final String	s2	= ATTRIBUTE_INFO_STRINGIFIER.stringify( (MBeanAttributeInfo)o2 );
		
		return( s1.compareTo( s2 ) );
	}
	
		public boolean
	equals( Object other )
	{
		return( other instanceof MBeanAttributeInfoComparator );
	}
}
	
	




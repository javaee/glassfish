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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/MBeanOperationInfoComparator.java,v 1.4 2005/11/08 22:40:22 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:40:22 $
 */
 

package com.sun.cli.jmxcmd.util.jmx;

import javax.management.MBeanOperationInfo;

import com.sun.cli.jmxcmd.util.jmx.stringifier.MBeanOperationInfoStringifier;
import com.sun.cli.jmxcmd.util.jmx.stringifier.MBeanFeatureInfoStringifierOptions;


/**
	Caution: this Comparator may be inconsistent with equals() because it ignores the description.
 */
public final class MBeanOperationInfoComparator implements java.util.Comparator
{
	private static final MBeanOperationInfoStringifier		OPERATION_INFO_STRINGIFIER	=
		new MBeanOperationInfoStringifier( new MBeanFeatureInfoStringifierOptions( false, ",") );
		
		
	public static final MBeanOperationInfoComparator	INSTANCE	= new MBeanOperationInfoComparator();
	
	private	MBeanOperationInfoComparator()	{}
	
		public int
	compare( Object o1, Object o2 )
	{
		final MBeanOperationInfoStringifier	sf	= OPERATION_INFO_STRINGIFIER;
		
		
		final MBeanOperationInfo	info1	= (MBeanOperationInfo)o1;
		final MBeanOperationInfo	info2	= (MBeanOperationInfo)o2;
		
		// we just want to sort based on name and signature; there can't be two operations with the
		// same name and same signature, so as long as we include the name and signature the
		// sorting will always be consistent.
		int	c	= info1.getName().compareTo( info2.getName() );
		if ( c == 0 )
		{
			// names the same, subsort on signature, first by number of params
			c	= info1.getSignature().length - info2.getSignature().length;
			if ( c == 0 )
			{
				// names the same, subsort on signature, first by number of params
				c	= sf.getSignature( info1 ).compareTo( sf.getSignature( info2 ) );
			}
			
		}
		
		return( c );
	}
	
		public boolean
	equals( Object other )
	{
		return( other instanceof MBeanOperationInfoComparator );
	}
}





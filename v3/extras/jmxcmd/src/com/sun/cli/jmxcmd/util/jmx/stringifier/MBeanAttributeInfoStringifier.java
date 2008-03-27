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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/stringifier/MBeanAttributeInfoStringifier.java,v 1.3 2005/11/15 20:21:49 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/15 20:21:49 $
 */
 
package com.sun.cli.jmxcmd.util.jmx.stringifier;

import java.lang.reflect.Array;
import javax.management.MBeanAttributeInfo;


import com.sun.cli.jcmd.util.stringifier.Stringifier;


public class MBeanAttributeInfoStringifier extends MBeanFeatureInfoStringifier implements Stringifier
{
	public final static MBeanAttributeInfoStringifier	DEFAULT	=
					new MBeanAttributeInfoStringifier();
	
		public
	MBeanAttributeInfoStringifier()
	{
		super();
	}
	
		public
	MBeanAttributeInfoStringifier( MBeanFeatureInfoStringifierOptions options )
	{
		super( options );
	}
	
		public String
	stringify( Object o )
	{
		MBeanAttributeInfo	attr	= (MBeanAttributeInfo)o;
		String	result	= attr.getName() + ":";
		if ( attr.isReadable() )
			result	= result + "r";
		if ( attr.isWritable() )
			result	= result + "w";
		result	= result + mOptions.mArrayDelimiter + getPresentationTypeString( attr.getType() );
		
		if ( mOptions.mIncludeDescription )
		{
			result	= result + ",\"" + attr.getDescription() + "\"";
		}
		
		return( result );
	}
}
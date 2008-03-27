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


import com.sun.cli.jcmd.util.stringifier.Stringifier;
import com.sun.cli.jcmd.util.stringifier.ArrayStringifier;


public class ModelMBeanInfoStringifier extends MBeanInfoStringifier implements Stringifier
{
	public static final ModelMBeanInfoStringifier	DEFAULT	= new ModelMBeanInfoStringifier();
	
		public
	ModelMBeanInfoStringifier()
	{
		super();
	}
	
		public
	ModelMBeanInfoStringifier( MBeanFeatureInfoStringifierOptions options )
	{
		super( options );
	}
	
		private String
	stringifyArray( Object [] a, Stringifier stringifier)
	{
		String	temp	= "";
		
		if ( a.length != 0 )
		{
			temp	= "\n" + ArrayStringifier.stringify( a, "\n", stringifier);
		}
		return( temp );
	}
	
		
	// subclass may override
		MBeanAttributeInfoStringifier
	getMBeanAttributeInfoStringifier( MBeanFeatureInfoStringifierOptions options )
	{
		return( new ModelMBeanAttributeInfoStringifier(options) );
	}
	
	// subclass may override
		MBeanOperationInfoStringifier
	getMBeanOperationInfoStringifier( MBeanFeatureInfoStringifierOptions options )
	{
		return( new ModelMBeanOperationInfoStringifier(options) );
	}
	
	// subclass may override
		MBeanConstructorInfoStringifier
	getMBeanConstructorInfoStringifier( MBeanFeatureInfoStringifierOptions options )
	{
		return( new ModelMBeanConstructorInfoStringifier(options) );
	}
	
	// subclass may override
		MBeanNotificationInfoStringifier
	getMBeanNotificationInfoStringifier( MBeanFeatureInfoStringifierOptions options )
	{
		return( new ModelMBeanNotificationInfoStringifier(options) );
	}
}






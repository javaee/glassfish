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

import javax.management.MBeanOperationInfo;

import com.sun.cli.jcmd.util.stringifier.Stringifier;

public class MBeanOperationInfoStringifier
	extends MBeanFeatureInfoStringifier implements Stringifier 
{
	public static final MBeanOperationInfoStringifier	DEFAULT	= new MBeanOperationInfoStringifier();
	
		public
	MBeanOperationInfoStringifier()
	{
		super( );
	}
	
		public
	MBeanOperationInfoStringifier( MBeanFeatureInfoStringifierOptions options )
	{
		super( options );
	}
	
		public static String
	getImpact( MBeanOperationInfo info )
	{
		String	impactStr	= null;
		
		switch( info.getImpact() )
		{
			default:								impactStr	= "unknown";	break;
			case MBeanOperationInfo.INFO:			impactStr	= "info";		break;
			case MBeanOperationInfo.ACTION:			impactStr	= "action";		break;
			case MBeanOperationInfo.ACTION_INFO:	impactStr	= "action-info";break;
		}
		
		return( impactStr );
	}
	
		public static String
	getSignature( MBeanOperationInfo info )
	{
		return( getSignature( info, MBeanFeatureInfoStringifierOptions.DEFAULT ) );
	}
	
		public static String
	getSignature( MBeanOperationInfo info, MBeanFeatureInfoStringifierOptions options )
	{
		return( ParamsToString( info.getSignature(), options ) );
	}
	
		public static String
	getDescription( MBeanOperationInfo info )
	{
		return( sOperationDelimiter + "\"" + info.getDescription() + "\"" );
	}
	
		public String
	stringify( Object o )
	{
		assert( o != null );
		final MBeanOperationInfo	op	= (MBeanOperationInfo)o;
		
		String	result	= getPresentationTypeString( op.getReturnType() ) + " " + op.getName() + "(";
		
		// create the signature string
		result	= result + getSignature( op, mOptions ) + ")";
					
		String impactStr	= getImpact( op );
		
		result	= result + sOperationDelimiter + "impact=" +impactStr;
			
		if ( mOptions.mIncludeDescription )
		{
			result	= result + getDescription( op );
		}
		
		return( result );
	}
}
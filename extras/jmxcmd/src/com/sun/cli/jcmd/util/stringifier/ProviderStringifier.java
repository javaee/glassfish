
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/stringifier/ProviderStringifier.java,v 1.2 2005/11/08 22:39:27 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:39:27 $
 */
 
package com.sun.cli.jcmd.util.stringifier;

import java.security.Provider;

/**
	Stringifies a java.security.Provider.
 */
 
public final class ProviderStringifier implements Stringifier
{
	public final static ProviderStringifier	DEFAULT	= new ProviderStringifier();
	
		public
	ProviderStringifier()
	{
	}
	
	
		public String
	stringify( Object object )
	{
		final Provider	provider	= (Provider)object;
		
		final StringBuffer	buf	= new StringBuffer();

		buf.append( provider.getInfo() );
		
		java.util.Iterator	iter	= provider.entrySet().iterator();
		while ( iter.hasNext() )
		{
			buf.append( iter.next().toString() + "\n" );
		}

		return( buf.toString() );
	}
}


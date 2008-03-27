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
package com.sun.cli.jcmd.util.stringifier;

/**
	Stringifies an object based on specified interfaces.
 */
public class InterfacesStringifier implements Stringifier
{
	private final StringifierRegistry	mRegistry;
	private final Class[]				mInterfaces;
	
		public 
	InterfacesStringifier( Class[] interfaces  )
	{
		this( StringifierRegistryImpl.DEFAULT, interfaces );
	}
	
		public 
	InterfacesStringifier( StringifierRegistry registry, Class[] interfaces)
	{
		mRegistry	= registry;
		mInterfaces	= interfaces;
	}
	
		private String
	stringifyAs( final Object o, final Class<?> theClass )
	{
		String	result	= null;
		if ( theClass.isAssignableFrom( o.getClass() ) )
		{
			final Stringifier	stringifier	= mRegistry.lookup( theClass );
			if ( stringifier != null )
			{
				result	= stringifier.stringify( o );
			}
		}
		return( result );
	}
	
		public String
	stringify( Object o )
	{
		String	result	= "";
		
		for( int i = 0; i < mInterfaces.length; ++i )
		{
			final Class	intf	= mInterfaces[ i ];
			
			final String s	= stringifyAs( o, intf );
			if ( s != null )
			{
				result	= result + intf.getName() + ": " + s + "\n";
			}
		}
		
		if ( result == null || result.length() == 0)
		{
			result	= o.toString();
		}
	
		return( result );
	}
}









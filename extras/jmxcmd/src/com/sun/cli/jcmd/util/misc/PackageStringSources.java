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

import java.lang.reflect.Constructor;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;


import com.sun.cli.jcmd.util.misc.ClassUtil;

/**
	A StringSource for a package which first looks for a class in the the package
	named "PackageStringSource", and then, if not found, attempts to locate
	a ResourceBundle with the name "Strings".
	<p>
	Also maintains a cache of such sources so that subsequent uses reuse prior ones.
 */
public class PackageStringSources
{
	static final Map<Class,StringSource>	mSources	= new HashMap<Class,StringSource>();
	
	/**
		Get a string source for the specified class, using the specified
		StringSource as its delegate.
	 */
		public static StringSource
	get( final Class theClass, final StringSource delegate )
	{
		StringSource	source	= mSources.get( theClass );
		if ( source == null )
		{
			source	= init( theClass, delegate );
			mSources.put( theClass, source );
		}

		return( source );
	}
	
	private PackageStringSources()	{}
	
		private static StringSource
	init( final Class theClass,  final StringSource delegate )
	{
		StringSource	source	= null;
		final String	packageName	= theClass.getPackage().getName();
		
		try
		{
			final String	classname	= packageName + ".PackageStrings";
			
			final Class	packageStringSourceClass	= ClassUtil.getClassFromName( classname );
			
			final Constructor	c	= packageStringSourceClass.getConstructor( new Class[] { StringSource.class } );
			
			source	= (StringSource)c.newInstance( new Object[] { delegate } );
		}
		catch( Exception e )
		{
			// no such class exists
			e.printStackTrace();
		}
		
		if ( source == null )
		{
			final ResourceBundle bundle	= ResourceBundle.getBundle( packageName + ".Strings",
										Locale.getDefault(), theClass.getClassLoader());
			
			source	= new ResourceBundleStringSource( bundle, delegate );
		}
		
		return( source );
	}
};




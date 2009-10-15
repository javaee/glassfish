/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/InvokeHelper.java,v 1.1 2003/11/21 21:23:52 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:52 $
 */
 

package com.sun.cli.jmxcmd.test.mbeans;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.glassfish.admin.amx.util.ClassUtil;
;


/**
	Utility class for simplifying the process of invoking a method on an Object.
 */
public class InvokeHelper
{
	final Object	mTarget;
	
		public 
	InvokeHelper( Object target )
	{
		mTarget	= target;
	}
	
		static public Class []
	StringsToClasses( String [] signature )
		throws ClassNotFoundException
	{
		final int	count	= Array.getLength( signature );
		
		Class []	classes	 = new Class [ count ];
		
		for( int i = 0; i < count; ++i )
		{
			classes[ i ]	= ClassUtil.getClassFromName( signature[ i ] );
		}
		
		return( classes );
	}
	
        public Object
    invoke(
    	String	name,
    	Object	params[],
    	String	signature[] )
    	throws NoSuchMethodException, ClassNotFoundException,
    			InvocationTargetException, IllegalAccessException
    {
    	Object	result	= null;
    	
    	final Class []	signatureClasses	= StringsToClasses( signature );
    	
    	final Method	method	= mTarget.getClass().getDeclaredMethod( name, signatureClasses );
    	
    	result	= method.invoke( mTarget, params );
    	
    	return( result );
    }
}



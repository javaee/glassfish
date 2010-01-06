/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * $Header: /m/jws/jmxcmd/optional/mbeans/com/sun/enterprise/jmx/kstat/kstat.java,v 1.2 2003/11/12 02:07:23 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 02:07:23 $
 */
package com.sun.enterprise.jmx.kstat;

import java.util.HashMap;
import java.util.Set;

import com.sun.cli.jcmd.util.misc.ClassUtil;
import com.sun.cli.jcmd.util.stringifier.SmartStringifier;

public final class kstat
{
	final String				mModuleName;
	final int					mInstanceNumber;
	final String				mkstatName;
	final HashMap				mAttributes;
	
	public static class kstatAttribute
	{
		public final String		mName;
		public final Object		mValue;
		
		
			private static Object
		tryType( Class theClass, String input )
		{
			Object	value	= null;
			
			try
			{
				value	= ClassUtil.InstantiateFromString( theClass, input );
			}
			catch( Exception e )
			{
				// ignore
			}
			return( value );
		}
		
			private static Object
		createValue( String input )
		{
			Object	value	= tryType( Long.class, input );
			if ( value == null )
			{
				value	= tryType( Double.class, input );
			}
			if ( value == null )
			{
				value	= input;
			}
			
			return( value );
		}
		
			public
		kstatAttribute( String name, String value )
		{
			mName	= name;
			mValue	= createValue( value );
		}
			public String
		toString()
		{
			return( mName + "=" + mValue );
		}
	};
	
		public
	kstat(
		String				moduleName,
		int					instanceNumber,
		String				kstatName )
	{
		mModuleName		= moduleName;
		mkstatName		= kstatName;
		mInstanceNumber	= instanceNumber;
		mAttributes		= new HashMap();
	}
		public void
	addAttribute( kstatAttribute attr )
	{
		mAttributes.put( attr.mName, attr );
	}
	
		String
	getModuleName()
	{
		return( mModuleName );
	}
	
		int
	getInstanceNumber()
	{
		return( mInstanceNumber );
	}
	
		String
	getName()
	{
		return( mkstatName );
	}
	
		Class
	getAttributeType( String name )
	{
		final Object	value	= getValue( name );
		return( value.getClass() );
	}
	
		String
	getScopedName( char delim )
	{
		return( getScopedName( mModuleName, mInstanceNumber, mkstatName, delim ) );
	}
	
		String
	getScopedName(  )
	{
		return( getScopedName( ':' ) );
	}
	
		public static String
	getScopedName( String moduleName, int instanceNumber, String name, char delim)
	{
		return( moduleName + delim + instanceNumber + delim + name );
	}
	
		public static String
	getScopedName( String moduleName, int instanceNumber, String name )
	{
		return( getScopedName( moduleName, instanceNumber, name, ':' ) );
	}
	
		Set
	getAttributeNames()
	{
		return( mAttributes.keySet() );
	}

		public Object
	getValue( String attributeName )
	{
		final kstatAttribute	attr	= (kstatAttribute)mAttributes.get( attributeName );
		
		Object	value	= null;
		if ( attr != null )
		{
			value	= attr.mValue;
		}
		
		return( value );
	}
	
		public String
	toString()
	{
		return( getScopedName( ':' ) + "\n" + SmartStringifier.toString( mAttributes ) );
	}
};



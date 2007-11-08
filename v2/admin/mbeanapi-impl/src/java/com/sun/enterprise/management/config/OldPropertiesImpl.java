/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
 
/*
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/OldPropertiesImpl.java,v 1.2 2005/12/25 03:39:34 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2005/12/25 03:39:34 $
 */

package com.sun.enterprise.management.config;

import javax.management.Attribute;
import javax.management.AttributeList;


import com.sun.appserv.management.util.misc.ThrowableMapper;

import com.sun.enterprise.management.support.oldconfig.OldProperties;
import com.sun.enterprise.management.support.Delegate;
import com.sun.enterprise.management.support.DelegateBase;

final class OldPropertiesImpl implements OldProperties
{
	final Delegate	mDelegate;
	
	private final static String[]	EMPTY_SIG				= new String[ 0 ];
	private final static String[]	GET_PROPERTY_VALUE_SIG	= new String[]{ String.class.getName() };
	private final static String[]	SET_PROPERTY_SIG		= new String[]{ Attribute.class.getName() };


		public
	OldPropertiesImpl( Delegate delegate )
	{
		mDelegate	= delegate;
	}
	
	/**
		We want to avoid throwing anything that is not standard on the client
		side.
	 */
		private void
	rethrowThrowable( final Throwable t )
	{
		final Throwable	result	= new ThrowableMapper( t ).map();
		
		if ( result instanceof Error )
		{
			throw (Error)result;
		}
		else if ( result instanceof RuntimeException )
		{
			throw (RuntimeException)result;
		}
		else
		{	
			throw new RuntimeException( result );
		}
	}
	
		public AttributeList
	getProperties()
	{
		try
		{
			return( (AttributeList)mDelegate.invoke( "getProperties", null, EMPTY_SIG ) );
		}
		catch( RuntimeException e )
		{
			rethrowThrowable( e );
		}
		return( null );
	}

		public String
	getPropertyValue( final String propertyName )
	{
		try
		{
			return( (String)mDelegate.invoke( "getPropertyValue",
						new Object[] { propertyName }, GET_PROPERTY_VALUE_SIG ) );
		}
		catch( RuntimeException e )
		{
			rethrowThrowable( e );
		}
		return( null );
	}

		public void
	setProperty( Attribute attr )
	{
		try
		{
			mDelegate.invoke( "setProperty", new Object[] { attr }, SET_PROPERTY_SIG );
		}
		catch( RuntimeException e )
		{
			rethrowThrowable( e );
		}
	}
}



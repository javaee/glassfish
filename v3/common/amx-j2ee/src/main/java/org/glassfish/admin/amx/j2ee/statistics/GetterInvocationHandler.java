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

package org.glassfish.admin.amx.j2ee.statistics;

import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.StringUtil;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
	Abstract base {@link InvocationHandler} for any getXXX() method.
	<br><b>Internal use only</b>
 */
public abstract class GetterInvocationHandler<T> implements InvocationHandler,Serializable
{
	static final long serialVersionUID = 7293181901362984709L;

	/**
	 */
		public
	GetterInvocationHandler()
	{
	}
	
	protected abstract T	    getValue( String name );
	protected abstract boolean	containsValue( String name );
	
	/**
	*/
		public Object
	invoke(
		Object		myProxy,
    	Method		method,
		Object[]	args )
   		throws java.lang.Throwable
   	{
   		Object			result	= null;
   		final String	methodName		= method.getName();
   		final int		numArgs	= args == null ? 0 : args.length;
   		
   		if ( numArgs == 0 && JMXUtil.isGetter( method ) )
   		{
   			final String	name	= StringUtil.stripPrefix( methodName, JMXUtil.GET );
   			
   			result	= getValue( name );
   			if ( result == null && ! containsValue( name ) )
   			{
   				throw new NoSuchMethodException( methodName );
   			}
   		}
   		else if ( method.getName().equals( "equals" ) &&
   			numArgs == 1 )
   		{
   			result	= new Boolean( equals( args[ 0 ] ) );
   		}
   		else if ( numArgs == 0 && method.getName().equals( "toString" ) &&
   			method.getReturnType() == String.class )
   		{
   			result	= this.toString();
   		}
   		else if ( numArgs == 0 && method.getName().equals( "hashCode" ) &&
   			method.getReturnType() == int.class )
   		{
   			result	= new Integer( this.hashCode() );
   		}
   		else
   		{
   			throw new IllegalArgumentException( methodName );
   		}

   		return( result );
   	}
   
}






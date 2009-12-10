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
package org.glassfish.admin.amx.util.stringifier;

import java.util.HashMap;
import java.util.Map;



/**
	Holds a lookup table for Stringifiers.   Certain Stringifier classes
	may use this registry to aid them in producing suitable output.
 */
public class StringifierRegistryImpl implements StringifierRegistry
{
	public static final StringifierRegistry	DEFAULT	= new StringifierRegistryImpl();
	
	private final Map<Class<?>,Stringifier>		mLookup;
	private final StringifierRegistry			mNextRegistry;
	
	/**
		Create a new registry with no next registry.
	 */
		public
	StringifierRegistryImpl()
	{
		this( null );
	}
	
	/**
		Create a new registry which is chained to an existing registry.
		
		When lookup() is called, if it cannot be found in this registry, then
		the chainee is used.
		
		@param registry	the registry to use if this registry fails to find a Stringifier
	 */
		public
	StringifierRegistryImpl( final StringifierRegistry registry )
	{
		mLookup			= new HashMap<Class<?>,Stringifier>();
		mNextRegistry	= registry;
	}
	
		public void
	add( final Class<?> theClass, final Stringifier stringifier )
	{
		if ( lookup( theClass ) != null )
		{
			throw new IllegalArgumentException( "Stringifier already registered for: " + theClass.getName() );
		}
		
		mLookup.remove( theClass );
		mLookup.put( theClass, stringifier );
	}
	
	
		public Stringifier
	lookup( final Class<?> theClass )
	{
		Stringifier		stringifier	= mLookup.get( theClass );
		
		if ( stringifier == null && mNextRegistry != null )
		{
			stringifier	= mNextRegistry.lookup( theClass );
		}
		
		return( stringifier );
	}
}




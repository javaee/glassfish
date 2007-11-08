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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/util/stringifier/StringifierRegistry.java,v 1.3 2005/12/25 03:46:09 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:46:09 $
 */
 
package com.sun.cli.util.stringifier;

import java.util.HashMap;
import java.util.Iterator;

import javax.management.*;

/*
	Holds a lookup table for Stringifiers.   Certain Stringifier classes
	may use this registry to aid them in producing suitable output.
 */
public class StringifierRegistry
{
	public static StringifierRegistry	DEFAULT	= new StringifierRegistry();
	
	private final HashMap						mLookup;
	private final StringifierRegistry			mNextRegistry;
	
	/*
		Create a new registry.  Use the DEFAULT registry if possible;
		certain Stringifier classes depend on it.
	 */
		public
	StringifierRegistry(  )
	{
		this( null );
	}
	
		public
	StringifierRegistry( StringifierRegistry registry )
	{
		mLookup			= new HashMap( );
		mNextRegistry	= registry;
	}
	
	/*
		Add a mapping from a Class to a Stringifier
		
		@param theClass	the Class to which the Stringifier should be associated
		@param stringifier	the Stringifier for the class
	 */
		public void
	add( Class theClass, Stringifier stringifier )
	{
		mLookup.remove( theClass );
		mLookup.put( theClass, stringifier );
	}
	
	
	/*
		Lookup a Stringifier from a Class.
		
		@param theClass	the Class
		@returns the Stringifier, or null if not found
	 */
		public Stringifier
	lookup( Class theClass )
	{
		Stringifier		stringifier	= (Stringifier)mLookup.get( theClass );
		
		if ( stringifier == null && mNextRegistry != null )
		{
		System.out.println( "can't find " + theClass.getName() );
			stringifier	= mNextRegistry.lookup( theClass );
		}
		
		return( stringifier );
	}
}




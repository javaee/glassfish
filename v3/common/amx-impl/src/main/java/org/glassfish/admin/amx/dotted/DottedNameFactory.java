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
package org.glassfish.admin.amx.dotted;

import java.util.Map;
import java.util.HashMap;


/*
	This class caches dotted name strings against their DottedName counterparts.
	
	This helps avoid the fairly expensive parse of the string, as well as minimizing
	creation of new objects.
 */
public final class DottedNameFactory
{
	static final Factory	sImpl	= newInstance();
	
	public interface Factory
	{
		//public DottedName	lookup( final String dottedNameString );
		public DottedName	get( final String dottedNameString );
		public void			clear();
		public void			add( final DottedName dn );
	}
	
	
		private
	DottedNameFactory()
	{
		// disallow instantiation outside newInstance()
	}
	
		public static Factory
	newInstance()
	{
		return( new Impl() );
	}
	
		public static Factory
	getInstance()
	{
		return( sImpl );
	}
		
	
	/* implements the cache */
	private static final class Impl implements Factory
	{
		final Map<String,DottedName>	mMapping;
		
			public
		Impl()
		{
			// disallow outside instantiation
			mMapping	= new HashMap<String,DottedName>( 600 );
		}
		
			public synchronized void
		add( final DottedName dottedName )
		{
			mMapping.put( dottedName.toString(), dottedName );
		}
		
			public synchronized void
		clear()
		{
			mMapping.clear();
		}
		
		
			public DottedName
		get( final String dottedNameString )
		{
			DottedName	dn	= lookup( dottedNameString );
			
			if ( dn == null )
			{
				dn	= new DottedName( dottedNameString );
				add( dn );
			}
			
			return( dn );
		}
		
		
			synchronized DottedName
		lookup( final String dottedNameString )
		{
			final DottedName	dn	= mMapping.get( dottedNameString );
			
			assert( dn == null || dn.toString().equals( dottedNameString ) );
			
			return( dn );
		}

	}
}



